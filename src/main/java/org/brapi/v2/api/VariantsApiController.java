package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.api.cache.MongoBrapiCache;
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

//	@Override
//	public ResponseEntity<CallListResponse> variantsVariantDbIdCallsGet(String variantDbId, Boolean expandHomozygotes,
//			String unknownString, String sepPhased, String sepUnphased, String pageToken, Integer pageSize,
//			String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public ResponseEntity<VariantSingleResponse> variantsVariantDbIdGet(String variantDbId, String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
