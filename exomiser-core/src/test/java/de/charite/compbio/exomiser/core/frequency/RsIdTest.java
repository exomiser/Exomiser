/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.frequency;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RsIdTest {
    
    RsId instance;
    
    private static final int ID = 234567364;
    
    public RsIdTest() {
    }
    
    @Before
    public void setUp() {
        instance = new RsId(ID);
    }

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(ID));
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        RsId other = new RsId(ID);
        int expected = other.hashCode();
        assertThat(instance.hashCode(), equalTo(expected));

    }

    @Test
    public void testEqualsNotNull() {
        Object obj = null;
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testEqualsNotSomethingElse() {
        Object obj = "1335464574";
        assertThat(instance.equals(obj), is(false));
    }
    
    @Test
    public void testEquals() {
        RsId obj = new RsId(ID);
        assertThat(instance.equals(obj), is(true));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("rsId" + ID));
    }
    
}
