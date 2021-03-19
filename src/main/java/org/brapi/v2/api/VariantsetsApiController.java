package org.brapi.v2.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;

import fr.cirad.mgdb.exporting.IExportHandler;
import fr.cirad.mgdb.exporting.individualoriented.AbstractIndividualOrientedExportHandler;
import fr.cirad.mgdb.exporting.individualoriented.FlapjackExportHandler;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.AppConfig;
import fr.cirad.tools.ProgressIndicator;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.BackOfficeController;
import io.swagger.annotations.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.CallSetsListResponse;
import org.brapi.v2.model.CallsListResponseResult;
import org.brapi.v2.model.ListValue;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.Pagination;
import org.brapi.v2.model.StudyListResponse;
import org.brapi.v2.model.VariantSetResponse;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetAvailableFormats;
import org.brapi.v2.model.VariantSetAvailableFormats.DataFormatEnum;
import org.brapi.v2.model.VariantSetAvailableFormats.FileFormatEnum;
import org.brapi.v2.model.VariantListResponse;
import org.brapi.v2.model.VariantSetListResponse;
import org.brapi.v2.model.VariantSetResponse;
import org.brapi.v2.model.VariantSetsExtractRequest;
import org.brapi.v2.model.VariantSetsSearchRequest;
import org.bson.Document;
import org.ga4gh.methods.SearchVariantSetsRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@CrossOrigin
@Controller
public class VariantsetsApiController implements ServletContextAware, VariantsetsApi {

    private static final Logger log = LoggerFactory.getLogger(VariantsetsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired private GigwaGa4ghServiceImpl ga4ghService;
    
    @Autowired private AbstractTokenManager tokenManager;
    
    @Autowired private SearchApiController searchApiController;
    
    @Autowired private AppConfig appConfig;
    
	private ServletContext servletContext;
	static final private String TMP_OUTPUT_FOLDER = "genofilt/brapiV2TmpOutput";
	static final private long EXPORT_FILE_EXPIRATION_DELAY_MILLIS = 1000*60*60*24;	/* 1 day */
	
    private static HashMap<String /*export id*/, FlapjackExportThread /*temporary file generation thread */> exportThreads = new HashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public VariantsetsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
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
//
//    public ResponseEntity<VariantSetListResponse> variantsetsGet(@NotNull @ApiParam(value = "The ID of the `VariantSet` to be retrieved.", required = true) @Valid @RequestParam(value = "variantSetDbId", required = true) String variantSetDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<VariantSetListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"availableFormats\" : [ {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      }, {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      } ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : \"variantSetDbId\",\n      \"callSetCount\" : 0,\n      \"VariantSetDbId\" : \"VariantSetDbId\",\n      \"variantSetName\" : \"variantSetName\",\n      \"analysis\" : [ {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      }, {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      } ],\n      \"studyDbId\" : \"studyDbId\",\n      \"variantCount\" : 6\n    }, {\n      \"availableFormats\" : [ {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      }, {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      } ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : \"variantSetDbId\",\n      \"callSetCount\" : 0,\n      \"VariantSetDbId\" : \"VariantSetDbId\",\n      \"variantSetName\" : \"variantSetName\",\n      \"analysis\" : [ {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      }, {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      } ],\n      \"studyDbId\" : \"studyDbId\",\n      \"variantCount\" : 6\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantSetListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<VariantSetListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<VariantSetListResponse>(HttpStatus.NOT_IMPLEMENTED);
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
		return searchApiController.searchVariantsetsPost(body, authorization);
	}
    
//	@Override
//	public ResponseEntity<VariantSetListResponse> variantsetsGet(String commonCropName, String studyType, String programDbId,
//			String locationDbId, String seasonDbId, String trialDbId, String studyDbId, String studyName,
//			String studyCode, String studyPUI, String germplasmDbId, String observationVariableDbId, Boolean active,
//			String sortBy, String sortOrder, String externalReferenceID, String externalReferenceSource, Integer page,
//			Integer pageSize, String authorization) {
//
//		return searchApiController.searchStudiesPost(null, authorization);
//	}

	@Override
	public ResponseEntity<CallListResponse> variantsetsVariantSetDbIdCallsGet(String variantSetDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, Integer page, Integer pageSize, String authorization) throws UnsupportedEncodingException {
		/* FIXME: check security implementation */	
		String token = ServerinfoApiController.readToken(authorization);
		String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 2);
		if (!tokenManager.canUserReadProject(token, info[0], info[1]))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		
    	String unknownGtCode = unknownString == null ? "-" : unknownString;
    	String unPhasedSeparator = sepUnphased == null ? "/" : sepUnphased;
    	String phasedSeparator = sepPhased == null ? "|" : URLDecoder.decode(sepPhased, "UTF-8");
    	
    	CallListResponse clr = new CallListResponse();
    	CallsListResponseResult result = new CallsListResponseResult();
    	result.setSepUnphased(unPhasedSeparator);
		
        try {
        	MongoTemplate mongoTemplate = MongoTemplateManager.get(info[0]);	/*FIXME: variantSetDbId must become module§run*/
        	Iterator<VariantRunData> runIt = mongoTemplate.find(new Query(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1]))/*, Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])*/)), VariantRunData.class).iterator();
        	HashMap<Integer, String> previousPhasingIds = new HashMap<>();
        	while (runIt.hasNext()) {
        		VariantRunData vrd = runIt.next();
        		for (Integer spId : vrd.getSampleGenotypes().keySet()) {
        			SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
					String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
					boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
					previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/

					String gtCode= sg.getCode(), genotype;
					if (gtCode.length() == 0)
						genotype = unknownGtCode;
					else
					{
						List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
						if (!Boolean.TRUE.equals(expandHomozygotes) && new HashSet<String>(alleles).size() == 1)
							genotype = alleles.get(0);
						else
							genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
					}
        			Call call = new Call();
        			ListValue lv = new ListValue();
        			lv.addValuesItem(genotype);
        			call.setGenotype(lv);
        			call.setVariantDbId(vrd.getId().getVariantId());
        			call.setVariantName(call.getVariantDbId());
        			call.setCallSetDbId(info[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
        			call.setCallSetName(call.getCallSetDbId());
        			
                	result.addDataItem(call);
        		}
        	}
        	
        	
//        	Map<String, Object> v1Attributes = brapiV1Service.germplasmAttributes(request, response, info[0], info[2], pageSize, pageSize);
//        	Collection<Map<String, String>> dataDoc = (Collection<Map<String, String>>) ((Map<String, Object>) v1Attributes.get("result")).get("data");
//        	for (Map<String, String> v1DataItem : dataDoc) {
//	        	Call dataItem = new Call();
//	        	dataItem.setAttributeDbId(v1DataItem.get("attributeDbId"));
//	        	dataItem.setDefaultValue(v1DataItem.get("value"));
//	        	result.addDataItem(dataItem);
//        	}
        	

			Metadata metadata = new Metadata();
			Pagination pagination = new Pagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
			clr.setMetadata(metadata);
			
			clr.setResult(result);
            return new ResponseEntity<CallListResponse>(clr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
//                return new ResponseEntity<CallSetsListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    }, {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetsListResponse.class), HttpStatus.NOT_IMPLEMENTED);
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
//        	VariantSetResponseResult result = new VariantSetResponseResult();
        	org.ga4gh.models.VariantSet ga4ghVariantSet = ga4ghService.getVariantSet(variantSetDbId);
//        	if (page == null)
//        		page = 0;
//        	int nAllowedDbIndex = 0;
//        	for (final org.ga4gh.models.VariantSet ga4ghRefSet : ga4ghRefSets)
        	VariantSet result = null;
        		if (tokenManager.canUserReadDB(tokenManager.getAuthenticationFromToken(token), ga4ghVariantSet.getId().split(GigwaGa4ghServiceImpl.ID_SEPARATOR)[0])) {
        			result = new VariantSet() {{
            			setVariantSetDbId(ga4ghVariantSet.getId());
            			setVariantSetName(ga4ghVariantSet.getName());
            			setReferenceSetDbId(ga4ghVariantSet.getReferenceSetId());
            			List<VariantSetAvailableFormats> formatList = new ArrayList<VariantSetAvailableFormats>();
            			VariantSetAvailableFormats format = new VariantSetAvailableFormats();
            			format.setDataFormat(DataFormatEnum.FLAPJACK);
            			format.setFileFormat(FileFormatEnum.TEXT_TSV);
            			String sWebAppRoot = appConfig.get("enforcedWebapRootUrl");
            			format.setFileURL((sWebAppRoot == null ? BackOfficeController.determinePublicHostName(request) + request.getContextPath() : sWebAppRoot) + request.getServletPath() + ServerinfoApi.URL_BASE_PREFIX + variantsetsGetExportFile_url.replace("{variantSetDbId}", variantSetDbId).replace("{dataFormat}", format.getDataFormat().toString()));
            			formatList.add(format);
            			setAvailableFormats(formatList);
            		}};
        		}

			Metadata metadata = new Metadata();
			Pagination pagination = new Pagination();
			pagination.setPageSize(0);
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(1);
			metadata.setPagination(pagination);
			rlr.setMetadata(metadata);
			
			rlr.setResult(result);
            return new ResponseEntity<VariantSetResponse>(rlr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<VariantSetResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
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
		File filterOutputLocation = new File(servletContext.getRealPath(File.separator + TMP_OUTPUT_FOLDER));
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
	public void variantsetsExportIntoFormat(HttpServletResponse response, String variantSetDbId, String dataFormat, String authorization) throws Exception {
		
		/* FIXME: check security implementation */	
		String token = ServerinfoApiController.readToken(authorization);
		String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 2);
		if (!tokenManager.canUserReadProject(token, info[0], info[1])){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("You are not allowed to access this content");
			return;
		}
		
		if (dataFormat.equalsIgnoreCase(DataFormatEnum.FLAPJACK.toString())) {
			cleanupOldExportData(request);
			
			FlapjackExportHandler fjExportHandler = (FlapjackExportHandler) AbstractIndividualOrientedExportHandler.getIndividualOrientedExportHandlers().get(dataFormat.toUpperCase());
			
        	String[] splitId = variantSetDbId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
        	MongoTemplate mongoTemplate = MongoTemplateManager.get(splitId[0]);
        	int projId = Integer.parseInt(splitId[1]);
        	String exportId = "brapiV2export-" + variantSetDbId;
        	MongoCollection<Document> varColl = mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantData.class));
        	
			String relativeOutputFolder = File.separator + TMP_OUTPUT_FOLDER + File.separator;
			File outputLocation = new File(servletContext.getRealPath(relativeOutputFolder));
			if (!outputLocation.exists() && !outputLocation.mkdirs())
				throw new Exception("Unable to create folder: " + outputLocation);
        	File exportFile = new File(servletContext.getRealPath(relativeOutputFolder + exportId + ".genotype"));

        	FlapjackExportThread tempFileGenerationThread = exportThreads.get(exportId);
        	if (tempFileGenerationThread == null) {	// job is not running: either complete or not started
        		if (exportFile.exists()) {	// seems complete
        			FileInputStream is = new FileInputStream(exportFile);
        			IOUtils.copy(is, response.getOutputStream());
        			is.close();
        			return;
        		}

        		// start it
            	ProgressIndicator progress = new ProgressIndicator(exportId, new String[] {"Reading and re-organizing genotypes"});
            	ProgressIndicator.registerProgressIndicator(progress);            	
        		tempFileGenerationThread = new FlapjackExportThread(fjExportHandler, exportFile, splitId[0], varColl, exportId, MgdbDao.getSamplesForProject(splitId[0], projId, MgdbDao.getProjectIndividuals(splitId[0], projId)), progress);
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
    		}
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Unsupported data format: " + dataFormat);
		}
	}
	
	public static class FlapjackExportThread extends Thread {
		Exception exception = null;
		Map<String, File> result;
		FlapjackExportHandler exportHandler;
		File exportFile;
		String module;
		MongoCollection<Document> varColl;
		String exportId;
		List<GenotypingSample> samplesToExport;
		ProgressIndicator progress;
		
		FlapjackExportThread(FlapjackExportHandler feh, File exportFile, String sModule, MongoCollection<Document> varColl, String exportID, List<GenotypingSample> samplesToExport, final ProgressIndicator progress) {
			exportHandler = feh;
			this.exportFile = exportFile;
			module = sModule;
			this.varColl = varColl;
			this.samplesToExport = samplesToExport;
			this.exportId = exportID;
			this.progress = progress;
		}
		
		ProgressIndicator getProgress() {
			return progress;
		}

		@Override
		public void run() {
			try {
				result = exportHandler.createExportFiles(module, varColl, new Document(), samplesToExport, new ArrayList<>(), exportId, new HashMap<>(), new HashMap<>(), samplesToExport, progress);

				for (String step : exportHandler.getStepList())
					progress.addStep(step);
				progress.moveToNextStep();

				MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
		    	Number avgObjSize = (Number) mongoTemplate.getDb().runCommand(new Document("collStats", mongoTemplate.getCollectionName(VariantRunData.class))).get("avgObjSize");
		        int nQueryChunkSize = (int) (IExportHandler.nMaxChunkSizeInMb * 1024 * 1024 / avgObjSize.doubleValue());
		        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(exportFile))) {
		        	exportHandler.writeGenotypeFile(os, module, nQueryChunkSize, varColl, new Document(), null, result.values(), null, progress);
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

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
