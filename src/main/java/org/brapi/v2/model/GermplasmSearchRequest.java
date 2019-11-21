package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * GermplasmSearchRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-20T14:32:35.470Z[GMT]")
public class GermplasmSearchRequest   {
  @JsonProperty("accessionNumbers")
  @Valid
  private List<String> accessionNumbers = null;

  @JsonProperty("commonCropNames")
  @Valid
  private List<String> commonCropNames = null;

  @JsonProperty("germplasmDbIds")
  @Valid
  private List<String> germplasmDbIds = null;

  @JsonProperty("germplasmGenus")
  @Valid
  private List<String> germplasmGenus = null;

  @JsonProperty("germplasmNames")
  @Valid
  private List<String> germplasmNames = null;

  @JsonProperty("germplasmPUIs")
  @Valid
  private List<String> germplasmPUIs = null;

  @JsonProperty("germplasmSpecies")
  @Valid
  private List<String> germplasmSpecies = null;

  @JsonProperty("parentDbIds")
  @Valid
  private List<String> parentDbIds = null;

  @JsonProperty("progenyDbIds")
  @Valid
  private List<String> progenyDbIds = null;

  @JsonProperty("studyDbIds")
  @Valid
  private List<String> studyDbIds = null;

  @JsonProperty("synonyms")
  @Valid
  private List<String> synonyms = null;

  @JsonProperty("xrefs")
  @Valid
  private List<String> xrefs = null;
  
  @JsonProperty("pageSize")
  private Integer pageSize = null;

  @JsonProperty("page")
  private Integer page = null;

  public GermplasmSearchRequest accessionNumbers(List<String> accessionNumbers) {
    this.accessionNumbers = accessionNumbers;
    return this;
  }

  public GermplasmSearchRequest addAccessionNumbersItem(String accessionNumbersItem) {
    if (this.accessionNumbers == null) {
      this.accessionNumbers = new ArrayList<String>();
    }
    this.accessionNumbers.add(accessionNumbersItem);
    return this;
  }

  /**
   * List unique identifiers for accessions within a genebank
   * @return accessionNumbers
  **/
  @ApiModelProperty(example = "[\"A0000003\",\"A0000477\"]", value = "List unique identifiers for accessions within a genebank")
  
    public List<String> getAccessionNumbers() {
    return accessionNumbers;
  }

  public void setAccessionNumbers(List<String> accessionNumbers) {
    this.accessionNumbers = accessionNumbers;
  }

  public GermplasmSearchRequest commonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
    return this;
  }

  public GermplasmSearchRequest addCommonCropNamesItem(String commonCropNamesItem) {
    if (this.commonCropNames == null) {
      this.commonCropNames = new ArrayList<String>();
    }
    this.commonCropNames.add(commonCropNamesItem);
    return this;
  }

  /**
   * List crops to search by
   * @return commonCropNames
  **/
  @ApiModelProperty(example = "[\"Tomatillo\",\"Paw Paw\"]", value = "List crops to search by")
  
    public List<String> getCommonCropNames() {
    return commonCropNames;
  }

  public void setCommonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
  }

  public GermplasmSearchRequest germplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
    return this;
  }

  public GermplasmSearchRequest addGermplasmDbIdsItem(String germplasmDbIdsItem) {
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
  @ApiModelProperty(example = "[\"e9c6edd7\",\"1b1df4a6\"]", value = "List of IDs which uniquely identify germplasm")
  
    public List<String> getGermplasmDbIds() {
    return germplasmDbIds;
  }

  public void setGermplasmDbIds(List<String> germplasmDbIds) {
    this.germplasmDbIds = germplasmDbIds;
  }

  public GermplasmSearchRequest germplasmGenus(List<String> germplasmGenus) {
    this.germplasmGenus = germplasmGenus;
    return this;
  }

  public GermplasmSearchRequest addGermplasmGenusItem(String germplasmGenusItem) {
    if (this.germplasmGenus == null) {
      this.germplasmGenus = new ArrayList<String>();
    }
    this.germplasmGenus.add(germplasmGenusItem);
    return this;
  }

  /**
   * List of Genus names to identify germplasm
   * @return germplasmGenus
  **/
  @ApiModelProperty(example = "[\"Aspergillus\",\"Byssochlamys\"]", value = "List of Genus names to identify germplasm")
  
    public List<String> getGermplasmGenus() {
    return germplasmGenus;
  }

  public void setGermplasmGenus(List<String> germplasmGenus) {
    this.germplasmGenus = germplasmGenus;
  }

  public GermplasmSearchRequest germplasmNames(List<String> germplasmNames) {
    this.germplasmNames = germplasmNames;
    return this;
  }

  public GermplasmSearchRequest addGermplasmNamesItem(String germplasmNamesItem) {
    if (this.germplasmNames == null) {
      this.germplasmNames = new ArrayList<String>();
    }
    this.germplasmNames.add(germplasmNamesItem);
    return this;
  }

  /**
   * List of human readable names to identify germplasm
   * @return germplasmNames
  **/
  @ApiModelProperty(example = "[\"A0000003\",\"A0000477\"]", value = "List of human readable names to identify germplasm")
  
    public List<String> getGermplasmNames() {
    return germplasmNames;
  }

  public void setGermplasmNames(List<String> germplasmNames) {
    this.germplasmNames = germplasmNames;
  }

  public GermplasmSearchRequest germplasmPUIs(List<String> germplasmPUIs) {
    this.germplasmPUIs = germplasmPUIs;
    return this;
  }

  public GermplasmSearchRequest addGermplasmPUIsItem(String germplasmPUIsItem) {
    if (this.germplasmPUIs == null) {
      this.germplasmPUIs = new ArrayList<String>();
    }
    this.germplasmPUIs.add(germplasmPUIsItem);
    return this;
  }

  /**
   * List of Permanent Unique Identifiers to identify germplasm
   * @return germplasmPUIs
  **/
  @ApiModelProperty(example = "[\"http://pui.per/accession/A0000003\",\"http://pui.per/accession/A0000477\"]", value = "List of Permanent Unique Identifiers to identify germplasm")
  
    public List<String> getGermplasmPUIs() {
    return germplasmPUIs;
  }

  public void setGermplasmPUIs(List<String> germplasmPUIs) {
    this.germplasmPUIs = germplasmPUIs;
  }

  public GermplasmSearchRequest germplasmSpecies(List<String> germplasmSpecies) {
    this.germplasmSpecies = germplasmSpecies;
    return this;
  }

  public GermplasmSearchRequest addGermplasmSpeciesItem(String germplasmSpeciesItem) {
    if (this.germplasmSpecies == null) {
      this.germplasmSpecies = new ArrayList<String>();
    }
    this.germplasmSpecies.add(germplasmSpeciesItem);
    return this;
  }

  /**
   * List of Species names to identify germplasm
   * @return germplasmSpecies
  **/
  @ApiModelProperty(example = "[\"fructus\",\"fulva\"]", value = "List of Species names to identify germplasm")
  
    public List<String> getGermplasmSpecies() {
    return germplasmSpecies;
  }

  public void setGermplasmSpecies(List<String> germplasmSpecies) {
    this.germplasmSpecies = germplasmSpecies;
  }

  public GermplasmSearchRequest parentDbIds(List<String> parentDbIds) {
    this.parentDbIds = parentDbIds;
    return this;
  }

  public GermplasmSearchRequest addParentDbIdsItem(String parentDbIdsItem) {
    if (this.parentDbIds == null) {
      this.parentDbIds = new ArrayList<String>();
    }
    this.parentDbIds.add(parentDbIdsItem);
    return this;
  }

  /**
   * Search for Germplasm with these parents
   * @return parentDbIds
  **/
  @ApiModelProperty(example = "[\"72c1001f\",\"7346c553\"]", value = "Search for Germplasm with these parents")
  
    public List<String> getParentDbIds() {
    return parentDbIds;
  }

  public void setParentDbIds(List<String> parentDbIds) {
    this.parentDbIds = parentDbIds;
  }

  public GermplasmSearchRequest progenyDbIds(List<String> progenyDbIds) {
    this.progenyDbIds = progenyDbIds;
    return this;
  }

  public GermplasmSearchRequest addProgenyDbIdsItem(String progenyDbIdsItem) {
    if (this.progenyDbIds == null) {
      this.progenyDbIds = new ArrayList<String>();
    }
    this.progenyDbIds.add(progenyDbIdsItem);
    return this;
  }

  /**
   * Search for Germplasm with these children
   * @return progenyDbIds
  **/
  @ApiModelProperty(example = "[\"16e16a7e\",\"ce06cf9e\"]", value = "Search for Germplasm with these children")
  
    public List<String> getProgenyDbIds() {
    return progenyDbIds;
  }

  public void setProgenyDbIds(List<String> progenyDbIds) {
    this.progenyDbIds = progenyDbIds;
  }

  public GermplasmSearchRequest studyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
    return this;
  }

  public GermplasmSearchRequest addStudyDbIdsItem(String studyDbIdsItem) {
    if (this.studyDbIds == null) {
      this.studyDbIds = new ArrayList<String>();
    }
    this.studyDbIds.add(studyDbIdsItem);
    return this;
  }

  /**
   * Search for Germplasm that are associated with a particular Study
   * @return studyDbIds
  **/
  @ApiModelProperty(example = "[\"4a5f9742\",\"07a6229c\"]", value = "Search for Germplasm that are associated with a particular Study")
  
    public List<String> getStudyDbIds() {
    return studyDbIds;
  }

  public void setStudyDbIds(List<String> studyDbIds) {
    this.studyDbIds = studyDbIds;
  }

  public GermplasmSearchRequest synonyms(List<String> synonyms) {
    this.synonyms = synonyms;
    return this;
  }

  public GermplasmSearchRequest addSynonymsItem(String synonymsItem) {
    if (this.synonyms == null) {
      this.synonyms = new ArrayList<String>();
    }
    this.synonyms.add(synonymsItem);
    return this;
  }

  /**
   * List of alternative names or IDs used to reference this germplasm
   * @return synonyms
  **/
  @ApiModelProperty(example = "[\"variety_1\",\"2c38f9b6\"]", value = "List of alternative names or IDs used to reference this germplasm")
  
    public List<String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(List<String> synonyms) {
    this.synonyms = synonyms;
  }

  public GermplasmSearchRequest xrefs(List<String> xrefs) {
    this.xrefs = xrefs;
    return this;
  }

  public GermplasmSearchRequest addXrefsItem(String xrefsItem) {
    if (this.xrefs == null) {
      this.xrefs = new ArrayList<String>();
    }
    this.xrefs.add(xrefsItem);
    return this;
  }

  /**
   * Search for Germplasm by an external reference
   * @return xrefs
  **/
  @ApiModelProperty(example = "[\"http://pui.per/accession/A0008766\",\"4cda72fe\"]", value = "Search for Germplasm by an external reference")
  
    public List<String> getXrefs() {
    return xrefs;
  }

  public void setXrefs(List<String> xrefs) {
    this.xrefs = xrefs;
  }

  public GermplasmSearchRequest pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public GermplasmSearchRequest page(Integer page) {
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
  
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) o;
    return Objects.equals(this.accessionNumbers, germplasmSearchRequest.accessionNumbers) &&
        Objects.equals(this.commonCropNames, germplasmSearchRequest.commonCropNames) &&
        Objects.equals(this.germplasmDbIds, germplasmSearchRequest.germplasmDbIds) &&
        Objects.equals(this.germplasmGenus, germplasmSearchRequest.germplasmGenus) &&
        Objects.equals(this.germplasmNames, germplasmSearchRequest.germplasmNames) &&
        Objects.equals(this.germplasmPUIs, germplasmSearchRequest.germplasmPUIs) &&
        Objects.equals(this.germplasmSpecies, germplasmSearchRequest.germplasmSpecies) &&
        Objects.equals(this.parentDbIds, germplasmSearchRequest.parentDbIds) &&
        Objects.equals(this.progenyDbIds, germplasmSearchRequest.progenyDbIds) &&
        Objects.equals(this.studyDbIds, germplasmSearchRequest.studyDbIds) &&
        Objects.equals(this.synonyms, germplasmSearchRequest.synonyms) &&
        Objects.equals(this.xrefs, germplasmSearchRequest.xrefs) && 
        Objects.equals(this.pageSize, germplasmSearchRequest.pageSize) &&
        Objects.equals(this.page, germplasmSearchRequest.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessionNumbers, commonCropNames, germplasmDbIds, germplasmGenus, germplasmNames, germplasmPUIs, germplasmSpecies, parentDbIds, progenyDbIds, studyDbIds, synonyms, xrefs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmSearchRequest {\n");    
    sb.append("    accessionNumbers: ").append(toIndentedString(accessionNumbers)).append("\n");
    sb.append("    commonCropNames: ").append(toIndentedString(commonCropNames)).append("\n");
    sb.append("    germplasmDbIds: ").append(toIndentedString(germplasmDbIds)).append("\n");
    sb.append("    germplasmGenus: ").append(toIndentedString(germplasmGenus)).append("\n");
    sb.append("    germplasmNames: ").append(toIndentedString(germplasmNames)).append("\n");
    sb.append("    germplasmPUIs: ").append(toIndentedString(germplasmPUIs)).append("\n");
    sb.append("    germplasmSpecies: ").append(toIndentedString(germplasmSpecies)).append("\n");
    sb.append("    parentDbIds: ").append(toIndentedString(parentDbIds)).append("\n");
    sb.append("    progenyDbIds: ").append(toIndentedString(progenyDbIds)).append("\n");
    sb.append("    studyDbIds: ").append(toIndentedString(studyDbIds)).append("\n");
    sb.append("    synonyms: ").append(toIndentedString(synonyms)).append("\n");
    sb.append("    xrefs: ").append(toIndentedString(xrefs)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
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
