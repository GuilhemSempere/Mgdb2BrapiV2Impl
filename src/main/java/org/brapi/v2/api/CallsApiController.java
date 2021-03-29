package org.brapi.v2.api;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.brapi.v2.api.cache.MongoBrapiCache;
import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.CallsListResponseResult;
import org.brapi.v2.model.GermplasmListResponse;
import org.brapi.v2.model.ListValue;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.TokenPagination;
import org.brapi.v2.model.VariantSet;
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
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@RestController
public class CallsApiController implements CallsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired private AbstractTokenManager tokenManager;
    
    @Autowired private MongoBrapiCache cache;

	private Integer MAX_SUPPORTED_CALL_COUNT_PER_PAGE = 10000;

    @org.springframework.beans.factory.annotation.Autowired
    public CallsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<CallListResponse> callsGet(String callSetDbId, String variantDbId, String variantSetDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, String pageToken, Integer pageSize, String authorization) {
		String token = ServerinfoApiController.readToken(authorization);

		String[] info = null;
		int projId, nVariantCount = 1;
		Query runQuery = null;
		MongoTemplate mongoTemplate = null;

    	CallsListResponseResult result = new CallsListResponseResult();
    	CallListResponse clr = new CallListResponse();
    	MetadataTokenPagination metadata = new MetadataTokenPagination();
		Status status = new Status();
		HttpStatus httpCode = null;
		
        if (pageSize == null || pageSize > MAX_SUPPORTED_CALL_COUNT_PER_PAGE)
        	pageSize = MAX_SUPPORTED_CALL_COUNT_PER_PAGE;
        int page = pageToken == null ? 0 : Integer.parseInt(pageToken);    	

		if (callSetDbId == null || callSetDbId.isEmpty()) {
			httpCode = HttpStatus.BAD_REQUEST;
			status.setMessage("A callSetDbId must be specified as parameter!");
			metadata.addStatusItem(status);
		}
		else 
			try {
				if (variantDbId != null && !variantDbId.isEmpty()) {
					info = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2);
					mongoTemplate = MongoTemplateManager.get(info[0]);
					runQuery = new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).is(info[1]));
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
					}
				} else {
					httpCode = HttpStatus.BAD_REQUEST;
					status.setMessage("A variantDbId or a variantSetDbId must be specified as parameter!");
					metadata.addStatusItem(status);				
				}
				
				if (runQuery != null) {
					runQuery.fields().include(AbstractVariantData.FIELDNAME_KNOWN_ALLELE_LIST).include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + callSetDbId.substring(1 + callSetDbId.lastIndexOf(GigwaGa4ghServiceImpl.ID_SEPARATOR)));
			
			    	String unknownGtCode = unknownString == null ? "-" : unknownString;
			    	String unPhasedSeparator = sepUnphased == null ? "/" : sepUnphased;
			    	String phasedSeparator = sepPhased == null ? "|" : URLDecoder.decode(sepPhased, "UTF-8");
			    	
			    	result.setSepUnphased(unPhasedSeparator);
				
		//	        long b4 = System.currentTimeMillis();
		        	List<AbstractVariantData> varList = IExportHandler.getMarkerListWithCorrectCollation(mongoTemplate, VariantRunData.class, runQuery, page * pageSize, pageSize);
		//        	System.err.println((System.currentTimeMillis() - b4) + " / " + variants.size()/* + ": " + variants*/);
		        	HashMap<Integer, String> previousPhasingIds = new HashMap<>();
		        	HashMap<Integer, String> sampleToIndividualMap = new HashMap<>();
		        	for (AbstractVariantData v : varList) {
		        		VariantRunData vrd = (VariantRunData) v;
		        		
		        		if (variantDbId != null && !variantDbId.isEmpty() && !tokenManager.canUserReadProject(token, info[0], vrd.getId().getProjectId())) { // only a single variant was requested: make sure we're allowed this genotype comes from a project we're allowed to show
							httpCode = HttpStatus.FORBIDDEN;
							status.setMessage("You are not allowed to access this content");
							metadata.addStatusItem(status);
		        		}
		        		for (Integer spId : vrd.getSampleGenotypes().keySet()) {
		        			String ind = sampleToIndividualMap.get(spId);
		        			if (ind == null) {
		        				ind = mongoTemplate.findDistinct(new Query(Criteria.where("_id").is(spId)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class).iterator().next();
		        				sampleToIndividualMap.put(spId, ind);
		        			}
		        			SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
							String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
							boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
							previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/
		
							String gtCode = sg.getCode(), genotype;
							if (gtCode.length() == 0)
								genotype = unknownGtCode;
							else
							{
								List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
								if (!Boolean.TRUE.equals(expandHomozygotes) && new HashSet<String>(alleles).size() == 1)
									genotype = alleles.get(0);
								else
									genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
							}
		        			Call call = new Call();
		        			ListValue lv = new ListValue();
		        			lv.addValuesItem(genotype);
		        			call.setGenotype(lv);
		        			call.setVariantDbId(info[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getVariantId());
		        			call.setVariantName(call.getVariantDbId());
		        			call.setCallSetDbId(callSetDbId);
		        			call.setCallSetName(callSetDbId);
		        			for (String key : sg.getAdditionalInfo().keySet())
		        				if (!key.equals(VariantData.GT_FIELD_PHASED_ID) && !key.equals(VariantData.GT_FIELD_PHASED_GT))
		        					call.putAdditionalInfoItem(key, sg.getAdditionalInfo().get(key).toString());
		                	result.addDataItem(call);
		        		}
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
		
		clr.setMetadata(metadata);		
		clr.setResult(result);
		return new ResponseEntity<>(clr, httpCode == null ? HttpStatus.OK : httpCode);
	}
}