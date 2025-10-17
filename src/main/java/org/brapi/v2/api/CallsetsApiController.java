package org.brapi.v2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.subtypes.Callset;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@Controller
public class CallsetsApiController implements CallsetsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsetsApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
    private final ObjectMapper objectMapper;
//
//    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public CallsetsApiController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public ResponseEntity<CallSetsListResponse> searchCallsetsPost(	@ApiParam(value = "CallSet Search request")  @Valid @RequestBody CallSetsSearchRequest body,
    																@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
//    	Authentication auth = tokenManager.getAuthenticationFromToken(token);
//    	String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
    	
        try {
    		Status status = new Status();
    		HttpStatus httpCode = null;
    		
        	CallSetsListResponse cslr = new CallSetsListResponse();
        	CallSetsListResponseResult result = new CallSetsListResponseResult();
            Metadata metadata = new Metadata();
            cslr.setMetadata(metadata);
            IndexPagination pagination = new IndexPagination();
            metadata.setPagination(pagination);
			
        	boolean fTriedToAccessForbiddenData = false;
        	HashMap<String /*module*/, List<Criteria>> callSetCritByModule = new HashMap<>();
        	
        	boolean fFilterOnCallSets = (body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty());
        	boolean fFilterOnSamples = (body.getSampleDbIds() != null && !body.getSampleDbIds().isEmpty());
        	boolean fFilterOnGermplasm = body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty();
        	boolean fFilterOnVariantSets = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
        	
            if (!fFilterOnCallSets && !fFilterOnSamples && !fFilterOnVariantSets && !fFilterOnGermplasm) {
                status.setMessage("Some callSetDbIds, sampleDbIds, germplasmDbIds or variantSetDbIds must be specified as parameter!");
                metadata.addStatusItem(status);
                httpCode = HttpStatus.BAD_REQUEST;
            }
            else {
	        	if (fFilterOnCallSets) {
	        		if (body.getCallSetDbIds().isEmpty()) { //return empty result
	                    cslr.getMetadata().getPagination().setTotalCount(0L);
	                    cslr.getMetadata().getPagination().setTotalPages(0);
	                    cslr.setResult(new CallSetsListResponseResult());
	                    return new ResponseEntity<>(cslr, httpCode == null ? HttpStatus.OK : httpCode);
	                }
	                        
		        	HashMap<String /*module*/, HashSet<Integer> /*callsets, null means all*/> callSetsByModule = new HashMap<>();
                    for (String csId : body.getCallSetDbIds()) {
                        String[] info = Helper.getInfoFromId(csId, 2);
                        HashSet<Integer> moduleCallSets = callSetsByModule.get(info[0]);
                        if (moduleCallSets == null) {
                            moduleCallSets = new HashSet<>();
                            callSetsByModule.put(info[0], moduleCallSets);
                        }
                        moduleCallSets.add(Integer.parseInt(info[1]));
                    }
					
		        	for (String db : callSetsByModule.keySet()) { // make sure we filter out any samples that are from projects the user is not allowed to see
			        	MongoTemplate mongoTemplate = MongoTemplateManager.get(db);
			        	HashSet<Integer> moduleCallSets = callSetsByModule.get(db);
			        	Criteria crit = Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "._id").in(moduleCallSets);
			        	HashMap<Integer, Boolean> projectAccessPermissions = new HashMap<>();
			        	for (Callset cs : mongoTemplate.findDistinct(new Query(crit), GenotypingSample.FIELDNAME_CALLSETS, GenotypingSample.class, Callset.class)) {
			        		Boolean fPjAllowed = projectAccessPermissions.get(cs.getProjectId());
			        		if (fPjAllowed == null) {
			        			fPjAllowed = tokenManager.canUserReadProject(token, db, cs.getProjectId());
			        			projectAccessPermissions.put(cs.getProjectId(), fPjAllowed);
			        		}
	                        if (!fPjAllowed) {
	                            fTriedToAccessForbiddenData = true;
	                            moduleCallSets.remove(cs.getId());
	                        }
			        	}
	
			        	if (moduleCallSets.size() > 0) {
			        		List<Criteria> moduleCrit = callSetCritByModule.get(db);
			        		if (moduleCrit == null) {
			        			moduleCrit = new ArrayList<>();
			        			callSetCritByModule.put(db, moduleCrit);
			        		}
			        		moduleCrit.add(crit);
			        	}
		        	}
	        	}
        	
	        	if (fFilterOnSamples) {
	                if (body.getSampleDbIds().isEmpty()) { //return empty result
	                    cslr.getMetadata().getPagination().setTotalCount(0L);
	                    cslr.getMetadata().getPagination().setTotalPages(0);
	                    cslr.setResult(new CallSetsListResponseResult());
	                    return new ResponseEntity<>(cslr, httpCode == null ? HttpStatus.OK : httpCode);
	                }
	            
			    	HashMap<String /*module*/, HashSet<String> /*samples, null means all*/> samplesByModule = new HashMap<>();
	                for (String spId : body.getSampleDbIds()) {
	                    String[] info = Helper.getInfoFromId(spId, 2);
	                    HashSet<String> moduleSamples = samplesByModule.get(info[0]);
	                    if (moduleSamples == null) {
	                        moduleSamples = new HashSet<>();
	                        samplesByModule.put(info[0], moduleSamples);
	                    }
	                    moduleSamples.add(info[1]);
	                }
					
			    	for (String db : samplesByModule.keySet()) { // make sure we filter out any samples that are from projects the user is not allowed to see
			        	MongoTemplate mongoTemplate = MongoTemplateManager.get(db);
			        	HashSet<String> moduleSamples = samplesByModule.get(db);
			        	Criteria crit = Criteria.where("_id").in(moduleSamples);
			        	HashMap<Integer, Boolean> projectAccessPermissions = new HashMap<>();
			        	for (GenotypingSample sp : mongoTemplate.find(new Query(crit), GenotypingSample.class)) {
			        		boolean fAllowedToSeeSample = false;
				        	for (int callSetProjectId : sp.getCallSets().stream().map(cs -> cs.getProjectId()).toList()) {
				        		Boolean fPjAllowed = projectAccessPermissions.get(callSetProjectId);
				        		if (fPjAllowed == null) {
				        			fPjAllowed = tokenManager.canUserReadProject(token, db, callSetProjectId);
				        			projectAccessPermissions.put(callSetProjectId, fPjAllowed);
				        		}
				        		if (fPjAllowed) {
				        			fAllowedToSeeSample = true;
				        			break;
				        		}
				        	}
	                        if (!fAllowedToSeeSample) {
	                            fTriedToAccessForbiddenData = true;
	                            moduleSamples.remove(sp.getId());
	                        }
			        	}
			
			        	if (moduleSamples.size() > 0) {
			        		List<Criteria> moduleCrit = callSetCritByModule.get(db);
			        		if (moduleCrit == null) {
			        			moduleCrit = new ArrayList<>();
			        			callSetCritByModule.put(db, moduleCrit);
			        		}
			        		moduleCrit.add(crit);
			        	}
			    	}
	        	}

	        	if (fFilterOnGermplasm) {
	                HashMap<String /*module*/, List<Criteria>> germplasmCritByModule = new HashMap<>();
	        		HashMap<String, Collection<String>> dbIndividuals = GermplasmApiController.readGermplasmIDs(body.getGermplasmDbIds());
	                for (String db : dbIndividuals.keySet()) {	// make sure at least one germplasm exists in each db before returning it
	                    if (((fFilterOnCallSets || fFilterOnSamples) && callSetCritByModule.containsKey(db))) { //germplasm base matches with callSets base, no need to check user access 
	                        ArrayList<Criteria> samplesCrit = new ArrayList<>();
	                        samplesCrit.addAll(callSetCritByModule.get(db));  
	                        samplesCrit.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(dbIndividuals.get(db))); 
	                        Criteria crit = new Criteria().andOperator(samplesCrit);
	                        List<Criteria> moduleCrit = germplasmCritByModule.get(db);
	                        if (moduleCrit == null) {
	                        	moduleCrit = new ArrayList<>();
	                            germplasmCritByModule.put(db, moduleCrit);
	                        }
	                        moduleCrit.add(crit);
	                    } else if (!fFilterOnCallSets) {
	                        if (tokenManager.canUserReadDB(token, db)) {
                                List<Criteria> moduleCrit = germplasmCritByModule.get(db);
                                if (moduleCrit == null) {
                                    moduleCrit = new ArrayList<>();
                                    germplasmCritByModule.put(db, moduleCrit);
                                }
                                moduleCrit.add(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(dbIndividuals.get(db)));
	                        }
	                    }
	                }
	                callSetCritByModule = germplasmCritByModule;
	        	}

	        	if (fFilterOnVariantSets) {
                    HashMap<String /*module*/, List<Criteria>> vsCritByModule = new HashMap<>();
                    boolean matchingVariantSetBase = false;
                    for (String variantSetDbId : body.getVariantSetDbIds()) {
                        String[] info = Helper.getInfoFromId(variantSetDbId, 3);
                        int projId = Integer.parseInt(info[1]); 
                        
                        if ((fFilterOnCallSets || fFilterOnSamples || fFilterOnGermplasm) && callSetCritByModule.containsKey(info[0])) { // variantSet base matches with callSets or germplasm base 
                            ArrayList<Criteria> callsetsCrit = new ArrayList<>();
                            callsetsCrit.addAll(callSetCritByModule.get(info[0]));
                            callsetsCrit.add(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_PROJECT_ID).is(projId));
                            callsetsCrit.add(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_RUN).is(info[2]));
                            Criteria crit = new Criteria().andOperator(callsetsCrit.toArray(new Criteria[callsetsCrit.size()]));
	                        List<Criteria> moduleCrit = vsCritByModule.get(info[0]);
	                        if (moduleCrit == null) {
	                        	moduleCrit = new ArrayList<>();
	                        	vsCritByModule.put(info[0], moduleCrit);
	                        }
	                        moduleCrit.add(crit);

                            matchingVariantSetBase = true;
                        } else if (!fFilterOnCallSets && !fFilterOnGermplasm) { // get all callsets for each accessible variantSets
                            if (tokenManager.canUserReadProject(token, info[0], projId)) {
                                List<Criteria> moduleCrit = vsCritByModule.get(info[0]);
                                if (moduleCrit == null) {
                                    moduleCrit = new ArrayList<>();
                                    vsCritByModule.put(info[0], moduleCrit);
                                }
                                moduleCrit.add(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_CALLSETS + "." + Callset.FIELDNAME_RUN).is(info[2])));
                            }
                            else
                                fTriedToAccessForbiddenData = true;
                        }
                    }
                    
                    if (!matchingVariantSetBase && (fFilterOnCallSets || fFilterOnGermplasm)) { // there is no variantSet matching with callsets: return empty result
                        cslr.getMetadata().getPagination().setTotalCount(0L);
                        cslr.getMetadata().getPagination().setTotalPages(0);
                        cslr.setResult(new CallSetsListResponseResult());
                        return new ResponseEntity<>(cslr, httpCode == null ? HttpStatus.OK : httpCode);
                    }
                    callSetCritByModule = vsCritByModule;
                }
                
                int nTotalCallSetsEncountered = 0;
                for (String db : callSetCritByModule.keySet()) {
                    MongoTemplate mongoTemplate = MongoTemplateManager.get(db);
                    List<Criteria> critList = callSetCritByModule.get(db);
                    List<Callset> callsets = mongoTemplate.find(new Query(new Criteria().orOperator(critList)), GenotypingSample.class).stream().map(sp -> sp.getCallSets()).flatMap(Collection::stream).toList();
                    for (int i=0; i < callsets.size(); i++) {
                        Callset callset = callsets.get(i);
                        nTotalCallSetsEncountered++;
                        CallSet brapiCallset = new CallSet();
                        brapiCallset.setCallSetDbId(db + Helper.ID_SEPARATOR + callset.getId());
                        brapiCallset.setCallSetName(String.valueOf(callset.getId()));
                        brapiCallset.setSampleDbId(db + Helper.ID_SEPARATOR + callset.getSampleId());
                        brapiCallset.setVariantSetDbIds(Arrays.asList(db + Helper.ID_SEPARATOR + callset.getProjectId() + Helper.ID_SEPARATOR + callset.getRun()));
                        result.addDataItem(brapiCallset);
                    }
                }

	        	if (nTotalCallSetsEncountered == 0 && fTriedToAccessForbiddenData)
	        		httpCode = HttpStatus.FORBIDDEN;
	            else {
	    			cslr.getMetadata().getPagination().setPageSize(result.getData().size());
	    			cslr.getMetadata().getPagination().setCurrentPage(body.getPage());
	    			cslr.getMetadata().getPagination().setTotalPages(1);
	    			cslr.getMetadata().getPagination().setTotalCount((long) result.getData().size());
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
//            return new ResponseEntity<CallSetResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"sampleDbId\" : \"sampleDbId\",\n    \"callSetName\" : \"callSetName\",\n    \"created\" : \"created\",\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"callSetDbId\" : \"callSetDbId\",\n    \"updated\" : \"updated\",\n    \"variantSetDbIds\" : [ \"variantSetDbIds\", \"variantSetDbIds\" ],\n    \"studyDbId\" : \"studyDbId\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<CallSetsListResponse> callsetsGet(@ApiParam(value = "The ID of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetDbId", required = false) String callSetDbId,@ApiParam(value = "The human readbale name of the `CallSet` to be retrieved.") @Valid @RequestParam(value = "callSetName", required = false) String callSetName,@ApiParam(value = "The ID of the `VariantSet` to be retrieved.") @Valid @RequestParam(value = "variantSetDbId", required = false) String variantSetDbId,@ApiParam(value = "The ID of the `VariantSet` to be retrieved.") @Valid @RequestParam(value = "sampleDbId", required = false) String sampleDbId,@ApiParam(value = "Return only call sets generated from the Sample of this Germplasm") @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallSetsListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetDbIds\" : [ \"variantSetDbIds\", \"variantSetDbIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    }, {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetDbIds\" : [ \"variantSetDbIds\", \"variantSetDbIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetsListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallSetsListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}
