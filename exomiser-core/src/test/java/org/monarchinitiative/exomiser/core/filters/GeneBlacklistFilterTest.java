package org.monarchinitiative.exomiser.core.filters;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.filters.FilterType.GENE_BLACKLIST_FILTER;

public class GeneBlacklistFilterTest {


    private VariantEvaluation variantWithGeneSymbol(String geneSymbol) {
        return TestFactory.variantBuilder(1,1,"A", "T").geneSymbol(geneSymbol).build();
    }

    @Test
    public void testGetFilterType() {
        GeneBlacklistFilter instance = new GeneBlacklistFilter();
        assertThat(instance.getFilterType(), equalTo(GENE_BLACKLIST_FILTER));
    }

    @Test
    public void testFilterPassesWhenNoBlacklistedGeneInsideAnalysis() {
        GeneBlacklistFilter instance = new GeneBlacklistFilter();
        FilterResult filterResult = instance.runFilter(variantWithGeneSymbol("DEF2"));

        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testFilterFailsWhenBlacklistedGeneInsideAnalysis() {
        VariantEvaluation failVariant = variantWithGeneSymbol("LINC02081");
        GeneBlacklistFilter instance = new GeneBlacklistFilter();
        FilterResult filterResult = instance.runFilter(failVariant);

        FilterTestHelper.assertFailed(filterResult);
    }
    @Test
    public void testFilterPassesWhenNoGeneInsideAnalysis() {
        VariantEvaluation emptyVariant = variantWithGeneSymbol("");
        GeneBlacklistFilter instance = new GeneBlacklistFilter();
        FilterResult filterResult = instance.runFilter(emptyVariant);
        FilterTestHelper.assertPassed(filterResult);
    }

    @Test
    public void testEquals() {
        GeneBlacklistFilter instance = new GeneBlacklistFilter();
        GeneBlacklistFilter instance2 = new GeneBlacklistFilter();

        assertThat(instance, equalTo(instance2));
    }
}