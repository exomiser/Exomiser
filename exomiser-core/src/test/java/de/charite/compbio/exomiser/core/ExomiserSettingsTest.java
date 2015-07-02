/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link de.charite.compbio.exomiser.core.ExomiserSettings}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserSettingsTest {

    SettingsBuilder instance;

    //
    private static final String BUILD_VERSION_DEFAULT = "";
    private static final String BUILD_VERSION = "2.1.0";
    private static final String BUILD_TIMESTAMP_DEFAULT = "";
    private static final String BUILD_TIMESTAMP = "20140704-1556";

    //input settings
    private static final Path VCF_PATH_NOT_SET = null;
    private static final Path VCF_PATH = Paths.get("data/test.vcf");
    private static final Path PED_PATH_NOT_SET = null;
    private static final Path PED_PATH = Paths.get("data/test.ped");
    
    //filter settings
    private static final boolean RUN_FULL_ANALYSIS_DEFAULT = false;
    private static final boolean RUN_FULL_ANALYSIS = true;
    private static final float MAXIMUM_FREQUENCY_DEFAULT = 100.0f;
    private static final float MAXIMUM_FREQUENCY = 42.24f;
    private static final float MIMIMUM_QUALITY_DEFAULT = 0.0f;
    private static final float MIMIMUM_QUALITY = 666.24f;
    private static final GeneticInterval GENETIC_INTERVAL_DEFAULT = null;
    private static final GeneticInterval GENETIC_INTERVAL = new GeneticInterval((byte) 2, 12345, 67890);
    private static final boolean REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT = false;
    private static final boolean REMOVE_PATHOGENIC_FILTER_CUTOFF = true;
    private static final boolean REMOVE_KNOWN_VARIANTS_DEFAULT = false;
    private static final boolean REMOVE_KNOWN_VARIANTS = true;
    private static final boolean KEEP_OFF_TARGET_VARIANTS_DEFAULT = false;
    private static final boolean KEEP_OFF_TARGET_VARIANTS = true;
    private static final String CANDIDATE_GENE_NAME_DEFAULT = "";
    private static final String CANDIDATE_GENE_NAME = "ADH1";
    private static final ModeOfInheritance MODE_OF_INHERITANCE = ModeOfInheritance.AUTOSOMAL_DOMINANT;
    private static final ModeOfInheritance MODE_OF_INHERITANCE_DEFAULT = ModeOfInheritance.UNINITIALIZED;
    
    //prioritiser settings
    private static final PriorityType PRIORITISER_DEFAULT = PriorityType.NONE;
    private static final String DISEASE_STRING_DEFAULT = "";
    private static final String DISEASE_STRING = "OMIM:100100";
    private static final List<String> HPO_LIST_DEFAULT = new ArrayList<>();
    private static final List<String> HPO_LIST = new ArrayList<>(Arrays.asList("HPO:123456"));
    private static final List<Integer> SEED_GENE_LIST_DEFAULT = new ArrayList<>();
    private static final List<Integer> SEED_GENE_LIST = new ArrayList<>(Arrays.asList(1, 23, 56));
    
    //output settings
    private static final boolean OUTPUT_PASS_VARIANTS_ONLY_DEFAULT = false;
    private static final boolean OUTPUT_PASS_VARIANTS_ONLY = true;
    private static final int NUMBER_OF_GENES_TO_SHOW_DEFAULT = 0;
    private static final int NUMBER_OF_GENES_TO_SHOW = 12438803;    
    private static final String OUTPUT_PREFIX_DEFAULT = "";
    private static final String OUTPUT_PREFIX_DEFAULT_WHEN_VCF_SET = "results/test.vcf-exomiser-results";
    private static final String OUTPUT_PREFIX_DEFAULT_WHEN_VCF_AND_BUILD_VERSION_SET = "results/test.vcf-exomiser-" + BUILD_VERSION + "-results";
    private static final String OUTPUT_PREFIX_NAME = "wibbler";
    private static final Set<OutputFormat> OUTPUT_FORMAT_DEFAULT = EnumSet.of(OutputFormat.HTML);
    private static final Set<OutputFormat> OUTPUT_FORMAT = EnumSet.of(OutputFormat.TSV_GENE);
    
    public ExomiserSettingsTest() {
    }

    @Before
    public void setUp() {
        instance = new SettingsBuilder();
        instance.vcfFilePath(VCF_PATH);
    }

    @Test
    public void testThatTheBuilderProducesDefaultExomiserSettingsObject() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings, instanceOf(ExomiserSettings.class));
        System.out.println(settings);
        assertThat(settings.getVcfPath(), equalTo(VCF_PATH_NOT_SET));
        assertThat(settings.getPedPath(), equalTo(PED_PATH_NOT_SET));
        assertThat(settings.getPrioritiserType(), equalTo(PRIORITISER_DEFAULT));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY_DEFAULT));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
        assertThat(settings.removeKnownVariants(), is(REMOVE_KNOWN_VARIANTS_DEFAULT));
        assertThat(settings.keepOffTargetVariants(), is(KEEP_OFF_TARGET_VARIANTS_DEFAULT));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST_DEFAULT));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST_DEFAULT));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_DEFAULT));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT_DEFAULT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));

    }

    @Test
    public void testBuildVersionDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getBuildVersion(), equalTo(BUILD_VERSION_DEFAULT));
    }
    
    @Test
    public void testThatBuildVersionCanBeSet() {
        instance.buildVersion(BUILD_VERSION);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getBuildVersion(), equalTo(BUILD_VERSION));
    }
    
    @Test
    public void testBuildTimestampDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getBuildTimestamp(), equalTo(BUILD_TIMESTAMP_DEFAULT));
    }
    
    @Test
    public void testThatBuildTimestampCanBeSet() {
        instance.buildTimestamp(BUILD_TIMESTAMP);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getBuildTimestamp(), equalTo(BUILD_TIMESTAMP));
    }
    
    /**
     * Test of getVcfPath method, of class ExomiserSettings.
     */
    @Test
    public void testThatGetVcfPathReturnsAPath() {

        instance.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = instance.build();

        assertThat(settings.getVcfPath(), equalTo(VCF_PATH));
    }

    /**
     * Test of getVcfPath method, of class ExomiserSettings.
     */
    @Test
    public void testThatTheDefaultVcfPathIsNull() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.getVcfPath(), nullValue());
    }

    @Test
    public void testThatTheDefaultSettingsIsNotValid() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.isValid(), is(false));
    }

    @Test
    public void testThatJustSettingAFcvFileIsValid() {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = settingsBuilder.build();
        assertThat(settings.isValid(), is(true));
    }

    @Test
    public void testThatTheMinimumRequiredValidSettingsIsAFcvFileAndPrioritiser() {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.vcfFilePath(VCF_PATH);
        settingsBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
        ExomiserSettings settings = settingsBuilder.build();
        assertThat(settings.isValid(), is(true));
    }

    /**
     * Test of getPedPath method, of class ExomiserSettings.
     */
    @Test
    public void testThatGetPedPathReturnsAPath() {

        instance.pedFilePath(PED_PATH);
        ExomiserSettings settings = instance.build();

        assertThat(settings.getPedPath(), equalTo(PED_PATH));
    }

    /**
     * Test of getPrioritiserType method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesPriorityTypeNoneAsDefault() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.NONE));
    }

    /**
     * Test of getPrioritiserType method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesTheSpecifiedPriorityType() {
        instance.usePrioritiser(PriorityType.OMIM_PRIORITY);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    /**
     * Test of getMaximumFrequency method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesMaximumFrequencyDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY_DEFAULT));
    }

    /**
     * Test of getMaximumFrequency method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesMaximumFrequencySpecified() {
        instance.maximumFrequency(MAXIMUM_FREQUENCY);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
    }

    /**
     * Test of getMinimumQuality method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultMinimumQuality() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesMinimumQualitySpecified() {
        instance.minimumQuality(MIMIMUM_QUALITY);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY));
    }

    /**
     * Test of getGeneticInterval method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesGeneticIntervalDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesGeneticIntervalSpecified() {
        instance.geneticInterval(GENETIC_INTERVAL);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL));
    }

    /**
     * Test of removePathFilterCutOff method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesIncludePathogenicDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesIncludePathogenicWhenSet() {
        instance.removePathFilterCutOff(REMOVE_PATHOGENIC_FILTER_CUTOFF);
        ExomiserSettings settings = instance.build();
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF));
    }

    @Test
    public void testThatBuilderProducesRemoveKnownVariantsDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.removeKnownVariants(), is(REMOVE_KNOWN_VARIANTS_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesRemoveKnownVariantsWhenSet() {
        instance.removeKnownVariants(REMOVE_KNOWN_VARIANTS);
        ExomiserSettings settings = instance.build();
        assertThat(settings.removeKnownVariants(), is(REMOVE_KNOWN_VARIANTS));
    }

    /**
     * Test of keepOffTargetVariants method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesRemoveOffTargetVariantsDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.keepOffTargetVariants(), is(KEEP_OFF_TARGET_VARIANTS_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesRemoveOffTargetVariantsWhenSet() {
        instance.keepOffTargetVariants(KEEP_OFF_TARGET_VARIANTS);
        ExomiserSettings settings = instance.build();
        assertThat(settings.keepOffTargetVariants(), is(KEEP_OFF_TARGET_VARIANTS));
    }

    /**
     * Test of getCandidateGene method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesCandidateGeneDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesCandidateGeneWhenSet() {
        instance.candidateGene(CANDIDATE_GENE_NAME);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME));
    }

    /**
     * Test of getModeOfInheritance method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesGetModeOfInheritanceDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesGetModeOfInheritanceWhenSet() {
        instance.modeOfInheritance(MODE_OF_INHERITANCE);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE));
    }

    /**
     * Test of getDiseaseId method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptyDiseaseId() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesDiseaseIdWhenSet() {
        instance.diseaseId(DISEASE_STRING);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING));
    }
    
    /**
     * Test of getHpoIds method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptyHpoIds() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesHpoIdsWhenSet() {
        instance.hpoIdList(HPO_LIST);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
    }

    /**
     * Test of getSeedGeneList method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptySeedGeneList() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSeedGeneListWhenSet() {
        instance.seedGeneList(SEED_GENE_LIST);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
    }

    @Test
    public void testThatBuilderProducesDefaultOutputPassVariantsOption() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.outputPassVariantsOnly(), equalTo(OUTPUT_PASS_VARIANTS_ONLY_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesOutputPassVariantsOptionWhenSet() {
        instance.outputPassVariantsOnly(OUTPUT_PASS_VARIANTS_ONLY);
        ExomiserSettings settings = instance.build();
        assertThat(settings.outputPassVariantsOnly(), equalTo(OUTPUT_PASS_VARIANTS_ONLY));
    }

    /**
     * Test of getNumberOfGenesToShow method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultNumberOfGenesToShow() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSetNumberOfGenesToShow() {
        instance.numberOfGenesToShow(NUMBER_OF_GENES_TO_SHOW);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW));
    }

    /**
     * Test of getOutputPrefix method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultOutFileName() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesDefaultOutFileNameBasedOnInputVcfFileName() {
        instance.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_DEFAULT_WHEN_VCF_SET));
    }
    
    @Test
    public void testThatBuilderProducesDefaultOutFileNameBasedOnInputVcfFileNameAndBuildVersion() {
        instance.vcfFilePath(VCF_PATH);
        instance.buildVersion(BUILD_VERSION);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_DEFAULT_WHEN_VCF_AND_BUILD_VERSION_SET));
    }

    @Test
    public void testThatBuilderProducesSetOutFileName() {
        instance.outputPrefix(OUTPUT_PREFIX_NAME);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_NAME));
    }

    /**
     * Test of getOutputFormats method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultOutputFormat() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSetOutputFormat() {
        instance.outputFormats(OUTPUT_FORMAT);
        ExomiserSettings settings = instance.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
    }
    
    @Test
    public void testThatBuilderProducesRunFullAnalysisDefault() {
        ExomiserSettings settings = instance.build();
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));
    }
    
    @Test
    public void testThatBuilderProducesRunFullAnalysisWhenDefined() {
        instance.runFullAnalysis(RUN_FULL_ANALYSIS);
        ExomiserSettings settings = instance.build();
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS));
    }

    @Test
    public void testThatBuilderCanSetAllValues() {

        instance.vcfFilePath(VCF_PATH)
                .pedFilePath(PED_PATH)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .maximumFrequency(MAXIMUM_FREQUENCY)
                .minimumQuality(MIMIMUM_QUALITY)
                .geneticInterval(GENETIC_INTERVAL)
                .removePathFilterCutOff(REMOVE_PATHOGENIC_FILTER_CUTOFF)
                .removeKnownVariants(REMOVE_KNOWN_VARIANTS)
                .keepOffTargetVariants(KEEP_OFF_TARGET_VARIANTS)
                .candidateGene(CANDIDATE_GENE_NAME)
                .modeOfInheritance(MODE_OF_INHERITANCE)
                .diseaseId(DISEASE_STRING)
                .hpoIdList(HPO_LIST)
                .seedGeneList(SEED_GENE_LIST)
                .outputPassVariantsOnly(OUTPUT_PASS_VARIANTS_ONLY)
                .numberOfGenesToShow(NUMBER_OF_GENES_TO_SHOW)
                .outputPrefix(OUTPUT_PREFIX_NAME)
                .outputFormats(OUTPUT_FORMAT)
                .runFullAnalysis(RUN_FULL_ANALYSIS);

        ExomiserSettings settings = instance.build();

        assertThat(settings.getVcfPath(), equalTo(VCF_PATH));
        assertThat(settings.getPedPath(), equalTo(PED_PATH));
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF));
        assertThat(settings.removeKnownVariants(), is(REMOVE_KNOWN_VARIANTS));
        assertThat(settings.keepOffTargetVariants(), is(KEEP_OFF_TARGET_VARIANTS));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
        assertThat(settings.outputPassVariantsOnly(), equalTo(OUTPUT_PASS_VARIANTS_ONLY));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW));
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_NAME));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS));
        assertThat(settings.isValid(), is(true));
    }

    @Test
    public void testThatBuilderCanSetSomeValuesAndOthersRemainAsDefault() {

        instance.vcfFilePath(VCF_PATH)
                .pedFilePath(PED_PATH)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .maximumFrequency(MAXIMUM_FREQUENCY)
                .hpoIdList(HPO_LIST)
                .seedGeneList(SEED_GENE_LIST)
                .outputPrefix(OUTPUT_PREFIX_NAME)
                .outputFormats(OUTPUT_FORMAT);

        ExomiserSettings settings = instance.build();

        assertThat(settings.getVcfPath(), equalTo(VCF_PATH));
        assertThat(settings.getPedPath(), equalTo(PED_PATH));
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
        assertThat(settings.removeKnownVariants(), is(REMOVE_KNOWN_VARIANTS_DEFAULT));
        assertThat(settings.keepOffTargetVariants(), is(KEEP_OFF_TARGET_VARIANTS_DEFAULT));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
        assertThat(settings.outputPassVariantsOnly(), equalTo(OUTPUT_PASS_VARIANTS_ONLY_DEFAULT));
        assertThat(settings.getOutputPrefix(), equalTo(OUTPUT_PREFIX_NAME));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));
        assertThat(settings.isValid(), is(true));

    }

    @Test
    public void testJsonWrite() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        ExomiserSettings defaultSettings = instance.build();
        try {
            String jsonString = mapper.writeValueAsString(defaultSettings);
            System.out.println(jsonString);
        } catch (JsonProcessingException ex) {
            System.out.println(ex);
        }
    }

    @Test
    public void testJsonRead() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        String jsonString = "{\"prioritiser\":\"phenodigm-mgi\",\"max-freq\":0.1,\"min-qual\":0.0,\"restrict-interval\":\"\",\"include-pathogenic\":false,\"remove-dbsnp\":false,\"remove-off-target-syn\":true,\"candidate-gene\":\"FGFR2\",\"inheritance-mode\":\"AUTOSOMAL_DOMINANT\",\"disease-id\":\"\",\"hpo-ids\":[\"HP:0987654\",\"HP:1234567\"],\"seed-genes\":[123,4567],\"num-genes\":0,\"out-file\":\"\",\"out-format\":\"HTML\",\"vcf\":\"/src/test/resources/Pfeiffer.vcf\",\"ped\":null}";
        try {
            ExomiserSettings defaultSettings = mapper.readValue(jsonString, ExomiserSettings.class);
            System.out.println(defaultSettings);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    /* Ignoring this for now as the order depends on whether Exomiser or Genomiser
     * are being run. Eventually with Analysis/yaml object should be user-specified
     * and can probably reintroduce this test
     */
    
    @Ignore
    @Test
    public void testDetermineFilterTypesToRunOnDefaultSettings() {
        //make a new default Settings object
        ExomiserSettings settings = instance.build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        
        List<FilterType> result = settings.getFilterTypesToRun();
        
        assertThat(result, equalTo(expResult));
    }
    
    /* Ignoring this for now as the order depends on whether Exomiser or Genomiser
     * are being run. Eventually with Analysis/yaml object should be user-specified
     * and can probably reintroduce this test
     */
    
    @Ignore
    @Test
    public void testDetermineFilterTypesToRun() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        final GeneticInterval interval = new GeneticInterval(2, 12345, 67890);
        ExomiserSettings settings = instance.removePathFilterCutOff(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval).build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.QUALITY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        expResult.add(FilterType.INTERVAL_FILTER);
        
        List<FilterType> result = settings.getFilterTypesToRun();
        
        assertThat(result, equalTo(expResult));
    }
}
