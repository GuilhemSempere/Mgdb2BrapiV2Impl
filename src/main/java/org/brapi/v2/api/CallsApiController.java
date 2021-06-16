package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.CallsSearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.controller.GigwaMethods;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-03-22T14:25:44.495Z[GMT]")
@RestController
public class CallsApiController implements CallsApi {

    private static final Logger log = LoggerFactory.getLogger(CallsApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired private SearchApiController searchApiController;

    @org.springframework.beans.factory.annotation.Autowired
    public CallsApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<CallListResponse> callsGet(String callSetDbId, String variantDbId, String variantSetDbId, Boolean expandHomozygotes, String unknownString, String sepPhased, String sepUnphased, String pageToken, Integer pageSize, String authorization) throws SocketException, UnknownHostException, UnsupportedEncodingException {
		if (variantSetDbId == null && callSetDbId != null) {
			String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
			GenotypingSample sample = MongoTemplateManager.get(info[0]).find(new Query(Criteria.where("_id").is(Integer.parseInt(info[2]))), GenotypingSample.class).iterator().next();
			variantSetDbId = info[0] + GigwaMethods.ID_SEPARATOR + sample.getProjectId() + GigwaMethods.ID_SEPARATOR + sample.getRun();
		}
		
		CallsSearchRequest csr = new CallsSearchRequest();
		csr.setExpandHomozygotes(expandHomozygotes);
		csr.setUnknownString(unknownString);
		csr.setSepUnphased(sepUnphased);
		csr.setSepPhased(sepPhased);
		csr.setPageSize(pageSize);
		csr.setPageToken(pageToken);
		if (callSetDbId != null)
			csr.setCallSetDbIds(Arrays.asList(callSetDbId));
		if (variantDbId != null)
			csr.setVariantDbIds(Arrays.asList(variantDbId));
		if (variantSetDbId != null)
			csr.setVariantSetDbIds(Arrays.asList(variantSetDbId));
		
		return searchApiController.searchCallsPost(csr, authorization);
	}
}