/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

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
public class PhenotypeTermTest {
    
    private PhenotypeTerm instance;
    
    private String id;
    private String term;
    private double ic;
    
    @Before
    public void setUp() {
        id = "ID:12344";
        term = "big nose";
        ic = 2.00d;
        
        instance = new PhenotypeTerm(id, term, ic);
    }

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(id));
    }

    @Test
    public void testGetTerm() {
        assertThat(instance.getTerm(), equalTo(term));
    }

    @Test
    public void testGetIc() {
        assertThat(instance.getIc(), equalTo(ic));
    }

    @Test
    public void testHashCode() {
        assertThat(instance.hashCode(), equalTo(instance.hashCode()));
    }
    
    @Test
    public void testEquals() {
        assertThat(instance, equalTo(instance));
    }

    @Test
    public void testEqualsOther() {
        PhenotypeTerm other = new PhenotypeTerm(id, term, ic);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testNotEqualsOther() {
        PhenotypeTerm other = new PhenotypeTerm("wibble", term, ic);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString().isEmpty(), is(false));
    }
    
}
