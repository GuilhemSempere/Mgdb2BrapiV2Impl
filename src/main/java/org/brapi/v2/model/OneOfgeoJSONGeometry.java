package org.brapi.v2.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


/**
* OneOfgeoJSONGeometry
*/
//@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.WRAPPER_OBJECT)
//@JsonSubTypes({       
//    @JsonSubTypes.Type(value=PointGeometry.class, name="PointGeometry"),
//    @JsonSubTypes.Type(value=PolygonGeometry.class, name="PolygonGeometry")       
//}) 
public interface OneOfgeoJSONGeometry {

}
