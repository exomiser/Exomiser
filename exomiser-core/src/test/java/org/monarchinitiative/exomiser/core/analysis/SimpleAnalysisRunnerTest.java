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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private final SimpleAnalysisRunner instance = new SimpleAnalysisRunner(genomeAnalysisService);

    @Test
    public void runEmptyAnalysisThrowsException() {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();

        var exception = assertThrows(IllegalStateException.class, () -> instance.run(sample, analysis));
        assertThat(exception.getMessage(), equalTo("No analysis steps specified!"));
    }

    @Test
    public void runAnalysisVariantFilterOnlyOneVariantPasses() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        Sample sample = vcfOnlySample;
        Analysis analysis = makeAnalysis(intervalFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().size(), equalTo(1));

        VariantEvaluation rbm8Variant2 = rbm8a.getPassedVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));

    }

    @Test
    public void runAnalysisTwoVariantFiltersAllVariantsFailFiltersVariantsShouldHaveAllVariantFilterResults() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Sample sample = vcfOnlySample;
        Analysis analysis = makeAnalysis(intervalFilter, qualityFilter);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.QUALITY_FILTER)));
        
        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(false));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(true));
        
        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.QUALITY_FILTER)));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(false));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.QUALITY_FILTER)));
    }

    @Test
    public void runAnalysisTwoVariantFiltersOnePrioritiserVariantsShouldHaveAllVariantFilterResults() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Double> hiPhiveGeneScores = Map.of("GNRHR2", 0.75, "RBM8A", 0.65);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(intervalFilter, qualityFilter, mockHiPhivePrioritiser);

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.QUALITY_FILTER)));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
    }
    
    @Test
    public void runAnalysisTwoVariantFiltersOnePrioritiserRecessiveInheritanceFilterVariantsShouldContainOnlyOneFailedFilterResult() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Double> hiPhiveGeneScores = Map.of("GNRHR2", 0.75, "RBM8A", 0.65);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Sample sample = vcfandPhenotypesSample;

        Analysis analysis = Analysis.builder()
                .addStep(intervalFilter)
                .addStep(qualityFilter)
                .addStep(mockHiPhivePrioritiser)
                .addStep(inheritanceFilter)
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();

        AnalysisResults analysisResults = instance.run(sample, analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));
        assertThat(rbm8a.passedFilter(FilterType.INHERITANCE_FILTER), is(true));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.QUALITY_FILTER, FilterType.INHERITANCE_FILTER)));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }


    @Test
    public void runAnalysisPrioritiserAndPriorityScoreFilterOnly() {
        double desiredPrioritiserScore = 0.9;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1);

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(prioritiser, priorityScoreFilter);
        AnalysisResults analysisResults = instance.run(sample, analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(4));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));
        assertThat(rbm8a.hasVariants(), equalTo(false));
        assertThat(rbm8a.getPriorityScore(), equalTo(desiredPrioritiserScore));

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
        assertThat(gnrh2.hasVariants(), equalTo(false));

        Gene fgfr2 = results.get("FGFR2");
        assertThat(fgfr2.passedFilters(), is(false));
        assertThat(fgfr2.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
        assertThat(fgfr2.hasVariants(), equalTo(false));

        Gene shh = results.get("SHH");
        assertThat(shh.passedFilters(), is(false));
        assertThat(shh.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
        assertThat(shh.hasVariants(), equalTo(false));
    }

    @Test
    public void runAnalysisPrioritiserPriorityScoreFilterVariantFilter() {
        double desiredPrioritiserScore = 0.9f;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        Prioritiser prioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Sample sample = vcfandPhenotypesSample;
        Analysis analysis = makeAnalysis(prioritiser, priorityScoreFilter, intervalFilter);
        AnalysisResults analysisResults = instance.run(sample, analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.PRIORITY_SCORE_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getEntrezGeneID(), equalTo(9939));
        assertThat(rbm8a.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(rbm8a.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(rbm8a.getGeneSymbol()));
    }

    @Test
    public void runAnalysisVariantFilterPrioritiserPriorityScoreFilterVariantFilter() {
        double desiredPrioritiserScore = 0.9f;
        Map<String, Double> geneSymbolPrioritiserScores = Map.of("RBM8A", desiredPrioritiserScore);

        VariantFilter qualityFilter = new QualityFilter(120);
        Prioritiser prioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, desiredPrioritiserScore - 0.1);
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
        //TODO: remove all this repetitive cruft into common method
        AnalysisResults analysisResults = instance.run(sample, analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));

        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER, FilterType.PRIORITY_SCORE_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getEntrezGeneID(), equalTo(9939));
        assertThat(rbm8a.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(rbm8a.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.QUALITY_FILTER, FilterType.INTERVAL_FILTER, FilterType.INHERITANCE_FILTER)));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.contigId(), equalTo(1));
        assertThat(rbm8Variant2.start(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(rbm8a.getGeneSymbol()));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

    @Test
    void testRunAnalysisPrioritiserPriorityScoreFilterSeparatesVariantFiltersRequiringDataProviderWrapping() {
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
