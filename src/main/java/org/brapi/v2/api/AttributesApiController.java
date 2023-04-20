package org.brapi.v2.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.brapi.v2.model.GermplasmAttribute;
import org.brapi.v2.model.GermplasmAttributeListResponse;
import org.brapi.v2.model.GermplasmAttributeListResponseResult;
import org.brapi.v2.model.GermplasmAttributeNewRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.tools.Helper;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T11:58:54.700Z[GMT]")
@RestController
public class AttributesApiController implements AttributesApi {

    private static final Logger log = LoggerFactory.getLogger(AttributesApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
    @Autowired private BrapiRestController brapiV1Service;
    
    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public AttributesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

//    public ResponseEntity<GermplasmAttributeSingleResponse> attributesAttributeDbIdGet(@ApiParam(value = "The unique id for an attribute",required=true) @PathVariable("attributeDbId") String attributeDbId,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

//    public ResponseEntity<GermplasmAttributeSingleResponse> attributesAttributeDbIdPost(@ApiParam(value = "The unique id for an attribute",required=true) @PathVariable("attributeDbId") String attributeDbId,@ApiParam(value = ""  )  @Valid @RequestBody GermplasmAttributeNewRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

//    public ResponseEntity<GermplasmAttributeCategoryListResponse> attributesCategoriesGet(@ApiParam(value = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeCategoryListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"Morphological\", \"Agronomic\" ]\n  },\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeCategoryListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeCategoryListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeCategoryListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

    public ResponseEntity<GermplasmAttributeListResponse> attributesGet(HttpServletResponse response, @ApiParam(value = "The general category for the attribute. very similar to Trait class.") @Valid @RequestParam(value = "attributeCategory", required = false) String attributeCategory,@ApiParam(value = "The unique id for an attribute") @Valid @RequestParam(value = "attributeDbId", required = false) String attributeDbId,@ApiParam(value = "The human readable name for an attribute") @Valid @RequestParam(value = "attributeName", required = false) String attributeName,@ApiParam(value = "Get all attributes associated with this germplasm") @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@ApiParam(value = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {    	
		String token = ServerinfoApiController.readToken(authorization);
		String[] info = Helper.getInfoFromId(germplasmDbId, 2);

        try {
    		if (!tokenManager.canUserReadDB(token, info[0]))
    			return new ResponseEntity<GermplasmAttributeListResponse>(HttpStatus.FORBIDDEN);

    		GermplasmAttributeListResponse galr = new GermplasmAttributeListResponse();
        	GermplasmAttributeListResponseResult result = new GermplasmAttributeListResponseResult();
        	Map<String, Object> v1Attributes = brapiV1Service.germplasmAttributes(request, response, info[0], info[1], pageSize, pageSize);
        	Collection<Map<String, String>> dataDoc = (Collection<Map<String, String>>) ((Map<String, Object>) v1Attributes.get("result")).get("data");
        	for (Map<String, String> v1DataItem : dataDoc) {
	        	GermplasmAttribute dataItem = new GermplasmAttribute();
	        	dataItem.setAttributeDbId(v1DataItem.get("attributeDbId"));
	        	dataItem.setDefaultValue(v1DataItem.get("value"));
	        	result.addDataItem(dataItem);
        	}

			Metadata metadata = new Metadata();
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
			galr.setMetadata(metadata);
			
			galr.setResult(result);
            return new ResponseEntity<GermplasmAttributeListResponse>(galr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<GermplasmAttributeListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiIgnore
	@Override
	public ResponseEntity<GermplasmAttributeListResponse> attributesPost(List<GermplasmAttributeNewRequest> body, String authorization) {
		return new ResponseEntity<GermplasmAttributeListResponse>(HttpStatus.NOT_IMPLEMENTED);
	}
}
