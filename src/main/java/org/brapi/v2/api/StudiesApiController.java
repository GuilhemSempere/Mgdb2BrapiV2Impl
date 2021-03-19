package org.brapi.v2.api;

import io.swagger.annotations.*;

import org.brapi.v2.model.StudyListResponse;
import org.brapi.v2.model.StudyNewRequest;
import org.brapi.v2.model.StudySearchRequest;
import org.brapi.v2.model.StudySearchRequest.SortByEnum;
import org.brapi.v2.model.StudySearchRequest.SortOrderEnum;
import org.brapi.v2.model.StudySingleResponse;
import org.brapi.v2.model.VariantSetsSearchRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "org.brapi.v2.codegen.v3.generators.java.SpringCodegen", date = "2021-03-16T09:51:33.671Z[GMT]")
@RestController
public class StudiesApiController implements StudiesApi {

    private static final Logger log = LoggerFactory.getLogger(StudiesApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired SearchApiController searchApiController;

    @org.springframework.beans.factory.annotation.Autowired
    public StudiesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<StudyListResponse> studiesGet(String commonCropName, String studyType, String programDbId,
			String locationDbId, String seasonDbId, String trialDbId, String studyDbId, String studyName,
			String studyCode, String studyPUI, String germplasmDbId, String observationVariableDbId, Boolean active,
			String sortBy, String sortOrder, String externalReferenceID, String externalReferenceSource, Integer page,
			Integer pageSize, String authorization) {
		StudySearchRequest body = new StudySearchRequest();
		if (commonCropName != null)
			body.setCommonCropNames(Arrays.asList(commonCropName));
		if (studyType != null)
			body.setStudyTypes(Arrays.asList(studyType));
		if (programDbId != null)
			body.setProgramDbIds(Arrays.asList(programDbId));
		if (locationDbId != null)
			body.setLocationDbIds(Arrays.asList(locationDbId));
		if (seasonDbId != null)
			body.setSeasonDbIds(Arrays.asList(seasonDbId));
		if (trialDbId != null)
			body.setTrialDbIds(Arrays.asList(trialDbId));
		if (studyDbId != null)
			body.setStudyDbIds(Arrays.asList(studyDbId));
		if (studyName != null)
			body.setStudyNames(Arrays.asList(studyName));
		if (studyCode != null)
			body.setStudyCodes(Arrays.asList(studyCode));
		if (studyPUI != null)
			body.setStudyPUIs(Arrays.asList(studyPUI));
		if (germplasmDbId != null)
			body.setGermplasmDbIds(Arrays.asList(germplasmDbId));
		if (observationVariableDbId != null)
			body.setObservationVariableDbIds(Arrays.asList(observationVariableDbId));
		if (active != null)
			body.setActive(active);
		if (sortBy != null)
			body.setSortBy(SortByEnum.fromValue(sortBy));
		if (sortOrder != null)
			body.setSortOrder(SortOrderEnum.fromValue(sortOrder));
		if (externalReferenceID != null)
			body.setExternalReferenceIDs(Arrays.asList(externalReferenceID));
		if (externalReferenceSource != null)
			body.setExternalReferenceSources(Arrays.asList(externalReferenceSource));
		if (page != null)
			body.setPage(page);
		if (pageSize != null)
			body.setPageSize(pageSize);
		return searchApiController.searchStudiesPost(null, authorization);
	}

//    public ResponseEntity<StudyListResponse> studiesGet(@Parameter(in = ParameterIn.QUERY, description = "Common name for the crop associated with this study" ,schema=@Schema()) @Valid @RequestParam(value = "commonCropName", required = false) String commonCropName,@Parameter(in = ParameterIn.QUERY, description = "Filter based on study type unique identifier" ,schema=@Schema()) @Valid @RequestParam(value = "studyType", required = false) String studyType,@Parameter(in = ParameterIn.QUERY, description = "Program filter to only return studies associated with given program id." ,schema=@Schema()) @Valid @RequestParam(value = "programDbId", required = false) String programDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by location" ,schema=@Schema()) @Valid @RequestParam(value = "locationDbId", required = false) String locationDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by season or year" ,schema=@Schema()) @Valid @RequestParam(value = "seasonDbId", required = false) String seasonDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by trial" ,schema=@Schema()) @Valid @RequestParam(value = "trialDbId", required = false) String trialDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by study DbId" ,schema=@Schema()) @Valid @RequestParam(value = "studyDbId", required = false) String studyDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by study name" ,schema=@Schema()) @Valid @RequestParam(value = "studyName", required = false) String studyName,@Parameter(in = ParameterIn.QUERY, description = "Filter by study code" ,schema=@Schema()) @Valid @RequestParam(value = "studyCode", required = false) String studyCode,@Parameter(in = ParameterIn.QUERY, description = "Filter by study PUI" ,schema=@Schema()) @Valid @RequestParam(value = "studyPUI", required = false) String studyPUI,@Parameter(in = ParameterIn.QUERY, description = "Filter by germplasm DbId" ,schema=@Schema()) @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter by observation variable DbId" ,schema=@Schema()) @Valid @RequestParam(value = "observationVariableDbId", required = false) String observationVariableDbId,@Parameter(in = ParameterIn.QUERY, description = "Filter active status true/false." ,schema=@Schema()) @Valid @RequestParam(value = "active", required = false) Boolean active,@Parameter(in = ParameterIn.QUERY, description = "Name of the field to sort by." ,schema=@Schema(allowableValues={ "studyDbId", "trialDbId", "programDbId", "locationDbId", "seasonDbId", "studyType", "studyName", "studyLocation", "programName" }
//)) @Valid @RequestParam(value = "sortBy", required = false) String sortBy,@Parameter(in = ParameterIn.QUERY, description = "Sort order direction. Ascending/Descending." ,schema=@Schema(allowableValues={ "asc", "ASC", "desc", "DESC" }
//)) @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder,@Parameter(in = ParameterIn.QUERY, description = "An external reference ID. Could be a simple string or a URI. (use with `externalReferenceSource` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceID", required = false) String externalReferenceID,@Parameter(in = ParameterIn.QUERY, description = "An identifier for the source system or database of an external reference (use with `externalReferenceID` parameter)" ,schema=@Schema()) @Valid @RequestParam(value = "externalReferenceSource", required = false) String externalReferenceSource,@Parameter(in = ParameterIn.QUERY, description = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`." ,schema=@Schema()) @Valid @RequestParam(value = "page", required = false) Integer page,@Parameter(in = ParameterIn.QUERY, description = "The size of the pages to be returned. Default is `1000`." ,schema=@Schema()) @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<StudyListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", StudyListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<StudyListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<StudyListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

//    public ResponseEntity<StudyListResponse> studiesPost(@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization,@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody List<StudyNewRequest> body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<StudyListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", StudyListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<StudyListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<StudyListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<StudySingleResponse> studiesStudyDbIdGet(@Parameter(in = ParameterIn.PATH, description = "Identifier of the study. Usually a number, could be alphanumeric.", required=true, schema=@Schema()) @PathVariable("studyDbId") String studyDbId,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<StudySingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", StudySingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<StudySingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<StudySingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<StudySingleResponse> studiesStudyDbIdPut(@Parameter(in = ParameterIn.PATH, description = "Identifier of the study. Usually a number, could be alphanumeric.", required=true, schema=@Schema()) @PathVariable("studyDbId") String studyDbId,@Parameter(in = ParameterIn.HEADER, description = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ,schema=@Schema()) @RequestHeader(value="Authorization", required=false) String authorization,@Parameter(in = ParameterIn.DEFAULT, description = "", schema=@Schema()) @Valid @RequestBody StudyNewRequest body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<StudySingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : \"\",\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", StudySingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<StudySingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<StudySingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

}
