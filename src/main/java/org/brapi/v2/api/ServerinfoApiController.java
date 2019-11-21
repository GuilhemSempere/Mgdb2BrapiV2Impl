package org.brapi.v2.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;

import org.brapi.v2.model.CallsResponse;
import org.brapi.v2.model.ServerInfo;
import org.brapi.v2.model.Service;
import org.brapi.v2.model.WSMIMEDataTypes;
import org.brapi.v2.model.Service.MethodsEnum;
import org.brapi.v2.model.Service.VersionsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
@CrossOrigin
@Controller
public class ServerinfoApiController implements ServerinfoApi {

    private static final Logger log = LoggerFactory.getLogger(ServerinfoApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public ServerinfoApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<CallsResponse> serverinfoGet(@ApiParam(value = "The data format supported by the call.") @Valid @RequestParam(value = "dataType", required = false) WSMIMEDataTypes dataType,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	CallsResponse cr = new CallsResponse();
    	ServerInfo result = new ServerInfo();
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ServerinfoApi.serverinfoGet_url); }});
//		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchReferencesetsPost_url); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ReferencesetsApi.referencesetsGet_url); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchVariantsetsPost_url); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchCallsetsPost_url); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchSamplesPost_url); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchGermplasmPost_url); }});
		cr.setResult(result);
        return new ResponseEntity<CallsResponse>(cr, HttpStatus.OK);
    }

}
