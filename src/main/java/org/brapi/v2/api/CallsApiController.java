package org.brapi.v2.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallGenotypeMetadata;
import org.brapi.v2.model.CallsListResponse;
import org.brapi.v2.model.CallsListResponseResult;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import fr.cirad.manager.IModuleManager;

import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import static fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData.GT_FIELD_PHASED_GT;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFHeaderVersion;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import jhi.brapi.api.Pagination;
import org.apache.commons.lang.StringUtils;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@CrossOrigin
@Controller
public class CallsApiController implements CallsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
    @Autowired MongoTemplateManager mongoTemplateManager;

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
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 2);
            GenotypingSample sample = MongoTemplateManager.get(info[0]).find(new Query(Criteria.where("_id").is(Integer.parseInt(info[1]))), GenotypingSample.class).iterator().next();
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
    public ResponseEntity<CallsListResponse> callsPut(String authorization, CallsListResponseResult body) {
        boolean updateDBInfo = false;
        String token = ServerinfoApiController.readToken(authorization);
        List<Call> callsToUpdate = body.getData();
        String unknownGT = body.getUnknownString() != null ? body.getUnknownString() : ".";
        String sepPhased = body.getSepPhased() != null ? body.getSepPhased()  : "/";
        String sepUnphased = body.getSepUnphased() != null ? body.getSepUnphased() : "|";
        
        CallsListResponse response = new CallsListResponse();
        Metadata metadata = new Metadata();
        response.setMetadata(metadata);
        
        String module = null;
        Set<String> variantSetDbIds = new HashSet();
        Set<String> variantIds = new HashSet();
        Set<Integer> callSetIds = new HashSet();
        for (Call c:callsToUpdate) {
            if (c.getVariantDbId() == null || c.getVariantSetDbId() == null || c.getCallSetDbId() == null) {
                Status status = new Status();
                status.setMessage("You must provide variantDbId, variantSetDbId and callSetDbId for each call");
                metadata.addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (module == null) {
                module = GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3)[0];
                variantIds.add(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantDbId(), 2)[1]);
                callSetIds.add(Integer.valueOf(GigwaSearchVariantsRequest.getInfoFromId(c.getCallSetDbId(), 2)[1]));    
                variantSetDbIds.add(c.getVariantSetDbId());
                
            } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3)[0])) {
                Status status = new Status();
                status.setMessage("You must specify VariantSets belonging to the same program / trial!");
                response.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(c.getCallSetDbId(), 2)[0])) {
                Status status = new Status();
                status.setMessage("You must specify CallSets belonging to the same program / trial!");
                response.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantDbId(), 2)[0])) {
                Status status = new Status();
                status.setMessage("You must specify Variants belonging to the same program / trial!");
                response.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        
        MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
        if (mongoTemplate == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        //check if callSetDbIds, variantDbIds, variantSetDbIds exist
        Query sQuery = new Query(Criteria.where("_id").in(callSetIds));
        List<GenotypingSample> samples = mongoTemplate.find(sQuery, GenotypingSample.class); 
        if (samples.size() < callSetIds.size()) { //at least one sample doesn't exist
            List<Integer> existingSampleIds = samples.stream().map(GenotypingSample::getId).collect(Collectors.toList());
            callSetIds.removeAll(existingSampleIds);
            Status status = new Status();
            status.setMessage("Those callSetDbIds don't exist: " + callSetIds.toString());
            response.getMetadata().addStatusItem(status);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } 
        
        Query vQuery = new Query(Criteria.where("_id").in(variantIds));
        List<VariantData> variants = mongoTemplate.find(vQuery, VariantData.class); 
        if (variants.size() < variantIds.size()) { //at least one sample doesn't exist
            List<String> existingVariantIds = variants.stream().map(VariantData::getId).collect(Collectors.toList());
            variantIds.removeAll(existingVariantIds);
            Status status = new Status();
            status.setMessage("Those variantDbIds don't exist: " + variantIds.toString());
            response.getMetadata().addStatusItem(status);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } 
        
        ArrayList<String> missingVariantSets = new ArrayList<>();
        for (String vsId:variantSetDbIds) {
            Query q = new Query(new Criteria().andOperator(
                    Criteria.where("_id." + VariantRunData.VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.valueOf(GigwaSearchVariantsRequest.getInfoFromId(vsId, 3)[1])),
                    Criteria.where("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_RUN).is(GigwaSearchVariantsRequest.getInfoFromId(vsId, 3)[2])
            ));
            List<VariantRunData> vrd = mongoTemplate.find(q, VariantRunData.class); 
            if (vrd == null || vrd.isEmpty()) {
                missingVariantSets.add(vsId);
            }
        }
        if (!missingVariantSets.isEmpty()) {
            Status status = new Status();
            status.setMessage("Those variantSetDbIds don't exist: " + missingVariantSets.toString());
            response.getMetadata().addStatusItem(status);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        
    	// check permissions
    	Collection<Integer> projectIDs = callsToUpdate.stream()
                .map(call -> Integer.parseInt(GigwaSearchVariantsRequest.getInfoFromId(call.getVariantSetDbId(), 3)[1]))
                .collect(Collectors.toSet());

        List<Integer> forbiddenProjectIDs = new ArrayList<>();
        for (int pj : projectIDs) {
            if (!tokenManager.canUserWriteToProject(token, module, pj))
                forbiddenProjectIDs.add(pj);
        }
        projectIDs.removeAll(forbiddenProjectIDs);
        
        if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        Query query = new Query(Criteria.where("_id").in(projectIDs));
        List<GenotypingProject> projects = mongoTemplate.find(query, GenotypingProject.class);        
        Map<Integer, Integer> ploidyLevels = projects.stream().collect(Collectors.toMap(GenotypingProject::getId, GenotypingProject::getPloidyLevel));
        
        //try retrieving metadata information from DBVCFHeader collection
        Document filter = new Document();
        filter.put("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, new Document("$in", projectIDs));                        
        MongoCollection<Document> vcfHeadersColl = mongoTemplate.getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));         

        MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).iterator();
        Map<Integer, Map<String, VCFFormatHeaderLine>> metadataMap = new HashMap<>();
        while (headerCursor.hasNext()) {
            DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
            int key = Objects.hash(Integer.toString(dbVcfHeader.getId().getProject()), dbVcfHeader.getId().getRun());
            if (metadataMap.get(key) == null) {
                //add a new DBVCFHeader
                metadataMap.put(key, new HashMap());
            }
            Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData(); 
            if (vcfMetadata != null) {
                metadataMap.get(key).putAll(vcfMetadata);
            }
        }        
        
        List<Call> updatedCalls = new ArrayList<>();
        
        for (Call c:callsToUpdate) {
            List<Criteria> crits = new ArrayList<>();

            String[] info = GigwaSearchVariantsRequest.getInfoFromId(c.getVariantSetDbId(), 3);
            int projectId = Integer.parseInt(info[1]);
            String runName = info[2];

            crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(projectId));
            crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(runName));
            crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).is(GigwaSearchVariantsRequest.getInfoFromId(c.getVariantDbId(), 2)[1]));                

            Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()]))); 
            
            //update DBVCFHeader if not in DB yet
            int hash = Objects.hash(Integer.toString(projectId), runName);
            if (!c.getGenotypeMetadata().isEmpty()) {
                boolean newVCFHeader = false;
                Map<String, VCFFormatHeaderLine> newVcfFormatHLines = new HashMap<>();
                for (CallGenotypeMetadata cgm:c.getGenotypeMetadata()) {                    
                    if (metadataMap.get(hash) == null) {                        
                        metadataMap.put(hash, new HashMap<>());
                        newVCFHeader = true;
                    }                    
                    if (metadataMap.get(hash).get(cgm.getFieldAbbreviation()) == null) {
                        VCFHeaderLineType type = null;
                        try {
                            String str = cgm.getDataType().toString();
                            type = VCFHeaderLineType.valueOf(str.substring(0, 1).toUpperCase() + str.substring(1));
                        } catch (Exception e) {
                            Status status = new Status();
                            status.setMessage("Wrong genotypeMetadata type: " + cgm.getDataType().toString());
                            response.getMetadata().addStatusItem(status);
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }
                        VCFFormatHeaderLine vcfHLine = new VCFFormatHeaderLine(cgm.getFieldName(), 1, type, "");
                        metadataMap.get(hash).put(cgm.getFieldAbbreviation(), vcfHLine);
                        newVcfFormatHLines.put(cgm.getFieldAbbreviation(), vcfHLine);
                    }                    
                }
                if (newVCFHeader) {
                    //insert new VCFHeader
                    DBVCFHeader vcfHeader = new DBVCFHeader();
                    vcfHeader.setId(new DBVCFHeader.VcfHeaderId(projectId, runName));
                    vcfHeader.setmFormatMetaData(newVcfFormatHLines);
                    mongoTemplate.insert(vcfHeader, mongoTemplate.getCollectionName(DBVCFHeader.class));

                } else if (!newVcfFormatHLines.isEmpty()) {
                    //update VCFHeader
                    Query q = new Query(new Criteria().andOperator(
                            Criteria.where("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT).is(projectId),
                            Criteria.where("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_RUN).is(runName)
                    ));
                    Update update = new Update();
                    for (String key:newVcfFormatHLines.keySet()) {
                        ObjectMapper oMapper = new ObjectMapper();
                        update.set(DBVCFHeader.FIELDNAME_FORMAT_METADATA + "." + key, newVcfFormatHLines.get(key));
                    }
                    UpdateResult result = mongoTemplate.updateFirst(q, update, DBVCFHeader.class, mongoTemplate.getCollectionName(DBVCFHeader.class));                        
                }
            }            
            
            //Check if variantRunData exists
            List<VariantRunData> vrd;
            try {
                vrd = mongoTemplate.find(runQuery, VariantRunData.class);
            }  catch (Exception e) {
                Status status = new Status();
                status.setMessage(e.getMessage());
                response.getMetadata().addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
            }
            
            if (vrd.isEmpty()) { //create variantRunData                
                List<VariantData> vd = mongoTemplate.find(runQuery, VariantData.class);
                if (vd.isEmpty()) { //variant doesn't exist
                    Status status = new Status();
                    status.setMessage("variantDbId " + c.getVariantDbId() + "doesn't exist" );
                    response.getMetadata().addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
                } else {
                    VariantRunData newVrd = convertCallToVrd(c, vd.get(0), projectId, runName);
                    mongoTemplate.insert(newVrd, mongoTemplate.getCollectionName(VariantRunData.class));
                    updateDBInfo = true;
                }
                
            } else { //update variantRunData
                runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
                String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(c.getCallSetDbId(), 2);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1]);
                
                Update update = new Update();
                
                if (c.getGenotypeValue() != null) { 
                    String gt = null;
                    if (c.getGenotypeValue().equals(unknownGT)) {
                        if (c.getAdditionalInfo().isEmpty() && c.getGenotypeMetadata().isEmpty()) {
                            update.unset(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1]); //remove the sp
                        } else {                        
                            update.unset(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.FIELDNAME_GENOTYPECODE); //remove gt field
                        }
                    } else {
                        String[] splitUnphased = c.getGenotypeValue().split(sepUnphased);
                        String[] splitPhased = c.getGenotypeValue().split(sepPhased);                        
                        if (splitUnphased.length > 1 && splitUnphased.length == ploidyLevels.get(projectId)) {
                            gt = String.join("/", splitUnphased);
                        } else if (splitPhased.length > 1 && splitPhased.length == ploidyLevels.get(projectId)) {
                            gt = String.join("/", splitPhased);
                            //add phGT and phID into ai
                            update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.SECTION_ADDITIONAL_INFO + "." + VariantData.GT_FIELD_PHASED_GT, String.join("|", splitPhased));
                            update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.SECTION_ADDITIONAL_INFO + "." + VariantData.GT_FIELD_PHASED_ID, splitCallSetDbId[1]);
                        } else if ((c.getGenotypeValue().equals("0") || c.getGenotypeValue().equals("1")) && !body.isExpandHomozygotes()){
                            gt = c.getGenotypeValue();
                        }       
                        
                    }
                    if (gt != null) {
                        if (gt.equals("1/0")) {
                            gt = "0/1";
                        }
                        update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.FIELDNAME_GENOTYPECODE, gt);
                    }
                }
                
                
                //add additionalInfo into variantRunData ai
                if (c.getAdditionalInfo() !=  null && !c.getAdditionalInfo().isEmpty()) {
                    for (String key:c.getAdditionalInfo().keySet()) {
                        update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.SECTION_ADDITIONAL_INFO + "." + key, c.getAdditionalInfo().get(key));
                    }
                }
                
                //add genotypeMetadata into variantRunData ai
                if (c.getGenotypeMetadata() != null) {
                    for (CallGenotypeMetadata gm:c.getGenotypeMetadata())
                        update.set(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[1] + "." + SampleGenotype.SECTION_ADDITIONAL_INFO + "." + gm.getFieldAbbreviation(), gm.getFieldValue());
                }
                
                try {
                    UpdateResult ur = mongoTemplate.updateFirst(runQuery, update, VariantRunData.class, mongoTemplate.getCollectionName(VariantRunData.class));
                    if (ur.getModifiedCount() > 0)
                        updateDBInfo = true;
                    if (ur.getMatchedCount() > 0)
                        updatedCalls.add(c);
                } catch(Exception e) {
                    Status status = new Status();
                    status.setMessage(e.getMessage());
                    response.getMetadata().addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
                }
            }
        }
        CallsListResponseResult result = new CallsListResponseResult(); //TODO retrieve information of sepPhased, unknownString...
        
        if (updateDBInfo) {
            MongoTemplateManager.updateDatabaseLastModification(module);
        }
        
        //Metadata part
        Status status = new Status();
        status.setMessage("Calls updated");
        status.setMessageType(Status.MessageTypeEnum.INFO);
        metadata.addStatusItem(status);
        metadata.setPagination(new IndexPagination());
        metadata.getPagination().setCurrentPage(0);
        metadata.getPagination().setTotalCount(updatedCalls.size());
        int pageSize = 1000;
        metadata.getPagination().setPageSize(pageSize);
        int totalPages = updatedCalls.size() % pageSize > 0 ? updatedCalls.size() / pageSize + 1 : updatedCalls.size() / pageSize;
        metadata.getPagination().setTotalPages(totalPages);        
        
        result.setData(updatedCalls);
        response.setResult(result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CallsListResponse> searchCallsPost(String authorization, CallsSearchRequest body) {
        if (body.isExpandHomozygotes() == null) {
            body.setExpandHomozygotes(Boolean.FALSE);
        }
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
                    status.setMessage("You must specify VariantSets belonging to the same program / trial!");
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
                    status.setMessage("You may specify VariantSets / Variants only belonging to the same program / trial!");
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
                    status.setMessage("You may specify VariantSets / Variants / CallSets only belonging to the same program / trial!");
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
        int numberOfSamples = 0;
        if (fGotCallSetList) {	// project necessary fields to get only the required genotypes
            runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
            numberOfSamples = body.getCallSetDbIds().size();
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
            numberOfSamples = sampleIndividuals.size();
        }

        int page = body.getPage() == null ? 1000 : body.getPage();        
        int pageSize = body.getPageSize() == null ? 1000 : body.getPageSize();
        
	//int theoriticalPageSize = body.getPageSize() == null || body.getPageSize() > VariantsApi.MAX_CALL_MATRIX_SIZE ? VariantsApi.MAX_CALL_MATRIX_SIZE : body.getPageSize();
        int numberOfMarkersPerPage = (int) Math.ceil(1f * pageSize / numberOfSamples);
        Integer nTotalMarkerCount = fGotVariantList ? body.getVariantDbIds().size() : null;
        if (nTotalMarkerCount == null) {	// we don't have a definite variant list: see if we can guess it (only possible for single-run projects since there is no run index on VariantRunData)
            if (mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where("_id").in(projectIDs), Criteria.where(GenotypingProject.FIELDNAME_RUNS + ".1").exists(false))), GenotypingProject.class) == projectIDs.size())
                nTotalMarkerCount = (int) mongoTemplate.count(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).in(projectIDs)), VariantRunData.class);
        }
        
    	String unknownGtCode = body.getUnknownString() == null ? "." : body.getUnknownString();
    	String phasedSeparator = body.getSepPhased() == null ? "|" : body.getSepPhased();
    	String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
    	result.setSepUnphased(unPhasedSeparator);

        try {
            List<AbstractVariantData> varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, VariantRunData.class, runQuery, page * numberOfMarkersPerPage, numberOfMarkersPerPage);
            HashMap<Integer, String> previousPhasingIds = new HashMap<>();
            
            //try retrieving metadata information from DBVCFHeader collection
            Document filter = new Document();
            if (fGotVariantSetList) {
                List<Document> filtersList = new ArrayList<>();
                for (String vsId : body.getVariantSetDbIds()) {
                    String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
                    Document ifilter = new Document("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, Integer.parseInt(info[1]));
                    ifilter.append("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_RUN, info[2]);
                    filtersList.add(ifilter);
                }
                filter.put("$or", filtersList);
                
            } else {
                filter.put("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, new Document("$in", projectIDs)); // we only had a list of variants as input so all we can filter on is the list of projects thery are involved in
            }
                        
            MongoCollection<Document> vcfHeadersColl = mongoTemplate.getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));         
            
            MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).iterator();
            Map<String, VCFFormatHeaderLine> metadataMap = new HashMap<>();
            while (headerCursor.hasNext()) {
                DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
                Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData(); 
                if (vcfMetadata != null) {
                    metadataMap.putAll(vcfMetadata);
                }
            }            
            

            HashSet<String> distinctVariantIDs = new HashSet<>();
            for (AbstractVariantData v : varList) {
                VariantRunData vrd = (VariantRunData) v;
                distinctVariantIDs.add(v.getVariantId());
                for (Integer spId : vrd.getSampleGenotypes().keySet()) {
                    SampleGenotype sg = vrd.getSampleGenotypes().get(spId);                    
                    String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
                    previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);
                    boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
                    	
                    String gtCode = sg.getCode();
                    String genotype;
                    if (gtCode == null || gtCode.length() == 0) {
                        genotype = unknownGtCode;                    
                    } else {
                        List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
                        String sep = "/";
                        if (!body.isExpandHomozygotes() && new HashSet<String>(alleles).size() == 1) {
                            //genotype = alleles.get(0);                            
                            genotype = gtCode.split(sep)[0];
                        } else {
                            //genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
                            genotype = gtCode.replace(sep, fPhased ? phasedSeparator : unPhasedSeparator);
                        }
                    }
                    Call call = new Call();
                    call.setGenotypeValue(genotype);
                    call.setVariantDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getVariantId());
                    call.setVariantName(call.getVariantDbId());
                    call.setCallSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
                    //call.setCallSetName(call.getCallSetDbId());
                    call.setVariantSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getProjectId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getRunName());
                    for (String key : sg.getAdditionalInfo().keySet()) {
                        if (!key.equals(VariantData.GT_FIELD_PHASED_ID) && !key.equals(VariantData.GT_FIELD_PHASED_GT)) {
                            CallGenotypeMetadata gm = new CallGenotypeMetadata();
                            gm.setFieldAbbreviation(key);
                            if (metadataMap.get(key) != null) {
                                try {
                                    gm.setDataType(CallGenotypeMetadata.DataTypeEnum.fromValue(metadataMap.get(key).getType().toString().toLowerCase()));
                                    gm.setFieldName(metadataMap.get(key).getDescription());
                                } catch (Exception e) {                                
                                }
                            }
                            gm.setFieldValue(sg.getAdditionalInfo().get(key).toString());
                            call.addGenotypeMetadataItem(gm);
                        }
                    }
                    result.addDataItem(call);
                }
            }

            IndexPagination pagination = new IndexPagination();
            pagination.setCurrentPage(page);
            pagination.setPageSize(pageSize);
            if (nTotalMarkerCount != null) {
                pagination.setTotalCount(nTotalMarkerCount*numberOfSamples);
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

    private VariantRunData convertCallToVrd(Call c, VariantData vd, int projectId, String runName) {
        VariantRunData vrd = new VariantRunData(new VariantRunDataId(projectId, runName, vd.getId()));
        vrd.setAdditionalInfo((HashMap<String, Object>) (Map) c.getAdditionalInfo());
        vrd.setReferencePosition(vd.getReferencePosition());
        vrd.setKnownAlleles(vd.getKnownAlleles());
        HashMap<Integer, SampleGenotype> genotypes = new HashMap();
        SampleGenotype sg = new SampleGenotype();
        String gt = c.getGenotypeValue().equals("1/0") ? "0/1" : c.getGenotypeValue();
        sg.setCode(gt);
        String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(c.getCallSetDbId(), 2);
        genotypes.put(Integer.valueOf(splitCallSetDbId[1]), sg);
        vrd.setSampleGenotypes(genotypes);
        return vrd;
    }

}