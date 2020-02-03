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
 * ****************  /references  ******************* This request maps to the body of &#x60;POST /references/search&#x60; as JSON.
 */
@ApiModel(description = "****************  /references  ******************* This request maps to the body of `POST /references/search` as JSON.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class SearchReferencesRequest   {
  @JsonProperty("accession")
  private String accession = null;

  @JsonProperty("md5checksum")
  private String md5checksum = null;

  @JsonProperty("pageSize")
  private Integer pageSize = null;

  @JsonProperty("page")
  private Integer page = null;

  @JsonProperty("referenceSetDbId")
  private String referenceSetDbId = null;

  public SearchReferencesRequest accession(String accession) {
    this.accession = accession;
    return this;
  }

  /**
   * If specified, return the references for which the `accession` matches this string (case-sensitive, exact match).
   * @return accession
  **/
  @ApiModelProperty(value = "If specified, return the references for which the `accession` matches this string (case-sensitive, exact match).")
  
    public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public SearchReferencesRequest md5checksum(String md5checksum) {
    this.md5checksum = md5checksum;
    return this;
  }

  /**
   * If specified, return the references for which the `md5checksum` matches this string (case-sensitive, exact match). See `ReferenceSet::md5checksum` for details.
   * @return md5checksum
  **/
  @ApiModelProperty(value = "If specified, return the references for which the `md5checksum` matches this string (case-sensitive, exact match). See `ReferenceSet::md5checksum` for details.")
  
    public String getMd5checksum() {
    return md5checksum;
  }

  public void setMd5checksum(String md5checksum) {
    this.md5checksum = md5checksum;
  }

  public SearchReferencesRequest pageSize(Integer pageSize) {
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

  public SearchReferencesRequest page(Integer page) {
    this.page = page;
    return this;
  }

  @ApiModelProperty(value = "The page number.")
  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public SearchReferencesRequest referenceSetDbId(String referenceSetDbId) {
    this.referenceSetDbId = referenceSetDbId;
    return this;
  }

  /**
   * The `ReferenceSet` to search.
   * @return referenceSetDbId
  **/
  @ApiModelProperty(value = "The `ReferenceSet` to search.")
  
    public String getReferenceSetDbId() {
    return referenceSetDbId;
  }

  public void setReferenceSetDbId(String referenceSetDbId) {
    this.referenceSetDbId = referenceSetDbId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchReferencesRequest searchReferencesRequest = (SearchReferencesRequest) o;
    return Objects.equals(this.accession, searchReferencesRequest.accession) &&
        Objects.equals(this.md5checksum, searchReferencesRequest.md5checksum) &&
        Objects.equals(this.pageSize, searchReferencesRequest.pageSize) &&
        Objects.equals(this.page, searchReferencesRequest.page) &&
        Objects.equals(this.referenceSetDbId, searchReferencesRequest.referenceSetDbId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accession, md5checksum, pageSize, page, referenceSetDbId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchReferencesRequest {\n");
    
    sb.append("    accession: ").append(toIndentedString(accession)).append("\n");
    sb.append("    md5checksum: ").append(toIndentedString(md5checksum)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    referenceSetDbId: ").append(toIndentedString(referenceSetDbId)).append("\n");
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
