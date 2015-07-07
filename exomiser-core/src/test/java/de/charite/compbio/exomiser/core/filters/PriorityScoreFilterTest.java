/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import java.util.Objects;
import static org.hamcrest.CoreMatchers.equalTo;
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
        PriorityResult priorityResult = new GenericPriorityResult(PriorityType.OMIM_PRIORITY, minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreSameAsThreshold() {
        PriorityResult priorityResult = new GenericPriorityResult(priorityType, minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreOverThreshold() {
        PriorityResult priorityResult = new GenericPriorityResult(priorityType, minPriorityScore + 0.2f);
        gene.addPriorityResult(priorityResult);

        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_FailsGeneWithCorrectPriorityType_ScoreUnderThreshold() {
        PriorityResult priorityResult = new GenericPriorityResult(priorityType, minPriorityScore - 0.2f);
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

    private class GenericPriorityResult implements PriorityResult {

        private final PriorityType priorityType;
        private final float priorityScore;

        GenericPriorityResult(PriorityType PriorityType, float priorityScore) {
            this.priorityType = PriorityType;
            this.priorityScore = priorityScore;
        } 
                       
        @Override
        public PriorityType getPriorityType() {
            return priorityType;
        }

        @Override
        public String getHTMLCode() {
            return "Not implemented here";
        }

        @Override
        public float getScore() {
            return priorityScore;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.priorityType);
            hash = 47 * hash + Float.floatToIntBits(this.priorityScore);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GenericPriorityResult other = (GenericPriorityResult) obj;
            if (this.priorityType != other.priorityType) {
                return false;
            }
            if (Float.floatToIntBits(this.priorityScore) != Float.floatToIntBits(other.priorityScore)) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return "MockPriorityResult{" + "priorityType=" + priorityType + ", priorityScore=" + priorityScore + '}';
        }
        
    }
    
}

