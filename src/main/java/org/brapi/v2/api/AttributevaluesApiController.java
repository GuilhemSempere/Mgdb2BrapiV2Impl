package org.brapi.v2.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.tools.Helper;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import org.brapi.v2.model.Germplasm;
import org.brapi.v2.model.GermplasmAttributeValue;
import org.brapi.v2.model.GermplasmAttributeValueListResponse;
import org.brapi.v2.model.GermplasmAttributeValueListResponseResult;
import org.brapi.v2.model.GermplasmAttributeValueSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-09-14T15:37:29.213Z[GMT]")
@RestController
public class AttributevaluesApiController implements AttributevaluesApi {

    private static final Logger log = LoggerFactory.getLogger(AttributevaluesApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired
    private BrapiRestController brapiV1Service;
    
    @Autowired
    AbstractTokenManager tokenManager;

    @org.springframework.beans.factory.annotation.Autowired
    public AttributevaluesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

//    public ResponseEntity<GermplasmAttributeValueSingleResponse> attributevaluesAttributeValueDbIdGet(@Parameter(in = ParameterIn.PATH, description = "The unique id for an attribute value", required=true, schema=@Schema()) @PathVariable("attributeValueDbId") String attributeValueDbId,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeValueSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeValueSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeValueSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeValueSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<GermplasmAttributeValueSingleResponse> attributevaluesAttributeValueDbIdPut(@Parameter(in = ParameterIn.PATH, description = "The unique id for an attribute value", required=true, schema=@Schema()) @PathVariable("attributeValueDbId") String attributeValueDbId,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization,@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody GermplasmAttributeValueNewRequest body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeValueSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeValueSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeValueSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeValueSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<GermplasmAttributeValueListResponse> attributevaluesGet(@Parameter(in = ParameterIn.QUERY, description = "The unique id for an attribute value" ,schema=@Schema()) @Valid @RequestParam(value = "attributeValueDbId", required = false) String attributeValueDbId,@Parameter(in = ParameterIn.QUERY, description = "The unique id for an attribute" ,schema=@Schema()) @Valid @RequestParam(value = "attributeDbId", required = false) String attributeDbId,@Parameter(in = ParameterIn.QUERY, description = "The human readable name for an attribute" ,schema=@Schema()) @Valid @RequestParam(value = "attributeName", required = false) String attributeName,@Parameter(in = ParameterIn.QUERY, description = "Get all attributes associated with this germplasm" ,schema=@Schema()) @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@Parameter(in = ParameterIn.QUERY, description = "An external reference ID. Could be a simple string or a URI. (use with `externalReferenceSource` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceID", required = false) String externalReferenceID,@Parameter(in = ParameterIn.QUERY, description = "An identifier for the source system or database of an external reference (use with `externalReferenceID` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceSource", required = false) String externalReferenceSource,@Parameter(in = ParameterIn.QUERY, description = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`." ,schema=@Schema()) @Valid @RequestParam(value = "page", required = false) Integer page,@Parameter(in = ParameterIn.QUERY, description = "The size of the pages to be returned. Default is `1000`." ,schema=@Schema()) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeValueListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeValueListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeValueListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeValueListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

//    public ResponseEntity<GermplasmAttributeValueListResponse> attributevaluesPost(@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization,@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody List<GermplasmAttributeValueNewRequest> body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GermplasmAttributeValueListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmAttributeValueListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GermplasmAttributeValueListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GermplasmAttributeValueListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

    @Override
    public ResponseEntity<GermplasmAttributeValueListResponse> searchAttributevaluesPost(HttpServletResponse response, GermplasmAttributeValueSearchRequest body, String authorization) {
    	try {
	        String token = ServerinfoApiController.readToken(authorization);
	        
	        Collection<String> germplasmIdsToReturn = new HashSet<>(), requestedGermplasmIDs;
	        Metadata metadata = new Metadata();
	        GermplasmAttributeValueListResponse resp = new GermplasmAttributeValueListResponse();
	        resp.setMetadata(metadata);
	        String programDbId = null;
	        if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
	            requestedGermplasmIDs = body.getGermplasmDbIds();
	            for (String gpId : requestedGermplasmIDs) {
	                String[] info = Helper.getInfoFromId(gpId, 2);
	                if (programDbId == null) {
	                    programDbId = info[0];
	                } else if (!programDbId.equals(info[0])) {
	                    Status status = new Status();
	                    status.setMessage("You may only supply IDs of germplasm records from one program at a time!");
	                    metadata.addStatusItem(status);
	                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
	                }
	                germplasmIdsToReturn.add(info[1]);
	            }

	            fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest gsr = new fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest();
	            gsr.germplasmDbIds = germplasmIdsToReturn;

	            if (!tokenManager.canUserReadDB(token, programDbId))
	                return new ResponseEntity<>(HttpStatus.FORBIDDEN);

	            try {
	                GermplasmAttributeValueListResponseResult result = new GermplasmAttributeValueListResponseResult();
	                String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
	                
	                Authentication auth = tokenManager.getAuthenticationFromToken(tokenManager.readToken(request));
	                String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
	                LinkedHashMap<String, Individual> individuals = MgdbDao.getInstance().loadIndividualsWithAllMetadata(programDbId, sCurrentUser, null, germplasmIdsToReturn);
	                
	                ArrayList<GermplasmAttributeValue> data = new ArrayList<>();
	                for (String individualId : individuals.keySet()) {
	                    Map<String, Object> addInfo = individuals.get(individualId).getAdditionalInfo();
	                    if (addInfo != null) {
	                        for (String attributeDbId : addInfo.keySet()) {
	                        	String sLCkey = attributeDbId.toLowerCase();
	                            if (Germplasm.germplasmFields.containsKey(sLCkey) || BrapiRestController.extRefList.contains(attributeDbId) || lowerCaseIdFieldName.equals(sLCkey))
	                        		continue;

	                        	Object val = addInfo.get(attributeDbId);
	                        	if (val == null)
	                        		continue;

	                            GermplasmAttributeValue attrValue = new GermplasmAttributeValue();
	                            attrValue.setGermplasmDbId(programDbId + Helper.ID_SEPARATOR + individualId);
	                            attrValue.setGermplasmName(individualId);
	                            attrValue.setAttributeValueDbId(attributeDbId);
	                            attrValue.setValue(addInfo.get(attributeDbId).toString());
	                            data.add(attrValue);
	                        }                    
	                    }
	                }     
	                
	                IndexPagination pagination = new IndexPagination();
	                if (body.getPage() != null && body.getPage() >= 0  && body.getPageSize() != null && body.getPageSize() >= 0) {
	                    int offset = body.getPageSize()*body.getPage();                    
	                    List<GermplasmAttributeValue> elementsList = data.stream().skip(offset).limit(body.getPageSize()).collect(Collectors.toList());
	                    result.setData(elementsList);
	                    pagination.setPageSize(result.getData().size());
	                    pagination.setCurrentPage(body.getPage());
	                    if ((data.size() % body.getPageSize()) == 0) {
	                        pagination.setTotalPages(data.size()/body.getPageSize());
	                    } else {
	                        pagination.setTotalPages(data.size()/body.getPageSize() + 1);
	                    }
	                    pagination.setTotalCount(data.size());
	                    
	                } else {
	                    result.setData(data);                     
	                    pagination.setPageSize(result.getData().size());
	                    pagination.setCurrentPage(0);
	                    pagination.setTotalPages(1);
	                    pagination.setTotalCount(result.getData().size());
	                    
	                }
	                metadata.setPagination(pagination);               
	                
	                resp.setMetadata(metadata);
	                resp.setResult(result);
	                return new ResponseEntity<>(resp, HttpStatus.OK);
	            } catch (Exception e) {
	                log.error("Couldn't serialize response for content type application/json", e);
	                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	            }    
	        } else {
	            Status status = new Status();
	            status.setMessage("A list of germplasmDbIds must be specified as parameter");
	            metadata.addStatusItem(status);
	            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
	        }
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<GermplasmAttributeValueListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
