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
 * ******************  /variantsets  ********************* This request maps to the body of &#x60;POST /search/variantsets&#x60; as JSON.
 */
@ApiModel(description = "******************  /variantsets  ********************* This request maps to the body of `POST /search/variantsets` as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class VariantSetsSearchRequest   {
  @JsonProperty("studyDbIds")
  @Valid
  private List<String> studyDbIds = null;

  @JsonProperty("variantSetDbIds")
  @Valid
  private List<String> variantSetDbIds = null;

  public VariantSetsSearchRequest studyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
    return this;
  }

  public VariantSetsSearchRequest addStudyDbIdsItem(String studyDbIdsItem) {
    if (this.studyDbIds == null) {
      this.studyDbIds = new ArrayList<String>();
    }
    this.studyDbIds.add(studyDbIdsItem);
    return this;
  }

  /**
   * The `Dataset` to search.
   * @return studyDbIds
  **/
  @ApiModelProperty(value = "The `Dataset` to search.")
  
    public List<String> getStudyDbIds() {
    return studyDbIds;
  }

  public void setStudyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
  }

  public VariantSetsSearchRequest variantSetDbIds(List<String> variantSetDbIds) {
    this.variantSetDbIds = variantSetDbIds;
    return this;
  }

  public VariantSetsSearchRequest addVariantSetDbIdsItem(String variantSetDbIdsItem) {
    if (this.variantSetDbIds == null) {
      this.variantSetDbIds = new ArrayList<String>();
    }
    this.variantSetDbIds.add(variantSetDbIdsItem);
    return this;
  }

  /**
   * The VariantSet to search.
   * @return variantSetDbIds
  **/
  @ApiModelProperty(value = "The VariantSet to search.")
  
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
    VariantSetsSearchRequest variantSetsSearchRequest = (VariantSetsSearchRequest) o;
    return Objects.equals(this.studyDbIds, variantSetsSearchRequest.studyDbIds) &&
        Objects.equals(this.variantSetDbIds, variantSetsSearchRequest.variantSetDbIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(studyDbIds, variantSetDbIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VariantSetsSearchRequest {\n");
    
    sb.append("    studyDbIds: ").append(toIndentedString(studyDbIds)).append("\n");
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
