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
 * ****************  /referencesets  ******************* This request maps to the body of &#x60;POST /referencesets/search&#x60; as JSON.
 */
@ApiModel(description = "****************  /referencesets  ******************* This request maps to the body of `POST /referencesets/search` as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class SearchReferenceSetsRequest   {
  @JsonProperty("accession")
  private String accession = null;

  @JsonProperty("assemblyPUI")
  private String assemblyPUI = null;

  @JsonProperty("md5checksum")
  private String md5checksum = null;

  @JsonProperty("page_size")
  private Integer pageSize = null;

  @JsonProperty("page_token")
  private String pageToken = null;

  public SearchReferenceSetsRequest accession(String accession) {
    this.accession = accession;
    return this;
  }

  /**
   * If unset, return the reference sets for which the `accession` matches this string (case-sensitive, exact match).
   * @return accession
  **/
  @ApiModelProperty(value = "If unset, return the reference sets for which the `accession` matches this string (case-sensitive, exact match).")
  
    public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public SearchReferenceSetsRequest assemblyPUI(String assemblyPUI) {
    this.assemblyPUI = assemblyPUI;
    return this;
  }

  /**
   * If unset, return the reference sets for which the `assemblyId` matches this string (case-sensitive, exact match).
   * @return assemblyPUI
  **/
  @ApiModelProperty(value = "If unset, return the reference sets for which the `assemblyId` matches this string (case-sensitive, exact match).")
  
    public String getAssemblyPUI() {
    return assemblyPUI;
  }

  public void setAssemblyPUI(String assemblyPUI) {
    this.assemblyPUI = assemblyPUI;
  }

  public SearchReferenceSetsRequest md5checksum(String md5checksum) {
    this.md5checksum = md5checksum;
    return this;
  }

  /**
   * If unset, return the reference sets for which the `md5checksum` matches this string (case-sensitive, exact match). See `ReferenceSet::md5checksum` for details.
   * @return md5checksum
  **/
  @ApiModelProperty(value = "If unset, return the reference sets for which the `md5checksum` matches this string (case-sensitive, exact match). See `ReferenceSet::md5checksum` for details.")
  
    public String getMd5checksum() {
    return md5checksum;
  }

  public void setMd5checksum(String md5checksum) {
    this.md5checksum = md5checksum;
  }

  public SearchReferenceSetsRequest pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * Specifies the maximum number of results to return in a single page. If unspecified, a system default will be used.
   * @return pageSize
  **/
  @ApiModelProperty(value = "Specifies the maximum number of results to return in a single page. If unspecified, a system default will be used.")
  
    public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public SearchReferenceSetsRequest pageToken(String pageToken) {
    this.pageToken = pageToken;
    return this;
  }

  /**
   * The continuation token, which is used to page through large result sets. To get the next page of results, set this parameter to the value of `next_page_token` from the previous response.
   * @return pageToken
  **/
  @ApiModelProperty(value = "The continuation token, which is used to page through large result sets. To get the next page of results, set this parameter to the value of `next_page_token` from the previous response.")
  
    public String getPageToken() {
    return pageToken;
  }

  public void setPageToken(String pageToken) {
    this.pageToken = pageToken;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchReferenceSetsRequest searchReferenceSetsRequest = (SearchReferenceSetsRequest) o;
    return Objects.equals(this.accession, searchReferenceSetsRequest.accession) &&
        Objects.equals(this.assemblyPUI, searchReferenceSetsRequest.assemblyPUI) &&
        Objects.equals(this.md5checksum, searchReferenceSetsRequest.md5checksum) &&
        Objects.equals(this.pageSize, searchReferenceSetsRequest.pageSize) &&
        Objects.equals(this.pageToken, searchReferenceSetsRequest.pageToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accession, assemblyPUI, md5checksum, pageSize, pageToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchReferenceSetsRequest {\n");
    
    sb.append("    accession: ").append(toIndentedString(accession)).append("\n");
    sb.append("    assemblyPUI: ").append(toIndentedString(assemblyPUI)).append("\n");
    sb.append("    md5checksum: ").append(toIndentedString(md5checksum)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    pageToken: ").append(toIndentedString(pageToken)).append("\n");
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
