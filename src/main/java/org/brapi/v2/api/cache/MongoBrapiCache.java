package org.brapi.v2.api.cache;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import fr.cirad.mgdb.model.mongo.maintypes.DBVCFHeader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.api.ServerinfoApi;
import org.brapi.v2.api.VariantsetsApi;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetAvailableFormats;
import org.brapi.v2.model.VariantSetAvailableFormats.DataFormatEnum;
import org.brapi.v2.model.VariantSetAvailableFormats.FileFormatEnum;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.tools.AppConfig;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.web.controller.BackOfficeController;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.Map;
import org.brapi.v2.model.VariantSetMetadataFields;
import org.brapi.v2.model.VariantSetMetadataFields.DataTypeEnum;
import org.bson.Document;

@Component
public class MongoBrapiCache {

	public static final String BRAPI_CACHE_COLL_VARIANTSET = "brapiCache_VariantSet";

	@Autowired private AppConfig appConfig;
	
	private HttpServletRequest request;

	private String publicHostName;
	
    @Autowired
    public MongoBrapiCache(HttpServletRequest request) {
        this.request = request;
    }
	
	public VariantSet getVariantSet(MongoTemplate mongoTemplate, String variantSetDbId) throws SocketException, UnknownHostException {
		VariantSet variantSet = mongoTemplate.findById(variantSetDbId, VariantSet.class, BRAPI_CACHE_COLL_VARIANTSET);
    	if (variantSet == null) {
    		long before = System.currentTimeMillis();
    		variantSet = new VariantSet();
//          Analysis analysisItem = new Analysis();
//	        analysisItem.setAnalysisDbId(ga4ghVariantSet.getId());
//	        analysisItem.setType("TODO: check how to deal with this field");
//          variantSet.addAnalysisItem(analysisItem);
			
			String[] splitId = variantSetDbId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
			int projId = Integer.parseInt(splitId[1]);
			variantSet.setStudyDbId(splitId[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + projId);
			variantSet.setReferenceSetDbId(variantSet.getStudyDbId());
			variantSet.setVariantSetDbId(variantSetDbId);
			variantSet.setVariantSetName(splitId[2]);
//    	    variantSet.setCallSetCount(mongoTemplate.findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(proj.getId())), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class).size());
			variantSet.setCallSetCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class));
//    	    variantSet.setVariantCount((int) mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantData.class)).estimatedDocumentCount() /* this counts all variants in the database */);
			variantSet.setVariantCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(projId), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(splitId[2]))), VariantRunData.class));

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
                        
			mongoTemplate.save(variantSet, BRAPI_CACHE_COLL_VARIANTSET);
			Log.debug("VariantSet cache generated for " + variantSetDbId + " in " + (System.currentTimeMillis() - before) / 1000 + "s");
    	}
    	
		List<VariantSetAvailableFormats> formatList = new ArrayList<VariantSetAvailableFormats>();
		VariantSetAvailableFormats format = new VariantSetAvailableFormats();
    	format.setDataFormat(DataFormatEnum.FLAPJACK);
    	format.setFileFormat(FileFormatEnum.TEXT_TSV);
    	formatList.add(format);
    	// we keep PLINK disabled now because we have no way of providing the map file
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

	private String getPublicHostName(HttpServletRequest request2) throws SocketException, UnknownHostException {
		if (publicHostName == null)
			publicHostName = BackOfficeController.determinePublicHostName(request);
		return publicHostName;
	}
}
