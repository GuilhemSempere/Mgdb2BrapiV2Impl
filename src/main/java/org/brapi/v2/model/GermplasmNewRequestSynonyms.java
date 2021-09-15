package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * GermplasmNewRequestSynonyms
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-09-14T15:37:29.213Z[GMT]")


public class GermplasmNewRequestSynonyms   {
  @JsonProperty("synonym")
  private String synonym = null;

  @JsonProperty("type")
  private String type = null;

  public GermplasmNewRequestSynonyms synonym(String synonym) {
    this.synonym = synonym;
    return this;
  }

  /**
   * Alternative name or ID used to reference this germplasm
   * @return synonym
   **/
  @Schema(example = "variety_1", description = "Alternative name or ID used to reference this germplasm")
  
    public String getSynonym() {
    return synonym;
  }

  public void setSynonym(String synonym) {
    this.synonym = synonym;
  }

  public GermplasmNewRequestSynonyms type(String type) {
    this.type = type;
    return this;
  }

  /**
   * A descriptive classification for this synonym
   * @return type
   **/
  @Schema(example = "Pre-Code", description = "A descriptive classification for this synonym")
  
    public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GermplasmNewRequestSynonyms germplasmNewRequestSynonyms = (GermplasmNewRequestSynonyms) o;
    return Objects.equals(this.synonym, germplasmNewRequestSynonyms.synonym) &&
        Objects.equals(this.type, germplasmNewRequestSynonyms.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(synonym, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmNewRequestSynonyms {\n");
    
    sb.append("    synonym: ").append(toIndentedString(synonym)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
