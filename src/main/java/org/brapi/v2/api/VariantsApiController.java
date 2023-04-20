package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.collections.IteratorUtils;
import org.brapi.v2.model.CallsListResponse;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.TokenPagination;
import org.brapi.v2.model.Variant;
import org.brapi.v2.model.VariantListResponse;
import org.brapi.v2.model.VariantListResponseResult;
import org.brapi.v2.model.VariantsSearchRequest;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import fr.cirad.mgdb.importing.VcfImport;
import fr.cirad.mgdb.model.mongo.maintypes.Assembly;
import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

import htsjdk.variant.variantcontext.VariantContext.Type;
import io.swagger.annotations.ApiParam;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.IndexOfArray.arrayOf;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@Controller
public class VariantsApiController implements VariantsApi {

    private static final Logger log = LoggerFactory.getLogger(VariantsApiController.class);

//    private final ObjectMapper objectMapper;
//
//    private final HttpServletRequest request;
//    	
//    @Autowired private MongoBrapiCache cache;
    
    @Autowired private CallsApiController callsApiController;
    
    @Autowired private AbstractTokenManager tokenManager;
    
//    @org.springframework.beans.factory.annotation.Autowired
//    public VariantsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
//        this.objectMapper = objectMapper;
//        this.request = request;
//    }

    public ResponseEntity<VariantListResponse> searchVariantsPost(@ApiParam(value = "Variant Search request") @Valid @RequestBody VariantsSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	boolean fGotVariants = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
    	boolean fGotVariantSets = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
    	if (body.getReferenceDbId() != null && !body.getReferenceDbId().isEmpty() && (body.getReferenceDbIds() == null || body.getReferenceDbId().isEmpty()))
    		body.setReferenceDbIds(Arrays.asList(body.getReferenceDbId()));
    	boolean fGotRefDbIds = body.getReferenceDbIds() != null && !body.getReferenceDbIds().isEmpty();
    	boolean fGotRefSetDbIds = body.getReferenceSetDbIds() != null && !body.getReferenceSetDbIds().isEmpty();

		String token = ServerinfoApiController.readToken(authorization);

		String module = null;
		Integer projId = null;
		Integer assemblyIdForReturnedPositions = null;
		List<Criteria> varQueryCrits = new ArrayList<>(), runQueryCrits = new ArrayList<>();

		VariantListResponseResult result = new VariantListResponseResult();
		VariantListResponse vlr = new VariantListResponse();
    	MetadataTokenPagination metadata = new MetadataTokenPagination();
		vlr.setMetadata(metadata);
		vlr.setResult(result);
		Status status = new Status();

        if (body.getPageSize() == null || body.getPageSize() > VariantsApi.MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE)
        	body.setPageSize(VariantsApi.MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE);
        int page = body.getPageToken() == null ? 0 : Integer.parseInt(body.getPageToken());

		Collection<String> variantIDs = null;
		try {
			if (fGotVariants) {
				variantIDs = new LinkedHashSet<>();
				for (String variantDbId : body.getVariantDbIds()) {
					String[] info = Helper.getInfoFromId(variantDbId, 2);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only request variant records from one program at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
					}
					module = info[0];
					variantIDs.add(info[1]);
				}
				varQueryCrits.add(Criteria.where("_id").in(variantIDs));
			}
	    	else if (fGotRefDbIds || fGotRefSetDbIds) {
		    	if (!fGotRefDbIds) { // select all references in this referenceSet 
		    		for (String referenceSetDbId : body.getReferenceSetDbIds()) {
		    			String[] info = Helper.getInfoFromId(referenceSetDbId, 3);
						if (module != null && !module.equals(info[0])) {
							status.setMessage("You may only request variant records from one program at a time!");
							metadata.addStatusItem(status);
							return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
						}
			        	module = info[0];
			        	Integer assemblyId = info[2].isEmpty() ? null : Integer.parseInt(info[2]);
			        	for (String ref : MongoTemplateManager.get(module).findDistinct(new Query(), assemblyId != null ? GenotypingProject.FIELDNAME_CONTIGS + "." + assemblyId : GenotypingProject.FIELDNAME_SEQUENCES, GenotypingProject.class, String.class))
			        		body.addReferenceDbIdsItem(referenceSetDbId + Helper.ID_SEPARATOR + ref);
		    		}

			    	if (body.getReferenceDbIds() == null || body.getReferenceDbIds().isEmpty()) {
						status.setMessage("Unable to find any references for the supplied referenceSet(s)!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
			    	}
			    }

		    	HashMap<String /*refPos path*/, List<String> /*sequences*/> seqsByAssembly = new HashMap<>();
	        	for (String referenceDbId : body.getReferenceDbIds()) {
		        	String[] info = Helper.getInfoFromId(referenceDbId, 4);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only request variant records from one program at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
					}
		        	module = info[0];
		        	projId = Integer.parseInt(info[1]);
		        	Integer assemblyId = info[2].isEmpty() ? null : Integer.parseInt(info[2]);
		        	if (assemblyIdForReturnedPositions == null)
		        		 assemblyIdForReturnedPositions = assemblyId;	// if there are several then the first encountered will be used

		        	String refPosPath = assemblyId != null ? VariantData.FIELDNAME_POSITIONS + "." + assemblyId : VariantData.FIELDNAME_REFERENCE_POSITION;
		        	List<String> asmSeqs = seqsByAssembly.get(refPosPath);
		        	if (asmSeqs == null) {
		        		asmSeqs = new ArrayList<>();
		        		seqsByAssembly.put(refPosPath, asmSeqs);
		        	}
		        	asmSeqs.add(info[3]);
	        	}

	        	varQueryCrits.add(new Criteria().orOperator(seqsByAssembly.entrySet().stream().map(e -> Criteria.where(e.getKey() + "." + ReferencePosition.FIELDNAME_SEQUENCE).in(e.getValue())).toList()));
	    		if (body.getStart() != null)
	    			varQueryCrits.add(new Criteria().orOperator(seqsByAssembly.keySet().stream().map(k -> new Criteria().orOperator(Criteria.where(k + "." + ReferencePosition.FIELDNAME_START_SITE).gte(body.getStart()), Criteria.where(k + "." + ReferencePosition.FIELDNAME_END_SITE).gte(body.getStart()))).toList()));
	    		if (body.getEnd() != null)
	    			varQueryCrits.add(new Criteria().orOperator(seqsByAssembly.keySet().stream().map(k -> Criteria.where(k + "." + ReferencePosition.FIELDNAME_START_SITE).lte(body.getEnd())).toList()));

	    	}
			else if (fGotVariantSets) {
				HashMap<Integer, Set<String>> runsByProject = new HashMap<>();
		    	for (String variantSetDbId : body.getVariantSetDbIds()) {
		    		String[] info = Helper.getInfoFromId(variantSetDbId, 3);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only request variant records from one program at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
					}
					module = info[0];

					projId = Integer.parseInt(info[1]);
					if (!tokenManager.canUserReadProject(token, info[0], Integer.parseInt(info[1]))) {
						status.setMessage("You are not allowed to access this content");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.FORBIDDEN);
					}
					
					Set<String> projectRuns = runsByProject.get(projId);
					if (projectRuns == null) {
						projectRuns = new HashSet<>();
						runsByProject.put(projId, projectRuns);
					}
					projectRuns.add(info[2]);
		    	}
		    	
		    	List<Criteria> orCrits = new ArrayList<>();
		    	for (Integer proj : runsByProject.keySet())
		    		orCrits.add(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(proj), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).in(runsByProject.get(proj))));
		    	runQueryCrits.add(new Criteria().orOperator(orCrits.toArray(new Criteria[orCrits.size()])));
			}
			
			if (module == null) {
				status.setMessage("You must provide one of variantDbIds, referenceSetDbIds, referenceDbIds, variantSetDbIds! Only the first filter (in given order) encountered will be applied.");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
			}
			
			if (!tokenManager.canUserReadDB(token, module)) {
				status.setMessage("You are not allowed to access this content");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.FORBIDDEN);
			}
			
			MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
			if (fGotVariants || fGotVariantSets) {
				if (assemblyIdForReturnedPositions == null) {	// in this case we haven't searched for an assembly
					List<Assembly> assemblies = mongoTemplate.findAll(Assembly.class);
					if (!assemblies.isEmpty())
						assemblyIdForReturnedPositions = assemblies.get(0).getId();	// otherwise we must be working on an old-style DBs that does not support assemblies
				}
				
				// account for start / end filters in this case too
				if (body.getStart() != null || body.getEnd() != null) {
					String refPosPath = assemblyIdForReturnedPositions != null ? VariantData.FIELDNAME_POSITIONS + "." + assemblyIdForReturnedPositions : VariantData.FIELDNAME_REFERENCE_POSITION;
					List<Criteria> crit = fGotVariants ? varQueryCrits: runQueryCrits;
		    		if (body.getStart() != null)
		    			crit.add(new Criteria().orOperator(Criteria.where(refPosPath + "." + ReferencePosition.FIELDNAME_START_SITE).gte(body.getStart()), Criteria.where(refPosPath + "." + ReferencePosition.FIELDNAME_END_SITE).gte(body.getStart())));
		    		if (body.getEnd() != null)
		    			crit.add(Criteria.where(refPosPath + "." + ReferencePosition.FIELDNAME_START_SITE).lte(body.getEnd()));
				}
			}

			AtomicLong totalCount = new AtomicLong();
			Thread countThread = null;
			List<? extends AbstractVariantData> varList;
			HashMap<String, Integer> projectByVariant = projId == null ? new HashMap<>() : null;	// if searching by variantDbIds we have no info what projectId to set in referenceDbId and referenceSetDbId: find this out 
			if (!varQueryCrits.isEmpty()) {
				final Query finalVarQuery = new Query(new Criteria().andOperator(varQueryCrits));
				countThread = new Thread() {
					public void run() {
						totalCount.set(mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantData.class)).countDocuments(finalVarQuery.getQueryObject()));
					}
				};
				countThread.start();

                if (fGotVariants) {
                    //use aggregation to keep the order of variantDbIds
                    MatchOperation match = match(new Criteria().andOperator(varQueryCrits));
                    AddFieldsOperation addFields = AddFieldsOperation.addField("_order").withValue(arrayOf(variantIDs).indexOf("$_id")).build();
                    SortOperation sort = sort(Sort.by(Sort.Direction.ASC, "_order"));
                    Aggregation aggregation = Aggregation.newAggregation(match, addFields, sort, Aggregation.skip(page * body.getPageSize()), Aggregation.limit(body.getPageSize())).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                    varList = mongoTemplate.aggregate(aggregation, VariantData.class, VariantData.class).getMappedResults();
                } else {
                	finalVarQuery.limit(body.getPageSize());
					if (page > 0)
						finalVarQuery.skip(page * body.getPageSize());
					varList = IteratorUtils.toList(mongoTemplate.find(finalVarQuery, VariantData.class).iterator());
				}
				// we may need to grab functional annotations from VRD records (FIXME: these should really be duplicated into VariantData)
            	if (!fGotVariants)
            		variantIDs = varList.stream().map(avd -> avd.getVariantId()).collect(Collectors.toList());
            	
            	HashMap<String, AbstractVariantData> variantsById = new LinkedHashMap<>();
            	for (AbstractVariantData variant : varList)
            		variantsById.put(variant.getVariantId(), variant);
            	
            	finalVarQuery.fields().include(AbstractVariantData.SECTION_ADDITIONAL_INFO);
            	
            	Query vrdQueryForAI = new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(variantsById.keySet()));
            	vrdQueryForAI.fields().include(VariantRunData.SECTION_ADDITIONAL_INFO);
            	mongoTemplate.find(vrdQueryForAI, VariantRunData.class).forEach(vrd -> {
        			variantsById.get(vrd.getVariantId()).getAdditionalInfo().putAll(vrd.getAdditionalInfo());	// FIXME: this is sub-optimal as it may be called several times for the same variant
//        			Optional.ofNullable(variantsById.get(vrd.getVariantId())).ifPresent(variant -> variant.getAdditionalInfo().putAll(vrd.getAdditionalInfo()));	// FIXME: this is sub-optimal as it may be called several times for the same variant
            		if (projectByVariant != null && !projectByVariant.containsKey(vrd.getVariantId()))
            			projectByVariant.put(vrd.getVariantId(), vrd.getId().getProjectId());
            	});
			}
			else if (!runQueryCrits.isEmpty()) {
				final Query finalRunQuery = new Query(new Criteria().andOperator(runQueryCrits));
				countThread = new Thread() {
					public void run() {
						totalCount.set(mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantRunData.class)).countDocuments(finalRunQuery.getQueryObject()));
					}
				};
				countThread.start();
				finalRunQuery.fields().exclude(VariantRunData.FIELDNAME_SAMPLEGENOTYPES);
	        	varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, assemblyIdForReturnedPositions, VariantRunData.class, finalRunQuery, page * body.getPageSize(), body.getPageSize());
			}
			else {
				status.setMessage("At least a variantDbId, a variantSetDbId, or a referenceDbId must be specified as parameter!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
			}

            ArrayList<String> headerList = null;
            boolean fAnnStyle = true;

        	for (AbstractVariantData dbVariant : varList) {
        		Variant variant = new Variant();
        		variant.setVariantDbId(module + Helper.ID_SEPARATOR + (dbVariant instanceof VariantRunData ? ((VariantRunData) dbVariant).getId().getVariantId() : ((VariantData) dbVariant).getId()));
        		List<String> alleles = dbVariant.getKnownAlleles();
        		if (alleles.size() > 0)
        			variant.setReferenceBases(alleles.get(0));
        		if (alleles.size() > 1)
        			variant.setAlternateBases(alleles.subList(1, alleles.size()));
        		variant.setVariantType(dbVariant.getType());
        		ReferencePosition refPos = assemblyIdForReturnedPositions != null ? dbVariant.getReferencePosition(assemblyIdForReturnedPositions) : dbVariant.getDefaultReferencePosition();
        		if (refPos != null) {
        			Integer nProjectIdForVariant = projId != null ? projId : projectByVariant.get(dbVariant.getVariantId());
        			if (nProjectIdForVariant != null) {
	        			variant.setReferenceSetDbId(module + Helper.ID_SEPARATOR + nProjectIdForVariant + Helper.ID_SEPARATOR + (assemblyIdForReturnedPositions == null ? "" : assemblyIdForReturnedPositions));
				    	variant.setReferenceDbId(variant.getReferenceSetDbId() + Helper.ID_SEPARATOR + refPos.getSequence());
						if (status.getMessage() == null) {
			        		status.setMessage("Returned variant positions are based on referenceSetDbId " + variant.getReferenceSetDbId());
			        		status.setMessageType(Status.MessageTypeEnum.INFO);
							metadata.addStatusItem(status);
			        	}
        			}
	        		variant.setReferenceName(refPos.getSequence());
	        		variant.setStart((int) refPos.getStartSite());
	        		if (refPos.getEndSite() != null)
	        			variant.setEnd(refPos.getEndSite().intValue());
	        		else if (Type.SNP.toString().equals(dbVariant.getType()))
	        			variant.setEnd(variant.getStart());
	        		else if (variant.getReferenceBases() != null)
	        			variant.setEnd(variant.getStart() + variant.getReferenceBases().length() - 1);
        		}

        		List<String> variantNames = new ArrayList<>() {{ add(dbVariant.getVariantId()); }};
        		if (dbVariant.getSynonyms() != null && !dbVariant.getSynonyms().isEmpty())
        			for (TreeSet<String> synsForAType : dbVariant.getSynonyms().values())
        				variantNames.addAll(synsForAType);
    			variant.setVariantNames(variantNames);
        		
        		Map<String, Object> annotations = new HashMap<>();
                for (String subKey : dbVariant.getAdditionalInfo().keySet()) {
                    if (subKey.equals(VcfImport.ANNOTATION_FIELDNAME_ANN) || subKey.equals(VcfImport.ANNOTATION_FIELDNAME_CSQ) || subKey.equals(VcfImport.ANNOTATION_FIELDNAME_EFF)) {                    	
                    	if (headerList == null) {	// go get it
                            fAnnStyle = !subKey.equals(VcfImport.ANNOTATION_FIELDNAME_EFF);
                    		
                    		BasicDBObject fieldHeader = new BasicDBObject(AbstractVariantData.VCF_CONSTANT_INFO_META_DATA + "." + (fAnnStyle ? VcfImport.ANNOTATION_FIELDNAME_ANN : VcfImport.ANNOTATION_FIELDNAME_EFF) + "." + AbstractVariantData.VCF_CONSTANT_DESCRIPTION, 1);
                            if (fAnnStyle)
                            	fieldHeader.put(AbstractVariantData.VCF_CONSTANT_INFO_META_DATA + "." + VcfImport.ANNOTATION_FIELDNAME_CSQ + "." + AbstractVariantData.VCF_CONSTANT_DESCRIPTION, 1);
                            
        	                MongoCollection<Document> vcfHeaderColl = MongoTemplateManager.get(module).getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));
        	                BasicDBList vcfHeaderQueryOrList = new BasicDBList();
        	                for (String key : fieldHeader.keySet())
        	                	vcfHeaderQueryOrList.add(new BasicDBObject(key, new BasicDBObject("$exists", true)));
        	
        	                Document vcfHeaderEff = vcfHeaderColl.find(new BasicDBObject("$or", vcfHeaderQueryOrList)).projection(fieldHeader).first();
        	                
        	                headerList = new ArrayList<>();
        	                if (!fAnnStyle)
        	                	headerList.add("Consequence");	// EFF style annotations
        	                Document annInfo = (Document) ((Document) vcfHeaderEff.get(AbstractVariantData.VCF_CONSTANT_INFO_META_DATA)).get(fAnnStyle ? VcfImport.ANNOTATION_FIELDNAME_ANN : VcfImport.ANNOTATION_FIELDNAME_EFF);
        	                if (annInfo == null && fAnnStyle)
        	                	annInfo = (Document) ((Document) vcfHeaderEff.get(AbstractVariantData.VCF_CONSTANT_INFO_META_DATA)).get(VcfImport.ANNOTATION_FIELDNAME_CSQ);
        	                if (annInfo != null) {
        	                    String header = (String) annInfo.get(AbstractVariantData.VCF_CONSTANT_DESCRIPTION);
        	                    if (header != null) {
        	                        // consider using the headers for additional info keySet
        	                    	String sBeforeFieldList = fAnnStyle ? ": " : " (";
        	                    	String[] headerField = header.substring(header.indexOf(sBeforeFieldList) + sBeforeFieldList.length(), fAnnStyle ? header.length() : header.indexOf(")")).replaceAll("'", "").split("\\|");
        	                        for (String head : headerField) {
        	                        	String headerName = head.replace("[", "").replace("]", "").trim();
        	                            headerList.add(headerList.size() == 1 && headerName.equals("Effefct_Impact") ? "Effect_Impact" : headerName);
        	                        }
        	                    }
        	                }
                    	}
                    	
                    	List<Map> functionalAnnotations = new ArrayList<>();
                        for (String effectDesc : ((String) dbVariant.getAdditionalInfo().get(subKey)).split(",")) {
	                    	int parenthesisPos = effectDesc.indexOf("(");
                        	List<String> fields = Helper.split(effectDesc.substring(parenthesisPos + 1).replaceAll("\\)", ""), "|");
    	                    if (!fAnnStyle)
    	                    	fields.add(0, effectDesc.substring(0, parenthesisPos));	// EFF style annotations
                        	LinkedHashMap<String, String> annotationMap = new LinkedHashMap<>();
                        	for (int i=0; i<fields.size(); i++)
                        		if (fields.get(i) != null && !fields.get(i).isEmpty())
                        			annotationMap.put(headerList.get(i), fields.get(i));
                        	functionalAnnotations.add(annotationMap);
                        }
                        annotations.put("transcriptEffects", functionalAnnotations);
                    } else if (!subKey.equals(VariantRunData.FIELDNAME_ADDITIONAL_INFO_EFFECT_GENE) && !subKey.equals(VariantRunData.FIELDNAME_ADDITIONAL_INFO_EFFECT_NAME)) {
                        annotations.put(subKey, dbVariant.getAdditionalInfo().get(subKey));
                    }
                }
                variant.setAdditionalInfo(annotations);
        		
        		result.addDataItem(variant);
        	}
        	
			if (assemblyIdForReturnedPositions != null && status.getMessage() == null) {
        		status.setMessage("Returned variant positions are based on assembly #" + assemblyIdForReturnedPositions + " (" + mongoTemplate.findById(assemblyIdForReturnedPositions, Assembly.class).getName() + ")");
        		status.setMessageType(Status.MessageTypeEnum.INFO);
				metadata.addStatusItem(status);
        	}

        	int nNextPage = page + 1;
        	TokenPagination pagination = new TokenPagination();
    		pagination.setPageSize(body.getPageSize());
    		pagination.setCurrentPageToken("" + page);
    		if (!varList.isEmpty())
    			pagination.setNextPageToken("" + nNextPage);
    		if (page > 0)
    			pagination.setPrevPageToken("" + (page - 1));
    		if (countThread != null) {
    			countThread.join(5000);
    			pagination.setTotalCount((int) totalCount.get());
    		}
			metadata.setPagination(pagination);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(vlr, HttpStatus.INTERNAL_SERVER_ERROR);
        }

		return new ResponseEntity<>(vlr, HttpStatus.OK);
    }

	protected static List<AbstractVariantData> getSortedVariantListChunk(MongoTemplate mongoTemplate, Integer nAssemblyId, Class varClass, Query varQuery, int skip, int limit) {
		String refPosPath = Assembly.getVariantRefPosPath(nAssemblyId);
		varQuery.collation(org.springframework.data.mongodb.core.query.Collation.of("en_US").numericOrderingEnabled());
		varQuery.with(Sort.by(Order.asc(refPosPath + "." + ReferencePosition.FIELDNAME_SEQUENCE), Order.asc(refPosPath + "." + ReferencePosition.FIELDNAME_START_SITE)));
		varQuery.skip(skip).limit(limit).cursorBatchSize(limit);
		return mongoTemplate.find(varQuery, varClass, mongoTemplate.getCollectionName(varClass));
	}

	@Override
	public ResponseEntity<VariantListResponse> variantsGet(String variantDbId, String variantSetDbId, String referenceSetDbId, Integer start, Integer end, String pageToken, Integer pageSize, String authorization) {
		VariantsSearchRequest vsr = new VariantsSearchRequest();
		if (variantDbId != null)
			vsr.setVariantDbIds(Arrays.asList(variantDbId));
		if (variantSetDbId != null)
			vsr.setVariantSetDbIds(Arrays.asList(variantSetDbId));
		vsr.setReferenceDbId(referenceSetDbId);
		vsr.setStart(start);
		vsr.setEnd(end);
		vsr.setPageToken(pageToken);
		vsr.setPageSize(pageSize);

		return searchVariantsPost(vsr, authorization);
	}
	
//	protected ResponseEntity<CallListResponse> buildCallListResponse(Query runQuery, MongoTemplate mongoTemplate, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, String pageToken, Integer pageSize) {
//    	String unknownGtCode = unknownString == null ? "-" : unknownString;
//    	String unPhasedSeparator = sepUnphased == null ? "/" : sepUnphased;
//    	String phasedSeparator = sepPhased == null ? "|" : URLDecoder.decode(sepPhased, "UTF-8");
//    	
//    	CallListResponse clr = new CallListResponse();
//    	CallsListResponseResult result = new CallsListResponseResult();
//    	result.setSepUnphased(unPhasedSeparator);
//		
//        try {
////	        long b4 = System.currentTimeMillis();
////        	mongoTemplate.getCollection("variantRunData").createIndex(new BasicDBObject(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_SEQUENCE, 1).append(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE, 1), new IndexOptions().collation(Collation.builder().locale("en_US").numericOrdering(true).build()));
//        	List<AbstractVariantData> varList = IExportHandler.getMarkerListWithCorrectCollation(mongoTemplate, VariantRunData.class, runQuery, page * numberOfMarkersToReturn, numberOfMarkersToReturn);
////        	System.err.println((System.currentTimeMillis() - b4) + " / " + variants.size()/* + ": " + variants*/);
//        	HashMap<Integer, String> previousPhasingIds = new HashMap<>();
//
//        	HashMap<Integer, String> sampleIndividuals = new HashMap<>();	// we are going to need the individual each sample is related to, in order to build callSetDbIds
//        	for (GenotypingSample gs : mongoTemplate.find(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(info[2]))), GenotypingSample.class))
//        		sampleIndividuals.put(gs.getId(), gs.getIndividual());
//
//        	for (AbstractVariantData v : varList) {
//        		VariantRunData vrd = (VariantRunData) v;
//        		for (Integer spId : vrd.getSampleGenotypes().keySet()) {
//        			SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
//					String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
//					boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
//					previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/
//
//					String gtCode = sg.getCode(), genotype;
//					if (gtCode == null || gtCode.length() == 0)
//						genotype = unknownGtCode;
//					else
//					{
//						List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
//						if (!Boolean.TRUE.equals(expandHomozygotes) && new HashSet<String>(alleles).size() == 1)
//							genotype = alleles.get(0);
//						else
//							genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
//					}
//        			Call call = new Call();
//        			ListValue lv = new ListValue();
//        			lv.addValuesItem(genotype);
//        			call.setGenotype(lv);
//        			call.setVariantDbId(info[0] + Helper.ID_SEPARATOR + vrd.getId().getVariantId());
//        			call.setVariantName(call.getVariantDbId());
//        			call.setCallSetDbId(info[0] + Helper.ID_SEPARATOR + sampleIndividuals.get(spId) + Helper.ID_SEPARATOR + spId);
//        			call.setCallSetName(call.getCallSetDbId());
//                	result.addDataItem(call);
//        		}
//        	}
//
//        	int nNextPage = page + 1;
//        	MetadataTokenPagination metadata = new MetadataTokenPagination();
//        	TokenPagination pagination = new TokenPagination();
//			pagination.setPageSize(result.getData().size());
//			pagination.setTotalCount((int) (variantSet.getVariantCount() * nCallSetCount));
//			pagination.setTotalPages(varList.isEmpty() ? 0 : (int) Math.ceil((float) pagination.getTotalCount() / pagination.getPageSize()));
//			pagination.setCurrentPageToken("" + page);
//			if (nNextPage < pagination.getTotalPages())
//				pagination.setNextPageToken("" + nNextPage);
//			if (page > 0)
//				pagination.setPrevPageToken("" + (page - 1));
//			metadata.setPagination(pagination);
//			clr.setMetadata(metadata);
//		
//			clr.setResult(result);
//			return new ResponseEntity<CallListResponse>(clr, HttpStatus.OK);
//        } catch (Exception e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//	}

	@Override
	public ResponseEntity<CallsListResponse> variantsVariantDbIdCallsGet(String variantDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, Integer page, Integer pageSize, String authorization) throws SocketException, UnknownHostException, UnsupportedEncodingException {
		CallsSearchRequest csr = new CallsSearchRequest();
		csr.setExpandHomozygotes(expandHomozygotes);
		csr.setUnknownString(unknownString);
		csr.setSepUnphased(sepUnphased);
		csr.setSepPhased(sepPhased);
		csr.setPageSize(pageSize);
		csr.setPage(page);
		if (variantDbId != null)
			csr.setVariantDbIds(Arrays.asList(variantDbId));
		
		return callsApiController.searchCallsPost(authorization, csr);
	}

//	@Override
//	public ResponseEntity<VariantSingleResponse> variantsVariantDbIdGet(String variantDbId, String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
