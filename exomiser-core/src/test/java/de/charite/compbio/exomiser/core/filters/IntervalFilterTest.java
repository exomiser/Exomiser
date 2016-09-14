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

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.junit.Before;
import org.junit.Test;

import static de.charite.compbio.exomiser.core.filters.FilterTestHelper.assertFailed;
import static de.charite.compbio.exomiser.core.filters.FilterTestHelper.assertPassed;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat(instance.getGeneticInterval(), equalTo(SEARCH_INTERVAL));
    }
    
    @Test
    public void testThatRightChromosomeRightPositionPassesFilter() {
        FilterResult filterResult = instance.runFilter(rightChromosomeRightPosition);
        assertPassed(filterResult);
    }

    @Test
    public void testThatRightChromosomeWrongPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(rightChromosomeWrongPosition);
        assertFailed(filterResult);
    }

    @Test
    public void testThatWrongChromosomeRightPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(wrongChromosomeRightPosition);
        assertFailed(filterResult);
    }

    @Test
    public void testThatWrongChromosomeWrongPositionFailsFilter() {
        FilterResult filterResult = instance.runFilter(wrongChromosomeWrongPosition);
        assertFailed(filterResult);
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
