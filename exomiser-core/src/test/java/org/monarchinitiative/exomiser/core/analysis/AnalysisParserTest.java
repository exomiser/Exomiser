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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.AnalysisParser.AnalysisFileNotFoundException;
import org.monarchinitiative.exomiser.core.analysis.AnalysisParser.AnalysisParserException;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceStub;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;

import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisParserTest {

    private AnalysisParser instance;
    private PriorityFactory priorityFactory;

    private List<AnalysisStep> analysisSteps;

    private List<String> hpoIds;
    private Set<FrequencySource> frequencySources;
    private Set<PathogenicitySource> pathogenicitySources;

    @Before
    public void setUp() {
        priorityFactory = new NoneTypePriorityFactoryStub();
        instance = new AnalysisParser(priorityFactory, new VariantDataServiceStub());

        analysisSteps = new ArrayList<>();
        hpoIds = new ArrayList<>(Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055"));
        frequencySources = EnumSet.of(FrequencySource.THOUSAND_GENOMES, FrequencySource.ESP_AFRICAN_AMERICAN, FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN);
        pathogenicitySources = EnumSet.of(PathogenicitySource.SIFT, PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER);
    }

    private static String addStepToAnalysis(String step) {
        return String.format("analysis:\n"
                + "    vcf: test.vcf\n"
                + "    ped:\n"
                + "    modeOfInheritance: AUTOSOMAL_DOMINANT\n"
                + "    hpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']\n"
                + "    analysisMode: PASS_ONLY \n"
                + "    frequencySources: [THOUSAND_GENOMES, ESP_AFRICAN_AMERICAN, EXAC_AFRICAN_INC_AFRICAN_AMERICAN]\n"
                + "    pathogenicitySources: [SIFT, POLYPHEN, MUTATION_TASTER]\n"
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
        assertThat(analysis.getProbandSampleName(), equalTo(""));
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(analysis.getHpoIds(), equalTo(hpoIds));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test(expected = AnalysisParserException.class)
    public void throwsExceptionWhenNoVcfIsSet() {
        instance.parseAnalysis(
                "analysis:\n"
                + "    vcf: \n"
        );
    }

    @Test
    public void testParseAnalysisProbandSampleNameSpecified() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    proband: Bod \n"
                        + "    ");
        assertThat(analysis.getProbandSampleName(), equalTo("Bod"));
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
    public void testParseAnalysisModeOfInheritanceAutosomalDominant() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    modeOfInheritance: AUTOSOMAL_DOMINANT \n"
                        + "    ");
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    @Test(expected = AnalysisParserException.class)
    public void testParseAnalysisModeOfInheritanceUserUsesWrongValue() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    modeOfInheritance: AD\n"
                        + "    ");
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    /**
     * geneScoreMode was removed in commit 2055ac3b36c401569d9b201f43cf23d1f8c6aed2. We're checking that old analysis
     * scripts will still function.
     */
    @Test
    public void testParseAnalysis_DeprecatedGeneScoreModeHasNoEffect() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    geneScoreMode: RAW_SCORE\n"
                        + "    ");
        Analysis expected = Analysis.builder().vcfPath(Paths.get("test.vcf")).build();
        assertThat(analysis, equalTo(expected));
    }

    @Test
    public void testParseAnalysisStep_UnsupportedFilterAddsNothingToAnalysisSteps() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("wibbleFilter: {}"));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test
    public void testParseAnalysisStep_FailedVariantFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("failedVariantFilter: {}"));
        analysisSteps.add(new FailedVariantFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
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

    @Test(expected = AnalysisParserException.class)
    public void testParseAnalysisStep_FrequencyFilterNoFrequencySourcesDefined() {
        String script = "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    frequencySources: []\n"
                + "    steps: ["
                + "        frequencyFilter: {maxFrequency: 1.0}\n"
                + "]";
                
        instance.parseAnalysis(script);
    }

    @Test
    public void testParseAnalysisStep_FrequencyFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("frequencyFilter: {maxFrequency: 1.0}"));
        analysisSteps.add(new FrequencyFilter(1.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test(expected = AnalysisParserException.class)
    public void testParseAnalysisStep_PathogenicityFilterNoPathSourcesDefined() {
        String script = "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    pathogenicitySources: []\n"
                + "    steps: ["
                + "        pathogenicityFilter: {keepNonPathogenic: false}\n"
                + "]";
                
        instance.parseAnalysis(script);
    }

    @Test
    public void testParseAnalysisStep_PathogenicityFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("pathogenicityFilter: {keepNonPathogenic: false}"));
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
                + "    modeOfInheritance: UNINITIALIZED\n"
                + "    hpoIds: []\n"
                + "    analysisMode: PASS_ONLY \n"
                + "    pathogenicitySources: []\n"
                + "    frequencySources: []\n"
                + "    steps: ["
                + "        inheritanceFilter: {}\n"
                + "]"
        );
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test(expected = AnalysisParserException.class)
    public void testParseAnalysisStep_InheritanceFilterUnrecognisedValue() {
        instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    modeOfInheritance: WIBBLE!"
        );
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
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, HiPhiveOptions.DEFAULT));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStep_HiPhivePrioritiserWithUserDefinedOptions() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("hiPhivePrioritiser: {diseaseId: 'OMIM:101600', candidateGeneSymbol: FGFR2, runParams: 'human,mouse,fish,ppi'}"));
        HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder().diseaseId("OMIM:101600").candidateGeneSymbol("FGFR2").runParams("human,mouse,fish,ppi").build();
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, hiPhiveOptions));
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
    public void testParseAnalysisFileFromPath() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;

        Analysis analysis = instance.parseAnalysis(Paths.get("src/test/resources/analysisExample.yml"));
        System.out.println(analysis);
        assertThat(analysis.getVcfPath(), equalTo(Paths.get("test.vcf")));
        assertThat(analysis.getPedPath(), nullValue());
        assertThat(analysis.getHpoIds(), equalTo(hpoIds));
        assertThat(analysis.getModeOfInheritance(), equalTo(modeOfInheritance));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        analysisSteps.add(new IntervalFilter(new GeneticInterval(10, 123256200, 123256300)));
        analysisSteps.add(new EntrezGeneIdFilter(new LinkedHashSet<>(Arrays.asList(12345, 34567, 98765))));
        analysisSteps.add(new QualityFilter(50.0f));
        analysisSteps.add(new VariantEffectFilter(EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT)));
        analysisSteps.add(new KnownVariantFilter());
        analysisSteps.add(new FrequencyFilter(1.0f));
        analysisSteps.add(new PathogenicityFilter(false));
        analysisSteps.add(new InheritanceFilter(modeOfInheritance));
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, HiPhiveOptions.DEFAULT));
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, HiPhiveOptions.builder().diseaseId("OMIM:101600").candidateGeneSymbol("FGFR2").build()));
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
        OutputSettings expected = OutputSettings.builder()
                .outputPassVariantsOnly(false)
                .numberOfGenesToShow(0)
                .outputPrefix("results/Pfeiffer-hiphive")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF, OutputFormat.HTML))
                .build();
        assertThat(outputSettings, equalTo(expected));
    }
}
