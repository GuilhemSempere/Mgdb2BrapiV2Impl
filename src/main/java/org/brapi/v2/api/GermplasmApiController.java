package org.brapi.v2.api;

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.ObjectNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.brapi.v2.model.ExternalReferences;
import org.brapi.v2.model.ExternalReferencesInner;
import org.brapi.v2.model.Germplasm;
import org.brapi.v2.model.GermplasmListResponse;
import org.brapi.v2.model.GermplasmListResponseResult;
import org.brapi.v2.model.GermplasmSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@Controller
public class GermplasmApiController implements GermplasmApi {

    private static final Logger log = LoggerFactory.getLogger(GermplasmApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    AbstractTokenManager tokenManager;

    @org.springframework.beans.factory.annotation.Autowired
    public GermplasmApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }
    
    public static HashMap<String, Collection<String>> readGermplasmIDs(Collection<String> germplasmDbIds) throws Exception {
    	HashMap<String, Collection<String>> dbIndividuals = new HashMap<>();
        for (String gpId : germplasmDbIds) {
            String[] info = Helper.getInfoFromId(gpId, 2); 
            if (info == null) {
                throw new MalformedParametersException("malformed germplasmDbId: " + gpId);
            } else {
	       	 Collection<String> individuals = dbIndividuals.get(info[0]);
	       	 if (individuals == null) {
	       		 individuals = new ArrayList<>();
	       		 dbIndividuals.put(info[0], individuals);
	       	 }
	       	individuals.add(info[1]);
            }
        }
        return dbIndividuals;
    }

    public ResponseEntity<GermplasmListResponse> searchGermplasmPost(HttpServletResponse response, @ApiParam(value = "Germplasm Search request") @Valid @RequestBody GermplasmSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        if (body.getCommonCropNames() != null && body.getCommonCropNames().size() > 0) {
            return new ResponseEntity<>(HttpStatus.OK);	// not supported
        }
        
        GermplasmListResponse glr = new GermplasmListResponse();
        GermplasmListResponseResult result = new GermplasmListResponseResult();
        Metadata metadata = new Metadata();
        glr.setMetadata(metadata); 
        try {
            boolean fGotTrialIDs = false, fGotProgramIDs = false;
            Collection<String> dbsToAccountFor = null;

            if (body.getTrialDbIds() == null)
                    body.setTrialDbIds(new ArrayList<>());
            if (body.getProgramDbIds() == null)
                    body.setProgramDbIds(new ArrayList<>());

            fGotTrialIDs = !body.getTrialDbIds().isEmpty();
            fGotProgramIDs = !body.getProgramDbIds().isEmpty();
	    	
            Authentication auth = tokenManager.getAuthenticationFromToken(tokenManager.readToken(request));

        	HashMap<String /*module*/, HashSet<Integer> /*projects*/> projectsByModuleFromSpecifiedStudies = new HashMap<>();
        	if (body.getStudyDbIds() != null)
				for (String studyId : body.getStudyDbIds()) {
					String[] info = Helper.getInfoFromId(studyId, 2);
					int nProjId = Integer.parseInt(info[1]);
	            	if (!tokenManager.canUserReadProject(auth == null ? null : auth.getAuthorities(), info[0], nProjId)) {
	                    Status status = new Status();
	                    status.setMessage("You don't have access to this study: " + studyId);
	                    glr.getMetadata().addStatusItem(status);
	                    return new ResponseEntity<>(glr, HttpStatus.UNAUTHORIZED);
	                }
					HashSet<Integer> moduleProjects = projectsByModuleFromSpecifiedStudies.get(info[0]);
					if (moduleProjects == null) {
						moduleProjects = new HashSet<>();
						projectsByModuleFromSpecifiedStudies.put(info[0], moduleProjects);
					}
					moduleProjects.add(nProjId);
				}
        	
        	boolean fGotGermplasmNames = body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty();
        	boolean fGotExtRefs = body.getExternalReferenceIds() != null && !body.getExternalReferenceIds().isEmpty();
        	
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
        	else if (!fGotGermplasmNames && !fGotExtRefs) {
	            Status status = new Status();
	            status.setMessage("Please specify some of the following parameters: programDbIds, trialDbIds, studyDbIds, germplasmDbIds, germplasmNames, externalReferenceIds");
	            metadata.addStatusItem(status);
	            return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
        	}
        	
            if (dbsToAccountFor != null)
	            for (String db : dbsToAccountFor)
                        if (!tokenManager.canUserReadDB(auth == null ? null : auth.getAuthorities(), db)) {
                            Status status = new Status();
                            status.setMessage("You don't have access to this program / trial: " + db);
                            glr.getMetadata().addStatusItem(status);
                            return new ResponseEntity<>(glr, HttpStatus.UNAUTHORIZED);
                        }
                String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();

        	// germplasm name filtering
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
                    	List<Criteria> andCrits = new ArrayList<>();
                    	andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()));
	                	 
	                	// make sure we don't return individuals that are in projects this user doesn't have access to
	                	Collection<Integer> allowedProjects = projectsByModuleFromSpecifiedStudies.get(db);
	                	if (allowedProjects == null)
	                		allowedProjects = MgdbDao.getUserReadableProjectsIds(tokenManager, auth == null ? null : auth.getAuthorities(), db, true);
	                	andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(allowedProjects));
	                	 	
                   		List<String> individualsFoundByName = MongoTemplateManager.get(db).findDistinct(new Query(new Criteria().andOperator(andCrits)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
       					individualsByModuleFromSpecifiedGermplasm.put(db, new HashSet<>(individualsFoundByName));
                    }
    			}
        	}
        	
        	// ext ref filtering
        	if (fGotExtRefs) {
    			if (dbsSpecifiedViaProgramsAndTrials == null && projectsByModuleFromSpecifiedStudies.isEmpty()) {
    				Status status = new Status();
    				status.setMessage("You must specify valid studyDbIds, trialDbIds or programDbIds to be able to filter on externalReferences!");
    				metadata.addStatusItem(status);
    				return new ResponseEntity<GermplasmListResponse>(glr, HttpStatus.BAD_REQUEST);
    			}
    			else {
                    for (String db : dbsToAccountFor) {
                    	MongoTemplate mongoTemplate = MongoTemplateManager.get(db);
                    	List<Criteria> andCrits = new ArrayList<>();	/*TODO: should first check in CustomIndividualMetaData...*/
                    	List<String> individualsWithExtRefs = mongoTemplate.findDistinct(new Query(Criteria.where(Individual.SECTION_ADDITIONAL_INFO + "." + BrapiService.BRAPI_FIELD_extGermplasmDbId).in(body.getExternalReferenceIds())), "_id", Individual.class, String.class);
                    	andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(individualsWithExtRefs));
                    	
                    	// make sure we don't return individuals that are in projects this user doesn't have access to
                    	Collection<Integer> allowedProjects = projectsByModuleFromSpecifiedStudies.get(db);
	                	if (allowedProjects == null)
	                		allowedProjects = MgdbDao.getUserReadableProjectsIds(tokenManager, auth == null ? null : auth.getAuthorities(), db, true);
	                	andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(allowedProjects));
                    	
                   		List<String> individualsFoundByExtRef = mongoTemplate.findDistinct(new Query(new Criteria().andOperator(andCrits)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
     					HashSet<String> moduleIndividuals = individualsByModuleFromSpecifiedGermplasm.get(db);
    					if (moduleIndividuals == null)
    						individualsByModuleFromSpecifiedGermplasm.put(db, new HashSet<>(individualsFoundByExtRef));
    					else
    						moduleIndividuals.retainAll(individualsFoundByExtRef);
                    }
    			}
        	}

        	// germplasm id filtering
        	if (dbIndividualsSpecifiedById != null) {
                 for (String db : dbsToAccountFor) {
                	 if (dbsSpecifiedViaProgramsAndTrials == null || body.getTrialDbIds().contains(db) || body.getProgramDbIds().contains(db)) {
	                	 Collection<String> individuals = dbIndividualsSpecifiedById.get(db);
	                	 List<Criteria> andCrits = new ArrayList<>();
	                	 andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(individuals));
	                	 
	                	 // make sure we don't return individuals that are in projects this user doesn't have access to
	                	 Collection<Integer> allowedProjects = projectsByModuleFromSpecifiedStudies.get(db);
		                	if (allowedProjects == null)
		                		allowedProjects = MgdbDao.getUserReadableProjectsIds(tokenManager, auth == null ? null : auth.getAuthorities(), db, true);
		                	andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(allowedProjects));

	                	 List<String> individualsFoundById = MongoTemplateManager.get(db).findDistinct(new Query(new Criteria().andOperator(andCrits)), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
     						HashSet<String> moduleIndividuals = individualsByModuleFromSpecifiedGermplasm.get(db);
    					 if (moduleIndividuals == null)
    						 individualsByModuleFromSpecifiedGermplasm.put(db, new HashSet<>(individualsFoundById));
    					 else
    						 moduleIndividuals.retainAll(individualsFoundById);
                	 }
                 }
            }

            for (String database : dbsToAccountFor) {
                for (Individual ind : MgdbDao.getInstance().loadIndividualsWithAllMetadata(database, sCurrentUser, !projectsByModuleFromSpecifiedStudies.isEmpty() ? projectsByModuleFromSpecifiedStudies.get(database) : null, !individualsByModuleFromSpecifiedGermplasm.isEmpty() ? individualsByModuleFromSpecifiedGermplasm.get(database) : null).values()) {
                	
                	List synonyms = (List) ind.getAdditionalInfo().get("synonyms");
            		if (synonyms != null)
            			for (int i=0; i<synonyms.size(); i++) {
            				Object syn = synonyms.get(i);
            				if (syn instanceof String)
            					synonyms.set(i, new HashMap() {{ put("synonym", syn); }});	// hack synonym object from BrAPI v1 to BrAPI v2 format (avoids IllegalArgumentException)
            			}

                	Germplasm germplasm = objectMapper.convertValue(ind.getAdditionalInfo(), Germplasm.class);
                	germplasm.setGermplasmDbId(database + Helper.ID_SEPARATOR + ind.getId());
                	germplasm.setGermplasmName(ind.getId());

                	Object extRefId = ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_externalReferenceId);
	                if (extRefId != null) {
	                    ExternalReferencesInner ref = new ExternalReferencesInner();
                        ref.setReferenceId(extRefId.toString());
                        Object extRefSrc= ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_externalReferenceSource);
	                    if (extRefSrc != null)
	                        ref.setReferenceSource(ind.getAdditionalInfo().get(BrapiService.BRAPI_FIELD_externalReferenceSource).toString());
	                    germplasm.setExternalReferences(new ExternalReferences() {{ add(ref); }});
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

            return new ResponseEntity<>(glr, HttpStatus.OK);
        } catch (ObjectNotFoundException | MalformedParametersException e) {
            Status status = new Status();
            status.setMessage(e.getMessage());
            glr.getMetadata().addStatusItem(status);
            return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);        
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}