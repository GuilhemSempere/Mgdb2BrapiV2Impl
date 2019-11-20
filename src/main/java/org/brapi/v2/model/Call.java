package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brapi.v2.model.ListValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A &#x60;Call&#x60; represents the determination of genotype with respect to a particular &#x60;Variant&#x60;.  It may include associated information such as quality and phasing. For example, a call might assign a probability of 0.32 to the occurrence of a SNP named rs1234 in a call set with the name NA12345.
 */
@ApiModel(description = "A `Call` represents the determination of genotype with respect to a particular `Variant`.  It may include associated information such as quality and phasing. For example, a call might assign a probability of 0.32 to the occurrence of a SNP named rs1234 in a call set with the name NA12345.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class Call   {
  @JsonProperty("additionalInfo")
  @Valid
  private Map<String, String> additionalInfo = null;

  @JsonProperty("callSetDbId")
  private String callSetDbId = null;

  @JsonProperty("callSetName")
  private String callSetName = null;

  @JsonProperty("genotype")
  private ListValue genotype = null;

  @JsonProperty("genotype_likelihood")
  @Valid
  private List<Double> genotypeLikelihood = null;

  @JsonProperty("phaseset")
  private String phaseset = null;

  @JsonProperty("variantDbId")
  private String variantDbId = null;

  @JsonProperty("variantName")
  private String variantName = null;

  public Call additionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public Call putAdditionalInfoItem(String key, String additionalInfoItem) {
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

  public Call callSetDbId(String callSetDbId) {
    this.callSetDbId = callSetDbId;
    return this;
  }

  /**
   * The ID of the call set this variant call belongs to.  If this field is not present, the ordering of the call sets from a `SearchCallSetsRequest` over this `VariantSet` is guaranteed to match the ordering of the calls on this `Variant`. The number of results will also be the same.
   * @return callSetDbId
  **/
  @ApiModelProperty(value = "The ID of the call set this variant call belongs to.  If this field is not present, the ordering of the call sets from a `SearchCallSetsRequest` over this `VariantSet` is guaranteed to match the ordering of the calls on this `Variant`. The number of results will also be the same.")
  
    public String getCallSetDbId() {
    return callSetDbId;
  }

  public void setCallSetDbId(String callSetDbId) {
    this.callSetDbId = callSetDbId;
  }

  public Call callSetName(String callSetName) {
    this.callSetName = callSetName;
    return this;
  }

  /**
   * The name of the call set this variant call belongs to. If this field is not present, the ordering of the call sets from a `SearchCallSetsRequest` over this `VariantSet` is guaranteed to match the ordering of the calls on this `Variant`. The number of results will also be the same.
   * @return callSetName
  **/
  @ApiModelProperty(value = "The name of the call set this variant call belongs to. If this field is not present, the ordering of the call sets from a `SearchCallSetsRequest` over this `VariantSet` is guaranteed to match the ordering of the calls on this `Variant`. The number of results will also be the same.")
  
    public String getCallSetName() {
    return callSetName;
  }

  public void setCallSetName(String callSetName) {
    this.callSetName = callSetName;
  }

  public Call genotype(ListValue genotype) {
    this.genotype = genotype;
    return this;
  }

  /**
   * Get genotype
   * @return genotype
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public ListValue getGenotype() {
    return genotype;
  }

  public void setGenotype(ListValue genotype) {
    this.genotype = genotype;
  }

  public Call genotypeLikelihood(List<Double> genotypeLikelihood) {
    this.genotypeLikelihood = genotypeLikelihood;
    return this;
  }

  public Call addGenotypeLikelihoodItem(Double genotypeLikelihoodItem) {
    if (this.genotypeLikelihood == null) {
      this.genotypeLikelihood = new ArrayList<Double>();
    }
    this.genotypeLikelihood.add(genotypeLikelihoodItem);
    return this;
  }

  /**
   * The genotype likelihoods for this variant call. Each array entry represents how likely a specific genotype is for this call as log10(P(data | genotype)), analogous to the GL tag in the VCF spec. The value ordering is defined by the GL tag in the VCF spec.
   * @return genotypeLikelihood
  **/
  @ApiModelProperty(value = "The genotype likelihoods for this variant call. Each array entry represents how likely a specific genotype is for this call as log10(P(data | genotype)), analogous to the GL tag in the VCF spec. The value ordering is defined by the GL tag in the VCF spec.")
  
    public List<Double> getGenotypeLikelihood() {
    return genotypeLikelihood;
  }

  public void setGenotypeLikelihood(List<Double> genotypeLikelihood) {
    this.genotypeLikelihood = genotypeLikelihood;
  }

  public Call phaseset(String phaseset) {
    this.phaseset = phaseset;
    return this;
  }

  /**
   * If this field is populated, this variant call's genotype ordering implies the phase of the bases and is consistent with any other variant calls on the same contig which have the same phaseset string.
   * @return phaseset
  **/
  @ApiModelProperty(value = "If this field is populated, this variant call's genotype ordering implies the phase of the bases and is consistent with any other variant calls on the same contig which have the same phaseset string.")
  
    public String getPhaseset() {
    return phaseset;
  }

  public void setPhaseset(String phaseset) {
    this.phaseset = phaseset;
  }

  public Call variantDbId(String variantDbId) {
    this.variantDbId = variantDbId;
    return this;
  }

  /**
   * The ID of the variant this call belongs to.
   * @return variantDbId
  **/
  @ApiModelProperty(value = "The ID of the variant this call belongs to.")
  
    public String getVariantDbId() {
    return variantDbId;
  }

  public void setVariantDbId(String variantDbId) {
    this.variantDbId = variantDbId;
  }

  public Call variantName(String variantName) {
    this.variantName = variantName;
    return this;
  }

  /**
   * The name of the variant this call belongs to.
   * @return variantName
  **/
  @ApiModelProperty(value = "The name of the variant this call belongs to.")
  
    public String getVariantName() {
    return variantName;
  }

  public void setVariantName(String variantName) {
    this.variantName = variantName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Call call = (Call) o;
    return Objects.equals(this.additionalInfo, call.additionalInfo) &&
        Objects.equals(this.callSetDbId, call.callSetDbId) &&
        Objects.equals(this.callSetName, call.callSetName) &&
        Objects.equals(this.genotype, call.genotype) &&
        Objects.equals(this.genotypeLikelihood, call.genotypeLikelihood) &&
        Objects.equals(this.phaseset, call.phaseset) &&
        Objects.equals(this.variantDbId, call.variantDbId) &&
        Objects.equals(this.variantName, call.variantName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalInfo, callSetDbId, callSetName, genotype, genotypeLikelihood, phaseset, variantDbId, variantName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Call {\n");
    
    sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
    sb.append("    callSetDbId: ").append(toIndentedString(callSetDbId)).append("\n");
    sb.append("    callSetName: ").append(toIndentedString(callSetName)).append("\n");
    sb.append("    genotype: ").append(toIndentedString(genotype)).append("\n");
    sb.append("    genotypeLikelihood: ").append(toIndentedString(genotypeLikelihood)).append("\n");
    sb.append("    phaseset: ").append(toIndentedString(phaseset)).append("\n");
    sb.append("    variantDbId: ").append(toIndentedString(variantDbId)).append("\n");
    sb.append("    variantName: ").append(toIndentedString(variantName)).append("\n");
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
