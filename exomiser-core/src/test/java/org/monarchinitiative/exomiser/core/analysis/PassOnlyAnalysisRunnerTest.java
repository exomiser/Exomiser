/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.analysis.sample.SampleMismatchException;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private final PassOnlyAnalysisRunner instance = new PassOnlyAnalysisRunner(genomeAnalysisService);

    @Test
    public void testRunAnalysisNoFiltersNoPrioritisers() {
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
    public void testRunAnalysisRemovesAllelesUnobservedForProband() {
        Analysis analysis = Analysis.builder()
                .vcfPath(Paths.get("src/test/resources/multiSampleWithProbandHomRef.vcf"))
                .probandSampleName("Seth")
                .pedigree(TestPedigrees.trioChildAffected())
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                //need at least one filter step to trigger the code path
                .addStep(new FailedVariantFilter())
                .build();

        AnalysisResults analysisResults = instance.run(analysis);

        printResults(analysisResults);
        assertThat(analysisResults.getGenes().size(), equalTo(1));
        assertThat(analysisResults.getVariantEvaluations().size(), equalTo(1));

        for (Gene gene : analysisResults.getGenes()) {
            assertThat(gene.passedFilters(), is(true));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                Map<String, SampleGenotype> sampleGenotypes = variantEvaluation.getSampleGenotypes();
                SampleGenotype probandGenotype = sampleGenotypes.get(analysisResults.getProbandSampleName());
                assertThat(probandGenotype.getCalls().contains(AlleleCall.ALT), is(true));
            }
        }
    }

    @Test
    public void testRunAnalysisVariantFilterOnlyOneVariantPasses() {
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
    public void testRunAnalysisFailVariantFilterOnlyOneVariantPasses() {
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
        assertThat(passedVariant.getStart(), equalTo(145508800));
        assertThat(passedVariant.getRef(), equalTo("T"));
        assertThat(passedVariant.getAlt(), equalTo("C"));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersAllVariantsFail() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter);
        AnalysisResults analysisResults = instance.run(analysis);
        printResults(analysisResults);
        assertThat(analysisResults.getGenes().isEmpty(), is(true));
        assertThat(analysisResults.getVariantEvaluations().isEmpty(), is(true));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersOnePrioritiser() {
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

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotInSingleSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .probandSampleName("mickyMouse")
                .build();
        assertThrows(SampleMismatchException.class , () -> instance.run(analysis));
    }

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotSpecifiedAndHaveSingleSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(vcfPath)
                .build();
        instance.run(analysis);
    }

    @Test
    public void testRunAnalysisWhenProbandSampleNameIsNotInMultiSampleVcf() {
        Analysis analysis = Analysis.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .probandSampleName("mickyMouse")
                .build();
        assertThrows(SampleMismatchException.class , () -> instance.run(analysis));
    }

    @Test
    public void testRunAnalysisTwoVariantFiltersOnePrioritiserRecessiveInheritanceFilter() {
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
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
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
    public void testRunAnalysisPrioritiserAndPriorityScoreFilterOnly() {
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
        System.out.println(passedGene.getGeneScores());    }

    @Test
    public void testRunAnalysisPrioritiserPriorityScoreFilterVariantFilter() {
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
        System.out.println(passedGene.getGeneScores());
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation rbm8Variant2 = passedGene.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getStart(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
    }
    
    @Test
    public void testRunAnalysisVariantFilterPrioritiserPriorityScoreFilterVariantFilter() {
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
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
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
        System.out.println(passedGene.getGeneScores());
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));

        VariantEvaluation rbm8Variant2 = passedGene.getVariantEvaluations().get(0);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getStart(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(passedGene.getGeneSymbol()));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

    @Test
    public void testRunAnalysisAutosomalDominantTrioDeNovoInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
    	Analysis analysis = Analysis.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
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
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(114814));
        assertThat(passedGene.getGeneSymbol(), equalTo("GNRHR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getStart(), equalTo(145510000));
        
    }

    @Test
    public void testRunAnalysisAutosomalDominantTrioSharedInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        Analysis analysis = Analysis.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAndFatherAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
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
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getStart(), equalTo(123256214));
    	
    }

    @Test
    public void testRunAnalysisAutosomalRecessiveTrioInheritanceFilter() {
    	VariantFilter qualityFilter = new QualityFilter(5);
    	InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
    	Analysis analysis = Analysis.builder()
                .vcfPath(TestPedigrees.trioVcfPath())
                .pedigree(TestPedigrees.trioChildAffected())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
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
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(9939));
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(2));
        assertThat(passedGene.getVariantEvaluations().get(0).getStart(), equalTo(123256214));
        assertThat(passedGene.getVariantEvaluations().get(1).getStart(), equalTo(145508800));

        //Homozygous
        passedGene = results.get("FGFR2");
        assertThat(passedGene.passedFilters(), is(true));
        assertThat(passedGene.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(passedGene.getCompatibleInheritanceModes(), hasItem(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        assertThat(passedGene.getEntrezGeneID(), equalTo(2263));
        assertThat(passedGene.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
        assertThat(passedGene.getVariantEvaluations().get(0).getStart(), equalTo(123239370));
    }

}
