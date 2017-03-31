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
package org.monarchinitiative.exomiser.core.phenotype;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeTermTest {
    
    private PhenotypeTerm instance;
    
    private String id;
    private String label;

    @Before
    public void setUp() {
        id = "ID:12344";
        label = "big nose";
        instance = PhenotypeTerm.of(id, label);
    }

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(id));
    }

    @Test
    public void testGetTerm() {
        assertThat(instance.getLabel(), equalTo(label));
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
        PhenotypeTerm other = PhenotypeTerm.of(id, label);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testNotEqualsOther() {
        PhenotypeTerm other = PhenotypeTerm.of("other", label);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString().isEmpty(), is(false));
    }

    @Test
    public void testOf() {
        assertThat(PhenotypeTerm.of("id", "label"), equalTo(PhenotypeTerm.of("id", "label")));
        assertThat(PhenotypeTerm.of("id", "label").isPresent(), is(true));
        assertThat(PhenotypeTerm.of("id", "label").notPresent(), is(false));
        assertThat(PhenotypeTerm.of("otherId", "otherLabel"), not(equalTo(PhenotypeTerm.of("id", "label"))));
    }

    @Test
    public void testNotOf() {
        assertThat(PhenotypeTerm.notOf("id", "label"), equalTo(PhenotypeTerm.notOf("id", "label")));
        assertThat(PhenotypeTerm.notOf("id", "label"), not(equalTo(PhenotypeTerm.of("id", "label"))));
        assertThat(PhenotypeTerm.notOf("id", "label").isPresent(), is(false));
        assertThat(PhenotypeTerm.notOf("id", "label").notPresent(), is(true));
    }
}
