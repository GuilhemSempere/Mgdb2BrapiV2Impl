package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import org.brapi.v2.model.OntologyReferenceDocumentationLinks;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * OntologyReference
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:08.794Z[GMT]")
public class OntologyReference   {
  @JsonProperty("documentationLinks")
  @Valid
  private List<OntologyReferenceDocumentationLinks> documentationLinks = null;

  @JsonProperty("ontologyDbId")
  private String ontologyDbId = null;

  @JsonProperty("ontologyName")
  private String ontologyName = null;

  @JsonProperty("version")
  private String version = null;

  public OntologyReference documentationLinks(List<OntologyReferenceDocumentationLinks> documentationLinks) {
    this.documentationLinks = documentationLinks;
    return this;
  }

  public OntologyReference addDocumentationLinksItem(OntologyReferenceDocumentationLinks documentationLinksItem) {
    if (this.documentationLinks == null) {
      this.documentationLinks = new ArrayList<OntologyReferenceDocumentationLinks>();
    }
    this.documentationLinks.add(documentationLinksItem);
    return this;
  }

  /**
   * links to various ontology documentation
   * @return documentationLinks
  **/
  @ApiModelProperty(value = "links to various ontology documentation")
      @Valid
    public List<OntologyReferenceDocumentationLinks> getDocumentationLinks() {
    return documentationLinks;
  }

  public void setDocumentationLinks(List<OntologyReferenceDocumentationLinks> documentationLinks) {
    this.documentationLinks = documentationLinks;
  }

  public OntologyReference ontologyDbId(String ontologyDbId) {
    this.ontologyDbId = ontologyDbId;
    return this;
  }

  /**
   * Ontology database unique identifier
   * @return ontologyDbId
  **/
  @ApiModelProperty(example = "6b071868", required = true, value = "Ontology database unique identifier")
      @NotNull

    public String getOntologyDbId() {
    return ontologyDbId;
  }

  public void setOntologyDbId(String ontologyDbId) {
    this.ontologyDbId = ontologyDbId;
  }

  public OntologyReference ontologyName(String ontologyName) {
    this.ontologyName = ontologyName;
    return this;
  }

  /**
   * Ontology name
   * @return ontologyName
  **/
  @ApiModelProperty(example = "The Crop Ontology", required = true, value = "Ontology name")
      @NotNull

    public String getOntologyName() {
    return ontologyName;
  }

  public void setOntologyName(String ontologyName) {
    this.ontologyName = ontologyName;
  }

  public OntologyReference version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Ontology version (no specific format)
   * @return version
  **/
  @ApiModelProperty(example = "7.2.3", value = "Ontology version (no specific format)")
  
    public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OntologyReference ontologyReference = (OntologyReference) o;
    return Objects.equals(this.documentationLinks, ontologyReference.documentationLinks) &&
        Objects.equals(this.ontologyDbId, ontologyReference.ontologyDbId) &&
        Objects.equals(this.ontologyName, ontologyReference.ontologyName) &&
        Objects.equals(this.version, ontologyReference.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentationLinks, ontologyDbId, ontologyName, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OntologyReference {\n");
    
    sb.append("    documentationLinks: ").append(toIndentedString(documentationLinks)).append("\n");
    sb.append("    ontologyDbId: ").append(toIndentedString(ontologyDbId)).append("\n");
    sb.append("    ontologyName: ").append(toIndentedString(ontologyName)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
