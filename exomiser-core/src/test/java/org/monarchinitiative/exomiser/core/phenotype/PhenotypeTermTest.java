/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.phenotype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeTermTest {
    
    private PhenotypeTerm instance;
    
    private String id;
    private String label;

    @BeforeEach
    public void setUp() {
        id = "ID:12344";
        label = "big nose";
        instance = PhenotypeTerm.of(id, label);
    }

    @Test
    void wontAcceptNullId() {
        assertThrows(NullPointerException.class, () -> PhenotypeTerm.of(null, "label"));
    }

    @Test
    void willAcceptNullLabel() {
        PhenotypeTerm instance = PhenotypeTerm.of("id", null);
        assertThat(instance.label(), equalTo(""));
    }

    @Test
    public void testGetId() {
        assertThat(instance.id(), equalTo(id));
    }

    @Test
    public void testGetTerm() {
        assertThat(instance.label(), equalTo(label));
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
        assertThat(PhenotypeTerm.of("otherId", "otherLabel"), not(equalTo(PhenotypeTerm.of("id", "label"))));
    }

}
