package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.ejb.ObjectNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;

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
    
    public static HashMap<String, HashMap<Integer, Collection<String>>> readGermplasmIDs(Collection<String> germplasmDbIds) {
    	HashMap<String, HashMap<Integer, Collection<String>>> dbProjectIndividuals = new HashMap<>();
        for (String gpId : germplasmDbIds) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 3);
	       	 int projId = Integer.parseInt(info[1]);
	            
	       	 HashMap<Integer, Collection<String>> projectIndividuals = dbProjectIndividuals.get(info[0]);
	       	 if (projectIndividuals == null) {
	       		 projectIndividuals = new HashMap<>();
	       		 dbProjectIndividuals.put(info[0], projectIndividuals);
	       	 }
	
	       	 Collection<String> individuals = projectIndividuals.get(projId);
	       	 if (individuals == null) {
	       		 individuals = new ArrayList<>();
	       		 projectIndividuals.put(projId, individuals);
	       	 }
	       	 individuals.add(info[2]);
        }
        return dbProjectIndividuals;
    }

    public ResponseEntity<GermplasmListResponse> searchGermplasmPost(HttpServletResponse response, @ApiParam(value = "Germplasm Search request") @Valid @RequestBody GermplasmSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        String token = ServerinfoApiController.readToken(authorization);

        if (body.getCommonCropNames() != null && body.getCommonCropNames().size() > 0) {
            return new ResponseEntity<>(HttpStatus.OK);	// not supported
        }
        
        try {
            GermplasmListResponse glr = new GermplasmListResponse();
            GermplasmListResponseResult result = new GermplasmListResponseResult();
            Metadata metadata = new Metadata();
            glr.setMetadata(metadata);
            
            String sErrorMsg = "";
            List<Criteria> vsCrits = new ArrayList<>();

            String programDbId = null;
            Integer projId = null;
            Collection<String> germplasmIdsToReturn = new HashSet<>(), requestedGermplasmIDs;
            
            Set<Integer> projIds = new HashSet<>();
            if (body.getStudyDbIds() != null) {
                for (String studyId : body.getStudyDbIds()) {
                    String[] info = GigwaSearchVariantsRequest.getInfoFromId(studyId, 2);
                    if (programDbId == null)
                        programDbId = info[0];
                    else if (!programDbId.equals(info[0]))
                        sErrorMsg += "You may only supply IDs of study records from one program at a time! ";
                    if (projId == null)
                        projId = Integer.parseInt(info[1]);
//                        else if (!projId.equals(Integer.parseInt(info[1])))
//                            sErrorMsg += "You may only supply a single studyDbId at a time!";
                    projIds.add(projId);
                }
            }   
            
            if (body.getGermplasmDbIds() != null) {                
                for (String gpId : body.getGermplasmDbIds()) {
                    String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 3);
                    if (programDbId == null) {
                        programDbId = info[0];
                    } else if (!programDbId.equals(info[0])) {
                        sErrorMsg += "You may only supply IDs of study records from one program at a time! ";
                    }
//                    if (projId == null) {
//                        projId = Integer.parseInt(info[1]);
//                    } else if (!projId.equals(Integer.parseInt(info[1]))) {
//                        Status status = new Status();
//                        status.setMessage("You may only supply a single studyDbId at a time!");
//                        metadata.addStatusItem(status);
//                        return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
//                    }
                    germplasmIdsToReturn.add(info[2]);
//                    projIds.add(projId);                    
                }
            }
            
            if (body.getGermplasmNames() != null) {
                germplasmIdsToReturn.addAll(body.getGermplasmNames());
            }
                
            if (body.getExternalReferenceIds() != null && !body.getExternalReferenceIds().isEmpty())  {
                vsCrits.add(new Criteria().where(Individual.SECTION_ADDITIONAL_INFO + "." + BrapiService.BRAPI_FIELD_germplasmExternalReferenceId).in(body.getExternalReferenceIds()));
            }
            
            if (body.getExternalReferenceSources() != null && !body.getExternalReferenceSources().isEmpty())  {
                vsCrits.add(new Criteria().where(Individual.SECTION_ADDITIONAL_INFO + "." + BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource).in(body.getExternalReferenceSources()));
            }

            if (body.getProgramDbIds() != null && !body.getProgramDbIds().isEmpty()) {
                if (body.getProgramDbIds().size() > 1) {
                    sErrorMsg += "You may only supply one programDbId at a time! ";  
                } else if (programDbId != null && !programDbId.equals(body.getProgramDbIds().get(0))) {
                    sErrorMsg += "the studyDbIds or germplasmDbIds are in a different programDbId from the one specified " ;               
                } else {
                    programDbId = body.getProgramDbIds().get(0);
                }
            } else if (projIds.isEmpty()) {
                sErrorMsg += "You must provide at least a programDbId, a studyDbId, a list of germplasmDbIds! ";
            }
            
            if (!sErrorMsg.isEmpty()) {
                Status status = new Status();
                status.setMessage(sErrorMsg);
                glr.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
            }

            String dbSpecies = MongoTemplateManager.getSpecies(programDbId);
            if (body.getSpecies() != null && dbSpecies != null) {
                List<String> lcTrimmedTaxonList = body.getSpecies().stream().map(sp -> sp.toLowerCase().trim()).collect(Collectors.toList());
                if (!lcTrimmedTaxonList.contains(dbSpecies.toLowerCase().trim())) {
                    return new ResponseEntity<>(glr, HttpStatus.OK);
                }
            }
            
            MongoTemplate mongoTemplate = MongoTemplateManager.get(programDbId);
            
            //Get list of projects the user has access to
            List<Integer> readableProjIds;
            try {
                readableProjIds = MgdbDao.getUserReadableProjectsIds(tokenManager, token, programDbId, true);
            } catch (ObjectNotFoundException e) {
                Status status = new Status();
                status.setMessage("You don't have access to this programDbId: " + programDbId);
                glr.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
            }
            
            //retrieve individual ids from samples collection based on projects ids
            Set<String> indIDs = new HashSet<>();
            if (!readableProjIds.isEmpty()) {
                if (!projIds.isEmpty()) {
                    readableProjIds.retainAll(projIds); //to only filter on projects based on filtered studies or germplasm or sample
                }
                Query q = new Query(new Criteria().where(GenotypingSample.FIELDNAME_PROJECT_ID).in(readableProjIds));
                List<GenotypingSample> genotypingSamples = mongoTemplate.find(q, GenotypingSample.class);
                indIDs = genotypingSamples.stream().map(s -> s.getIndividual()).collect(Collectors.toSet());
            } else {
                Status status = new Status();
                status.setMessage("You don't have access to any projects of this programDbId: " + programDbId);
                glr.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
            }
            
            if (germplasmIdsToReturn.isEmpty()) {
                germplasmIdsToReturn = indIDs;
            }

            if (!germplasmIdsToReturn.isEmpty()) {
                vsCrits.add(new Criteria().where("_id").in(germplasmIdsToReturn));
            }

            Query q = !vsCrits.isEmpty() ? new Query(new Criteria().andOperator(vsCrits)) : new Query();
            long count = mongoTemplate.count(q, Individual.class);

            if (body.getPageSize() != null) {
                q.limit(body.getPageSize());
                if (body.getPage() != null) {
                    q.skip(body.getPage() * body.getPage());
                }
            }
            List<Individual> individuals = mongoTemplate.find(q, Individual.class);

//            //check if user has read right to those individuals projects
//            List<Integer> forbiddenProjects = MgdbDao.getUserReadableProjectsIds(tokenManager, token, body.getProgramDbIds().get(0), false);
//            
//            Set<String> individualIds = new HashSet<>();
//            if (!forbiddenProjects.isEmpty()) {
//                projMap.keySet().removeAll(forbiddenProjects);
//                for (Integer key:projMap.keySet()) {
//                    individualIds.addAll(projMap.get(key));
//                }
//            } else {
//                individualIds = new HashSet<>(indIDsForCurrentPage);
//            }
//            
//            //load individuals metadata
//            Authentication auth = tokenManager.getAuthenticationFromToken(tokenManager.readToken(request));
//            String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
//            List<Individual> indList = new ArrayList(MgdbDao.getInstance().loadIndividualsWithAllMetadata(database, sCurrentUser, null, individualIds).values());
//            
            //convert to brapi format
            result = convertIndividualsToGermplasm(individuals, programDbId, projId);
            
            glr.setResult(result);
            glr.setMetadata(new Metadata());

            IndexPagination pagination = new IndexPagination();
            pagination.setPageSize(body.getPageSize());
            pagination.setCurrentPage(body.getPage());
            pagination.setTotalCount((int) count);
            int totalPages = 0;
            if (count % body.getPageSize() == 0) {
                totalPages = (int) (count/body.getPageSize());
            } else {
                totalPages = (int) (count/body.getPageSize()) + 1;
            }
            pagination.setTotalPages(totalPages);
            glr.getMetadata().setPagination(pagination);
            
            return new ResponseEntity<>(glr, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private GermplasmListResponseResult convertIndividualsToGermplasm(List<Individual> individuals, String programDbId, Integer projId) {
        GermplasmListResponseResult result = new GermplasmListResponseResult();
        String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
        
        for (Individual ind : individuals) {
            Germplasm germplasm = new Germplasm();
            germplasm.setGermplasmDbId(ga4ghService.createId(programDbId, projId, ind.getId()));
            germplasm.setGermplasmName(ga4ghService.createId(programDbId, projId, ind.getId())); 
            
            LinkedHashMap<String, Object> addInfo = ind.getAdditionalInfo();
            
            if (addInfo.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId) != null) {
                ExternalReferencesInner ref = new ExternalReferencesInner();
                ref.setReferenceID(addInfo.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceId).toString());
                
                if (addInfo.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource) != null) {
                    ref.setReferenceSource(addInfo.get(BrapiService.BRAPI_FIELD_germplasmExternalReferenceSource).toString());
                }
                ExternalReferences refs = new ExternalReferences();
                refs.add(ref);
                germplasm.setExternalReferences(refs);                

                for (String key : addInfo.keySet()) {
                    
                    String sLCkey = key.toLowerCase();
                    Object val = addInfo.get(key);
                    if (val == null)
                    	continue;

                    if (!Germplasm.germplasmFields.containsKey(sLCkey) && !BrapiRestController.extRefList.contains(key) && !lowerCaseIdFieldName.equals(sLCkey)) {
                        if ("additionalinfo".equals(sLCkey)) {
                            for (String aiKey : ((HashMap<String, String>) val).keySet()) {
                                germplasm.putAdditionalInfoItem(aiKey, ((HashMap<String, String>) val).get(aiKey));
                            }
                        } else {
                            germplasm.putAdditionalInfoItem(key, val.toString());
                        }
                        
                    } else {
                        switch (sLCkey) {
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
                
            }

            result.addDataItem(germplasm);
        }

        return result;
    }
}
