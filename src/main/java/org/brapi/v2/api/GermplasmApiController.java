package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.Germplasm;
import org.brapi.v2.model.GermplasmListResponse;
import org.brapi.v2.model.GermplasmListResponseResult;
import org.brapi.v2.model.GermplasmMCPD;
import org.brapi.v2.model.GermplasmSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.ListValue;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.TokenPagination;
import org.brapi.v2.model.GermplasmNewRequest.BiologicalStatusOfAccessionCodeEnum;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.controller.GigwaMethods;
import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;
import jhi.brapi.api.germplasm.BrapiGermplasm;
import org.brapi.v2.model.GermplasmNewRequestStorageTypes;
import org.brapi.v2.model.GermplasmNewRequestStorageTypes.CodeEnum;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@CrossOrigin
@Controller
public class GermplasmApiController implements GermplasmApi {

    private static final Logger log = LoggerFactory.getLogger(GermplasmApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired AbstractTokenManager tokenManager;
    
    @Autowired private GigwaGa4ghServiceImpl ga4ghService;
    
    @Autowired private BrapiRestController brapiV1Service;

    @org.springframework.beans.factory.annotation.Autowired
    public GermplasmApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }


    public ResponseEntity<GermplasmListResponse> searchGermplasmPost(HttpServletResponse response, @ApiParam(value = "Germplasm Search request") @Valid @RequestBody GermplasmSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization)  throws Exception {
    	String token = ServerinfoApiController.readToken(authorization);

		if (body.getCommonCropNames() != null && body.getCommonCropNames().size() > 0)
			return new ResponseEntity<>(HttpStatus.OK);	// not supported

    	try {
			GermplasmListResponse glr = new GermplasmListResponse();
			GermplasmListResponseResult result = new GermplasmListResponseResult();
			Metadata metadata = new Metadata();
			glr.setMetadata(metadata);

			String programDbId = null;
			Integer projId = null;
			Collection<String> germplasmIdsToReturn = new HashSet<>(), requestedGermplasmIDs;
			if (body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty()) {
				if (body.getStudyDbIds().size() > 1) {
					Status status = new Status();
					status.setMessage("You may only supply a single studyDbId at a time!");
					metadata.addStatusItem(status);
					return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
				}
				String[] info = GigwaSearchVariantsRequest.getInfoFromId(body.getStudyDbIds().get(0), 2);
				programDbId = info[0];
				projId = Integer.parseInt(info[1]);
				germplasmIdsToReturn = MgdbDao.getProjectIndividuals(programDbId, projId);
			}
			else if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
				requestedGermplasmIDs = body.getGermplasmDbIds();
				for (String gpId : requestedGermplasmIDs) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 3);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0])) {
						Status status = new Status();
						status.setMessage("You may only supply IDs of germplasm records from one program at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
					}
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1]))) {
						Status status = new Status();
						status.setMessage("You may only supply a single studyDbId at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
					}
					germplasmIdsToReturn.add(info[2]);
				}
			}
			else {
				Status status = new Status();
				status.setMessage("Either a studyDbId or a list of germplasmDbIds must be specified as parameter!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
			}

   			if (!tokenManager.canUserReadProject(token, programDbId, projId))
   				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
 			
   			fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest gsr = new fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest();
   	    	gsr.accessionNumbers = body.getAccessionNumbers();
   	    	gsr.germplasmPUIs = body.getGermplasmPUIs();
   	    	gsr.germplasmGenus = body.getGermplasmGenus();
   	    	gsr.germplasmSpecies = body.getGermplasmSpecies();
   	    	gsr.germplasmNames = body.getGermplasmNames() == null ? null : body.getGermplasmNames().stream().map(nm -> nm.substring(1 + nm.lastIndexOf(GigwaMethods.ID_SEPARATOR))).collect(Collectors.toList());
   	    	gsr.germplasmDbIds = germplasmIdsToReturn;
   	    	gsr.page = body.getPage();
   	    	gsr.pageSize = body.getPageSize();
   	    	
   			Map<String, Object> v1response = (Map<String, Object>) brapiV1Service.executeGermplasmSearch(request, response, programDbId, gsr);
   			Map<String, Object> v1Result = (Map<String, Object>) v1response.get("result");
   	    	ArrayList<Map<String, Object>> v1data = (ArrayList<Map<String, Object>>) v1Result.get("data");
   	    	String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
   	    	for (Map<String, Object> v1germplasmRecord : v1data) {
    			Germplasm germplasm = new Germplasm();
    			
   	    		for (String key : v1germplasmRecord.keySet()) {
   	    			String sLCkey = key.toLowerCase();
   	    			Object val = v1germplasmRecord.get(key);
					if (!BrapiGermplasm.germplasmFields.containsKey(sLCkey) && !lowerCaseIdFieldName.equals(sLCkey)) {
						if ("additionalinfo".equals(sLCkey)) {
							for (String aiKey : ((HashMap<String, String>) val).keySet())
								germplasm.putAdditionalInfoItem(aiKey, ((HashMap<String, String>) val).get(aiKey));
						}
						else	
							germplasm.putAdditionalInfoItem(key, val.toString());
					}
					else {
						switch (sLCkey) {
							case "germplasmdbid":
								germplasm.germplasmDbId(ga4ghService.createId(programDbId, projId, val.toString()));
								break;
							case "germplasmname":
								germplasm.setGermplasmName(val.toString());
								break;
							case "defaultdisplayname":
								germplasm.setDefaultDisplayName(val.toString());
								break;
							case "accessionnumber":
								germplasm.setAccessionNumber(val.toString());
								break;
							case "germplasmpui":
								germplasm.setGermplasmPUI(val.toString());
								break;
							case "pedigree":
								germplasm.setPedigree(val.toString());
								break;
							case "seedsource":
								germplasm.setSeedSource(val.toString());
								break;
							case "commoncropname":
								germplasm.setCommonCropName(val.toString());
								break;
							case "institutecode":
								germplasm.setInstituteCode(val.toString());
								break;
							case "institutename":
								germplasm.setInstituteName(val.toString());
								break;
							case "biologicalstatusofaccessioncode":
								germplasm.setBiologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum.fromValue(val.toString()));
								break;
							case "countryoforigincode":
								germplasm.setCountryOfOriginCode(val.toString());
								break;
							case "typeofgermplasmstoragecode":
                                                                GermplasmNewRequestStorageTypes storageType = new GermplasmNewRequestStorageTypes();
                                                                storageType.setCode(CodeEnum.fromValue(val.toString()));
								germplasm.setStorageTypes(Arrays.asList(storageType));
								break;
							case "genus":
								germplasm.setGenus(val.toString());
								break;
							case "species":
								germplasm.setSpecies(val.toString());
								break;
							case "speciesauthority":
								germplasm.setSpeciesAuthority(val.toString());
								break;
							case "subtaxa":
								germplasm.setSubtaxa(val.toString());
								break;
							case "subtaxaauthority":
								germplasm.setSubtaxaAuthority(val.toString());
								break;
							case "acquisitiondate":
								try {
									germplasm.setAcquisitionDate(LocalDate.parse(val.toString()));
								}
								catch (DateTimeParseException dtpe){
									log.error("Unable to parse germplasm acquisition date: " + val);
								}
								break;
						}
					}
   	    		}
				result.addDataItem(germplasm);
   	    	}
			glr.setResult(result);
			IndexPagination pagination = new IndexPagination();
			jhi.brapi.api.Metadata v1Metadata = (jhi.brapi.api.Metadata) v1response.get("metadata");
			pagination.setPageSize(v1Metadata.getPagination().getPageSize());
			pagination.setCurrentPage(v1Metadata.getPagination().getCurrentPage());
			pagination.setTotalPages(v1Metadata.getPagination().getTotalPages());
			pagination.setTotalCount((int) v1Metadata.getPagination().getTotalCount());
			metadata.setPagination(pagination);			
			
			return new ResponseEntity<>(glr, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Couldn't serialize response for content type application/json", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}