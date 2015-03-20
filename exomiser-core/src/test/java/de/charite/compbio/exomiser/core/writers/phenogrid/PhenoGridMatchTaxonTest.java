/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchTaxonTest {
    
    private PhenoGridMatchTaxon instance;
    
    private String id;
    private String label;
    
    @Before
    public void setUp() {
        id = "9606";
        label = "Homo sapiens";
        instance = new PhenoGridMatchTaxon(id, label);
    }

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(id));
    }

    @Test
    public void testGetLabel() {
        assertThat(instance.getLabel(), equalTo(label));
    }

    @Test
    public void testHashCode() {
        PhenoGridMatchTaxon other = new PhenoGridMatchTaxon(id, label);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        PhenoGridMatchTaxon other = new PhenoGridMatchTaxon(id, label);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testNotEqualsOther() {
        PhenoGridMatchTaxon other = new PhenoGridMatchTaxon("10090", "Mus musculus");
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testNotEqualsNull() {
        assertThat(instance, not(equalTo(null)));
    }
    
    @Test
    public void testNotEqualsOtherClass() {
        String other = "Homo sapiens";
        assertThat(instance.equals(other), is(false));
    }
}
