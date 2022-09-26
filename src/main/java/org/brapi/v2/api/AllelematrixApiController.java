package org.brapi.v2.api;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_END_SITE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_SEQUENCE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_START_SITE;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.brapi.v2.model.AlleleMatrix;
import org.brapi.v2.model.AlleleMatrixDataMatrices;
import org.brapi.v2.model.AlleleMatrixDataMatrices.DataTypeEnum;
import org.brapi.v2.model.AlleleMatrixPagination;
import org.brapi.v2.model.AlleleMatrixResponse;
import org.brapi.v2.model.AlleleMatrixSearchRequest;
import org.brapi.v2.model.AlleleMatrixSearchRequestPagination;
import org.brapi.v2.model.CallGenotypeMetadata;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Controller
public class AllelematrixApiController implements AllelematrixApi {

    private static final Logger log = LoggerFactory.getLogger(AllelematrixApiController.class);

    @Autowired AbstractTokenManager tokenManager;

    @Override
    public ResponseEntity<AlleleMatrixResponse> allelematrixGet(Integer dimensionVariantPage, Integer dimensionVariantPageSize, Integer dimensionCallSetPage, Integer dimensionCallSetPageSize,
            Boolean preview, String dataMatrixNames, String dataMatrixAbbreviations, String positionRange, String germplasmDbId, String germplasmName, String germplasmPUI, String callSetDbId,
            String variantDbId, String variantSetDbId, Boolean expandHomozygotes, String unknownString,String sepPhased, String sepUnphased, String authorization) {
        
        if (variantSetDbId == null && callSetDbId != null) {
            String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
            GenotypingSample sample = MongoTemplateManager.get(info[0]).find(new Query(Criteria.where("_id").is(Integer.parseInt(info[2]))), GenotypingSample.class).iterator().next();
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
        if (dataMatrixNames != null) {
            request.setDataMatrixNames(Arrays.asList(dataMatrixNames.split(",")));
        }
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
    public ResponseEntity<AlleleMatrixResponse> searchAllelematrixPost(String authorization, AlleleMatrixSearchRequest body) {
        String token = ServerinfoApiController.readToken(authorization);
        
        AlleleMatrixResponse response = new AlleleMatrixResponse();        
        AlleleMatrix result = new AlleleMatrix();
        Metadata metadata = new Metadata();
        response.setResult(result);
        response.setMetadata(metadata);
        
        boolean fGotVariantSetList = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
        boolean fGotVariantList = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
        boolean fGotCallSetList = body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty();
        boolean fGotGermplasmFilter = ((body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty())
                || (body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty())
                || (body.getGermplasmPUIs() != null && !body.getGermplasmPUIs().isEmpty()));
        
        if (!fGotVariantSetList && !fGotVariantList && !fGotCallSetList) {
            Status status = new Status();
            status.setMessage("You must specify at least one of callSetDbId, variantDbId, or variantSetDbId!");
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
                    status.setMessage("You must specify VariantSets belonging to the same referenceSet!");
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
                    status.setMessage("You may specify VariantSets / Variants only belonging to the same referenceSet!");
                    metadata.addStatusItem(status);
                    response.setMetadata(metadata);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        Set<String> germplasmIds = new HashSet<>();
        if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
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
                    status.setMessage("You may specify VariantSets / Variants / CallSets / germplasm only belonging to the same referenceSet!");
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
                List<String> individualsFoundByName = MongoTemplateManager.get(module).findDistinct(query, GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
                germplasmIds.addAll(individualsFoundByName);
            }
        }        
        
        HashMap<Integer, String> sampleIndividuals = new HashMap<>();	// we are going to need the individual each sample is related to, in order to build callSetDbIds
        if (!germplasmIds.isEmpty()) { //search sample id based on germplasm id

            Query q = new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmIds));                

            List<GenotypingSample> foundSampleIds = MongoTemplateManager.get(module).find(q, GenotypingSample.class);    
            for (GenotypingSample s:foundSampleIds) {
                sampleIndividuals.put(s.getId(), s.getIndividual());
            }
            
        }  
    	
        if (fGotCallSetList) {
            for (String callSetDbId : body.getCallSetDbIds()) {
                String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
                if (module == null)
                    module = info[0];
                else if (!module.equals(info[0])) {
                    Status status = new Status();
                    status.setMessage("You may specify VariantSets / Variants / CallSets only belonging to the same referenceSet!");
                    metadata.addStatusItem(status);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                sampleIndividuals.put(Integer.parseInt(info[2]), info[1]);
            }
            
        }

        if (!sampleIndividuals.isEmpty()) {
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
                String[] pr = positionRange.split(":");
                String chr = pr[0];
                String[] range = pr[1].split("-");
                int start = Integer.parseInt(range[0]);  
                int end = Integer.parseInt(range[1]); 
                vsCrits.add(new Criteria().andOperator(
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(chr),
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).gte(start), 
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_START_SITE).lte(end))
                );
                vsCrits.add(new Criteria().andOperator(
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_SEQUENCE).is(chr),
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_END_SITE).gte(start), 
                        Criteria.where(VariantRunData.FIELDNAME_REFERENCE_POSITION + "." + FIELDNAME_END_SITE).lte(end))
                );
                
            }
            crits.add(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
            
        }

        Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));
        
        int numberOfCallSetsPerPage = 1000;
        int callSetsPage = 0;
        int numberOfMarkersPerPage = 1000;
        int variantsPage = 0;

        if (body.getPagination() != null) {
            for (AlleleMatrixSearchRequestPagination pagination:body.getPagination()) {
                if (pagination.getDimension().equals(AlleleMatrixSearchRequestPagination.DimensionEnum.CALLSETS)) {
                    numberOfCallSetsPerPage = pagination.getPageSize();
                    callSetsPage = pagination.getPage();
                } else if (pagination.getDimension().equals(AlleleMatrixSearchRequestPagination.DimensionEnum.VARIANTS)) {
                    numberOfMarkersPerPage = pagination.getPageSize();
                    variantsPage = pagination.getPage();
                }
            }
        }

    	// now deal with samples
        int nTotalSamplesCount = 0;
        if (!sampleIndividuals.isEmpty()) {	// project necessary fields to get only the required genotypes
            runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);
            for (Integer s:sampleIndividuals.keySet()) {
                //String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + s);
            }
            nTotalSamplesCount = sampleIndividuals.keySet().size();
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
            for (GenotypingSample gs : mongoTemplate.find(sampleQuery.skip(callSetsPage * numberOfCallSetsPerPage).limit(numberOfCallSetsPerPage), GenotypingSample.class)) {
                sampleIndividuals.put(gs.getId(), gs.getIndividual());
            }
        }
        List<String> callSetIds = new ArrayList<>();
        for (Integer spId:sampleIndividuals.keySet()) {
            callSetIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
        }

    	String unknownGtCode = body.getUnknownString() == null ? "-" : body.getUnknownString();
        result.setUnknownString(unknownGtCode);
    	String phasedSeparator = body.getSepPhased() == null ? "|" : body.getSepPhased();
        result.setSepPhased(phasedSeparator);
    	String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
    	result.setSepUnphased(unPhasedSeparator);
        
        //count variants
        int nTotalMarkerCount = (int) mongoTemplate.count(runQuery, VariantRunData.class);      
         AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
        variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
        variantPagination.setPage(variantsPage);
        variantPagination.setPageSize(numberOfMarkersPerPage);
        variantPagination.setTotalCount(nTotalMarkerCount);
        int nbOfPages =  nTotalMarkerCount / numberOfMarkersPerPage;
        if (nTotalMarkerCount % numberOfMarkersPerPage > 0) {
            nbOfPages++;
        }
        variantPagination.setTotalPages(nbOfPages);

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

        result.setPagination(Arrays.asList(variantPagination, callSetPagination));
        
        if (body.isPreview()) { //don't return dataMatrices
            return new ResponseEntity<>(response, HttpStatus.OK); 
        }

        try {            
            List<AbstractVariantData> varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, VariantRunData.class, runQuery, variantsPage * numberOfMarkersPerPage, numberOfMarkersPerPage);
            
            Document filter = new Document();
            if (fGotVariantSetList) {
                List<Criteria> vsCrits = new ArrayList<>();
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

            Map<String, AlleleMatrixDataMatrices> metadataMatricesMap = new HashMap<>();            
            MongoCollection<Document> vcfHeadersColl = mongoTemplate.getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));
            //List<DBVCFHeader> vcfHeaders = mongoTemplate.find(headersQuery, DBVCFHeader.class);
            Document fields = new Document();
            if (body.getDataMatrixAbbreviations() != null && !body.getDataMatrixAbbreviations().isEmpty()) {
                for (String key:body.getDataMatrixAbbreviations()) {
                    fields.put(DBVCFHeader.FIELDNAME_FORMAT_METADATA + "." + key, 1);
                }
            }            
            
            MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).projection(fields).iterator();
            while (headerCursor.hasNext()) {
                DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
                Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData();
                for (String key:vcfMetadata.keySet()) {
                    if (body.getDataMatrixNames() != null && !body.getDataMatrixNames().isEmpty()) {
                        if (vcfMetadata.get(key).getDescription() != null) {
                            if (body.getDataMatrixNames().contains(vcfMetadata.get(key).getDescription())) {
                                AlleleMatrixDataMatrices matrix = new AlleleMatrixDataMatrices();
                                matrix.setDataMatrix(new ArrayList());
                                matrix.setDataMatrixAbbreviation(vcfMetadata.get(key).getID());
                                matrix.setDataMatrixName(vcfMetadata.get(key).getDescription());
                                VCFHeaderLineType type = vcfMetadata.get(key).getType();
                                DataTypeEnum brapiType = DataTypeEnum.fromValue(type.toString());
                                matrix.setDataType(brapiType);
                                metadataMatricesMap.put(key, matrix);
                            }
                        }
                    } else { 
                        AlleleMatrixDataMatrices matrix = new AlleleMatrixDataMatrices();
                        matrix.setDataMatrix(new ArrayList());
                        matrix.setDataMatrixAbbreviation(vcfMetadata.get(key).getID());
                        matrix.setDataMatrixName(vcfMetadata.get(key).getDescription());
                        VCFHeaderLineType type = vcfMetadata.get(key).getType();
                        DataTypeEnum brapiType = DataTypeEnum.fromValue(type.toString());
                        matrix.setDataType(brapiType);
                        metadataMatricesMap.put(key, matrix);                                               
                    }
                }
            }
                       
            
            HashMap<Integer, String> previousPhasingIds = new HashMap<>();

            List<String> variantIds = new ArrayList<>();            
            Set<String> variantSetIds = new HashSet();

            List<List<String>> data = new ArrayList(); 
 
            for (AbstractVariantData v : varList) {
                VariantRunData vrd = (VariantRunData) v;
                
                variantSetIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + Integer.toString(vrd.getId().getProjectId()) + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getRunName());
                variantIds.add(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getVariantId());

                List<String> variantGenotypes = new ArrayList<>();
                Map<String, List<String>> otherDataMap= new HashMap<>();
                for (String key:metadataMatricesMap.keySet()) {
                    otherDataMap.put(key, new ArrayList<>());
                }

                for (Integer spId : sampleIndividuals.keySet()) {
                    SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
                    if (sg != null) {
                        String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
                        boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
                        previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/

                        String gtCode = sg.getCode();
                        if (gtCode == null || gtCode.length() == 0) {
                            variantGenotypes.add(unknownGtCode);
                        } else {
                            List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
                            String sep = fPhased ? "|" : "/";
                            if (!Boolean.TRUE.equals(body.isExpandHomozygotes()) && new HashSet<String>(alleles).size() == 1) {
                                variantGenotypes.add(gtCode.split(sep)[0]);
                            } else {
                                variantGenotypes.add(gtCode.replace(sep, fPhased ? phasedSeparator : unPhasedSeparator));
                            }
                        }

                        Map<String, Object> ai = sg.getAdditionalInfo();
                        for (String key:metadataMatricesMap.keySet()) {
                            AlleleMatrixDataMatrices matrix = metadataMatricesMap.get(key);
                            if (ai.get(key) != null) {
                                otherDataMap.get(key).add(ai.get(key).toString());
                            } else {
                                otherDataMap.get(key).add(unknownGtCode);
                            }
                        }                        
                    }
                }
                data.add(variantGenotypes);
                for (String key:metadataMatricesMap.keySet()) {
                    metadataMatricesMap.get(key).getDataMatrix().add(otherDataMap.get(key));
                }
            }
                
            result.setCallSetDbIds(callSetIds);
            result.setVariantDbIds(variantIds);
            result.setVariantSetDbIds(new ArrayList(variantSetIds));
            metadataMatricesMap.get("GT").setDataMatrix(data);
            
//            AlleleMatrixDataMatrices gtMatrix = new AlleleMatrixDataMatrices();
//            gtMatrix.setDataMatrix(data);
//            gtMatrix.setDataMatrixAbbreviation("GT");
//            gtMatrix.setDataMatrixName("Genotype");
//            gtMatrix.setDataType(AlleleMatrixDataMatrices.DataTypeEnum.STRING);
//
//            result.getDataMatrices().add(gtMatrix);
            
            //Add metadata matrices
            for (String key:metadataMatricesMap.keySet()) {
                result.getDataMatrices().add(metadataMatricesMap.get(key));
            }
		
            response.setResult(result);
            return new ResponseEntity<AlleleMatrixResponse>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<AlleleMatrixResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private DBVCFHeader getVCFHeader(MongoTemplate mongoTemplate, Integer project, String run) {
        BasicDBObject whereQuery = new BasicDBObject();
        Query query = new Query(new Criteria().andOperator(Criteria.where("_id."+ DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT).is(project), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(run)));
        List<DBVCFHeader> vcfHeaders = mongoTemplate.findDistinct(query, GenotypingProject.FIELDNAME_RUNS, DBVCFHeader.class, DBVCFHeader.class);
        if (vcfHeaders == null || vcfHeaders.isEmpty()) {
            return null;
        } else {
            return vcfHeaders.get(0);        
        }
    }

}
