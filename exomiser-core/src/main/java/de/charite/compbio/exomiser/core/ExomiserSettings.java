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
import jannovar.common.ModeOfInheritance;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
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
    public static final String PRIORITISER_OPTION = "prioritiser"; //values for this are handled by PriorityType

    //REQUIRED INPUT variables 
    private final Path vcfFilePath; //required, no default
    private final Path pedFilePath; //might be required if vcf if a multi-sample family vcf
    private final PriorityType prioritiserType;  //required, no default

    //ANALYSIS OPTIONS
    public static final String RUN_FULL_ANALYSIS_OPTION = "full-analysis";
    
    private final boolean runFullAnalysis;
    
    //FILTER OPTIONS (these are used for JSON de/serealisation and the command-line)
    public static final String MAX_FREQ_OPTION = "max-freq";
    public static final String MIN_QUAL_OPTION = "min-qual";
    public static final String GENETIC_INTERVAL_OPTION = "restrict-interval";
    public static final String REMOVE_PATHOGENICITY_FILTER_CUTOFF = "keep-non-pathogenic";
    public static final String REMOVE_DBSNP_OPTION = "remove-dbsnp";
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
    private final boolean removeDbSnp;
    //remove-off-target-syn the target filter switch - not specified in the original exomiser as this was a default. 
    private final boolean removeOffTargetVariants;
    //genes to keep in final results
    private final Set<Integer> genesToKeep;
    
    //PRIORITISER OPTIONS
    public static final String CANDIDATE_GENE_OPTION = "candidate-gene";
    public static final String HPO_IDS_OPTION = "hpo-ids";
    public static final String SEED_GENES_OPTION = "seed-genes";
    public static final String DISEASE_ID_OPTION = "disease-id";
    public static final String MODE_OF_INHERITANCE_OPTION = "inheritance-mode";
    public static final String EXOMISER2_PARAMS_OPTION = "hiphive-params";

    //PRIORITISER variables
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
    public static final String NUM_GENES_OPTION = "num-genes";
    public static final String OUT_FILE_OPTION = "out-file";
    public static final String OUT_FORMAT_OPTION = "out-format";

    public static final String DEFAULT_OUTPUT_DIR = "results";
    //OUTPUT variables
    //num-genes (command-line was: ngenes, refered to variable: numberOfGenesToShow)
    private final int numberOfGenesToShow;
    //out-file (command-line was: outfile, refered to variable: outfile)
    private final String outFileName;
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
        private PriorityType prioritiserType = PriorityType.NOT_SET;
        //FILTER options
        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private GeneticInterval geneticInterval = null;
        private boolean removePathFilterCutOff = false;
        private boolean removeDbSnp = false;
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
        private int numberOfGenesToShow = 0;
        private String outFileName = "";
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

        @JsonSetter(REMOVE_DBSNP_OPTION)
        public SettingsBuilder removeDbSnp(boolean value) {
            removeDbSnp = value;
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

        @JsonSetter(NUM_GENES_OPTION)
        public SettingsBuilder numberOfGenesToShow(int value) {
            numberOfGenesToShow = value;
            return this;
        }

        @JsonSetter(OUT_FILE_OPTION)
        public SettingsBuilder outFileName(String value) {
            outFileName = value;
            return this;
        }

        @JsonSetter(OUT_FORMAT_OPTION)
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
        prioritiserType = builder.prioritiserType;  //required, no default
        if (prioritiserType == PriorityType.NOT_SET) {
            logger.error("Error building ExomiserSettings - Prioritiser has not been set, settings are INVALID!");
            isValid = false;
        }
        //FILTER options
        maximumFrequency = builder.maximumFrequency;
        minimumQuality = builder.minimumQuality;
        geneticInterval = builder.geneticInterval;
        removePathFilterCutOff = builder.removePathFilterCutOff;
        removeDbSnp = builder.removeDbSnp;
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
        numberOfGenesToShow = builder.numberOfGenesToShow;
        if (builder.outFileName.isEmpty() && builder.vcfFilePath != null) {
            outFileName = generateDefaultOutputFileName(builder.vcfFilePath, builder.buildVersion);
        } else {
            //here the user has explicitly set a path for where and what they want the output file to be called.
            outFileName = builder.outFileName;
        }
        outputFormats = builder.outputFormats;

        diseaseGeneFamilyName = builder.diseaseGeneFamilyName;
    }

    /**
     * The default output name is set to the vcf file name (minus the full path
     * and file extension), unless the filename is explicitly set by the user.
     *
     * @param vcfFilePath
     */
    private String generateDefaultOutputFileName(Path vcfFilePath, String buildVersion) {
        String outputFileName;
        String vcfFilenameWithoutExtension = FilenameUtils.removeExtension(vcfFilePath.toFile().getName());
        if (buildVersion.isEmpty()) {
            outputFileName = String.format("%s/%s-exomiser-results", DEFAULT_OUTPUT_DIR, vcfFilenameWithoutExtension);
        } else {
            outputFileName = String.format("%s/%s-exomiser-%s-results", DEFAULT_OUTPUT_DIR, vcfFilenameWithoutExtension, buildVersion);   
        }
        logger.debug("Output filename set to: {}", outputFileName);
        return outputFileName;
    }


    @JsonIgnore
    public boolean isValid() {
        return isValid;
    }

    @JsonProperty(VCF_OPTION)
    public Path getVcfPath() {
        return vcfFilePath;
    }

    @JsonProperty(PED_OPTION)
    public Path getPedPath() {
        return pedFilePath;
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

    @JsonProperty(REMOVE_DBSNP_OPTION)
    public boolean removeDbSnp() {
        return removeDbSnp;
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
    
    @JsonProperty(NUM_GENES_OPTION)
    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    @JsonProperty(OUT_FORMAT_OPTION)
    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    @JsonProperty(OUT_FILE_OPTION)
    public String getOutFileName() {
        return outFileName;
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
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.vcfFilePath);
        hash = 13 * hash + Objects.hashCode(this.pedFilePath);
        hash = 13 * hash + Objects.hashCode(this.prioritiserType);
        hash = 13 * hash + (this.runFullAnalysis ? 1 : 0);
        hash = 13 * hash + Float.floatToIntBits(this.maximumFrequency);
        hash = 13 * hash + Float.floatToIntBits(this.minimumQuality);
        hash = 13 * hash + Objects.hashCode(this.geneticInterval);
        hash = 13 * hash + (this.removePathFilterCutOff ? 1 : 0);
        hash = 13 * hash + (this.removeDbSnp ? 1 : 0);
        hash = 13 * hash + (this.removeOffTargetVariants ? 1 : 0);
        hash = 13 * hash + Objects.hashCode(this.genesToKeep);
        hash = 13 * hash + Objects.hashCode(this.candidateGene);
        hash = 13 * hash + Objects.hashCode(this.modeOfInheritance);
        hash = 13 * hash + Objects.hashCode(this.diseaseId);
        hash = 13 * hash + Objects.hashCode(this.hpoIds);
        hash = 13 * hash + Objects.hashCode(this.seedGeneList);
        hash = 13 * hash + Objects.hashCode(this.exomiser2Params);
        hash = 13 * hash + this.numberOfGenesToShow;
        hash = 13 * hash + Objects.hashCode(this.outFileName);
        hash = 13 * hash + Objects.hashCode(this.outputFormats);
        hash = 13 * hash + Objects.hashCode(this.diseaseGeneFamilyName);
        hash = 13 * hash + (this.isValid ? 1 : 0);
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
        if (!Objects.equals(this.vcfFilePath, other.vcfFilePath)) {
            return false;
        }
        if (!Objects.equals(this.pedFilePath, other.pedFilePath)) {
            return false;
        }
        if (this.prioritiserType != other.prioritiserType) {
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
        if (this.removeDbSnp != other.removeDbSnp) {
            return false;
        }
        if (this.removeOffTargetVariants != other.removeOffTargetVariants) {
            return false;
        }
        if (!Objects.equals(this.genesToKeep, other.genesToKeep)) {
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
        if (this.numberOfGenesToShow != other.numberOfGenesToShow) {
            return false;
        }
        if (!Objects.equals(this.outFileName, other.outFileName)) {
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
        return "ExomiserSettings{" + "vcfFilePath=" + vcfFilePath + ", pedFilePath=" + pedFilePath + ", prioritiser=" + prioritiserType + ", maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", removePathFilterCutOff=" + removePathFilterCutOff + ", removeDbSnp=" + removeDbSnp + ", removeOffTargetVariants=" + removeOffTargetVariants + ", candidateGene=" + candidateGene + ", modeOfInheritance=" + modeOfInheritance + ", diseaseId=" + diseaseId + ", hpoIds=" + hpoIds + ", seedGeneList=" + seedGeneList + ", numberOfGenesToShow=" + numberOfGenesToShow + ", outFileName=" + outFileName + ", outputFormat=" + outputFormats + ", diseaseGeneFamilyName=" + diseaseGeneFamilyName + ", buildVersion=" + buildVersion + ", buildTimestamp=" + buildTimestamp + '}';
    }

}
