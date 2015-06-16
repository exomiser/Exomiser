/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenePriorityScoreFilterTest {

    private GenePriorityScoreFilter instance;

    private final float minPriorityScore = 0.8f;

    private Gene gene;

    @Before
    public void setUp() {
        instance = new GenePriorityScoreFilter(minPriorityScore);
        
        gene = new Gene("GENE1", 12345);
    }

    private void assertPasses(FilterResult result) {
        assertThat(result.passedFilter(), is(true));
        assertThat(result.getScore(), equalTo(1f));
        assertThat(result.getResultStatus(), equalTo(FilterResultStatus.PASS));
        assertThat(result.getFilterType(), equalTo(instance.getFilterType()));
    }
    
    private void assertFails(FilterResult result) {
        assertThat(result.passedFilter(), is(false));
        assertThat(result.getScore(), equalTo(0f));
        assertThat(result.getResultStatus(), equalTo(FilterResultStatus.FAIL));
        assertThat(result.getFilterType(), equalTo(instance.getFilterType()));
    }
    
    @Test
    public void testGetFilterType() {
        instance = new GenePriorityScoreFilter(0f);
        assertThat(instance.getFilterType(), equalTo(FilterType.PRIORITY_SCORE_FILTER));
    }

    @Test
    public void testRunFilter_PassesGeneWithPriorityScoreSameAsThreshold() {
        PriorityResult priorityResult = new PhivePriorityResult(null, null, minPriorityScore);
        gene.addPriorityResult(priorityResult);
        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithPriorityScoreOverThreshold() {
        PriorityResult priorityResult = new PhivePriorityResult(null, null, minPriorityScore + 0.2f);
        gene.addPriorityResult(priorityResult);
        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_FailsGeneWithPriorityScoreUnderThreshold() {
        PriorityResult priorityResult = new PhivePriorityResult(null, null, minPriorityScore - 0.2f);
        gene.addPriorityResult(priorityResult);
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }

    @Test
    public void testRunFilter_FailsGeneWithNoSetPriorityScore() {
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    
    @Test
    public void testHashCode() {
        GenePriorityScoreFilter other = new GenePriorityScoreFilter(minPriorityScore);

        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GenePriorityScoreFilter other = new GenePriorityScoreFilter(minPriorityScore);

        assertThat(instance.equals(other), is(true));

    }

    @Test
    public void testNotEquals() {
        GenePriorityScoreFilter other = new GenePriorityScoreFilter(minPriorityScore + .03f);

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
        assertThat(instance.toString(), equalTo("Gene priority score filter: minPriorityScore=0.8"));
    }

}
