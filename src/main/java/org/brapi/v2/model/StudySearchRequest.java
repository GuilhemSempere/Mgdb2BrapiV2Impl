package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * StudySearchRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
public class StudySearchRequest   {
  @JsonProperty("active")
  private Boolean active = null;

  @JsonProperty("commonCropNames")
  @Valid
  private List<String> commonCropNames = null;

  @JsonProperty("germplasmDbIds")
  @Valid
  private List<String> germplasmDbIds = null;

  @JsonProperty("locationDbIds")
  @Valid
  private List<String> locationDbIds = null;

  @JsonProperty("observationVariableDbIds")
  @Valid
  private List<String> observationVariableDbIds = null;

  @JsonProperty("programDbIds")
  @Valid
  private List<String> programDbIds = null;

  @JsonProperty("programNames")
  @Valid
  private List<String> programNames = null;

  @JsonProperty("seasonDbIds")
  @Valid
  private List<String> seasonDbIds = null;

  /**
   * Name of one of the fields within the study object on which results can be sorted
   */
  public enum SortByEnum {
    STUDYDBID("studyDbId"),
    
    TRIALDBID("trialDbId"),
    
    PROGRAMDBID("programDbId"),
    
    LOCATIONDBID("locationDbId"),
    
    SEASONDBID("seasonDbId"),
    
    STUDYTYPE("studyType"),
    
    STUDYNAME("studyName"),
    
    STUDYLOCATION("studyLocation"),
    
    PROGRAMNAME("programName"),
    
    GERMPLASMDBID("germplasmDbId"),
    
    OBSERVATIONVARIABLEDBID("observationVariableDbId");

    private String value;

    SortByEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SortByEnum fromValue(String text) {
      for (SortByEnum b : SortByEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("sortBy")
  private SortByEnum sortBy = null;

  /**
   * Order results should be sorted. ex. \"ASC\" or \"DESC\"
   */
  public enum SortOrderEnum {
    ASC("ASC"),
    
    DESC("DESC");

    private String value;

    SortOrderEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SortOrderEnum fromValue(String text) {
      for (SortOrderEnum b : SortOrderEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("sortOrder")
  private SortOrderEnum sortOrder = null;

  @JsonProperty("studyDbIds")
  @Valid
  private List<String> studyDbIds = null;

  @JsonProperty("studyNames")
  @Valid
  private List<String> studyNames = null;

  @JsonProperty("studyTypes")
  @Valid
  private List<String> studyTypes = null;

  @JsonProperty("trialDbIds")
  @Valid
  private List<String> trialDbIds = null;

  public StudySearchRequest active(Boolean active) {
    this.active = active;
    return this;
  }

  /**
   * Is this study currently active
   * @return active
  **/
  @ApiModelProperty(example = "true", value = "Is this study currently active")
  
    public Boolean isActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public StudySearchRequest commonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
    return this;
  }

  public StudySearchRequest addCommonCropNamesItem(String commonCropNamesItem) {
    if (this.commonCropNames == null) {
      this.commonCropNames = new ArrayList<String>();
    }
    this.commonCropNames.add(commonCropNamesItem);
    return this;
  }

  /**
   * Common names for the crop associated with this study
   * @return commonCropNames
  **/
  @ApiModelProperty(example = "[\"Tomatillo\",\"Paw Paw\"]", value = "Common names for the crop associated with this study")
  
    public List<String> getCommonCropNames() {
    return commonCropNames;
  }

  public void setCommonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
  }

  public StudySearchRequest germplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
    return this;
  }

  public StudySearchRequest addGermplasmDbIdsItem(String germplasmDbIdsItem) {
    if (this.germplasmDbIds == null) {
      this.germplasmDbIds = new ArrayList<String>();
    }
    this.germplasmDbIds.add(germplasmDbIdsItem);
    return this;
  }

  /**
   * List of IDs which uniquely identify germplasm
   * @return germplasmDbIds
  **/
  @ApiModelProperty(example = "[\"fa4ad588\",\"5731ebe2\"]", value = "List of IDs which uniquely identify germplasm")
  
    public List<String> getGermplasmDbIds() {
    return germplasmDbIds;
  }

  public void setGermplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
  }

  public StudySearchRequest locationDbIds(List<String> locationDbIds) {
    this.locationDbIds = locationDbIds;
    return this;
  }

  public StudySearchRequest addLocationDbIdsItem(String locationDbIdsItem) {
    if (this.locationDbIds == null) {
      this.locationDbIds = new ArrayList<String>();
    }
    this.locationDbIds.add(locationDbIdsItem);
    return this;
  }

  /**
   * List of location names to filter search results
   * @return locationDbIds
  **/
  @ApiModelProperty(example = "[\"d6e7c6a9\",\"8f9f6916\"]", value = "List of location names to filter search results")
  
    public List<String> getLocationDbIds() {
    return locationDbIds;
  }

  public void setLocationDbIds(List<String> locationDbIds) {
    this.locationDbIds = locationDbIds;
  }

  public StudySearchRequest observationVariableDbIds(List<String> observationVariableDbIds) {
    this.observationVariableDbIds = observationVariableDbIds;
    return this;
  }

  public StudySearchRequest addObservationVariableDbIdsItem(String observationVariableDbIdsItem) {
    if (this.observationVariableDbIds == null) {
      this.observationVariableDbIds = new ArrayList<String>();
    }
    this.observationVariableDbIds.add(observationVariableDbIdsItem);
    return this;
  }

  /**
   * List of observation variable IDs to search for
   * @return observationVariableDbIds
  **/
  @ApiModelProperty(example = "[\"819e508f\",\"f540b703\"]", value = "List of observation variable IDs to search for")
  
    public List<String> getObservationVariableDbIds() {
    return observationVariableDbIds;
  }

  public void setObservationVariableDbIds(List<String> observationVariableDbIds) {
    this.observationVariableDbIds = observationVariableDbIds;
  }

  public StudySearchRequest programDbIds(List<String> programDbIds) {
    this.programDbIds = programDbIds;
    return this;
  }

  public StudySearchRequest addProgramDbIdsItem(String programDbIdsItem) {
    if (this.programDbIds == null) {
      this.programDbIds = new ArrayList<String>();
    }
    this.programDbIds.add(programDbIdsItem);
    return this;
  }

  /**
   * List of program identifiers to filter search results
   * @return programDbIds
  **/
  @ApiModelProperty(example = "[\"9a855886\",\"51697c22\"]", value = "List of program identifiers to filter search results")
  
    public List<String> getProgramDbIds() {
    return programDbIds;
  }

  public void setProgramDbIds(List<String> programDbIds) {
    this.programDbIds = programDbIds;
  }

  public StudySearchRequest programNames(List<String> programNames) {
    this.programNames = programNames;
    return this;
  }

  public StudySearchRequest addProgramNamesItem(String programNamesItem) {
    if (this.programNames == null) {
      this.programNames = new ArrayList<String>();
    }
    this.programNames.add(programNamesItem);
    return this;
  }

  /**
   * List of program names to filter search results
   * @return programNames
  **/
  @ApiModelProperty(example = "[\"Better Breeding Program\",\"Best Breeding Program\"]", value = "List of program names to filter search results")
  
    public List<String> getProgramNames() {
    return programNames;
  }

  public void setProgramNames(List<String> programNames) {
    this.programNames = programNames;
  }

  public StudySearchRequest seasonDbIds(List<String> seasonDbIds) {
    this.seasonDbIds = seasonDbIds;
    return this;
  }

  public StudySearchRequest addSeasonDbIdsItem(String seasonDbIdsItem) {
    if (this.seasonDbIds == null) {
      this.seasonDbIds = new ArrayList<String>();
    }
    this.seasonDbIds.add(seasonDbIdsItem);
    return this;
  }

  /**
   * The ID which uniquely identifies a season
   * @return seasonDbIds
  **/
  @ApiModelProperty(example = "[\"Harvest Two 2017\",\"Summer 2018\"]", value = "The ID which uniquely identifies a season")
  
    public List<String> getSeasonDbIds() {
    return seasonDbIds;
  }

  public void setSeasonDbIds(List<String> seasonDbIds) {
    this.seasonDbIds = seasonDbIds;
  }

  public StudySearchRequest sortBy(SortByEnum sortBy) {
    this.sortBy = sortBy;
    return this;
  }

  /**
   * Name of one of the fields within the study object on which results can be sorted
   * @return sortBy
  **/
  @ApiModelProperty(value = "Name of one of the fields within the study object on which results can be sorted")
  
    public SortByEnum getSortBy() {
    return sortBy;
  }

  public void setSortBy(SortByEnum sortBy) {
    this.sortBy = sortBy;
  }

  public StudySearchRequest sortOrder(SortOrderEnum sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Order results should be sorted. ex. \"ASC\" or \"DESC\"
   * @return sortOrder
  **/
  @ApiModelProperty(value = "Order results should be sorted. ex. \"ASC\" or \"DESC\"")
  
    public SortOrderEnum getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(SortOrderEnum sortOrder) {
    this.sortOrder = sortOrder;
  }

  public StudySearchRequest studyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
    return this;
  }

  public StudySearchRequest addStudyDbIdsItem(String studyDbIdsItem) {
    if (this.studyDbIds == null) {
      this.studyDbIds = new ArrayList<String>();
    }
    this.studyDbIds.add(studyDbIdsItem);
    return this;
  }

  /**
   * List of study identifiers to search for
   * @return studyDbIds
  **/
  @ApiModelProperty(example = "[\"cf6c4bd4\",\"691e69d6\"]", value = "List of study identifiers to search for")
  
    public List<String> getStudyDbIds() {
    return studyDbIds;
  }

  public void setStudyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
  }

  public StudySearchRequest studyNames(List<String> studyNames) {
    this.studyNames = studyNames;
    return this;
  }

  public StudySearchRequest addStudyNamesItem(String studyNamesItem) {
    if (this.studyNames == null) {
      this.studyNames = new ArrayList<String>();
    }
    this.studyNames.add(studyNamesItem);
    return this;
  }

  /**
   * List of study names to filter search results
   * @return studyNames
  **/
  @ApiModelProperty(example = "[\"The First Bob Study 2017\",\"Wheat Yield Trial 246\"]", value = "List of study names to filter search results")
  
    public List<String> getStudyNames() {
    return studyNames;
  }

  public void setStudyNames(List<String> studyNames) {
    this.studyNames = studyNames;
  }

  public StudySearchRequest studyTypes(List<String> studyTypes) {
    this.studyTypes = studyTypes;
    return this;
  }

  public StudySearchRequest addStudyTypesItem(String studyTypesItem) {
    if (this.studyTypes == null) {
      this.studyTypes = new ArrayList<String>();
    }
    this.studyTypes.add(studyTypesItem);
    return this;
  }

  /**
   * The type of study being performed. ex. \"Yield Trial\", etc
   * @return studyTypes
  **/
  @ApiModelProperty(example = "[\"Yield Trial\",\"Disease Resistance Trial\"]", value = "The type of study being performed. ex. \"Yield Trial\", etc")
  
    public List<String> getStudyTypes() {
    return studyTypes;
  }

  public void setStudyTypes(List<String> studyTypes) {
    this.studyTypes = studyTypes;
  }

  public StudySearchRequest trialDbIds(List<String> trialDbIds) {
    this.trialDbIds = trialDbIds;
    return this;
  }

  public StudySearchRequest addTrialDbIdsItem(String trialDbIdsItem) {
    if (this.trialDbIds == null) {
      this.trialDbIds = new ArrayList<String>();
    }
    this.trialDbIds.add(trialDbIdsItem);
    return this;
  }

  /**
   * List of trial identifiers to filter search results
   * @return trialDbIds
  **/
  @ApiModelProperty(example = "[\"29f375a1\",\"753d882b\"]", value = "List of trial identifiers to filter search results")
  
    public List<String> getTrialDbIds() {
    return trialDbIds;
  }

  public void setTrialDbIds(List<String> trialDbIds) {
    this.trialDbIds = trialDbIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StudySearchRequest studySearchRequest = (StudySearchRequest) o;
    return Objects.equals(this.active, studySearchRequest.active) &&
        Objects.equals(this.commonCropNames, studySearchRequest.commonCropNames) &&
        Objects.equals(this.germplasmDbIds, studySearchRequest.germplasmDbIds) &&
        Objects.equals(this.locationDbIds, studySearchRequest.locationDbIds) &&
        Objects.equals(this.observationVariableDbIds, studySearchRequest.observationVariableDbIds) &&
        Objects.equals(this.programDbIds, studySearchRequest.programDbIds) &&
        Objects.equals(this.programNames, studySearchRequest.programNames) &&
        Objects.equals(this.seasonDbIds, studySearchRequest.seasonDbIds) &&
        Objects.equals(this.sortBy, studySearchRequest.sortBy) &&
        Objects.equals(this.sortOrder, studySearchRequest.sortOrder) &&
        Objects.equals(this.studyDbIds, studySearchRequest.studyDbIds) &&
        Objects.equals(this.studyNames, studySearchRequest.studyNames) &&
        Objects.equals(this.studyTypes, studySearchRequest.studyTypes) &&
        Objects.equals(this.trialDbIds, studySearchRequest.trialDbIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(active, commonCropNames, germplasmDbIds, locationDbIds, observationVariableDbIds, programDbIds, programNames, seasonDbIds, sortBy, sortOrder, studyDbIds, studyNames, studyTypes, trialDbIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StudySearchRequest {\n");
    
    sb.append("    active: ").append(toIndentedString(active)).append("\n");
    sb.append("    commonCropNames: ").append(toIndentedString(commonCropNames)).append("\n");
    sb.append("    germplasmDbIds: ").append(toIndentedString(germplasmDbIds)).append("\n");
    sb.append("    locationDbIds: ").append(toIndentedString(locationDbIds)).append("\n");
    sb.append("    observationVariableDbIds: ").append(toIndentedString(observationVariableDbIds)).append("\n");
    sb.append("    programDbIds: ").append(toIndentedString(programDbIds)).append("\n");
    sb.append("    programNames: ").append(toIndentedString(programNames)).append("\n");
    sb.append("    seasonDbIds: ").append(toIndentedString(seasonDbIds)).append("\n");
    sb.append("    sortBy: ").append(toIndentedString(sortBy)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    studyDbIds: ").append(toIndentedString(studyDbIds)).append("\n");
    sb.append("    studyNames: ").append(toIndentedString(studyNames)).append("\n");
    sb.append("    studyTypes: ").append(toIndentedString(studyTypes)).append("\n");
    sb.append("    trialDbIds: ").append(toIndentedString(trialDbIds)).append("\n");
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
