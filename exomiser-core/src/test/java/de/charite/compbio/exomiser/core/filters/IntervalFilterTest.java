/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class IntervalFilterTest {
    
    IntervalFilter instance;
    
    private static final byte RIGHT_CHR = 7;
    private static final byte WRONG_CHR = 3;
    private static final int START_REGION = 155595590;
    private static final int END_REGION = 155604810;
    
    private static final int INSIDE_REGION = START_REGION + 20;
    private static final int BEFORE_REGION = START_REGION - 20;
    private static final int AFTER_REGION = END_REGION + 20;

    
    private static final GeneticInterval SEARCH_INTERVAL = new GeneticInterval(RIGHT_CHR, START_REGION, END_REGION);

        
    private VariantEvaluation rightChromosomeRightPosition;
    private VariantEvaluation rightChromosomeWrongPosition;
    private VariantEvaluation wrongChromosomeRightPosition;
    private VariantEvaluation wrongChromosomeWrongPosition;
    
    public IntervalFilterTest() {
        setUpVariants();
    }

    private void setUpVariants() {
        rightChromosomeRightPosition = new VariantEvaluation.VariantBuilder(RIGHT_CHR, INSIDE_REGION, "A", "T").build();
        rightChromosomeWrongPosition = new VariantEvaluation.VariantBuilder(RIGHT_CHR, BEFORE_REGION, "A", "T").build();
        wrongChromosomeRightPosition = new VariantEvaluation.VariantBuilder(WRONG_CHR, INSIDE_REGION, "A", "T").build();
        wrongChromosomeWrongPosition = new VariantEvaluation.VariantBuilder(RIGHT_CHR, AFTER_REGION, "A", "T").build();
    }

    @Before
    public void setUp() {
        instance = new IntervalFilter(SEARCH_INTERVAL);
    }

    @Test
    public void testGetInterval() {
        assertThat(instance.getInterval(), equalTo(SEARCH_INTERVAL));
    }
    
    @Test
    public void testThatRightChromosomeRightPositionPassesFilter() {
        FilterResult filterResult = instance.runFilter(rightChromosomeRightPosition);
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }

    @Test
    public void testThatRightChromosomeWrongPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(rightChromosomeWrongPosition);
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testThatWrongChromosomeRightPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(wrongChromosomeRightPosition);
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testThatWrongChromosomeWrongPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(wrongChromosomeWrongPosition);
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.INTERVAL_FILTER));
    }

    @Test
    public void testHashCode() {
        IntervalFilter otherFilter = new IntervalFilter(SEARCH_INTERVAL);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testNotEquals() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }

    @Test
    public void testNotEqualsIntervalDifferent() {
        IntervalFilter otherFilter = new IntervalFilter(GeneticInterval.parseString(HG19RefDictBuilder.build(),
                "chr3:12334-67850"));
        assertThat(instance.equals(otherFilter), is(false));
    }
    
    @Test
    public void testIsEquals() {
        IntervalFilter otherFilter = new IntervalFilter(SEARCH_INTERVAL);
        assertThat(instance.equals(otherFilter), is(true));
    }
    
    @Test
    public void testToString() {
        String expected = String.format("%s filter chromosome=%d, from=%d, to=%d, interval=%s",
                instance.getFilterType(), RIGHT_CHR, START_REGION, END_REGION, SEARCH_INTERVAL);
        assertThat(instance.toString(), equalTo(expected));
    }
    
}
