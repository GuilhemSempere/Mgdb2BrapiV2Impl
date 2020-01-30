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
import org.threeten.bp.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * VariableBaseClass
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T11:58:54.700Z[GMT]")
public class VariableBaseClass   {
  @JsonProperty("additionalInfo")
  @Valid
  private Map<String, String> additionalInfo = null;

  @JsonProperty("commonCropName")
  private String commonCropName = null;

  @JsonProperty("contextOfUse")
  @Valid
  private List<String> contextOfUse = null;

  @JsonProperty("defaultValue")
  private String defaultValue = null;

  @JsonProperty("documentationURL")
  private String documentationURL = null;

  @JsonProperty("growthStage")
  private String growthStage = null;

  @JsonProperty("institution")
  private String institution = null;

  @JsonProperty("language")
  private String language = null;

  @JsonProperty("method")
  private Object method = null;

  @JsonProperty("ontologyReference")
  private OntologyReference ontologyReference = null;

  @JsonProperty("scale")
  private Object scale = null;

  @JsonProperty("scientist")
  private String scientist = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("submissionTimestamp")
  private OffsetDateTime submissionTimestamp = null;

  @JsonProperty("synonyms")
  @Valid
  private List<String> synonyms = null;

  @JsonProperty("trait")
  private Object trait = null;

  @JsonProperty("xref")
  private String xref = null;

  public VariableBaseClass additionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public VariableBaseClass putAdditionalInfoItem(String key, String additionalInfoItem) {
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

  public VariableBaseClass commonCropName(String commonCropName) {
    this.commonCropName = commonCropName;
    return this;
  }

  /**
   * Crop name (examples: \"Maize\", \"Wheat\")
   * @return commonCropName
  **/
  @ApiModelProperty(example = "Maize", value = "Crop name (examples: \"Maize\", \"Wheat\")")
  
    public String getCommonCropName() {
    return commonCropName;
  }

  public void setCommonCropName(String commonCropName) {
    this.commonCropName = commonCropName;
  }

  public VariableBaseClass contextOfUse(List<String> contextOfUse) {
    this.contextOfUse = contextOfUse;
    return this;
  }

  public VariableBaseClass addContextOfUseItem(String contextOfUseItem) {
    if (this.contextOfUse == null) {
      this.contextOfUse = new ArrayList<String>();
    }
    this.contextOfUse.add(contextOfUseItem);
    return this;
  }

  /**
   * Indication of how trait is routinely used. (examples: [\"Trial evaluation\", \"Nursery evaluation\"])
   * @return contextOfUse
  **/
  @ApiModelProperty(example = "[\"Trial evaluation\",\"Nursery evaluation\"]", value = "Indication of how trait is routinely used. (examples: [\"Trial evaluation\", \"Nursery evaluation\"])")
  
    public List<String> getContextOfUse() {
    return contextOfUse;
  }

  public void setContextOfUse(List<String> contextOfUse) {
    this.contextOfUse = contextOfUse;
  }

  public VariableBaseClass defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Variable default value. (examples: \"red\", \"2.3\", etc.)
   * @return defaultValue
  **/
  @ApiModelProperty(example = "2", value = "Variable default value. (examples: \"red\", \"2.3\", etc.)")
  
    public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public VariableBaseClass documentationURL(String documentationURL) {
    this.documentationURL = documentationURL;
    return this;
  }

  /**
   * A URL to the human readable documentation of this object
   * @return documentationURL
  **/
  @ApiModelProperty(example = "https://wiki.brapi.org/documentation.html", value = "A URL to the human readable documentation of this object")
  
    public String getDocumentationURL() {
    return documentationURL;
  }

  public void setDocumentationURL(String documentationURL) {
    this.documentationURL = documentationURL;
  }

  public VariableBaseClass growthStage(String growthStage) {
    this.growthStage = growthStage;
    return this;
  }

  /**
   * Growth stage at which measurement is made (examples: \"flowering\")
   * @return growthStage
  **/
  @ApiModelProperty(example = "flowering", value = "Growth stage at which measurement is made (examples: \"flowering\")")
  
    public String getGrowthStage() {
    return growthStage;
  }

  public void setGrowthStage(String growthStage) {
    this.growthStage = growthStage;
  }

  public VariableBaseClass institution(String institution) {
    this.institution = institution;
    return this;
  }

  /**
   * Name of institution submitting the variable
   * @return institution
  **/
  @ApiModelProperty(example = "The BrAPI Institute", value = "Name of institution submitting the variable")
  
    public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public VariableBaseClass language(String language) {
    this.language = language;
    return this;
  }

  /**
   * 2 letter ISO 639-1 code for the language of submission of the variable.
   * @return language
  **/
  @ApiModelProperty(example = "en", value = "2 letter ISO 639-1 code for the language of submission of the variable.")
  
    public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public VariableBaseClass method(Object method) {
    this.method = method;
    return this;
  }

  /**
   * Get method
   * @return method
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public Object getMethod() {
    return method;
  }

  public void setMethod(Object method) {
    this.method = method;
  }

  public VariableBaseClass ontologyReference(OntologyReference ontologyReference) {
    this.ontologyReference = ontologyReference;
    return this;
  }

  /**
   * Get ontologyReference
   * @return ontologyReference
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public OntologyReference getOntologyReference() {
    return ontologyReference;
  }

  public void setOntologyReference(OntologyReference ontologyReference) {
    this.ontologyReference = ontologyReference;
  }

  public VariableBaseClass scale(Object scale) {
    this.scale = scale;
    return this;
  }

  /**
   * Get scale
   * @return scale
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public Object getScale() {
    return scale;
  }

  public void setScale(Object scale) {
    this.scale = scale;
  }

  public VariableBaseClass scientist(String scientist) {
    this.scientist = scientist;
    return this;
  }

  /**
   * Name of scientist submitting the variable.
   * @return scientist
  **/
  @ApiModelProperty(example = "Dr. Bob Robertson", value = "Name of scientist submitting the variable.")
  
    public String getScientist() {
    return scientist;
  }

  public void setScientist(String scientist) {
    this.scientist = scientist;
  }

  public VariableBaseClass status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Variable status. (examples: \"recommended\", \"obsolete\", \"legacy\", etc.)
   * @return status
  **/
  @ApiModelProperty(example = "recommended", value = "Variable status. (examples: \"recommended\", \"obsolete\", \"legacy\", etc.)")
  
    public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public VariableBaseClass submissionTimestamp(OffsetDateTime submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  /**
   * Timestamp when the Variable was added (ISO 8601)
   * @return submissionTimestamp
  **/
  @ApiModelProperty(value = "Timestamp when the Variable was added (ISO 8601)")
  
    @Valid
    public OffsetDateTime getSubmissionTimestamp() {
    return submissionTimestamp;
  }

  public void setSubmissionTimestamp(OffsetDateTime submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
  }

  public VariableBaseClass synonyms(List<String> synonyms) {
    this.synonyms = synonyms;
    return this;
  }

  public VariableBaseClass addSynonymsItem(String synonymsItem) {
    if (this.synonyms == null) {
      this.synonyms = new ArrayList<String>();
    }
    this.synonyms.add(synonymsItem);
    return this;
  }

  /**
   * Other variable names
   * @return synonyms
  **/
  @ApiModelProperty(example = "[\"Maize Height\",\"Stalk Height\",\"Corn Height\"]", value = "Other variable names")
  
    public List<String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(List<String> synonyms) {
    this.synonyms = synonyms;
  }

  public VariableBaseClass trait(Object trait) {
    this.trait = trait;
    return this;
  }

  /**
   * Get trait
   * @return trait
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public Object getTrait() {
    return trait;
  }

  public void setTrait(Object trait) {
    this.trait = trait;
  }

  public VariableBaseClass xref(String xref) {
    this.xref = xref;
    return this;
  }

  /**
   * Cross reference of the variable term to a term from an external ontology or to a database of a major system.
   * @return xref
  **/
  @ApiModelProperty(example = "http://purl.obolibrary.org/obo/ro.owl", value = "Cross reference of the variable term to a term from an external ontology or to a database of a major system.")
  
    public String getXref() {
    return xref;
  }

  public void setXref(String xref) {
    this.xref = xref;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VariableBaseClass variableBaseClass = (VariableBaseClass) o;
    return Objects.equals(this.additionalInfo, variableBaseClass.additionalInfo) &&
        Objects.equals(this.commonCropName, variableBaseClass.commonCropName) &&
        Objects.equals(this.contextOfUse, variableBaseClass.contextOfUse) &&
        Objects.equals(this.defaultValue, variableBaseClass.defaultValue) &&
        Objects.equals(this.documentationURL, variableBaseClass.documentationURL) &&
        Objects.equals(this.growthStage, variableBaseClass.growthStage) &&
        Objects.equals(this.institution, variableBaseClass.institution) &&
        Objects.equals(this.language, variableBaseClass.language) &&
        Objects.equals(this.method, variableBaseClass.method) &&
        Objects.equals(this.ontologyReference, variableBaseClass.ontologyReference) &&
        Objects.equals(this.scale, variableBaseClass.scale) &&
        Objects.equals(this.scientist, variableBaseClass.scientist) &&
        Objects.equals(this.status, variableBaseClass.status) &&
        Objects.equals(this.submissionTimestamp, variableBaseClass.submissionTimestamp) &&
        Objects.equals(this.synonyms, variableBaseClass.synonyms) &&
        Objects.equals(this.trait, variableBaseClass.trait) &&
        Objects.equals(this.xref, variableBaseClass.xref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(additionalInfo, commonCropName, contextOfUse, defaultValue, documentationURL, growthStage, institution, language, method, ontologyReference, scale, scientist, status, submissionTimestamp, synonyms, trait, xref);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VariableBaseClass {\n");
    
    sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
    sb.append("    commonCropName: ").append(toIndentedString(commonCropName)).append("\n");
    sb.append("    contextOfUse: ").append(toIndentedString(contextOfUse)).append("\n");
    sb.append("    defaultValue: ").append(toIndentedString(defaultValue)).append("\n");
    sb.append("    documentationURL: ").append(toIndentedString(documentationURL)).append("\n");
    sb.append("    growthStage: ").append(toIndentedString(growthStage)).append("\n");
    sb.append("    institution: ").append(toIndentedString(institution)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    ontologyReference: ").append(toIndentedString(ontologyReference)).append("\n");
    sb.append("    scale: ").append(toIndentedString(scale)).append("\n");
    sb.append("    scientist: ").append(toIndentedString(scientist)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    submissionTimestamp: ").append(toIndentedString(submissionTimestamp)).append("\n");
    sb.append("    synonyms: ").append(toIndentedString(synonyms)).append("\n");
    sb.append("    trait: ").append(toIndentedString(trait)).append("\n");
    sb.append("    xref: ").append(toIndentedString(xref)).append("\n");
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
