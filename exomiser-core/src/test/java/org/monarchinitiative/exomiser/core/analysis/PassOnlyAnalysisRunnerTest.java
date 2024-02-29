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

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnalyser;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnnotator;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private final PassOnlyAnalysisRunner instance = new PassOnlyAnalysisRunner(genomeAnalysisService);

    @Test
    public void testRunAnalysisNoFiltersNoPrioritisersThrowsException() {
        Sample sample = vcfOnlySample;
        Analysis analysis = makeAnalysis();

        var exception = assertThrows(IllegalStateException.class, () -> instance.run(sample, analysis));
        assertThat(exception.getMessage(), equalTo("No analysis steps specified!"));
    }

    @Test
    public void testRunAnalysisRemovesAllelesUnobservedForProband() {
        Sample sample = Sample.builder()
                .vcfPath(Paths.get("src/test/resources/multiSampleWithProbandHomRef.vcf"))
                .probandSampleName("Seth")
                .pedigree(TestPedigrees.trioChildAffected())
                .build();

        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                //need at least one filter step to trigger the code path
                .addStep(new FailedVariantFilter())
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));
        assertThat(analysisResults.getVariantEvaluations().size(), equalTo(1));

        for (Gene gene : analysisResults.getGenes()) {
            assertThat(gene.passedFilters(), is(true));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(analysisResults.getProbandSampleName());
                assertThat(probandGenotype.getCalls().contains(AlleleCall.ALT), is(true));
            }
        }
    }

    @Test
    public void testRunAnalysisVariantFilterOnlyOneVariantPasses() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Sample sample = vcfOnlySample;
        Analysis analysis = makeAnalysis(intervalFilter);
        AnalysisResults analysisResults = instance.run(sample, analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Gene passedGene = analysisResults.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
    }

    @Test
    public void testRunAnalysisFailVariantFilterOnlyOneVariantPasses() {
        VariantFilter failedVariantFilter = new FailedVariantFilter();

        Sample sample = Sample.builder().vcfPath(Paths.get("src/test/resources/failedVariant.vcf")).build();
        Analysis analysis = makeAnalysis(failedVariantFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Gene passedGene = analysisResults.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        //For the PassOnlyAnalysisRunner the resulting genes should only contain passed variants
        assertThat(passedGene.getVariantEvaluations(), equalTo(passedGene.getPassedVariantEvaluations()));

        VariantEvaluation passedVariant = passedGene.getPassedVariantEvaluations().get(0);
        //1	145508800	rs12345678	T	C	123.15	PASS	GENE=RBM8A	GT:DP	1/1:33
        assertThat(passedVariant.contigId(), equalTo(1));
        assertThat(passedVariant.start(), equalTo(145508800));
        assertThat(passedVariant.ref(), equalTo("T"));
        assertThat(passedVariant.alt(), equalTo("C"));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersAllVariantsFail() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Sample sample = vcfOnlySample;
        Analysis analysis = makeAnalysis(intervalFilter, qualityFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().isEmpty(), is(true));
        assertThat(analysisResults.getVariantEvaluations().isEmpty(), is(true));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersOnePrioritiser() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Double> hiPhiveGeneScores = Map.of("GNRHR2", 0.75, "RBM8A", 0.65);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(intervalFilter, qualityFilter, mockHiPhivePrioritiser);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(1));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(true));
        assertThat(rbm8Variant1.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant1.passedFilter(FilterType.QUALITY_FILTER), is(true));
    }

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotInSingleSampleVcf() {
        Sample sample = Sample.builder()
                .vcfPath(vcfPath)
                .probandSampleName("mickyMouse")
                .build();
        Analysis analysis = makeAnalysis(new QualityFilter(120), new NoneTypePrioritiser());

        var exception = assertThrows(IllegalStateException.class, () -> instance.run(sample, analysis));
        assertThat(exception.getMessage(), equalTo("Proband sample name 'mickyMouse' is not found in the VCF sample. Expected one of [manuel]. Please check your sample and analysis files match."));
    }

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotInMultiSampleVcf() {
        Sample sample = Sample.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .probandSampleName("mickyMouse")
                .build();
        Analysis analysis = makeAnalysis(new QualityFilter(120), new NoneTypePrioritiser());

        var exception = assertThrows(IllegalStateException.class, () -> instance.run(sample, analysis));
        assertThat(exception.getMessage(), equalTo("Proband sample name 'mickyMouse' is not found in the VCF sample. Expected one of [Seth, Adam, Eva]. Please check your sample and analysis files match."));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersOnePrioritiserRecessiveInheritanceFilter() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Double> hiPhiveGeneScores = Map.of("GNRHR2", 0.75, "RBM8A", 0.65);
        Prioritiser<?> mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = Analysis.builder()
                .frequencySources(FrequencySource.ALL_EXAC_SOURCES)
                .addStep(intervalFilter)
                .addStep(qualityFilter)
                .addStep(new FrequencyFilter(0.1f))
                .addStep(mockHiPhivePrioritiser)
                .addStep(inheritanceFilter)
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();
        AnalysisResults analysisResults = instance.run(sample, analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(1));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

    @Test
    public void testRunAnalysisPrioritiserAndPriorityScoreFilterOnly() {
        double desiredPrioritiserScore = 0.9;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser<?> prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(prioritiser, priorityScoreFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(passedGene.hasVariants(), equalTo(false));
        System.out.println(passedGene.getGeneScores());    }

    @Test
    public void testRunAnalysisPrioritiserPriorityScoreFilterVariantFilter() {
        double desiredPrioritiserScore = 0.9f;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(prioritiser, priorityScoreFilter, intervalFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
        System.out.println(passedGene.getGeneScores());
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation rbm8Variant2 = passedGene.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
    }
    
    @Test
    public void testRunAnalysisVariantFilterPrioritiserPriorityScoreFilterVariantFilter() {
        double desiredPrioritiserScore = 0.9f;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        VariantFilter qualityFilter = new QualityFilter(120);
        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = Analysis.builder()
                .addStep(qualityFilter)
                .addStep(prioritiser)
                .addStep(priorityScoreFilter)
                .addStep(intervalFilter)
                .addStep(inheritanceFilter)
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
        System.out.println(passedGene.getGeneScores());
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation rbm8Variant2 = passedGene.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

    @Test
    public void testRunAnalysisAutosomalDominantTrioDeNovoInheritanceFilter() {
        VariantFilter qualityFilter = new QualityFilter(5);
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        Sample sample = Sample.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .build();
        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        Gene passedGene = results.get("GNRHR2");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(114814));
        assertThat(passedGene.getGeneSymbol(), equalTo("GNRHR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).start(), equalTo(145510000));
        
    }

    @Test
    public void testRunAnalysisAutosomalDominantTrioSharedInheritanceFilter() {
        VariantFilter qualityFilter = new QualityFilter(5);
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        Sample sample = Sample.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAndFatherAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .build();
        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).start(), equalTo(123256214));
    	
    }

    @Test
    public void testRunAnalysisAutosomalRecessiveTrioInheritanceFilter() {
        VariantFilter qualityFilter = new QualityFilter(5);
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Sample sample = Sample.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .build();

        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        //CompoundHeterozygous
        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(2));
        assertThat(passedGene.getVariantEvaluations().get(0).start(), equalTo(123256214));
        assertThat(passedGene.getVariantEvaluations().get(1).start(), equalTo(145508800));

        //Homozygous
        passedGene = results.get("FGFR2");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(2263));
        assertThat(passedGene.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).start(), equalTo(123239370));
    }

    @Test
    void testMergedCanvasCnvCalls() {
        VcfReader vcfReader = TestVcfReader.builder()
                .samples("Proband", "Mother")
                .vcfLines(
                        "1 145508656 Canvas:GAIN N <CNV> 100 PASS END=145508956 GT:CN .:3 .:.",
                        "1 145508756 Canvas:GAIN N <CNV> 100 PASS END=145509056 GT:CN .:. .:4"
                )
                .build();
        VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory(vcfReader);
        List<VariantEvaluation> variants = variantFactory.createVariantEvaluations()
                .collect(toList());

        Gene rbm8a = new Gene("ABC", 123);
        VariantEffectFilter variantEffectFilter = new VariantEffectFilter(EnumSet.of(VariantEffect.UPSTREAM_GENE_VARIANT, VariantEffect.DOWNSTREAM_GENE_VARIANT));
        variants.forEach(variantEvaluation -> {
            var result = variantEffectFilter.runFilter(variantEvaluation);
            variantEvaluation.addFilterResult(result);
            if (result.passed()) {
                rbm8a.addVariant(variantEvaluation);
            }
        });

        Pedigree pedigree = Pedigree.of(Pedigree.Individual.builder().id("Proband").sex(Pedigree.Individual.Sex.MALE).motherId("Mother").status(Pedigree.Individual.Status.AFFECTED).build(),
                Pedigree.Individual.builder().id("Mother").sex(Pedigree.Individual.Sex.FEMALE).status(Pedigree.Individual.Status.AFFECTED).build());

        InheritanceModeAnalyser instance = new InheritanceModeAnalyser(new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults()));
        instance.analyseInheritanceModes(List.of(rbm8a));
        System.out.println(rbm8a.getCompatibleInheritanceModes());
        rbm8a.getPassedVariantEvaluations().forEach(System.out::println);
    }

    @Test
    void testRunAnalysisPrioritiserPriorityScoreFilterSeperatesVariantFiltersRequiringDataProviderWrapping() {
        double desiredPrioritiserScore = 0.9f;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1);
        IntervalFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        FrequencyFilter frequencyFiler = new FrequencyFilter(1.0f);
        PathogenicityFilter pathogenicityFilter = new PathogenicityFilter(true);

        Analysis analysis = Analysis.builder()
                .frequencySources(EnumSet.of(FrequencySource.GNOMAD_E_AFR))
                .pathogenicitySources(EnumSet.of(PathogenicitySource.REVEL))
                .steps(List.of(intervalFilter, prioritiser, pathogenicityFilter, priorityScoreFilter, frequencyFiler))
                .build();

        // setup data and mocking data
        Variant variant = TestFactory.variantBuilder(1, 145508800, "T", "C").build();
        FrequencyData frequencyData = FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_E_AFR, 0.001f));
        PathogenicityData pathogenicityData = PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.REVEL, 1.0f));

        VariantDataService mockVariantDataService = TestVariantDataService.builder()
                .put(variant, frequencyData)
                .put(variant, pathogenicityData)
                .build();

        GenomeAnalysisService mockGenomeAnalysisService = new GenomeAnalysisServiceImpl(TestFactory.getDefaultGenomeAssembly(),
                TestFactory.buildDefaultGenomeDataService(),
                mockVariantDataService,
                TestFactory.buildDefaultVariantAnnotator());
        PassOnlyAnalysisRunner instance = new PassOnlyAnalysisRunner(mockGenomeAnalysisService);

        AnalysisResults analysisResults = instance.run(vcfandPhenotypesSample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation rbm8Variant2 = passedGene.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
        assertThat(rbm8Variant2.getFrequencyData(), equalTo(frequencyData));
        assertThat(rbm8Variant2.getPathogenicityData(), equalTo(pathogenicityData));
    }
}
