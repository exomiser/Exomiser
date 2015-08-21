/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.AnalysisParser.AnalysisFileNotFoundException;
import de.charite.compbio.exomiser.core.AnalysisParser.AnalysisParserException;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriority;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.writers.OutputSettings;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisParserTest {

    private AnalysisParser instance;
    private PriorityFactory priorityFactory;

    private SampleData sampleData;
    private List<AnalysisStep> analysisSteps;

    private List<String> hpoIds;

    @Before
    public void setUp() {
        priorityFactory = new NoneTypePriorityFactoryStub();
        instance = new AnalysisParser(priorityFactory);

        sampleData = new SampleData();
        sampleData.setVcfPath(Paths.get("test.vcf"));
        sampleData.setPedPath(null);

        analysisSteps = new ArrayList<>();
        hpoIds = new ArrayList<>(Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055"));
    }

    private static String addStepToAnalysis(String step) {
        return String.format("analysis:\n"
                + "    vcf: test.vcf\n"
                + "    ped:\n"
                + "    modeOfInheritance: AUTOSOMAL_DOMINANT\n"
                + "    hpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']\n"
                + "    analysisMode: PASS_ONLY \n"
                + "    geneScoreMode: RAW_SCORE\n"
                + "    steps: ["
                + "        %s\n"
                + "]", step);
    }

    @Test
    public void testParseAnalysisSteps_NoSteps() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis(""));
        System.out.println(analysis);
        assertThat(analysis.getVcfPath(), equalTo(Paths.get("test.vcf")));
        assertThat(analysis.getPedPath(), nullValue());
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(analysis.getScoringMode(), equalTo(ScoringMode.RAW_SCORE));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test(expected = AnalysisParserException.class)
    public void throwsExceptionWhenNoVcfIsSet() {
        instance.parseAnalysis(
                "analysis:\n"
                + "    vcf: \n");
    }

    @Test
    public void testParseAnalysis_FullAnalysisMode() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    analysisMode: FULL \n"
                + "    ");
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testParseAnalysisStep_UnsupportedFilterAddsNothingToAnalysisSteps() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("wibbleFilter: {}"));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test
    public void testParseAnalysisStep_IntervalFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("intervalFilter: {interval: 'chr10:122892600-122892700'}"));
        analysisSteps.add(new IntervalFilter(new GeneticInterval(10, 122892600, 122892700)));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_GeneIdFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("geneIdFilter: {geneIds: [12345, 34567, 98765]}"));
        analysisSteps.add(new EntrezGeneIdFilter(new LinkedHashSet<>(Arrays.asList(12345, 34567, 98765))));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_QualityFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("qualityFilter: {minQuality: 50.0}"));
        analysisSteps.add(new QualityFilter(50.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_VariantEffectFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("variantEffectFilter: {remove: [SYNONYMOUS_VARIANT, INTERGENIC_VARIANT]}"));
        analysisSteps.add(new VariantEffectFilter(EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT, VariantEffect.INTERGENIC_VARIANT)));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test(expected = AnalysisParserException.class)
    public void testParseAnalysisStep_VariantEffectFilter_illegalVariantEffect() {
        instance.parseAnalysis(addStepToAnalysis("variantEffectFilter: {remove: [WIBBLE]}"));
    }

    @Test
    public void testParseAnalysisStep_KnownVariantFilterFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("knownVariantFilter: {}"));
        analysisSteps.add(new KnownVariantFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_FrequencyFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("frequencyFilter: {maxFrequency: 1.0, frequencySources: [THOUSAND_GENOMES, ESP_AFRICAN_AMERICAN]}"));
        analysisSteps.add(new FrequencyFilter(1.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_PathogenicityFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("pathogenicityFilter: {keepNonPathogenic: false, pathogenicitySources: [SIFT, POLYPHEN, CADD]}"));
        analysisSteps.add(new PathogenicityFilter(false));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_PriorityScoreFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.65}"));
        analysisSteps.add(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.65f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_InheritanceFilterUndefinedMode() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    ped:\n"
                + "    modeOfInheritance: UNINITIALIZED\n"
                + "    hpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']\n"
                + "    analysisMode: PASS_ONLY \n"
                + "    geneScoreMode: RAW_SCORE\n"
                + "    steps: ["
                + "        inheritanceFilter: {}\n"
                + "]");
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_InheritanceFilterDefinedMode() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("inheritanceFilter: {}"));
        analysisSteps.add(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_OmimPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("omimPrioritiser: {}"));
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_HiPhivePrioritiserWithDefaultOptions() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("hiPhivePrioritiser: {}"));
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions()));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_HiPhivePrioritiserWithUserDefinedOptions() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("hiPhivePrioritiser: {diseaseId: 'OMIM:101600', candidateGeneSymbol: FGFR2, runParams: 'human,mouse,fish,ppi'}"));
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions("OMIM:101600", "FGFR2", "human,mouse,fish,ppi")));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_PhivePrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("phivePrioritiser: {}"));
        analysisSteps.add(priorityFactory.makePhivePrioritiser(hpoIds));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_PhenixPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("phenixPrioritiser: {}"));
        analysisSteps.add(priorityFactory.makePhenixPrioritiser(hpoIds));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_WalkerPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}"));
        analysisSteps.add(priorityFactory.makeExomeWalkerPrioritiser(new ArrayList<>(Arrays.asList(11111, 22222, 33333))));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParsePath() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;

        Analysis analysis = instance.parseAnalysis(Paths.get("src/test/resources/analysisExample.yml"));
        System.out.println(analysis);
        assertThat(analysis.getVcfPath(), equalTo(Paths.get("test.vcf")));
        assertThat(analysis.getPedPath(), nullValue());
        assertThat(analysis.getModeOfInheritance(), equalTo(modeOfInheritance));
        assertThat(analysis.getScoringMode(), equalTo(ScoringMode.RAW_SCORE));
        analysisSteps.add(new IntervalFilter(new GeneticInterval(10, 123256200, 123256300)));
        analysisSteps.add(new EntrezGeneIdFilter(new LinkedHashSet<>(Arrays.asList(12345, 34567, 98765))));
        analysisSteps.add(new QualityFilter(50.0f));
        analysisSteps.add(new VariantEffectFilter(EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT)));
        analysisSteps.add(new KnownVariantFilter());
        analysisSteps.add(new FrequencyFilter(1.0f));
        analysisSteps.add(new PathogenicityFilter(false));
        analysisSteps.add(new InheritanceFilter(modeOfInheritance));
        analysisSteps.add(new OMIMPriority());
        analysisSteps.add(new HiPhivePriority(hpoIds, new HiPhiveOptions(), null));
        analysisSteps.add(new HiPhivePriority(hpoIds, new HiPhiveOptions("OMIM:101600", "FGFR2", "mouse,fish,human,ppi"), null));
        analysisSteps.add(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.7f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test(expected = AnalysisFileNotFoundException.class)
    public void testParseAnalysis_NonExistentFile() {
        instance.parseAnalysis(Paths.get("src/test/resources/wibble"));
    }

    @Test(expected = AnalysisFileNotFoundException.class)
    public void testParseOutputSettings_NonExistentFile() {
        instance.parseOutputSettings(Paths.get("src/test/resources/wibble"));
    }

    @Test(expected = AnalysisParserException.class)
    public void testParseOutputSettings_OutputPassVariantsOnlyThrowsExceptionWithNoValue() {
        instance.parseOutputSettings(
                "outputOptions:\n"
                + "    outputPassVariantsOnly: ");
    }

    @Test
    public void testParseOutputSettings_OutputPassVariantsOnly() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                + "    outputPassVariantsOnly: true\n"
                + "    numGenes: 1\n"
                + "    outputPrefix: results/Pfeiffer-hiphive\n"
                + "    outputFormats: [HTML, TSV-GENE, TSV-VARIANT, VCF]\n");
        assertThat(outputSettings.outputPassVariantsOnly(), is(true));
    }

    @Test
    public void testParseOutputSettings_NumGenesToShow() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                 "outputOptions:\n"
                + "    outputPassVariantsOnly: true\n"
                + "    numGenes: 1\n"
                + "    outputPrefix: results/Pfeiffer-hiphive\n"
                + "    outputFormats: [HTML, TSV-GENE, TSV-VARIANT, VCF]\n");
        assertThat(outputSettings.getNumberOfGenesToShow(), equalTo(1));
    }

    @Test
    public void testParseOutputSettings_OutputPrefix() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                + "    outputPassVariantsOnly: true\n"
                + "    numGenes: 1\n"
                + "    outputPrefix: results/Pfeiffer-hiphive\n"
                + "    outputFormats: [HTML, TSV-GENE, TSV-VARIANT, VCF]\n");
        assertThat(outputSettings.getOutputPrefix(), equalTo("results/Pfeiffer-hiphive"));
    }
    
    @Test
    public void testParseOutputSettings_OutputFormats() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                + "    outputPassVariantsOnly: true\n"
                + "    numGenes: 1\n"
                + "    outputPrefix: results/Pfeiffer-hiphive\n"
                + "    outputFormats: [HTML, TSV-GENE, TSV-VARIANT, VCF]\n");
        Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF);
        assertThat(outputSettings.getOutputFormats(), equalTo((outputFormats)));
    }

    @Test
    public void testParseOutputSettings() {
        OutputSettings outputSettings = instance.parseOutputSettings(Paths.get("src/test/resources/analysisExample.yml"));
        OutputSettings expected = new OutputSettingsImp.OutputSettingsBuilder()
                .outputPassVariantsOnly(false)
                .numberOfGenesToShow(0)
                .outputPrefix("results/Pfeiffer-hiphive")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF, OutputFormat.HTML))
                .build();
        assertThat(outputSettings, equalTo(expected));
    }
}
