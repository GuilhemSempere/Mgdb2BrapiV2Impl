package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.brapi.v2.model.GermplasmNewRequest;
import org.brapi.v2.model.GermplasmNewRequestDonors;
import org.brapi.v2.model.GermplasmOrigin;
import org.brapi.v2.model.TaxonID;
import java.util.List;
import java.util.Map;
import org.threeten.bp.LocalDate;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Germplasm
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-20T14:32:35.470Z[GMT]")
public class Germplasm extends GermplasmNewRequest  {
  @JsonProperty("germplasmDbId")
  private String germplasmDbId = null;

  public Germplasm germplasmDbId(String germplasmDbId) {
    this.germplasmDbId = germplasmDbId;
    return this;
  }

  /**
   * The ID which uniquely identifies a germplasm within the given database server
   * @return germplasmDbId
  **/
  @ApiModelProperty(example = "d4076594", required = true, value = "The ID which uniquely identifies a germplasm within the given database server")
      @NotNull

    public String getGermplasmDbId() {
    return germplasmDbId;
  }

  public void setGermplasmDbId(String germplasmDbId) {
    this.germplasmDbId = germplasmDbId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Germplasm germplasm = (Germplasm) o;
    return Objects.equals(this.germplasmDbId, germplasm.germplasmDbId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(germplasmDbId, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Germplasm {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    germplasmDbId: ").append(toIndentedString(germplasmDbId)).append("\n");
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
