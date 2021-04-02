package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.api.cache.MongoBrapiCache;
import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.TokenPagination;
import org.brapi.v2.model.Variant;
import org.brapi.v2.model.VariantListResponse;
import org.brapi.v2.model.VariantListResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.mgdb.exporting.IExportHandler;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@RestController
public class VariantsApiController implements VariantsApi {

    private static final Logger log = LoggerFactory.getLogger(VariantsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
	private Integer MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE = 10000;
	
    @Autowired private MongoBrapiCache cache;
    
    @Autowired private SearchApiController searchApiController;
    
    @Autowired private AbstractTokenManager tokenManager;
    
    @org.springframework.beans.factory.annotation.Autowired
    public VariantsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<VariantListResponse> variantsGet(String variantDbId, String variantSetDbId, String pageToken, Integer pageSize, String authorization) {
		String token = ServerinfoApiController.readToken(authorization);

		String[] info = null;
		int projId, nVariantCount = 1;
		Query runQuery = null;
		MongoTemplate mongoTemplate = null;

		VariantListResponseResult result = new VariantListResponseResult();
		VariantListResponse vlr = new VariantListResponse();
    	MetadataTokenPagination metadata = new MetadataTokenPagination();
		Status status = new Status();
		HttpStatus httpCode = null;
		
        if (pageSize == null || pageSize > MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE)
        	pageSize = MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE;
        int page = pageToken == null ? 0 : Integer.parseInt(pageToken);    	

		try {
			if (variantDbId != null && !variantDbId.isEmpty()) {
				info = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2);
				mongoTemplate = MongoTemplateManager.get(info[0]);
				runQuery = new Query(Criteria.where("_id").is(info[1]));
			}
			else if (variantSetDbId != null && !variantSetDbId.isEmpty()) {
				info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 3);
				mongoTemplate = MongoTemplateManager.get(info[0]);
				projId = Integer.parseInt(info[1]);
				if (!tokenManager.canUserReadProject(token, info[0], info[1])) {
					httpCode = HttpStatus.FORBIDDEN;
					status.setMessage("You are not allowed to access this content");
					metadata.addStatusItem(status);
				}
				else {
					nVariantCount = cache.getVariantSet(mongoTemplate, variantSetDbId).getVariantCount();
					runQuery = new Query(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(projId), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])));
					runQuery.fields().exclude(VariantRunData.FIELDNAME_SAMPLEGENOTYPES);
				}
			} else {
				httpCode = HttpStatus.BAD_REQUEST;
				status.setMessage("A variantDbId or a variantSetDbId must be specified as parameter!");
				metadata.addStatusItem(status);				
			}
			
			if (runQuery != null) {
	//	        long b4 = System.currentTimeMillis();
	        	List<AbstractVariantData> varList = IExportHandler.getMarkerListWithCorrectCollation(mongoTemplate, variantDbId != null && !variantDbId.isEmpty() ? VariantData.class : VariantRunData.class, runQuery, page * pageSize, pageSize);
	//        	System.err.println((System.currentTimeMillis() - b4) + " / " + variants.size()/* + ": " + variants*/);

	        	for (AbstractVariantData dbVariant : varList) {
	        		Variant variant = new Variant();
	        		variant.setVariantDbId(info[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + (dbVariant instanceof VariantRunData ? ((VariantRunData) dbVariant).getId().getVariantId() : ((VariantData) dbVariant).getId()));
	        		List<String> alleles = dbVariant.getKnownAlleleList();
	        		if (alleles.size() > 0)
	        			variant.setReferenceBases(alleles.get(0));
	        		if (alleles.size() > 1)
	        			variant.setAlternateBases(alleles.subList(1, alleles.size()));
	        		variant.setVariantType(dbVariant.getType());
	        		variant.setReferenceName(dbVariant.getReferencePosition().getSequence());
	        		variant.setStart((int) dbVariant.getReferencePosition().getStartSite());
	        		variant.setEnd((int) (dbVariant.getReferencePosition().getEndSite() != null ? dbVariant.getReferencePosition().getEndSite() : (variant.getReferenceBases() != null ? (variant.getStart() + variant.getReferenceBases().length() - 1) : null)));
	        		if (!dbVariant.getSynonyms().isEmpty()) {
	        			List<String> synonyms = new ArrayList<>();
	        			for (TreeSet<String> synsForAType : dbVariant.getSynonyms().values())
	        				synonyms.addAll(synsForAType);
	        			variant.setVariantNames(synonyms);
	        		}
	        		result.addDataItem(variant);
	        	}

	        	int nNextPage = page + 1;
	        	TokenPagination pagination = new TokenPagination();
	    		pagination.setPageSize(pageSize);
	    		pagination.setTotalCount(httpCode == null ? nVariantCount : 0);
	    		pagination.setTotalPages(httpCode == null ? (int) Math.ceil((float) pagination.getTotalCount() / pageSize) : 0);
	    		pagination.setCurrentPageToken("" + page);
	    		if (nNextPage < pagination.getTotalPages())
	    			pagination.setNextPageToken("" + nNextPage);
	    		if (page > 0)
	    			pagination.setPrevPageToken("" + (page - 1));
				metadata.setPagination(pagination);
			}

        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
		
		vlr.setMetadata(metadata);		
		vlr.setResult(result);
		return new ResponseEntity<>(vlr, httpCode == null ? HttpStatus.OK : httpCode);
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
		csr.setVariantDbIds(Arrays.asList(variantDbId));
		
		return searchApiController.searchCallsPost(csr, authorization);
	}

//	@Override
//	public ResponseEntity<VariantSingleResponse> variantsVariantDbIdGet(String variantDbId, String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
