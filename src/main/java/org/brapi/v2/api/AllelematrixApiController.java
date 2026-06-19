package org.brapi.v2.api;

import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_END_SITE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_SEQUENCE;
import static fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_START_SITE;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.ejb.ObjectNotFoundException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ObjectOperators.MergeObjects;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import fr.cirad.mgdb.exporting.IExportHandler;
import fr.cirad.mgdb.exporting.tools.ExportManager;
import fr.cirad.mgdb.model.mongo.maintypes.Assembly;
import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.Callset;
import fr.cirad.mgdb.model.mongo.subtypes.Run;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.model.mongo.subtypes.VariantRunDataId;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.tools.AppConfig;
import fr.cirad.tools.ExpiringHashMap;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.query.GroupedExecutor;
import fr.cirad.tools.query.GroupedExecutor.TaskWrapper;
import fr.cirad.tools.security.base.AbstractTokenManager;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;

@Controller
public class AllelematrixApiController implements AllelematrixApi {

    static private final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AllelematrixApiController.class);

    @Autowired
    AbstractTokenManager tokenManager;

    @Autowired
    AppConfig appConfig;

    private static final ExpiringHashMap<String, Long> countCache = new ExpiringHashMap<>(1000 * 60 * 5 /* expiration delay: 5 minutes */, true /* push back expiry date each time we get an existing value */);

    private List<String> variantIds;

    public List<String> getVariantIds() {
        return variantIds;
    }

    @Override
    public ResponseEntity<AlleleMatrixResponse> allelematrixGet(Integer dimensionVariantPage, Integer dimensionVariantPageSize, Integer dimensionCallSetPage, Integer dimensionCallSetPageSize,
                                                                Integer dimensionColumnPage, Integer dimensionColumnPageSize,
                                                                Boolean preview, String dataMatrixAbbreviations, String positionRange, String germplasmDbId, String germplasmName, String germplasmPUI, String callSetDbId,
                                                                String variantDbId, String variantSetDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased,
                                                                String studyDbId, AlleleMatrixSearchRequest.DimensionColumnAggregationEnum dimensionColumnAggregation, String authorization) throws InterruptedException, ObjectNotFoundException, MalformedParametersException {

        if (variantSetDbId == null && callSetDbId != null) {
            String[] info = Helper.getInfoFromId(callSetDbId, 2);
            Callset cs = MongoTemplateManager.get(info[0]).findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "._id").is(Integer.parseInt(info[1]))), GenotypingSample.FIELDNAME_CALLSETS, GenotypingSample.class, Callset.class).iterator().next();
            variantSetDbId = info[0] + Helper.ID_SEPARATOR + cs.getProjectId() + Helper.ID_SEPARATOR + cs.getRun();
        }

        AlleleMatrixSearchRequest request = new AlleleMatrixSearchRequest();

        AlleleMatrixSearchRequestPagination csPagination = new AlleleMatrixSearchRequestPagination();
        csPagination.setDimension(AlleleMatrixSearchRequestPagination.DimensionEnum.COLUMNS);
        if (dimensionCallSetPage != null) {
            csPagination.setPage(dimensionCallSetPage);
        }
        if (dimensionColumnPage != null) {
            csPagination.setPage(dimensionColumnPage);
        }
        if (dimensionCallSetPageSize != null) {
            csPagination.setPageSize(dimensionCallSetPageSize);
        }
        if (dimensionColumnPageSize != null) {
            csPagination.setPageSize(dimensionColumnPageSize);
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
        if (studyDbId != null) {
            request.setStudyDbIds(Arrays.asList(studyDbId));
        }
        if (dimensionColumnAggregation != null) {
            request.setDimensionColumnAggregation(dimensionColumnAggregation);
        }

        return searchAllelematrixPost(authorization, request);
    }

    @Override
    public ResponseEntity<AlleleMatrixResponse> searchAllelematrixPost(String authorization, AlleleMatrixSearchRequest body) throws InterruptedException, ObjectNotFoundException {
        return searchAllelematrixPost(authorization, body, true);
    }

    public ResponseEntity<AlleleMatrixResponse> searchAllelematrixPost(String authorization, AlleleMatrixSearchRequest body, boolean fVcfStyleGenotypes) throws InterruptedException, ObjectNotFoundException {
        int maxTotalCalls = appConfig.getAlleleSearchMaxTotalPageSize();
        String token = ServerinfoApiController.readToken(authorization);
        long before = System.currentTimeMillis();
        AlleleMatrixResponse response = new AlleleMatrixResponse();
        AlleleMatrix result = new AlleleMatrix();
        Metadata metadata = new Metadata();
        response.setResult(result);
        response.setMetadata(metadata);

        try {
            if (body.getGermplasmPUIs() != null && !body.getGermplasmPUIs().isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "germplasmPUIs filter is not supported"
                );
            }

            // -------------------------------------------------------------------------
            // Parse pagination parameters
            // -------------------------------------------------------------------------
            int numberOfBioEntitiesPerPage = 100;
            int bioEntitiesPage = 0;
            int numberOfMarkersPerPage = 100;
            int variantsPage = 0;
            boolean fCSPagination = false; // just to stay compliant with callset pagination dimension

            if (body.getPagination() != null && !body.getPagination().isEmpty()) {
                for (AlleleMatrixSearchRequestPagination pagination : body.getPagination()) {
                    if (pagination.getDimension() == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid pagination dimension specified, only 'VARIANTS', 'GERMPLASM', 'SAMPLES' and 'CALLSETS' are accepted!"
                        );
                    }

                    if (pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.CALLSETS
                            || pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.COLUMNS) {
                        numberOfBioEntitiesPerPage = pagination.getPageSize();
                        bioEntitiesPage = pagination.getPage();
                        fCSPagination = pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.CALLSETS;
                    } else if (pagination.getDimension() == AlleleMatrixSearchRequestPagination.DimensionEnum.VARIANTS) {
                        numberOfMarkersPerPage = pagination.getPageSize();
                        variantsPage = pagination.getPage();
                    }
                }
            }

            if (numberOfBioEntitiesPerPage * numberOfMarkersPerPage > maxTotalCalls) {
                Status status = new Status();
                String message = "";
                if (numberOfBioEntitiesPerPage > maxTotalCalls) {
                    numberOfMarkersPerPage = 1;
                    message = "CALLSET pageSize out of bounds (>" + maxTotalCalls + "), returning calls by variant, VARIANT pageSize set to " + numberOfMarkersPerPage;
                } else {
                    numberOfMarkersPerPage = maxTotalCalls / numberOfBioEntitiesPerPage;
                    message = "VARIANT pageSize out of bounds, set to " + numberOfMarkersPerPage;
                }
                status.setMessage(message + " (VARIANT pageSize * CALLSETS pageSize should not exceed " + maxTotalCalls + ")");
                metadata.addStatusItem(status);
            }

            String unknownGtCode = body.getUnknownString() == null ? "." : body.getUnknownString();
            result.setUnknownString(unknownGtCode);
            String phasedSeparator = body.getSepPhased() == null ? "|" : body.getSepPhased();
            result.setSepPhased(phasedSeparator);
            String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
            result.setSepUnphased(unPhasedSeparator);

            // -------------------------------------------------------------------------
            // Validate that at least one filter is provided
            // -------------------------------------------------------------------------
            boolean fGotVariantSetList = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
            boolean fGotVariantList = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
            boolean fGotSampleFilter = ((body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty())
                    || (body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty())
                    || (body.getGermplasmPUIs() != null && !body.getGermplasmPUIs().isEmpty())
                    || (body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty())
                    || (body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty()));
            boolean fGotStudyFilter = body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty();

            if (!fGotVariantSetList && !fGotVariantList && !fGotSampleFilter && !fGotStudyFilter) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "You must specify at least one of those filters : callSetDbId, sampleDbId, germplasmDbId, variantDbId, variantSetDbId, or studyDbId!"
                );
            }

            // -------------------------------------------------------------------------
            // Resolve module (database) from the provided filter IDs — all must agree
            // -------------------------------------------------------------------------
            String module = null;

            if (fGotVariantSetList) {
                for (String variantSetDbId : body.getVariantSetDbIds()) {
                    String[] info = Helper.getInfoFromId(variantSetDbId, 3);
                    if (info == null) {
                        throw new MalformedParametersException("malformed variantSetDbId: " + variantSetDbId);
                    }
                    if (module == null) {
                        module = info[0];
                    } else if (!module.equals(info[0])) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "You must specify VariantSets belonging to the same program / trial!"
                        );
                    }
                }
            }

            if (fGotVariantList) {
                for (String variantDbId : body.getVariantDbIds()) {
                    String[] info = Helper.getInfoFromId(variantDbId, 2);
                    if (info == null) {
                        throw new MalformedParametersException("malformed variantDbId: " + variantDbId);
                    }
                    if (module == null) {
                        module = info[0];
                    } else if (!module.equals(info[0])) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "You may specify VariantSets / Variants only belonging to the same program / trial!"
                        );
                    }
                }
            }

            // -------------------------------------------------------------------------
            // Resolve studyDbIds → variantSetDbIds (rows) and restrict samples (columns)
            // -------------------------------------------------------------------------
            if (fGotStudyFilter) {
                // Row dimension: resolve studies to VariantSets via VariantSet.studyDbId
                List<String> studyVariantSetIds = new ArrayList<>();
                for (String studyDbId : body.getStudyDbIds()) {
                    String[] info = Helper.getInfoFromId(studyDbId, 2);
                    if (info == null)
                        throw new MalformedParametersException("malformed studyDbId: " + studyDbId);
                    if (module == null) {
                        module = info[0];
                    } else if (!module.equals(info[0])) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "You may only specify studies belonging to the same program / trial!");
                    }
                    MongoTemplate mt = MongoTemplateManager.get(info[0]);
                    if (mt == null)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database " + info[0] + " does not exist");
                    List<GenotypingProject> studyProjects = mt.find(new Query(Criteria.where("_id").is(Integer.parseInt(info[1]))), GenotypingProject.class);
                    for (GenotypingProject proj : studyProjects)
                        for (String run : proj.getRuns())
                            studyVariantSetIds.add(info[0] + Helper.ID_SEPARATOR + proj.getId() + Helper.ID_SEPARATOR + run);
                }
                if (studyVariantSetIds.isEmpty())
                    return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);

                // Intersect with any caller-supplied variantSetDbIds
                if (fGotVariantSetList) {
                    studyVariantSetIds.retainAll(body.getVariantSetDbIds());
                    if (studyVariantSetIds.isEmpty())
                        return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
                }
                body.setVariantSetDbIds(studyVariantSetIds);
                fGotVariantSetList = true;
            }

            // -------------------------------------------------------------------------
            // Resolve germplasm → individual IDs
            // -------------------------------------------------------------------------
            List<String> germplasmIds = null;
            if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
                germplasmIds = new ArrayList<>();
                Map<String, Collection<String>> gMap = GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds());
                if (gMap.size() > 1) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "You can't specify germplasm ids from different programs"
                    );
                }
                if (module == null) {
                    module = gMap.keySet().iterator().next();
                } else if (!module.equals(gMap.keySet().iterator().next())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "You may specify VariantSets / Variants / CallSets / Germplasm only belonging to the same program / trial!"
                    );
                }
                germplasmIds.addAll(gMap.get(module));
            }

            HashSet<String> givenSampleIds = new HashSet<>();
            if (body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty()) {
                for (String sampleDbId : body.getSampleDbIds()) {
                    String[] info = Helper.getInfoFromId(sampleDbId, 2);
                    if (info == null) {
                        throw new MalformedParametersException("malformed sampleDbId: " + sampleDbId);
                    }
                    if (module == null) {
                        module = info[0];
                    } else if (!module.equals(info[0])) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "You may specify VariantSets / Variants / CallSets / Germplasm only belonging to the same program / trial!"
                        );
                    }
                    givenSampleIds.add(info[1]);
                }
            }

            List<Integer> givenCallsetIds = new ArrayList<>();
            if (body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty()) {
                for (String callsetId : body.getCallSetDbIds()) {
                    String[] info = Helper.getInfoFromId(callsetId, 2);
                    if (info == null) {
                        throw new MalformedParametersException("malformed callsetDbId: " + callsetId);
                    }
                    if (module == null) {
                        module = info[0];
                    } else if (!module.equals(info[0])) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "You may specify VariantSets / Variants / CallSets / Samples only belonging to the same program / trial!"
                        );
                    }
                    givenCallsetIds.add(Integer.parseInt(info[1]));
                }
            }

            MongoTemplate mongoTemplate = module == null ? null : MongoTemplateManager.get(module);
            if (module != null && mongoTemplate == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Database " + module + " does not exist"
                );
            }

            if (body.getGermplasmNames() != null && !body.getGermplasmNames().isEmpty()) {
                if (module == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "When using germplasmName filter, you have to specify at least a variantSetDbId or a variantDbId"
                    );
                }
                Query query = new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(body.getGermplasmNames()));
                List<String> germplasmIdsByNames = mongoTemplate.findDistinct(query, GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
                if (germplasmIds == null)
                    germplasmIds = germplasmIdsByNames;
                else
                    germplasmIds.retainAll(germplasmIdsByNames);
            }

            if (germplasmIds != null && germplasmIds.isEmpty())
                return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);

            // -------------------------------------------------------------------------
            // Resolve germplasm → sample IDs → callset IDs
            // -------------------------------------------------------------------------
            HashSet<String> sampleIDs = new HashSet<>();
            if (germplasmIds != null) {
                if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.GERMPLASM) {
                    if (bioEntitiesPage * numberOfBioEntitiesPerPage >= germplasmIds.size()) {
                        germplasmIds = new ArrayList<>();
                    } else {
                        Integer endOfList = (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage >= germplasmIds.size() ? germplasmIds.size() : (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage;
                        germplasmIds = germplasmIds.subList(bioEntitiesPage * numberOfBioEntitiesPerPage, endOfList);
                    }
                }
                for (GenotypingSample s : mongoTemplate.find(new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmIds)), GenotypingSample.class))
                    sampleIDs.add(s.getId());
            }

            if (!givenSampleIds.isEmpty()) {
                if (sampleIDs.isEmpty())
                    sampleIDs = givenSampleIds;
                else
                    sampleIDs.retainAll(givenSampleIds);
                if (sampleIDs.isEmpty()) {
                    return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
                } else {
                    if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.SAMPLE) {
                        if (bioEntitiesPage * numberOfBioEntitiesPerPage >= sampleIDs.size()) {
                            sampleIDs = new HashSet<>();
                        } else {
                            Integer endOfList = (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage >= sampleIDs.size() ? sampleIDs.size() : (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage;
                            sampleIDs = new HashSet<>(new ArrayList<>(sampleIDs).subList(bioEntitiesPage * numberOfBioEntitiesPerPage, endOfList));
                        }
                    }
                }
            }

            List<Integer> callsetIds = new ArrayList<>();
            if (!sampleIDs.isEmpty())
                callsetIds = mongoTemplate.findDistinct(new Query(Criteria.where("_id").in(sampleIDs)), GenotypingSample.FIELDNAME_CALLSETS + "._id", GenotypingSample.class, Integer.class);

            if (!givenCallsetIds.isEmpty()) {
                if (callsetIds.isEmpty())
                    callsetIds = givenCallsetIds;
                else
                    callsetIds.retainAll(givenCallsetIds);
            }

            if (!callsetIds.isEmpty()) {
                List<Callset> callsets = mongoTemplate.findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "._id").in(callsetIds)), GenotypingSample.FIELDNAME_CALLSETS, GenotypingSample.class, Callset.class);
                if (callsets.isEmpty()) {
                    return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
                } else {
                    Map<String, List<Integer>> callSetsByVariantSet = new HashMap<>();
                    for (Callset cs : callsets) {
                        String variantSetDbId = module + Helper.ID_SEPARATOR + cs.getProjectId() + Helper.ID_SEPARATOR + cs.getRun();
                        List<Integer> variantSetCallSets = callSetsByVariantSet.get(variantSetDbId);
                        if (variantSetCallSets == null) {
                            variantSetCallSets = new ArrayList<>();
                            callSetsByVariantSet.put(variantSetDbId, variantSetCallSets);
                        }
                        variantSetCallSets.add(cs.getId());
                    }
                    if (fGotVariantSetList) {
                        callsetIds = new ArrayList<>();
                        for (String vs : callSetsByVariantSet.keySet())
                            if (body.getVariantSetDbIds().contains(vs))
                                callsetIds.addAll(callSetsByVariantSet.get(vs));
                        if (callsetIds.isEmpty())
                            return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
                    } else {
                        body.setVariantSetDbIds(new ArrayList<>(callSetsByVariantSet.keySet()));
                        fGotVariantSetList = true;
                    }
                }
            } else if (fGotSampleFilter) {
                return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
            }

            // -------------------------------------------------------------------------
            // Check permissions
            // -------------------------------------------------------------------------
            Collection<Integer> projectIDs = fGotVariantSetList
                    ? body.getVariantSetDbIds().stream().map(vsId -> Integer.parseInt(Helper.getInfoFromId(vsId, 3)[1])).collect(Collectors.toSet())
                    : mongoTemplate.findDistinct(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(Helper.ID_SEPARATOR))).collect(Collectors.toList()))), "_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, VariantRunData.class, Integer.class);
            List<Integer> forbiddenProjectIDs = new ArrayList<>();
            for (int pj : projectIDs)
                try {
                    if (!tokenManager.canUserReadProject(token, module, pj))
                        forbiddenProjectIDs.add(pj);
                } catch (ObjectNotFoundException ignored) {
                }
            projectIDs.removeAll(forbiddenProjectIDs);
            if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty())
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);

            // -------------------------------------------------------------------------
            // Build query criteria for VariantRunData and VariantData collections
            // -------------------------------------------------------------------------
            List<Criteria> vrdCrits = new ArrayList<>();
            List<Criteria> variantCrits = new ArrayList<>();
            if (fGotVariantSetList) {
                GenotypingProject[] allGenotypingProjects = mongoTemplate.find(new Query(), GenotypingProject.class).toArray(new GenotypingProject[0]);
                if (allGenotypingProjects.length > 1 || allGenotypingProjects[0].getRuns().size() > 1) {
                    List<Criteria> pjVariantRunCrits = new ArrayList<>();
                    List<Criteria> pjVariantCrits = new ArrayList<>();
                    for (String vsId : body.getVariantSetDbIds()) {
                        String[] info = Helper.getInfoFromId(vsId, 3);
                        int projId = Integer.parseInt(info[1]);
                        pjVariantRunCrits.add(new Criteria().andOperator(
                                new Criteria("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(projId),
                                new Criteria("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])));
                        pjVariantCrits.add(new Criteria().andOperator(
                                new Criteria(VariantData.FIELDNAME_RUNS + "." + Run.FIELDNAME_PROJECT_ID).is(projId),
                                new Criteria(VariantData.FIELDNAME_RUNS + "." + Run.FIELDNAME_RUNNAME).is(info[2])));
                    }
                    if (!pjVariantRunCrits.isEmpty()) {
                        vrdCrits.add(new Criteria().orOperator(pjVariantRunCrits));
                        variantCrits.add(new Criteria().orOperator(pjVariantCrits));
                    }
                } else if (!body.getVariantSetDbIds().contains(module + Helper.ID_SEPARATOR + allGenotypingProjects[0].getId() + Helper.ID_SEPARATOR + allGenotypingProjects[0].getRuns().get(0)))
                    return returnEmptyMatrix(response, variantsPage, numberOfMarkersPerPage, bioEntitiesPage, numberOfBioEntitiesPerPage);
            }

            List<String> varIDs = new ArrayList<>();
            if (fGotVariantList) {
                varIDs = body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(Helper.ID_SEPARATOR))).collect(Collectors.toList());
                vrdCrits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(varIDs));
                variantCrits.add(Criteria.where("_id").in(varIDs));
            }

            List<Assembly> assemblies = mongoTemplate.findAll(Assembly.class);
            Integer nAssemblyId = assemblies.size() == 0 ? null : assemblies.get(0).getId();
            String refPosPathWithTrailingDot = Assembly.getVariantRefPosPath(nAssemblyId) + ".";

            if (body.getPositionRanges() != null) {
                List<Criteria> rangeCrits = new ArrayList<>();
                for (String positionRange : body.getPositionRanges()) {
                    try {
                        String[] pr = positionRange.split(":");
                        if (pr.length > 2)
                            throw new Exception("Only one colon is supported in positionRange strings");
                        Criteria posCrits = Criteria.where(refPosPathWithTrailingDot + FIELDNAME_SEQUENCE).is(pr[0]);
                        if (pr.length == 2) {
                            String[] range = pr[1].split("-");
                            if (!range[0].isEmpty() && !pr[1].startsWith("-")) {
                                int start = Integer.parseInt(range[0]);
                                posCrits.orOperator(Arrays.asList(
                                        Criteria.where(refPosPathWithTrailingDot + FIELDNAME_START_SITE).gte(start),
                                        Criteria.where(refPosPathWithTrailingDot + FIELDNAME_END_SITE).gte(start)));
                            }
                            if (!range[range.length - 1].isEmpty() && !pr[1].endsWith("-"))
                                posCrits.and(refPosPathWithTrailingDot + FIELDNAME_START_SITE).lte(Integer.parseInt(range[range.length - 1]));
                        }
                        rangeCrits.add(posCrits);
                    } catch (Exception e) {
                        Status status = new Status();
                        status.setMessage("Can't read positionRange: " + positionRange);
                        metadata.addStatusItem(status);
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                }
                if (!rangeCrits.isEmpty()) {
                    vrdCrits.add(new Criteria().orOperator(rangeCrits));
                    variantCrits.add(new Criteria().orOperator(rangeCrits));
                }
            }

            Query runQuery = vrdCrits.isEmpty() ? new Query() : new Query(new Criteria().andOperator(vrdCrits));
            Query variantsQuery = variantCrits.isEmpty() ? new Query() : new Query(new Criteria().andOperator(variantCrits));

            // -------------------------------------------------------------------------
            // Callset pagination
            // -------------------------------------------------------------------------
            // allCallsetIdsForAgg holds the complete pre-pagination callset list so we can
            // count distinct aggregation keys across all pages for the column totalCount.
            List<Integer> allCallsetIdsForAgg = new ArrayList<>();
            int nTotalCallsetsCount = 0;
            if (callsetIds != null && !callsetIds.isEmpty()) {
                allCallsetIdsForAgg = new ArrayList<>(callsetIds);
                nTotalCallsetsCount = callsetIds.size();
                if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.CALLSET) {
                    if (bioEntitiesPage * numberOfBioEntitiesPerPage >= callsetIds.size()) {
                        callsetIds = new ArrayList<>();
                    } else {
                        Integer endOfList = (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage >= callsetIds.size() ? callsetIds.size() : (bioEntitiesPage + 1) * numberOfBioEntitiesPerPage;
                        callsetIds = callsetIds.subList(bioEntitiesPage * numberOfBioEntitiesPerPage, endOfList);
                    }
                } else {
                    callsetIds = allCallsetIdsForAgg;
                }
            } else {
                Query query;
                if (fGotVariantSetList) {
                    List<Criteria> vsCrits = new ArrayList<>();
                    for (String vsId : body.getVariantSetDbIds()) {
                        String[] info = Helper.getInfoFromId(vsId, 3);
                        vsCrits.add(new Criteria().andOperator(
                                Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])),
                                Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_RUN).is(info[2])));
                    }
                    query = new Query(new Criteria().orOperator(vsCrits));
                } else
                    query = new Query(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_PROJECT_ID).in(projectIDs));

                int fromIndex = bioEntitiesPage * numberOfBioEntitiesPerPage;
                if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.CALLSET) {
                    List<Integer> allCallSetIDs = mongoTemplate.findDistinct(query, GenotypingSample.FIELDNAME_CALLSETS + "._id", GenotypingSample.class, Integer.class);
                    callsetIds = allCallSetIDs.subList(fromIndex, Math.min(fromIndex + numberOfBioEntitiesPerPage, allCallSetIDs.size()));
                } else if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.SAMPLE) {
                    List<String> allSampleIDs = mongoTemplate.findDistinct(query, "._id", GenotypingSample.class, String.class);
                    if (fromIndex < allSampleIDs.size()) {
                        List<String> samplesIds = allSampleIDs.subList(fromIndex, Math.min(fromIndex + numberOfBioEntitiesPerPage, allSampleIDs.size()));
                        Query callsetsQuery = new Query(Criteria.where("_id").in(samplesIds));
                        callsetIds = mongoTemplate.findDistinct(callsetsQuery, GenotypingSample.FIELDNAME_CALLSETS + "._id", GenotypingSample.class, Integer.class);
                    }
                } else if (body.getDimensionColumnAggregation() == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.GERMPLASM) {
                    List<String> allIndIDs = mongoTemplate.findDistinct(query, GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class);
                    if (fromIndex < allIndIDs.size()) {
                        List<String> indIds = allIndIDs.subList(fromIndex, Math.min(fromIndex + numberOfBioEntitiesPerPage, allIndIDs.size()));
                        Query callsetsQuery = new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(indIds));
                        callsetIds = mongoTemplate.findDistinct(callsetsQuery, GenotypingSample.FIELDNAME_CALLSETS + "._id", GenotypingSample.class, Integer.class);
                    }
                }
            }

            if (!fVcfStyleGenotypes && !body.isPreview())
                runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELES);

            List<String> callSetDbIds = new ArrayList<>();
            for (Integer csId : callsetIds) {
                callSetDbIds.add(module + Helper.ID_SEPARATOR + csId);
                runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + csId);
            }
            // NOTE: aggregationKeys / aggKeyToCallsets / callsetToAggKey are built below,
            // after callsetIds is finalised, and are used in the matrix-building loop.
            // callSetDbIds here reflects the raw fetched callsets; the response's
            // callSetDbIds is overridden with aggregationKeys in buildFinalResponse when
            // column aggregation is active.

            // -------------------------------------------------------------------------
            // Build callset → aggregation-key mapping for dimensionColumnAggregation
            // -------------------------------------------------------------------------
            // aggregationKeys: ordered list of column labels (callSetDbId / sampleDbId / germplasmId)
            // callsetToAggKey:  maps each callset integer ID → its aggregation key
            // aggKeyToCallsets: maps each aggregation key → the callset IDs that belong to it
            List<String> aggregationKeys = new ArrayList<>();
            Map<Integer, String> callsetToAggKey = new LinkedHashMap<>();
            Map<String, List<Integer>> aggKeyToCallsets = new LinkedHashMap<>();

            AlleleMatrixSearchRequest.DimensionColumnAggregationEnum colAgg = body.getDimensionColumnAggregation(); // "callSet", "sample", or "germplasm"; null = default
            if (colAgg == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.CALLSET) {
                // Default: one column per callset
                for (Integer csId : callsetIds) {
                    String key = module + Helper.ID_SEPARATOR + csId;
                    callsetToAggKey.put(csId, key);
                    aggKeyToCallsets.computeIfAbsent(key, k -> new ArrayList<>()).add(csId);
                    aggregationKeys.add(key);
                }
            } else {
                // Aggregate by sample or germplasm: build reverse maps
                List<GenotypingSample> samplesForCallsets = mongoTemplate.find(new Query(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "._id").in(callsetIds)), GenotypingSample.class);
                // Map callsetId → sample
                Map<Integer, GenotypingSample> callsetToSample = new HashMap<>();
                for (GenotypingSample s : samplesForCallsets)
                    if (s.getCallSets() != null)
                        for (Callset cs : s.getCallSets())
                            if (callsetIds.contains(cs.getId()))
                                callsetToSample.put(cs.getId(), s);

                for (Integer csId : callsetIds) {
                    GenotypingSample sample = callsetToSample.get(csId);
                    String key;
                    if (colAgg == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.SAMPLE)
                        key = module + Helper.ID_SEPARATOR + (sample != null ? sample.getId() : csId);
                    else // germplasm
                        key = module + Helper.ID_SEPARATOR + (sample != null && sample.getIndividual() != null ? sample.getIndividual() : csId);
                    callsetToAggKey.put(csId, key);
                    if (!aggKeyToCallsets.containsKey(key))
                        aggregationKeys.add(key);
                    aggKeyToCallsets.computeIfAbsent(key, k -> new ArrayList<>()).add(csId);
                }
            }
            
            // -------------------------------------------------------------------------
            // Compute total aggregated column count for pagination.
            // Uses allCallsetIdsForAgg (full pre-pagination list) so totalCount reflects
            // distinct keys across all pages, not just the current one.
            // -------------------------------------------------------------------------
            int nTotalAggregatedColumnCount = nTotalCallsetsCount; // default for callSet level
            if (colAgg != AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.CALLSET && !allCallsetIdsForAgg.isEmpty()) {
                List<GenotypingSample> allSamplesForCount = mongoTemplate.find(
                        new Query(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "._id").in(allCallsetIdsForAgg)),
                        GenotypingSample.class);
                Set<String> distinctAggKeys = new LinkedHashSet<>();
                for (GenotypingSample s : allSamplesForCount) {
                    if (s.getCallSets() == null) continue;
                    for (Callset cs : s.getCallSets()) {
                        if (!allCallsetIdsForAgg.contains(cs.getId())) continue;
                        String key = "sample".equalsIgnoreCase(colAgg.toString())
                                ? module + Helper.ID_SEPARATOR + s.getId()
                                : module + Helper.ID_SEPARATOR + (s.getIndividual() != null ? s.getIndividual() : cs.getId());
                        distinctAggKeys.add(key);
                    }
                }
                nTotalAggregatedColumnCount = distinctAggKeys.size();
            }

            // -------------------------------------------------------------------------
            // Start async variant count — runs concurrently with the data fetch below
            // -------------------------------------------------------------------------
            AtomicLong nTotalMarkerCount = new AtomicLong(-1);
            Thread countThread = new Thread() {
                public void run() {
                    long b4 = System.currentTimeMillis();
                    Long countVar = countCache.get(variantsQuery.getQueryObject().toString());
                    if (countVar == null) {
                        countVar = !variantsQuery.getQueryObject().isEmpty() ? mongoTemplate.count(variantsQuery, VariantData.class) : Helper.estimDocCount(mongoTemplate, VariantData.class);
                        countCache.put(variantsQuery.getQueryObject().toString(), countVar);
                    }
                    nTotalMarkerCount.set(countVar);
                    LOG.info("alleleMatrix variant totalCount (" + countVar + ") obtained in " + (System.currentTimeMillis() - b4) / 1000f + "s");
                }
            };
            countThread.start();

            // module is reassigned above so capture it as final for use in lambdas and inner classes
            final String finalModule = module;

            Status status = new Status();
            Set<String> variantSetDbIds = new HashSet<>();


            int nSkipCount = variantsPage * numberOfMarkersPerPage;

            if (callsetIds.isEmpty()) {
                countThread.join();
                return buildFinalResponse(response, result, metadata, status,
                        false, !sampleIDs.isEmpty(), fCSPagination, nTotalMarkerCount.get(),
                        numberOfMarkersPerPage, variantsPage, nTotalCallsetsCount,
                        numberOfBioEntitiesPerPage, bioEntitiesPage, nSkipCount);            }

            countThread.join(100);
            if (nTotalMarkerCount.get() != -1 && nSkipCount > nTotalMarkerCount.get()) {
                countThread.join();
                return buildFinalResponse(response, result, metadata, status,
                        false, !sampleIDs.isEmpty(), fCSPagination, nTotalMarkerCount.get(),
                        numberOfMarkersPerPage, variantsPage, nTotalCallsetsCount,
                        numberOfBioEntitiesPerPage, bioEntitiesPage, nSkipCount);
            }

            Map<String, AlleleMatrixDataMatrices> matricesMap = buildMatricesMap(body, fGotVariantSetList, mongoTemplate, projectIDs);
            HashMap<Integer, String> previousPhasingIds = new HashMap<>();
            LinkedHashSet<String> variantDbIds = new LinkedHashSet<>();

            // Both paths below produce mergedByVariantId: an ordered map of
            // variantId → single merged VariantRunData (genotypes from all runs
            // already combined), which the shared matrix-building loop consumes.
            LinkedHashMap<String, VariantRunData> mergedByVariantId = new LinkedHashMap<>();
            List<VariantRunDataWithRuns> varList;   // sentinel — used only to detect empty result in buildFinalResponse

            if (fGotVariantList || (fGotVariantSetList && body.getVariantSetDbIds().size() > 1)) {
                // -----------------------------------------------------------------
                // PATH A — explicit variant list or multiple variantSets.
                //
                // A single MongoDB aggregation ($match / $project / $group / $sort /
                // $skip / $limit) handles multi-run deduplication and preserves the
                // caller-supplied variant ordering. Genotypes are merged server-side
                // by the $group stage so no client-side merging is needed.
                // -----------------------------------------------------------------
                MatchOperation match = match(new Criteria().andOperator(vrdCrits.toArray(new Criteria[vrdCrits.size()])));
                ProjectionOperation project = Aggregation.project("_id", VariantRunData.FIELDNAME_KNOWN_ALLELES)
                        // preserve the order of the caller-supplied variantDbIds
                        .and(ArrayOperators.arrayOf(varIDs).indexOf("$_id." + VariantRunDataId.FIELDNAME_VARIANT_ID)).as("_order")
                        // restrict genotype columns to the current callset page
                        .and(VariantRunData.FIELDNAME_SAMPLEGENOTYPES).nested(Fields.from(callsetIds.stream().map(csId -> Fields.field(csId.toString(), VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + csId.toString())).toArray(Field[]::new)));
                if (!body.isPreview() && !fVcfStyleGenotypes)
                    project = project.and(VariantRunData.FIELDNAME_KNOWN_ALLELES).as(VariantRunData.FIELDNAME_KNOWN_ALLELES);
                if (!body.isPreview() && body.getDataMatrixAbbreviations() != null && body.getDataMatrixAbbreviations().stream().anyMatch(k -> !k.equals("GT")))
                    project = project.and(VariantRunData.SECTION_ADDITIONAL_INFO).as(VariantRunData.SECTION_ADDITIONAL_INFO);

                // Group by variantId so that $skip/$limit count variants, not runs.
                // Using _order as the synthetic projectId is a deliberate hack: the field
                // must exist for deserialization, and _order is identical across all runs
                // of the same variant so grouping still works correctly.
                GroupOperation group = Aggregation.group(Fields.from(new Field[] {
                                Fields.field("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID),
                                Fields.field(VariantRunDataId.FIELDNAME_PROJECT_ID, "_order") }))
                        .and("_order", ArrayOperators.First.firstOf("$_order"))
                        .addToSet(new Document()
                                .append(VariantRunDataId.FIELDNAME_PROJECT_ID, "$_id." + VariantRunDataId.FIELDNAME_PROJECT_ID)
                                .append(VariantRunDataId.FIELDNAME_RUNNAME, "$_id." + VariantRunDataId.FIELDNAME_RUNNAME))
                        .as(VariantRunDataWithRuns.FIELDNAME_RUNS)
                        .and(VariantRunData.FIELDNAME_KNOWN_ALLELES, ArrayOperators.First.firstOf(VariantRunData.FIELDNAME_KNOWN_ALLELES))
                        .and(VariantRunData.FIELDNAME_SAMPLEGENOTYPES, MergeObjects.mergeValuesOf(VariantRunData.FIELDNAME_SAMPLEGENOTYPES))
                        .and(VariantRunData.SECTION_ADDITIONAL_INFO, MergeObjects.mergeValuesOf(VariantRunData.SECTION_ADDITIONAL_INFO));

                Aggregation aggregation = Aggregation.newAggregation(
                                match, project, group,
                                sort(Sort.by(Sort.Direction.ASC, "_id." + Run.FIELDNAME_PROJECT_ID)),
                                Aggregation.skip(nSkipCount), Aggregation.limit(numberOfMarkersPerPage))
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

                varList = mongoTemplate.aggregate(aggregation, VariantRunData.class, VariantRunDataWithRuns.class).getMappedResults();

                // Collect variantSetDbIds from the run metadata embedded in each result
                variantSetDbIds = varList.stream()
                        .flatMap(v -> v.getRuns().stream())
                        .map(r -> finalModule + "§" + r.getProjectId() + "§" + r.getRunName())
                        .collect(Collectors.toCollection(HashSet::new));

                // MongoDB already merged genotypes across runs via $group — one entry per variant
                for (VariantRunDataWithRuns vrd : varList)
                    mergedByVariantId.put(vrd.getVariantId(), vrd);

            } else {
                // -----------------------------------------------------------------
                // PATH B — single variantSet, no explicit variant list.
                //
                // Step 1: get the ordered page of variant IDs by genomic position.
                // Step 2: fetch VariantRunData in parallel chunks via the executor.
                // Step 3: reassemble chunks in order and merge runs per variant.
                // -----------------------------------------------------------------

                // Step 1: get the ordered page of variant IDs by genomic position
                final List<String> pageVariantIDs = VariantsApiController.getSortedVariantListChunk(mongoTemplate, nAssemblyId, VariantData.class, variantsQuery, nSkipCount, numberOfMarkersPerPage).stream().map(AbstractVariantData::getVariantId).collect(Collectors.toList());
                if (pageVariantIDs.isEmpty())
                	return buildFinalResponse(response, result, metadata, status,
                            false, !sampleIDs.isEmpty(), fCSPagination, nTotalMarkerCount.get(),
                            numberOfMarkersPerPage, variantsPage, nTotalCallsetsCount,
                            numberOfBioEntitiesPerPage, bioEntitiesPage, nSkipCount);

                // Step 2 — nQueryChunkSize is kept large enough to stay within
                // nMaxQueryChunkCount concurrent Mongo queries, but never smaller
                // than what computeQueryChunkSize recommends for document size.
                int nMaxQueryChunkCount = (int) mongoTemplate.getCollection(MgdbDao.COLLECTION_NAME_TAGGED_VARIANT_IDS).countDocuments();
                int nQueryChunkSize = Math.max(
                        (int) Math.ceil((float) pageVariantIDs.size() / nMaxQueryChunkCount),
                        Math.max(1, (int) IExportHandler.computeQueryChunkSize(mongoTemplate, pageVariantIDs.size())));
                int nChunkCount = (int) Math.ceil((float) pageVariantIDs.size() / nQueryChunkSize);

                @SuppressWarnings("unchecked")
                List<VariantRunData>[] chunkResults = new List[nChunkCount];

                final List<BasicDBObject> projectFilterList = buildProjectFilterList(body, fGotVariantSetList, mongoTemplate);
                final List<Integer> finalCallsetIds = Collections.unmodifiableList(callsetIds);
                final Integer finalAssemblyId = nAssemblyId;
                ExportManager.VariantRunDataComparator vrdComparator = new ExportManager.VariantRunDataComparator(nAssemblyId);

                ExecutorService executor = MongoTemplateManager.getExecutor(module);
                String taskGroup = "brapiMatrix_" + System.currentTimeMillis();

                @SuppressWarnings("unchecked")
                Future<Void>[] chunkTasks = new Future[nChunkCount];

                MongoCollection<VariantRunData> runColl = mongoTemplate.getDb()
                        .withCodecRegistry(ExportManager.pojoCodecRegistry)
                        .getCollection(mongoTemplate.getCollectionName(VariantRunData.class), VariantRunData.class);

                for (int ci = 0; ci < nChunkCount; ci++) {
                    final int chunkIndex = ci;
                    final List<String> chunkMarkerIDs = pageVariantIDs.subList(
                            ci * nQueryChunkSize, Math.min((ci + 1) * nQueryChunkSize, pageVariantIDs.size()));

                    Thread chunkThread = new Thread() {
                        public void run() {
                            try {
                                BasicDBList matchAndList = new BasicDBList();
                                if (!projectFilterList.isEmpty())
                                    matchAndList.add(projectFilterList.size() == 1
                                            ? projectFilterList.get(0)
                                            : new BasicDBObject("$or", projectFilterList));
                                matchAndList.add(new BasicDBObject(
                                        "_id." + VariantRunDataId.FIELDNAME_VARIANT_ID,
                                        new BasicDBObject("$in", chunkMarkerIDs)));

                                String refPosPath = Assembly.getVariantRefPosPath(finalAssemblyId);
                                Document projection = new Document();
                                projection.append(refPosPath, 1);
                                projection.append(AbstractVariantData.FIELDNAME_KNOWN_ALLELES, 1);
                                projection.append(AbstractVariantData.FIELDNAME_TYPE, 1);
                                projection.append(AbstractVariantData.SECTION_ADDITIONAL_INFO, 1);
                                for (Integer csId : finalCallsetIds)
                                    projection.append(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + csId, 1);

                                ArrayList<VariantRunData> runs = runColl.aggregate(Arrays.asList(
                                        new BasicDBObject("$match", new BasicDBObject("$and", matchAndList)),
                                        new BasicDBObject("$sort", new Document(refPosPath + "." + fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_SEQUENCE, 1)
                                                .append(refPosPath + "." + fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition.FIELDNAME_START_SITE, 1)),
                                        new BasicDBObject("$project", projection)
                                ), VariantRunData.class).allowDiskUse(true).into(new ArrayList<>(chunkMarkerIDs.size()));

                                Collections.sort(runs, vrdComparator);
                                chunkResults[chunkIndex] = runs;
                            } catch (Exception e) {
                                LOG.error("Error fetching VRD chunk " + chunkIndex, e);
                                chunkResults[chunkIndex] = new ArrayList<>();
                            }
                        }
                    };
                    chunkTasks[ci] = (Future<Void>) executor.submit(new TaskWrapper(taskGroup, chunkThread));
                }

                // Step 3 — wait for chunks in order; group consecutive rows by variantId
                LinkedHashMap<String, List<VariantRunData>> variantRunsByID = new LinkedHashMap<>(pageVariantIDs.size());
                for (String vid : pageVariantIDs)
                    variantRunsByID.put(vid, null);

                for (int ci = 0; ci < nChunkCount; ci++) {
                    chunkTasks[ci].get();
                    List<VariantRunData> chunkRuns = chunkResults[ci];
                    if (chunkRuns != null) {
                        String varId = null, previousVarId = null;
                        List<VariantRunData> currentMarkerRuns = new ArrayList<>();
                        for (VariantRunData vrd : chunkRuns) {
                            varId = vrd.getId().getVariantId();
                            if (previousVarId != null && !varId.equals(previousVarId)) {
                                variantRunsByID.put(previousVarId, currentMarkerRuns);
                                currentMarkerRuns = new ArrayList<>();
                            }
                            currentMarkerRuns.add(vrd);
                            previousVarId = varId;
                        }
                        if (!currentMarkerRuns.isEmpty())
                            variantRunsByID.put(previousVarId, currentMarkerRuns);
                    }
                    chunkResults[ci] = null;    // allow GC of completed chunk data
                }

                if (executor instanceof GroupedExecutor)
                    ((GroupedExecutor) executor).shutdown(taskGroup);

                // Merge multiple runs per variant into a single VariantRunData entry,
                // collecting variantSetDbIds from run metadata along the way
                for (Map.Entry<String, List<VariantRunData>> entry : variantRunsByID.entrySet()) {
                    List<VariantRunData> runs = entry.getValue();
                    if (runs == null || runs.isEmpty()) {
                        mergedByVariantId.put(entry.getKey(), null);
                        continue;
                    }
                    VariantRunData merged = runs.get(0);
                    if (runs.size() > 1) {
                        HashMap<Integer, SampleGenotype> mergedGenotypes = new HashMap<>();
                        for (VariantRunData vrd : runs) {
                            if (vrd.getSampleGenotypes() != null)
                                mergedGenotypes.putAll(vrd.getSampleGenotypes());
                            if (vrd.getRunName() != null)
                                variantSetDbIds.add(finalModule + Helper.ID_SEPARATOR + vrd.getId().getProjectId() + Helper.ID_SEPARATOR + vrd.getRunName());
                        }
                        merged.setSampleGenotypes(mergedGenotypes);
                    } else {
                        if (merged.getRunName() != null)
                            variantSetDbIds.add(finalModule + Helper.ID_SEPARATOR + merged.getId().getProjectId() + Helper.ID_SEPARATOR + merged.getRunName());
                    }
                    mergedByVariantId.put(entry.getKey(), merged);
                }

                varList = mergedByVariantId.isEmpty() ? new ArrayList<>() : Collections.singletonList(new VariantRunDataWithRuns()); // non-empty sentinel
            }

            // -------------------------------------------------------------------------
            // Shared matrix-building loop — identical for both paths.
            // Iterates mergedByVariantId in insertion order (= variant page order).
            // Phasing state (previousPhasingIds) requires strict variant order,
            // which is guaranteed by the LinkedHashMap.
            // -------------------------------------------------------------------------
            variantLoop:
            for (Map.Entry<String, VariantRunData> entry : mergedByVariantId.entrySet()) {
                String variantId = entry.getKey();
                VariantRunData vrd = entry.getValue();

                variantDbIds.add(finalModule + Helper.ID_SEPARATOR + variantId);

                if (!body.isPreview()) {
                    Map<String, List<String>> dataMap = new HashMap<>();
                    for (String key : matricesMap.keySet())
                        dataMap.put(key, new ArrayList<>());

                    Map<Integer, SampleGenotype> genotypes = (vrd != null && vrd.getSampleGenotypes() != null) ? vrd.getSampleGenotypes() : Collections.emptyMap();
                    List<String> knownAlleles = vrd != null ? vrd.getKnownAlleles() : null;

                    for (String aggKey : aggregationKeys) {
                        if (nTotalMarkerCount.get() == 0)
                            break variantLoop;

                        List<Integer> csIdsForKey = aggKeyToCallsets.get(aggKey);

                        // Collect GT strings for all callsets in this aggregation group
                        Map<String, Integer> gtFrequency = new LinkedHashMap<>();
                        String firstNonNullGt = null;
                        SampleGenotype representativeSg = null; // used for non-GT fields

                        for (Integer callSetId : csIdsForKey) {
                            SampleGenotype sg = genotypes.get(callSetId);
                            if (sg == null) continue;
                            if (representativeSg == null) representativeSg = sg;

                            String gtCode = sg.getCode();
                            if (gtCode == null || gtCode.length() == 0) continue;

                            List<String> alleles = fVcfStyleGenotypes
                                    ? Helper.split(gtCode, "/")
                                    : VariantData.staticGetAllelesFromGenotypeCode(knownAlleles, gtCode);
                            String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
                            boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(callSetId));
                            previousPhasingIds.put(callSetId, currentPhId == null ? variantId : currentPhId);

                            String rendered;
                            if (!Boolean.TRUE.equals(body.isExpandHomozygotes()) && new HashSet<>(alleles).size() == 1)
                                rendered = alleles.get(0);
                            else
                                rendered = String.join(fPhased ? phasedSeparator : unPhasedSeparator, alleles);

                            if (firstNonNullGt == null) firstNonNullGt = rendered;
                            gtFrequency.merge(rendered, 1, Integer::sum);
                        }

                        // Majority-vote: pick the most frequent genotype; if tie or all missing, use unknownGtCode
                        String majorityGt = unknownGtCode;
                        if (!gtFrequency.isEmpty()) {
                            int maxCount = gtFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(0);
                            long nWithMaxCount = gtFrequency.values().stream().filter(v -> v == maxCount).count();
                            if (nWithMaxCount == 1)
                                majorityGt = gtFrequency.entrySet().stream().filter(e -> e.getValue() == maxCount).findFirst().get().getKey();
                            // else: true tie → treat as missing (unknownGtCode)
                        }

                        for (String key : matricesMap.keySet()) {
                            if (!key.equals("GT")) {
                                // For non-GT fields use the first available representative callset
                                String val = unknownGtCode;
                                if (representativeSg != null) {
                                    Object v = representativeSg.getAdditionalInfo().get(key);
                                    if (v != null) val = v.toString();
                                }
                                dataMap.get(key).add(val);
                            } else {
                                dataMap.get(key).add(majorityGt);
                            }
                        }
                    }

                    for (String key : matricesMap.keySet())
                        matricesMap.get(key).getDataMatrix().add(dataMap.get(key));
                }
            }

            if (nTotalMarkerCount.get() == 0)
                matricesMap.values().forEach(dm -> dm.dataMatrix(new ArrayList<>()));

            result.setDataMatrices(new ArrayList<>(matricesMap.values()));

            countThread.join();

            // Route column identifiers to the correct response field based on aggregation level.
            if (colAgg == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.SAMPLE)
                result.setSampleDbIds(aggregationKeys);
            else if (colAgg == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.GERMPLASM)
                result.setGermplasmDbIds(aggregationKeys);
            else
            	result.setCallSetDbIds(callSetDbIds);
            List<String> responseCallSetIds = (colAgg == AlleleMatrixSearchRequest.DimensionColumnAggregationEnum.CALLSET) ? callSetDbIds : new ArrayList<>();

            // variantDbIds and variantSetDbIds are accumulated during the matrix loop
            result.setVariantDbIds(new ArrayList<>(variantDbIds));
            if (!variantSetDbIds.isEmpty())
                result.setVariantSetDbIds(new ArrayList<>(variantSetDbIds));
            else if (fGotVariantSetList)
                result.setVariantSetDbIds(body.getVariantSetDbIds());

            LOG.info("alleleMatrix took " + (System.currentTimeMillis() - before) / 1000d + "s");

            return buildFinalResponse(response, result, metadata, status,
                    !variantDbIds.isEmpty(), !sampleIDs.isEmpty(), fCSPagination,
                    nTotalMarkerCount.get(), numberOfMarkersPerPage, variantsPage,
                    nTotalAggregatedColumnCount, numberOfBioEntitiesPerPage, bioEntitiesPage, nSkipCount);

 

        } catch (MalformedParametersException | ResponseStatusException e) {
            Status status = new Status();
            status.setMessage(e.getMessage());
            metadata.addStatusItem(status);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper: build project filter list for chunk $match (mirrors ExportManager)
    // -------------------------------------------------------------------------
    private List<BasicDBObject> buildProjectFilterList(AlleleMatrixSearchRequest body, boolean fGotVariantSetList, MongoTemplate mongoTemplate) {
        List<BasicDBObject> projectFilterList = new ArrayList<>();
        if (fGotVariantSetList && body.getVariantSetDbIds() != null) {
            for (String vsId : body.getVariantSetDbIds()) {
                String[] info = Helper.getInfoFromId(vsId, 3);
                int projId = Integer.parseInt(info[1]);
                projectFilterList.add(new BasicDBObject("$and", Arrays.asList(
                        new BasicDBObject("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, projId),
                        new BasicDBObject("_id." + VariantRunDataId.FIELDNAME_RUNNAME, info[2])
                )));
            }
        }
        return projectFilterList;
    }

    // -------------------------------------------------------------------------
    // Helper: build matricesMap from VCF header metadata (extracted from inline)
    // -------------------------------------------------------------------------
    private Map<String, AlleleMatrixDataMatrices> buildMatricesMap(AlleleMatrixSearchRequest body, boolean fGotVariantSetList, MongoTemplate mongoTemplate, Collection<Integer> projectIDs) {
        Map<String, AlleleMatrixDataMatrices> matricesMap = new HashMap<>();
        if (body.getDataMatrixAbbreviations() == null || body.getDataMatrixAbbreviations().isEmpty())
            return matricesMap;

        Document filter = new Document();
        if (fGotVariantSetList) {
            List<Document> filtersList = new ArrayList<>();
            for (String vsId : body.getVariantSetDbIds()) {
                String[] info = Helper.getInfoFromId(vsId, 3);
                Document ifilter = new Document("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, Integer.parseInt(info[1]));
                ifilter.append("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_RUN, info[2]);
                filtersList.add(ifilter);
            }
            filter.put("$or", filtersList);
        } else {
            filter.put("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, new Document("$in", projectIDs));
        }

        MongoCollection<Document> vcfHeadersColl = mongoTemplate.getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));
        Document fields = new Document();
        boolean fReturnGenotypes = false;
        for (String key : body.getDataMatrixAbbreviations()) {
            fields.put(DBVCFHeader.FIELDNAME_FORMAT_METADATA + "." + key, 1);
            if ("GT".equals(key))
                fReturnGenotypes = true;
        }

        MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).projection(fields).iterator();
        while (headerCursor.hasNext()) {
            DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
            Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData();
            if (vcfMetadata != null) {
                for (String key : vcfMetadata.keySet()) {
                    if (!"GT".equals(key) && matricesMap.get(key) == null) {
                        AlleleMatrixDataMatrices matrix = new AlleleMatrixDataMatrices();
                        matrix.setDataMatrix(new ArrayList<>());
                        matrix.setDataMatrixAbbreviation(vcfMetadata.get(key).getID());
                        matrix.setDataMatrixName(vcfMetadata.get(key).getDescription());
                        VCFHeaderLineType type = vcfMetadata.get(key).getType();
                        matrix.setDataType(DataTypeEnum.fromValue(type.toString().toLowerCase()));
                        matricesMap.put(key, matrix);
                    }
                }
            }
        }

        if (fReturnGenotypes) {
            AlleleMatrixDataMatrices gtMatrix = new AlleleMatrixDataMatrices();
            gtMatrix.setDataMatrix(new ArrayList<>());
            gtMatrix.setDataMatrixAbbreviation("GT");
            gtMatrix.setDataMatrixName("Genotype");
            gtMatrix.setDataType(DataTypeEnum.STRING);
            matricesMap.put("GT", gtMatrix);
        }
        return matricesMap;
    }

//    // -------------------------------------------------------------------------
//    // Helper: set pagination and finalize response (for early-exit paths)
//    // -------------------------------------------------------------------------
//    private ResponseEntity<AlleleMatrixResponse> buildFinalResponse(
//            AlleleMatrixResponse response, AlleleMatrix result, Metadata metadata, Status status,
//            List<?> varList, List<String> callSetDbIds, Set<String> variantSetDbIds,
//            AlleleMatrixSearchRequest body, boolean fGotVariantSetList, HashSet<String> sampleIDs,
//            long totalMarkerCount, int numberOfMarkersPerPage, int variantsPage,
//            int nTotalCallsetsCount, int numberOfCallSetsPerPage, int callSetsPage, int nSkipCount) {
//        return buildFinalResponse(response, result, metadata, status, varList, callSetDbIds,
//                variantSetDbIds, body, fGotVariantSetList, sampleIDs, totalMarkerCount,
//                numberOfMarkersPerPage, variantsPage, nTotalCallsetsCount, numberOfCallSetsPerPage,
//                callSetsPage, nSkipCount, null, null);
//    }
// 
//    private ResponseEntity<AlleleMatrixResponse> buildFinalResponse(
//            AlleleMatrixResponse response, AlleleMatrix result, Metadata metadata, Status status,
//            List<?> varList, List<String> callSetDbIds, Set<String> variantSetDbIds,
//            AlleleMatrixSearchRequest body, boolean fGotVariantSetList, HashSet<String> sampleIDs,
//            long totalMarkerCount, int numberOfMarkersPerPage, int variantsPage,
//            int nTotalAggregatedColumnCount, int numberOfCallSetsPerPage, int callSetsPage, int nSkipCount,
//            List<String> variantDbIds, List<AlleleMatrixDataMatrices> dataMatrices) {
// 
//        if (dataMatrices != null)
//            result.setDataMatrices(dataMatrices);
// 
//        // Column pagination dimension and totalCount reflect the actual aggregation level.
//        // When aggregating at sample or germplasm level the total is the number of distinct
//        // aggregation keys across all pages, not the raw callset count.
//        String colAggForPagination = result.getGermplasmDbIds() != null && !result.getGermplasmDbIds().isEmpty() ? "germplasm" : result.getSampleDbIds() != null && !result.getSampleDbIds().isEmpty() ? "sample" : "callSet";
//        AlleleMatrixPagination.DimensionEnum colDimension = "germplasm".equals(colAggForPagination) ? AlleleMatrixPagination.DimensionEnum.GERMPLASM : "sample".equals(colAggForPagination) ? AlleleMatrixPagination.DimensionEnum.SAMPLES : AlleleMatrixPagination.DimensionEnum.CALLSETS;
// 
//        AlleleMatrixPagination bioEntityPagination = new AlleleMatrixPagination();
//        bioEntityPagination.setDimension(colDimension);
//        bioEntityPagination.setPage(callSetsPage);
//        bioEntityPagination.setPageSize(numberOfCallSetsPerPage);
//        bioEntityPagination.setTotalCount(nTotalAggregatedColumnCount);
//        int nbOfCallSetPages = nTotalAggregatedColumnCount / numberOfCallSetsPerPage + (nTotalAggregatedColumnCount % numberOfCallSetsPerPage > 0 ? 1 : 0);
//        bioEntityPagination.setTotalPages(nbOfCallSetPages);
// 
//        AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
//        variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
//        variantPagination.setPage(variantsPage);
//        variantPagination.setPageSize(numberOfMarkersPerPage);
//        variantPagination.setTotalCount((int) totalMarkerCount);
//        int nbOfPages = (int) totalMarkerCount / numberOfMarkersPerPage + (totalMarkerCount % numberOfMarkersPerPage > 0 ? 1 : 0);
//        variantPagination.setTotalPages(nbOfPages);
//        result.setPagination(Arrays.asList(variantPagination, bioEntityPagination));
// 
//        if (!varList.isEmpty()) {
//            if (variantDbIds != null)
//                result.setVariantDbIds(variantDbIds);
//            if (!variantSetDbIds.isEmpty())
//                result.setVariantSetDbIds(new ArrayList<>(variantSetDbIds));
//            else if (fGotVariantSetList)
//                result.setVariantSetDbIds(body.getVariantSetDbIds());
//        } else {
//            if (nSkipCount > totalMarkerCount) {
//                status.setMessage("Requested variant page does not exist");
//                status.setMessageType(Status.MessageTypeEnum.INFO);
//            } else if (sampleIDs.isEmpty()) {
//                status.setMessage("Requested callSet page does not exist");
//                status.setMessageType(Status.MessageTypeEnum.INFO);
//            }
//        }
// 
//        if (body.isPreview())
//            return new ResponseEntity<>(response, HttpStatus.OK);
// 
//        metadata.addStatusItem(status);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
// 
//    public ResponseEntity<AlleleMatrixResponse> returnEmptyMatrix(AlleleMatrixResponse response, int variantsPage, int variantsPageSize, int callSetsPage, int callSetsPageSize) {
//        AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
//        variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
//        variantPagination.setPage(variantsPage);
//        variantPagination.setPageSize(variantsPageSize);
//        variantPagination.setTotalCount(0);
//        variantPagination.setTotalPages(0);
// 
//        AlleleMatrixPagination callSetPagination = new AlleleMatrixPagination();
//        callSetPagination.setDimension(AlleleMatrixPagination.DimensionEnum.CALLSETS);
//        callSetPagination.setPage(callSetsPage);
//        callSetPagination.setPageSize(callSetsPageSize);
//        callSetPagination.setTotalCount(0);
//        callSetPagination.setTotalPages(0);
// 
//        response.getResult().setPagination(Arrays.asList(variantPagination, callSetPagination));
//        response.getResult().setCallSetDbIds(new ArrayList<>());
//        response.getResult().setVariantDbIds(new ArrayList<>());
//        response.getResult().setVariantSetDbIds(new ArrayList<>());
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
    
    // -------------------------------------------------------------------------
    // Helper: set pagination and finalize response.
    //
    // Column IDs (callSetDbIds / sampleDbIds / germplasmDbIds), variantDbIds,
    // variantSetDbIds, and dataMatrices are set on `result` by the caller before
    // this method is invoked — this method only handles pagination and status messages.
    //
    // fGotVariants — true when the main data path produced at least one variant row
    // fGotColumns  — true when at least one column-dimension filter resolved to results
    //                (used to distinguish "no variants on this page" from "no columns")
    // -------------------------------------------------------------------------
    private ResponseEntity<AlleleMatrixResponse> buildFinalResponse(
            AlleleMatrixResponse response, AlleleMatrix result, Metadata metadata, Status status,
            boolean fGotVariants, boolean fGotColumns, boolean fCSPagination,
            long totalMarkerCount, int numberOfMarkersPerPage, int variantsPage,
            int nTotalColumnCount, int numberOfColumnsPerPage, int columnsPage, int nSkipCount) {

        AlleleMatrixPagination.DimensionEnum colDimension =
                fCSPagination ? AlleleMatrixPagination.DimensionEnum.CALLSETS : AlleleMatrixPagination.DimensionEnum.COLUMNS;
 
        AlleleMatrixPagination columnPagination = new AlleleMatrixPagination();
        columnPagination.setDimension(colDimension);
        columnPagination.setPage(columnsPage);
        columnPagination.setPageSize(numberOfColumnsPerPage);
        columnPagination.setTotalCount(nTotalColumnCount);
        columnPagination.setTotalPages(nTotalColumnCount / numberOfColumnsPerPage + (nTotalColumnCount % numberOfColumnsPerPage > 0 ? 1 : 0));
 
        AlleleMatrixPagination variantPagination = new AlleleMatrixPagination();
        variantPagination.setDimension(AlleleMatrixPagination.DimensionEnum.VARIANTS);
        variantPagination.setPage(variantsPage);
        variantPagination.setPageSize(numberOfMarkersPerPage);
        variantPagination.setTotalCount((int) totalMarkerCount);
        variantPagination.setTotalPages((int) totalMarkerCount / numberOfMarkersPerPage + (totalMarkerCount % numberOfMarkersPerPage > 0 ? 1 : 0));
        result.setPagination(Arrays.asList(variantPagination, columnPagination));
 
        if (!fGotVariants) {
            if (nSkipCount > totalMarkerCount) {
                status.setMessage("Requested variant page does not exist");
                status.setMessageType(Status.MessageTypeEnum.INFO);
            } else if (!fGotColumns) {
                String str = fCSPagination ? "callset" :  "column";
                status.setMessage("Requested " + str + " page does not exist");
                status.setMessageType(Status.MessageTypeEnum.INFO);
            }
        }
 
        metadata.addStatusItem(status);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private class VariantRunDataWithRuns extends VariantRunData {
        public final static String FIELDNAME_RUNS = "r";

        @org.springframework.data.mongodb.core.mapping.Field(FIELDNAME_RUNS)
        private Set<Run> runs = new HashSet<>();

        public Set<Run> getRuns() {
            return runs;
        }
    }
}