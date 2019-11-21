package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * The response from &#x60;POST /listreferencebases&#x60; expressed as JSON.
 */
@ApiModel(description = "The response from `POST /listreferencebases` expressed as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class ReferenceBases   {

  @JsonProperty("offset")
  private String offset = null;

  @JsonProperty("sequence")
  private String sequence = null;

  public ReferenceBases nextPageToken(String nextPageToken) {
    return this;
  }

  /**
   * The offset position (0-based) of the given sequence from the start of this `Reference`. This value will differ for each page in a paginated request.
   * @return offset
  **/
  @ApiModelProperty(value = "The offset position (0-based) of the given sequence from the start of this `Reference`. This value will differ for each page in a paginated request.")
  
    public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }

  public ReferenceBases sequence(String sequence) {
    this.sequence = sequence;
    return this;
  }

  /**
   * A substring of the bases that make up this reference. Bases are represented as IUPAC-IUB codes; this string matches the regexp `[ACGTMRWSYKVHDBN]*`.
   * @return sequence
  **/
  @ApiModelProperty(value = "A substring of the bases that make up this reference. Bases are represented as IUPAC-IUB codes; this string matches the regexp `[ACGTMRWSYKVHDBN]*`.")
  
    public String getSequence() {
    return sequence;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceBases referenceBases = (ReferenceBases) o;
    return Objects.equals(this.offset, referenceBases.offset) &&
        Objects.equals(this.sequence, referenceBases.sequence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReferenceBases {\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    sequence: ").append(toIndentedString(sequence)).append("\n");
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
