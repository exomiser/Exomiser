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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.MockPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(result.passed(), is(true));
        assertThat(result.getFilterType(), equalTo(instance.getFilterType()));
    }
    
    private void assertFails(FilterResult result) {
        assertThat(result.failed(), is(true));
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
        PriorityResult priorityResult = new MockPriorityResult(PriorityType.OMIM_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreSameAsThreshold() {
        PriorityResult priorityResult = makeDefaultPriorityResult(minPriorityScore);
        gene.addPriorityResult(priorityResult);
        
        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_PassesGeneWithCorrectPriorityType_ScoreOverThreshold() {
        PriorityResult priorityResult = makeDefaultPriorityResult(minPriorityScore + 0.2f);
        gene.addPriorityResult(priorityResult);

        FilterResult result = instance.runFilter(gene);
        
        assertPasses(result);
    }
    
    @Test
    public void testRunFilter_FailsGeneWithCorrectPriorityType_ScoreUnderThreshold() {
        PriorityResult priorityResult = makeDefaultPriorityResult(minPriorityScore - 0.2f);
        gene.addPriorityResult(priorityResult);

        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
    }
    
    @Test
    public void testRunFilter_VariantInFailedGeneAlsoFailsTheFilter() {
        PriorityResult priorityResult = makeDefaultPriorityResult(minPriorityScore - 0.2f);
        gene.addPriorityResult(priorityResult);
        VariantEvaluation variant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        gene.addVariant(variant);
        
        FilterResult result = instance.runFilter(gene);
        
        assertFails(result);
        assertThat(variant.passedFilter(FilterType.PRIORITY_SCORE_FILTER), is(false));
        assertThat(variant.getFailedFilterTypes(), hasItem(FilterType.PRIORITY_SCORE_FILTER));
    }

    private PriorityResult makeDefaultPriorityResult(float score) {
        return new MockPriorityResult(priorityType, gene.getEntrezGeneID(), gene.getGeneSymbol(), score);
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

