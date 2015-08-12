package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.FilterStatus;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
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
        instance = new SparseAnalysisRunner(sampleDataFactory, stubDataService);
    }

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath, new AnalysisStep[]{});

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
    public void testRunAnalysis_VariantFilterOnly() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getVariantEvaluations().size(), equalTo(1));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getVariantEvaluations().size(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().size(), equalTo(1));

        VariantEvaluation variantEvaluation = rbm8a.getPassedVariantEvaluations().get(0);
        assertThat(variantEvaluation.getChromosome(), equalTo(1));
        assertThat(variantEvaluation.getPosition(), equalTo(145508800));

    }

    @Test
    public void testRunAnalysis_TwoVariantFilters_AllVariantsShouldContainOnlyOneFailedFilterResult() {
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
        assertThat(gnrh2.getVariantEvaluations().size(), equalTo(1));
        VariantEvaluation gnrh2Variant1 = gnrh2.getVariantEvaluations().get(0);
        assertThat(gnrh2Variant1.passedFilters(), is(false));
        assertThat(gnrh2Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
        

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(false));
        assertThat(rbm8a.getVariantEvaluations().size(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(true));
        
        VariantEvaluation rbm8Variant1 = rbm8a.getVariantEvaluations().get(0);
        assertThat(rbm8Variant1.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.INTERVAL_FILTER)));
            
        VariantEvaluation rbm8Variant2 = rbm8a.getVariantEvaluations().get(1);
        assertThat(rbm8Variant2.passedFilter(FilterType.INTERVAL_FILTER), is(true));
        assertThat(rbm8Variant2.getFailedFilterTypes(), equalTo(EnumSet.of(FilterType.QUALITY_FILTER)));
    }

}
