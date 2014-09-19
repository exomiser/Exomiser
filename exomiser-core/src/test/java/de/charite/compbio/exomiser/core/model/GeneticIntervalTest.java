/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneticIntervalTest {
    
    GeneticInterval instance;
    
    private final byte CHR = 2;
    private final int START = 123;
    private final int END = 456;
    
    public GeneticIntervalTest() {
    }
    
    @Before
    public void setUp() {
        instance = new GeneticInterval(CHR, START, END);
    }

    @Test
    public void testParseStringNumericChromosome() {
        String interval = "chr3:123-456";
        GeneticInterval expResult = new GeneticInterval((byte) 3, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(interval);
        
        assertThat(result, equalTo(expResult));
        
    }
    
    @Test
    public void testParseStringChromosomeX() {
        String interval = "chrX:123-456";
        GeneticInterval expResult = new GeneticInterval((byte) 23, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(interval);
        
        assertThat(result, equalTo(expResult));
        
    }
    
    @Test
    public void testParseStringChromosomeY() {
        String interval = "chrY:123-456";
        GeneticInterval expResult = new GeneticInterval((byte) 24, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(interval);
        
        assertThat(result, equalTo(expResult));
        
    }
    
    @Test
    public void testParseStringChromosomeM() {
        String interval = "chrM:123-456";
        GeneticInterval expResult = new GeneticInterval((byte) 25, 123, 456);
        GeneticInterval result = GeneticInterval.parseString(interval);
        
        assertThat(result, equalTo(expResult));
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnMissingChromosomePrefix() {
        String interval = "3:123-456";
        GeneticInterval.parseString(interval);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnIncorrectChromosomeNumber() {
        String interval = "chr33:123-456";
        GeneticInterval.parseString(interval);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnAnotherIncorrectChromosomeNumber() {
        String interval = "chr666:123-456";
        GeneticInterval.parseString(interval);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnUndefinedStart() {
        String interval = "chr6:-456";
        GeneticInterval.parseString(interval);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorOnUndefinedEnd() {
        String interval = "chr6:123-";
        GeneticInterval.parseString(interval);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorWithIncorrectPositionDelimiter() {
        String interval = "chr6:123:456";
        GeneticInterval.parseString(interval);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseStringThrowsErrorWithSwitchedPositions() {
        String interval = "chr6:456:123";
        GeneticInterval.parseString(interval);
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
