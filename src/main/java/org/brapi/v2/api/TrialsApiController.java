package org.brapi.v2.api;

import java.util.Arrays;
import java.util.List;

import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Trial;
import org.brapi.v2.model.TrialListResponse;
import org.brapi.v2.model.TrialListResponseResult;
import org.brapi.v2.model.TrialSearchRequest;
import org.brapi.v2.model.TrialSingleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.threeten.bp.LocalDate;

import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-06-24T08:33:51.198Z[GMT]")
@Controller
public class TrialsApiController implements TrialsApi {

    private static final Logger log = LoggerFactory.getLogger(TrialsApiController.class);

    @Autowired AbstractTokenManager tokenManager;

    @Override
    public ResponseEntity<TrialListResponse> searchTrialsPost(String authorization, TrialSearchRequest body) {
        String token = ServerinfoApiController.readToken(authorization);
        try {
            TrialListResponse slr = new TrialListResponse();
            TrialListResponseResult result = new TrialListResponseResult();
            if (body.getTrialDbIds() == null || body.getTrialDbIds().isEmpty())
            	body.setTrialDbIds(body.getProgramDbIds());	// We make no difference between Program and Trial
            for (String module : MongoTemplateManager.getAvailableModules())
                if ((body.getTrialDbIds() == null || body.getTrialDbIds().isEmpty() || body.getTrialDbIds().contains(module))
            			&& tokenManager.canUserReadDB(token, module) &&
            			(body.getCommonCropNames() == null || body.getCommonCropNames().isEmpty() || body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module))))
            		)
                    result.addDataItem(new Trial() {{
                        setTrialDbId(module);
                        setProgramDbId(module);
                    }} );
            
            Metadata metadata = new Metadata();
            IndexPagination pagination = new IndexPagination();
            pagination.setPageSize(result.getData().size());
            pagination.setCurrentPage(0);
            pagination.setTotalPages(1);
            pagination.setTotalCount((long) result.getData().size());
            metadata.setPagination(pagination);
            slr.setMetadata(metadata);
    
            slr.setResult(result);      
            return new ResponseEntity<TrialListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<TrialListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@Override
	public ResponseEntity<TrialListResponse> trialsGet(Boolean active, String commonCropName, String contactDbId,
			String programDbId, String locationDbId, LocalDate searchDateRangeStart, LocalDate searchDateRangeEnd,
			String studyDbId, String trialDbId, String trialName, String trialPUI, String sortBy, String sortOrder,
			String externalReferenceID, String externalReferenceSource, Integer page, Integer pageSize,
			String authorization) {
		
		TrialSearchRequest tsr = new TrialSearchRequest();
		if (programDbId != null)
			tsr.setProgramDbIds(Arrays.asList(programDbId));
		if (trialDbId != null)
			tsr.setTrialDbIds(Arrays.asList(trialDbId));
                if (commonCropName != null)
			tsr.setCommonCropNames(Arrays.asList(commonCropName));
		return searchTrialsPost(authorization, tsr);
	}

//	@Override
//	public ResponseEntity<TrialListResponse> trialsPost(String authorization, List<TrialNewRequest> body) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public ResponseEntity<TrialSingleResponse> trialsTrialDbIdGet(String trialDbId, String authorization) {
		TrialSearchRequest psr = new TrialSearchRequest();
		psr.setTrialDbIds(Arrays.asList(trialDbId));
		List<Trial> trials = searchTrialsPost(authorization, psr).getBody().getResult().getData();
		
		TrialSingleResponse plr = new TrialSingleResponse();
		if (trials.size() == 1)
			plr.result(trials.get(0));
		return new ResponseEntity<TrialSingleResponse>(plr, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<TrialSingleResponse> trialsTrialDbIdPut(String trialDbId, String authorization,
//			TrialNewRequest body) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}