package org.brapi.v2.api;

import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_END_SITE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_SEQUENCE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_START_SITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.brapi.v2.api.cache.MongoBrapiCache;
import org.brapi.v2.model.AlleleMatrix;
import org.brapi.v2.model.AlleleMatrixDataMatrices;
import org.brapi.v2.model.AlleleMatrixDataMatrices.DataTypeEnum;
import org.brapi.v2.model.AlleleMatrixPagination;
import org.brapi.v2.model.AlleleMatrixResponse;
import org.brapi.v2.model.AlleleMatrixSearchRequest;
import org.brapi.v2.model.AlleleMatrixSearchRequestPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.CountOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;

@CrossOrigin
@Controller
public class AllelematrixApiController implements AllelematrixApi {

    private static final Logger log = LoggerFactory.getLogger(AllelematrixApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
    @Autowired private MongoBrapiCache brapiCache;

    @Override
    public ResponseEntity<AlleleMatrixResponse> allelematrixGet(Integer dimensionVariantPage, Integer dimensionVariantPageSize, Integer dimensionCallSetPage, Integer dimensionCallSetPageSize,
            Boolean preview, String dataMatrixAbbreviations, String positionRange, String germplasmDbId, String germplasmName, String germplasmPUI, String callSetDbId,
            String variantDbId, String variantSetDbId, Boolean expandHomozygotes, String unknownString,String sepPhased, String sepUnphased, String authorization) throws InterruptedException {
        
        if (variantSetDbId == null && callSetDbId != null) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 2);
            GenotypingSample sample = MongoTemplateManager.get(info[0]).find(new Query(Criteria.where("_id").is(Integer.parseInt(info[1]))), GenotypingSample.class).iterator().next();
            variantSetDbId = info[0] + IGigwaService.ID_SEPARATOR + sample.getProjectId() + IGigwaService.ID_SEPARATOR + sample.getRun();
        }
		
        AlleleMatrixSearchRequest request = new AlleleMatrixSearchRequest();
        AlleleMatrixSearchRequestPagination csPagination = new AlleleMatrixSearchRequestPagination();
        csPagination.setDimension(AlleleMatrixSearchRequestPagination.DimensionEnum.CALLSETS);
        if (dimensionCallSetPage != null) { 
            csPagination.setPage(dimensionCallSetPage);
        }
        if (dimensionCallSetPageSize != null) {
            csPagination.setPageSize(dimensionCallSetPageSize);
        }
        request.getPagination().add(csPagination);
        
        AlleleMatrixSearchRequestPagination vPagination = new AlleleMatrixSearchRequestPagination();
        vPagination.setDimension(AlleleMatrixSearchRequestPagination.DimensionEnum.VARIANTS);
        if (dimensionVariantPage != null) {
            vPagination.setPage(dimensionVariantPage);
        }
        if (dimensionVariantPageSize != null) {
            vPagination.setPageSize(dimensionVariantPageSize);
        }
        request.getPagination().add(vPagination);

        request.setExpandHomozygotes(expandHomozygotes);
        request.setUnknownString(unknownString);
        request.setSepUnphased(sepUnphased);
        request.setSepPhased(sepPhased);
        request.setPreview(preview);

        if (callSetDbId != null) {
            request.setCallSetDbIds(Arrays.asList(callSetDbId));
        }
        if (variantDbId != null) {
            request.setVariantDbIds(Arrays.asList(variantDbId));
        }
        if (variantSetDbId != null) {
            request.setVariantSetDbIds(Arrays.asList(variantSetDbId));
        }
//        if (dataMatrixNames != null) {
//            request.setDataMatrixNames(Arrays.asList(dataMatrixNames.split(",")));
//        }
        if (dataMatrixAbbreviations != null) {
            request.setDataMatrixAbbreviations(Arrays.asList(dataMatrixAbbreviations.split(",")));
        }        
        if (positionRange != null) {
            request.setPositionRanges(Arrays.asList(positionRange));
        }
        if (germplasmDbId != null) {
            request.setGermplasmDbIds(Arrays.asList(germplasmDbId));
        }
        if (germplasmName != null) {
            request.setGermplasmNames(Arrays.asList(germplasmName));
        }
        if (germplasmPUI != null) {
            request.setGermplasmPUIs(Arrays.asList(germplasmPUI));
        }

        return searchAllelematrixPost(authorization, request);
    }

    @Override
    public ResponseEntity<AlleleMatrixResponse> searchAllelematrixPost(String authorization, AlleleMatrixSearchRequest body) throws InterruptedException {
        String token = ServerinfoApiController.readToken(authorization);
        
        AlleleMatrixResponse response = new AlleleMatrixResponse();        
        AlleleMatrix result = new AlleleMatrix();
        Metadata metadata = new Metadata();
        response.setResult(result);
        response.setMetadata(metadata);
        
        int numberOfCallSetsPerPage = 1000;
        int callSetsPage = 0;
        int numberOfMarkersPerPage = 1000;
        int variantsPage = 0;

        if (body.getPagination() != null) {
            for (AlleleMatrixSearchRequestPagination pagination:body.getPagination()) {
            	if (pagination.getDimension() == null)  {
                    Status status = new Status();
                    status.setMessage("Invalid pagination dimension specified, only 'VARIANTS' and 'CALLSETS' are accepeted!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                if (pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.CALLSETS) {
                    numberOfCallSetsPerPage = pagination.getPageSize();
                    callSetsPage = pagination.getPage();
                } else if (pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.VARIANTS) {
                    numberOfMarkersPerPage = pagination.getPageSize();
                    variantsPage = pagination.getPage();
                }
            }
        }
        
        String unknownGtCode = body.getUnknownString() == null ? "." : body.getUnknownString();
        result.setUnknownString(unknownGtCode);
    	String phasedSeparator = body.getSepPhased() == null ? "|" : body.getSepPhased();
        result.setSepPhased(phasedSeparator);
    	String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
    	result.setSepUnphased(unPhasedSeparator);
        
        boolean fGotVariantSetList = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
        boolean fGotVariantList = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
        //boolean fGotCallSetList = body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty();
        boolean fGotSampleFilter = ((body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty())
                || (body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty())
                || (body.getGermplasmPUIs() != null && !body.getGermplasmPUIs().isEmpty())
                || (body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty())
                || (body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty())
                );
        
        if (!fGotVariantSetList && !fGotVariantList && !fGotSampleFilter) {
            Status status = new Status();
            status.setMessage("You must specify at least one of those filters : callSetDbId, sampleDbId, germplasmDbId, variantDbId, or variantSetDbId!");
            metadata.addStatusItem(status);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
                    response.setMetadata(metadata);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
                    response.setMetadata(metadata);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        List<String> germplasmIds = null;
        if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
            germplasmIds = new ArrayList<>();
            try {
                Map<String, Collection<String>> gMap = GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds());
                if (gMap.size() > 1) {
                    Status status = new Status();
                    status.setMessage("You can't specify germplasm ids from different programs");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                if (module == null) {
                    module = gMap.keySet().iterator().next(); //get first element of 
                } else if (!module.equals(gMap.keySet().iterator().next())) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants / CallSets / Germplasm only belonging to the same program / trial!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                germplasmIds.addAll(gMap.get(module));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(AllelematrixApiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty()) {
            if (module == null) {
                Status status = new Status();
                status.setMessage("When using germplasmName filter, you have to specify at least a variantSetDbId or a variantDbId");
                metadata.addStatusItem(status);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            } else {
                Query query = new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()));
                List<String> germplasmIdsByNames = MongoTemplateManager.get(module).findDistinct(query, GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
                if (germplasmIds == null) {
                    germplasmIds = germplasmIdsByNames;
                } else {
                    germplasmIds.retainAll(germplasmIdsByNames);
                }
            }
        }
        
        if (germplasmIds != null && germplasmIds.isEmpty()) { //if no germplasm were found based on germplasmDbId and/or germplasmName, no data to return
            return returnEmptyMatrix(response, variantsPage, variantsPage, callSetsPage, callSetsPage);
        }
        
        List<Integer> sampleIDs = null;
        if (germplasmIds != null) {
            sampleIDs = new ArrayList<>();
            for (GenotypingSample s : MongoTemplateManager.get(module).find(new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmIds)), GenotypingSample.class)) {
            	sampleIDs.add(s.getId());
            }
        }
    	
        if (body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty()) {
            List<Integer> callSetIds = new ArrayList<>();
            for (String callSetDbId : body.getCallSetDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 2);
                if (module == null)
                    module = info[0];
                else if (!module.equals(info[0])) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants / CallSets only belonging to the same program / trial!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                callSetIds.add(Integer.parseInt(info[1]));
            }
            if (sampleIDs ==  null) {
                sampleIDs = callSetIds;
            } else {
                sampleIDs.retainAll(callSetIds);
            }
        }
        
        if (body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty()) {
            List<Integer> givenSampleIds = new ArrayList<>();
            for (String sampleDbId : body.getSampleDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(sampleDbId, 2);
                if (module == null)
                    module = info[0];
                else if (!module.equals(info[0])) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants / CallSets / sampleDbIds only belonging to the same program / trial!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                givenSampleIds.add(Integer.parseInt(info[1]));
            }
            if (sampleIDs ==  null) {
                sampleIDs = givenSampleIds;
            } else {
                sampleIDs.retainAll(givenSampleIds);
            }
        }
        
        if (fGotSampleFilter && sampleIDs.isEmpty()) {
            return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, callSetsPage, numberOfCallSetsPerPage); //intersection of sample, callset, germplasm filters returned no sampleDbId
        }

        if (sampleIDs != null && !sampleIDs.isEmpty()) { // identify the runs those samples are involved in, update run list if necessary
            List<GenotypingSample> samples = MongoTemplateManager.get(module).find(new Query(Criteria.where("_id").in(sampleIDs)), GenotypingSample.class);
            if (samples.isEmpty()) { //return empty dataMatrices
                return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, callSetsPage, numberOfCallSetsPerPage); //if no samples were found based on germplasm or sample or callset id, no data to return
            } else {
                Map<String, List<Integer>> variantSetSamples = new HashMap();
                for (GenotypingSample sp : MongoTemplateManager.get(module).find(new Query(Criteria.where("_id").in(sampleIDs)), GenotypingSample.class)) {                    
                    String variantSetDbId = module + IGigwaService.ID_SEPARATOR + sp.getProjectId() + IGigwaService.ID_SEPARATOR + sp.getRun();
                    if (variantSetSamples.get(variantSetDbId) == null) {
                        variantSetSamples.put(variantSetDbId, new ArrayList<>());
                        variantSetSamples.get(variantSetDbId).add(sp.getId());
                    } else {
//                        List<Integer> s = variantSetSamples.get(variantSetDbId);
//                        s.add(sp.getId());
                        variantSetSamples.get(variantSetDbId).add(sp.getId());
                    }
                }
                if (body.getVariantSetDbIds() !=  null && !body.getVariantSetDbIds().isEmpty()) { //we keep only sampleIDs corresponding to the given variantSets
                    sampleIDs = new ArrayList<>();
                    for (String vs:variantSetSamples.keySet()) {
                        if (body.getVariantSetDbIds().contains(vs)) {
                            sampleIDs.addAll(variantSetSamples.get(vs));
                        }
                    }
                } else {
                    body.setVariantSetDbIds(new ArrayList<>(variantSetSamples.keySet()));
                    fGotVariantSetList = true;
                }
            }           
            if (sampleIDs.isEmpty()) { //return empty dataMatrices
                return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, callSetsPage, numberOfCallSetsPerPage); //if no samples were found based on germplasm or sample or callset id, no data to return
            }
        }        

        MongoTemplate mongoTemplate = MongoTemplateManager.get(module);

    	// check permissions
    	Collection<Integer> projectIDs = fGotVariantSetList ?
    		body.getVariantSetDbIds().stream().map(vsId -> Integer.parseInt(GigwaSearchVariantsRequest.getInfoFromId(vsId, 3)[1])).collect(Collectors.toSet()) :
    		mongoTemplate.findDistinct(new Query(Criteria.where("_id." + VariantRunData.VariantRunDataId.FIELDNAME_VARIANT_ID).in(body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList()))), "_id." + VariantRunData.VariantRunDataId.FIELDNAME_PROJECT_ID, VariantRunData.class, Integer.class);
    	List<Integer> forbiddenProjectIDs = new ArrayList<>();
        for (int pj : projectIDs)
            if (!tokenManager.canUserReadProject(token, module, pj))
            	forbiddenProjectIDs.add(pj);
        projectIDs.removeAll(forbiddenProjectIDs);
        if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty())
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        List<Criteria> crits = new ArrayList<>();
        if (fGotVariantSetList) {
            List<Criteria> vsCrits = new ArrayList<>();
            for (String vsId : body.getVariantSetDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
                vsCrits.add(new Criteria().andOperator(Criteria.where("_id." + VariantRunData.VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])), Criteria.where("_id." + VariantRunData.VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])));
            }
            crits.add(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
        }

        if (fGotVariantList) {
            List<String> varIDs = body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(IGigwaService.ID_SEPARATOR))).collect(Collectors.toList());
            crits.add(Criteria.where("_id." + VariantRunData.VariantRunDataId.FIELDNAME_VARIANT_ID).in(varIDs));
        }
        
        if (body.getPositionRanges() != null) {
            List<Criteria> vsCrits = new ArrayList<>();
            for (String positionRange:body.getPositionRanges()) {      
                try {                   
                	positionRange = positionRange.replaceAll("\\s+", "");
                    if (positionRange.contains(":") && !positionRange.endsWith(":")) { //filtering on sequence and position  
                        String[] pr = positionRange.split(":");
                        String chr = pr[0];
                        String[] range = pr[1].split("-");
                        if (pr[1].endsWith("-")) { //filtering on start
                            int start = Integer.parseInt(range[0]);
                            vsCrits.add(new Criteria().andOperator(
                                Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(chr),
                                Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).gte(start)
                            ));
                        } else if(pr[1].startsWith("-")){ //filtering on end
                            int end = Integer.parseInt(range[1]);
                            vsCrits.add(new Criteria().andOperator(
                                    Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(chr),
                                    Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).lte(end)
                            ));
                        } else { //filtering on start and end
                            int start = Integer.parseInt(range[0]), end = Integer.parseInt(range[range.length - 1]);
                            vsCrits.add(new Criteria().andOperator(
                                Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(chr),
                                Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).gte(start), 
                                Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).lte(end)
                            ));                        
                        }
                        //TODO take into account variant endsite

                    } else { //filtering only on sequence
                        if (positionRange.endsWith(":")) { 
                            positionRange = StringUtils.chop(positionRange);
                        }
                        vsCrits.add(Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(positionRange));
                    }
                } catch (Exception e) {
                    Status status = new Status();
                    status.setMessage("Can't read positionRange: " + positionRange);
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                
            }
            crits.add(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
        }

        Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));

    	// now deal with samples
        int nTotalSamplesCount = 0;
        if (sampleIDs != null && !sampleIDs.isEmpty()) {	// project necessary fields to get only the required genotypes
            runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
            nTotalSamplesCount = sampleIDs.size();
            if (callSetsPage * numberOfCallSetsPerPage >= sampleIDs.size()) {
                sampleIDs = new ArrayList();
            } else {
                Integer endOfList = (callSetsPage + 1) * numberOfCallSetsPerPage >= sampleIDs.size() ? sampleIDs.size() : (callSetsPage + 1) * numberOfCallSetsPerPage;
                sampleIDs = sampleIDs.subList(callSetsPage * numberOfCallSetsPerPage, endOfList);
            }            
            for (Integer s : sampleIDs) {
                //String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + s);
            }            
            
        } else {	// find out which samples are involved and keep track of corresponding individuals
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
            //count samples
            nTotalSamplesCount = (int) mongoTemplate.count(sampleQuery, GenotypingSample.class);  
            sampleIDs = new ArrayList<>();
            for (GenotypingSample gs : mongoTemplate.find(sampleQuery.skip(callSetsPage * numberOfCallSetsPerPage).limit(numberOfCallSetsPerPage), GenotypingSample.class)) {
            	sampleIDs.add(gs.getId());
            }
        }
        List<String> callSetIds = new ArrayList<>();
        for (Integer spId : sampleIDs)
            callSetIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);    	
        
        //count variants
        AtomicInteger nTotalMarkerCount = new AtomicInteger(-1);
        final boolean finalGotVariantSetList = fGotVariantSetList;
        Thread countThread = new Thread() {	// count asynchronously for faster response
        	public void run() {
                MatchOperation match = Aggregation.match(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));
                GroupOperation group = Aggregation.group("_id", "$_id." + VariantRunData.VariantRunDataId.FIELDNAME_VARIANT_ID);
                if (finalGotVariantSetList && crits.size() == 1) {	// we need the overall variant counts in a list of VariantSets: check cache before counting
                    int n = 0;            
                	try {
                		for (String variantSetDbId : body.getVariantSetDbIds())
                			n += brapiCache.getVariantSet(mongoTemplate, variantSetDbId).getVariantCount();
                		nTotalMarkerCount.set(n);
        			} catch (Exception e) {}
                }
                if (nTotalMarkerCount.get() == -1) {
        	        Aggregation aggregation = Aggregation.newAggregation(match, group, new CountOperation("countResult")).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        	        AggregationResults<Document> countVar = mongoTemplate.aggregate(aggregation, VariantRunData.class, Document.class);
                    nTotalMarkerCount.set(countVar.getUniqueMappedResult() == null ? 0 : countVar.getUniqueMappedResult().getInteger("countResult"));
                }
        	}
        };
        countThread.start();

        AlleleMatrixPagination callSetPagination = new AlleleMatrixPagination();
        callSetPagination.setDimension(AlleleMatrixPagination.DimensionEnum.CALLSETS);
        callSetPagination.setPage(callSetsPage);
        callSetPagination.setPageSize(numberOfCallSetsPerPage);
        callSetPagination.setTotalCount(nTotalSamplesCount);
        int nbOfCallSetPages =  nTotalSamplesCount / numberOfCallSetsPerPage;
        if (nTotalSamplesCount % numberOfCallSetsPerPage > 0) {
            nbOfCallSetPages++;
        }
        callSetPagination.setTotalPages(nbOfCallSetPages);            
        
        if (body.isPreview())
            return new ResponseEntity<>(response, HttpStatus.OK); //don't return dataMatrices

        try {            
            List<AbstractVariantData> varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, VariantRunData.class, runQuery, variantsPage * numberOfMarkersPerPage, numberOfMarkersPerPage);
            
            Map<String, AlleleMatrixDataMatrices> matricesMap = new HashMap<>();
            
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
            Document fields = new Document();
            boolean fReturnGenotypes = false;
            if (body.getDataMatrixAbbreviations() != null && !body.getDataMatrixAbbreviations().isEmpty()) {
                for (String key:body.getDataMatrixAbbreviations()) {
                    fields.put(DBVCFHeader.FIELDNAME_FORMAT_METADATA + "." + key, 1);
                    if ("GT".equals(key))
                    	fReturnGenotypes = true;
                }
            }             
            
            MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).projection(fields).iterator();
            while (headerCursor.hasNext()) {
                DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
                Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData();
                if (vcfMetadata != null) {
                    for (String key:vcfMetadata.keySet())
                    	if (!"GT".equals(key)) {
//                        if (body.getDataMatrixNames() != null && !body.getDataMatrixNames().isEmpty()) {
//                            if (vcfMetadata.get(key).getDescription() != null) {
//                                if (body.getDataMatrixNames().contains(vcfMetadata.get(key).getDescription())) {
//                                    AlleleMatrixDataMatrices matrix = new AlleleMatrixDataMatrices();
//                                    matrix.setDataMatrix(new ArrayList<>());
//                                    matrix.setDataMatrixAbbreviation(vcfMetadata.get(key).getID());
//                                    matrix.setDataMatrixName(vcfMetadata.get(key).getDescription());
//                                    VCFHeaderLineType type = vcfMetadata.get(key).getType();
//                                    DataTypeEnum brapiType = DataTypeEnum.fromValue(type.toString().toLowerCase());
//                                    matrix.setDataType(brapiType);
//                                    if (matricesMap.get(key) == null) {
//                                        matricesMap.put(key, matrix);
//                                    }
//                                }
//                            }
//                        } else { 
                            AlleleMatrixDataMatrices matrix = new AlleleMatrixDataMatrices();
                            matrix.setDataMatrix(new ArrayList<>());
                            matrix.setDataMatrixAbbreviation(vcfMetadata.get(key).getID());
                            matrix.setDataMatrixName(vcfMetadata.get(key).getDescription());
                            VCFHeaderLineType type = vcfMetadata.get(key).getType();
                            DataTypeEnum brapiType = DataTypeEnum.fromValue(type.toString().toLowerCase());
                            matrix.setDataType(brapiType);
                            if (matricesMap.get(key) == null)
                                matricesMap.put(key, matrix);                                          
//                        }
                    }
                }
            }
            
            if (fReturnGenotypes) {
                //add GT matrix in the case of data with no VCFheader metadata
                AlleleMatrixDataMatrices gtMmatrix = new AlleleMatrixDataMatrices();
                gtMmatrix.setDataMatrix(new ArrayList<>());
                gtMmatrix.setDataMatrixAbbreviation("GT");
                gtMmatrix.setDataMatrixName("Genotype");
                gtMmatrix.setDataType(DataTypeEnum.STRING);
                matricesMap.put("GT", gtMmatrix);
            }
            
            HashMap<Integer, String> previousPhasingIds = new HashMap<>();
            List<String> variantIds = new ArrayList<>();            
            Set<String> variantSetDbIds = new HashSet<>();
 
            for (AbstractVariantData v : varList) {
                VariantRunData vrd = (VariantRunData) v;
                
                variantSetDbIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + Integer.toString(vrd.getId().getProjectId()) + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getRunName());
                variantIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getVariantId());

                Map<String, List<String>> dataMap= new HashMap<>(); //key="GT", value=List<GT> list of genotypes of these variant
                                                                    //key="DP", value=List<DP> list of DP of these variant
                for (String key:matricesMap.keySet())
                    dataMap.put(key, new ArrayList<>());

                for (Integer spId : sampleIDs) {
                    SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
                    if (sg != null) {
                        String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
                        boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
                        previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/                        

                        Map<String, Object> ai = sg.getAdditionalInfo();
                        for (String key:matricesMap.keySet()) {
                            if (!key.equals("GT")) {  //adding additionalInfo
                                if (ai.get(key) != null) {
                                    dataMap.get(key).add(ai.get(key).toString());
                                } else {
                                    dataMap.get(key).add(unknownGtCode);
                                }
                            } else {  //adding genotypes
                                String gtCode = sg.getCode();
                                if (gtCode == null || gtCode.length() == 0) {
                                    dataMap.get(key).add(unknownGtCode);
                                } else {
                                    List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
                                    String sep = "/";
                                    if (!Boolean.TRUE.equals(body.isExpandHomozygotes()) && new HashSet<String>(alleles).size() == 1) {
                                        dataMap.get(key).add(gtCode.split(sep)[0]);
                                    } else {
                                        dataMap.get(key).add(gtCode.replace(sep, fPhased ? phasedSeparator : unPhasedSeparator));
                                    }
                                }
                            }
                        }                        
                    } else {
                        for (String key:matricesMap.keySet()) {
                            dataMap.get(key).add(unknownGtCode);
                        }
                    }
                }

                //Filling metadataMatrices with data (additionalInfo of VariantRunData will be displayed only if the key has been described in DBVCFheader)
                for (String key:matricesMap.keySet()) {
                    matricesMap.get(key).getDataMatrix().add(dataMap.get(key));
                }
            }
                
            result.setCallSetDbIds(callSetIds);
            result.setVariantDbIds(variantIds);
            result.setVariantSetDbIds(new ArrayList<>(variantSetDbIds));            
            result.setDataMatrices(new ArrayList<>(matricesMap.values())); //convert Map to List
            
            countThread.join();
            AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
            variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
            variantPagination.setPage(variantsPage);
            variantPagination.setPageSize(numberOfMarkersPerPage);
            variantPagination.setTotalCount(nTotalMarkerCount.get());
            int nbOfPages = nTotalMarkerCount.get() / numberOfMarkersPerPage;
            if (nTotalMarkerCount.get() % numberOfMarkersPerPage > 0)
                nbOfPages++;
            variantPagination.setTotalPages(nbOfPages);
            result.setPagination(Arrays.asList(variantPagination, callSetPagination));

            return new ResponseEntity<AlleleMatrixResponse>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<AlleleMatrixResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    public ResponseEntity<AlleleMatrixResponse> returnEmptyMatrix(AlleleMatrixResponse response, int variantsPage, int variantsPageSize, int callSetsPage, int callSetsPageSize) {
        AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
        variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
        variantPagination.setPage(variantsPage);
        variantPagination.setPageSize(variantsPageSize);
        variantPagination.setTotalCount(0);
        variantPagination.setTotalPages(0);

        AlleleMatrixPagination callSetPagination = new AlleleMatrixPagination();
        callSetPagination.setDimension(AlleleMatrixPagination.DimensionEnum.CALLSETS);
        callSetPagination.setPage(callSetsPage);
        callSetPagination.setPageSize(callSetsPageSize);
        callSetPagination.setTotalCount(0);
        callSetPagination.setTotalPages(0);

        response.getResult().setPagination(Arrays.asList(variantPagination, callSetPagination));
        response.getResult().setCallSetDbIds(new ArrayList<>());
        response.getResult().setVariantDbIds(new ArrayList<>());
        response.getResult().setVariantSetDbIds(new ArrayList<>());
        return new ResponseEntity<AlleleMatrixResponse>(response, HttpStatus.OK);
    }

}