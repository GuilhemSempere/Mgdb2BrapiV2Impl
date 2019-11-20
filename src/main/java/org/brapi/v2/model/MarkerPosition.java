package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * MarkerPosition
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class MarkerPosition   {
  @JsonProperty("additionalInfo")
  @Valid
  private Map<String, String> additionalInfo = null;

  @JsonProperty("linkageGroupName")
  private String linkageGroupName = null;

  @JsonProperty("mapDbId")
  private String mapDbId = null;

  @JsonProperty("mapName")
  private String mapName = null;

  @JsonProperty("markerDbId")
  private String markerDbId = null;

  @JsonProperty("markerName")
  private String markerName = null;

  @JsonProperty("position")
  private Integer position = null;

  public MarkerPosition additionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public MarkerPosition putAdditionalInfoItem(String key, String additionalInfoItem) {
    if (this.additionalInfo == null) {
      this.additionalInfo = new HashMap<String, String>();
    }
    this.additionalInfo.put(key, additionalInfoItem);
    return this;
  }

  /**
   * Additional arbitrary info
   * @return additionalInfo
  **/
  @ApiModelProperty(value = "Additional arbitrary info")
  
    public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public MarkerPosition linkageGroupName(String linkageGroupName) {
    this.linkageGroupName = linkageGroupName;
    return this;
  }

  /**
   * The Uniquely Identifiable name of this linkage group
   * @return linkageGroupName
  **/
  @ApiModelProperty(example = "Chromosome 3", value = "The Uniquely Identifiable name of this linkage group")
  
    public String getLinkageGroupName() {
    return linkageGroupName;
  }

  public void setLinkageGroupName(String linkageGroupName) {
    this.linkageGroupName = linkageGroupName;
  }

  public MarkerPosition mapDbId(String mapDbId) {
    this.mapDbId = mapDbId;
    return this;
  }

  /**
   * The unique ID of the map
   * @return mapDbId
  **/
  @ApiModelProperty(example = "3d52bdf3", value = "The unique ID of the map")
  
    public String getMapDbId() {
    return mapDbId;
  }

  public void setMapDbId(String mapDbId) {
    this.mapDbId = mapDbId;
  }

  public MarkerPosition mapName(String mapName) {
    this.mapName = mapName;
    return this;
  }

  /**
   * The human readbale name of the map
   * @return mapName
  **/
  @ApiModelProperty(example = "Genome Map 1", value = "The human readbale name of the map")
  
    public String getMapName() {
    return mapName;
  }

  public void setMapName(String mapName) {
    this.mapName = mapName;
  }

  public MarkerPosition markerDbId(String markerDbId) {
    this.markerDbId = markerDbId;
    return this;
  }

  /**
   * Internal db identifier
   * @return markerDbId
  **/
  @ApiModelProperty(example = "a1eb250a", value = "Internal db identifier")
  
    public String getMarkerDbId() {
    return markerDbId;
  }

  public void setMarkerDbId(String markerDbId) {
    this.markerDbId = markerDbId;
  }

  public MarkerPosition markerName(String markerName) {
    this.markerName = markerName;
    return this;
  }

  /**
   * The human readable name for a marker
   * @return markerName
  **/
  @ApiModelProperty(example = "Marker_2390", value = "The human readable name for a marker")
  
    public String getMarkerName() {
    return markerName;
  }

  public void setMarkerName(String markerName) {
    this.markerName = markerName;
  }

  public MarkerPosition position(Integer position) {
    this.position = position;
    return this;
  }

  /**
   * The position of a marker within a linkage group
   * @return position
  **/
  @ApiModelProperty(example = "2390", value = "The position of a marker within a linkage group")
  
    public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarkerPosition markerPosition = (MarkerPosition) o;
    return Objects.equals(this.additionalInfo, markerPosition.additionalInfo) &&
        Objects.equals(this.linkageGroupName, markerPosition.linkageGroupName) &&
        Objects.equals(this.mapDbId, markerPosition.mapDbId) &&
        Objects.equals(this.mapName, markerPosition.mapName) &&
        Objects.equals(this.markerDbId, markerPosition.markerDbId) &&
        Objects.equals(this.markerName, markerPosition.markerName) &&
        Objects.equals(this.position, markerPosition.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalInfo, linkageGroupName, mapDbId, mapName, markerDbId, markerName, position);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarkerPosition {\n");
    
    sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
    sb.append("    linkageGroupName: ").append(toIndentedString(linkageGroupName)).append("\n");
    sb.append("    mapDbId: ").append(toIndentedString(mapDbId)).append("\n");
    sb.append("    mapName: ").append(toIndentedString(mapName)).append("\n");
    sb.append("    markerDbId: ").append(toIndentedString(markerDbId)).append("\n");
    sb.append("    markerName: ").append(toIndentedString(markerName)).append("\n");
    sb.append("    position: ").append(toIndentedString(position)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
