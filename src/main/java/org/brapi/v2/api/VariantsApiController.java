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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.collections.IteratorUtils;
import org.brapi.v2.model.CallListResponse;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import fr.cirad.mgdb.importing.VcfImport;
import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.utils.Constants;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@CrossOrigin
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
    	boolean fGotRefDbId = body.getReferenceDbId() != null && !body.getReferenceDbId().isEmpty();

		String token = ServerinfoApiController.readToken(authorization);

		String module = null;
		int projId;
		Query varQuery = null, runQuery = null;

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
				variantIDs = new HashSet<>();
				for (String variantDbId : body.getVariantDbIds()) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only supply IDs of variant records from one program at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
					}
					module = info[0];
					variantIDs.add(info[1]);
				}
				varQuery = new Query(Criteria.where("_id").in(variantIDs));
			}
	    	else if (fGotRefDbId) {
	        	String[] info = GigwaSearchVariantsRequest.getInfoFromId(body.getReferenceDbId(), 2);
	        	module = info[0];
	    		List<Criteria> crits = new ArrayList<>();	    		
	        	crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_SEQUENCE).is(info[1]));
	    		if (body.getStart() != null)
	        		crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE).gte(body.getStart()));
	    		if (body.getEnd() != null)
	        		crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE).lte(body.getEnd()));
	    		varQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));
	    	}
			else if (fGotVariantSets) {
				HashMap<Integer, Set<String>> runsByProject = new HashMap<>();
		    	for (String variantSetDbId : body.getVariantSetDbIds()) {
		    		String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 3);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only supply IDs of variantSet records from one program at a time!");
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
		    	runQuery = new Query(new Criteria().orOperator(orCrits.toArray(new Criteria[orCrits.size()])));
				runQuery.fields().exclude(VariantRunData.FIELDNAME_SAMPLEGENOTYPES);
			}
			
			if (!tokenManager.canUserReadDB(token, module)) {
				status.setMessage("You are not allowed to access this content");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.FORBIDDEN);
			}
			
			List<AbstractVariantData> varList;
			if (varQuery != null) {
				varQuery.limit(body.getPageSize());
				if (page > 0)
					varQuery.skip(page * body.getPageSize());
				varList = IteratorUtils.toList(MongoTemplateManager.get(module).find(varQuery, VariantData.class).iterator());
				
				// we may need to grab functional annotations from VRD records (FIXME: these should really be duplicated into VariantData)
            	if (!fGotVariants)
            		variantIDs = varList.stream().map(avd -> avd.getVariantId()).collect(Collectors.toList());
            	
            	HashMap<String, AbstractVariantData> variantsById = new LinkedHashMap<>();
            	for (AbstractVariantData variant : varList)
            		variantsById.put(variant.getVariantId(), variant);
            	
            	Query q = new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(variantIDs));
            	q.fields().include(AbstractVariantData.SECTION_ADDITIONAL_INFO);
            	for (VariantRunData vrd : MongoTemplateManager.get(module).find(q, VariantRunData.class))
            		variantsById.get(vrd.getVariantId()).getAdditionalInfo().putAll(vrd.getAdditionalInfo());	// FIXE: this is sub-optimal as it may be called several times for the same variant
			}
			else if (runQuery != null)
	        	varList = VariantsApiController.getSortedVariantListChunk(MongoTemplateManager.get(module), VariantRunData.class, runQuery, page * body.getPageSize(), body.getPageSize());
			else {
				status.setMessage("At least a variantDbId, a variantSetDbId, or a referenceDbId must be specified as parameter!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
			}

            ArrayList<String> headerList = null;
            boolean fAnnStyle = true;

        	for (AbstractVariantData dbVariant : varList) {
        		Variant variant = new Variant();
        		variant.setVariantDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + (dbVariant instanceof VariantRunData ? ((VariantRunData) dbVariant).getId().getVariantId() : ((VariantData) dbVariant).getId()));
        		List<String> alleles = dbVariant.getKnownAlleles();
        		if (alleles.size() > 0)
        			variant.setReferenceBases(alleles.get(0));
        		if (alleles.size() > 1)
        			variant.setAlternateBases(alleles.subList(1, alleles.size()));
        		variant.setVariantType(dbVariant.getType());
        		if (dbVariant.getReferencePosition() != null) {
	        		variant.setReferenceName(dbVariant.getReferencePosition().getSequence());
	        		variant.setStart((int) dbVariant.getReferencePosition().getStartSite());
	        		variant.setEnd((int) (dbVariant.getReferencePosition().getEndSite() != null ? dbVariant.getReferencePosition().getEndSite() : (variant.getReferenceBases() != null ? (variant.getStart() + variant.getReferenceBases().length() - 1) : null)));
        		}

        		List<String> variantNames = Arrays.asList(dbVariant.getVariantId());
        		if (dbVariant.getSynonyms() != null && !dbVariant.getSynonyms().isEmpty())
        			for (TreeSet<String> synsForAType : dbVariant.getSynonyms().values())
        				variantNames.addAll(synsForAType);
    			variant.setVariantNames(variantNames);
        		
        		Map<String, Object> annotations = new HashMap<>();
                for (String subKey : dbVariant.getAdditionalInfo().keySet()) {
                    if (subKey.equals(VcfImport.ANNOTATION_FIELDNAME_ANN) || subKey.equals(VcfImport.ANNOTATION_FIELDNAME_CSQ) || subKey.equals(VcfImport.ANNOTATION_FIELDNAME_EFF)) {                    	
                    	if (headerList == null) {	// go get it
                            fAnnStyle = !subKey.equals(VcfImport.ANNOTATION_FIELDNAME_EFF);
                    		
                    		BasicDBObject fieldHeader = new BasicDBObject(Constants.INFO_META_DATA + "." + (fAnnStyle ? VcfImport.ANNOTATION_FIELDNAME_ANN : VcfImport.ANNOTATION_FIELDNAME_EFF) + "." + Constants.DESCRIPTION, 1);
                            if (fAnnStyle)
                            	fieldHeader.put(Constants.INFO_META_DATA + "." + VcfImport.ANNOTATION_FIELDNAME_CSQ + "." + Constants.DESCRIPTION, 1);
                            
        	                MongoCollection<Document> vcfHeaderColl = MongoTemplateManager.get(module).getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));
        	                BasicDBList vcfHeaderQueryOrList = new BasicDBList();
        	                for (String key : fieldHeader.keySet())
        	                	vcfHeaderQueryOrList.add(new BasicDBObject(key, new BasicDBObject("$exists", true)));
        	
        	                Document vcfHeaderEff = vcfHeaderColl.find(new BasicDBObject("$or", vcfHeaderQueryOrList)).projection(fieldHeader).first();
        	                
        	                headerList = new ArrayList<>();
        	                if (!fAnnStyle)
        	                	headerList.add("Consequence");	// EFF style annotations
        	                Document annInfo = (Document) ((Document) vcfHeaderEff.get(Constants.INFO_META_DATA)).get(fAnnStyle ? VcfImport.ANNOTATION_FIELDNAME_ANN : VcfImport.ANNOTATION_FIELDNAME_EFF);
        	                if (annInfo == null && fAnnStyle)
        	                	annInfo = (Document) ((Document) vcfHeaderEff.get(Constants.INFO_META_DATA)).get(VcfImport.ANNOTATION_FIELDNAME_CSQ);
        	                if (annInfo != null) {
        	                    String header = (String) annInfo.get(Constants.DESCRIPTION);
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

        	int nNextPage = page + 1;
        	TokenPagination pagination = new TokenPagination();
    		pagination.setPageSize(body.getPageSize());
    		pagination.setCurrentPageToken("" + page);
    		if (!varList.isEmpty())
    			pagination.setNextPageToken("" + nNextPage);
    		if (page > 0)
    			pagination.setPrevPageToken("" + (page - 1));
			metadata.setPagination(pagination);

        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(vlr, HttpStatus.INTERNAL_SERVER_ERROR);
        }

		return new ResponseEntity<>(vlr, HttpStatus.OK);
    }

	protected static List<AbstractVariantData> getSortedVariantListChunk(MongoTemplate mongoTemplate, Class varClass, Query varQuery, int skip, int limit) {
		varQuery.collation(org.springframework.data.mongodb.core.query.Collation.of("en_US").numericOrderingEnabled());
		varQuery.with(Sort.by(Order.asc(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_SEQUENCE), Order.asc(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE)));
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
//        			call.setVariantDbId(info[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getVariantId());
//        			call.setVariantName(call.getVariantDbId());
//        			call.setCallSetDbId(info[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + sampleIndividuals.get(spId) + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
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
	public ResponseEntity<CallListResponse> variantsVariantDbIdCallsGet(String variantDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, String pageToken, Integer pageSize, String authorization) throws SocketException, UnknownHostException, UnsupportedEncodingException {
		CallsSearchRequest csr = new CallsSearchRequest();
		csr.setExpandHomozygotes(expandHomozygotes);
		csr.setUnknownString(unknownString);
		csr.setSepUnphased(sepUnphased);
		csr.setSepPhased(sepPhased);
		csr.setPageSize(pageSize);
		csr.setPageToken(pageToken);
		if (variantDbId != null)
			csr.setVariantDbIds(Arrays.asList(variantDbId));
		
		return callsApiController.searchCallsPost(csr, authorization);
	}

//	@Override
//	public ResponseEntity<VariantSingleResponse> variantsVariantDbIdGet(String variantDbId, String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
