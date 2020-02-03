package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.threeten.bp.LocalDate;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TrialNewRequestDatasetAuthorships
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
public class TrialNewRequestDatasetAuthorships   {
  @JsonProperty("datasetPUI")
  private String datasetPUI = null;

  @JsonProperty("license")
  private String license = null;

  @JsonProperty("publicReleaseDate")
  private LocalDate publicReleaseDate = null;

  @JsonProperty("submissionDate")
  private LocalDate submissionDate = null;

  public TrialNewRequestDatasetAuthorships datasetPUI(String datasetPUI) {
    this.datasetPUI = datasetPUI;
    return this;
  }

  /**
   * Get datasetPUI
   * @return datasetPUI
  **/
  @ApiModelProperty(example = "doi:10.15454/312953986E3", value = "")
  
    public String getDatasetPUI() {
    return datasetPUI;
  }

  public void setDatasetPUI(String datasetPUI) {
    this.datasetPUI = datasetPUI;
  }

  public TrialNewRequestDatasetAuthorships license(String license) {
    this.license = license;
    return this;
  }

  /**
   * Get license
   * @return license
  **/
  @ApiModelProperty(example = "https://CreativeCommons.org/licenses/by/4.0", value = "")
  
    public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public TrialNewRequestDatasetAuthorships publicReleaseDate(LocalDate publicReleaseDate) {
    this.publicReleaseDate = publicReleaseDate;
    return this;
  }

  /**
   * Get publicReleaseDate
   * @return publicReleaseDate
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public LocalDate getPublicReleaseDate() {
    return publicReleaseDate;
  }

  public void setPublicReleaseDate(LocalDate publicReleaseDate) {
    this.publicReleaseDate = publicReleaseDate;
  }

  public TrialNewRequestDatasetAuthorships submissionDate(LocalDate submissionDate) {
    this.submissionDate = submissionDate;
    return this;
  }

  /**
   * Get submissionDate
   * @return submissionDate
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public LocalDate getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(LocalDate submissionDate) {
    this.submissionDate = submissionDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrialNewRequestDatasetAuthorships trialNewRequestDatasetAuthorships = (TrialNewRequestDatasetAuthorships) o;
    return Objects.equals(this.datasetPUI, trialNewRequestDatasetAuthorships.datasetPUI) &&
        Objects.equals(this.license, trialNewRequestDatasetAuthorships.license) &&
        Objects.equals(this.publicReleaseDate, trialNewRequestDatasetAuthorships.publicReleaseDate) &&
        Objects.equals(this.submissionDate, trialNewRequestDatasetAuthorships.submissionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasetPUI, license, publicReleaseDate, submissionDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrialNewRequestDatasetAuthorships {\n");
    
    sb.append("    datasetPUI: ").append(toIndentedString(datasetPUI)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    publicReleaseDate: ").append(toIndentedString(publicReleaseDate)).append("\n");
    sb.append("    submissionDate: ").append(toIndentedString(submissionDate)).append("\n");
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
