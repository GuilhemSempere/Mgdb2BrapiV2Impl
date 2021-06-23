package org.brapi.v2.api;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.brapi.v2.model.CallsResponse;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.ServerInfo;
import org.brapi.v2.model.Service;
import org.brapi.v2.model.WSMIMEDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
@CrossOrigin
@RestController
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
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ServerinfoApi.serverinfoGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(StudiesApi.studiesGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(CallsApi.callsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(StudiesApi.studiesGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ReferencesetsApi.referencesetsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsVariantSetDbIdCallsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsExportIntoFormat_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsVariantSetDbIdGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsApi.variantsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsApi.variantsVariantDbIdCallsGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(CommoncropnamesApi.commoncropnamesGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchVariantsPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchVariantsetsPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchStudiesPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchCallsetsPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchSamplesPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchCallsPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SearchApi.searchGermplasmPost_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(AttributesApi.attributesGet_url); setDataTypes(new ArrayList<WSMIMEDataTypes>() {{ add(WSMIMEDataTypes.fromValue("application/json")); }}); }});

		Metadata metadata = new Metadata();
		IndexPagination pagination = new IndexPagination();
		pagination.setPageSize(0);
		pagination.setCurrentPage(0);
		pagination.setTotalPages(1);
		pagination.setTotalCount(result.getCalls().size());
		metadata.setPagination(pagination);
		cr.setMetadata(metadata);
		cr.setResult(result);
        return new ResponseEntity<CallsResponse>(cr, HttpStatus.OK);
    }

	public static String readToken(String authorization) {
		return authorization != null && authorization.startsWith("Bearer ") ? authorization = authorization.substring(7) : null;
	}

}
