package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.threetenbp.ser.OffsetTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.threeten.bp.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * GermplasmAttributeValueNewRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-09-14T15:37:29.213Z[GMT]")


public class GermplasmAttributeValueNewRequest   {
  @JsonProperty("additionalInfo")
  @Valid
  private Map<String, String> additionalInfo = null;

  @JsonProperty("attributeDbId")
  private String attributeDbId = null;

  @JsonProperty("attributeName")
  private String attributeName = null;

  @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
  @JsonSerialize(using = OffsetTimeSerializer.class)
  @JsonProperty("determinedDate")
  private OffsetDateTime determinedDate = null;

  @JsonProperty("externalReferences")
  private ExternalReferences externalReferences = null;

  @JsonProperty("germplasmDbId")
  private String germplasmDbId = null;

  @JsonProperty("germplasmName")
  private String germplasmName = null;

  @JsonProperty("value")
  private String value = null;

  public GermplasmAttributeValueNewRequest additionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public GermplasmAttributeValueNewRequest putAdditionalInfoItem(String key, String additionalInfoItem) {
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
  @Schema(description = "Additional arbitrary info")
  
    public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public GermplasmAttributeValueNewRequest attributeDbId(String attributeDbId) {
    this.attributeDbId = attributeDbId;
    return this;
  }

  /**
   * The ID which uniquely identifies this attribute within the given database server
   * @return attributeDbId
   **/
  @Schema(example = "e529dd5a", description = "The ID which uniquely identifies this attribute within the given database server")
  
    public String getAttributeDbId() {
    return attributeDbId;
  }

  public void setAttributeDbId(String attributeDbId) {
    this.attributeDbId = attributeDbId;
  }

  public GermplasmAttributeValueNewRequest attributeName(String attributeName) {
    this.attributeName = attributeName;
    return this;
  }

  /**
   * A human readable name for this attribute
   * @return attributeName
   **/
  @Schema(example = "Weevil Resistance", required = true, description = "A human readable name for this attribute")
      @NotNull

    public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public GermplasmAttributeValueNewRequest determinedDate(OffsetDateTime determinedDate) {
    this.determinedDate = determinedDate;
    return this;
  }

  /**
   * The date the value of this attribute was determined for a given germplasm
   * @return determinedDate
   **/
  @Schema(description = "The date the value of this attribute was determined for a given germplasm")
  
    @Valid
    public OffsetDateTime getDeterminedDate() {
    return determinedDate;
  }

  public void setDeterminedDate(OffsetDateTime determinedDate) {
    this.determinedDate = determinedDate;
  }

  public GermplasmAttributeValueNewRequest externalReferences(ExternalReferences externalReferences) {
    this.externalReferences = externalReferences;
    return this;
  }

  /**
   * Get externalReferences
   * @return externalReferences
   **/
  @Schema(description = "")
  
    @Valid
    public ExternalReferences getExternalReferences() {
    return externalReferences;
  }

  public void setExternalReferences(ExternalReferences externalReferences) {
    this.externalReferences = externalReferences;
  }

  public GermplasmAttributeValueNewRequest germplasmDbId(String germplasmDbId) {
    this.germplasmDbId = germplasmDbId;
    return this;
  }

  /**
   * The ID which uniquely identifies a germplasm within the given database server
   * @return germplasmDbId
   **/
  @Schema(example = "d4076594", description = "The ID which uniquely identifies a germplasm within the given database server")
  
    public String getGermplasmDbId() {
    return germplasmDbId;
  }

  public void setGermplasmDbId(String germplasmDbId) {
    this.germplasmDbId = germplasmDbId;
  }

  public GermplasmAttributeValueNewRequest germplasmName(String germplasmName) {
    this.germplasmName = germplasmName;
    return this;
  }

  /**
   * Name of the germplasm. It can be the preferred name and does not have to be unique.
   * @return germplasmName
   **/
  @Schema(example = "A0000003", description = "Name of the germplasm. It can be the preferred name and does not have to be unique.")
  
    public String getGermplasmName() {
    return germplasmName;
  }

  public void setGermplasmName(String germplasmName) {
    this.germplasmName = germplasmName;
  }

  public GermplasmAttributeValueNewRequest value(String value) {
    this.value = value;
    return this;
  }

  /**
   * The value of this attribute for a given germplasm
   * @return value
   **/
  @Schema(example = "Present", description = "The value of this attribute for a given germplasm")
  
    public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GermplasmAttributeValueNewRequest germplasmAttributeValueNewRequest = (GermplasmAttributeValueNewRequest) o;
    return Objects.equals(this.additionalInfo, germplasmAttributeValueNewRequest.additionalInfo) &&
        Objects.equals(this.attributeDbId, germplasmAttributeValueNewRequest.attributeDbId) &&
        Objects.equals(this.attributeName, germplasmAttributeValueNewRequest.attributeName) &&
        Objects.equals(this.determinedDate, germplasmAttributeValueNewRequest.determinedDate) &&
        Objects.equals(this.externalReferences, germplasmAttributeValueNewRequest.externalReferences) &&
        Objects.equals(this.germplasmDbId, germplasmAttributeValueNewRequest.germplasmDbId) &&
        Objects.equals(this.germplasmName, germplasmAttributeValueNewRequest.germplasmName) &&
        Objects.equals(this.value, germplasmAttributeValueNewRequest.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalInfo, attributeDbId, attributeName, determinedDate, externalReferences, germplasmDbId, germplasmName, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmAttributeValueNewRequest {\n");
    
    sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
    sb.append("    attributeDbId: ").append(toIndentedString(attributeDbId)).append("\n");
    sb.append("    attributeName: ").append(toIndentedString(attributeName)).append("\n");
    sb.append("    determinedDate: ").append(toIndentedString(determinedDate)).append("\n");
    sb.append("    externalReferences: ").append(toIndentedString(externalReferences)).append("\n");
    sb.append("    germplasmDbId: ").append(toIndentedString(germplasmDbId)).append("\n");
    sb.append("    germplasmName: ").append(toIndentedString(germplasmName)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
