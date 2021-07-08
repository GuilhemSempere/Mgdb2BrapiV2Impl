package org.brapi.v2.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Program
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-06-24T08:33:25.155Z[GMT]")
public class Program extends ProgramNewRequest {
  @SerializedName("programDbId")
  private String programDbId = null;

  public Program programDbId(String programDbId) {
    this.programDbId = programDbId;
    return this;
  }

   /**
   * The ID which uniquely identifies the program
   * @return programDbId
  **/
  @ApiModelProperty(example = "f60f15b2", required = true, value = "The ID which uniquely identifies the program")
  public String getProgramDbId() {
    return programDbId;
  }

  public void setProgramDbId(String programDbId) {
    this.programDbId = programDbId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Program program = (Program) o;
    return Objects.equals(this.programDbId, program.programDbId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(programDbId, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Program {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    programDbId: ").append(toIndentedString(programDbId)).append("\n");
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
