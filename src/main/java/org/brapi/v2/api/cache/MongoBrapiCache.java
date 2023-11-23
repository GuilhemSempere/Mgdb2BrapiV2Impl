package org.brapi.v2.api.cache;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.brapi.v2.api.ServerinfoApi;
import org.brapi.v2.api.VariantsetsApi;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetAvailableFormats;
import org.brapi.v2.model.VariantSetAvailableFormats.DataFormatEnum;
import org.brapi.v2.model.VariantSetAvailableFormats.FileFormatEnum;
import org.brapi.v2.model.VariantSetMetadataFields;
import org.brapi.v2.model.VariantSetMetadataFields.DataTypeEnum;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.subtypes.Run;
import fr.cirad.tools.AppConfig;
import fr.cirad.tools.Helper;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.web.controller.BackOfficeController;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;

@Component
public class MongoBrapiCache {

	@Autowired private AppConfig appConfig;
	
    private static final Logger LOG = Logger.getLogger(MongoBrapiCache.class);
	
	private HttpServletRequest request;

	private String publicHostName;
	
    @Autowired
    public MongoBrapiCache(HttpServletRequest request) {
        this.request = request;
    }
	
	public VariantSet getVariantSet(MongoTemplate mongoTemplate, String variantSetDbId) throws SocketException, UnknownHostException {
		VariantSet variantSet = mongoTemplate.findById(variantSetDbId, VariantSet.class, VariantSet.BRAPI_CACHE_COLL_VARIANTSET);
    	if (variantSet == null) {
    		long before = System.currentTimeMillis();
    		variantSet = new VariantSet();
//          Analysis analysisItem = new Analysis();
//	        analysisItem.setAnalysisDbId(ga4ghVariantSet.getId());
//	        analysisItem.setType("TODO: check how to deal with this field");
//          variantSet.addAnalysisItem(analysisItem);
			
			String[] splitId = variantSetDbId.split(Helper.ID_SEPARATOR);
			int projId = Integer.parseInt(splitId[1]);
			variantSet.setStudyDbId(splitId[0] + Helper.ID_SEPARATOR + projId);
			variantSet.setReferenceSetDbId(variantSet.getStudyDbId());
			variantSet.setVariantSetDbId(variantSetDbId);
			variantSet.setVariantSetName(splitId[2]);
//    	    variantSet.setCallSetCount(mongoTemplate.findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(proj.getId())), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class).size());
			variantSet.setCallSetCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class));
			variantSet.setVariantCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where(VariantData.FIELDNAME_RUNS + "." + Run.FIELDNAME_RUNNAME).is(splitId[2]), Criteria.where(VariantData.FIELDNAME_RUNS + "." + Run.FIELDNAME_PROJECT_ID).is(projId))), VariantData.class));

			if (variantSet.getCallSetCount() == 0 && variantSet.getVariantCount() == 0)
				return null;	// this run probably doesn't exist
                        
            //try retrieving metadata information from DBVCFHeader collection                        
            Document filter = new Document();
            filter.put("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_PROJECT, projId); 
            filter.put("_id." + DBVCFHeader.VcfHeaderId.FIELDNAME_RUN, splitId[2]);                        

            MongoCollection<Document> vcfHeadersColl = mongoTemplate.getCollection(MongoTemplateManager.getMongoCollectionName(DBVCFHeader.class));           
            MongoCursor<Document> headerCursor = vcfHeadersColl.find(filter).iterator();

            while (headerCursor.hasNext()) {
                DBVCFHeader dbVcfHeader = DBVCFHeader.fromDocument(headerCursor.next());
                Map<String, VCFFormatHeaderLine> vcfMetadata = dbVcfHeader.getmFormatMetaData();
                if (vcfMetadata != null) {
                    for (String key:vcfMetadata.keySet()) {
                        if (!key.equals("GT")) {
                            VariantSetMetadataFields field = new VariantSetMetadataFields();
                            VCFHeaderLineType type = vcfMetadata.get(key).getType();
                            DataTypeEnum brapiType = DataTypeEnum.fromValue(type.toString().toLowerCase());
                            field.setDataType(brapiType);
                            field.setFieldAbbreviation(key);
                            field.setFieldName(vcfMetadata.get(key).getDescription());
                            variantSet.addMetadataFieldsItem(field);
                        }
                    }
                }     
            }
                        
			mongoTemplate.save(variantSet, VariantSet.BRAPI_CACHE_COLL_VARIANTSET);
			LOG.info("VariantSet cache generated for " + variantSetDbId + " in " + (System.currentTimeMillis() - before) + "ms");
    	}
    	
		List<VariantSetAvailableFormats> formatList = new ArrayList<VariantSetAvailableFormats>();
		VariantSetAvailableFormats format = new VariantSetAvailableFormats();
    	format.setDataFormat(DataFormatEnum.FLAPJACK);
    	format.setFileFormat(FileFormatEnum.TEXT_TSV);
    	formatList.add(format);
    	// we keep PLINK disabled for now because we have no way of providing the map file
//    	format = new VariantSetAvailableFormats();
//    	format.setDataFormat(DataFormatEnum.PLINK);
//    	format.setFileFormat(FileFormatEnum.TEXT_TSV);
//    	formatList.add(format);
    	format = new VariantSetAvailableFormats();
    	format.setDataFormat(DataFormatEnum.VCF);
    	format.setFileFormat(FileFormatEnum.TEXT_TSV);
    	formatList.add(format);
		variantSet.setAvailableFormats(formatList);
		
    	// construct export URLs dynamically because we might get wrong URLs in cases where multiple instances are connected to a same DB
    	String sWebAppRoot = appConfig.get("enforcedWebapRootUrl");
    	for (VariantSetAvailableFormats someFormat : variantSet.getAvailableFormats())
    		someFormat.setFileURL((sWebAppRoot == null ? getPublicHostName(request) + request.getContextPath() : sWebAppRoot) + request.getServletPath() + ServerinfoApi.URL_BASE_PREFIX + "/" + VariantsetsApi.variantsetsExportIntoFormat_url.replace("{variantSetDbId}", variantSetDbId).replace("{dataFormat}", someFormat.getDataFormat().toString()));

		return variantSet;
	}

	private String getPublicHostName(HttpServletRequest req) throws SocketException, UnknownHostException {
		if (publicHostName == null)
			publicHostName = BackOfficeController.determinePublicHostName(req);	/*FIXME: not sure this project should depend on role_manager*/
		return publicHostName;
	}
}
