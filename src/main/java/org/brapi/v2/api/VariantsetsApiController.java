package org.brapi.v2.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.brapi.v2.api.cache.MongoBrapiCache;
import org.brapi.v2.model.CallsListResponse;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetAvailableFormats.DataFormatEnum;
import org.brapi.v2.model.VariantSetListResponse;
import org.brapi.v2.model.VariantSetListResponseResult;
import org.brapi.v2.model.VariantSetResponse;
import org.brapi.v2.model.VariantSetsSearchRequest;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.ServletContextAware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;

import fr.cirad.mgdb.exporting.IExportHandler;
import fr.cirad.mgdb.exporting.individualoriented.AbstractIndividualOrientedExportHandler;
import fr.cirad.mgdb.exporting.individualoriented.FlapjackExportHandler;
import fr.cirad.mgdb.exporting.individualoriented.PLinkExportHandler;
import fr.cirad.mgdb.exporting.markeroriented.AbstractMarkerOrientedExportHandler;
import fr.cirad.mgdb.exporting.markeroriented.VcfExportHandler;
import fr.cirad.mgdb.exporting.tools.ExportManager;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition;
import fr.cirad.tools.AlphaNumericComparator;
import fr.cirad.tools.Helper;
import fr.cirad.tools.ProgressIndicator;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.variant.variantcontext.VariantContext.Type;
import htsjdk.variant.variantcontext.writer.CustomVCFWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@Controller
public class VariantsetsApiController implements ServletContextAware, VariantsetsApi {

    private static final Logger log = LoggerFactory.getLogger(VariantsetsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
        
    @Autowired private AbstractTokenManager tokenManager;
    
    @Autowired private CallsApiController callsApiController;
       
    @Autowired private MongoBrapiCache cache;
    
	private ServletContext servletContext;

	static final private long EXPORT_FILE_EXPIRATION_DELAY_MILLIS = 1000*60*60*24;	/* 1 day */

    private static HashMap<String /*export id*/, Thread /*temporary file generation thread */> exportThreads = new HashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public VariantsetsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }
    
    /* study IDs have 2 levels: module+project, variantSet IDs have 3 levels: module+project+run */
    private Map<String /* module */, Map<Integer /* project */, List<String> /* runs */>> parseVariantSetOrStudyDbIDs(List<String> variantSetOrStudyDbIds) {
    	Map<String, Map<Integer, List<String>>> result = new HashMap<>();
    	if (variantSetOrStudyDbIds != null)
	    	for (String variantSetOrStudyId : variantSetOrStudyDbIds) {
	    		String[] splitId = variantSetOrStudyId.split(Helper.ID_SEPARATOR);
	    		Map<Integer, List<String>> moduleProjectsAndRuns = result.get(splitId[0]);
	    		if (moduleProjectsAndRuns == null) {
	    			moduleProjectsAndRuns = new HashMap<>();
	    			result.put(splitId[0], moduleProjectsAndRuns);
	    		}
	    		int pjId = Integer.parseInt(splitId[1]);
	    		List<String> projectRuns = moduleProjectsAndRuns.get(pjId);
	    		if (projectRuns == null) {
	    			projectRuns = new ArrayList<>();
	    			moduleProjectsAndRuns.put(pjId, projectRuns);
	    		}
	    		if (splitId.length == 3)	// otherwise it was a study ID: the run list will remain empty, meaning all of them are requested 
	    			projectRuns.add(splitId[2]);
	    	}
    	return result;
    }
    
    public ResponseEntity<VariantSetListResponse> searchVariantsetsPost(@ApiParam(value = "Variantset Search request") @Valid @RequestBody VariantSetsSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
		HttpStatus httpCode = null;

        try {
        	VariantSetListResponse vslr = new VariantSetListResponse();
			Metadata metadata = new Metadata();
			vslr.setMetadata(metadata);
        	VariantSetListResponseResult result = new VariantSetListResponseResult();
	        int pageToken = 0;

	        if ((body.getVariantSetDbIds() != null || body.getStudyDbIds() != null) || body.getCallSetDbIds() == null) {
	    		List<String> relevantIDs = body.getVariantSetDbIds() != null ? body.getVariantSetDbIds() : body.getStudyDbIds();
	    		Map<String /*module*/, Map<Integer /*project*/, List<String> /*runs (all if null)*/>> variantSetDbIDsByStudyAndRefSet = parseVariantSetOrStudyDbIDs(relevantIDs);
	        
		        for (String programDbId : variantSetDbIDsByStudyAndRefSet.isEmpty() ? MongoTemplateManager.getAvailableModules() : variantSetDbIDsByStudyAndRefSet.keySet()) {
	    			MongoTemplate mongoTemplate = MongoTemplateManager.get(programDbId);
	    			Collection<Integer> allowedPjIDs = new HashSet<>();
		        	Map<Integer, List<String>> variantSetDbIDsByStudy = variantSetDbIDsByStudyAndRefSet.get(programDbId);
		        	for (int pjId : variantSetDbIDsByStudy != null ? variantSetDbIDsByStudy.keySet() : mongoTemplate.findDistinct("_id", GenotypingProject.class, Integer.class))
	            		if (tokenManager.canUserReadProject(token, programDbId, pjId))
	            			allowedPjIDs.add(pjId);
	
	    	        Query q = new Query(Criteria.where("_id").in(allowedPjIDs));
	    	        q.fields().include(GenotypingProject.FIELDNAME_RUNS);
	    	        for (GenotypingProject proj : mongoTemplate.find(q, GenotypingProject.class)) {
	    	        	List<String> wantedProjectRuns = variantSetDbIDsByStudy == null ? new ArrayList<>() : variantSetDbIDsByStudy.get(proj.getId());
	    	        	for (String run : proj.getRuns())
		    	        	if (wantedProjectRuns.isEmpty() || wantedProjectRuns.contains(run)) {
		    	        		VariantSet variantSet = cache.getVariantSet(mongoTemplate, programDbId + Helper.ID_SEPARATOR + proj.getId() + Helper.ID_SEPARATOR + run);
			    	            result.addDataItem(variantSet);
		    	        	}
	    	        }
		        }
		    }
	        else {	// no study or variantSet specified, but we have a list of callSets
	        	HashMap<String /*module*/, HashSet<Integer> /*samples*/> samplesByModule = new HashMap<>();
				for (String csId : body.getCallSetDbIds()) {
					String[] info = Helper.getInfoFromId(csId, 2);
					HashSet<Integer> moduleSamples = samplesByModule.get(info[0]);
					if (moduleSamples == null) {
						moduleSamples = new HashSet<>();
						samplesByModule.put(info[0], moduleSamples);
					}
					moduleSamples.add(Integer.parseInt(info[1]));
				}
				HashSet<String> addedVariantSets = new HashSet<>();	// will be used to avoid adding the same variantSet several times
	        	for (String module : samplesByModule.keySet()) {
	        		MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
	    	        for (GenotypingSample sample : mongoTemplate.find(new Query(Criteria.where("_id").in(samplesByModule.get(module))), GenotypingSample.class)) {
	    	        	String variantSetDbId = module + Helper.ID_SEPARATOR + sample.getProjectId() + Helper.ID_SEPARATOR + sample.getRun();
	    	        	if (!addedVariantSets.contains(variantSetDbId)) {
	    	        		VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);
		    	            result.addDataItem(variantSet);
		    	            addedVariantSets.add(variantSetDbId);
	    	        	}
	    	        }
	        	}
        	}
        	
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(pageToken);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);

			vslr.setResult(result);
            return new ResponseEntity<VariantSetListResponse>(vslr, httpCode == null ? HttpStatus.OK : httpCode);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<VariantSetListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//
//    public ResponseEntity<VariantSetResponse> variantsetsExtractPost(@ApiParam(value = "Study Search request"  )  @Valid @RequestBody VariantSetsExtractRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<VariantSetResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"availableFormats\" : [ {\n      \"dataFormat\" : \"DartSeq\",\n      \"fileURL\" : \"http://example.com/aeiou\",\n      \"fileFormat\" : \"text/csv\"\n    }, {\n      \"dataFormat\" : \"DartSeq\",\n      \"fileURL\" : \"http://example.com/aeiou\",\n      \"fileFormat\" : \"text/csv\"\n    } ],\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"variantSetDbId\" : \"variantSetDbId\",\n    \"callSetCount\" : 0,\n    \"VariantSetDbId\" : \"VariantSetDbId\",\n    \"variantSetName\" : \"variantSetName\",\n    \"analysis\" : [ {\n      \"software\" : [ \"software\", \"software\" ],\n      \"analysisDbId\" : \"analysisDbId\",\n      \"created\" : \"created\",\n      \"description\" : \"description\",\n      \"type\" : \"type\",\n      \"updated\" : \"updated\",\n      \"analysisName\" : \"analysisName\"\n    }, {\n      \"software\" : [ \"software\", \"software\" ],\n      \"analysisDbId\" : \"analysisDbId\",\n      \"created\" : \"created\",\n      \"description\" : \"description\",\n      \"type\" : \"type\",\n      \"updated\" : \"updated\",\n      \"analysisName\" : \"analysisName\"\n    } ],\n    \"studyDbId\" : \"studyDbId\",\n    \"variantCount\" : 6\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantSetResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<VariantSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<VariantSetResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
	
	@Override
	public ResponseEntity<VariantSetListResponse> variantsetsGet(String variantSetDbId, String variantDbId, String callSetDbId, String studyDbId, String studyName, Integer page, Integer pageSize, String authorization) {
		VariantSetsSearchRequest body = new VariantSetsSearchRequest();
		if (variantSetDbId != null)
			body.setVariantSetDbIds(Arrays.asList(variantSetDbId));
		if (variantDbId != null)
			body.setVariantDbIds(Arrays.asList(variantDbId));
		if (callSetDbId != null)
			body.setCallSetDbIds(Arrays.asList(callSetDbId));
		if (studyDbId != null)
			body.setStudyDbIds(Arrays.asList(studyDbId));
		if (studyName != null)
			body.setStudyNames(Arrays.asList(studyName));
		if (page != null)
			body.setPage(page);
		if (pageSize != null)
			body.setPageSize(pageSize);
		return searchVariantsetsPost(body, authorization);
	}
	
	@Override
	public ResponseEntity<CallsListResponse> variantsetsVariantSetDbIdCallsGet(String variantSetDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, Integer page, Integer pageSize, String authorization) throws UnsupportedEncodingException, SocketException, UnknownHostException {
		CallsSearchRequest csr = new CallsSearchRequest();
		csr.setExpandHomozygotes(expandHomozygotes);
		csr.setUnknownString(unknownString);
		csr.setSepUnphased(sepUnphased);
		csr.setSepPhased(sepPhased);
		csr.setPageSize(pageSize);
		csr.setPage(page);
		csr.setVariantSetDbIds(Arrays.asList(variantSetDbId));
		
		return callsApiController.searchCallsPost(authorization, csr);
	}
	
//    public ResponseEntity<CallListResponse> variantsetsVariantSetDbIdCallsGet(@ApiParam(value = "The ID of the `VariantSet` to be retrieved.",required=true) @PathVariable("variantSetDbId") String variantSetDbId,@ApiParam(value = "Should homozygotes be expanded (true) or collapsed into a single occurence (false)") @Valid @RequestParam(value = "expandHomozygotes", required = false) Boolean expandHomozygotes,@ApiParam(value = "The string to use as a representation for missing data") @Valid @RequestParam(value = "unknownString", required = false) String unknownString,@ApiParam(value = "The string to use as a separator for phased allele calls") @Valid @RequestParam(value = "sepPhased", required = false) String sepPhased,@ApiParam(value = "The string to use as a separator for unphased allele calls") @Valid @RequestParam(value = "sepUnphased", required = false) String sepUnphased,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<CallListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    }, {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    } ],\n    \"unknownString\" : \"unknownString\",\n    \"expandHomozygotes\" : true,\n    \"sepPhased\" : \"sepPhased\",\n    \"sepUnphased\" : \"sepUnphased\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<CallListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<CallSetsListResponse> variantsetsVariantSetDbIdCallsetsGet(@ApiParam(value = "The ID of the `VariantSet` to be retrieved.",required=true) @PathVariable("variantSetDbId") String variantSetDbId,@ApiParam(value = "The ID of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetDbId", required = false) String callSetDbId,@ApiParam(value = "The human readbale name of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetName", required = false) String callSetName,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<CallSetsListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetDbIds\" : [ \"variantSetDbIds\", \"variantSetDbIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    }, {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetDbIds\" : [ \"variantSetDbIds\", \"variantSetDbIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetsListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<CallSetsListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<CallSetsListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<VariantSetResponse> variantsetsVariantSetDbIdGet(@ApiParam(value = "The ID of the `Variant Set` to be retrieved.",required=true) @PathVariable("variantSetDbId") String variantSetDbId,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<VariantSetResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"availableFormats\" : [ {\n      \"dataFormat\" : \"DartSeq\",\n      \"fileURL\" : \"http://example.com/aeiou\",\n      \"fileFormat\" : \"text/csv\"\n    }, {\n      \"dataFormat\" : \"DartSeq\",\n      \"fileURL\" : \"http://example.com/aeiou\",\n      \"fileFormat\" : \"text/csv\"\n    } ],\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"variantSetDbId\" : \"variantSetDbId\",\n    \"callSetCount\" : 0,\n    \"VariantSetDbId\" : \"VariantSetDbId\",\n    \"variantSetName\" : \"variantSetName\",\n    \"analysis\" : [ {\n      \"software\" : [ \"software\", \"software\" ],\n      \"analysisDbId\" : \"analysisDbId\",\n      \"created\" : \"created\",\n      \"description\" : \"description\",\n      \"type\" : \"type\",\n      \"updated\" : \"updated\",\n      \"analysisName\" : \"analysisName\"\n    }, {\n      \"software\" : [ \"software\", \"software\" ],\n      \"analysisDbId\" : \"analysisDbId\",\n      \"created\" : \"created\",\n      \"description\" : \"description\",\n      \"type\" : \"type\",\n      \"updated\" : \"updated\",\n      \"analysisName\" : \"analysisName\"\n    } ],\n    \"studyDbId\" : \"studyDbId\",\n    \"variantCount\" : 6\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantSetResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<VariantSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<VariantSetResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
    
//    public ResponseEntity<VariantListResponse> variantsetsVariantSetDbIdVariantsGet(@ApiParam(value = "The ID of the `VariantSet` to be retrieved.",required=true) @PathVariable("variantSetDbId") String variantSetDbId,@ApiParam(value = "The ID of the `Variant` to be retrieved.") @Valid @RequestParam(value = "variantDbId", required = false) String variantDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<VariantListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"created\" : \"1573671122\",\n      \"referenceBases\" : \"ATCGATTGAGCTCTAGCG\",\n      \"start\" : \"500\",\n      \"cipos\" : [ -12000, 1000 ],\n      \"variantType\" : \"DUP\",\n      \"ciend\" : [ -1000, 0 ],\n      \"alternate_bases\" : [ \"TAGGATTGAGCTCTATAT\" ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : [ \"c8ae400b\", \"ef2c204b\" ],\n      \"filtersFailed\" : [ \"d629a669\", \"3f14f578\" ],\n      \"svlen\" : \"1500\",\n      \"variantDbId\" : \"628e89c5\",\n      \"variantNames\" : [ \"RefSNP_ID_1\", \"06ea312e\" ],\n      \"end\" : \"518\",\n      \"filtersApplied\" : true,\n      \"filtersPassed\" : true,\n      \"updated\" : \"1573672019\",\n      \"referenceName\" : \"chr20\"\n    }, {\n      \"created\" : \"1573671122\",\n      \"referenceBases\" : \"ATCGATTGAGCTCTAGCG\",\n      \"start\" : \"500\",\n      \"cipos\" : [ -12000, 1000 ],\n      \"variantType\" : \"DUP\",\n      \"ciend\" : [ -1000, 0 ],\n      \"alternate_bases\" : [ \"TAGGATTGAGCTCTATAT\" ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : [ \"c8ae400b\", \"ef2c204b\" ],\n      \"filtersFailed\" : [ \"d629a669\", \"3f14f578\" ],\n      \"svlen\" : \"1500\",\n      \"variantDbId\" : \"628e89c5\",\n      \"variantNames\" : [ \"RefSNP_ID_1\", \"06ea312e\" ],\n      \"end\" : \"518\",\n      \"filtersApplied\" : true,\n      \"filtersPassed\" : true,\n      \"updated\" : \"1573672019\",\n      \"referenceName\" : \"chr20\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<VariantListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<VariantListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
	@Override
	public ResponseEntity<VariantSetResponse> variantsetsVariantSetDbIdGet(String variantSetDbId, String authorization) {
		String token = ServerinfoApiController.readToken(authorization);
        try {
        	VariantSetResponse rlr = new VariantSetResponse();
			Metadata metadata = new Metadata();
			rlr.setMetadata(metadata);
        	String[] splitId = Helper.getInfoFromId(variantSetDbId, 3);
        	
    		if (!tokenManager.canUserReadProject(token, splitId[0], Integer.parseInt(splitId[1]))) {
				Status status = new Status();
				status.setMessage("You are not allowed to access this content!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(rlr, HttpStatus.FORBIDDEN);
			}
    		
			MongoTemplate mongoTemplate = MongoTemplateManager.get(splitId[0]);
			VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);

			rlr.setResult(variantSet);
            return new ResponseEntity<VariantSetResponse>(rlr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<VariantSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@Override
	public void variantsetsExportIntoFormat(HttpServletResponse response, String variantSetDbId, String dataFormat, String authorization) throws Exception {
		String token = ServerinfoApiController.readToken(authorization);
		String[] info = Helper.getInfoFromId(variantSetDbId, 3);
		if (!tokenManager.canUserReadProject(token, info[0], Integer.parseInt(info[1]))){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("You are not allowed to access this content");
			return;
		}
		
		cleanupOldExportData(request);
		if (dataFormat.equalsIgnoreCase(DataFormatEnum.PLINK.toString())) {
			PLinkExportHandler exportHandler = (PLinkExportHandler) AbstractIndividualOrientedExportHandler.getIndividualOrientedExportHandlers().get(dataFormat.toUpperCase());
			
        	String[] splitId = variantSetDbId.split(Helper.ID_SEPARATOR);
        	MongoTemplate mongoTemplate = MongoTemplateManager.get(splitId[0]);
        	int projId = Integer.parseInt(splitId[1]);
        	String exportId = VariantSet.brapiV2ExportFilePrefix  + variantSetDbId;
        	
			String relativeOutputFolder = File.separator + VariantSet.TMP_OUTPUT_FOLDER + File.separator;
			File outputLocation = new File(servletContext.getRealPath(relativeOutputFolder));
			if (!outputLocation.exists() && !outputLocation.mkdirs())
				throw new Exception("Unable to create folder: " + outputLocation);
        	File exportFile = new File(servletContext.getRealPath(relativeOutputFolder + exportId + ".genotype"));

        	PLinkExportThread tempFileGenerationThread = (PLinkExportThread) exportThreads.get(exportId);
        	if (tempFileGenerationThread == null) {	// job is not running: either complete or not started
            	List<GenotypingSample> runSamples = mongoTemplate.find(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class);
        		
        		if (exportFile.exists()) {	// seems complete
        			HashMap<String, Integer> individualToSampleMap = new HashMap<>();
        			for (GenotypingSample sp : runSamples)
        				individualToSampleMap.put(sp.getIndividual(), sp.getId());

        			Scanner scanner = new Scanner(exportFile);
        			while (scanner.hasNextLine()) {	// iterate over lines to replace individual names with callsetDdIDs
        				String sLine  = scanner.nextLine();
    					int nFirstSpacePos = sLine.indexOf(" "), nSecondSpacePos = sLine.indexOf(" ", nFirstSpacePos + 1);
    					String ind = sLine.substring(nFirstSpacePos + 1, nSecondSpacePos);
    					response.getWriter().write(sLine.substring(0, nFirstSpacePos + 1) + splitId[0] + Helper.ID_SEPARATOR + individualToSampleMap.get(ind) + sLine.substring(nSecondSpacePos) + "\n");
        			}
        			scanner.close();
        			response.getWriter().close();
        			return;
        		}
        		
    			VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);
    			if (variantSet == null) {
        			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        			response.getWriter().write("No data found for this variantSet");
        			return;
    			}

        		// start it
            	ProgressIndicator progress = new ProgressIndicator(exportId, new String[] {"Reading and re-organizing genotypes"});
            	ProgressIndicator.registerProgressIndicator(progress);
            	
        		tempFileGenerationThread = new PLinkExportThread(exportHandler, exportFile, variantSet, exportId, runSamples, progress);
        		exportThreads.put(exportId, tempFileGenerationThread);
        		tempFileGenerationThread.start();
        	}

        	if (tempFileGenerationThread.isAlive()) {
    			response.setStatus(HttpServletResponse.SC_ACCEPTED);
    			response.getWriter().write(tempFileGenerationThread.getProgress().getProgressDescription());
        	}
        	else if (tempFileGenerationThread.getException() != null) {
    			exportThreads.remove(exportId);
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			log.error("Error generating PLINK file", tempFileGenerationThread.getException());
    			tempFileGenerationThread.getException().printStackTrace(response.getWriter());
    			exportFile.delete();
    		}
		}
		else if (dataFormat.equalsIgnoreCase(DataFormatEnum.FLAPJACK.toString())) {
			FlapjackExportHandler exportHandler = (FlapjackExportHandler) AbstractIndividualOrientedExportHandler.getIndividualOrientedExportHandlers().get(dataFormat.toUpperCase());
			
        	String[] splitId = variantSetDbId.split(Helper.ID_SEPARATOR);
        	MongoTemplate mongoTemplate = MongoTemplateManager.get(splitId[0]);
        	int projId = Integer.parseInt(splitId[1]);
        	String exportId = VariantSet.brapiV2ExportFilePrefix + variantSetDbId;
        	
			String relativeOutputFolder = File.separator + VariantSet.TMP_OUTPUT_FOLDER + File.separator;
			File outputLocation = new File(servletContext.getRealPath(relativeOutputFolder));
			if (!outputLocation.exists() && !outputLocation.mkdirs())
				throw new Exception("Unable to create folder: " + outputLocation);
        	File exportFile = new File(servletContext.getRealPath(relativeOutputFolder + exportId + ".genotype"));

        	FlapjackExportThread tempFileGenerationThread = (FlapjackExportThread) exportThreads.get(exportId);
        	if (tempFileGenerationThread == null) {	// job is not running: either complete or not started
            	List<GenotypingSample> runSamples = mongoTemplate.find(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class);
        		
        		if (exportFile.exists()) {	// seems complete
        			HashMap<String, Integer> individualToSampleMap = new HashMap<>();
        			for (GenotypingSample sp : runSamples)
        				individualToSampleMap.put(sp.getIndividual(), sp.getId());

        			Scanner scanner = new Scanner(exportFile);
        			int i = 0;
        			while (scanner.hasNextLine()) {	// iterate over lines to replace individual names with callsetDdIDs
        				String sLine  = scanner.nextLine();
        				if (++i <= 2)
        					response.getWriter().write(sLine + "\n");	// header or marker line
        				else {
        					int nFirstTabPos = sLine.indexOf("\t");
        					String ind = sLine.substring(0, nFirstTabPos);
        					response.getWriter().write(splitId[0] + Helper.ID_SEPARATOR + individualToSampleMap.get(ind) + sLine.substring(nFirstTabPos) + "\n");
        				}
        			}
        			scanner.close();
        			response.getWriter().close();
        			return;
        		}
        		
    			VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);
    			if (variantSet == null) {
        			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        			response.getWriter().write("No data found for this variantSet");
        			return;
    			}

        		// start it
            	ProgressIndicator progress = new ProgressIndicator(exportId, new String[] {"Reading and re-organizing genotypes"});
            	ProgressIndicator.registerProgressIndicator(progress);
            	
        		tempFileGenerationThread = new FlapjackExportThread(exportHandler, exportFile, variantSet, exportId, runSamples, progress);
        		exportThreads.put(exportId, tempFileGenerationThread);
        		tempFileGenerationThread.start();
        	}

        	if (tempFileGenerationThread.isAlive()) {
    			response.setStatus(HttpServletResponse.SC_ACCEPTED);
    			response.getWriter().write(tempFileGenerationThread.getProgress().getProgressDescription());
        	}
        	else if (tempFileGenerationThread.getException() != null) {
    			exportThreads.remove(exportId);
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			log.error("Error generating Flapjack file", tempFileGenerationThread.getException());
    			tempFileGenerationThread.getException().printStackTrace(response.getWriter());
    			exportFile.delete();
    		}
		}
		else if (dataFormat.equalsIgnoreCase(DataFormatEnum.VCF.toString())) {
			VcfExportHandler exportHandler = (VcfExportHandler) AbstractMarkerOrientedExportHandler.getMarkerOrientedExportHandlers().get(dataFormat.toUpperCase());
			
        	String[] splitId = variantSetDbId.split(Helper.ID_SEPARATOR);
        	MongoTemplate mongoTemplate = MongoTemplateManager.get(splitId[0]);
        	int projId = Integer.parseInt(splitId[1]);
        	String exportId = VariantSet.brapiV2ExportFilePrefix + variantSetDbId;
        	
			String relativeOutputFolder = File.separator + VariantSet.TMP_OUTPUT_FOLDER + File.separator;
			File outputLocation = new File(servletContext.getRealPath(relativeOutputFolder));
			if (!outputLocation.exists() && !outputLocation.mkdirs())
				throw new Exception("Unable to create folder: " + outputLocation);
        	File exportFile = new File(servletContext.getRealPath(relativeOutputFolder + exportId + ".vcf"));

        	VcfExportThread tempFileGenerationThread = (VcfExportThread) exportThreads.get(exportId);
        	if (tempFileGenerationThread == null) {	// job is not running: either complete or not started
            	List<GenotypingSample> runSamples = mongoTemplate.find(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class);
        		
        		if (exportFile.exists()) {	// seems complete
        			HashMap<String, Integer> individualToSampleMap = new HashMap<>();
        			for (GenotypingSample sp : runSamples)
        				individualToSampleMap.put(sp.getIndividual(), sp.getId());

        			Scanner scanner = new Scanner(exportFile);
        			while (scanner.hasNextLine()) {	// iterate over lines to replace individual names with callsetDdIDs
        				String sLine  = scanner.nextLine();
        				if (!sLine.startsWith("#CHROM\t"))
        					response.getWriter().write(sLine + "\n");	// header or marker line
        				else {
        					String[] splitLine = sLine.split("\t");
        					long nIndCount = runSamples.stream().map(gs -> gs.getIndividual()).distinct().count();
        					for (int i=0; i<nIndCount; i++)
        						splitLine[splitLine.length - 1 - i] = splitId[0] + Helper.ID_SEPARATOR /*+ splitLine[splitLine.length - 1 - i] + Helper.ID_SEPARATOR*/ + individualToSampleMap.get(splitLine[splitLine.length - 1 - i]);
        					for (int i=0; i<splitLine.length; i++)
        						response.getWriter().write(splitLine[i] + (i == splitLine.length - 1 ? "\n" : "\t"));
        				}
        			}
        			scanner.close();
        			response.getWriter().close();
        			return;
        		}
        		
    			VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);
    			if (variantSet == null) {
        			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        			response.getWriter().write("No data found for this variantSet");
        			return;
    			}

        		// start it
            	ProgressIndicator progress = new ProgressIndicator(exportId, new String[0]);
            	ProgressIndicator.registerProgressIndicator(progress);

        		tempFileGenerationThread = new VcfExportThread(exportHandler, exportFile, variantSet, exportId, runSamples, progress);
        		exportThreads.put(exportId, tempFileGenerationThread);
        		tempFileGenerationThread.start();
        	}

        	if (tempFileGenerationThread.isAlive()) {
    			response.setStatus(HttpServletResponse.SC_ACCEPTED);
    			response.getWriter().write(tempFileGenerationThread.getProgress().getProgressDescription());
        	}
        	else if (tempFileGenerationThread.getException() != null) {
    			exportThreads.remove(exportId);
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			log.error("Error generating VCF file", tempFileGenerationThread.getException());
    			tempFileGenerationThread.getException().printStackTrace(response.getWriter());
    			exportFile.delete();
    		}
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Unsupported data format: " + dataFormat);
		}
	}
	
	public class PLinkExportThread extends Thread {
		Exception exception = null;
		File[] result;
		PLinkExportHandler exportHandler;
		File exportFile;
		MongoCollection<Document> varColl;
		String exportId;
		List<GenotypingSample> samplesToExport;
		ProgressIndicator progress;
		VariantSet variantSet;
		
		PLinkExportThread(PLinkExportHandler feh, File exportFile, VariantSet variantSet, String exportID, List<GenotypingSample> samplesToExport, final ProgressIndicator progress) throws SocketException, UnknownHostException {
			exportHandler = feh;
			this.exportFile = exportFile;
			MongoTemplate mongoTemplate = MongoTemplateManager.get(variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR)[0]);
			this.varColl = mongoTemplate.getDb().withCodecRegistry(ExportManager.pojoCodecRegistry).getCollection(mongoTemplate.getCollectionName(VariantRunData.class));
			this.samplesToExport = samplesToExport;
			this.exportId = exportID;
			this.progress = progress;
			this.variantSet = variantSet;
		}
		
		ProgressIndicator getProgress() {
			return progress;
		}

		@Override
		public void run() {
			try {
				String[] splitId = variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR);
	        	int projId = Integer.parseInt(splitId[1]);
				String module = splitId[0];
				Document varQuery = new Document("$and", new BasicDBList() {{ add(new Document("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, projId)); add(new Document("_id." + VariantRunDataId.FIELDNAME_RUNNAME, splitId[2])); add(new Document(VariantData.FIELDNAME_TYPE, Type.SNP.toString())); /*only SNPs are supported*/ }} );
				
				MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
				result = exportHandler.createExportFiles(module, null /*FIXME*/, varColl.getNamespace().getCollectionName(), varQuery, variantSet.getVariantCount(), new ArrayList(), new ArrayList<>(), exportId, new HashMap(), new HashMap<>(), samplesToExport, progress);
				for (String step : exportHandler.getStepList())
					progress.addStep(step);
				progress.moveToNextStep();

		        int nQueryChunkSize = IExportHandler.computeQueryChunkSize(mongoTemplate, variantSet.getVariantCount());
		        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(exportFile))) {
		        	exportHandler.writeGenotypeFile(os, module, samplesToExport.stream().map(sp -> sp.getIndividual()).distinct().collect(Collectors.toList()), nQueryChunkSize, varColl, new Document(), null, result, null, progress);
		        }
				exportThreads.remove(exportId);
			} catch (Exception ex) {
				exception = ex;
			}
		}

		Exception getException() {
			return exception;
		}
	}
	
	public class FlapjackExportThread extends Thread {
		Exception exception = null;
		File[] result;
		FlapjackExportHandler exportHandler;
		File exportFile;
		MongoCollection<Document> varColl;
		String exportId;
		List<GenotypingSample> samplesToExport;
		ProgressIndicator progress;
		VariantSet variantSet;
		
		FlapjackExportThread(FlapjackExportHandler feh, File exportFile, VariantSet variantSet, String exportID, List<GenotypingSample> samplesToExport, final ProgressIndicator progress) throws SocketException, UnknownHostException {
			exportHandler = feh;
			this.exportFile = exportFile;
			MongoTemplate mongoTemplate = MongoTemplateManager.get(variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR)[0]);
			this.varColl = mongoTemplate.getDb().withCodecRegistry(ExportManager.pojoCodecRegistry).getCollection(mongoTemplate.getCollectionName(VariantRunData.class));
			this.samplesToExport = samplesToExport;
			this.exportId = exportID;
			this.progress = progress;
			this.variantSet = variantSet;
		}
		
		ProgressIndicator getProgress() {
			return progress;
		}

		@Override
		public void run() {
			try {
				String[] splitId = variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR);
	        	int projId = Integer.parseInt(splitId[1]);
				String module = splitId[0];
				Document varQuery = new Document("$and", new BasicDBList() {{ add(new Document("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, projId)); add(new Document("_id." + VariantRunDataId.FIELDNAME_RUNNAME, splitId[2])); }} );
				
				MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
				result = exportHandler.createExportFiles(module, null /*FIXME*/, varColl.getNamespace().getCollectionName(), varQuery, variantSet.getVariantCount(), new ArrayList(), new ArrayList<>(), exportId, new HashMap(), new HashMap<>(), samplesToExport, progress);
				for (String step : exportHandler.getStepList())
					progress.addStep(step);
				progress.moveToNextStep();

		        int nQueryChunkSize = IExportHandler.computeQueryChunkSize(mongoTemplate, variantSet.getVariantCount());
		        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(exportFile))) {
		        	exportHandler.writeGenotypeFile(os, module, null /*FIXME*/, nQueryChunkSize, varColl, new Document(), null, result, null, progress);
		        }
				exportThreads.remove(exportId);
			} catch (Exception ex) {
				exception = ex;
			}
		}

		Exception getException() {
			return exception;
		}
	}
	
	public class VcfExportThread extends Thread {
		Exception exception = null;
		File[] result;
		VcfExportHandler exportHandler;
		File exportFile;
		MongoCollection<Document> varColl;
		String exportId;
		List<GenotypingSample> samplesToExport;
		ProgressIndicator progress;
		VariantSet variantSet;
		
		VcfExportThread(VcfExportHandler feh, File exportFile, VariantSet variantSet, String exportID, List<GenotypingSample> samplesToExport, final ProgressIndicator progress) throws SocketException, UnknownHostException {
			exportHandler = feh;
			this.exportFile = exportFile;
			MongoTemplate mongoTemplate = MongoTemplateManager.get(variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR)[0]);
			this.varColl = mongoTemplate.getDb().withCodecRegistry(ExportManager.pojoCodecRegistry).getCollection(mongoTemplate.getCollectionName(VariantRunData.class));
			this.samplesToExport = samplesToExport;
			this.exportId = exportID;
			this.progress = progress;
			this.variantSet = variantSet;
		}
		
		ProgressIndicator getProgress() {
			return progress;
		}

		@Override
		public void run() {
			try {
				String[] splitId = variantSet.getVariantSetDbId().split(Helper.ID_SEPARATOR);
	        	int projId = Integer.parseInt(splitId[1]);
				String module = splitId[0];
				Document varQuery = new Document("$and", new BasicDBList() {{ add(new Document("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, projId)); add(new Document("_id." + VariantRunDataId.FIELDNAME_RUNNAME, splitId[2])); }} );

				for (String step : exportHandler.getStepList())
					progress.addStep(step);

				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(exportFile));
				MongoTemplate mongoTemplate = MongoTemplateManager.get(module);

		    	VariantContextWriter writer = null;
		    	List<String> distinctSequenceNames = new ArrayList<String>();

				for (Object chr : mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantRunData.class)).distinct(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_SEQUENCE, varQuery, String.class))	// find out distinctSequenceNames by looking at exported variant list
					if (chr != null)
						distinctSequenceNames.add(chr.toString());

				Collections.sort(distinctSequenceNames, new AlphaNumericComparator());
				SAMSequenceDictionary dict = exportHandler.createSAMSequenceDictionary(module, distinctSequenceNames);
				writer = new CustomVCFWriter(null, os, dict, false, false, true);
				exportHandler.writeGenotypeFile(module, null /*FIXME*/, new ArrayList(), new ArrayList<>(), progress, varColl.getNamespace().getCollectionName(), varQuery, (long) variantSet.getVariantCount(), null, null, null, samplesToExport, samplesToExport.stream().map(gs -> gs.getIndividual()).distinct().sorted(new AlphaNumericComparator<String>()).collect(Collectors.toList()), distinctSequenceNames, dict, null, writer);

				exportThreads.remove(exportId);
			} catch (Exception ex) {
				exception = ex;
			}
		}

		Exception getException() {
			return exception;
		}
	}
	
	/**
	 * Cleanup old export data.
	 *
	 * @param request the request
	 * @throws Exception
	 */
	private void cleanupOldExportData(HttpServletRequest request) throws Exception
	{
		if (request.getSession() == null)
			throw new Exception("Invalid request object");

		long nowMillis = new Date().getTime();
		File filterOutputLocation = new File(servletContext.getRealPath(File.separator + VariantSet.TMP_OUTPUT_FOLDER));
		if (filterOutputLocation.exists() && filterOutputLocation.isDirectory())
			for (File f : filterOutputLocation.listFiles())
				if (!f.isDirectory() && nowMillis - f.lastModified() > EXPORT_FILE_EXPIRATION_DELAY_MILLIS)
				{
					if (!f.delete())
						log.warn("Unable to delete " + f.getPath());
					else
						log.info("BrAPI export file was deleted: " + f.getPath());
				}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
