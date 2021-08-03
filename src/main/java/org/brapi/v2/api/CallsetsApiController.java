package org.brapi.v2.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.brapi.v2.model.CallSet;
import org.brapi.v2.model.CallSetsListResponse;
import org.brapi.v2.model.CallSetsListResponseResult;
import org.brapi.v2.model.CallSetsSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

import fr.cirad.mgdb.model.mongo.maintypes.CustomIndividualMetadata;
import fr.cirad.mgdb.model.mongo.maintypes.CustomIndividualMetadata.CustomIndividualMetadataId;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@CrossOrigin
@Controller
public class CallsetsApiController implements CallsetsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsetsApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
//    private final ObjectMapper objectMapper;
//
//    private final HttpServletRequest request;

//    @org.springframework.beans.factory.annotation.Autowired
//    public CallsetsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
//        this.objectMapper = objectMapper;
//        this.request = request;
//    }


    public ResponseEntity<CallSetsListResponse> searchCallsetsPost(	@ApiParam(value = "CallSet Search request")  @Valid @RequestBody CallSetsSearchRequest body,
    																@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
    	Authentication auth = tokenManager.getAuthenticationFromToken(token);
    	String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
    	
        try {
    		Status status = new Status();
    		HttpStatus httpCode = null;
    		
        	CallSetsListResponse cslr = new CallSetsListResponse();
        	CallSetsListResponseResult result = new CallSetsListResponseResult();
			Metadata metadata = new Metadata();
			cslr.setMetadata(metadata);
			
        	boolean fTriedToAccessForbiddenData = false;
        	HashMap<String /*module*/, Query> sampleQueryByModule = new HashMap<>();
        	
			if ((body.getCallSetDbIds() == null || body.getCallSetDbIds().isEmpty()) && (body.getVariantSetDbIds() == null || body.getVariantSetDbIds().isEmpty())) {
				status.setMessage("Some callSetDbIds or variantSetDbIds must be specified as parameter!");
				metadata.addStatusItem(status);
				httpCode = HttpStatus.BAD_REQUEST;
			}
			else {
				if (body.getVariantSetDbIds() == null || body.getVariantSetDbIds().isEmpty()) {	// no variantSets specified, but we have a list of callSets
		        	HashMap<String /*module*/, HashSet<Integer> /*samples, null means all*/> samplesByModule = new HashMap<>();
					for (String csId : body.getCallSetDbIds()) {
						String[] info = GigwaSearchVariantsRequest.getInfoFromId(csId, 3);
						HashSet<Integer> moduleSamples = samplesByModule.get(info[0]);
						if (moduleSamples == null) {
							moduleSamples = new HashSet<>();
							samplesByModule.put(info[0], moduleSamples);
						}
						moduleSamples.add(Integer.parseInt(info[2]));
					}
		        	for (String module : samplesByModule.keySet()) { // make sure we filter out any samples that are from projects the user is not allowed to see
			        	MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
			        	HashSet<Integer> moduleSamples = samplesByModule.get(module);
			        	Query query = new Query(Criteria.where("_id").in(moduleSamples));
			        	HashMap<Integer, Boolean> projectAccessPermissions = new HashMap<>();
			        	for (GenotypingSample sample : mongoTemplate.find(query, GenotypingSample.class)) {
			        		Boolean fPjAllowed = projectAccessPermissions.get(sample.getProjectId());
			        		if (fPjAllowed == null) {
			        			fPjAllowed = tokenManager.canUserReadProject(token, module, sample.getProjectId());
			        			projectAccessPermissions.put(sample.getProjectId(), fPjAllowed);
			        		}
		            		if (!fPjAllowed) {
		            			fTriedToAccessForbiddenData = true;
		            			moduleSamples.remove(sample.getId());
		            		}
			        	}

			        	if (moduleSamples.size() > 0)
			        		sampleQueryByModule.put(module, query);
		        	}
				}
				else
					for (String variantSetDbId : body.getVariantSetDbIds()) {
						String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 3);
			        	int projId = Integer.parseInt(info[1]);
		    			if (tokenManager.canUserReadProject(token, info[0], projId))
			    			sampleQueryByModule.put(info[0], new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(info[2]))));
		    			else
		    				fTriedToAccessForbiddenData = true;
					}

				int nTotalCallSetsEncountered = 0;
				for (String module : sampleQueryByModule.keySet()) {
		        	MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
    				Map<String, Integer> indIdToSampleIdMap = new HashMap<>();
    				List<GenotypingSample> samples = mongoTemplate.find(sampleQueryByModule.get(module), GenotypingSample.class);
    				for (GenotypingSample sample : samples)
    					indIdToSampleIdMap.put(sample.getIndividual(), sample.getId());

    				// attach individual metadata to samples
    				Query q = new Query(Criteria.where("_id").in(indIdToSampleIdMap.keySet()));
    				q.with(Sort.by(Sort.Direction.ASC, "_id"));
    				
    				List<Individual> listInd = mongoTemplate.find(q, Individual.class);
    				Map<String, Individual> indMap = mongoTemplate.find(q, Individual.class).stream().collect(Collectors.toMap(Individual::getId, ind -> ind));
					for (CustomIndividualMetadata cimd : mongoTemplate.find(new Query(Criteria.where("_id." + CustomIndividualMetadataId.FIELDNAME_USER).is(sCurrentUser)), CustomIndividualMetadata.class))	// merge with custom metadata if available
		                if (cimd.getAdditionalInfo() != null && !cimd.getAdditionalInfo().isEmpty())
		                	indMap.get(cimd.getId().getIndividualId()).getAdditionalInfo().putAll(cimd.getAdditionalInfo());
    				
					for (int i=0; i<samples.size(); i++) {
						GenotypingSample sample = samples.get(i);
						nTotalCallSetsEncountered++;
		            	CallSet callset = new CallSet();
		            	callset.setCallSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getIndividual() + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getId());
		            	callset.setCallSetName(callset.getCallSetDbId());
		            	callset.setSampleDbId(callset.getCallSetDbId());
			            callset.setVariantSetIds(Arrays.asList(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getProjectId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getRun()));
            			final Individual ind = listInd.get(i);
            			if (!ind.getAdditionalInfo().isEmpty())
            				callset.setAdditionalInfo(ind.getAdditionalInfo().keySet().stream().collect(Collectors.toMap(k -> k, k -> (List<String>) Arrays.asList(ind.getAdditionalInfo().get(k).toString()))));
	            		result.addDataItem(callset);
					}
				}

	        	if (nTotalCallSetsEncountered == 0 && fTriedToAccessForbiddenData)
	        		httpCode = HttpStatus.FORBIDDEN;
	        	else {
	    			IndexPagination pagination = new IndexPagination();
	    			pagination.setPageSize(result.getData().size());
	    			pagination.setCurrentPage(body.getPage());
	    			pagination.setTotalPages(1);
	    			pagination.setTotalCount(result.getData().size());
	    			metadata.setPagination(pagination);
	        	}
			}

			cslr.setResult(result);
            return new ResponseEntity<>(cslr, httpCode == null ? HttpStatus.OK : httpCode);

        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    public ResponseEntity<CallListResponse> callsetsCallSetDbIdCallsGet(@ApiParam(value = "The ID of the `CallSet` to be retrieved.",required=true) @PathVariable("callSetDbId") String callSetDbId,@ApiParam(value = "Should homozygotes be expanded (true) or collapsed into a single occurence (false)") @Valid @RequestParam(value = "expandHomozygotes", required = false) Boolean expandHomozygotes,@ApiParam(value = "The string to use as a representation for missing data") @Valid @RequestParam(value = "unknownString", required = false) String unknownString,@ApiParam(value = "The string to use as a separator for phased allele calls") @Valid @RequestParam(value = "sepPhased", required = false) String sepPhased,@ApiParam(value = "The string to use as a separator for unphased allele calls") @Valid @RequestParam(value = "sepUnphased", required = false) String sepUnphased,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    }, {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    } ],\n    \"unknownString\" : \"unknownString\",\n    \"expandHomozygotes\" : true,\n    \"sepPhased\" : \"sepPhased\",\n    \"sepUnphased\" : \"sepUnphased\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<CallSetResponse> callsetsCallSetDbIdGet(@ApiParam(value = "The ID of the `CallSet` to be retrieved.",required=true) @PathVariable("callSetDbId") String callSetDbId,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallSetResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"sampleDbId\" : \"sampleDbId\",\n    \"callSetName\" : \"callSetName\",\n    \"created\" : \"created\",\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"callSetDbId\" : \"callSetDbId\",\n    \"updated\" : \"updated\",\n    \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n    \"studyDbId\" : \"studyDbId\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<CallSetsListResponse> callsetsGet(@ApiParam(value = "The ID of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetDbId", required = false) String callSetDbId,@ApiParam(value = "The human readbale name of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetName", required = false) String callSetName,@ApiParam(value = "The ID of the `VariantSet` to be retrieved.") @Valid @RequestParam(value = "variantSetDbId", required = false) String variantSetDbId,@ApiParam(value = "The ID of the `VariantSet` to be retrieved.") @Valid @RequestParam(value = "sampleDbId", required = false) String sampleDbId,@ApiParam(value = "Return only call sets generated from the Sample of this Germplasm") @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallSetsListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    }, {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetsListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallSetsListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}
