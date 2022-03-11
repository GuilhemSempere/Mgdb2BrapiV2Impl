/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.27).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.brapi.v2.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import org.brapi.v2.model.GermplasmAttributeValueListResponse;
import org.brapi.v2.model.SuccessfulSearchResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.servlet.http.HttpServletResponse;
import org.brapi.v2.model.GermplasmAttributeValueSearchRequest;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-09-14T15:37:29.213Z[GMT]")
@Validated
@Api(value = "attributevalues", description = "the attributevalues API", tags={ "Attribute values", })
public interface AttributevaluesApi {

    public static String searchAttributevaluesPost_url = "search/attributevalues";

//    @Operation(summary = "Get the details for a specific Germplasm Attribute", description = "Get the details for a specific Germplasm Attribute", security = {
//        @SecurityRequirement(name = "AuthorizationToken")    }, tags={ "Germplasm Attribute Values" })
//    @ApiResponses(value = { 
//        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GermplasmAttributeValueSingleResponse.class))),
//        
//        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
//    @RequestMapping(value = "/attributevalues/{attributeValueDbId}",
//        produces = { "application/json" }, 
//        method = RequestMethod.GET)
//    ResponseEntity<GermplasmAttributeValueSingleResponse> attributevaluesAttributeValueDbIdGet(@Parameter(in = ParameterIn.PATH, description = "The unique id for an attribute value", required=true, schema=@Schema()) @PathVariable("attributeValueDbId") String attributeValueDbId, @Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization);
//
//
//    @Operation(summary = "Update an existing Germplasm Attribute Value", description = "Update an existing Germplasm Attribute Value", security = {
//        @SecurityRequirement(name = "AuthorizationToken")    }, tags={ "Germplasm Attribute Values" })
//    @ApiResponses(value = { 
//        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GermplasmAttributeValueSingleResponse.class))),
//        
//        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
//    @RequestMapping(value = "/attributevalues/{attributeValueDbId}",
//        produces = { "application/json" }, 
//        consumes = { "application/json" }, 
//        method = RequestMethod.PUT)
//    ResponseEntity<GermplasmAttributeValueSingleResponse> attributevaluesAttributeValueDbIdPut(@Parameter(in = ParameterIn.PATH, description = "The unique id for an attribute value", required=true, schema=@Schema()) @PathVariable("attributeValueDbId") String attributeValueDbId, @Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization, @Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody GermplasmAttributeValueNewRequest body);


//    @ApiOperation(value = "Get the Germplasm Attribute Values", notes = "Get the Germplasm Attribute Values", authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attribute Values" })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeValueListResponse.class),
//        
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + attributevaluesGet_url,
//        produces = { "application/json" },
//        method = RequestMethod.GET)
//    ResponseEntity<GermplasmAttributeValueListResponse> attributevaluesGet(@Parameter(in = ParameterIn.QUERY, description = "The unique id for an attribute value" ,schema=@Schema()) @Valid @RequestParam(value = "attributeValueDbId", required = false) String attributeValueDbId, @Parameter(in = ParameterIn.QUERY, description = "The unique id for an attribute" ,schema=@Schema()) @Valid @RequestParam(value = "attributeDbId", required = false) String attributeDbId, @Parameter(in = ParameterIn.QUERY, description = "The human readable name for an attribute" ,schema=@Schema()) @Valid @RequestParam(value = "attributeName", required = false) String attributeName, @Parameter(in = ParameterIn.QUERY, description = "Get all attributes associated with this germplasm" ,schema=@Schema()) @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId, @Parameter(in = ParameterIn.QUERY, description = "An external reference ID. Could be a simple string or a URI. (use with `externalReferenceSource` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceID", required = false) String externalReferenceID, @Parameter(in = ParameterIn.QUERY, description = "An identifier for the source system or database of an external reference (use with `externalReferenceID` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceSource", required = false) String externalReferenceSource, @Parameter(in = ParameterIn.QUERY, description = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`." ,schema=@Schema()) @Valid @RequestParam(value = "page", required = false) Integer page, @Parameter(in = ParameterIn.QUERY, description = "The size of the pages to be returned. Default is `1000`." ,schema=@Schema()) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize, @Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization);


//    @Operation(summary = "Create new Germplasm Attribute Values", description = "Create new Germplasm Attribute Values", security = {
//        @SecurityRequirement(name = "AuthorizationToken")    }, tags={ "Germplasm Attribute Values" })
//    @ApiResponses(value = { 
//        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GermplasmAttributeValueListResponse.class))),
//        
//        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
//        
//        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
//    @RequestMapping(value = "/attributevalues",
//        produces = { "application/json" }, 
//        consumes = { "application/json" }, 
//        method = RequestMethod.POST)
//    ResponseEntity<GermplasmAttributeValueListResponse> attributevaluesPost(@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization, @Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody List<GermplasmAttributeValueNewRequest> body);

    @ApiOperation(value = "Submit a search request for Germplasm `Attributes`", notes = "Submit a search request for Germplasm `Attributes`", authorizations = {
        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes" })
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "OK", response = SuccessfulSearchResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
            @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + searchAttributevaluesPost_url,
        produces = { "application/json" }, 
        consumes = { "application/json" }, 
        method = RequestMethod.POST)
    ResponseEntity<GermplasmAttributeValueListResponse> searchAttributevaluesPost(HttpServletResponse response, @ApiParam @Valid @RequestBody GermplasmAttributeValueSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);

}

