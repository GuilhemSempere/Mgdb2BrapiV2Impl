package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallsListResponseResult;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.ListValue;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.TokenPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import org.brapi.v2.model.CallGenotypeMetadata;
import org.brapi.v2.model.CallsListResponse;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Update;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@CrossOrigin
@Controller
public class CallsApiController implements CallsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsApiController.class);

    @Autowired AbstractTokenManager tokenManager;

    @Override
    public ResponseEntity<CallsListResponse> callsGet(
            String callSetDbId, 
            String variantDbId, 
            String variantSetDbId, 
            Boolean expandHomozygotes, 
            String unknownString, 
            String sepPhased, 
            String sepUnphased,
            Integer page, 
            Integer pageSize, 
            String authorization) {
        
        if (variantSetDbId == null && callSetDbId != null) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
            GenotypingSample sample = MongoTemplateManager.get(info[0]).find(new Query(Criteria.where("_id").is(Integer.parseInt(info[2]))), GenotypingSample.class).iterator().next();
            variantSetDbId = info[0] + IGigwaService.ID_SEPARATOR + sample.getProjectId() + IGigwaService.ID_SEPARATOR + sample.getRun();
        }
		
        CallsSearchRequest csr = new CallsSearchRequest();
        csr.setExpandHomozygotes(expandHomozygotes);
        csr.setUnknownString(unknownString);
        csr.setSepUnphased(sepUnphased);
        csr.setSepPhased(sepPhased);
        csr.setPageSize(pageSize);
        
        if (callSetDbId != null)
                csr.setCallSetDbIds(Arrays.asList(callSetDbId));
        if (variantDbId != null)
                csr.setVariantDbIds(Arrays.asList(variantDbId));
        if (variantSetDbId != null)
                csr.setVariantSetDbIds(Arrays.asList(variantSetDbId));

        return searchCallsPost(authorization, csr);
    }

    @Override
    public ResponseEntity<CallsListResponse> callsPut(String authorization, List<Call> callsToUpdate) {
        String token = ServerinfoApiController.readToken(authorization);
        
        CallsListResponse response = new CallsListResponse();
        Metadata metadata = new Metadata();
	response.setMetadata(metadata);
        
        String module = null;
        for (Call c:callsToUpdate) {
            if (c.getVariantSetDbId() != null) {
                module = GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3)[0];
            } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3)[0])) {
                Status status = new Status();
                status.setMessage("You must specify VariantSets belonging to the same referenceSet!");
                response.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

            }
        }
        
        MongoTemplate mongoTemplate = MongoTemplateManager.get(module);

    	// check permissions
    	Collection<Integer> projectIDs = callsToUpdate.stream()
                .map(call -> Integer.parseInt(GigwaSearchVariantsRequest.getInfoFromId(call.getVariantSetDbId(), 3)[1]))
                .collect(Collectors.toSet());
        
    	mongoTemplate.findDistinct(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(callsToUpdate.stream().map(call -> call.getVariantSetDbId().substring(1 + call.getVariantSetDbId().indexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList()))), "_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, VariantRunData.class, Integer.class);
    	
        List<Integer> forbiddenProjectIDs = new ArrayList<>();
        for (int pj : projectIDs) {
            if (!tokenManager.canUserReadProject(token, module, pj))
                forbiddenProjectIDs.add(pj);
        }
        projectIDs.removeAll(forbiddenProjectIDs);
        
        if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        List<Call> updatedCalls = new ArrayList<>();
        
        for (Call c:callsToUpdate) {
            
            if (c.getVariantDbId() == null || c.getVariantSetDbId() == null || c.getCallSetDbId() == null) {
               return new ResponseEntity<>(HttpStatus.FORBIDDEN);
               
            } else {

                List<Criteria> crits = new ArrayList<>();

                String[] info = GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3);
                crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])));
                crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2]));
                crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).is(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantDbId(), 2)[1]));                

                Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));


                runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
                String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(c.getCallSetDbId(), 3);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[2]);

                Update update = new Update();
                update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[2] + "." + SampleGenotype.FIELDNAME_GENOTYPECODE, c.getGenotypeValue());
                
                try {
                    VariantRunData vrd = mongoTemplate.findAndModify(runQuery, update, new FindAndModifyOptions().returnNew(true), VariantRunData.class, mongoTemplate.getCollectionName(VariantRunData.class));
                    updatedCalls.add(c);
                } catch(Exception e) {
                    Status status = new Status();
                    status.setMessage(e.getMessage());
                    response.getMetadata().addStatusItem(status);
                    return new ResponseEntity(response, HttpStatus.EXPECTATION_FAILED);
                }
            }
        }
        CallsListResponseResult result = new CallsListResponseResult(); //TODO retrieve information of sepPhased, unknownString...
        result.setData(updatedCalls);
        response.setResult(result);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CallsListResponse> searchCallsPost(String authorization, CallsSearchRequest body) {
        String token = ServerinfoApiController.readToken(authorization);

    	CallsListResponse clr = new CallsListResponse();
    	CallsListResponseResult result = new CallsListResponseResult();
    	Metadata metadata = new Metadata();
	clr.setMetadata(metadata);

        boolean fGotVariantSetList = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
        boolean fGotVariantList = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
        boolean fGotCallSetList = body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty();
        if (!fGotVariantSetList && !fGotVariantList && !fGotCallSetList) {
            Status status = new Status();
            status.setMessage("You must specify at least one of callSetDbId, variantDbId, or variantSetDbId!");
            metadata.addStatusItem(status);
            return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);
        }


        String module = null;
        if (fGotVariantSetList) {
            for (String variantDbId : body.getVariantSetDbIds()) {
                if (module == null) {
                    module = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 3)[0];
                } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 3)[0])) {
                    Status status = new Status();
                    status.setMessage("You must specify VariantSets belonging to the same referenceSet!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);

		}
            }
        }

        if (fGotVariantList) {
            for (String variantDbId : body.getVariantDbIds()) {
                if (module == null) {
                    module = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2)[0];
                } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2)[0])) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants only belonging to the same referenceSet!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);
                }
            }
        }

    	HashMap<Integer, String> sampleIndividuals = new HashMap<>();	// we are going to need the individual each sample is related to, in order to build callSetDbIds

        if (fGotCallSetList) {
            for (String callSetDbId : body.getCallSetDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 2);
                if (module == null) {
                    module = info[0];
                } else if (!module.equals(info[0])) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants / CallSets only belonging to the same referenceSet!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);
                }
                sampleIndividuals.put(Integer.parseInt(info[1]), info[1]);
            }

            // identify the runs those samples are involved in
            body.setVariantSetDbIds(new ArrayList<>());
            for (GenotypingSample sp : MongoTemplateManager.get(module).find(new Query(Criteria.where("_id").in(sampleIndividuals.keySet())), GenotypingSample.class)) {
                String variantSetDbId = module + IGigwaService.ID_SEPARATOR + sp.getProjectId() + IGigwaService.ID_SEPARATOR + sp.getRun();
                if (!body.getVariantSetDbIds().contains(variantSetDbId)) {
                    body.getVariantSetDbIds().add(variantSetDbId);
                    fGotVariantSetList = true;
                }
            }
        }

        MongoTemplate mongoTemplate = MongoTemplateManager.get(module);

    	// check permissions
    	Collection<Integer> projectIDs = fGotVariantSetList ? body.getVariantSetDbIds().stream().map(vsId -> Integer.parseInt(GigwaSearchVariantsRequest.getInfoFromId(vsId, 3)[1])).collect(Collectors.toSet()) :
        mongoTemplate.findDistinct(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList()))), "_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, VariantRunData.class, Integer.class);
    	List<Integer> forbiddenProjectIDs = new ArrayList<>();
        for (int pj : projectIDs) {
            if (!tokenManager.canUserReadProject(token, module, pj))
                forbiddenProjectIDs.add(pj);
        }
        projectIDs.removeAll(forbiddenProjectIDs);
        if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty())
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        List<Criteria> crits = new ArrayList<>();
        if (fGotVariantSetList) {
            List<Criteria> vsCrits = new ArrayList<>();
            for (String vsId : body.getVariantSetDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
                vsCrits.add(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])));
            }
            crits.add(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
        }
		
        if (fGotVariantList) {
            List<String> varIDs = body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList());
            crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(varIDs));
        }

        Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));

    	// now deal with samples
        if (fGotCallSetList) {	// project necessary fields to get only the required genotypes
            runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
            for (String callSetDbId : body.getCallSetDbIds()) {
                String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 2);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1]);
            }
        } else { // find out which samples are involved and keep track of corresponding individuals
            Query sampleQuery;
                if (fGotVariantSetList) {
                    List<Criteria> vsCrits = new ArrayList<>();
                    for (String vsId : body.getVariantSetDbIds()) {
                        String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
                        vsCrits.add(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(info[2])));
                    }
                    sampleQuery = new Query(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
                } else {
                    sampleQuery = new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(projectIDs));	// we only had a list of variants as input so all we can filter on is the list of projects thery are involved in
                }
            for (GenotypingSample gs : mongoTemplate.find(sampleQuery, GenotypingSample.class)) {
                sampleIndividuals.put(gs.getId(), gs.getIndividual());
            }
        }

        int page = 0;
        if (body.getPage() == null) {
            page = body.getPage();
        }

	int theoriticalPageSize = body.getPageSize() == null || body.getPageSize() > VariantsApi.MAX_CALL_MATRIX_SIZE ? VariantsApi.MAX_CALL_MATRIX_SIZE : body.getPageSize();
        int numberOfMarkersPerPage = (int) Math.ceil(1f * theoriticalPageSize / sampleIndividuals.size());
        Integer nTotalMarkerCount = fGotVariantList ? body.getVariantDbIds().size() : null;
        if (nTotalMarkerCount == null) {	// we don't have a definite variant list: see if we can guess it (only possible for single-run projects since there is no run index on VariantRunData)
            if (mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where("_id").in(projectIDs), Criteria.where(GenotypingProject.FIELDNAME_RUNS + ".1").exists(false))), GenotypingProject.class) == projectIDs.size())
                nTotalMarkerCount = (int) mongoTemplate.count(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).in(projectIDs)), VariantRunData.class);
        }
        
    	String unknownGtCode = body.getUnknownString() == null ? "-" : body.getUnknownString();
    	//String phasedSeparator = body.getSepPhased() == null ? "|" : URLDecoder.decode(body.getSepPhased(), "UTF-8");
    	String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
    	result.setSepUnphased(unPhasedSeparator);

        try {
            List<AbstractVariantData> varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, VariantRunData.class, runQuery, page * numberOfMarkersPerPage, numberOfMarkersPerPage);
            HashMap<Integer, String> previousPhasingIds = new HashMap<>();

            HashSet<String> distinctVariantIDs = new HashSet<>();
            for (AbstractVariantData v : varList) {
                VariantRunData vrd = (VariantRunData) v;
                distinctVariantIDs.add(v.getVariantId());
                for (Integer spId : vrd.getSampleGenotypes().keySet()) {
                    SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
                    String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
                    boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
                    previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/

                    String gtCode = sg.getCode();
                    if (gtCode == null || gtCode.length() == 0) {
                        gtCode = unknownGtCode;
                    }
//                    } else {
//						List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
//						if (!Boolean.TRUE.equals(body.isExpandHomozygotes()) && new HashSet<String>(alleles).size() == 1)
//							genotype = alleles.get(0);
//						else
//							genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
//                                }
                    Call call = new Call();
                    ListValue lv = new ListValue();
                    call.setGenotypeValue(gtCode);
                    call.setVariantDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getVariantId());
                    call.setVariantName(call.getVariantDbId());
                    call.setCallSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
                    //call.setCallSetName(call.getCallSetDbId());
                    call.setVariantSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getProjectId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getRunName());
                    for (String key : sg.getAdditionalInfo().keySet()) {
                        CallGenotypeMetadata gm = new CallGenotypeMetadata();
                        gm.setFieldAbbreviation(key);
                        gm.setDataType(CallGenotypeMetadata.DataTypeEnum.FLOAT); //TODO see what to put in datatype and fieldName
                        gm.setFieldName(key);
                        gm.setFieldValue(sg.getAdditionalInfo().get(key).toString());
                        call.addGenotypeMetadataItem(gm);
                    }
                    result.addDataItem(call);
                }
            }

            IndexPagination pagination = new IndexPagination();
            pagination.setCurrentPage(page);
            pagination.setPageSize(numberOfMarkersPerPage * sampleIndividuals.size());
            if (nTotalMarkerCount != null) {
                pagination.setTotalCount(nTotalMarkerCount);
                pagination.setTotalPages(varList.isEmpty() ? 0 : (int) Math.ceil((float) pagination.getTotalCount() / pagination.getPageSize()));
            }            
            metadata.setPagination(pagination);
            clr.setResult(result);
            return new ResponseEntity<CallsListResponse>(clr, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<CallsListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}