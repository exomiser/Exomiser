/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
    
    private static final byte RIGHT_CHR = 2;
    private static final byte WRONG_CHR = 3;
    private static final int START_REGION = 12345;
    private static final int END_REGION = 67890;
    
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
        Variant rightChromosomeRightPositionVariant = new Variant(RIGHT_CHR, INSIDE_REGION, "A", "T", null, 3, "");
        rightChromosomeRightPosition = new VariantEvaluation(rightChromosomeRightPositionVariant);
    
        Variant rightChromosomeWrongPositionVariant = new Variant(RIGHT_CHR, BEFORE_REGION, "T", "A", null, 3, "");
        rightChromosomeWrongPosition = new VariantEvaluation(rightChromosomeWrongPositionVariant);
        
        Variant wrongChromosomeRightPositionVariant = new Variant(WRONG_CHR, INSIDE_REGION, "C", "T", null, 3, "");
        wrongChromosomeRightPosition = new VariantEvaluation(wrongChromosomeRightPositionVariant);
        
        Variant wrongChromosomeWrongPositionVariant = new Variant(WRONG_CHR, AFTER_REGION, "A", "G", null, 3, "");
        wrongChromosomeWrongPosition = new VariantEvaluation(wrongChromosomeWrongPositionVariant);
    }
    
    @Before
    public void setUp() {
        instance = new IntervalFilter(SEARCH_INTERVAL);
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
        assertThat(instance.equals(obj), is (false));
    }
    
    @Test
    public void testNotEqualsIntervalDifferent() {
        IntervalFilter otherFilter = new IntervalFilter(GeneticInterval.parseString("chr3:12334-67850"));
        assertThat(instance.equals(otherFilter), is (false));
    }
    
    @Test
    public void testIsEquals() {
        IntervalFilter otherFilter = new IntervalFilter(SEARCH_INTERVAL);
        assertThat(instance.equals(otherFilter), is (true));
    }
    
    @Test
    public void testToString() {
        String expected = String.format("%s filter chromosome=%d, from=%d, to=%d, interval=%s", instance.getFilterType(), RIGHT_CHR, START_REGION, END_REGION, SEARCH_INTERVAL);
        assertThat(instance.toString(), equalTo(expected));
    }
    
}
