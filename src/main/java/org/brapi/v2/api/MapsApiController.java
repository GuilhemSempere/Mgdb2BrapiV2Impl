package org.brapi.v2.api;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")

@Controller
public class MapsApiController implements MapsApi {

    private static final Logger log = LoggerFactory.getLogger(MapsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public MapsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

//    public ResponseEntity<GenomeMapListResponse> mapsGet(@ApiParam(value = "The common name of the crop, found from \"GET /commoncropnames\"") @Valid @RequestParam(value = "commonCropName", required = false) String commonCropName,@ApiParam(value = "The DOI or other permanent identifier for this genomic map") @Valid @RequestParam(value = "mapPUI", required = false) String mapPUI,@ApiParam(value = "Full scientific binomial format name. This includes Genus, Species, and Sub-species") @Valid @RequestParam(value = "scientificName", required = false) String scientificName,@ApiParam(value = "Type of map", allowableValues = "physical, genomic") @Valid @RequestParam(value = "type", required = false) String type,@ApiParam(value = "Unique Id to filter by Program") @Valid @RequestParam(value = "programDbId", required = false) String programDbId,@ApiParam(value = "Unique Id to filter by Trial") @Valid @RequestParam(value = "trialDbId", required = false) String trialDbId,@ApiParam(value = "Unique Id to filter by Study") @Valid @RequestParam(value = "studyDbId", required = false) String studyDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GenomeMapListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"commonCropName\" : \"Paw Paw\",\n      \"documentationURL\" : \"https://brapi.org\",\n      \"mapDbId\" : \"142cffd5\",\n      \"comments\" : \"Comments about this map\",\n      \"mapPUI\" : \"doi:10.3207/2959859860\",\n      \"scientificName\" : \"Asimina triloba\",\n      \"type\" : \"Genetic\",\n      \"unit\" : \"cM\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"markerCount\" : 1100,\n      \"mapName\" : \"Genome Map 1\",\n      \"publishedDate\" : \"2000-01-23T04:56:07.000+00:00\",\n      \"linkageGroupCount\" : 5\n    }, {\n      \"commonCropName\" : \"Paw Paw\",\n      \"documentationURL\" : \"https://brapi.org\",\n      \"mapDbId\" : \"142cffd5\",\n      \"comments\" : \"Comments about this map\",\n      \"mapPUI\" : \"doi:10.3207/2959859860\",\n      \"scientificName\" : \"Asimina triloba\",\n      \"type\" : \"Genetic\",\n      \"unit\" : \"cM\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"markerCount\" : 1100,\n      \"mapName\" : \"Genome Map 1\",\n      \"publishedDate\" : \"2000-01-23T04:56:07.000+00:00\",\n      \"linkageGroupCount\" : 5\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GenomeMapListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GenomeMapListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GenomeMapListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<GenomeMapSingleResponse> mapsMapDbIdGet(@ApiParam(value = "The internal db id of a selected map",required=true) @PathVariable("mapDbId") String mapDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<GenomeMapSingleResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"commonCropName\" : \"Paw Paw\",\n    \"documentationURL\" : \"https://brapi.org\",\n    \"mapDbId\" : \"142cffd5\",\n    \"comments\" : \"Comments about this map\",\n    \"mapPUI\" : \"doi:10.3207/2959859860\",\n    \"scientificName\" : \"Asimina triloba\",\n    \"type\" : \"Genetic\",\n    \"unit\" : \"cM\",\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"markerCount\" : 1100,\n    \"mapName\" : \"Genome Map 1\",\n    \"publishedDate\" : \"2000-01-23T04:56:07.000+00:00\",\n    \"linkageGroupCount\" : 5\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GenomeMapSingleResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<GenomeMapSingleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<GenomeMapSingleResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }
//
//    public ResponseEntity<LinkageGroupListResponse> mapsMapDbIdLinkagegroupsGet(@ApiParam(value = "The internal db id of a selected map",required=true) @PathVariable("mapDbId") String mapDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<LinkageGroupListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"linkageGroupName\" : \"Chromosome 3\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"markerCount\" : 150,\n      \"maxPosition\" : 2500\n    }, {\n      \"linkageGroupName\" : \"Chromosome 3\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"markerCount\" : 150,\n      \"maxPosition\" : 2500\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", LinkageGroupListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<LinkageGroupListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<LinkageGroupListResponse>(HttpStatus.NOT_IMPLEMENTED);
//    }

}
