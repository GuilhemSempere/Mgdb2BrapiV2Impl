package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.text.CaseUtils;
import org.brapi.v2.model.ExternalReferences;
import org.brapi.v2.model.ExternalReferencesInner;
import org.brapi.v2.model.Germplasm;
import org.brapi.v2.model.GermplasmListResponse;
import org.brapi.v2.model.GermplasmListResponseResult;
import org.brapi.v2.model.GermplasmNewRequest.BiologicalStatusOfAccessionCodeEnum;
import org.brapi.v2.model.GermplasmSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.StudyListResponse;
import org.brapi.v2.model.StudySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@CrossOrigin
@Controller
public class GermplasmApiController implements GermplasmApi {

    private static final Logger log = LoggerFactory.getLogger(GermplasmApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    AbstractTokenManager tokenManager;

    @Autowired
    private GigwaGa4ghServiceImpl ga4ghService;

    @Autowired
    private BrapiRestController brapiV1Service;

    @org.springframework.beans.factory.annotation.Autowired
    public GermplasmApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }
    
    public static HashMap<String, Collection<String>> readGermplasmIDs(Collection<String> germplasmDbIds) {
    	HashMap<String, Collection<String>> dbIndividuals = new HashMap<>();
        for (String gpId : germplasmDbIds) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 2);  
	       	 Collection<String> individuals = dbIndividuals.get(info[0]);
	       	 if (individuals == null) {
	       		 individuals = new ArrayList<>();
	       		 dbIndividuals.put(info[0], individuals);
	       	 }
	       	individuals.add(info[1]);
        }
        return dbIndividuals;
    }

    public ResponseEntity<GermplasmListResponse> searchGermplasmPost(HttpServletResponse response, @ApiParam(value = "Germplasm Search request") @Valid @RequestBody GermplasmSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        if (body.getCommonCropNames() != null && body.getCommonCropNames().size() > 0) {
            return new ResponseEntity<>(HttpStatus.OK);	// not supported
        }
        try {
            GermplasmListResponse glr = new GermplasmListResponse();
            GermplasmListResponseResult result = new GermplasmListResponseResult();
            Metadata metadata = new Metadata();
            glr.setMetadata(metadata);

        	HashMap<String /*module*/, Collection<Integer> /*samples*/> individualsByModule = new HashMap<>();
        	boolean fGotTrialIDs = false, fGotProgramIDs = false;
			Collection<String> dbsToAccountFor = null;

			if (body == null)
				body = new GermplasmSearchRequest();	// should not happen?...
			if (body.getTrialDbIds() == null)
				body.setTrialDbIds(new ArrayList<>());
			if (body.getProgramDbIds() == null)
				body.setProgramDbIds(new ArrayList<>());

	    	fGotTrialIDs = !body.getTrialDbIds().isEmpty();
	    	fGotProgramIDs = !body.getProgramDbIds().isEmpty();
        	HashMap<String /*module*/, HashSet<Integer> /*projects*/> projectsByModuleFromSpecifiedStudies = new HashMap<>();
        	if (body.getStudyDbIds() != null)
				for (String studyId : body.getStudyDbIds()) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(studyId, 2);
					HashSet<Integer> moduleIndividuals = projectsByModuleFromSpecifiedStudies.get(info[0]);
					if (moduleIndividuals == null) {
						moduleIndividuals = new HashSet<>();
						projectsByModuleFromSpecifiedStudies.put(info[0], moduleIndividuals);
					}
					moduleIndividuals.add(Integer.parseInt(info[1]));
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
        	else if (!fGotGermplasmNames) {
	            Status status = new Status();
	            status.setMessage("Please specify some of the following parameters: programDbIds, trialDbIds, studyDbIds, germplasmDbIds, germplasmNames");
	            metadata.addStatusItem(status);
	            return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
        	}
        	
        	HashMap<String /*module*/, HashSet<String> /*individuals*/> individualsByModuleFromSpecifiedGermplasm = new HashMap<>();
        	if (fGotGermplasmNames) {
    			if (dbsSpecifiedViaProgramsAndTrials == null && projectsByModuleFromSpecifiedStudies.isEmpty()) {
    				Status status = new Status();
    				status.setMessage("You must specify valid studyDbIds, trialDbIds or programDbIds to be able to filter on germplasmNames!");
    				metadata.addStatusItem(status);
    				return new ResponseEntity<GermplasmListResponse>(glr, HttpStatus.BAD_REQUEST);
    			}
    			else {
                    for (String db : dbsToAccountFor) {
                    	List<Criteria> andCrit = new ArrayList<>();
                    	andCrit.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()));
                    	
                    	HashSet<Integer> projectsSpecifiedAsStudies = projectsByModuleFromSpecifiedStudies.get(db);
                    	if (projectsSpecifiedAsStudies != null && !projectsSpecifiedAsStudies.isEmpty())
                    		andCrit.add(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(projectsSpecifiedAsStudies));
                    	
                   		List<String> individualsFoundByName = MongoTemplateManager.get(db).findDistinct(new Query(andCrit.size() > 1 ? new Criteria().andOperator(andCrit.toArray(new Criteria[andCrit.size()])) : andCrit.get(0)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
       					individualsByModuleFromSpecifiedGermplasm.put(db, new HashSet<>(individualsFoundByName));
                    }
    			}
        	}

        	if (dbIndividualsSpecifiedById != null) {
                 for (String db : dbsToAccountFor) {
                	 if ((dbsSpecifiedViaProgramsAndTrials == null) || body.getTrialDbIds().contains(db) || body.getProgramDbIds().contains(db)) {
	                	 Collection<String> individuals = dbIndividualsSpecifiedById.get(db);
	                	 List<String> individualsFoundById = MongoTemplateManager.get(db).findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(individuals)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
                		 if (!individualsFoundById.isEmpty()) {
         					HashSet<String> moduleIndividuals = individualsByModuleFromSpecifiedGermplasm.get(db);
        					if (moduleIndividuals == null)
        						individualsByModuleFromSpecifiedGermplasm.put(db, new HashSet<>(individualsFoundById));
        					else
        						moduleIndividuals.retainAll(individualsFoundById);
                		 }
                	 }
                 }
             }

//        	System.err.println(projectsByModuleFromSpecifiedStudies);
//            System.err.println(individualsByModuleFromSpecifiedGermplasm);
//            System.err.println(dbsToAccountFor);
            
        	String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
            for (String database : dbsToAccountFor) {
                Authentication auth = tokenManager.getAuthenticationFromToken(tokenManager.readToken(request));
                String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
                
                Collection<Integer> selectedProjects = projectsByModuleFromSpecifiedStudies.get(database);
                Query q = selectedProjects == null || selectedProjects.isEmpty() ? new Query() : new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(selectedProjects));

                for (Individual ind : MgdbDao.getInstance().loadIndividualsWithAllMetadata(database, sCurrentUser, null, !individualsByModuleFromSpecifiedGermplasm.isEmpty() ? individualsByModuleFromSpecifiedGermplasm.get(database) : null).values()) {
                	Germplasm germplasm = new Germplasm();
                	germplasm.setGermplasmDbId(database + IGigwaService.ID_SEPARATOR + ind.getId());

	                // Add the extRefId and extRefSrc to externalReferences. If extRefSrc is sample, then the externalReferences id will be the sample germplasmDbId                        
	                if (ind.getAdditionalInfo().containsKey(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId)) {
	                    ExternalReferencesInner ref = new ExternalReferencesInner();
	                    if (ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceType).equals("sample")) {
	                        if (ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_extGermplasmDbId) != null)
	                            ref.setReferenceID(ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_extGermplasmDbId).toString());
	                    }
	                    else
	                        ref.setReferenceID(ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId).toString());
	                    if (ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource) != null)
	                        ref.setReferenceSource(ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource).toString());
	                    germplasm.setExternalReferences(new ExternalReferences() {{ add(ref);}});
	                }

                	for (String key : ind.getAdditionalInfo().keySet()) {
                        String sLCkey = key.toLowerCase();
                        Object val = ind.getAdditionalInfo().get(key);
                        if (val == null)
                        	continue;

                        if (!Germplasm.germplasmFields.containsKey(sLCkey) && !BrapiRestController.extRefList.contains(key) && !lowerCaseIdFieldName.equals(sLCkey)) {
                            if (HashMap.class.isAssignableFrom(Map.class))
                                for (String aiKey : ((Map<String, Object>) val).keySet())
                                    germplasm.putAdditionalInfoItem(aiKey, Helper.nullToEmptyString(((Map<String, Object>) val).get(aiKey)));
                            else
                                germplasm.putAdditionalInfoItem(key, val.toString());
                        } else {
                            switch (sLCkey) {
                                case "germplasmname":
                                    germplasm.setGermplasmName(val.toString());
                                    break;
                                case "defaultdisplayname":
                                    germplasm.setDefaultDisplayName(val.toString());
                                    break;
                                case "accessionnumber":
                                    germplasm.setAccessionNumber(val.toString());
                                    break;
                                case "germplasmpui":
                                    germplasm.setGermplasmPUI(val.toString());
                                    break;
                                case "pedigree":
                                    germplasm.setPedigree(val.toString());
                                    break;
                                case "seedsource":
                                    germplasm.setSeedSource(val.toString());
                                    break;
                                case "commoncropname":
                                    germplasm.setCommonCropName(val.toString());
                                    break;
                                case "institutecode":
                                    germplasm.setInstituteCode(val.toString());
                                    break;
                                case "institutename":
                                    germplasm.setInstituteName(val.toString());
                                    break;
                                case "biologicalstatusofaccessioncode":
                                    germplasm.setBiologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum.fromValue(val.toString()));
                                    break;
                                case "biologicalstatusofaccessiondescription":
                                    germplasm.setBiologicalStatusOfAccessionDescription(val.toString());
                                    break;
                                case "countryoforigincode":
                                    germplasm.setCountryOfOriginCode(val.toString());
                                    break;
                                case "genus":
                                    germplasm.setGenus(val.toString());
                                    break;
                                case "species":
                                    germplasm.setSpecies(val.toString());
                                    break;
                                case "speciesauthority":
                                    germplasm.setSpeciesAuthority(val.toString());
                                    break;
                                case "subtaxa":
                                    germplasm.setSubtaxa(val.toString());
                                    break;
                                case "subtaxaauthority":
                                    germplasm.setSubtaxaAuthority(val.toString());
                                    break;
                                case "acquisitiondate":
                                    germplasm.setAcquisitionDate(val.toString());
                                    break;
                            }
                        }
                	}                	
                	result.addDataItem(germplasm);
                }
            }
            glr.setResult(result);
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
            
            
//            
//            
//            String programDbId = null;
//            Integer projId = null;
//            Collection<String> germplasmIdsToReturn = new HashSet<>(), requestedGermplasmIDs;
//            if (body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty()) {
////                if (body.getStudyDbIds().size() > 1) {
////                    Status status = new Status();
////                    status.setMessage("You may only supply a single studyDbId at a time!");
////                    metadata.addStatusItem(status);
////                    return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
////                }
//            	for (String study : body.getStudyDbIds()) {
//	                String[] info = GigwaSearchVariantsRequest.getInfoFromId(study, 2);
//	                programDbId = info[0];
//	                projId = Integer.parseInt(info[1]);
//	                germplasmIdsToReturn = MgdbDao.getProjectIndividuals(programDbId, projId);
//            	}
//            } else if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
//                requestedGermplasmIDs = body.getGermplasmDbIds();
//                for (String gpId : requestedGermplasmIDs) {
//                    String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 2);
//                    if (programDbId == null) {
//                        programDbId = info[0];
//                    } else if (!programDbId.equals(info[0])) {
//                        Status status = new Status();
//                        status.setMessage("You may only supply IDs of germplasm records from one program at a time!");
//                        metadata.addStatusItem(status);
//                        return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
//                    }
//                    germplasmIdsToReturn.add(info[1]);
//                }
//            } else {
//                Status status = new Status();
//                status.setMessage("Either a studyDbId or a list of germplasmDbIds must be specified as parameter!");
//                metadata.addStatusItem(status);
//                return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
//            }
//
//            if (!tokenManager.canUserReadProject(token, programDbId, projId)) {
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//            
//            fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest gsr = new fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest();
//            gsr.accessionNumbers = body.getAccessionNumbers();
//            gsr.germplasmPUIs = body.getGermplasmPUIs();
//            gsr.germplasmGenus = body.getGenus();
//            gsr.germplasmSpecies = body.getSpecies();
//            gsr.germplasmNames = body.getGermplasmNames() == null ? null : body.getGermplasmNames().stream().map(nm -> nm.substring(1 + nm.lastIndexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList());
//            gsr.germplasmDbIds = germplasmIdsToReturn;
//            gsr.page = body.getPage();
//            gsr.pageSize = body.getPageSize();
//
//            Map<String, Object> v1response = (Map<String, Object>) brapiV1Service.executeGermplasmSearch(request, response, programDbId, gsr, Germplasm.germplasmFields);
//            Map<String, Object> v1Result = (Map<String, Object>) v1response.get("result");
//            ArrayList<Map<String, Object>> v1data = (ArrayList<Map<String, Object>>) v1Result.get("data");
//            String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
//            for (Map<String, Object> v1germplasmRecord : v1data) {
//                Germplasm germplasm = new Germplasm();
//
//                //add the extRefId and extRefSrc to externalReferences
//                //if extRefSrc is sample, then the externalReferences id will be the sample germplasmDbId                        
//                if (v1germplasmRecord.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId) != null) {
//                    ExternalReferencesInner ref = new ExternalReferencesInner();
//                    if (v1germplasmRecord.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceType).equals("sample")) {
//                        if (v1germplasmRecord.get(BrapiService.BRAPI_FIELD_extGermplasmDbId) != null) {
//                            ref.setReferenceID(v1germplasmRecord.get(BrapiService.BRAPI_FIELD_extGermplasmDbId).toString());
//                        }
//
//                    } else {
//                        ref.setReferenceID(v1germplasmRecord.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId).toString());
//                    }
//                    if (v1germplasmRecord.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource) != null) {
//                        ref.setReferenceSource(v1germplasmRecord.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource).toString());
//                    }
//                    ExternalReferences refs = new ExternalReferences();
//                    refs.add(ref);
//                    germplasm.setExternalReferences(refs);
//                }
//
//                for (String key : v1germplasmRecord.keySet()) {
//                    String sLCkey = key.toLowerCase();
//                    Object val = v1germplasmRecord.get(key);
//                    if (val == null)
//                    	continue;
//
//                    if (!Germplasm.germplasmFields.containsKey(sLCkey) && !BrapiRestController.extRefList.contains(key) && !lowerCaseIdFieldName.equals(sLCkey)) {
//                        if ("additionalinfo".equals(sLCkey)) {
//                            for (String aiKey : ((HashMap<String, String>) val).keySet()) {
//                                germplasm.putAdditionalInfoItem(aiKey, ((HashMap<String, String>) val).get(aiKey));
//                            }
//                        } else {
//                            germplasm.putAdditionalInfoItem(key, val.toString());
//                        }
//                    } else {
//                        switch (sLCkey) {
//                            case "germplasmdbid":
//                                germplasm.germplasmDbId(ga4ghService.createId(programDbId, projId, val.toString()));
//                                break;
//                            case "germplasmname":
//                                germplasm.setGermplasmName(val.toString());
//                                break;
//                            case "defaultdisplayname":
//                                germplasm.setDefaultDisplayName(val.toString());
//                                break;
//                            case "accessionnumber":
//                                germplasm.setAccessionNumber(val.toString());
//                                break;
//                            case "germplasmpui":
//                                germplasm.setGermplasmPUI(val.toString());
//                                break;
//                            case "pedigree":
//                                germplasm.setPedigree(val.toString());
//                                break;
//                            case "seedsource":
//                                germplasm.setSeedSource(val.toString());
//                                break;
//                            case "commoncropname":
//                                germplasm.setCommonCropName(val.toString());
//                                break;
//                            case "institutecode":
//                                germplasm.setInstituteCode(val.toString());
//                                break;
//                            case "institutename":
//                                germplasm.setInstituteName(val.toString());
//                                break;
//                            case "biologicalstatusofaccessioncode":
//                                germplasm.setBiologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum.fromValue(val.toString()));
//                                break;
//                            case "biologicalstatusofaccessiondescription":
//                                germplasm.setBiologicalStatusOfAccessionDescription(val.toString());
//                                break;
//                            case "countryoforigincode":
//                                germplasm.setCountryOfOriginCode(val.toString());
//                                break;
//                            case "genus":
//                                germplasm.setGenus(val.toString());
//                                break;
//                            case "species":
//                                germplasm.setSpecies(val.toString());
//                                break;
//                            case "speciesauthority":
//                                germplasm.setSpeciesAuthority(val.toString());
//                                break;
//                            case "subtaxa":
//                                germplasm.setSubtaxa(val.toString());
//                                break;
//                            case "subtaxaauthority":
//                                germplasm.setSubtaxaAuthority(val.toString());
//                                break;
//                            case "acquisitiondate":
//                                germplasm.setAcquisitionDate(val.toString());
//                                break;
//                        }
//                    }
//                }
//                result.addDataItem(germplasm);
//            }
//            glr.setResult(result);
//            IndexPagination pagination = new IndexPagination();
//            jhi.brapi.api.Metadata v1Metadata = (jhi.brapi.api.Metadata) v1response.get("metadata");
//            pagination.setPageSize(v1Metadata.getPagination().getPageSize());
//            pagination.setCurrentPage(v1Metadata.getPagination().getCurrentPage());
//            pagination.setTotalPages(v1Metadata.getPagination().getTotalPages());
//            pagination.setTotalCount((int) v1Metadata.getPagination().getTotalCount());
//            metadata.setPagination(pagination);

            return new ResponseEntity<>(glr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
