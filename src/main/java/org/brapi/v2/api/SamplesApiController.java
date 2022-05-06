package org.brapi.v2.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.brapi.v2.model.Germplasm;
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

import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.mgdb.service.IGigwaService;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@CrossOrigin
@Controller
//@ApiIgnore
public class SamplesApiController implements SamplesApi {

    private static final Logger log = LoggerFactory.getLogger(SamplesApiController.class);

    @Autowired private GigwaGa4ghServiceImpl ga4ghService;
    
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

	public ResponseEntity<SampleListResponse> searchSamplesPost(@ApiParam(value = "")  @Valid @RequestBody SampleSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
    	Authentication auth = tokenManager.getAuthenticationFromToken(token);
    	String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();

        try {
        	SampleListResponse slr = new SampleListResponse();
        	SampleListResponseResult result = new SampleListResponseResult();
        	String programDbId = null;
        	Integer projId = null;
        	Collection<Integer> sampleIds = new HashSet<>();
			Metadata metadata = new Metadata();
			slr.setMetadata(metadata);
			String sErrorMsg = "";

        	if ((body.getObservationUnitDbIds() != null && body.getObservationUnitDbIds().size() > 0) || (body.getPlateDbIds() != null && body.getPlateDbIds().size() > 0))
        		sErrorMsg += "Searching by Plate or ObservationUnit is not supported! ";
        	else if (body.getStudyDbIds() != null) {
        		Collection<String> studyIds = new HashSet<>();
	        	for (String studyId : body.getStudyDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(studyId, 2);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0]))
						sErrorMsg += "You may only supply IDs of study records from one program at a time!";
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1])))
						sErrorMsg += "You may only supply a single studyDbId at a time!";
					studyIds.add(info[1]);
	        	}
	        	sampleIds = MgdbDao.getSamplesForProject(programDbId, projId, null).stream().map(sp -> sp.getId()).collect(Collectors.toList());
        	}
        	else if (body.getGermplasmDbIds() != null) {
        		Collection<String> germplasmIds = new HashSet<>();
	        	for (String gpId : body.getGermplasmDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 2);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0]))
						sErrorMsg += "You may only supply IDs of germplasm records from one program at a time!";
					germplasmIds.add(info[1]);
	        	}
	        	sampleIds = MongoTemplateManager.get(programDbId).findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_INDIVIDUAL).in(germplasmIds)), "_id", GenotypingSample.class, Integer.class);
        	}
        	else if (body.getSampleDbIds() != null)
	        	for (String spId : body.getSampleDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(spId, 2);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0]))
						sErrorMsg += "You may only supply IDs of sample records from one program at a time!";
					sampleIds.add(Integer.parseInt(info[1]));
	        	}
        	else
        		sErrorMsg += "You must provide either a list of germplasmDbIds or a list of sampleDbIds!";

   			if (!sErrorMsg.isEmpty()) {
				Status status = new Status();
				status.setMessage(sErrorMsg);
				metadata.addStatusItem(status);
				return new ResponseEntity<>(slr, HttpStatus.BAD_REQUEST);
			}

   			if (!tokenManager.canUserReadDB(token, programDbId))
   				return new ResponseEntity<SampleListResponse>(HttpStatus.FORBIDDEN);

        	MongoTemplate mongoTemplate = MongoTemplateManager.get(programDbId);
        	Query q = new Query(Criteria.where("_id").in(sampleIds));
        	long count = mongoTemplate.count(q, GenotypingSample.class);
            if (body.getPageSize() != null) {
            	q.limit(body.getPageSize());
                if (body.getPage() != null)
                	q.skip(body.getPage() * body.getPageSize());
            }
            
			// attach individual metadata to samples
            List<GenotypingSample> genotypingSamples = mongoTemplate.find(q, GenotypingSample.class);
			Map<String, Individual> indMap = MgdbDao.getInstance().loadIndividualsWithAllMetadata(programDbId, sCurrentUser, null, genotypingSamples.stream().map(sp -> sp.getIndividual()).collect(Collectors.toList()));
			
			String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
        	for (GenotypingSample mgdbSample : genotypingSamples) {
        		Sample sample = new Sample();
        		sample.sampleDbId(ga4ghService.createId(programDbId, mgdbSample.getId()));
        		sample.germplasmDbId(ga4ghService.createId(programDbId, mgdbSample.getIndividual()));
        		sample.setSampleName(mgdbSample.getSampleName());
        		sample.studyDbId(programDbId + IGigwaService.ID_SEPARATOR + mgdbSample.getProjectId());
    			final Individual ind = indMap.get(mgdbSample.getIndividual());
            	for (String key : ind.getAdditionalInfo().keySet()) {
                    String sLCkey = key.toLowerCase();
                    Object val = ind.getAdditionalInfo().get(key);
                    if (val == null)
                    	continue;

                    if (!Germplasm.germplasmFields.containsKey(sLCkey) && !BrapiRestController.extRefList.contains(key) && !lowerCaseIdFieldName.equals(sLCkey))
                    	sample.putAdditionalInfoItem(key, ind.getAdditionalInfo().get(key));
            	}
        		result.addDataItem(sample);
        	}

			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize((int) (body.getPageSize() == null ? count : body.getPageSize()));
			pagination.setCurrentPage(body.getPage());
			pagination.setTotalPages(body.getPageSize() == null ? 1 : (int) Math.ceil((float) count / body.getPageSize()));
			pagination.setTotalCount((int) count);
			metadata.setPagination(pagination);
        	
			slr.setResult(result);
            return new ResponseEntity<SampleListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<SampleListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
//    public ResponseEntity<SampleListResponse> samplesGet(@ApiParam(value = "the internal DB id for a sample") @Valid @RequestParam(value = "sampleDbId", required = false) String sampleDbId,@ApiParam(value = "the internal DB id for an observation unit where a sample was taken from") @Valid @RequestParam(value = "observationUnitDbId", required = false) String observationUnitDbId,@ApiParam(value = "the internal DB id for a plate of samples") @Valid @RequestParam(value = "plateDbId", required = false) String plateDbId,@ApiParam(value = "the internal DB id for a germplasm") @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SampleListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SampleListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SampleListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SampleListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<SampleListResponse> samplesPost(@ApiParam(value = ""  )  @Valid @RequestBody List<SampleNewRequest> body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SampleListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SampleListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SampleListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SampleListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<SampleSingleResponse> samplesSampleDbIdGet(@ApiParam(value = "the internal DB id for a sample",required=true) @PathVariable("sampleDbId") String sampleDbId,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SampleSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SampleSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SampleSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SampleSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<SampleSingleResponse> samplesSampleDbIdPut(@ApiParam(value = "the internal DB id for a sample",required=true) @PathVariable("sampleDbId") String sampleDbId,@ApiParam(value = ""  )  @Valid @RequestBody SampleNewRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SampleSingleResponse>(objectMapper.readValue("{\n  \"result\" : \"\",\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SampleSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SampleSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SampleSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

}
