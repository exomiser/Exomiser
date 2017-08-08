/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneticIntervalTest {

    GeneticInterval instance;

    private final int CHR = 2;
    private final int START = 123;
    private final int END = 456;

    ReferenceDictionary refDict;

    public GeneticIntervalTest() {
    }

    @Before
    public void setUp() {
        instance = new GeneticInterval(CHR, START, END);
        refDict = HG19RefDictBuilder.build();
    }

    @Test
    public void testParseStringNumericChromosomeSingleDigit() {
        String interval = "chr3:123-456";
        GeneticInterval expResult = new GeneticInterval(3, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(refDict, interval);

        assertThat(result, equalTo(expResult));

    }

    @Test
    public void testParseStringNumericChromosomeDoubleDigit() {
        String interval = "chr14:123-456";
        GeneticInterval expResult = new GeneticInterval(14, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(refDict, interval);

        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testParseStringChromosomeX() {
        String interval = "chrX:123-456";
        GeneticInterval expResult = new GeneticInterval(23, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(refDict, interval);

        assertThat(result, equalTo(expResult));

    }

    @Test
    public void testParseStringChromosomeY() {
        String interval = "chrY:123-456";
        GeneticInterval expResult = new GeneticInterval(24, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(refDict, interval);

        assertThat(result, equalTo(expResult));

    }

    @Test
    public void testParseStringChromosomeM() {
        String interval = "chrM:123-456";
        GeneticInterval expResult = new GeneticInterval(25, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(refDict, interval);

        assertThat(result, equalTo(expResult));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnMissingChromosomePrefix() {
        String interval = "3:123-456";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnIncorrectChromosomeNumber() {
        String interval = "chr33:123-456";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnAnotherIncorrectChromosomeNumber() {
        String interval = "chr666:123-456";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnUndefinedStart() {
        String interval = "chr6:-456";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnUndefinedEnd() {
        String interval = "chr6:123-";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorWithIncorrectPositionDelimiter() {
        String interval = "chr6:123:456";
        GeneticInterval.parseString(refDict, interval);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorWithSwitchedPositions() {
        String interval = "chr6:456:123";
        GeneticInterval.parseString(refDict, interval);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorThrowsErrorWhenEndDefinedBeforeStart() {
        GeneticInterval interval = new GeneticInterval(CHR, END, START);
    }

    @Test
    public void testGetChromosome() {
        assertThat(instance.getChromosome(), equalTo(CHR));
    }

    @Test
    public void testGetStart() {
        assertThat(instance.getStart(), equalTo(START));
    }

    @Test
    public void testGetEnd() {
        assertThat(instance.getEnd(), equalTo(END));
    }

    @Test
    public void testHashCode() {
        int expResult = new GeneticInterval(CHR, START, END).hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        GeneticInterval other = new GeneticInterval(CHR, START, END);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testObjectNotEquals() {
        String other = "wibble";
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNullNotEquals() {
        assertThat(instance.equals(null), is(false));
    }

    @Test
    public void testChrNotEquals() {
        GeneticInterval other = new GeneticInterval((byte) 4, START, END);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testStartNotEquals() {
        GeneticInterval other = new GeneticInterval(CHR, 345, END);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testEndNotEquals() {
        GeneticInterval other = new GeneticInterval(CHR, START, 124);
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("chr2:123-456"));
    }

}
