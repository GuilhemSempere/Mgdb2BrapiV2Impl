package org.brapi.v2.api;

import java.util.Arrays;
import java.util.List;

import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Program;
import org.brapi.v2.model.ProgramListResponse;
import org.brapi.v2.model.ProgramListResponseResult;
import org.brapi.v2.model.ProgramSearchRequest;
import org.brapi.v2.model.ProgramSingleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-06-24T08:33:51.198Z[GMT]")
@Controller
public class ProgramsApiController implements ProgramsApi {

    private static final Logger log = LoggerFactory.getLogger(ProgramsApiController.class);
    
    @Autowired AbstractTokenManager tokenManager;
    
	@Override
	public ResponseEntity<ProgramListResponse> searchProgramsPost(String authorization, ProgramSearchRequest body) {
    	String token = ServerinfoApiController.readToken(authorization);
    	try {
	    	ProgramListResponse slr = new ProgramListResponse();
	    	ProgramListResponseResult result = new ProgramListResponseResult();
	    	for (String module : MongoTemplateManager.getAvailableModules())
        		if ((body.getProgramDbIds() == null || body.getProgramDbIds().isEmpty() || body.getProgramDbIds().contains(module))
        			&& tokenManager.canUserReadDB(token, module) &&
        			(body.getCommonCropNames() == null || body.getCommonCropNames().isEmpty() || body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module))))
        		)
		            	result.addDataItem(new Program() {{
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
            return new ResponseEntity<ProgramListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<ProgramListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@Override
	public ResponseEntity<ProgramListResponse> programsGet(String commonCropName, String programDbId,
			String programName, String abbreviation, String externalReferenceID, String externalReferenceSource,
			Integer page, Integer pageSize, String authorization) {
		
		ProgramSearchRequest psr = new ProgramSearchRequest();
		if (commonCropName != null)
			psr.setCommonCropNames(Arrays.asList(commonCropName));
		if (programDbId != null)
			psr.setProgramDbIds(Arrays.asList(programDbId));
		return searchProgramsPost(authorization, psr);
	}

//	@Override
//	public ResponseEntity<ProgramListResponse> programsPost(String authorization, List<ProgramNewRequest> body) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public ResponseEntity<ProgramSingleResponse> programsProgramDbIdGet(String programDbId, String authorization) {
		ProgramSearchRequest psr = new ProgramSearchRequest();
		psr.setProgramDbIds(Arrays.asList(programDbId));
		List<Program> programs = searchProgramsPost(authorization, psr).getBody().getResult().getData();
		
		ProgramSingleResponse plr = new ProgramSingleResponse();
		if (programs.size() == 1)
			plr.result(programs.get(0));
		return new ResponseEntity<ProgramSingleResponse>(plr, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<ProgramSingleResponse> programsProgramDbIdPut(String programDbId, String authorization,
//			ProgramNewRequest body) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
