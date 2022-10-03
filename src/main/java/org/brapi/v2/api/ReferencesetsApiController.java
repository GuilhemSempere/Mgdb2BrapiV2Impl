package org.brapi.v2.api;

import java.util.Arrays;

import javax.validation.Valid;

import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.ReferenceListResponse1;
import org.brapi.v2.model.ReferenceListResponseResult1;
import org.brapi.v2.model.ReferenceSet;
import org.brapi.v2.model.Study;
import org.brapi.v2.model.StudySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@Controller
public class ReferencesetsApiController implements ReferencesetsApi {

    private static final Logger log = LoggerFactory.getLogger(ReferencesetsApiController.class);
    
    @Autowired AbstractTokenManager tokenManager;

	@Autowired private StudiesApiController studiesApiController;

    public ResponseEntity<ReferenceListResponse1> referencesetsGet(@ApiParam(value = "The ID of the `ReferenceSet` to be retrieved.") @Valid @RequestParam(value = "referenceSetDbId", required = false) String referenceSetDbId,@ApiParam(value = "If unset, return the reference sets for which the `accession` matches this string (case-sensitive, exact match).") @Valid @RequestParam(value = "accession", required = false) String accession,@ApiParam(value = "If unset, return the reference sets for which the `assemblyId` matches this string (case-sensitive, exact match).") @Valid @RequestParam(value = "assemblyPUI", required = false) String assemblyPUI,@ApiParam(value = "If unset, return the reference sets for which the `md5checksum` matches this string (case-sensitive, exact match). See `ReferenceSet::md5checksum` for details.") @Valid @RequestParam(value = "md5checksum", required = false) String md5checksum,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	
        try {
        	ReferenceListResponse1 rlr = new ReferenceListResponse1();
        	ReferenceListResponseResult1 result = new ReferenceListResponseResult1();
        	
        	StudySearchRequest ssr = new StudySearchRequest();
        	if (referenceSetDbId != null)
        		ssr.setStudyDbIds(Arrays.asList(referenceSetDbId));
        	for (Study study : studiesApiController.searchStudiesPost(ssr, authorization).getBody().getResult().getData()) {
	        	result.addDataItem(new ReferenceSet() {{
	    			setReferenceSetDbId(study.getStudyDbId());
	    			setReferenceSetName(study.getStudyName());
	    		}} );
        	}

			Metadata metadata = new Metadata();
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
			rlr.setMetadata(metadata);
			
			rlr.setResult(result);
            return new ResponseEntity<ReferenceListResponse1>(rlr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<ReferenceListResponse1>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    public ResponseEntity<ReferenceSet> referencesetsReferenceSetDbIdGet(@ApiParam(value = "The ID of the `ReferenceSet` to be retrieved.",required=true) @PathVariable("referenceSetDbId") String referenceSetDbId) {    
//        try {
//            return new ResponseEntity<ReferenceSet>(objectMapper.readValue("{\n  \"isDerived\" : true,\n  \"sourceURI\" : \"sourceURI\",\n  \"species\" : {\n    \"termURI\" : \"termURI\",\n    \"term\" : \"term\"\n  },\n  \"md5checksum\" : \"md5checksum\",\n  \"additionalInfo\" : {\n    \"key\" : \"additionalInfo\"\n  },\n  \"assemblyPUI\" : \"assemblyPUI\",\n  \"description\" : \"description\",\n  \"referenceSetDbId\" : \"referenceSetDbId\",\n  \"referenceSetName\" : \"referenceSetName\",\n  \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ]\n}", ReferenceSet.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceSet>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}
