/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import jannovar.common.ModeOfInheritance;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link de.charite.compbio.exomiser.core.ExomiserSettings}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserSettingsTest {

    SettingsBuilder vcfPathAndPrioritiserSetBuilder;

    //
    private static final String BUILD_VERSION_DEFAULT = "";
    private static final String BUILD_VERSION = "2.1.0";
    private static final String BUILD_TIMESTAMP_DEFAULT = "";
    private static final String BUILD_TIMESTAMP = "20140704-1556";

    private static final Path VCF_PATH_NOT_SET = null;
    private static final Path VCF_PATH = Paths.get("data/test.vcf");
    private static final Path PED_PATH_NOT_SET = null;
    private static final Path PED_PATH = Paths.get("data/test.ped");
    private static final float MAXIMUM_FREQUENCY_DEFAULT = 100.0f;
    private static final float MAXIMUM_FREQUENCY = 42.24f;
    private static final float MIMIMUM_QUALITY_DEFAULT = 0.0f;
    private static final float MIMIMUM_QUALITY = 666.24f;
    private static final GeneticInterval GENETIC_INTERVAL_DEFAULT = null;
    private static final GeneticInterval GENETIC_INTERVAL = new GeneticInterval((byte) 2, 12345, 67890);
    private static final boolean REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT = false;
    private static final boolean REMOVE_PATHOGENIC_FILTER_CUTOFF = true;
    private static final boolean REMOVE_DBSNP_DEFAULT = false;
    private static final boolean REMOVE_DBSNP = true;
    private static final boolean REMOVE_OFF_TARGET_VARIANTS_DEFAULT = true;
    private static final boolean REMOVE_OFF_TARGET_VARIANTS = false;
    private static final String CANDIDATE_GENE_NAME_DEFAULT = "";
    private static final String CANDIDATE_GENE_NAME = "ADH1";
    private static final ModeOfInheritance MODE_OF_INHERITANCE = ModeOfInheritance.AUTOSOMAL_DOMINANT;
    private static final ModeOfInheritance MODE_OF_INHERITANCE_DEFAULT = ModeOfInheritance.UNINITIALIZED;
    private static final String DISEASE_STRING_DEFAULT = "";
    private static final String DISEASE_STRING = "OMIM:100100";
    private static final List<String> HPO_LIST_DEFAULT = new ArrayList<>();
    private static final List<String> HPO_LIST = new ArrayList<>(Arrays.asList("HPO:123456"));
    private static final List<Integer> SEED_GENE_LIST_DEFAULT = new ArrayList<>();
    private static final List<Integer> SEED_GENE_LIST = new ArrayList<>(Arrays.asList(1, 23, 56));
    private static final int NUMBER_OF_GENES_TO_SHOW_DEFAULT = 0;
    private static final int NUMBER_OF_GENES_TO_SHOW = 12438803;
    private static final String OUT_FILE_NAME_DEFAULT = "";
    private static final String OUT_FILE_NAME_DEFAULT_WHEN_VCF_SET = "results/test-exomiser-results";
    private static final String OUT_FILE_NAME_DEFAULT_WHEN_VCF_AND_BUILD_VERSION_SET = "results/test-exomiser-" + BUILD_VERSION + "-results";
    private static final String OUT_FILE_NAME = "wibbler";
    private static final Set<OutputFormat> OUTPUT_FORMAT_DEFAULT = EnumSet.of(OutputFormat.HTML);
    private static final Set<OutputFormat> OUTPUT_FORMAT = EnumSet.of(OutputFormat.TSV_GENE);
    private static final boolean RUN_FULL_ANALYSIS_DEFAULT = false;
    private static final boolean RUN_FULL_ANALYSIS = true;
    
    public ExomiserSettingsTest() {
    }

    @Before
    public void setUp() {
        vcfPathAndPrioritiserSetBuilder = new SettingsBuilder();
        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH);
        vcfPathAndPrioritiserSetBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
    }

    @Test
    public void testThatTheBuilderProducesDefaultExomiserSettingsObject() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings, instanceOf(ExomiserSettings.class));
        System.out.println(settings);
        assertThat(settings.getVcfPath(), equalTo(VCF_PATH_NOT_SET));
        assertThat(settings.getPedPath(), equalTo(PED_PATH_NOT_SET));
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.NOT_SET));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY_DEFAULT));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
        assertThat(settings.removeDbSnp(), is(REMOVE_DBSNP_DEFAULT));
        assertThat(settings.removeOffTargetVariants(), is(REMOVE_OFF_TARGET_VARIANTS_DEFAULT));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST_DEFAULT));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST_DEFAULT));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME_DEFAULT));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT_DEFAULT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));

    }

    @Test
    public void testBuildVersionDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getBuildVersion(), equalTo(BUILD_VERSION_DEFAULT));
    }
    
    @Test
    public void testThatBuildVersionCanBeSet() {
        vcfPathAndPrioritiserSetBuilder.buildVersion(BUILD_VERSION);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getBuildVersion(), equalTo(BUILD_VERSION));
    }
    
    @Test
    public void testBuildTimestampDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getBuildTimestamp(), equalTo(BUILD_TIMESTAMP_DEFAULT));
    }
    
    @Test
    public void testThatBuildTimestampCanBeSet() {
        vcfPathAndPrioritiserSetBuilder.buildTimestamp(BUILD_TIMESTAMP);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getBuildTimestamp(), equalTo(BUILD_TIMESTAMP));
    }
    
    /**
     * Test of getVcfPath method, of class ExomiserSettings.
     */
    @Test
    public void testThatGetVcfPathReturnsAPath() {

        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();

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
    public void testThatJustSettingAFcvFileIsNotValid() {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = settingsBuilder.build();
        assertThat(settings.isValid(), is(false));
    }

    @Test
    public void testThatJustSettingAPrioritiserIsNotValid() {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
        ExomiserSettings settings = settingsBuilder.build();
        assertThat(settings.isValid(), is(false));
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

        vcfPathAndPrioritiserSetBuilder.pedFilePath(PED_PATH);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();

        assertThat(settings.getPedPath(), equalTo(PED_PATH));
    }

    /**
     * Test of getPrioritiserType method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesAnUndefinedPriorityTypeAsDefault() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.NOT_SET));
    }

    /**
     * Test of getPrioritiserType method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesTheSpecifiedPriorityType() {
        vcfPathAndPrioritiserSetBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    /**
     * Test of getMaximumFrequency method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesMaximumFrequencyDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY_DEFAULT));
    }

    /**
     * Test of getMaximumFrequency method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesMaximumFrequencySpecified() {
        vcfPathAndPrioritiserSetBuilder.maximumFrequency(MAXIMUM_FREQUENCY);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
    }

    /**
     * Test of getMinimumQuality method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultMinimumQuality() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesMinimumQualitySpecified() {
        vcfPathAndPrioritiserSetBuilder.minimumQuality(MIMIMUM_QUALITY);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY));
    }

    /**
     * Test of getGeneticInterval method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesGeneticIntervalDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesGeneticIntervalSpecified() {
        vcfPathAndPrioritiserSetBuilder.geneticInterval(GENETIC_INTERVAL);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL));
    }

    /**
     * Test of removePathFilterCutOff method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesIncludePathogenicDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesIncludePathogenicWhenSet() {
        vcfPathAndPrioritiserSetBuilder.removePathFilterCutOff(REMOVE_PATHOGENIC_FILTER_CUTOFF);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF));
    }

    /**
     * Test of removeDbSnp method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesRemoveDbSnpDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removeDbSnp(), is(REMOVE_DBSNP_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesRemoveDbSnpWhenSet() {
        vcfPathAndPrioritiserSetBuilder.removeDbSnp(REMOVE_DBSNP);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removeDbSnp(), is(REMOVE_DBSNP));
    }

    /**
     * Test of removeOffTargetVariants method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesRemoveOffTargetVariantsDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removeOffTargetVariants(), is(REMOVE_OFF_TARGET_VARIANTS_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesRemoveOffTargetVariantsWhenSet() {
        vcfPathAndPrioritiserSetBuilder.removeOffTargetVariants(REMOVE_OFF_TARGET_VARIANTS);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.removeOffTargetVariants(), is(REMOVE_OFF_TARGET_VARIANTS));
    }

    /**
     * Test of getCandidateGene method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesCandidateGeneDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesCandidateGeneWhenSet() {
        vcfPathAndPrioritiserSetBuilder.candidateGene(CANDIDATE_GENE_NAME);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME));
    }

    /**
     * Test of getModeOfInheritance method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesGetModeOfInheritanceDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesGetModeOfInheritanceWhenSet() {
        vcfPathAndPrioritiserSetBuilder.modeOfInheritance(MODE_OF_INHERITANCE);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE));
    }

    /**
     * Test of getDiseaseId method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptyDiseaseId() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesDiseaseIdWhenSet() {
        vcfPathAndPrioritiserSetBuilder.diseaseId(DISEASE_STRING);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING));
    }

    /**
     * Test of getHpoIds method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptyHpoIds() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesHpoIdsWhenSet() {
        vcfPathAndPrioritiserSetBuilder.hpoIdList(HPO_LIST);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
    }

    /**
     * Test of getSeedGeneList method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultEmptySeedGeneList() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSeedGeneListWhenSet() {
        vcfPathAndPrioritiserSetBuilder.seedGeneList(SEED_GENE_LIST);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
    }

    /**
     * Test of getNumberOfGenesToShow method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultNumberOfGenesToShow() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSetNumberOfGenesToShow() {
        vcfPathAndPrioritiserSetBuilder.numberOfGenesToShow(NUMBER_OF_GENES_TO_SHOW);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW));
    }

    /**
     * Test of getOutFileName method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultOutFileName() {
        ExomiserSettings settings = new SettingsBuilder().build();
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesDefaultOutFileNameBasedOnInputVcfFileName() {
        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME_DEFAULT_WHEN_VCF_SET));
    }
    
    @Test
    public void testThatBuilderProducesDefaultOutFileNameBasedOnInputVcfFileNameAndBuildVersion() {
        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH);
        vcfPathAndPrioritiserSetBuilder.buildVersion(BUILD_VERSION);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME_DEFAULT_WHEN_VCF_AND_BUILD_VERSION_SET));
    }

    @Test
    public void testThatBuilderProducesSetOutFileName() {
        vcfPathAndPrioritiserSetBuilder.outFileName(OUT_FILE_NAME);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME));
    }

    /**
     * Test of getOutputFormats method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultOutputFormat() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT_DEFAULT));
    }

    @Test
    public void testThatBuilderProducesSetOutputFormat() {
        vcfPathAndPrioritiserSetBuilder.outputFormats(OUTPUT_FORMAT);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
    }
    
    @Test
    public void testThatBuilderProducesRunFullAnalysisDefault() {
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));
    }
    
    @Test
    public void testThatBuilderProducesRunFullAnalysisWhenDefined() {
        vcfPathAndPrioritiserSetBuilder.runFullAnalysis(RUN_FULL_ANALYSIS);
        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS));
    }

    @Test
    public void testThatBuilderCanSetAllValues() {

        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH)
                .pedFilePath(PED_PATH)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .maximumFrequency(MAXIMUM_FREQUENCY)
                .minimumQuality(MIMIMUM_QUALITY)
                .geneticInterval(GENETIC_INTERVAL)
                .removePathFilterCutOff(REMOVE_PATHOGENIC_FILTER_CUTOFF)
                .removeDbSnp(REMOVE_DBSNP)
                .removeOffTargetVariants(REMOVE_OFF_TARGET_VARIANTS)
                .candidateGene(CANDIDATE_GENE_NAME)
                .modeOfInheritance(MODE_OF_INHERITANCE)
                .diseaseId(DISEASE_STRING)
                .hpoIdList(HPO_LIST)
                .seedGeneList(SEED_GENE_LIST)
                .numberOfGenesToShow(NUMBER_OF_GENES_TO_SHOW)
                .outFileName(OUT_FILE_NAME)
                .outputFormats(OUTPUT_FORMAT)
                .runFullAnalysis(RUN_FULL_ANALYSIS);

        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();

        assertThat(settings.getVcfPath(), equalTo(VCF_PATH));
        assertThat(settings.getPedPath(), equalTo(PED_PATH));
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF));
        assertThat(settings.removeDbSnp(), is(REMOVE_DBSNP));
        assertThat(settings.removeOffTargetVariants(), is(REMOVE_OFF_TARGET_VARIANTS));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW));
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS));
        assertThat(settings.isValid(), is(true));
    }

    @Test
    public void testThatBuilderCanSetSomeValuesAndOthersRemainAsDefault() {

        vcfPathAndPrioritiserSetBuilder.vcfFilePath(VCF_PATH)
                .pedFilePath(PED_PATH)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .maximumFrequency(MAXIMUM_FREQUENCY)
                .hpoIdList(HPO_LIST)
                .seedGeneList(SEED_GENE_LIST)
                .outFileName(OUT_FILE_NAME)
                .outputFormats(OUTPUT_FORMAT);

        ExomiserSettings settings = vcfPathAndPrioritiserSetBuilder.build();

        assertThat(settings.getVcfPath(), equalTo(VCF_PATH));
        assertThat(settings.getPedPath(), equalTo(PED_PATH));
        assertThat(settings.getPrioritiserType(), equalTo(PriorityType.OMIM_PRIORITY));
        assertThat(settings.getMaximumFrequency(), equalTo(MAXIMUM_FREQUENCY));
        assertThat(settings.getMinimumQuality(), equalTo(MIMIMUM_QUALITY_DEFAULT));
        assertThat(settings.getGeneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
        assertThat(settings.removePathFilterCutOff(), is(REMOVE_PATHOGENIC_FILTER_CUTOFF_DEFAULT));
        assertThat(settings.removeDbSnp(), is(REMOVE_DBSNP_DEFAULT));
        assertThat(settings.removeOffTargetVariants(), is(REMOVE_OFF_TARGET_VARIANTS_DEFAULT));
        assertThat(settings.getCandidateGene(), equalTo(CANDIDATE_GENE_NAME_DEFAULT));
        assertThat(settings.getModeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
        assertThat(settings.getDiseaseId(), equalTo(DISEASE_STRING_DEFAULT));
        assertThat(settings.getHpoIds(), equalTo(HPO_LIST));
        assertThat(settings.getSeedGeneList(), equalTo(SEED_GENE_LIST));
        assertThat(settings.getNumberOfGenesToShow(), equalTo(NUMBER_OF_GENES_TO_SHOW_DEFAULT));
        assertThat(settings.getOutFileName(), equalTo(OUT_FILE_NAME));
        assertThat(settings.getOutputFormats(), equalTo(OUTPUT_FORMAT));
        assertThat(settings.runFullAnalysis(), equalTo(RUN_FULL_ANALYSIS_DEFAULT));
        assertThat(settings.isValid(), is(true));

    }

    @Test
    public void testJsonWrite() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        ExomiserSettings defaultSettings = vcfPathAndPrioritiserSetBuilder.build();
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
}
