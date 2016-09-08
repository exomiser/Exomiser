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

package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.MockPrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest extends AnalysisRunnerTestBase {

    private PassOnlyAnalysisRunner instance;
    
    @Before
    public void setUp() {
        instance = new PassOnlyAnalysisRunner(sampleDataFactory, stubDataService);
    }

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath);

        SampleData sampleData = instance.run(analysis);

        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));
        for (Gene gene : sampleData.getGenes()) {
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
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Gene passedGene = sampleData.getGenes().get(0);
        assertThat(passedGene.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(passedGene.getNumberOfVariants(), equalTo(1));
    }

    @Test
    public void testRunAnalysis_TwoVariantFilters_AllVariantsFail() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter);
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().isEmpty(), is(true));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(true));
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
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
    public void testRunAnalysis_TwoVariantFiltersOnePrioritiserRecessiveInheritanceFilter() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Float> hiPhiveGeneScores = new HashMap<>();
        hiPhiveGeneScores.put("GNRHR2", 0.75f);
        hiPhiveGeneScores.put("RBM8A", 0.65f);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Analysis analysis = Analysis.newBuilder()
                .vcfPath(vcfPath)
                .addStep(intervalFilter)
                .addStep(qualityFilter)
                .addStep(mockHiPhivePrioritiser)
                .addStep(inheritanceFilter)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
        
        Analysis analysis = Analysis.newBuilder()
                .vcfPath(vcfPath)
                .addStep(qualityFilter)
                .addStep(prioritiser)
                .addStep(priorityScoreFilter)
                .addStep(intervalFilter)
                .addStep(inheritanceFilter)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
    	Analysis analysis = Analysis.newBuilder()
                .vcfPath(inheritanceFilterVCFPath)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .pedPath(childAffectedPedPath)
                .build();
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));
        
        Map<String, Gene> results = makeResults(sampleData.getGenes());
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
        Analysis analysis = Analysis.newBuilder()
                .vcfPath(inheritanceFilterVCFPath)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .pedPath(twoAffectedPedPath)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));
        
        Map<String, Gene> results = makeResults(sampleData.getGenes());
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
    	Analysis analysis = Analysis.newBuilder()
                .vcfPath(inheritanceFilterVCFPath)
                .addStep(qualityFilter)
                .addStep(inheritanceFilter)
                .pedPath(childAffectedPedPath)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        SampleData sampleData = instance.run(analysis);
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));
        
        Map<String, Gene> results = makeResults(sampleData.getGenes());
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
