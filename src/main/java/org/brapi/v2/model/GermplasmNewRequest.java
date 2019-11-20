package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.brapi.v2.model.GermplasmNewRequestDonors;
import org.brapi.v2.model.GermplasmOrigin;
import org.brapi.v2.model.TaxonID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.threeten.bp.LocalDate;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * GermplasmNewRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-20T14:32:35.470Z[GMT]")
public class GermplasmNewRequest   {
  @JsonProperty("accessionNumber")
  private String accessionNumber = null;

  @JsonProperty("acquisitionDate")
  private LocalDate acquisitionDate = null;

  @JsonProperty("additionalInfo")
  @Valid
  private Map<String, String> additionalInfo = null;

  /**
   * MCPD (v2.1) (SAMPSTAT) 19. The coding scheme proposed can be used at 3 different levels of detail: either by using the general codes (in boldface) such as 100, 200, 300, 400, or by using the more specific codes such as 110, 120, etc. 100) Wild 110) Natural 120) Semi-natural/wild 130) Semi-natural/sown 200) Weedy 300) Traditional cultivar/landrace 400) Breeding/research material 410) Breeders line 411) Synthetic population 412) Hybrid 413) Founder stock/base population 414) Inbred line (parent of hybrid cultivar) 415) Segregating population 416) Clonal selection 420) Genetic stock 421) Mutant (e.g. induced/insertion mutants, tilling populations) 422) Cytogenetic stocks (e.g. chromosome addition/substitution, aneuploids,  amphiploids) 423) Other genetic stocks (e.g. mapping populations) 500) Advanced or improved cultivar (conventional breeding methods) 600) GMO (by genetic engineering) 999) Other (Elaborate in REMARKS field)
   */
  public enum BiologicalStatusOfAccessionCodeEnum {
    _100("100"),
    
    _110("110"),
    
    _120("120"),
    
    _130("130"),
    
    _200("200"),
    
    _300("300"),
    
    _400("400"),
    
    _410("410"),
    
    _411("411"),
    
    _412("412"),
    
    _413("413"),
    
    _414("414"),
    
    _415("415"),
    
    _416("416"),
    
    _420("420"),
    
    _421("421"),
    
    _422("422"),
    
    _423("423"),
    
    _500("500"),
    
    _600("600"),
    
    _999("999");

    private String value;

    BiologicalStatusOfAccessionCodeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static BiologicalStatusOfAccessionCodeEnum fromValue(String text) {
      for (BiologicalStatusOfAccessionCodeEnum b : BiologicalStatusOfAccessionCodeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("biologicalStatusOfAccessionCode")
  private BiologicalStatusOfAccessionCodeEnum biologicalStatusOfAccessionCode = null;

  @JsonProperty("breedingMethodDbId")
  private String breedingMethodDbId = null;

  @JsonProperty("commonCropName")
  private String commonCropName = null;

  @JsonProperty("countryOfOriginCode")
  private String countryOfOriginCode = null;

  @JsonProperty("defaultDisplayName")
  private String defaultDisplayName = null;

  @JsonProperty("documentationURL")
  private String documentationURL = null;

  @JsonProperty("donors")
  @Valid
  private List<GermplasmNewRequestDonors> donors = null;

  @JsonProperty("germplasmGenus")
  private String germplasmGenus = null;

  @JsonProperty("germplasmName")
  private String germplasmName = null;

  @JsonProperty("germplasmOrigin")
  @Valid
  private List<GermplasmOrigin> germplasmOrigin = null;

  @JsonProperty("germplasmPUI")
  private String germplasmPUI = null;

  @JsonProperty("germplasmPreprocessing")
  private String germplasmPreprocessing = null;

  @JsonProperty("germplasmSpecies")
  private String germplasmSpecies = null;

  @JsonProperty("instituteCode")
  private String instituteCode = null;

  @JsonProperty("instituteName")
  private String instituteName = null;

  @JsonProperty("pedigree")
  private String pedigree = null;

  @JsonProperty("seedSource")
  private String seedSource = null;

  @JsonProperty("seedSourceDescription")
  private String seedSourceDescription = null;

  @JsonProperty("speciesAuthority")
  private String speciesAuthority = null;

  @JsonProperty("subtaxa")
  private String subtaxa = null;

  @JsonProperty("subtaxaAuthority")
  private String subtaxaAuthority = null;

  @JsonProperty("synonyms")
  @Valid
  private List<String> synonyms = null;

  @JsonProperty("taxonIds")
  @Valid
  private List<TaxonID> taxonIds = null;

  @JsonProperty("typeOfGermplasmStorageCode")
  @Valid
  private List<String> typeOfGermplasmStorageCode = null;

  @JsonProperty("xref")
  private String xref = null;

  public GermplasmNewRequest accessionNumber(String accessionNumber) {
    this.accessionNumber = accessionNumber;
    return this;
  }

  /**
   * This is the unique identifier for accessions within a genebank, and is assigned when a sample is entered into the genebank collection
   * @return accessionNumber
  **/
  @ApiModelProperty(example = "A0000003", value = "This is the unique identifier for accessions within a genebank, and is assigned when a sample is entered into the genebank collection")
  
    public String getAccessionNumber() {
    return accessionNumber;
  }

  public void setAccessionNumber(String accessionNumber) {
    this.accessionNumber = accessionNumber;
  }

  public GermplasmNewRequest acquisitionDate(LocalDate acquisitionDate) {
    this.acquisitionDate = acquisitionDate;
    return this;
  }

  /**
   * The date this germplasm was acquired by the genebank (MCPD)
   * @return acquisitionDate
  **/
  @ApiModelProperty(value = "The date this germplasm was acquired by the genebank (MCPD)")
  
    @Valid
    public LocalDate getAcquisitionDate() {
    return acquisitionDate;
  }

  public void setAcquisitionDate(LocalDate acquisitionDate) {
    this.acquisitionDate = acquisitionDate;
  }

  public GermplasmNewRequest additionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }

  public GermplasmNewRequest putAdditionalInfoItem(String key, String additionalInfoItem) {
    if (this.additionalInfo == null) {
      this.additionalInfo = new HashMap<String, String>();
    }
    this.additionalInfo.put(key, additionalInfoItem);
    return this;
  }

  /**
   * Additional arbitrary info
   * @return additionalInfo
  **/
  @ApiModelProperty(value = "Additional arbitrary info")
  
    public Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public GermplasmNewRequest biologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum biologicalStatusOfAccessionCode) {
    this.biologicalStatusOfAccessionCode = biologicalStatusOfAccessionCode;
    return this;
  }

  /**
   * MCPD (v2.1) (SAMPSTAT) 19. The coding scheme proposed can be used at 3 different levels of detail: either by using the general codes (in boldface) such as 100, 200, 300, 400, or by using the more specific codes such as 110, 120, etc. 100) Wild 110) Natural 120) Semi-natural/wild 130) Semi-natural/sown 200) Weedy 300) Traditional cultivar/landrace 400) Breeding/research material 410) Breeders line 411) Synthetic population 412) Hybrid 413) Founder stock/base population 414) Inbred line (parent of hybrid cultivar) 415) Segregating population 416) Clonal selection 420) Genetic stock 421) Mutant (e.g. induced/insertion mutants, tilling populations) 422) Cytogenetic stocks (e.g. chromosome addition/substitution, aneuploids,  amphiploids) 423) Other genetic stocks (e.g. mapping populations) 500) Advanced or improved cultivar (conventional breeding methods) 600) GMO (by genetic engineering) 999) Other (Elaborate in REMARKS field)
   * @return biologicalStatusOfAccessionCode
  **/
  @ApiModelProperty(example = "421", value = "MCPD (v2.1) (SAMPSTAT) 19. The coding scheme proposed can be used at 3 different levels of detail: either by using the general codes (in boldface) such as 100, 200, 300, 400, or by using the more specific codes such as 110, 120, etc. 100) Wild 110) Natural 120) Semi-natural/wild 130) Semi-natural/sown 200) Weedy 300) Traditional cultivar/landrace 400) Breeding/research material 410) Breeders line 411) Synthetic population 412) Hybrid 413) Founder stock/base population 414) Inbred line (parent of hybrid cultivar) 415) Segregating population 416) Clonal selection 420) Genetic stock 421) Mutant (e.g. induced/insertion mutants, tilling populations) 422) Cytogenetic stocks (e.g. chromosome addition/substitution, aneuploids,  amphiploids) 423) Other genetic stocks (e.g. mapping populations) 500) Advanced or improved cultivar (conventional breeding methods) 600) GMO (by genetic engineering) 999) Other (Elaborate in REMARKS field)")
  
    public BiologicalStatusOfAccessionCodeEnum getBiologicalStatusOfAccessionCode() {
    return biologicalStatusOfAccessionCode;
  }

  public void setBiologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum biologicalStatusOfAccessionCode) {
    this.biologicalStatusOfAccessionCode = biologicalStatusOfAccessionCode;
  }

  public GermplasmNewRequest breedingMethodDbId(String breedingMethodDbId) {
    this.breedingMethodDbId = breedingMethodDbId;
    return this;
  }

  /**
   * The unique identifier for the breeding method used to create this germplasm
   * @return breedingMethodDbId
  **/
  @ApiModelProperty(example = "ffcce7ef", value = "The unique identifier for the breeding method used to create this germplasm")
  
    public String getBreedingMethodDbId() {
    return breedingMethodDbId;
  }

  public void setBreedingMethodDbId(String breedingMethodDbId) {
    this.breedingMethodDbId = breedingMethodDbId;
  }

  public GermplasmNewRequest commonCropName(String commonCropName) {
    this.commonCropName = commonCropName;
    return this;
  }

  /**
   * Common name for the crop (MCPD)
   * @return commonCropName
  **/
  @ApiModelProperty(example = "Maize", required = true, value = "Common name for the crop (MCPD)")
      @NotNull

    public String getCommonCropName() {
    return commonCropName;
  }

  public void setCommonCropName(String commonCropName) {
    this.commonCropName = commonCropName;
  }

  public GermplasmNewRequest countryOfOriginCode(String countryOfOriginCode) {
    this.countryOfOriginCode = countryOfOriginCode;
    return this;
  }

  /**
   * 3-letter ISO 3166-1 code of the country in which the sample was originally collected (MCPD)
   * @return countryOfOriginCode
  **/
  @ApiModelProperty(example = "BES", value = "3-letter ISO 3166-1 code of the country in which the sample was originally collected (MCPD)")
  
    public String getCountryOfOriginCode() {
    return countryOfOriginCode;
  }

  public void setCountryOfOriginCode(String countryOfOriginCode) {
    this.countryOfOriginCode = countryOfOriginCode;
  }

  public GermplasmNewRequest defaultDisplayName(String defaultDisplayName) {
    this.defaultDisplayName = defaultDisplayName;
    return this;
  }

  /**
   * Human readable name used for display purposes
   * @return defaultDisplayName
  **/
  @ApiModelProperty(example = "A0000003", value = "Human readable name used for display purposes")
  
    public String getDefaultDisplayName() {
    return defaultDisplayName;
  }

  public void setDefaultDisplayName(String defaultDisplayName) {
    this.defaultDisplayName = defaultDisplayName;
  }

  public GermplasmNewRequest documentationURL(String documentationURL) {
    this.documentationURL = documentationURL;
    return this;
  }

  /**
   * A URL to the human readable documentation of this object
   * @return documentationURL
  **/
  @ApiModelProperty(example = "https://wiki.brapi.org", value = "A URL to the human readable documentation of this object")
  
    public String getDocumentationURL() {
    return documentationURL;
  }

  public void setDocumentationURL(String documentationURL) {
    this.documentationURL = documentationURL;
  }

  public GermplasmNewRequest donors(List<GermplasmNewRequestDonors> donors) {
    this.donors = donors;
    return this;
  }

  public GermplasmNewRequest addDonorsItem(GermplasmNewRequestDonors donorsItem) {
    if (this.donors == null) {
      this.donors = new ArrayList<GermplasmNewRequestDonors>();
    }
    this.donors.add(donorsItem);
    return this;
  }

  /**
   * List of donor institutes (MCPD)
   * @return donors
  **/
  @ApiModelProperty(value = "List of donor institutes (MCPD)")
      @Valid
    public List<GermplasmNewRequestDonors> getDonors() {
    return donors;
  }

  public void setDonors(List<GermplasmNewRequestDonors> donors) {
    this.donors = donors;
  }

  public GermplasmNewRequest germplasmGenus(String germplasmGenus) {
    this.germplasmGenus = germplasmGenus;
    return this;
  }

  /**
   * Genus name for taxon. Initial uppercase letter required. (MCPD)
   * @return germplasmGenus
  **/
  @ApiModelProperty(example = "Aspergillus", value = "Genus name for taxon. Initial uppercase letter required. (MCPD)")
  
    public String getGermplasmGenus() {
    return germplasmGenus;
  }

  public void setGermplasmGenus(String germplasmGenus) {
    this.germplasmGenus = germplasmGenus;
  }

  public GermplasmNewRequest germplasmName(String germplasmName) {
    this.germplasmName = germplasmName;
    return this;
  }

  /**
   * Name of the germplasm. It can be the preferred name and does not have to be unique.
   * @return germplasmName
  **/
  @ApiModelProperty(example = "A0000003", required = true, value = "Name of the germplasm. It can be the preferred name and does not have to be unique.")
      @NotNull

    public String getGermplasmName() {
    return germplasmName;
  }

  public void setGermplasmName(String germplasmName) {
    this.germplasmName = germplasmName;
  }

  public GermplasmNewRequest germplasmOrigin(List<GermplasmOrigin> germplasmOrigin) {
    this.germplasmOrigin = germplasmOrigin;
    return this;
  }

  public GermplasmNewRequest addGermplasmOriginItem(GermplasmOrigin germplasmOriginItem) {
    if (this.germplasmOrigin == null) {
      this.germplasmOrigin = new ArrayList<GermplasmOrigin>();
    }
    this.germplasmOrigin.add(germplasmOriginItem);
    return this;
  }

  /**
   * Information for material (orchard, natural sites, ...). Geographic identification of the plants from which seeds or cutting have been taken to produce that germplasm.
   * @return germplasmOrigin
  **/
  @ApiModelProperty(value = "Information for material (orchard, natural sites, ...). Geographic identification of the plants from which seeds or cutting have been taken to produce that germplasm.")
      @Valid
    public List<GermplasmOrigin> getGermplasmOrigin() {
    return germplasmOrigin;
  }

  public void setGermplasmOrigin(List<GermplasmOrigin> germplasmOrigin) {
    this.germplasmOrigin = germplasmOrigin;
  }

  public GermplasmNewRequest germplasmPUI(String germplasmPUI) {
    this.germplasmPUI = germplasmPUI;
    return this;
  }

  /**
   * The Permanent Unique Identifier which represents a germplasm
   * @return germplasmPUI
  **/
  @ApiModelProperty(example = "http://pui.per/accession/A0000003", required = true, value = "The Permanent Unique Identifier which represents a germplasm")
      @NotNull

    public String getGermplasmPUI() {
    return germplasmPUI;
  }

  public void setGermplasmPUI(String germplasmPUI) {
    this.germplasmPUI = germplasmPUI;
  }

  public GermplasmNewRequest germplasmPreprocessing(String germplasmPreprocessing) {
    this.germplasmPreprocessing = germplasmPreprocessing;
    return this;
  }

  /**
   * Description of any process or treatment applied uniformly to the germplasm, prior to the study itself. Can be provided as free text or as an accession number from a suitable controlled vocabulary.
   * @return germplasmPreprocessing
  **/
  @ApiModelProperty(example = "EO:0007210; transplanted from study 2351 observation unit ID: pot:894", value = "Description of any process or treatment applied uniformly to the germplasm, prior to the study itself. Can be provided as free text or as an accession number from a suitable controlled vocabulary.")
  
    public String getGermplasmPreprocessing() {
    return germplasmPreprocessing;
  }

  public void setGermplasmPreprocessing(String germplasmPreprocessing) {
    this.germplasmPreprocessing = germplasmPreprocessing;
  }

  public GermplasmNewRequest germplasmSpecies(String germplasmSpecies) {
    this.germplasmSpecies = germplasmSpecies;
    return this;
  }

  /**
   * Specific epithet portion of the scientific name in lowercase letters. (MCPD)
   * @return germplasmSpecies
  **/
  @ApiModelProperty(example = "fructus", value = "Specific epithet portion of the scientific name in lowercase letters. (MCPD)")
  
    public String getGermplasmSpecies() {
    return germplasmSpecies;
  }

  public void setGermplasmSpecies(String germplasmSpecies) {
    this.germplasmSpecies = germplasmSpecies;
  }

  public GermplasmNewRequest instituteCode(String instituteCode) {
    this.instituteCode = instituteCode;
    return this;
  }

  /**
   * The code for the Institute that has bred the material. (MCPD)
   * @return instituteCode
  **/
  @ApiModelProperty(example = "PER001", value = "The code for the Institute that has bred the material. (MCPD)")
  
    public String getInstituteCode() {
    return instituteCode;
  }

  public void setInstituteCode(String instituteCode) {
    this.instituteCode = instituteCode;
  }

  public GermplasmNewRequest instituteName(String instituteName) {
    this.instituteName = instituteName;
    return this;
  }

  /**
   * The name of the institution which bred the material (MCPD)
   * @return instituteName
  **/
  @ApiModelProperty(example = "The BrAPI Institute", value = "The name of the institution which bred the material (MCPD)")
  
    public String getInstituteName() {
    return instituteName;
  }

  public void setInstituteName(String instituteName) {
    this.instituteName = instituteName;
  }

  public GermplasmNewRequest pedigree(String pedigree) {
    this.pedigree = pedigree;
    return this;
  }

  /**
   * The cross name and optional selection history.
   * @return pedigree
  **/
  @ApiModelProperty(example = "A0000001/A0000002", value = "The cross name and optional selection history.")
  
    public String getPedigree() {
    return pedigree;
  }

  public void setPedigree(String pedigree) {
    this.pedigree = pedigree;
  }

  public GermplasmNewRequest seedSource(String seedSource) {
    this.seedSource = seedSource;
    return this;
  }

  /**
   * The source of the seed 
   * @return seedSource
  **/
  @ApiModelProperty(example = "A0000001/A0000002", value = "The source of the seed ")
  
    public String getSeedSource() {
    return seedSource;
  }

  public void setSeedSource(String seedSource) {
    this.seedSource = seedSource;
  }

  public GermplasmNewRequest seedSourceDescription(String seedSourceDescription) {
    this.seedSourceDescription = seedSourceDescription;
    return this;
  }

  /**
   * Description of the material source
   * @return seedSourceDescription
  **/
  @ApiModelProperty(example = "Branches were collected from a 10-year-old tree growing in a progeny trial established in a loamy brown earth soil.", value = "Description of the material source")
  
    public String getSeedSourceDescription() {
    return seedSourceDescription;
  }

  public void setSeedSourceDescription(String seedSourceDescription) {
    this.seedSourceDescription = seedSourceDescription;
  }

  public GermplasmNewRequest speciesAuthority(String speciesAuthority) {
    this.speciesAuthority = speciesAuthority;
    return this;
  }

  /**
   * The authority organization responsible for tracking and maintaining the species name (MCPD)
   * @return speciesAuthority
  **/
  @ApiModelProperty(example = "Smith, 1822", value = "The authority organization responsible for tracking and maintaining the species name (MCPD)")
  
    public String getSpeciesAuthority() {
    return speciesAuthority;
  }

  public void setSpeciesAuthority(String speciesAuthority) {
    this.speciesAuthority = speciesAuthority;
  }

  public GermplasmNewRequest subtaxa(String subtaxa) {
    this.subtaxa = subtaxa;
    return this;
  }

  /**
   * Subtaxon can be used to store any additional taxonomic identifier. (MCPD)
   * @return subtaxa
  **/
  @ApiModelProperty(example = "Aspergillus fructus A", value = "Subtaxon can be used to store any additional taxonomic identifier. (MCPD)")
  
    public String getSubtaxa() {
    return subtaxa;
  }

  public void setSubtaxa(String subtaxa) {
    this.subtaxa = subtaxa;
  }

  public GermplasmNewRequest subtaxaAuthority(String subtaxaAuthority) {
    this.subtaxaAuthority = subtaxaAuthority;
    return this;
  }

  /**
   *  The authority organization responsible for tracking and maintaining the subtaxon information (MCPD)
   * @return subtaxaAuthority
  **/
  @ApiModelProperty(example = "Smith, 1822", value = " The authority organization responsible for tracking and maintaining the subtaxon information (MCPD)")
  
    public String getSubtaxaAuthority() {
    return subtaxaAuthority;
  }

  public void setSubtaxaAuthority(String subtaxaAuthority) {
    this.subtaxaAuthority = subtaxaAuthority;
  }

  public GermplasmNewRequest synonyms(List<String> synonyms) {
    this.synonyms = synonyms;
    return this;
  }

  public GermplasmNewRequest addSynonymsItem(String synonymsItem) {
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
  @ApiModelProperty(example = "[\"variety_1\"]", value = "List of alternative names or IDs used to reference this germplasm")
  
    public List<String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(List<String> synonyms) {
    this.synonyms = synonyms;
  }

  public GermplasmNewRequest taxonIds(List<TaxonID> taxonIds) {
    this.taxonIds = taxonIds;
    return this;
  }

  public GermplasmNewRequest addTaxonIdsItem(TaxonID taxonIdsItem) {
    if (this.taxonIds == null) {
      this.taxonIds = new ArrayList<TaxonID>();
    }
    this.taxonIds.add(taxonIdsItem);
    return this;
  }

  /**
   * The list of IDs for this SPECIES from different sources. If present, NCBI Taxon should be always listed as \"ncbiTaxon\" preferably with a purl. The rank of this ID should be species.
   * @return taxonIds
  **/
  @ApiModelProperty(value = "The list of IDs for this SPECIES from different sources. If present, NCBI Taxon should be always listed as \"ncbiTaxon\" preferably with a purl. The rank of this ID should be species.")
      @Valid
    public List<TaxonID> getTaxonIds() {
    return taxonIds;
  }

  public void setTaxonIds(List<TaxonID> taxonIds) {
    this.taxonIds = taxonIds;
  }

  public GermplasmNewRequest typeOfGermplasmStorageCode(List<String> typeOfGermplasmStorageCode) {
    this.typeOfGermplasmStorageCode = typeOfGermplasmStorageCode;
    return this;
  }

  public GermplasmNewRequest addTypeOfGermplasmStorageCodeItem(String typeOfGermplasmStorageCodeItem) {
    if (this.typeOfGermplasmStorageCode == null) {
      this.typeOfGermplasmStorageCode = new ArrayList<String>();
    }
    this.typeOfGermplasmStorageCode.add(typeOfGermplasmStorageCodeItem);
    return this;
  }

  /**
   * The 2 digit code representing the type of storage this germplasm is kept in at a genebank. (MCPD)
   * @return typeOfGermplasmStorageCode
  **/
  @ApiModelProperty(example = "[\"10\",\"11\"]", value = "The 2 digit code representing the type of storage this germplasm is kept in at a genebank. (MCPD)")
  
    public List<String> getTypeOfGermplasmStorageCode() {
    return typeOfGermplasmStorageCode;
  }

  public void setTypeOfGermplasmStorageCode(List<String> typeOfGermplasmStorageCode) {
    this.typeOfGermplasmStorageCode = typeOfGermplasmStorageCode;
  }

  public GermplasmNewRequest xref(String xref) {
    this.xref = xref;
    return this;
  }

  /**
   * External reference to another system
   * @return xref
  **/
  @ApiModelProperty(example = "http://pui.per/accession/A0000079", value = "External reference to another system")
  
    public String getXref() {
    return xref;
  }

  public void setXref(String xref) {
    this.xref = xref;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GermplasmNewRequest germplasmNewRequest = (GermplasmNewRequest) o;
    return Objects.equals(this.accessionNumber, germplasmNewRequest.accessionNumber) &&
        Objects.equals(this.acquisitionDate, germplasmNewRequest.acquisitionDate) &&
        Objects.equals(this.additionalInfo, germplasmNewRequest.additionalInfo) &&
        Objects.equals(this.biologicalStatusOfAccessionCode, germplasmNewRequest.biologicalStatusOfAccessionCode) &&
        Objects.equals(this.breedingMethodDbId, germplasmNewRequest.breedingMethodDbId) &&
        Objects.equals(this.commonCropName, germplasmNewRequest.commonCropName) &&
        Objects.equals(this.countryOfOriginCode, germplasmNewRequest.countryOfOriginCode) &&
        Objects.equals(this.defaultDisplayName, germplasmNewRequest.defaultDisplayName) &&
        Objects.equals(this.documentationURL, germplasmNewRequest.documentationURL) &&
        Objects.equals(this.donors, germplasmNewRequest.donors) &&
        Objects.equals(this.germplasmGenus, germplasmNewRequest.germplasmGenus) &&
        Objects.equals(this.germplasmName, germplasmNewRequest.germplasmName) &&
        Objects.equals(this.germplasmOrigin, germplasmNewRequest.germplasmOrigin) &&
        Objects.equals(this.germplasmPUI, germplasmNewRequest.germplasmPUI) &&
        Objects.equals(this.germplasmPreprocessing, germplasmNewRequest.germplasmPreprocessing) &&
        Objects.equals(this.germplasmSpecies, germplasmNewRequest.germplasmSpecies) &&
        Objects.equals(this.instituteCode, germplasmNewRequest.instituteCode) &&
        Objects.equals(this.instituteName, germplasmNewRequest.instituteName) &&
        Objects.equals(this.pedigree, germplasmNewRequest.pedigree) &&
        Objects.equals(this.seedSource, germplasmNewRequest.seedSource) &&
        Objects.equals(this.seedSourceDescription, germplasmNewRequest.seedSourceDescription) &&
        Objects.equals(this.speciesAuthority, germplasmNewRequest.speciesAuthority) &&
        Objects.equals(this.subtaxa, germplasmNewRequest.subtaxa) &&
        Objects.equals(this.subtaxaAuthority, germplasmNewRequest.subtaxaAuthority) &&
        Objects.equals(this.synonyms, germplasmNewRequest.synonyms) &&
        Objects.equals(this.taxonIds, germplasmNewRequest.taxonIds) &&
        Objects.equals(this.typeOfGermplasmStorageCode, germplasmNewRequest.typeOfGermplasmStorageCode) &&
        Objects.equals(this.xref, germplasmNewRequest.xref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessionNumber, acquisitionDate, additionalInfo, biologicalStatusOfAccessionCode, breedingMethodDbId, commonCropName, countryOfOriginCode, defaultDisplayName, documentationURL, donors, germplasmGenus, germplasmName, germplasmOrigin, germplasmPUI, germplasmPreprocessing, germplasmSpecies, instituteCode, instituteName, pedigree, seedSource, seedSourceDescription, speciesAuthority, subtaxa, subtaxaAuthority, synonyms, taxonIds, typeOfGermplasmStorageCode, xref);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GermplasmNewRequest {\n");
    
    sb.append("    accessionNumber: ").append(toIndentedString(accessionNumber)).append("\n");
    sb.append("    acquisitionDate: ").append(toIndentedString(acquisitionDate)).append("\n");
    sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
    sb.append("    biologicalStatusOfAccessionCode: ").append(toIndentedString(biologicalStatusOfAccessionCode)).append("\n");
    sb.append("    breedingMethodDbId: ").append(toIndentedString(breedingMethodDbId)).append("\n");
    sb.append("    commonCropName: ").append(toIndentedString(commonCropName)).append("\n");
    sb.append("    countryOfOriginCode: ").append(toIndentedString(countryOfOriginCode)).append("\n");
    sb.append("    defaultDisplayName: ").append(toIndentedString(defaultDisplayName)).append("\n");
    sb.append("    documentationURL: ").append(toIndentedString(documentationURL)).append("\n");
    sb.append("    donors: ").append(toIndentedString(donors)).append("\n");
    sb.append("    germplasmGenus: ").append(toIndentedString(germplasmGenus)).append("\n");
    sb.append("    germplasmName: ").append(toIndentedString(germplasmName)).append("\n");
    sb.append("    germplasmOrigin: ").append(toIndentedString(germplasmOrigin)).append("\n");
    sb.append("    germplasmPUI: ").append(toIndentedString(germplasmPUI)).append("\n");
    sb.append("    germplasmPreprocessing: ").append(toIndentedString(germplasmPreprocessing)).append("\n");
    sb.append("    germplasmSpecies: ").append(toIndentedString(germplasmSpecies)).append("\n");
    sb.append("    instituteCode: ").append(toIndentedString(instituteCode)).append("\n");
    sb.append("    instituteName: ").append(toIndentedString(instituteName)).append("\n");
    sb.append("    pedigree: ").append(toIndentedString(pedigree)).append("\n");
    sb.append("    seedSource: ").append(toIndentedString(seedSource)).append("\n");
    sb.append("    seedSourceDescription: ").append(toIndentedString(seedSourceDescription)).append("\n");
    sb.append("    speciesAuthority: ").append(toIndentedString(speciesAuthority)).append("\n");
    sb.append("    subtaxa: ").append(toIndentedString(subtaxa)).append("\n");
    sb.append("    subtaxaAuthority: ").append(toIndentedString(subtaxaAuthority)).append("\n");
    sb.append("    synonyms: ").append(toIndentedString(synonyms)).append("\n");
    sb.append("    taxonIds: ").append(toIndentedString(taxonIds)).append("\n");
    sb.append("    typeOfGermplasmStorageCode: ").append(toIndentedString(typeOfGermplasmStorageCode)).append("\n");
    sb.append("    xref: ").append(toIndentedString(xref)).append("\n");
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
