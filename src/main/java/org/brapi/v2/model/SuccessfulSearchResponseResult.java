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
 * SuccessfulSearchResponseResult
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:08.794Z[GMT]")
public class SuccessfulSearchResponseResult   {
  @JsonProperty("searchResultsDbId")
  private String searchResultsDbId = null;

  public SuccessfulSearchResponseResult searchResultsDbId(String searchResultsDbId) {
    this.searchResultsDbId = searchResultsDbId;
    return this;
  }

  /**
   * Get searchResultsDbId
   * @return searchResultsDbId
  **/
  @ApiModelProperty(example = "551ae08c", value = "")
  
    public String getSearchResultsDbId() {
    return searchResultsDbId;
  }

  public void setSearchResultsDbId(String searchResultsDbId) {
    this.searchResultsDbId = searchResultsDbId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessfulSearchResponseResult successfulSearchResponseResult = (SuccessfulSearchResponseResult) o;
    return Objects.equals(this.searchResultsDbId, successfulSearchResponseResult.searchResultsDbId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchResultsDbId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessfulSearchResponseResult {\n");
    
    sb.append("    searchResultsDbId: ").append(toIndentedString(searchResultsDbId)).append("\n");
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
