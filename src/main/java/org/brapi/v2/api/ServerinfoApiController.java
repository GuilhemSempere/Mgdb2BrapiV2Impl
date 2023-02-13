package org.brapi.v2.api;

import java.util.ArrayList;

import javax.validation.Valid;

import org.brapi.v2.model.CallsResponse;
import org.brapi.v2.model.ContentTypes;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.ServerInfo;
import org.brapi.v2.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
@Controller
public class ServerinfoApiController implements ServerinfoApi {

    private static final Logger log = LoggerFactory.getLogger(ServerinfoApiController.class);

    @SuppressWarnings("serial")
	public ResponseEntity<CallsResponse> serverinfoGet(@ApiParam(value = "The data format supported by the call.") @Valid @RequestParam(value = "dataType", required = false) ContentTypes dataType,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	CallsResponse cr = new CallsResponse();
    	ServerInfo result = new ServerInfo();
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ServerinfoApi.serverinfoGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(StudiesApi.studiesGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(CallsApi.callsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ReferencesetsApi.referencesetsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsVariantSetDbIdCallsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsExportIntoFormat_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsetsApi.variantsetsVariantSetDbIdGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(VariantsApi.variantsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.GET); setService(VariantsApi.variantsVariantDbIdCallsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ProgramsApi.programsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(ProgramsApi.programsProgramDbIdGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(TrialsApi.trialsGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(TrialsApi.trialsTrialDbIdGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.GET); setService(CommoncropnamesApi.commoncropnamesGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});

		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.GET); setService(AllelematrixApi.alleleMatrixGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.PUT); setService(CallsApi.callsPut_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(StudiesApi.studiesPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(VariantsApi.searchVariantsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(VariantsetsApi.searchVariantsetsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(StudiesApi.searchStudiesPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(CallsetsApi.searchCallsetsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(SamplesApi.searchSamplesPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(CallsApi.searchCallsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(ProgramsApi.searchProgramsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(TrialsApi.searchTrialsPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(AttributesApi.attributesGet_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._0); getMethods().add(MethodsEnum.POST); setService(AttributevaluesApi.searchAttributevaluesPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});

		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.POST); setService(GermplasmApi.searchGermplasmPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		result.addCallsItem(new Service() {{ getVersions().add(VersionsEnum._1); getMethods().add(MethodsEnum.POST); setService(AllelematrixApi.searchAlleleMatrixPost_url); setContentTypes(new ArrayList<ContentTypes>() {{ add(ContentTypes.fromValue("application/json")); }}); }});
		
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
