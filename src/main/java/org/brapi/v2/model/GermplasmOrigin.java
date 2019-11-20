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
 * GermplasmOrigin
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-20T14:32:35.470Z[GMT]")
public class GermplasmOrigin   {
  @JsonProperty("altitude")
  private String altitude = null;

  @JsonProperty("coordinateUncertainty")
  private String coordinateUncertainty = null;

  @JsonProperty("latitudeDecimal")
  private String latitudeDecimal = null;

  @JsonProperty("latitudeDegrees")
  private String latitudeDegrees = null;

  @JsonProperty("longitudeDecimal")
  private String longitudeDecimal = null;

  @JsonProperty("longitudeDegrees")
  private String longitudeDegrees = null;

  public GermplasmOrigin altitude(String altitude) {
    this.altitude = altitude;
    return this;
  }

  /**
   * Elevation of collecting site expressed in meters above sea level. Negative values are allowed.
   * @return altitude
  **/
  @ApiModelProperty(example = "35", value = "Elevation of collecting site expressed in meters above sea level. Negative values are allowed.")
  
    public String getAltitude() {
    return altitude;
  }

  public void setAltitude(String altitude) {
    this.altitude = altitude;
  }

  public GermplasmOrigin coordinateUncertainty(String coordinateUncertainty) {
    this.coordinateUncertainty = coordinateUncertainty;
    return this;
  }

  /**
   * Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.
   * @return coordinateUncertainty
  **/
  @ApiModelProperty(example = "20", value = "Uncertainty associated with the coordinates in meters. Leave the value empty if the uncertainty is unknown.")
  
    public String getCoordinateUncertainty() {
    return coordinateUncertainty;
  }

  public void setCoordinateUncertainty(String coordinateUncertainty) {
    this.coordinateUncertainty = coordinateUncertainty;
  }

  public GermplasmOrigin latitudeDecimal(String latitudeDecimal) {
    this.latitudeDecimal = latitudeDecimal;
    return this;
  }

  /**
   * Latitude expressed in decimal degrees. Positive values are North of the Equator; negative values are South of the Equator (e.g. -44.6975).
   * @return latitudeDecimal
  **/
  @ApiModelProperty(example = "-44.6975", value = "Latitude expressed in decimal degrees. Positive values are North of the Equator; negative values are South of the Equator (e.g. -44.6975).")
  
    public String getLatitudeDecimal() {
    return latitudeDecimal;
  }

  public void setLatitudeDecimal(String latitudeDecimal) {
    this.latitudeDecimal = latitudeDecimal;
  }

  public GermplasmOrigin latitudeDegrees(String latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
    return this;
  }

  /**
   * Degrees (2 digits) minutes (2 digits), and seconds (2 digits) followed by N (North) or S (South) (e.g. 103020S). Every missing digit (minutes or seconds) should be indicated with a hyphen. Leading zeros are required (e.g. 10
   * @return latitudeDegrees
  **/
  @ApiModelProperty(example = "103020S", value = "Degrees (2 digits) minutes (2 digits), and seconds (2 digits) followed by N (North) or S (South) (e.g. 103020S). Every missing digit (minutes or seconds) should be indicated with a hyphen. Leading zeros are required (e.g. 10")
  
    public String getLatitudeDegrees() {
    return latitudeDegrees;
  }

  public void setLatitudeDegrees(String latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
  }

  public GermplasmOrigin longitudeDecimal(String longitudeDecimal) {
    this.longitudeDecimal = longitudeDecimal;
    return this;
  }

  /**
   * Longitude expressed in decimal degrees. Positive values are East of the Greenwich Meridian; negative values are West of the Greenwich Meridian (e.g. +120.9123).
   * @return longitudeDecimal
  **/
  @ApiModelProperty(example = "+120.9123", value = "Longitude expressed in decimal degrees. Positive values are East of the Greenwich Meridian; negative values are West of the Greenwich Meridian (e.g. +120.9123).")
  
    public String getLongitudeDecimal() {
    return longitudeDecimal;
  }

  public void setLongitudeDecimal(String longitudeDecimal) {
    this.longitudeDecimal = longitudeDecimal;
  }

  public GermplasmOrigin longitudeDegrees(String longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
    return this;
  }

  /**
   * Degrees (3 digits), minutes (2 digits), and seconds (2 digits) followed by E (East) or W (West) (e.g. 0762510W). Every missing digit (minutes or seconds) should be indicated with a hyphen. Leading zeros are required (e.g. 076
   * @return longitudeDegrees
  **/
  @ApiModelProperty(example = "0762510W", value = "Degrees (3 digits), minutes (2 digits), and seconds (2 digits) followed by E (East) or W (West) (e.g. 0762510W). Every missing digit (minutes or seconds) should be indicated with a hyphen. Leading zeros are required (e.g. 076")
  
    public String getLongitudeDegrees() {
    return longitudeDegrees;
  }

  public void setLongitudeDegrees(String longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
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
    return Objects.equals(this.altitude, germplasmOrigin.altitude) &&
        Objects.equals(this.coordinateUncertainty, germplasmOrigin.coordinateUncertainty) &&
        Objects.equals(this.latitudeDecimal, germplasmOrigin.latitudeDecimal) &&
        Objects.equals(this.latitudeDegrees, germplasmOrigin.latitudeDegrees) &&
        Objects.equals(this.longitudeDecimal, germplasmOrigin.longitudeDecimal) &&
        Objects.equals(this.longitudeDegrees, germplasmOrigin.longitudeDegrees);
  }

  @Override
  public int hashCode() {
    return Objects.hash(altitude, coordinateUncertainty, latitudeDecimal, latitudeDegrees, longitudeDecimal, longitudeDegrees);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmOrigin {\n");
    
    sb.append("    altitude: ").append(toIndentedString(altitude)).append("\n");
    sb.append("    coordinateUncertainty: ").append(toIndentedString(coordinateUncertainty)).append("\n");
    sb.append("    latitudeDecimal: ").append(toIndentedString(latitudeDecimal)).append("\n");
    sb.append("    latitudeDegrees: ").append(toIndentedString(latitudeDegrees)).append("\n");
    sb.append("    longitudeDecimal: ").append(toIndentedString(longitudeDecimal)).append("\n");
    sb.append("    longitudeDegrees: ").append(toIndentedString(longitudeDegrees)).append("\n");
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
