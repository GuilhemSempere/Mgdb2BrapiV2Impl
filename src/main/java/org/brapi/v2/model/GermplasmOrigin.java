package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * MIAPPE V1.1 (DM-52)   MIAPPE V1.1 (DM-53)   MIAPPE V1.1 (DM-54)   MIAPPE V1.1 (DM-55)  MCPD (v2.1) (COORDUNCERT) 15.5   MCPD (v2.1) (ELEVATION) 16.   MCPD (v2.1) (GEOREFMETH) 15.7   MCPD (v2.1) (DECLATITUDE) 15.1   MCPD (v2.1) (DECLONGITUDE) 15.3 
 */
@Schema(description = "MIAPPE V1.1 (DM-52)   MIAPPE V1.1 (DM-53)   MIAPPE V1.1 (DM-54)   MIAPPE V1.1 (DM-55)  MCPD (v2.1) (COORDUNCERT) 15.5   MCPD (v2.1) (ELEVATION) 16.   MCPD (v2.1) (GEOREFMETH) 15.7   MCPD (v2.1) (DECLATITUDE) 15.1   MCPD (v2.1) (DECLONGITUDE) 15.3 ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-09-14T15:37:29.213Z[GMT]")


public class GermplasmOrigin   {
  @JsonProperty("coordinateUncertainty")
  private String coordinateUncertainty = null;

  @JsonProperty("coordinates")
  private GeoJSON coordinates = null;

  public GermplasmOrigin coordinateUncertainty(String coordinateUncertainty) {
    this.coordinateUncertainty = coordinateUncertainty;
    return this;
  }

  /**
   * Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.
   * @return coordinateUncertainty
   **/
  @Schema(example = "20", description = "Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.")
  
    public String getCoordinateUncertainty() {
    return coordinateUncertainty;
  }

  public void setCoordinateUncertainty(String coordinateUncertainty) {
    this.coordinateUncertainty = coordinateUncertainty;
  }

  public GermplasmOrigin coordinates(GeoJSON coordinates) {
    this.coordinates = coordinates;
    return this;
  }

  /**
   * Get coordinates
   * @return coordinates
   **/
  @Schema(description = "")
  
    @Valid
    public GeoJSON getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(GeoJSON coordinates) {
    this.coordinates = coordinates;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GermplasmOrigin germplasmOrigin = (GermplasmOrigin) o;
    return Objects.equals(this.coordinateUncertainty, germplasmOrigin.coordinateUncertainty) &&
        Objects.equals(this.coordinates, germplasmOrigin.coordinates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coordinateUncertainty, coordinates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmOrigin {\n");
    
    sb.append("    coordinateUncertainty: ").append(toIndentedString(coordinateUncertainty)).append("\n");
    sb.append("    coordinates: ").append(toIndentedString(coordinates)).append("\n");
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
