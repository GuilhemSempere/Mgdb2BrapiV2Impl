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
 * ******************  /callsets  ********************* This request maps to the body of &#x60;POST /search/callsets&#x60; as JSON.
 */
@ApiModel(description = "******************  /callsets  ********************* This request maps to the body of `POST /search/callsets` as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class CallSetsSearchRequest   {
  @JsonProperty("callSetDbIds")
  @Valid
  private List<String> callSetDbIds = null;

  @JsonProperty("callSetNames")
  @Valid
  private List<String> callSetNames = null;

  @JsonProperty("germplasmDbIds")
  @Valid
  private List<String> germplasmDbIds = null;

  @JsonProperty("germplasmNames")
  @Valid
  private List<String> germplasmNames = null;

  @JsonProperty("sampleDbIds")
  @Valid
  private List<String> sampleDbIds = null;

  @JsonProperty("sampleNames")
  @Valid
  private List<String> sampleNames = null;

  @JsonProperty("variantSetDbIds")
  @Valid
  private List<String> variantSetDbIds = null;

  public CallSetsSearchRequest callSetDbIds(List<String> callSetDbIds) {
    this.callSetDbIds = callSetDbIds;
    return this;
  }

  public CallSetsSearchRequest addCallSetDbIdsItem(String callSetDbIdsItem) {
    if (this.callSetDbIds == null) {
      this.callSetDbIds = new ArrayList<String>();
    }
    this.callSetDbIds.add(callSetDbIdsItem);
    return this;
  }

  /**
   * Only return call sets with these DbIds (case-sensitive, exact match).
   * @return callSetDbIds
  **/
  @ApiModelProperty(value = "Only return call sets with these DbIds (case-sensitive, exact match).")
  
    public List<String> getCallSetDbIds() {
    return callSetDbIds;
  }

  public void setCallSetDbIds(List<String> callSetDbIds) {
    this.callSetDbIds = callSetDbIds;
  }

  public CallSetsSearchRequest callSetNames(List<String> callSetNames) {
    this.callSetNames = callSetNames;
    return this;
  }

  public CallSetsSearchRequest addCallSetNamesItem(String callSetNamesItem) {
    if (this.callSetNames == null) {
      this.callSetNames = new ArrayList<String>();
    }
    this.callSetNames.add(callSetNamesItem);
    return this;
  }

  /**
   * Only return call sets with these names (case-sensitive, exact match).
   * @return callSetNames
  **/
  @ApiModelProperty(value = "Only return call sets with these names (case-sensitive, exact match).")
  
    public List<String> getCallSetNames() {
    return callSetNames;
  }

  public void setCallSetNames(List<String> callSetNames) {
    this.callSetNames = callSetNames;
  }

  public CallSetsSearchRequest germplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
    return this;
  }

  public CallSetsSearchRequest addGermplasmDbIdsItem(String germplasmDbIdsItem) {
    if (this.germplasmDbIds == null) {
      this.germplasmDbIds = new ArrayList<String>();
    }
    this.germplasmDbIds.add(germplasmDbIdsItem);
    return this;
  }

  /**
   * Return only call sets generated from the Sample of this germplasm
   * @return germplasmDbIds
  **/
  @ApiModelProperty(value = "Return only call sets generated from the Sample of this germplasm")
  
    public List<String> getGermplasmDbIds() {
    return germplasmDbIds;
  }

  public void setGermplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
  }

  public CallSetsSearchRequest germplasmNames(List<String> germplasmNames) {
    this.germplasmNames = germplasmNames;
    return this;
  }

  public CallSetsSearchRequest addGermplasmNamesItem(String germplasmNamesItem) {
    if (this.germplasmNames == null) {
      this.germplasmNames = new ArrayList<String>();
    }
    this.germplasmNames.add(germplasmNamesItem);
    return this;
  }

  /**
   * Return only call sets generated from the Sample of this germplasm
   * @return germplasmNames
  **/
  @ApiModelProperty(value = "Return only call sets generated from the Sample of this germplasm")
  
    public List<String> getGermplasmNames() {
    return germplasmNames;
  }

  public void setGermplasmNames(List<String> germplasmNames) {
    this.germplasmNames = germplasmNames;
  }

  public CallSetsSearchRequest sampleDbIds(List<String> sampleDbIds) {
    this.sampleDbIds = sampleDbIds;
    return this;
  }

  public CallSetsSearchRequest addSampleDbIdsItem(String sampleDbIdsItem) {
    if (this.sampleDbIds == null) {
      this.sampleDbIds = new ArrayList<String>();
    }
    this.sampleDbIds.add(sampleDbIdsItem);
    return this;
  }

  /**
   * Return only call sets generated from the provided Biosample ID.
   * @return sampleDbIds
  **/
  @ApiModelProperty(value = "Return only call sets generated from the provided Biosample ID.")
  
    public List<String> getSampleDbIds() {
    return sampleDbIds;
  }

  public void setSampleDbIds(List<String> sampleDbIds) {
    this.sampleDbIds = sampleDbIds;
  }

  public CallSetsSearchRequest sampleNames(List<String> sampleNames) {
    this.sampleNames = sampleNames;
    return this;
  }

  public CallSetsSearchRequest addSampleNamesItem(String sampleNamesItem) {
    if (this.sampleNames == null) {
      this.sampleNames = new ArrayList<String>();
    }
    this.sampleNames.add(sampleNamesItem);
    return this;
  }

  /**
   * Return only call sets generated from the provided Biosample ID.
   * @return sampleNames
  **/
  @ApiModelProperty(value = "Return only call sets generated from the provided Biosample ID.")
  
    public List<String> getSampleNames() {
    return sampleNames;
  }

  public void setSampleNames(List<String> sampleNames) {
    this.sampleNames = sampleNames;
  }

  public CallSetsSearchRequest variantSetDbIds(List<String> variantSetDbIds) {
    this.variantSetDbIds = variantSetDbIds;
    return this;
  }

  public CallSetsSearchRequest addVariantSetDbIdsItem(String variantSetDbIdsItem) {
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
    CallSetsSearchRequest callSetsSearchRequest = (CallSetsSearchRequest) o;
    return Objects.equals(this.callSetDbIds, callSetsSearchRequest.callSetDbIds) &&
        Objects.equals(this.callSetNames, callSetsSearchRequest.callSetNames) &&
        Objects.equals(this.germplasmDbIds, callSetsSearchRequest.germplasmDbIds) &&
        Objects.equals(this.germplasmNames, callSetsSearchRequest.germplasmNames) &&
        Objects.equals(this.sampleDbIds, callSetsSearchRequest.sampleDbIds) &&
        Objects.equals(this.sampleNames, callSetsSearchRequest.sampleNames) &&
        Objects.equals(this.variantSetDbIds, callSetsSearchRequest.variantSetDbIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(callSetDbIds, callSetNames, germplasmDbIds, germplasmNames, sampleDbIds, sampleNames, variantSetDbIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CallSetsSearchRequest {\n");
    
    sb.append("    callSetDbIds: ").append(toIndentedString(callSetDbIds)).append("\n");
    sb.append("    callSetNames: ").append(toIndentedString(callSetNames)).append("\n");
    sb.append("    germplasmDbIds: ").append(toIndentedString(germplasmDbIds)).append("\n");
    sb.append("    germplasmNames: ").append(toIndentedString(germplasmNames)).append("\n");
    sb.append("    sampleDbIds: ").append(toIndentedString(sampleDbIds)).append("\n");
    sb.append("    sampleNames: ").append(toIndentedString(sampleNames)).append("\n");
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
