/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for storing the options data required for running the Exomiser.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = SettingsBuilder.class)
public class ExomiserSettings {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserSettings.class);

    //The options (static strings) and variables below are grouped together for 
    //better readability. They are bound using the Json annotations 
    public static final String SETTINGS_FILE_OPTION = "settings-file";

    public static final String BUILD_VERSION = "build-version";
    public static final String BUILD_TIMESTAMP = "build-timestamp";

    private final String buildVersion;
    private final String buildTimestamp;

    //REQUIRED INPUT OPTIONS (these are used for JSON de/serealisation and the command-line)
    public static final String VCF_OPTION = "vcf";
    public static final String PED_OPTION = "ped";

    //REQUIRED INPUT variables 
    private final Path vcfFilePath; //required, no default
    private final Path pedFilePath; //might be required if vcf if a multi-sample family vcf

    //ANALYSIS OPTIONS
    public static final String RUN_FULL_ANALYSIS_OPTION = "full-analysis";

    private final boolean runFullAnalysis;

    //FILTER OPTIONS (these are used for JSON de/serealisation and the command-line)
    public static final String MAX_FREQ_OPTION = "max-freq";
    public static final String MIN_QUAL_OPTION = "min-qual";
    public static final String GENETIC_INTERVAL_OPTION = "restrict-interval";
    public static final String REMOVE_PATHOGENICITY_FILTER_CUTOFF = "keep-non-pathogenic";
    public static final String REMOVE_KNOWN_VARIANTS_OPTION = "remove-known-variants";
    public static final String KEEP_OFF_TARGET_OPTION = "keep-off-target";
    public static final String GENES_TO_KEEP_OPTION = "genes-to-keep";

    //FILTER variables
    //max-freq (command-line was: freq_threshold, refered to variable: frequency_threshold)
    private final float maximumFrequency;
    //Quality threshold for variants. Corresponds to QUAL column in VCF file.
    //min-qual (command-line was: qual, refered to variable: quality_threshold)
    private final float minimumQuality;
    //restrict-interval (command-line was: interval, refered to variable: )
    private final GeneticInterval geneticInterval;
    //remove-path-filter-cutoff
    private final boolean removePathFilterCutOff;
    //remove-dbsnp (command-line was: dbsnp, refered to variable:  filterOutAlldbSNP)
    private final boolean removeKnownVariants;
    //remove-off-target-syn the target filter switch - not specified in the original exomiser as this was a default. 
    private final boolean removeOffTargetVariants;
    //genes to keep in final results
    private final Set<Integer> genesToKeep;

    //PRIORITISER OPTIONS
    public static final String PRIORITISER_OPTION = "prioritiser"; //values for this are handled by PriorityType
    public static final String CANDIDATE_GENE_OPTION = "candidate-gene";
    public static final String HPO_IDS_OPTION = "hpo-ids";
    public static final String SEED_GENES_OPTION = "seed-genes";
    public static final String DISEASE_ID_OPTION = "disease-id";
    public static final String MODE_OF_INHERITANCE_OPTION = "inheritance-mode";
    public static final String EXOMISER2_PARAMS_OPTION = "hiphive-params";

    //PRIORITISER variables
    private final PriorityType prioritiserType;  //required, no default
    //candidate-gene (command-line was: candidate_gene, refered to variable: candidateGene)
    private final String candidateGene;
    //inheritance-mode (command-line was: inheritance, refered to variable: inheritanceMode)
    private final ModeOfInheritance modeOfInheritance;
    //disease-id (command-line was: omim_disease, refered to variable: disease)
    private final String diseaseId;
    //hpo-ids (command-line was: hpo_ids, refered to variable: hpo_ids)
    private final List<String> hpoIds;
    //seed-genes (command-line was: SeedGenes, refered to variable: entrezSeedGenes)
    private final List<Integer> seedGeneList;
    private final String exomiser2Params;

    //OUTPUT OPTIONS (these are used for JSON de/serealisation and the command-line)
    public static final String OUTPUT_PASS_VARIANTS_ONLY_OPTION = "output-pass-variants-only";
    public static final String NUM_GENES_OPTION = "num-genes";
    public static final String OUT_FILE_PREFIX_OPTION = "out-prefix";
    public static final String OUT_FILE_FORMAT_OPTION = "out-format";

    public static final String DEFAULT_OUTPUT_DIR = "results";
    //OUTPUT variables
    private final boolean outputPassVariantsOnly;
    //num-genes (command-line was: ngenes, refered to variable: numberOfGenesToShow)
    private final int numberOfGenesToShow;
    //out-file (command-line was: outfile, refered to variable: outfile)
    private final String outputPrefix;
    //out-format 
    private final Set<OutputFormat> outputFormats;

    //Name of the disease gene family (an OMIM phenotypic series) that is being
    //used for prioritization with ExomeWalker.
    //should this therefore only be a part of an ExomeWalkerOptions?
    //(command-line was: ?, refered to variable: ?)
    private final String diseaseGeneFamilyName;

    private boolean isValid = true;

    public static class SettingsBuilder {

        //BUILD METADATA
        private String buildVersion = "";
        private String buildTimestamp = "";

        //INPUT file options
        private Path vcfFilePath; //required, no default
        private Path pedFilePath = null;

        //ANALYSIS options
        private boolean runFullAnalysis = false;

        //PRIORITISER
        private PriorityType prioritiserType = PriorityType.NONE;

        //FILTER options
        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private GeneticInterval geneticInterval = null;
        private boolean removePathFilterCutOff = false;
        private boolean removeKnownVariants = false;
        private boolean keepOffTargetVariants = false;
        private Set<Integer> geneIdsToKeep = new LinkedHashSet();

        //PRIORITISER options
        private String candidateGene = "";
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        private String diseaseId = "";
        private List<String> hpoIds = new ArrayList();
        private List<Integer> seedGeneList = new ArrayList();
        private String exomiser2Params = "";

        //OUTPUT options
        private boolean outputPassVariantsOnly = false;
        private int numberOfGenesToShow = 0;
        private String outputPrefix = "";
        private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML);

        private String diseaseGeneFamilyName = "";

        @JsonIgnore
        public SettingsBuilder buildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
            return this;
        }

        @JsonIgnore
        public SettingsBuilder buildTimestamp(String buildTimestamp) {
            this.buildTimestamp = buildTimestamp;
            return this;
        }

        @JsonSetter(VCF_OPTION)
        public SettingsBuilder vcfFilePath(Path vcfFilePath) {
            this.vcfFilePath = vcfFilePath;
            return this;
        }

        @JsonSetter(PED_OPTION)
        public SettingsBuilder pedFilePath(Path pedFilePath) {
            this.pedFilePath = pedFilePath;
            return this;
        }

        @JsonSetter(RUN_FULL_ANALYSIS_OPTION)
        public SettingsBuilder runFullAnalysis(boolean runFullAnalysis) {
            this.runFullAnalysis = runFullAnalysis;
            return this;
        }

        @JsonSetter(PRIORITISER_OPTION)
        public SettingsBuilder usePrioritiser(PriorityType prioritiserType) {
            this.prioritiserType = prioritiserType;
            return this;
        }

        @JsonSetter(MAX_FREQ_OPTION)
        public SettingsBuilder maximumFrequency(float value) {
            maximumFrequency = value;
            return this;
        }

        @JsonSetter(MIN_QUAL_OPTION)
        public SettingsBuilder minimumQuality(float value) {
            minimumQuality = value;
            return this;
        }

        @JsonSetter(GENETIC_INTERVAL_OPTION)
        public SettingsBuilder geneticInterval(GeneticInterval value) {
            geneticInterval = value;
            return this;
        }

        @JsonSetter(REMOVE_PATHOGENICITY_FILTER_CUTOFF)
        public SettingsBuilder removePathFilterCutOff(boolean value) {
            removePathFilterCutOff = value;
            return this;
        }

        @JsonSetter(REMOVE_KNOWN_VARIANTS_OPTION)
        public SettingsBuilder removeKnownVariants(boolean value) {
            removeKnownVariants = value;
            return this;
        }

        @JsonSetter(KEEP_OFF_TARGET_OPTION)
        public SettingsBuilder keepOffTargetVariants(boolean value) {
            keepOffTargetVariants = value;
            return this;
        }

        @JsonSetter(CANDIDATE_GENE_OPTION)
        public SettingsBuilder candidateGene(String value) {
            candidateGene = value;
            return this;
        }

        @JsonSetter(MODE_OF_INHERITANCE_OPTION)
        public SettingsBuilder modeOfInheritance(ModeOfInheritance value) {
            modeOfInheritance = value;
            return this;
        }

        @JsonSetter(DISEASE_ID_OPTION)
        public SettingsBuilder diseaseId(String value) {
            diseaseId = value;
            return this;
        }

        @JsonSetter(EXOMISER2_PARAMS_OPTION)
        public SettingsBuilder exomiser2Params(String value) {
            exomiser2Params = value;
            return this;
        }

        @JsonSetter(HPO_IDS_OPTION)
        public SettingsBuilder hpoIdList(List<String> value) {
            hpoIds = value;
            return this;
        }

        @JsonSetter(SEED_GENES_OPTION)
        public SettingsBuilder seedGeneList(List<Integer> value) {
            seedGeneList = value;
            return this;
        }

        @JsonSetter(GENES_TO_KEEP_OPTION)
        public SettingsBuilder genesToKeepList(Set<Integer> value) {
            geneIdsToKeep = value;
            return this;
        }

        @JsonSetter(OUTPUT_PASS_VARIANTS_ONLY_OPTION)
        public SettingsBuilder outputPassVariantsOnly(boolean value) {
            outputPassVariantsOnly = value;
            return this;
        }
        
        @JsonSetter(NUM_GENES_OPTION)
        public SettingsBuilder numberOfGenesToShow(int value) {
            numberOfGenesToShow = value;
            return this;
        }

        @JsonSetter(OUT_FILE_PREFIX_OPTION)
        public SettingsBuilder outputPrefix(String value) {
            outputPrefix = value;
            return this;
        }

        @JsonSetter(OUT_FILE_FORMAT_OPTION)
        public SettingsBuilder outputFormats(Set<OutputFormat> value) {
            outputFormats = value;
            return this;
        }

        @JsonIgnore
        public SettingsBuilder diseaseGeneFamilyName(String value) {
            diseaseGeneFamilyName = value;
            return this;
        }

        public ExomiserSettings build() {
            return new ExomiserSettings(this);
        }
    }

    private ExomiserSettings(SettingsBuilder builder) {

        //build metadata
        buildVersion = builder.buildVersion;
        buildTimestamp = builder.buildTimestamp;

        vcfFilePath = builder.vcfFilePath; //required, no default
        if (vcfFilePath == null) {
            logger.error("Error building ExomiserSettings - VCF file path has not been set, settings are INVALID!");
            isValid = false;
        }
        pedFilePath = builder.pedFilePath;

        //analysis
        runFullAnalysis = builder.runFullAnalysis;

        //Priority
        prioritiserType = builder.prioritiserType;

        //FILTER options
        maximumFrequency = builder.maximumFrequency;
        minimumQuality = builder.minimumQuality;
        geneticInterval = builder.geneticInterval;
        removePathFilterCutOff = builder.removePathFilterCutOff;
        removeKnownVariants = builder.removeKnownVariants;
        removeOffTargetVariants = builder.keepOffTargetVariants;
        genesToKeep = builder.geneIdsToKeep;

        //PRIORITISER options
        candidateGene = builder.candidateGene;
        modeOfInheritance = builder.modeOfInheritance;
        diseaseId = builder.diseaseId;
        hpoIds = builder.hpoIds;
        seedGeneList = builder.seedGeneList;
        exomiser2Params = builder.exomiser2Params;

        //OUTPUT options
        outputPassVariantsOnly = builder.outputPassVariantsOnly;
        numberOfGenesToShow = builder.numberOfGenesToShow;
        if (builder.outputPrefix.isEmpty() && builder.vcfFilePath != null) {
            outputPrefix = generateDefaultOutputPrefix(builder.vcfFilePath, builder.buildVersion);
        } else {
            //here the user has explicitly set a path for where and what they want the output file to be called.
            outputPrefix = builder.outputPrefix;
        }
        outputFormats = builder.outputFormats;

        diseaseGeneFamilyName = builder.diseaseGeneFamilyName;
    }

    /**
     * The default output name is set to the vcf file name (minus the full
     * path), unless the filename is explicitly set by the user.
     *
     * @param vcfFilePath
     */
    private String generateDefaultOutputPrefix(Path vcfFilePath, String buildVersion) {
        String defaultOutputPrefix;
        Path vcfFilename = vcfFilePath.getFileName();
        if (buildVersion.isEmpty()) {
            defaultOutputPrefix = String.format("%s/%s-exomiser-results", DEFAULT_OUTPUT_DIR, vcfFilename);
        } else {
            defaultOutputPrefix = String.format("%s/%s-exomiser-%s-results", DEFAULT_OUTPUT_DIR, vcfFilename, buildVersion);
        }
        logger.debug("Output prefix was unspecified. Now set to: {}", defaultOutputPrefix);
        return defaultOutputPrefix;
    }

    @JsonIgnore
    public boolean isValid() {
        return isValid;
    }

    @JsonIgnore
    public Path getVcfPath() {
        return vcfFilePath;
    }

    @JsonProperty(VCF_OPTION)
    public Path vcfFileName() {
        return vcfFilePath.getFileName();
    }

    @JsonIgnore
    public Path getPedPath() {
        return pedFilePath;
    }

    @JsonProperty(PED_OPTION)
    public Path getPedFileName() {
        return ((pedFilePath == null) ? null : pedFilePath.getFileName());
    }

    @JsonProperty(RUN_FULL_ANALYSIS_OPTION)
    public boolean runFullAnalysis() {
        return runFullAnalysis;
    }

    @JsonProperty(PRIORITISER_OPTION)
    public PriorityType getPrioritiserType() {
        return prioritiserType;
    }

    @JsonProperty(MAX_FREQ_OPTION)
    public float getMaximumFrequency() {
        return maximumFrequency;
    }

    @JsonProperty(MIN_QUAL_OPTION)
    public float getMinimumQuality() {
        return minimumQuality;
    }

    @JsonProperty(GENETIC_INTERVAL_OPTION)
    public GeneticInterval getGeneticInterval() {
        return geneticInterval;
    }

    @JsonProperty(REMOVE_PATHOGENICITY_FILTER_CUTOFF)
    public boolean removePathFilterCutOff() {
        return removePathFilterCutOff;
    }

    @JsonProperty(REMOVE_KNOWN_VARIANTS_OPTION)
    public boolean removeKnownVariants() {
        return removeKnownVariants;
    }

    @JsonProperty(KEEP_OFF_TARGET_OPTION)
    public boolean keepOffTargetVariants() {
        return removeOffTargetVariants;
    }

    @JsonProperty(CANDIDATE_GENE_OPTION)
    public String getCandidateGene() {
        return candidateGene;
    }

    @JsonProperty(MODE_OF_INHERITANCE_OPTION)
    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    @JsonProperty(DISEASE_ID_OPTION)
    public String getDiseaseId() {
        return diseaseId;
    }

    @JsonProperty(EXOMISER2_PARAMS_OPTION)
    public String getExomiser2Params() {
        return exomiser2Params;
    }

    @JsonProperty(HPO_IDS_OPTION)
    public List<String> getHpoIds() {
        return hpoIds;
    }

    @JsonProperty(SEED_GENES_OPTION)
    public List<Integer> getSeedGeneList() {
        return seedGeneList;
    }

    @JsonProperty(GENES_TO_KEEP_OPTION)
    public Set<Integer> getGenesToKeep() {
        return genesToKeep;
    }

    @JsonSetter(OUTPUT_PASS_VARIANTS_ONLY_OPTION)
    public boolean outputPassVariantsOnly() {
        return outputPassVariantsOnly;
    }
    
    @JsonProperty(NUM_GENES_OPTION)
    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    @JsonProperty(OUT_FILE_FORMAT_OPTION)
    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    @JsonProperty(OUT_FILE_PREFIX_OPTION)
    public String getOutputPrefix() {
        return outputPrefix;
    }

    @JsonProperty(BUILD_VERSION)
    public String getBuildVersion() {
        return buildVersion;
    }

    @JsonProperty(BUILD_TIMESTAMP)
    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.buildVersion);
        hash = 11 * hash + Objects.hashCode(this.buildTimestamp);
        hash = 11 * hash + Objects.hashCode(this.vcfFilePath);
        hash = 11 * hash + Objects.hashCode(this.pedFilePath);
        hash = 11 * hash + (this.runFullAnalysis ? 1 : 0);
        hash = 11 * hash + Float.floatToIntBits(this.maximumFrequency);
        hash = 11 * hash + Float.floatToIntBits(this.minimumQuality);
        hash = 11 * hash + Objects.hashCode(this.geneticInterval);
        hash = 11 * hash + (this.removePathFilterCutOff ? 1 : 0);
        hash = 11 * hash + (this.removeKnownVariants ? 1 : 0);
        hash = 11 * hash + (this.removeOffTargetVariants ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.genesToKeep);
        hash = 11 * hash + Objects.hashCode(this.prioritiserType);
        hash = 11 * hash + Objects.hashCode(this.candidateGene);
        hash = 11 * hash + Objects.hashCode(this.modeOfInheritance);
        hash = 11 * hash + Objects.hashCode(this.diseaseId);
        hash = 11 * hash + Objects.hashCode(this.hpoIds);
        hash = 11 * hash + Objects.hashCode(this.seedGeneList);
        hash = 11 * hash + Objects.hashCode(this.exomiser2Params);
        hash = 11 * hash + (this.outputPassVariantsOnly ? 1 : 0);
        hash = 11 * hash + this.numberOfGenesToShow;
        hash = 11 * hash + Objects.hashCode(this.outputPrefix);
        hash = 11 * hash + Objects.hashCode(this.outputFormats);
        hash = 11 * hash + Objects.hashCode(this.diseaseGeneFamilyName);
        hash = 11 * hash + (this.isValid ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExomiserSettings other = (ExomiserSettings) obj;
        if (!Objects.equals(this.buildVersion, other.buildVersion)) {
            return false;
        }
        if (!Objects.equals(this.buildTimestamp, other.buildTimestamp)) {
            return false;
        }
        if (!Objects.equals(this.vcfFilePath, other.vcfFilePath)) {
            return false;
        }
        if (!Objects.equals(this.pedFilePath, other.pedFilePath)) {
            return false;
        }
        if (this.runFullAnalysis != other.runFullAnalysis) {
            return false;
        }
        if (Float.floatToIntBits(this.maximumFrequency) != Float.floatToIntBits(other.maximumFrequency)) {
            return false;
        }
        if (Float.floatToIntBits(this.minimumQuality) != Float.floatToIntBits(other.minimumQuality)) {
            return false;
        }
        if (!Objects.equals(this.geneticInterval, other.geneticInterval)) {
            return false;
        }
        if (this.removePathFilterCutOff != other.removePathFilterCutOff) {
            return false;
        }
        if (this.removeKnownVariants != other.removeKnownVariants) {
            return false;
        }
        if (this.removeOffTargetVariants != other.removeOffTargetVariants) {
            return false;
        }
        if (!Objects.equals(this.genesToKeep, other.genesToKeep)) {
            return false;
        }
        if (this.prioritiserType != other.prioritiserType) {
            return false;
        }
        if (!Objects.equals(this.candidateGene, other.candidateGene)) {
            return false;
        }
        if (this.modeOfInheritance != other.modeOfInheritance) {
            return false;
        }
        if (!Objects.equals(this.diseaseId, other.diseaseId)) {
            return false;
        }
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        if (!Objects.equals(this.seedGeneList, other.seedGeneList)) {
            return false;
        }
        if (!Objects.equals(this.exomiser2Params, other.exomiser2Params)) {
            return false;
        }
        if (this.outputPassVariantsOnly != other.outputPassVariantsOnly) {
            return false;
        }
        if (this.numberOfGenesToShow != other.numberOfGenesToShow) {
            return false;
        }
        if (!Objects.equals(this.outputPrefix, other.outputPrefix)) {
            return false;
        }
        if (!Objects.equals(this.outputFormats, other.outputFormats)) {
            return false;
        }
        if (!Objects.equals(this.diseaseGeneFamilyName, other.diseaseGeneFamilyName)) {
            return false;
        }
        if (this.isValid != other.isValid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExomiserSettings{" + "vcfFilePath=" + vcfFilePath + ", pedFilePath=" + pedFilePath + ", prioritiser=" + prioritiserType + ", maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", removePathFilterCutOff=" + removePathFilterCutOff + ", removeDbSnp=" + removeKnownVariants + ", removeOffTargetVariants=" + removeOffTargetVariants + ", candidateGene=" + candidateGene + ", modeOfInheritance=" + modeOfInheritance + ", diseaseId=" + diseaseId + ", hpoIds=" + hpoIds + ", seedGeneList=" + seedGeneList + ", numberOfGenesToShow=" + numberOfGenesToShow + ", outFileName=" + outputPrefix + ", outputFormat=" + outputFormats + ", diseaseGeneFamilyName=" + diseaseGeneFamilyName + ", buildVersion=" + buildVersion + ", buildTimestamp=" + buildTimestamp + '}';
    }

}
