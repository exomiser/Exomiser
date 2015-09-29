package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.FilterStatus;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.MockPrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseAnalysisRunnerTest extends AnalysisRunnerTestBase {
    
    private SparseAnalysisRunner instance;
        
    @Before
    public void setUp() {
        instance = new SparseAnalysisRunner(sampleDataFactory);
    }

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath);

        instance.runAnalysis(analysis);
        
        SampleData sampleData = analysis.getSampleData();
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
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().size(), equalTo(1));

        VariantEvaluation variantEvaluation = rbm8a.getPassedVariantEvaluations().get(0);
        assertThat(variantEvaluation.getChromosome(), equalTo(1));
        assertThat(variantEvaluation.getPosition(), equalTo(145508800));

    }

    @Test
    public void testRunAnalysis_TwoVariantFilters_AllVariantsFail_VariantsShouldContainOnlyOneFailedFilterResult() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
        

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(false));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(true));
        
        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.QUALITY_FILTER)));
    }
    
    @Test
    public void testRunAnalysis_TwoVariantFiltersOnePrioritiser_VariantsShouldContainOnlyOneFailedFilterResult() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Float> hiPhiveGeneScores = new HashMap<>();
        hiPhiveGeneScores.put("GNRHR2", 0.75f);
        hiPhiveGeneScores.put("RBM8A", 0.65f);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter, mockHiPhivePrioritiser, inheritanceFilter);
        analysis.setModeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
    }
    
    @Test
    public void testRunAnalysis_TwoVariantFiltersOnePrioritiserRecessiveInheritanceFilter_VariantsShouldContainOnlyOneFailedFilterResult() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(120);
        Map<String, Float> hiPhiveGeneScores = new HashMap<>();
        hiPhiveGeneScores.put("GNRHR2", 0.75f);
        hiPhiveGeneScores.put("RBM8A", 0.65f);
        Prioritiser mockHiPhivePrioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, hiPhiveGeneScores);
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter, mockHiPhivePrioritiser, inheritanceFilter);
        analysis.setModeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
        assertThat(rbm8Variant1.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
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
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFilterStatus(), equalTo(FilterStatus.FAILED));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), hasItem(FilterType.PRIORITY_SCORE_FILTER));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(false));
        assertThat(rbm8a.getEntrezGeneID(), equalTo(9939));
        assertThat(rbm8a.getPriorityScore(), equalTo(desiredPrioritiserScore));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(true));
        assertThat(rbm8Variant1.getFilterStatus(), equalTo(FilterStatus.PASSED));
        assertThat(rbm8Variant1.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.getFilterStatus(), equalTo(FilterStatus.PASSED));
        assertThat(rbm8Variant2.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(true));
    }

    @Test
    public void testRunAnalysis_PrioritiserPriorityScoreFilterVariantFilter() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        Prioritiser prioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

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
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getPosition(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(rbm8a.getGeneSymbol()));
    }

    @Test
    public void testRunAnalysis_VariantFilterPrioritiserPriorityScoreFilterVariantFilter() {
        Float desiredPrioritiserScore = 0.9f;
        Map<String, Float> geneSymbolPrioritiserScores = new HashMap<>();
        geneSymbolPrioritiserScores.put("RBM8A", desiredPrioritiserScore);

        VariantFilter qualityFilter = new QualityFilter(120);
        Prioritiser prioritiser = new MockPrioritiser(PriorityType.HIPHIVE_PRIORITY, geneSymbolPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, desiredPrioritiserScore - 0.1f);
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        InheritanceFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        Analysis analysis = makeAnalysis(vcfPath, qualityFilter, prioritiser, priorityScoreFilter, intervalFilter, inheritanceFilter);
        analysis.setModeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        instance.runAnalysis(analysis);

        //TODO: remove all this repetitive cruft into common method
        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getNumberOfVariants(), equalTo(1));

        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.PRIORITY_SCORE_FILTER)));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getEntrezGeneID(), equalTo(9939));
        assertThat(rbm8a.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(rbm8a.getPriorityScore(), equalTo(desiredPrioritiserScore));
        assertThat(rbm8a.getNumberOfVariants(), equalTo(2));

        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.passedFilters(), is(false));
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.QUALITY_FILTER)));
        assertThat(rbm8Variant1.passedFilter(FilterType.INHERITANCE_FILTER), is(true));

        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilters(), is(true));
        assertThat(rbm8Variant2.getChromosome(), equalTo(1));
        assertThat(rbm8Variant2.getPosition(), equalTo(145508800));
        assertThat(rbm8Variant2.getGeneSymbol(), equalTo(rbm8a.getGeneSymbol()));
        assertThat(rbm8Variant2.passedFilter(FilterType.QUALITY_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
    }

}
