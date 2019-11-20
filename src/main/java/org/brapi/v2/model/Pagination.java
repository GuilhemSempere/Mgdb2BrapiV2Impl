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
 * The pagination object is applicable only when the payload contains a \&quot;data\&quot; key.  It describes the pagination of the data contained in the \&quot;data\&quot; array, as a way to identify which subset of data is being returned.  Pages are zero indexed, so the first page will be page 0 (zero).
 */
@ApiModel(description = "The pagination object is applicable only when the payload contains a \"data\" key.  It describes the pagination of the data contained in the \"data\" array, as a way to identify which subset of data is being returned.  Pages are zero indexed, so the first page will be page 0 (zero).")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:08.794Z[GMT]")
public class Pagination   {
  @JsonProperty("currentPage")
  private Integer currentPage = 0;

  @JsonProperty("pageSize")
  private String pageSize = "1000";

  @JsonProperty("totalCount")
  private Integer totalCount = null;

  @JsonProperty("totalPages")
  private Integer totalPages = null;

  public Pagination currentPage(Integer currentPage) {
    this.currentPage = currentPage;
    return this;
  }

  /**
   * Get currentPage
   * @return currentPage
  **/
  @ApiModelProperty(example = "0", required = true, value = "")
      @NotNull

    public Integer getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(Integer currentPage) {
    this.currentPage = currentPage;
  }

  public Pagination pageSize(String pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * Get pageSize
   * @return pageSize
  **/
  @ApiModelProperty(example = "1000", required = true, value = "")
      @NotNull

    public String getPageSize() {
    return pageSize;
  }

  public void setPageSize(String pageSize) {
    this.pageSize = pageSize;
  }

  public Pagination totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  /**
   * Get totalCount
   * @return totalCount
  **/
  @ApiModelProperty(example = "1", value = "")
  
    public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public Pagination totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * Get totalPages
   * @return totalPages
  **/
  @ApiModelProperty(example = "1", value = "")
  
    public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pagination pagination = (Pagination) o;
    return Objects.equals(this.currentPage, pagination.currentPage) &&
        Objects.equals(this.pageSize, pagination.pageSize) &&
        Objects.equals(this.totalCount, pagination.totalCount) &&
        Objects.equals(this.totalPages, pagination.totalPages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentPage, pageSize, totalCount, totalPages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Pagination {\n");
    
    sb.append("    currentPage: ").append(toIndentedString(currentPage)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
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
