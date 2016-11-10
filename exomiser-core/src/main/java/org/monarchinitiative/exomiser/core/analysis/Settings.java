/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;
import org.monarchinitiative.exomiser.core.filters.FilterSettings;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.prioritisers.PrioritiserSettings;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

/**
 * Class for storing the options data required for running the Exomiser.
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = SettingsBuilder.class)
public class Settings implements FilterSettings, PrioritiserSettings, OutputSettings {

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    //The options (static strings) and variables below are grouped together for 
    //better readability. They are bound using the Json annotations 

    public static final String BUILD_VERSION = "build-version";
    public static final String BUILD_TIMESTAMP = "build-timestamp";

    private final String buildVersion;
    private final String buildTimestamp;

    //REQUIRED INPUT variables 
    private final Path vcfFilePath; //required, no default
    private final Path pedFilePath; //might be required if vcf if a multi-sample family vcf

    //ANALYSIS OPTIONS
    private final boolean runFullAnalysis;

    //FILTER variables
    //max-freq (command-line was: freq_threshold, refered to variable: frequency_threshold)
    private final float maximumFrequency;
    //Quality threshold for variants. Corresponds to QUAL column in VCF file.
    //min-qual (command-line was: qual, refered to variable: quality_threshold)
    private final float minimumQuality;
    //restrict-interval (command-line was: interval, refered to variable: )
    private final GeneticInterval geneticInterval;
    //remove-path-filter-cutoff
    private final boolean keepNonPathogenicVariants;
    //remove-dbsnp (command-line was: dbsnp, refered to variable:  filterOutAlldbSNP)
    private final boolean removeKnownVariants;
    //remove-off-target-syn the target filter switch - not specified in the original exomiser as this was a default. 
    private final boolean keepOffTargetVariants;
    //genes to keep in final results
    private final Set<Integer> genesToKeep;

    //PRIORITISER variables
    private final PriorityType prioritiserType;  //default is NONE
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
    private final String hiPhiveParams;

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

    @JsonCreator
    public static SettingsBuilder builder() {
        return new SettingsBuilder();
    }

    public static class SettingsBuilder {

        //BUILD METADATA
        private String buildVersion = "";
        private String buildTimestamp = "";

        //INPUT file options
        private Path vcfFilePath; //required, no default
        private Path pedFilePath = null;

        //ANALYSIS options
        private boolean runFullAnalysis = false;

        //FILTER options
        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private GeneticInterval geneticInterval = null;
        private boolean removePathFilterCutOff = false;
        private boolean removeKnownVariants = false;
        private boolean keepOffTargetVariants = false;
        private Set<Integer> geneIdsToKeep = new LinkedHashSet();

        //PRIORITISER
        private PriorityType prioritiserType = PriorityType.NONE;

        //PRIORITISER options
        private String candidateGene = "";
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;
        private String diseaseId = "";
        private List<String> hpoIds = new ArrayList();
        private List<Integer> seedGeneList = new ArrayList();
        private String hiPhiveParams = "";

        //OUTPUT options
        private boolean outputPassVariantsOnly = false;
        private int numberOfGenesToShow = 0;
        private String outputPrefix = "";
        private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML);

        private String diseaseGeneFamilyName = "";

        private SettingsBuilder() {}

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

        @JsonSetter("vcf")
        public SettingsBuilder vcfFilePath(Path vcfFilePath) {
            this.vcfFilePath = vcfFilePath;
            return this;
        }

        @JsonSetter("ped")
        public SettingsBuilder pedFilePath(Path pedFilePath) {
            this.pedFilePath = pedFilePath;
            return this;
        }

        @JsonSetter
        public SettingsBuilder runFullAnalysis(boolean runFullAnalysis) {
            this.runFullAnalysis = runFullAnalysis;
            return this;
        }

        @JsonSetter("prioritiser")
        public SettingsBuilder usePrioritiser(PriorityType prioritiserType) {
            this.prioritiserType = prioritiserType;
            return this;
        }

        @JsonSetter("maxFrequency")
        public SettingsBuilder maximumFrequency(float value) {
            maximumFrequency = value;
            return this;
        }

        @JsonSetter("minQuality")
        public SettingsBuilder minimumQuality(float value) {
            minimumQuality = value;
            return this;
        }

        @JsonSetter("interval")
        public SettingsBuilder geneticInterval(GeneticInterval value) {
            geneticInterval = value;
            return this;
        }

        @JsonSetter("keepNonPathogenic")
        public SettingsBuilder keepNonPathogenic(boolean value) {
            removePathFilterCutOff = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder removeKnownVariants(boolean value) {
            removeKnownVariants = value;
            return this;
        }

        @JsonSetter("keepOffTarget")
        public SettingsBuilder keepOffTargetVariants(boolean value) {
            keepOffTargetVariants = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder candidateGene(String value) {
            candidateGene = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder modeOfInheritance(ModeOfInheritance value) {
            modeOfInheritance = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder diseaseId(String value) {
            diseaseId = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder hiPhiveParams(String value) {
            hiPhiveParams = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder hpoIdList(List<String> value) {
            hpoIds = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder seedGeneList(List<Integer> value) {
            seedGeneList = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder genesToKeep(Set<Integer> value) {
            geneIdsToKeep = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder outputPassVariantsOnly(boolean value) {
            outputPassVariantsOnly = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder numberOfGenesToShow(int value) {
            numberOfGenesToShow = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder outputPrefix(String value) {
            outputPrefix = value;
            return this;
        }

        @JsonSetter
        public SettingsBuilder outputFormats(Set<OutputFormat> value) {
            outputFormats = value;
            return this;
        }

        @JsonIgnore
        public SettingsBuilder diseaseGeneFamilyName(String value) {
            diseaseGeneFamilyName = value;
            return this;
        }

        public Settings build() {
            return new Settings(this);
        }

    }

    private Settings(SettingsBuilder builder) {

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
        keepNonPathogenicVariants = builder.removePathFilterCutOff;
        removeKnownVariants = builder.removeKnownVariants;
        keepOffTargetVariants = builder.keepOffTargetVariants;
        genesToKeep = builder.geneIdsToKeep;

        //PRIORITISER options
        candidateGene = builder.candidateGene;
        modeOfInheritance = builder.modeOfInheritance;
        diseaseId = builder.diseaseId;
        hpoIds = builder.hpoIds;
        seedGeneList = builder.seedGeneList;
        hiPhiveParams = builder.hiPhiveParams;

        //OUTPUT options
        outputPassVariantsOnly = builder.outputPassVariantsOnly;
        numberOfGenesToShow = builder.numberOfGenesToShow;
        outputPrefix = builder.outputPrefix;
        outputFormats = builder.outputFormats;

        diseaseGeneFamilyName = builder.diseaseGeneFamilyName;
    }
    
    @JsonIgnore
    public boolean isValid() {
        return isValid;
    }

    @JsonIgnore
    public Path getVcfPath() {
        return vcfFilePath;
    }

    @JsonProperty
    public Path vcfFileName() {
        return vcfFilePath.getFileName();
    }

    @JsonIgnore
    public Path getPedPath() {
        return pedFilePath;
    }

    @JsonProperty
    public Path getPedFileName() {
        return pedFilePath == null ? null : pedFilePath.getFileName();
    }

    @JsonProperty
    public boolean runFullAnalysis() {
        return runFullAnalysis;
    }

    @JsonProperty
    @Override
    public PriorityType getPrioritiserType() {
        return prioritiserType;
    }

    @JsonProperty
    @Override
    public float getMaximumFrequency() {
        return maximumFrequency;
    }

    @JsonProperty
    @Override
    public float getMinimumQuality() {
        return minimumQuality;
    }

    @JsonProperty
    @Override
    public GeneticInterval getGeneticInterval() {
        return geneticInterval;
    }

    @JsonProperty("keepNonPathogenic")
    @Override
    public boolean keepNonPathogenicVariants() {
        return keepNonPathogenicVariants;
    }

    @JsonProperty
    @Override
    public boolean removeKnownVariants() {
        return removeKnownVariants;
    }

    @JsonProperty
    @Override
    public boolean keepOffTargetVariants() {
        return keepOffTargetVariants;
    }

    @JsonProperty
    @Override
    public String getCandidateGene() {
        return candidateGene;
    }

    @JsonProperty
    @Override
    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    @JsonProperty
    @Override
    public String getDiseaseId() {
        return diseaseId;
    }

    @JsonProperty
    @Override
    public String getHiPhiveParams() {
        return hiPhiveParams;
    }

    @JsonProperty
    @Override
    public List<String> getHpoIds() {
        return hpoIds;
    }

    @JsonProperty
    @Override
    public List<Integer> getSeedGeneList() {
        return seedGeneList;
    }

    @JsonProperty
    @Override
    public Set<Integer> getGenesToKeep() {
        return genesToKeep;
    }

    @JsonSetter
    @Override
    public boolean outputPassVariantsOnly() {
        return outputPassVariantsOnly;
    }

    @JsonProperty
    @Override
    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    @JsonProperty
    @Override
    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    @JsonProperty
    @Override
    public String getOutputPrefix() {
        return outputPrefix;
    }

    @JsonProperty
    public String getBuildVersion() {
        return buildVersion;
    }

    @JsonProperty
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
        hash = 11 * hash + (this.keepNonPathogenicVariants ? 1 : 0);
        hash = 11 * hash + (this.removeKnownVariants ? 1 : 0);
        hash = 11 * hash + (this.keepOffTargetVariants ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.genesToKeep);
        hash = 11 * hash + Objects.hashCode(this.prioritiserType);
        hash = 11 * hash + Objects.hashCode(this.candidateGene);
        hash = 11 * hash + Objects.hashCode(this.modeOfInheritance);
        hash = 11 * hash + Objects.hashCode(this.diseaseId);
        hash = 11 * hash + Objects.hashCode(this.hpoIds);
        hash = 11 * hash + Objects.hashCode(this.seedGeneList);
        hash = 11 * hash + Objects.hashCode(this.hiPhiveParams);
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
        final Settings other = (Settings) obj;
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
        if (this.keepNonPathogenicVariants != other.keepNonPathogenicVariants) {
            return false;
        }
        if (this.removeKnownVariants != other.removeKnownVariants) {
            return false;
        }
        if (this.keepOffTargetVariants != other.keepOffTargetVariants) {
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
        if (!Objects.equals(this.hiPhiveParams, other.hiPhiveParams)) {
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
        return this.isValid == other.isValid;
    }

    @Override
    public String toString() {
        return "ExomiserSettings{" + "vcfFilePath=" + vcfFilePath + ", pedFilePath=" + pedFilePath + ", prioritiser=" + prioritiserType + ", maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", keepNonPathogenicVariants=" + keepNonPathogenicVariants + ", removeDbSnp=" + removeKnownVariants + ", removeOffTargetVariants=" + keepOffTargetVariants + ", candidateGene=" + candidateGene + ", modeOfInheritance=" + modeOfInheritance + ", diseaseId=" + diseaseId + ", hpoIds=" + hpoIds + ", seedGeneList=" + seedGeneList + ", numberOfGenesToShow=" + numberOfGenesToShow + ", outFileName=" + outputPrefix + ", outputFormat=" + outputFormats + ", diseaseGeneFamilyName=" + diseaseGeneFamilyName + ", buildVersion=" + buildVersion + ", buildTimestamp=" + buildTimestamp + '}';
    }

}
