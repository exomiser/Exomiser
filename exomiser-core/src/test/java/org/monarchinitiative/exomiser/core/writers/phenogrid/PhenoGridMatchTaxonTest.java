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
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

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
