package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;
import org.threeten.bp.OffsetDateTime;
import org.brapi.v2.model.SampleNewRequest;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Sample
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
public class Sample extends SampleNewRequest  {
  @JsonProperty("sampleDbId")
  private String sampleDbId = null;

  public Sample sampleDbId(String sampleDbId) {
    this.sampleDbId = sampleDbId;
    return this;
  }

  /**
   * The ID which uniquely identifies a sample
   * @return sampleDbId
  **/
  @ApiModelProperty(example = "cd06a61d", value = "The ID which uniquely identifies a sample")
  
    public String getSampleDbId() {
    return sampleDbId;
  }

  public void setSampleDbId(String sampleDbId) {
    this.sampleDbId = sampleDbId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sample sample = (Sample) o;
    return Objects.equals(this.sampleDbId, sample.sampleDbId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sampleDbId, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Sample {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    sampleDbId: ").append(toIndentedString(sampleDbId)).append("\n");
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
