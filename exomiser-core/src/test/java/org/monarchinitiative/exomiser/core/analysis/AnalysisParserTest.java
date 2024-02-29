/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisParserTest {

    private AnalysisParser instance;
    private PriorityFactory priorityFactory;
    private final OntologyService ontologyService = TestOntologyService.builder().build();

    private List<AnalysisStep> analysisSteps;

    private List<String> hpoIds;
    private Set<FrequencySource> frequencySources;
    private Set<PathogenicitySource> pathogenicitySources;

    @BeforeEach
    public void setUp() {
        priorityFactory = new NoneTypePriorityFactoryStub();
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(TestFactory.buildDefaultHg19GenomeAnalysisService());

        instance = new AnalysisParser(genomeAnalysisServiceProvider, priorityFactory, ontologyService);

        analysisSteps = new ArrayList<>();
        hpoIds = new ArrayList<>(Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055"));
        frequencySources = EnumSet.of(FrequencySource.THOUSAND_GENOMES, FrequencySource.ESP_AA, FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN);
        pathogenicitySources = EnumSet.of(PathogenicitySource.SIFT, PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER);
    }

    private static String addStepToAnalysis(String step) {
        return String.format("analysis:\n"
                + "    vcf: test.vcf\n"
                + "    genomeAssembly: hg19\n"
                + "    ped:\n"
                + "    inheritanceModes: {\n" +
                "            AUTOSOMAL_DOMINANT: 0.1,\n" +
                "            AUTOSOMAL_RECESSIVE_HOM_ALT: 0.1,\n" +
                "            AUTOSOMAL_RECESSIVE_COMP_HET: 2.0,\n" +
                "            X_DOMINANT: 0.1,\n" +
                "            X_RECESSIVE_HOM_ALT: 0.1,\n" +
                "            X_RECESSIVE_COMP_HET: 2.0,\n" +
                "            MITOCHONDRIAL: 0.2 \n" +
                "      }\n"
                + "    hpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']\n"
                + "    analysisMode: PASS_ONLY \n"
                + "    frequencySources: [THOUSAND_GENOMES, ESP_AFRICAN_AMERICAN, EXAC_AFRICAN_INC_AFRICAN_AMERICAN]\n"
                + "    pathogenicitySources: [SIFT, POLYPHEN, MUTATION_TASTER]\n"
                + "    steps: ["
                + "        %s\n"
                + "]", step);
    }

    @Test
    public void testParseAnalysisStepsNoSteps() {
        Sample sample = instance.parseSample(addStepToAnalysis(""));
        assertThat(sample.getVcfPath(), equalTo(Paths.get("test.vcf")));
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
        assertThat(sample.getProbandSampleName(), equalTo(""));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));

        Analysis analysis = instance.parseAnalysis(addStepToAnalysis(""));
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test
    public void testParseAnalysisPedPathSpecified() {
        Sample sample = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    proband: " + TestPedigrees.affectedChild().getId() + "\n"
                        + "    ped: " + TestPedigrees.trioChildAffectedPedPath() + "\n"
                        + "    ");
        assertThat(sample.getPedigree(), equalTo(TestPedigrees.trioChildAffected()));
    }

    @Test
    public void testParseAnalysisPedPathEmpty() {
        Sample sample = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    ped: ''\n"
                        + "    ");
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
    }

    @Test
    public void testParseAnalysisProbandSampleNameSpecified() {
        Sample sample = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    proband: Bod \n"
                        + "    ");
        assertThat(sample.getProbandSampleName(), equalTo("Bod"));
    }

    @Test
    public void testParseAnalysisFullAnalysisMode() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                + "    analysisMode: FULL \n"
                + "    ");
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testParseAnalysisPassOnlyAnalysisMode() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    analysisMode: PASS_ONLY \n"
                        + "    ");
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void testParseAnalysisSparseAnalysisModeReturnsPassOnlyDefault() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    analysisMode: SPARSE \n"
                        + "    ");
        // AnalysisMode.SPARSE was removed in version 11.0.0
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void testParseAnalysisNotSettingGenomeBuildReturnsDefault() {
        Sample sample = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    ");
        assertThat(sample.getGenomeAssembly(), equalTo(GenomeAssembly.defaultBuild()));
    }

    @Test
    public void testParseAnalysisThrowsExceptionForUnsupportedGenomeBuild() {
        assertThrows(UnsupportedGenomeAssemblyException.class, () ->
                instance.parseSample(
                        "analysis:\n"
                                + "    vcf: test.vcf\n"
                                + "    genomeAssembly: hg38\n"
                                + "    ")
        );
    }

    @Test
    public void testParseAnalysisCanSetAlternativeGenomeAssemblyUsingUcscName() {
        AnalysisParser hg19And38SupportedParser = getHg19and38SupportedParser();

        Sample hg38Sample = hg19And38SupportedParser.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    genomeAssembly: hg38\n"
                        + "    ");
        assertThat(hg38Sample.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));

        Sample hg19Sample = hg19And38SupportedParser.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    genomeAssembly: hg19\n"
                        + "    ");
        assertThat(hg19Sample.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
    }

    private AnalysisParser getHg19and38SupportedParser() {
        GenomeAnalysisService hg19AnalysisService = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG19);
        GenomeAnalysisService hg38AnalysisService = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG38);

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(hg19AnalysisService, hg38AnalysisService);
        return new AnalysisParser(genomeAnalysisServiceProvider, priorityFactory, ontologyService);
    }

    @Test
    public void testParseAnalysisCanSetGenomeBuildUsingGrcName() {
        Sample analysis = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    genomeAssembly: GRCh37\n"
                        + "    hpoIds: ['HP:0000001','HP:0000002']"
                        + "    ");
        assertThat(analysis.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
    }

    @Test
    public void testParseAnalysisUnrecognisedGenomeBuild() {
        assertThrows(GenomeAssembly.InvalidGenomeAssemblyException.class, () ->
                instance.parseSample(
                        "analysis:\n"
                                + "    vcf: test.vcf\n"
                                + "    genomeAssembly: invalid\n"
                                + "    ")
        );
    }

    @Test
    public void testParseAnalysisModeOfInheritanceAutosomalDominant() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    inheritanceModes: {\n"
                        + "        AUTOSOMAL_DOMINANT: 1.0 \n"
                        + "}\n"
                        + "    ");
        Map<SubModeOfInheritance, Float> options = Map.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0f);
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.of(options)));
    }

    @Test
    public void testParseAnalysisModeOfInheritanceMultipleModes() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    inheritanceModes: {\n"
                        + "        AUTOSOMAL_DOMINANT: 0.1,\n"
                        + "        ANY: 0.1 \n"
                        + "}\n"
                        + "    ");

        Map<SubModeOfInheritance, Float> options = Map.of(
                SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f,
                SubModeOfInheritance.ANY, 0.1f
        );
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.of(options)));
    }

    @Test
    public void testParseAnalysisOldModeOfInheritanceConvertsToInheritanceModes() {
        // The modeOfInheritance option was removed in version 11.0.0
        // Come version 13.0.0 and due to a new protobuf backend this is no longer converted.
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    modeOfInheritance: AUTOSOMAL_DOMINANT\n"
                        + "    ");
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.empty()));
    }

    @Test
    public void testParseAnalysisModeOfInheritanceUserUsesWrongValue() {
        // The modeOfInheritance option was removed in version 11.0.0
        // Come version 13.0.0 and due to a new protobuf backend this is no longer converted.
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                        + "    modeOfInheritance: AD\n"
        );
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.empty()));
    }

    /**
     * geneScoreMode was removed in commit 2055ac3b36c401569d9b201f43cf23d1f8c6aed2. We're checking that old analysis
     * scripts will still function.
     */
    @Test
    public void testParseAnalysisDeprecatedGeneScoreModeHasNoEffect() {
        Sample analysis = instance.parseSample(
                "analysis:\n"
                        + "    vcf: test.vcf\n"
                        + "    geneScoreMode: RAWSCORE\n"
        );
        Sample expected = Sample.builder().vcfPath(Paths.get("test.vcf")).build();
        assertThat(analysis, equalTo(expected));
    }

    @Test
    public void testParseAnalysisStepUnsupportedFilterAddsNothingToAnalysisSteps() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("wibbleFilter: {}"));
        assertThat(analysis.getAnalysisSteps().isEmpty(), is(true));
    }

    @Test
    public void testParseAnalysisStepFailedVariantFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("failedVariantFilter: {}"));
        analysisSteps.add(new FailedVariantFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepRegulatoryFeatureFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("regulatoryFeatureFilter: {}"));
        analysisSteps.add(new RegulatoryFeatureFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepIntervalFilterFromInterval() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("intervalFilter: {interval: 'chr10:122892600-122892700'}"));
        analysisSteps.add(new IntervalFilter(new GeneticInterval(10, 122892600, 122892700)));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepIntervalFilterFromList() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("intervalFilter: {intervals: ['chr10:122892600-122892700', 'chr10:122892900-122893000']}"));
        List<ChromosomalRegion> expectedIntervals = List.of(
                new GeneticInterval(10, 122892600, 122892700),
                new GeneticInterval(10, 122892900, 122893000)
        );

        analysisSteps.add(new IntervalFilter(expectedIntervals));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepIntervalFilterFromBedFile() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("intervalFilter: {bed: src/test/resources/intervals.bed}"));
        List<ChromosomalRegion> expectedIntervals = new ArrayList<>();
        expectedIntervals.add(new GeneticInterval(7, 127471197, 127472363));
        expectedIntervals.add(new GeneticInterval(7, 127472364, 127473530));
        expectedIntervals.add(new GeneticInterval(7, 127475865, 127477031));
        expectedIntervals.add(new GeneticInterval(7, 127479366, 127480532));
        expectedIntervals.add(new GeneticInterval(7, 127480533, 127481699));

        analysisSteps.add(new IntervalFilter(expectedIntervals));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testThrowsExceptionWithUnexpectedTokenForIntervalFilter() {
        assertThrows(IllegalArgumentException.class, () ->
                instance.parseAnalysis(addStepToAnalysis("intervalFilter: {bod: src/test/resources/intervals.bed}"))
        );
    }

    @Test
    public void testParseAnalysisStepGeneIdFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("genePanelFilter: {geneSymbols: [FGFR1, FGFR2]}"));
        analysisSteps.add(new GeneSymbolFilter(new LinkedHashSet<>(Arrays.asList("FGFR1", "FGFR2"))));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepQualityFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("qualityFilter: {minQuality: 50.0}"));
        analysisSteps.add(new QualityFilter(50.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepVariantEffectFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("variantEffectFilter: {remove: [SYNONYMOUS_VARIANT, INTERGENIC_VARIANT]}"));
        analysisSteps.add(new VariantEffectFilter(EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT, VariantEffect.INTERGENIC_VARIANT)));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepVariantEffectFilterillegalVariantEffect() {
        assertThrows(IllegalArgumentException.class, () ->
                instance.parseAnalysis(addStepToAnalysis("variantEffectFilter: {remove: [WIBBLE]}"))
        );
    }

    @Test
    public void testParseAnalysisStepKnownVariantFilterFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("knownVariantFilter: {}"));
        analysisSteps.add(new KnownVariantFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepFrequencyFilterNoFrequencySourcesDefined() {
        String script = "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    frequencySources: []\n"
                + "    steps: ["
                + "        frequencyFilter: {maxFrequency: 1.0}\n"
                + "]";

        assertThrows(IllegalStateException.class, () ->
                instance.parseAnalysis(script)
        );
    }

    @Test
    public void testParseAnalysisStepFrequencyFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("frequencyFilter: {maxFrequency: 1.0}"));
        analysisSteps.add(new FrequencyFilter(1.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepFrequencyFilterNoMaxFreqDefined() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("frequencyFilter: {}"));
        analysisSteps.add(new FrequencyFilter(2.0f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepPathogenicityFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("pathogenicityFilter: {keepNonPathogenic: false}"));
        analysisSteps.add(new PathogenicityFilter(false));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepPriorityScoreFilter() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.65}"));
        analysisSteps.add(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.65f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepInheritanceFilterUndefinedMode() {
        Analysis analysis = instance.parseAnalysis(
                "analysis:\n"
                + "    vcf: test.vcf\n"
                + "    inheritanceModes: {}\n"
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

    @Test
    public void testParseAnalysisStepInheritanceFilterUnrecognisedValue() {
        assertThrows(IllegalArgumentException.class, () ->
                instance.parseAnalysis(
                        "analysis:\n"
                                + "    vcf: test.vcf\n"
                                + "    inheritanceModes: {WIBBLE: 0.0}\n"
                )
        );
    }

    @Test
    public void testParseAnalysisGeneBlacklistfilter(){
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("geneBlacklistFilter: {}"));
        analysisSteps.add(new GeneBlacklistFilter());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepInheritanceFilterDefinedMode() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("inheritanceFilter: {}"));
        analysisSteps.add(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.MITOCHONDRIAL));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepOmimPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("omimPrioritiser: {}"));
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepHiPhivePrioritiserWithDefaultOptions() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("hiPhivePrioritiser: {}"));
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.defaults()));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepHiPhivePrioritiserWithUserDefinedOptions() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("hiPhivePrioritiser: {diseaseId: 'OMIM:101600', candidateGeneSymbol: FGFR2, runParams: 'human,mouse,fish,ppi'}"));
        HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder().diseaseId("OMIM:101600").candidateGeneSymbol("FGFR2").runParams("human,mouse,fish,ppi").build();
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepPhivePrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("phivePrioritiser: {}"));
        analysisSteps.add(priorityFactory.makePhivePrioritiser());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisStepPhenixPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("phenixPrioritiser: {}"));
        analysisSteps.add(priorityFactory.makePhenixPrioritiser());
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));

        //Disable here if we have out-of-date data.
//        assertThrows(IllegalArgumentException.class, () ->
//                instance.parseAnalysis(addStepToAnalysis("phenixPrioritiser: {}"))
//        );
    }

    @Disabled("Non-functional in proto version")
    @Test
    public void testParseAnalysisStepWalkerPrioritiser() {
        Analysis analysis = instance.parseAnalysis(addStepToAnalysis("exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}"));
        analysisSteps.add(priorityFactory.makeExomeWalkerPrioritiser(new ArrayList<>(Arrays.asList(11111, 22222, 33333))));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisFileFromPath() {
        Sample sample = instance.parseSample(Paths.get("src/test/resources/analysisExample.yml"));
        assertThat(sample.getVcfPath(), equalTo(Paths.get("test.vcf")));
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
        assertThat(sample.getHpoIds(), equalTo(hpoIds));

        Analysis analysis = instance.parseAnalysis(Paths.get("src/test/resources/analysisExample.yml"));
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.defaultForModes(modeOfInheritance)));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        analysisSteps.add(new IntervalFilter(new GeneticInterval(10, 123256200, 123256300)));
        analysisSteps.add(new GeneSymbolFilter(new LinkedHashSet<>(Arrays.asList("FGFR1", "FGFR2"))));
        analysisSteps.add(new QualityFilter(50.0f));
        analysisSteps.add(new VariantEffectFilter(EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT)));
        analysisSteps.add(new KnownVariantFilter());
        analysisSteps.add(new FrequencyFilter(1.0f));
        analysisSteps.add(new PathogenicityFilter(false));
        analysisSteps.add(new InheritanceFilter(modeOfInheritance));
        analysisSteps.add(priorityFactory.makeOmimPrioritiser());
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.defaults()));
        analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.builder()
                .diseaseId("OMIM:101600")
                .candidateGeneSymbol("FGFR2")
                .build()));
        analysisSteps.add(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.7f));
        assertThat(analysis.getAnalysisSteps(), equalTo(analysisSteps));
    }

    @Test
    public void testParseAnalysisNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () ->
                instance.parseAnalysis(Paths.get("src/test/resources/wibble"))
        );
    }

    @Test
    public void testParseOutputSettingsNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () ->
                instance.parseOutputSettings(Paths.get("src/test/resources/wibble"))
        );
    }

    @Test
    public void testParseOutputSettingsIgnoresOutputPassVariantsOnly() {
        // The outputPassVariantsOnly option was removed in version 11.0.0
        // Come version 13.0.0 and due to a new protobuf backend this is no longer converted.
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                        + "    outputPassVariantsOnly: true\n");
        assertThat(outputSettings.outputContributingVariantsOnly(), is(false));
    }

    @Test
    public void testParseOutputSettingsNumGenesToShow() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                        + "    numGenes: 1\n");
        assertThat(outputSettings.getNumberOfGenesToShow(), equalTo(1));
    }

    @Test
    public void testParseOutputSettingsOutputPrefix() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                        + "    outputPrefix: results/Pfeiffer-hiphive\n");
        assertThat(outputSettings.getOutputPrefix(), equalTo("results/Pfeiffer-hiphive"));
    }

    @Test
    public void testParseOutputSettingsAllSupportedOutputFormats() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                + "    outputPassVariantsOnly: true\n"
                + "    numGenes: 1\n"
                + "    outputPrefix: results/Pfeiffer-hiphive\n"
                + "    outputFormats: [HTML, JSON, TSV-GENE, TSV-VARIANT, VCF]\n");
        Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML, OutputFormat.JSON, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF);
        assertThat(outputSettings.getOutputFormats(), equalTo((outputFormats)));
    }

    @Test
    public void testParseOutputSettingsNoOutputFormats() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                        + "    outputPassVariantsOnly: true\n"
                        + "    numGenes: 1\n"
                        + "    outputPrefix: results/Pfeiffer-hiphive\n"
                        + "    outputFormats:\n");
        Set<OutputFormat> outputFormats = EnumSet.noneOf(OutputFormat.class);
        assertThat(outputSettings.getOutputFormats(), equalTo((outputFormats)));
    }

    @Test
    public void testParseOutputSettingsUnsupportedOutputFormatDefaultsToHtml() {
        OutputSettings outputSettings = instance.parseOutputSettings(
                "outputOptions:\n"
                        + "    outputPassVariantsOnly: true\n"
                        + "    numGenes: 1\n"
                        + "    outputPrefix: results/Pfeiffer-hiphive\n"
                        + "    outputFormats: [WIBBLE!]\n");
        Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML);
        assertThat(outputSettings.getOutputFormats(), equalTo((outputFormats)));
    }

    @Test
    public void testParseOutputSettings() {
        OutputSettings outputSettings = instance.parseOutputSettings(Paths.get("src/test/resources/analysisExample.yml"));
        OutputSettings expected = OutputSettings.builder()
                .outputContributingVariantsOnly(false)
                .numberOfGenesToShow(0)
                .outputDirectory(Path.of("results"))
                .outputFileName("Pfeiffer-hiphive")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF, OutputFormat.HTML))
                .build();
        assertThat(outputSettings, equalTo(expected));
    }
}
