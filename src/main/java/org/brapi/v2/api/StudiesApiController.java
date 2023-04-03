package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.Study;
import org.brapi.v2.model.StudyListResponse;
import org.brapi.v2.model.StudyListResponseResult;
import org.brapi.v2.model.StudyNewRequest;
import org.brapi.v2.model.StudySearchRequest;
import org.brapi.v2.model.StudySearchRequest.SortByEnum;
import org.brapi.v2.model.StudySearchRequest.SortOrderEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@javax.annotation.Generated(value = "org.brapi.v2.codegen.v3.generators.java.SpringCodegen", date = "2021-03-16T09:51:33.671Z[GMT]")
@Controller
public class StudiesApiController implements StudiesApi {

    private static final Logger log = LoggerFactory.getLogger(StudiesApiController.class);

    @Autowired AbstractTokenManager tokenManager;    

	public ResponseEntity<StudyListResponse> searchStudiesPost(@ApiParam(value = "Study Search request")  @Valid @RequestBody StudySearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);

    	try {
	    	StudyListResponse slr = new StudyListResponse();
	    	StudyListResponseResult result = new StudyListResponseResult();
			Metadata metadata = new Metadata();
			slr.setMetadata(metadata);
			
        	HashMap<String /*module*/, Collection<Integer> /*samples*/> projectsByModule = new HashMap<>();
        	boolean fGotTrialIDs = false, fGotProgramIDs = false;
			Collection<String> dbsToAccountFor = null;

			if (body.getTrialDbIds() == null)
				body.setTrialDbIds(new ArrayList<>());
			if (body.getProgramDbIds() == null)
				body.setProgramDbIds(new ArrayList<>());

	    	fGotTrialIDs = !body.getTrialDbIds().isEmpty();
	    	fGotProgramIDs = !body.getProgramDbIds().isEmpty();
        	HashMap<String /*module*/, HashSet<Integer> /*projects*/> projectsByModuleFromSpecifiedStudies = new HashMap<>();
        	if (body.getStudyDbIds() != null)
				for (String studyId : body.getStudyDbIds()) {
					String[] info = Helper.getInfoFromId(studyId, 2);
					HashSet<Integer> moduleProjects = projectsByModuleFromSpecifiedStudies.get(info[0]);
					if (moduleProjects == null) {
						moduleProjects = new HashSet<>();
						projectsByModuleFromSpecifiedStudies.put(info[0], moduleProjects);
					}
					moduleProjects.add(Integer.parseInt(info[1]));
				}
        	
        	boolean fGotGermplasmNames = body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty();
        	
        	HashMap<String, Collection<String>> dbIndividualsSpecifiedById = body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty() ? GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds()) : null;	        	
        	Set<String> dbsSpecifiedViaProgramsAndTrials = fGotTrialIDs && fGotProgramIDs ? body.getTrialDbIds().stream().distinct().filter(body.getProgramDbIds()::contains).collect(Collectors.toSet()) /*intersection*/ : fGotTrialIDs ? new HashSet<>(body.getTrialDbIds()) : (fGotProgramIDs ? new HashSet<>(body.getProgramDbIds()) : null);
        	List<Set<String>> moduleSetsToIntersect = new ArrayList<>();
        	if (!projectsByModuleFromSpecifiedStudies.isEmpty())
        		moduleSetsToIntersect.add(projectsByModuleFromSpecifiedStudies.keySet());
        	if (dbIndividualsSpecifiedById != null)
        		moduleSetsToIntersect.add(dbIndividualsSpecifiedById.keySet());
        	if (dbsSpecifiedViaProgramsAndTrials != null)
        		moduleSetsToIntersect.add(dbsSpecifiedViaProgramsAndTrials);
        	if (!moduleSetsToIntersect.isEmpty())
        		dbsToAccountFor = moduleSetsToIntersect.stream().skip(1).collect(() -> new HashSet<>(moduleSetsToIntersect.get(0)), Set::retainAll, Set::retainAll);
        	else
        		dbsToAccountFor = MongoTemplateManager.getAvailableModules();	// case where no supported parameters were found: return all allowed projects we find
        	
        	HashMap<String /*module*/, HashSet<Integer> /*projects*/> projectsByModuleFromSpecifiedGermplasm = new HashMap<>();
        	if (fGotGermplasmNames) {
    			if (dbsSpecifiedViaProgramsAndTrials == null && projectsByModuleFromSpecifiedStudies.isEmpty()) {
    				Status status = new Status();
    				status.setMessage("You must specify valid studyDbIds, trialDbIds or programDbIds to be able to filter on germplasmNames!");
    				metadata.addStatusItem(status);
    				return new ResponseEntity<StudyListResponse>(slr, HttpStatus.BAD_REQUEST);
    			}
    			else {
                    for (String db : dbsToAccountFor) {
                    	MongoTemplate mongoTemplate = MongoTemplateManager.get(db);
	                   	for (int nProjId : mongoTemplate.findDistinct("_id", GenotypingProject.class, Integer.class))	// make sure at least one germplasm exists in each project before returning it
	                   		 if (mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(nProjId), Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()))), GenotypingSample.class) > 0) {
	            					HashSet<Integer> moduleProjects = projectsByModuleFromSpecifiedGermplasm.get(db);
	           					if (moduleProjects == null) {
	           						moduleProjects = new HashSet<>();
	           						projectsByModuleFromSpecifiedGermplasm.put(db, moduleProjects);
	           					}
	           					moduleProjects.add(nProjId);
	                   		 }
                    }
    			}
        	}

        	if (dbIndividualsSpecifiedById != null) {
                 for (String db : dbsToAccountFor) {
                	 if ((dbsSpecifiedViaProgramsAndTrials == null) || body.getTrialDbIds().contains(db) || body.getProgramDbIds().contains(db)) {
	                	 Collection<String> individuals = dbIndividualsSpecifiedById.get(db);
	                	 List<Integer> projectsInvolvingIndividuals = MongoTemplateManager.get(db).findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(individuals)), GenotypingSample.FIELDNAME_PROJECT_ID, GenotypingSample.class, Integer.class);
                		 if (!projectsInvolvingIndividuals.isEmpty()) {
         					HashSet<Integer> moduleProjects = projectsByModuleFromSpecifiedGermplasm.get(db);
        					if (moduleProjects == null) {
        						moduleProjects = new HashSet<>(projectsInvolvingIndividuals);
        						projectsByModuleFromSpecifiedGermplasm.put(db, moduleProjects);
        					}
        					else
        						moduleProjects.retainAll(projectsInvolvingIndividuals);
                		 }
                	 }
                 }
             }
        	
        	// apply AND operator on active filters
        	for (String module : Stream.of(projectsByModuleFromSpecifiedStudies.keySet(), projectsByModuleFromSpecifiedGermplasm.keySet()).flatMap(x -> x.stream()).collect(Collectors.toList())) {
        		HashSet<Integer> retainedModuleProjects = projectsByModuleFromSpecifiedStudies.get(module);
        		HashSet<Integer> moduleProjectsFromGermplasm = projectsByModuleFromSpecifiedGermplasm.get(module);
        		if (moduleProjectsFromGermplasm != null) {
        			if (retainedModuleProjects != null)
        				retainedModuleProjects.retainAll(moduleProjectsFromGermplasm);
        			else
        				retainedModuleProjects = moduleProjectsFromGermplasm;
        		}
        		if (!retainedModuleProjects.isEmpty())
        			projectsByModule.put(module, retainedModuleProjects);
        	}

	    	for (String module : dbsToAccountFor)
	    		if (body.getCommonCropNames() == null || body.getCommonCropNames().isEmpty() || body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module)))) {	// only keep this DB's data if no crop was specified or if it is linked to a specified crop
		    		if (!projectsByModule.isEmpty() && !projectsByModule.containsKey(module))
		    			continue;
			    		
		    		if ((dbIndividualsSpecifiedById != null || fGotGermplasmNames) && !projectsByModuleFromSpecifiedGermplasm.containsKey(module))
		    			continue;

		    		for (GenotypingProject pj : MongoTemplateManager.get(module).find(projectsByModule.isEmpty() ? new Query() : new Query(Criteria.where("id_").in(projectsByModule.get(module))), GenotypingProject.class)) {
		        		if (tokenManager.canUserReadProject(token, module, pj.getId()))
			            	result.addDataItem(new Study() {{
			            		setTrialDbId(module);
			            		setStudyDbId(module + Helper.ID_SEPARATOR + pj.getId());
			            		setStudyType("genotype");
			            		setStudyName(pj.getName());	/* variantSets in GA4GH correspond to projects, i.e. studies in BrAPI v2 */
			            		setStudyDescription(pj.getDescription());
		            		}} );
		        	}
		    	}
	    	
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
	
			slr.setResult(result);		
            return new ResponseEntity<StudyListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<StudyListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
		if (trialDbId != null)
			body.setTrialDbIds(Arrays.asList(trialDbId));
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
		return searchStudiesPost(body, authorization);
	}

    @ApiIgnore
	@Override
	public ResponseEntity<StudyListResponse> studiesPost(String authorization, List<StudyNewRequest> body) {
		return new ResponseEntity<StudyListResponse>(HttpStatus.NOT_IMPLEMENTED);
	}

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
