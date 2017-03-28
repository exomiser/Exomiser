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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.model.FilterStatus;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private final PassOnlyAnalysisRunner instance = new PassOnlyAnalysisRunner(geneFactory, variantFactory, stubDataService);

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath);

        AnalysisResults analysisResults = instance.run(analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
        for (Gene gene : analysisResults.getGenes()) {
            assertThat(gene.passedFilters(), is(true));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                assertThat(variantEvaluation.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
            }
        }
    }

    @Test
    public void testRunAnalysis_VariantFilterOnly_OneVariantPasses() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter);
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Gene passedGene = analysisResults.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
    }

    @Test
    public void testRunAnalysis_FailVariantFilterOnly_OneVariantPasses() {
        VariantFilter failedVariantFilter = new FailedVariantFilter();

        Analysis analysis = makeAnalysis(Paths.get("src/test/resources/failedVariant.vcf"), failedVariantFilter);
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Gene passedGene = analysisResults.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        //For the PassOnlyAnalysisRunner the resulting genes should only contain passed variants
        assertThat(passedGene.getVariantEvaluations(), equalTo(passedGene.getPassedVariantEvaluations()));

        VariantEvaluation passedVariant = passedGene.getPassedVariantEvaluations().get(0);
        //1	145508800	rs12345678	T	C	123.15	PASS	GENE=RBM8A	GT:DP	1/1:33
        assertThat(passedVariant.getChromosome(), equalTo(1));
        assertThat(passedVariant.getPosition(), equalTo(145508800));
        assertThat(passedVariant.getRef(), equalTo("T"));
        assertThat(passedVariant.getAlt(), equalTo("C"));
    }

    @Test
    public void testRunAnalysis_TwoVariantFilters_AllVariantsFail() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter);
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().isEmpty(), is(true));
        assertThat(analysisResults.getVariantEvaluations().isEmpty(), is(true));
    }

    @Test
    public void testRunAnalysis_TwoVariantFiltersOnePrioritiser() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Float> hiPhiveGeneScores = new HashMap<>();
        hiPhiveGeneScores.put("GNRHR2", 0.75f);
        hiPhiveGeneScores.put("RBM8A", 0.65f);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter, mockHiPhivePrioritiser);
        AnalysisResults analysisResults = instance.run(analysis);
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

    @Test(expected = SampleMismatchException.class)
    public void testRunAnalysisWhenProbandSampleNameIsNotInSingleSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .probandSampleName("mickyMouse")
                .build();
        instance.run(analysis);
    }

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotSpecifiedAndHaveSingleSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .build();
        instance.run(analysis);
    }

    @Test(expected = SampleMismatchException.class)
    public void testRunAnalysisWhenProbandSampleNameIsNotInMultiSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(inheritanceFilterVCFPath)
                .probandSampleName("mickyMouse")
                .build();
        instance.run(analysis);
    }

    @Test
    public void testRunAnalysis_TwoVariantFiltersOnePrioritiserRecessiveInheritanceFilter() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Float> hiPhiveGeneScores = new HashMap<>();
        hiPhiveGeneScores.put("GNRHR2", 0.75f);
        hiPhiveGeneScores.put("RBM8A", 0.65f);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .addStep(intervalFilter)
                .addStep(qualityFilter)
                .addStep(mockHiPhivePrioritiser)
                .addStep(inheritanceFilter)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        AnalysisResults analysisResults = instance.run(analysis);
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
    public void testRunAnalysis_PrioritiserAndPriorityScoreFilterOnly() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter);
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(analysisResults.getGenes());

        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getPriorityScore(), equalTo(desiredPrioritiserScore));
    }

    @Test
    public void testRunAnalysis_PrioritiserPriorityScoreFilterVariantFilter() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter, intervalFilter);
        AnalysisResults analysisResults = instance.run(analysis);
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
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getPosition(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
    }
    
    @Test
    public void testRunAnalysis_VariantFilterPrioritiserPriorityScoreFilterVariantFilter() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        VariantFilter qualityFilter = new QualityFilter(120);
        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        
        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .addStep(qualityFilter)
                .addStep(prioritiser)
                .addStep(priorityScoreFilter)
                .addStep(intervalFilter)
                .addStep(inheritanceFilter)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        AnalysisResults analysisResults = instance.run(analysis);
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
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getPosition(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

    @Test
    public void testRunAnalysis_autosomalDominantTrioDeNovoInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
    	Analysis analysis = Analysis.builder()
                .vcfPath(inheritanceFilterVCFPath)
                .pedPath(childAffectedPedPath)
                .probandSampleName("Seth")
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));
        
        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        Gene passedGene = results.get("GNRHR2");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(passedGene.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(114814));
        assertThat(passedGene.getGeneSymbol(), equalTo("GNRHR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getPosition(), equalTo(145510000));
        
    }

    @Test
    public void testRunAnalysis_autosomalDominantTrioSharedInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        Analysis analysis = Analysis.builder()
                .vcfPath(inheritanceFilterVCFPath)
                .pedPath(twoAffectedPedPath)
                .probandSampleName("Seth")
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));
        
        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(passedGene.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getPosition(), equalTo(123256214));
    	
    }

    @Test
    public void testRunAnalysis_autosomalRecessiveTrioInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
    	Analysis analysis = Analysis.builder()
                .vcfPath(inheritanceFilterVCFPath)
                .pedPath(childAffectedPedPath)
                .probandSampleName("Seth")
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .build();
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
        
        Map<String, Gene> results = makeResults(analysisResults.getGenes());
        //CompoundHeterozygous
        Gene passedGene = results.get("RBM8A");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(passedGene.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(2));
        assertThat(passedGene.getVariantEvaluations().get(0).getPosition(), equalTo(123256214));
        assertThat(passedGene.getVariantEvaluations().get(1).getPosition(), equalTo(145508800));

        //Homozygous
        passedGene = results.get("FGFR2");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(passedGene.getInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(2263));
        assertThat(passedGene.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getPosition(), equalTo(123239370));
    }

}
