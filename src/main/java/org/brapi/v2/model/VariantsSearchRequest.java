package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ******************  /variants  ********************* This request maps to the body of &#x60;POST /variants/search&#x60; as JSON.
 */
@ApiModel(description = "******************  /variants  ********************* This request maps to the body of `POST /variants/search` as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class VariantsSearchRequest   {
  @JsonProperty("callSetDbIds")
  @Valid
  private List<String> callSetDbIds = null;

  @JsonProperty("end")
  private String end = null;

  @JsonProperty("reference_name")
  private String referenceName = null;

  @JsonProperty("start")
  private String start = null;

  @JsonProperty("variantSetDbIds")
  @Valid
  private List<String> variantSetDbIds = null;

  public VariantsSearchRequest callSetDbIds(List<String> callSetDbIds) {
    this.callSetDbIds = callSetDbIds;
    return this;
  }

  public VariantsSearchRequest addCallSetDbIdsItem(String callSetDbIdsItem) {
    if (this.callSetDbIds == null) {
      this.callSetDbIds = new ArrayList<String>();
    }
    this.callSetDbIds.add(callSetDbIdsItem);
    return this;
  }

  /**
   * Only return variant calls which belong to call sets with these IDs. If unspecified, return all variants and no variant call objects.
   * @return callSetDbIds
  **/
  @ApiModelProperty(value = "Only return variant calls which belong to call sets with these IDs. If unspecified, return all variants and no variant call objects.")
  
    public List<String> getCallSetDbIds() {
    return callSetDbIds;
  }

  public void setCallSetDbIds(List<String> callSetDbIds) {
    this.callSetDbIds = callSetDbIds;
  }

  public VariantsSearchRequest end(String end) {
    this.end = end;
    return this;
  }

  /**
   * Required. The end of the window (0-based, exclusive) for which overlapping variants should be returned.
   * @return end
  **/
  @ApiModelProperty(value = "Required. The end of the window (0-based, exclusive) for which overlapping variants should be returned.")
  
    public String getEnd() {
    return end;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public VariantsSearchRequest referenceName(String referenceName) {
    this.referenceName = referenceName;
    return this;
  }

  /**
   * Required. Only return variants on this reference.
   * @return referenceName
  **/
  @ApiModelProperty(value = "Required. Only return variants on this reference.")
  
    public String getReferenceName() {
    return referenceName;
  }

  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
  }

  public VariantsSearchRequest start(String start) {
    this.start = start;
    return this;
  }

  /**
   * Required. The beginning of the window (0-based, inclusive) for which overlapping variants should be returned. Genomic positions are non-negative integers less than reference length. Requests spanning the join of circular genomes are represented as two requests one on each side of the join (position 0).
   * @return start
  **/
  @ApiModelProperty(value = "Required. The beginning of the window (0-based, inclusive) for which overlapping variants should be returned. Genomic positions are non-negative integers less than reference length. Requests spanning the join of circular genomes are represented as two requests one on each side of the join (position 0).")
  
    public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public VariantsSearchRequest variantSetDbIds(List<String> variantSetDbIds) {
    this.variantSetDbIds = variantSetDbIds;
    return this;
  }

  public VariantsSearchRequest addVariantSetDbIdsItem(String variantSetDbIdsItem) {
    if (this.variantSetDbIds == null) {
      this.variantSetDbIds = new ArrayList<String>();
    }
    this.variantSetDbIds.add(variantSetDbIdsItem);
    return this;
  }

  /**
   * The `VariantSet` to search.
   * @return variantSetDbIds
  **/
  @ApiModelProperty(value = "The `VariantSet` to search.")
  
    public List<String> getVariantSetDbIds() {
    return variantSetDbIds;
  }

  public void setVariantSetDbIds(List<String> variantSetDbIds) {
    this.variantSetDbIds = variantSetDbIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VariantsSearchRequest variantsSearchRequest = (VariantsSearchRequest) o;
    return Objects.equals(this.callSetDbIds, variantsSearchRequest.callSetDbIds) &&
        Objects.equals(this.end, variantsSearchRequest.end) &&
        Objects.equals(this.referenceName, variantsSearchRequest.referenceName) &&
        Objects.equals(this.start, variantsSearchRequest.start) &&
        Objects.equals(this.variantSetDbIds, variantsSearchRequest.variantSetDbIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(callSetDbIds, end, referenceName, start, variantSetDbIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VariantsSearchRequest {\n");
    
    sb.append("    callSetDbIds: ").append(toIndentedString(callSetDbIds)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    referenceName: ").append(toIndentedString(referenceName)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    variantSetDbIds: ").append(toIndentedString(variantSetDbIds)).append("\n");
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
