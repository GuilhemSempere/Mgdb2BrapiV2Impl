package org.brapi.v2.api;

import java.util.stream.Collectors;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.brapi.v2.model.CommonCropNamesResponse;
import org.brapi.v2.model.CommonCropNamesResponseResult;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
@CrossOrigin
@Controller
public class CommoncropnamesApiController implements CommoncropnamesApi {

    private static final Logger log = LoggerFactory.getLogger(CommoncropnamesApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired AbstractTokenManager tokenManager;

    @org.springframework.beans.factory.annotation.Autowired
    public CommoncropnamesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<CommonCropNamesResponse> commoncropnamesGet(@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
		String token = ServerinfoApiController.readToken(authorization);
    	
        try {
        	CommonCropNamesResponse rlr = new CommonCropNamesResponse();
        	CommonCropNamesResponseResult result = new CommonCropNamesResponseResult();
        	if (page == null)
        		page = 0;
        	int nAllowedCropIndex = 0;
        	for (final String crop : MongoTemplateManager.getAvailableModules().stream()
    	        .filter(db -> {
					try {
						return tokenManager.canUserReadDB(token, db);
					} catch (ObjectNotFoundException e) {
						return false;
					}
				}).map(db -> MongoTemplateManager.getTaxonName(db)).collect(Collectors.toSet()))
        	{
    			if (pageSize == null || (page*pageSize <= nAllowedCropIndex && (page+1)*pageSize > nAllowedCropIndex))
	        		result.addDataItem(crop == null ? "" : crop);
    			nAllowedCropIndex++;
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
            return new ResponseEntity<CommonCropNamesResponse>(rlr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<CommonCropNamesResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
