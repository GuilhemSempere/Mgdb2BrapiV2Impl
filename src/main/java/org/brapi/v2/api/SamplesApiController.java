package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.ObjectNotFoundException;
import javax.validation.Valid;

import org.brapi.v2.model.ExternalReferences;
import org.brapi.v2.model.ExternalReferencesInner;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Sample;
import org.brapi.v2.model.SampleListResponse;
import org.brapi.v2.model.SampleListResponseResult;
import org.brapi.v2.model.SampleSearchRequest;
import org.brapi.v2.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.exporting.IExportHandler;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;
import java.lang.reflect.MalformedParametersException;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@Controller
//@ApiIgnore
public class SamplesApiController implements SamplesApi {

    private static final Logger log = LoggerFactory.getLogger(SamplesApiController.class);
    
    @Autowired AbstractTokenManager tokenManager;
    
//    private final ObjectMapper objectMapper;
//
//    private final HttpServletRequest request;
//
//    @org.springframework.beans.factory.annotation.Autowired
//    public SamplesApiController(ObjectMapper objectMapper, HttpServletRequest request) {
//        this.objectMapper = objectMapper;
//        this.request = request;
//    }

    @Override
    public ResponseEntity<SampleListResponse> searchSamplesPost(@ApiParam(value = "")  @Valid @RequestBody SampleSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
        String token = ServerinfoApiController.readToken(authorization);
    	Authentication auth = tokenManager.getAuthenticationFromToken(token);

        SampleListResponse slr = new SampleListResponse();
        Metadata metadata = new Metadata();
        slr.setMetadata(metadata);
        SampleListResponseResult result = new SampleListResponseResult();
        slr.setResult(result);
        
        try {
        
            if ((body.getObservationUnitDbIds() != null && body.getObservationUnitDbIds().size() > 0) || (body.getPlateDbIds() != null && body.getPlateDbIds().size() > 0)) {
                Status status = new Status();
                status.setMessage("observationUnitDbIds and plateDbIds filters are not supported");
                slr.getMetadata().addStatusItem(status);
            }

            boolean fGotTrialIDs, fGotProgramIDs;
            Collection<String> dbsToAccountFor = new ArrayList();
            if (body.getTrialDbIds() == null)
                body.setTrialDbIds(new ArrayList<>());
            if (body.getProgramDbIds() == null)
                body.setProgramDbIds(new ArrayList<>());
            fGotTrialIDs = !body.getTrialDbIds().isEmpty();
            fGotProgramIDs = !body.getProgramDbIds().isEmpty();

            boolean fGotGermplasmNames = body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty();
            boolean fGotSampleIds = body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty();
            boolean fGotSampleNames = body.getSampleNames() != null && !body.getSampleNames().isEmpty();
            boolean fGotExtRefs = body.getExternalReferenceIds() != null && !body.getExternalReferenceIds().isEmpty();

            HashMap<String /*module*/, HashSet<Integer> /*projects*/> projectsByModuleFromSpecifiedStudies = new HashMap<>();

            if (body.getStudyDbIds() != null) {                    
                for (String studyId : body.getStudyDbIds()) {
                    String[] info = GigwaSearchVariantsRequest.getInfoFromId(studyId, 2);
                    String module = info[0];
                    int nProjId = Integer.parseInt(info[1]);
                    if (!tokenManager.canUserReadProject(auth == null ? null : auth.getAuthorities(), module, nProjId)) {
                        Status status = new Status();
                        status.setMessage("You don't have access to this study: " + studyId);
                        slr.getMetadata().addStatusItem(status);
                        return new ResponseEntity<>(slr, HttpStatus.UNAUTHORIZED);
                    }
                    HashSet<Integer> moduleProjects = projectsByModuleFromSpecifiedStudies.get(module);
                    if (moduleProjects == null) {
                        moduleProjects = new HashSet<>();
                        projectsByModuleFromSpecifiedStudies.put(module, moduleProjects);
                    }
                    moduleProjects.add(nProjId);
                }
            }

            HashMap<String /*module*/, Collection<String>/*germplasmIds*/> dbIndividualsSpecifiedById = body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty() ? GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds()) : null;	        	
            HashMap<String /*module*/, Collection<Long>/*sampleIds*/> dbSamplesSpecifiedById = body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty() ? readSampleIDs(body.getSampleDbIds()) : null;	        	

            Set<String> dbsSpecifiedViaProgramsAndTrials = fGotTrialIDs && fGotProgramIDs ? body.getTrialDbIds().stream().distinct().filter(body.getProgramDbIds()::contains).collect(Collectors.toSet()) /*intersection*/ : fGotTrialIDs ? new HashSet<>(body.getTrialDbIds()) : (fGotProgramIDs ? new HashSet<>(body.getProgramDbIds()) : null);
            List<Set<String>> moduleSetsToIntersect = new ArrayList<>();
            if (!projectsByModuleFromSpecifiedStudies.isEmpty())
                moduleSetsToIntersect.add(projectsByModuleFromSpecifiedStudies.keySet());
            if (dbIndividualsSpecifiedById != null)
                moduleSetsToIntersect.add(dbIndividualsSpecifiedById.keySet());
            if (dbSamplesSpecifiedById != null)
                moduleSetsToIntersect.add(dbSamplesSpecifiedById.keySet());
            if (dbsSpecifiedViaProgramsAndTrials != null)
                moduleSetsToIntersect.add(dbsSpecifiedViaProgramsAndTrials);
            if (!moduleSetsToIntersect.isEmpty())
                dbsToAccountFor = moduleSetsToIntersect.stream().skip(1).collect(() -> new HashSet<>(moduleSetsToIntersect.get(0)), Set::retainAll, Set::retainAll);
            else if (!fGotGermplasmNames && !fGotExtRefs && !fGotSampleIds && !fGotSampleNames) {
                Status status = new Status();
                status.setMessage("Please specify some of the following parameters: programDbIds, trialDbIds, studyDbIds, germplasmDbIds, germplasmNames, externalReferenceIds, sampleIds, sampleNames");
                metadata.addStatusItem(status);
                return new ResponseEntity<>(slr, HttpStatus.BAD_REQUEST);
            }

            if (fGotGermplasmNames) {
                if (dbsSpecifiedViaProgramsAndTrials == null && projectsByModuleFromSpecifiedStudies.isEmpty()) {
                    Status status = new Status();
                    status.setMessage("You must specify valid studyDbIds, trialDbIds or programDbIds to be able to filter on germplasmNames!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<SampleListResponse>(slr, HttpStatus.BAD_REQUEST);
                }
            }


            HashMap<String /*module*/, List<Integer>/*sampleIds*/> dbSamplesIds = new HashMap<>();
            long totalCount = 0;

            for (String db : dbsToAccountFor) {
                if (!tokenManager.canUserReadDB(auth == null ? null : auth.getAuthorities(), db)) {
                    Status status = new Status();
                    status.setMessage("You don't have access to this program / trial: " + db);
                    slr.getMetadata().addStatusItem(status);
                    return new ResponseEntity<>(slr, HttpStatus.UNAUTHORIZED);
                }
                
                List<Criteria> andCrits = new ArrayList<>();
                if (fGotGermplasmNames) {
                    andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()));                    
                }

                if (dbIndividualsSpecifiedById != null && dbIndividualsSpecifiedById.get(db) != null) {
                    Collection<String> germplasmIds = dbIndividualsSpecifiedById.get(db);
                    if (!germplasmIds.isEmpty()) {
                        andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmIds));
                    }
                }    

                if (dbSamplesSpecifiedById != null && dbSamplesSpecifiedById.get(db) != null) {
                    Collection<Long> sampleIds = dbSamplesSpecifiedById.get(db);
                    if (!sampleIds.isEmpty()) {
                        andCrits.add(Criteria.where("_id").in(sampleIds));
                    }
                }

                if (body.getSampleNames() != null && !body.getSampleNames().isEmpty()) {
                    andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_NAME).in(body.getSampleNames()));
                }

                if (body.getExternalReferenceIds() != null && !body.getExternalReferenceIds().isEmpty())  {
                    andCrits.add(new Criteria().where(Individual.SECTION_ADDITIONAL_INFO + "." + BrapiService.BRAPI_FIELD_externalReferenceId).in(body.getExternalReferenceIds()));
                }

                if (body.getExternalReferenceSources() != null && !body.getExternalReferenceSources().isEmpty())  {
                    andCrits.add(new Criteria().where(Individual.SECTION_ADDITIONAL_INFO + "." + BrapiService.BRAPI_FIELD_externalReferenceSource).in(body.getExternalReferenceSources()));
                }

                // make sure we don't return individuals that are in projects this user doesn't have access to
                Collection<Integer> allowedProjects = projectsByModuleFromSpecifiedStudies.get(db);
                if (allowedProjects == null) {
                    try {
                        allowedProjects = MgdbDao.getUserReadableProjectsIds(tokenManager, auth == null ? null : auth.getAuthorities(), db, true);
                    } catch (ObjectNotFoundException ex) {
                        log.error(ex.getMessage());
                    }
                }

                if (allowedProjects != null && !allowedProjects.isEmpty()) {
                    andCrits.add(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(allowedProjects));
                }

                Query q = !andCrits.isEmpty() ? new Query(new Criteria().andOperator(andCrits)) : new Query();
                List<Integer> foundSampleIds = MongoTemplateManager.get(db).findDistinct(q, "_id", GenotypingSample.class, Integer.class);
                if (!foundSampleIds.isEmpty()) {
                    totalCount = totalCount + foundSampleIds.size();
                    dbSamplesIds.put(db, foundSampleIds);
                }
            } 

            //convert to brapi format
            List<Sample> allBrapiSamples = new ArrayList<>();

            int page = body.getPage();            
            int pageSize = body.getPageSize();
            int totalPages = 0;
            if (totalCount % pageSize == 0) {
                totalPages = (int) (totalCount / pageSize);
            } else {
                totalPages = (int) (totalCount / pageSize + 1);
            }
            int firstIndex = 0;
            int lastIndex;
            int nbOfReturnedElts = 0;
            int previousDbNb = 0;
            for (String db : dbSamplesIds.keySet()) {
                List<Integer> sampleIds = dbSamplesIds.get(db);

                if (nbOfReturnedElts < pageSize) {

                    if (nbOfReturnedElts == 0) {
                        firstIndex = page * pageSize - previousDbNb;
                    }

                    previousDbNb = previousDbNb + sampleIds.size();

                    if (firstIndex>sampleIds.size()) {
                        continue;
                    }

                    lastIndex = firstIndex + pageSize;

                    if (lastIndex > sampleIds.size()) {
                        sampleIds = sampleIds.subList(firstIndex, sampleIds.size());
                    } else {
                        sampleIds = sampleIds.subList(firstIndex, lastIndex);
                    }

                    nbOfReturnedElts = nbOfReturnedElts + sampleIds.size();

                    List<Sample> brapiSamples = convertGenotypingSampleToBrapiSample(db, MgdbDao.getInstance().loadSamplesWithAllMetadata(db, AbstractTokenManager.getUserNameFromAuthentication(auth), null, sampleIds).values()); 
                    allBrapiSamples.addAll(brapiSamples);
                    firstIndex = 0;
                }
            }

            result.setData(allBrapiSamples);
            IndexPagination pagination = new IndexPagination();
            pagination.setPageSize(result.getData().size());
            pagination.setCurrentPage(body.getPage());
            pagination.setTotalPages(totalPages);
            pagination.setTotalCount((int) totalCount);
            slr.getMetadata().setPagination(pagination);

            return new ResponseEntity<SampleListResponse>(slr, HttpStatus.OK);
            
        } catch (ObjectNotFoundException | MalformedParametersException e) {
            Status status = new Status();
            status.setMessage(e.getMessage());
            slr.getMetadata().addStatusItem(status);
            return new ResponseEntity<>(slr, HttpStatus.BAD_REQUEST);        
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    private List<Sample> convertGenotypingSampleToBrapiSample(String database, Collection<GenotypingSample> genotypingSamples) {
        List<Sample> brapiSamples = new ArrayList<>();
        for (GenotypingSample mgdbSample : genotypingSamples) {
            Sample sample = new Sample();
            sample.sampleDbId(GigwaGa4ghServiceImpl.createId(database, mgdbSample.getId()));
            sample.germplasmDbId(GigwaGa4ghServiceImpl.createId(database, mgdbSample.getIndividual()));
            sample.setSampleName(mgdbSample.getSampleName());
            sample.studyDbId(database + IGigwaService.ID_SEPARATOR + mgdbSample.getProjectId());
            if (mgdbSample.getAdditionalInfo() != null) {
                sample.setAdditionalInfo(new HashMap<>());
                ExternalReferencesInner ref = new ExternalReferencesInner();
                for (String key:mgdbSample.getAdditionalInfo().keySet()) {
                    String value = mgdbSample.getAdditionalInfo().get(key).toString();
                    if (key.equals(BrapiService.BRAPI_FIELD_externalReferenceId))
                        ref.setReferenceID(value);
                    else if (key.equals(BrapiService.BRAPI_FIELD_externalReferenceSource))
                        ref.setReferenceSource(value);                    
                    else
                        sample.getAdditionalInfo().put(key, value);
                }
                if (ref.getReferenceID() != null) {
                    ExternalReferences refs = new ExternalReferences();
                    refs.add(ref);
                    sample.setExternalReferences(refs); 
                }
            }
            
            brapiSamples.add(sample);
        }
        return brapiSamples;
    }

    private HashMap<String, Collection<Long>> readSampleIDs(List<String> sampleDbIds) {
        HashMap<String, Collection<Long>> dbSampleIDs = new HashMap<>();
        for (String sId : sampleDbIds) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(sId, 2);
            if (info == null)
                throw new MalformedParametersException("malformed sampleDbId: " + sId);

            String db = info[0];            
            Long id = Long.parseLong(info[1]);
            Collection<Long> sampleIDs = dbSampleIDs.get(db);
            if (sampleIDs == null) {
               sampleIDs = new ArrayList<>();
               dbSampleIDs.put(info[0], sampleIDs);
            }
            sampleIDs.add(id);            
        }
        return dbSampleIDs;   
    }

}
