/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.util;

import de.charite.compbio.exomiser.priority.BoqaPriority;
import de.charite.compbio.exomiser.priority.DynamicPhenoWandererPriority;
import de.charite.compbio.exomiser.priority.GenewandererPriority;
import de.charite.compbio.exomiser.priority.MGIPhenodigmPriority;
import de.charite.compbio.exomiser.priority.PhenoWandererPriority;
import de.charite.compbio.exomiser.priority.PhenomizerPriority;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.UberphenoPriority;
import de.charite.compbio.exomiser.priority.ZFINPhenodigmPriority;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for storing the options data required for running the Exomiser.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserSettings {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserSettings.class);

    //Input file options
    private final Path vcfFilePath; //required, no default
    private final Path pedFilePath;

    //Priority
    private final Priority prioritiser;  //required, no default
    private final Class<? extends Priority> prioritiserClass; //don't like this - would rather set the Priotity ready to go.

    //FILTER options
    //max-freq (command-line was: freq_threshold, refered to variable: frequency_threshold)
    private final float maximumFrequency;
    /**
     * Quality threshold for variants. Corresponds to QUAL column in VCF file.
     */
    //min-qual (command-line was: qual, refered to variable: quality_threshold)
    private final float minimumQuality;
    //restrict-interval (command-line was: interval, refered to variable: )
    private final String geneticInterval;
    //include-pathogenic (command-line was: path, refered to variable: use_pathogenicity_filter)
    private final boolean includePathogenic;
    //remove-dbsnp (command-line was: dbsnp, refered to variable:  filterOutAlldbSNP)
    private final boolean removeDbSnp;
    //remove-off-target-syn the target filter switch - not specified in the original exomiser as this was a default. 
    private final boolean removeOffTargetVariants;

    //PRIORITISER options
    //candidate-gene (command-line was: candidate_gene, refered to variable: candidateGene)
    private final String candidateGene;
    /**
     * This String can be set to AD, AR, or X to initiate filtering according to
     * inheritance pattern.
     */
    //inheritance-mode (command-line was: inheritance, refered to variable: inheritanceMode)
    private final ModeOfInheritance modeOfInheritance;
    //disease-id (command-line was: omim_disease, refered to variable: disease)
    private final String diseaseId;
    //hpo-ids (command-line was: hpo_ids, refered to variable: hpo_ids)
    private final List<String> hpoIds;
    //seed-genes (command-line was: SeedGenes, refered to variable: entrezSeedGenes)
    private final List<String> seedGeneList;

    //OUTPUT options
    //num-genes (command-line was: ngenes, refered to variable: numberOfGenesToShow)
    private final int numberOfGenesToShow;
    //out-file (command-line was: outfile, refered to variable: outfile)
    private final String outFileName;
    //out-format 
    private final OutputFormat outputFormat;

    /**
     * Name of the disease gene family (an OMIM phenotypic series) that is being
     * used for prioritization with ExomeWalker.
     */
    //should this therefore only be a part of an ExomeWalkerOptions?
    //(command-line was: ?, refered to variable: ?)
    private final String diseaseGeneFamilyName;

    public static class ExomiserOptionsBuilder {

        //INPUT file options
        private Path vcfFilePath; //required, no default
        private Path pedFilePath = null;

        //PRIORITISER
        private String prioritiser;  //required, no default
        private Class<? extends Priority> prioritiserClass; //don't like this - would rather set the Priotity ready to go.

        //FILTER options
        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private String geneticInterval = "";
        private boolean includePathogenic = false;
        private boolean removeDbSnp = true;
        private boolean removeOffTargetVariants = true;

        //PRIORITISER options
        private String candidateGene = "";
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        private String diseaseId = "";
        private List<String> hpoIds = new ArrayList();
        private List<String> seedGeneList = new ArrayList();

        //OUTPUT options
        private int numberOfGenesToShow = 0;
        private String outFileName = "exomiser.html";
        private OutputFormat outputFormat = OutputFormat.HTML;

        private String diseaseGeneFamilyName = "";

        public ExomiserOptionsBuilder vcfFilePath(Path vcfFilePath) {
            this.vcfFilePath = vcfFilePath;
            return this;
        }

        public ExomiserOptionsBuilder pedFilePath(Path pedFilePath) {
            this.pedFilePath = pedFilePath;
            return this;
        }

        public ExomiserOptionsBuilder usePrioritiser(String prioritiser) {
            this.prioritiser = prioritiser;
            return this;
        }

        public ExomiserOptionsBuilder maximumFrequency(float value) {
            maximumFrequency = value;
            return this;
        }

        public ExomiserOptionsBuilder minimumQuality(float value) {
            minimumQuality = value;
            return this;
        }

        public ExomiserOptionsBuilder geneticInterval(String value) {
            geneticInterval = value;
            return this;
        }
        
        public ExomiserOptionsBuilder includePathogenic(boolean value) {
            includePathogenic = value;
            return this;
        }

        public ExomiserOptionsBuilder removeDbSnp(boolean value) {
            removeDbSnp = value;
            return this;
        }

        public ExomiserOptionsBuilder removeOffTargetVariants(boolean value) {
            removeOffTargetVariants = value;
            return this;
        }

        public ExomiserOptionsBuilder candidateGene(String value) {
            candidateGene = value;
            return this;
        }

        public ExomiserOptionsBuilder modeOfInheritance(ModeOfInheritance value) {
            modeOfInheritance = value;
            return this;
        }

        public ExomiserOptionsBuilder diseaseId(String value) {
            diseaseId = value;
            return this;
        }

        public ExomiserOptionsBuilder hpoIdList(List<String> value) {
            hpoIds = value;
            return this;
        }

        public ExomiserOptionsBuilder seedGeneList(List<String> value) {
            seedGeneList = value;
            return this;
        }

        public ExomiserOptionsBuilder numberOfGenesToShow(int value) {
            numberOfGenesToShow = value;
            return this;
        }

        public ExomiserOptionsBuilder outFileName(String value) {
            outFileName = value;
            return this;
        }
        
        public ExomiserOptionsBuilder outputFormat(OutputFormat value) {
            outputFormat = value;
            return this;
        }

        public ExomiserOptionsBuilder diseaseGeneFamilyName(String value) {
            diseaseGeneFamilyName = value;
            return this;
        }

        public ExomiserSettings build() {
            return new ExomiserSettings(this);
        }
    }

    private ExomiserSettings(ExomiserOptionsBuilder builder) {

        Map<String, Class<? extends Priority>> commandLineToPriorityMap = new HashMap();;
        //this feels wrong... ought to use an EnumMap with types?
        commandLineToPriorityMap.put("boqa", BoqaPriority.class);
        commandLineToPriorityMap.put("dynamic-pheno-wanderer", DynamicPhenoWandererPriority.class);
        commandLineToPriorityMap.put("gene-wanderer", GenewandererPriority.class);
        commandLineToPriorityMap.put("mgi-phenodigm", MGIPhenodigmPriority.class);
        commandLineToPriorityMap.put("pheno-wanderer", PhenoWandererPriority.class);
        commandLineToPriorityMap.put("phenomizer", PhenomizerPriority.class);
        commandLineToPriorityMap.put("uber-pheno", UberphenoPriority.class);
        commandLineToPriorityMap.put("zfin-phenodigm", ZFINPhenodigmPriority.class);

        //TODO: these chaps are usually specified implicitly - perhaps they should be different types of ExomiserSettings?
        //probably better as a specific type of Exomiser - either a RareDiseaseExomiser or DefaultExomiser. These might be badly named as the OMIM proritiser is currently the default.
        //InheritancePriority ALWAYS runs but uses a default InheritanceMode.UNSPECIFIED
//        commandLineToPriorityMap.put(vcfFilePath, InheritancePriority.class);
        //TODO: check how this works - I think it is always included.
//        commandLineToPriorityMap.put(vcfFilePath, OMIMPriority.class);
        //not sure if this is actually used at all....
//        commandLineToPriorityMap.put(vcfFilePath, DynamicPhenodigmPriority.class);
        vcfFilePath = builder.vcfFilePath; //required, no default
        pedFilePath = builder.pedFilePath;

        //Priority
        prioritiser = null; //builder.prioritiser;  //required, no default
        prioritiserClass = commandLineToPriorityMap.get(builder.prioritiser); //don't like this - would rather set the Priotity ready to go.

        //FILTER options
        maximumFrequency = builder.maximumFrequency;
        minimumQuality = builder.minimumQuality;
        geneticInterval = builder.geneticInterval;
        includePathogenic = builder.includePathogenic;
        removeDbSnp = builder.removeDbSnp;
        removeOffTargetVariants = builder.removeOffTargetVariants;

        //PRIORITISER options
        candidateGene = builder.candidateGene;
        modeOfInheritance = builder.modeOfInheritance;
        diseaseId = builder.diseaseId;
        hpoIds = builder.hpoIds;
        seedGeneList = builder.seedGeneList;

        //OUTPUT options
        numberOfGenesToShow = builder.numberOfGenesToShow;
        outFileName = builder.outFileName;
        outputFormat = builder.outputFormat;

        diseaseGeneFamilyName = builder.diseaseGeneFamilyName;
    }

    public Path getVcfPath() {
        return vcfFilePath;
    }

    public Path getPedPath() {
        return pedFilePath;
    }

    public Priority getPrioritiser() {
        return prioritiser;
    }

    public Class<? extends Priority> getPrioritiserClass() {
        return prioritiserClass;
    }

    public float getMaximumFrequency() {
        return maximumFrequency;
    }

    public float getMinimumQuality() {
        return minimumQuality;
    }

    public String getGeneticInterval() {
        return geneticInterval;
    }

    public boolean includePathogenic() {
        return includePathogenic;
    }

    public boolean removeDbSnp() {
        return removeDbSnp;
    }

    public boolean removeOffTargetVariants() {
        return removeOffTargetVariants;
    }

    public String getCandidateGene() {
        return candidateGene;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public List<String> getHpoIds() {
        return hpoIds;
    }

    public List<String> getSeedGeneList() {
        return seedGeneList;
    }

    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public String toString() {
        return "ExomiserOptions{" + "vcfFilePath=" + vcfFilePath + ", pedFilePath=" + pedFilePath + ", prioritiser=" + prioritiser + ", prioritiserClass=" + prioritiserClass + ", maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", includePathogenic=" + includePathogenic + ", removeDbSnp=" + removeDbSnp + ", removeOffTargetVariants=" + removeOffTargetVariants + ", candidateGene=" + candidateGene + ", modeOfInheritance=" + modeOfInheritance + ", diseaseId=" + diseaseId + ", hpoIds=" + hpoIds + ", seedGeneList=" + seedGeneList + ", numberOfGenesToShow=" + numberOfGenesToShow + ", outFileName=" + outFileName + ", outputFormat=" + outputFormat + ", diseaseGeneFamilyName=" + diseaseGeneFamilyName + '}';
    }

    
}
