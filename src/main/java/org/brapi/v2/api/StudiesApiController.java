package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.ObjectNotFoundException;

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

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.subtypes.Callset;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import springfox.documentation.annotations.ApiIgnore;

@javax.annotation.Generated(value = "org.brapi.v2.codegen.v3.generators.java.SpringCodegen", date = "2021-03-16T09:51:33.671Z[GMT]")
@Controller
public class StudiesApiController implements StudiesApi {

    private static final Logger log = LoggerFactory.getLogger(StudiesApiController.class);

    @Autowired AbstractTokenManager tokenManager;

    @Override
    public ResponseEntity<StudyListResponse> searchStudiesPost(StudySearchRequest body, String authorization) {
        String token = ServerinfoApiController.readToken(authorization);
        
        StudyListResponse slr = new StudyListResponse();
        StudyListResponseResult result = new StudyListResponseResult();
        Metadata metadata = new Metadata();
        slr.setMetadata(metadata);
        
        try {
            // Identify studies using shared logic
            StudyIdentificationResult identificationResult = identifyStudies(body, token);
            Map<String, Set<Integer>> projectsByModule = identificationResult.getProjectsByModule();
            Set<String> dbsToAccountFor = identificationResult.getDatabases();
            
            // Determine if any filters were applied
            boolean hasDatabaseFilter = body.getProgramDbIds() != null && !body.getProgramDbIds().isEmpty() ||
                                        body.getTrialDbIds() != null && !body.getTrialDbIds().isEmpty();
            boolean hasProjectFilter = body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty() ||
                                       body.getStudyNames() != null && !body.getStudyNames().isEmpty() ||
                                       body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty() ||
                                       body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty();
            
            // Case 1: No filters at all - return ALL projects from ALL databases
            if (!hasDatabaseFilter && !hasProjectFilter) {
                dbsToAccountFor = new HashSet<>(MongoTemplateManager.getAvailableModules());
                for (String module : dbsToAccountFor) {
                    MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
                    if (mongoTemplate == null) continue;
                    
                    Set<Integer> moduleProjects = new HashSet<>();
                    for (GenotypingProject pj : mongoTemplate.findAll(GenotypingProject.class)) {
                        if (tokenManager.canUserReadProject(token, module, pj.getId())) {
                            moduleProjects.add(pj.getId());
                        }
                    }
                    if (!moduleProjects.isEmpty()) {
                        projectsByModule.put(module, moduleProjects);
                    }
                }
            }
            
            // Case 2: Filters applied but no projects matched - return empty
            if ((hasDatabaseFilter || hasProjectFilter) && projectsByModule.isEmpty()) {
                IndexPagination pagination = new IndexPagination();
                pagination.setPageSize(0);
                pagination.setCurrentPage(0);
                pagination.setTotalPages(1);
                pagination.setTotalCount(0L);
                metadata.setPagination(pagination);
                slr.setResult(result);
                return new ResponseEntity<>(slr, HttpStatus.OK);
            }
            
            // Build response
            for (String module : dbsToAccountFor) {
                // Check crop filter
                if (body.getCommonCropNames() != null && !body.getCommonCropNames().isEmpty() &&
                    !body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module)))) {
                    continue;
                }
                
                if (!projectsByModule.containsKey(module)) {
                    continue;
                }
                
                Query query = new Query(Criteria.where("id_").in(projectsByModule.get(module)));
                
                for (GenotypingProject pj : MongoTemplateManager.get(module).find(query, GenotypingProject.class)) {
                    if (tokenManager.canUserReadProject(token, module, pj.getId())) {
                        result.addDataItem(new Study() {{
                            setTrialDbId(module);
                            setStudyDbId(module + Helper.ID_SEPARATOR + pj.getId());
                            setStudyType("genotype");
                            setStudyName(pj.getName());
                            setStudyDescription(pj.getDescription());
                        }});
                    }
                }
            }
            
            // Build pagination
            IndexPagination pagination = new IndexPagination();
            pagination.setPageSize(result.getData().size());
            pagination.setCurrentPage(0);
            pagination.setTotalPages(1);
            pagination.setTotalCount((long) result.getData().size());
            metadata.setPagination(pagination);
            
            slr.setResult(result);
            return new ResponseEntity<>(slr, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Couldn't process search request", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    
    @Override
    public ResponseEntity<StudyListResponse> deleteStudiesPost(StudySearchRequest body, String authorization) {
        String token = ServerinfoApiController.readToken(authorization);
        
        // Validate request
        boolean hasStudyDbIds = body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty();
        boolean hasStudyNames = body.getStudyNames() != null && !body.getStudyNames().isEmpty();
        boolean hasProgramDbIds = body.getProgramDbIds() != null && !body.getProgramDbIds().isEmpty();
        boolean hasTrialDbIds = body.getTrialDbIds() != null && !body.getTrialDbIds().isEmpty();
        boolean hasGermplasmNames = body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty();
        boolean hasGermplasmDbIds = body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty();
        
        Metadata metadata = new Metadata();
        StudyListResponse response = new StudyListResponse();
        response.setMetadata(metadata);
        
        // Unsupported filters for deletion
        if (hasGermplasmNames || hasGermplasmDbIds) {
            metadata.addStatusItem(new Status() {{
                setMessage("Study deletion only supports studyDbIds, studyNames, programDbIds and trialDbIds filters.");
                setMessageType(Status.MessageTypeEnum.ERROR);
            }});
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // At least one of studyDbIds or studyNames is required
        if (!hasStudyDbIds && !hasStudyNames) {
            metadata.addStatusItem(new Status() {{
                setMessage("Study deletion requires at least one studyDbId or studyName.");
                setMessageType(Status.MessageTypeEnum.ERROR);
            }});
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // studyNames require programDbIds or trialDbIds
        if (hasStudyNames && !hasProgramDbIds && !hasTrialDbIds) {
            metadata.addStatusItem(new Status() {{
                setMessage("studyNames filter requires programDbIds or trialDbIds to be specified.");
                setMessageType(Status.MessageTypeEnum.ERROR);
            }});
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Identify studies using shared logic
            StudyIdentificationResult identificationResult = identifyStudies(body, token);
            Map<String, Set<Integer>> projectsByModule = identificationResult.getProjectsByModule();
            
            // Determine if any filters were applied
            boolean hasDatabaseFilter = hasProgramDbIds || hasTrialDbIds;
            boolean hasProjectFilter = hasStudyDbIds || hasStudyNames;
            
            // If filters were applied but no projects matched, return empty
            if ((hasDatabaseFilter || hasProjectFilter) && projectsByModule.isEmpty()) {
                metadata.addStatusItem(new Status() {{
                    setMessage("No studies matched the supplied criteria.");
                    setMessageType(Status.MessageTypeEnum.INFO);
                }});
                IndexPagination pagination = new IndexPagination();
                pagination.setPageSize(0);
                pagination.setCurrentPage(0);
                pagination.setTotalPages(1);
                pagination.setTotalCount(0L);
                metadata.setPagination(pagination);
                response.setResult(new StudyListResponseResult());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            
            // Process deletion
            StudyListResponseResult result = new StudyListResponseResult();
            int deletedCount = 0, failedCount = 0, forbiddenCount = 0;
            
            for (Map.Entry<String, Set<Integer>> entry : projectsByModule.entrySet()) {
                String module = entry.getKey();
                for (Integer projectId : entry.getValue()) {
                    String studyDbId = module + Helper.ID_SEPARATOR + projectId;
                    Study studyResult = new Study();
                    studyResult.setStudyDbId(studyDbId);
                    
                    Map<String, String> additionalInfo = new HashMap<>();
                    
                    // Check write permission
                    if (!tokenManager.canUserWriteToProject(token, module, projectId)) {
                        additionalInfo.put("success", "false");
                        additionalInfo.put("message", "Insufficient permissions to delete this study.");
                        forbiddenCount++;
                    } else {
                        // Perform actual deletion
                        if (MgdbDao.removeProjectAndRelatedRecords(module, projectId)) {
                            additionalInfo.put("success", "true");
                            additionalInfo.put("message", "Study deleted successfully.");
                            deletedCount++;
                        } else {
                            additionalInfo.put("success", "false");
                            additionalInfo.put("message", "Failed to delete study.");
                            failedCount++;
                        }
                    }
                    
                    studyResult.setAdditionalInfo(additionalInfo);
                    result.addDataItem(studyResult);
                }
            }
            
            // Build pagination
            IndexPagination pagination = new IndexPagination();
            pagination.setPageSize(result.getData().size());
            pagination.setCurrentPage(0);
            pagination.setTotalPages(1);
            pagination.setTotalCount((long) result.getData().size());
            metadata.setPagination(pagination);
            
            // Build status message
            String statusMessage;
            Status.MessageTypeEnum messageType;
            
            if (forbiddenCount > 0 && failedCount > 0) {
                statusMessage = String.format("Deleted %d studies. %d failed to delete. %d had insufficient permissions. Check additionalInfo for details.", 
                    deletedCount, failedCount, forbiddenCount);
                messageType = Status.MessageTypeEnum.WARNING;
            } else if (forbiddenCount > 0) {
                statusMessage = String.format("Deleted %d studies. %d studies had insufficient permissions. Check additionalInfo for details.", 
                    deletedCount, forbiddenCount);
                messageType = Status.MessageTypeEnum.WARNING;
            } else if (failedCount > 0) {
                statusMessage = String.format("Deleted %d studies. %d studies failed to delete. Check additionalInfo for details.", 
                    deletedCount, failedCount);
                messageType = Status.MessageTypeEnum.WARNING;
            } else if (deletedCount > 0) {
                statusMessage = String.format("Successfully deleted %d studies.", deletedCount);
                messageType = Status.MessageTypeEnum.INFO;
            } else {
                statusMessage = "No studies were deleted.";
                messageType = Status.MessageTypeEnum.INFO;
            }
            
            metadata.addStatusItem(new Status() {{
                setMessage(statusMessage);
                setMessageType(messageType);
            }});
            
            response.setResult(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Couldn't process deletion request", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper class for study identification result
    private static class StudyIdentificationResult {
        private final Set<String> databases;
        private final Map<String, Set<Integer>> projectsByModule;
        
        public StudyIdentificationResult(Set<String> databases, Map<String, Set<Integer>> projectsByModule) {
            this.databases = databases;
            this.projectsByModule = projectsByModule;
        }
        
        public Set<String> getDatabases() { return databases; }
        public Map<String, Set<Integer>> getProjectsByModule() { return projectsByModule; }
    }

    private StudyIdentificationResult identifyStudies(StudySearchRequest body, String token) throws ObjectNotFoundException {
        Set<String> allowedModules = new HashSet<>(MongoTemplateManager.getAvailableModules());
        
        boolean fGotTrialIDs = body.getTrialDbIds() != null && !body.getTrialDbIds().isEmpty();
        boolean fGotProgramIDs = body.getProgramDbIds() != null && !body.getProgramDbIds().isEmpty();
        boolean fGotStudyNames = body.getStudyNames() != null && !body.getStudyNames().isEmpty();
        boolean fGotStudyDbIds = body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty();
        boolean fGotGermplasmNames = body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty();
        boolean fGotGermplasmDbIds = body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty();
        
        // Step 1: Determine databases from program/trial filters (intersection)
        Set<String> dbsSpecifiedViaProgramsAndTrials = new HashSet<>();
        if (fGotTrialIDs && fGotProgramIDs) {
            dbsSpecifiedViaProgramsAndTrials.addAll(body.getTrialDbIds());
            dbsSpecifiedViaProgramsAndTrials.retainAll(body.getProgramDbIds());
            dbsSpecifiedViaProgramsAndTrials.retainAll(allowedModules);
        } else if (fGotTrialIDs) {
            dbsSpecifiedViaProgramsAndTrials.addAll(
                body.getTrialDbIds().stream()
                    .filter(allowedModules::contains)
                    .collect(Collectors.toSet())
            );
        } else if (fGotProgramIDs) {
            dbsSpecifiedViaProgramsAndTrials.addAll(
                body.getProgramDbIds().stream()
                    .filter(allowedModules::contains)
                    .collect(Collectors.toSet())
            );
        }
        
        // Step 2: Determine which databases to search
        Set<String> dbsToAccountFor = new HashSet<>();
        boolean hasDatabaseFilter = fGotTrialIDs || fGotProgramIDs;
        
        if (hasDatabaseFilter) {
            dbsToAccountFor.addAll(dbsSpecifiedViaProgramsAndTrials);
        } else {
            dbsToAccountFor.addAll(allowedModules);
        }
        
        Map<String, Set<Integer>> projectsByModule = new HashMap<>();
        
        // Step 3: If no project-level filters, return ALL projects from databases
        boolean hasProjectLevelFilter = fGotStudyDbIds || fGotStudyNames || fGotGermplasmNames || fGotGermplasmDbIds;
        
        if (!hasProjectLevelFilter) {
            for (String module : dbsToAccountFor) {
                if (body.getCommonCropNames() != null && !body.getCommonCropNames().isEmpty() &&
                    !body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module)))) {
                    continue;
                }
                
                MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
                if (mongoTemplate == null) continue;
                
                Set<Integer> moduleProjects = new HashSet<>();
                for (GenotypingProject pj : mongoTemplate.findAll(GenotypingProject.class)) {
                    if (tokenManager.canUserReadProject(token, module, pj.getId())) {
                        moduleProjects.add(pj.getId());
                    }
                }
                if (!moduleProjects.isEmpty()) {
                    projectsByModule.put(module, moduleProjects);
                }
            }
            return new StudyIdentificationResult(dbsToAccountFor, projectsByModule);
        }
        
        // Step 4: Build MongoDB query with $and combining all active filters
        for (String module : dbsToAccountFor) {
            MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
            if (mongoTemplate == null) continue;
            
            // Check crop filter
            if (body.getCommonCropNames() != null && !body.getCommonCropNames().isEmpty() &&
                !body.getCommonCropNames().contains(Helper.nullToEmptyString(MongoTemplateManager.getTaxonName(module)))) {
                continue;
            }
            
            List<Criteria> andCriteria = new ArrayList<>();
            boolean hasMatchingProjectId = false;
            
            // Add studyDbIds filter
            if (fGotStudyDbIds) {
                Set<Integer> projectIds = new HashSet<>();
                for (String studyId : body.getStudyDbIds()) {
                    String[] info = Helper.getInfoFromId(studyId, 2);
                    if (info[0].equals(module)) {
                        projectIds.add(Integer.parseInt(info[1]));
                        hasMatchingProjectId = true;
                    }
                }
                if (!projectIds.isEmpty()) {
                    andCriteria.add(Criteria.where("id_").in(projectIds));
                }
                // If studyDbIds filter is active but no project IDs match this module, skip this module
                if (!hasMatchingProjectId) {
                    continue;
                }
            }
            
            // Add studyNames filter
            if (fGotStudyNames)
                andCriteria.add(Criteria.where("name").in(body.getStudyNames()));
            
            // Add germplasm filters (if any)
            if (fGotGermplasmNames || fGotGermplasmDbIds) {
                List<String> germplasmConditions = new ArrayList<>();
                if (fGotGermplasmNames) {
                    germplasmConditions.addAll(body.getGermplasmNames());
                }
                if (fGotGermplasmDbIds) {
                    Map<String, Collection<String>> dbIndividualsSpecifiedById = 
                        GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds());
                    if (dbIndividualsSpecifiedById != null && dbIndividualsSpecifiedById.containsKey(module)) {
                        germplasmConditions.addAll(dbIndividualsSpecifiedById.get(module));
                    }
                }
                
                if (!germplasmConditions.isEmpty()) {
                    List<Integer> projectsWithGermplasm = mongoTemplate.findDistinct(
                        new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmConditions)),
                        GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_PROJECT_ID,
                        GenotypingSample.class,
                        Integer.class
                    );
                    
                    if (projectsWithGermplasm.isEmpty()) {
                        continue;
                    }
                    andCriteria.add(Criteria.where("id_").in(projectsWithGermplasm));
                } else {
                    // Germplasm filter is active but no conditions for this module
                    continue;
                }
            }
            
            // If no criteria, skip
            if (andCriteria.isEmpty()) {
                continue;
            }
            
            // Build the $and query
            Criteria finalCriteria = new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
            Query query = new Query(finalCriteria);
            
            // Execute query and collect results
            List<GenotypingProject> projects = mongoTemplate.find(query, GenotypingProject.class);
            Set<Integer> moduleProjects = new HashSet<>();
            for (GenotypingProject pj : projects) {
                if (tokenManager.canUserReadProject(token, module, pj.getId())) {
                    moduleProjects.add(pj.getId());
                }
            }
            
            if (!moduleProjects.isEmpty()) {
                projectsByModule.put(module, moduleProjects);
            }
        }
        
        return new StudyIdentificationResult(dbsToAccountFor, projectsByModule);
    }
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
