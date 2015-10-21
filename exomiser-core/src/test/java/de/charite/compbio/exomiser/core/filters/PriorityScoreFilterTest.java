/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.BasePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PriorityScoreFilterTest {

    private PriorityScoreFilter instance;

    private final PriorityType priorityType = PriorityType.PHIVE_PRIORITY;
    private final float minPriorityScore = 0.8f;

    private Gene gene;

    @Before
    public void setUp() {
        instance = new PriorityScoreFilter(priorityType, minPriorityScore);
        gene = new Gene("GENE1", 12345);
    }

    private void assertPasses(FilterResult result) {
        assertThat(result.passedFilter(), is(true));
        assertThat(result.getResultStatus(), equalTo(FilterResultStatus.PASS));
        assertThat(result.getFilterType(), equalTo(instance.getFilterType()));
    }
    
    private void assertFails(FilterResult result) {
        assertThat(result.passedFilter(), is(false));
        assertThat(result.getResultStatus(), equalTo(FilterResultStatus.FAIL));
        assertThat(result.getFilterType(), equalTo(instance.getFilterType()));
    }
     
    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(priorityType));
    }
    
    @Test
    public void testGetMinPriorityScore() {
        assertThat(instance.getMinPriorityScore(), equalTo(minPriorityScore));
    }
    
    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.PRIORITY_SCORE_FILTER));
    }

    @Test
    public void testRunFilter_FailsGeneWithWrongPriorityType_ScoreSameAsThreshold() {
        PriorityResult priorityResult = new BasePriorityResult(PriorityType.OMIM_PRIORITY, minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreSameAsThreshold() {
        PriorityResult priorityResult = new BasePriorityResult(priorityType, minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreOverThreshold() {
        PriorityResult priorityResult = new BasePriorityResult(priorityType, minPriorityScore + 0.2f);
        gene.addPriorityResult(priorityResult);

        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_FailsGeneWithCorrectPriorityType_ScoreUnderThreshold() {
        PriorityResult priorityResult = new BasePriorityResult(priorityType, minPriorityScore - 0.2f);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    @Test
    public void testRunFilter_VariantInFailedGeneAlsoFailsTheFilter() {
        PriorityResult priorityResult = new BasePriorityResult(priorityType, minPriorityScore - 0.2f);
        gene.addPriorityResult(priorityResult);
        VariantEvaluation variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        gene.addVariant(variant);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
        assertThat(variant.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
        assertThat(variant.getFailedFilterTypes(), hasItem(FilterType.PRIORITY_SCORE_FILTER));
    }

    @Test
    public void testRunFilter_FailsGeneWithNoSetPriorityScore() {
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    
    @Test
    public void testHashCode() {
        PriorityScoreFilter other = new PriorityScoreFilter(priorityType, minPriorityScore);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        PriorityScoreFilter other = new PriorityScoreFilter(priorityType, minPriorityScore);
        assertThat(instance.equals(other), is(true));

    }

    @Test
    public void testNotEquals_differentScore() {
        PriorityScoreFilter other = new PriorityScoreFilter(priorityType, minPriorityScore + .03f);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEquals_differentPrioritiser() {
        PriorityScoreFilter other = new PriorityScoreFilter(PriorityType.NONE, minPriorityScore);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualOtherObject() {
        String string = "string";
        assertThat(instance.equals(string), is(false));
        assertThat(string.equals(instance), is(false));

    }
    
    @Test
    public void testNotEqualNull() {
        Object object = null;
        assertThat(instance.equals(object), is(false));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("PriorityScoreFilter{priorityType=PHIVE_PRIORITY, minPriorityScore=0.8}"));
    }

}

