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

package org.monarchinitiative.exomiser.data.genome.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleTest {

    @Test
    public void alleleWithNoProperties() {
        Allele instance = new Allele(1, 123435, "A", "T");
        assertThat(instance.getChr(), equalTo(1));
        assertThat(instance.getPos(), equalTo(123435));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
        assertThat(instance.getRsId(), equalTo("."));
        assertThat(instance.getValues().isEmpty(), is(true));
    }

    @Test
    public void allelesSortedNaturally() {
        Allele instance0 = new Allele(1, 123435, "A", "C");
        Allele instance1 = new Allele(1, 123435, "A", "G");
        Allele instance2 = new Allele(1, 123435, "A", "T");
        Allele instance3 = new Allele(1, 123436, "A", "T");
        Allele instance4 = new Allele(2, 123436, "A", "AT");
        Allele instance5 = new Allele(2, 123436, "A", "T");
        Allele instance6 = new Allele(2, 123436, "AA", "T");

        List<Allele> sorted = Arrays.asList(instance0, instance1, instance2, instance3, instance4, instance5, instance6);
        Collections.shuffle(sorted);
        Collections.sort(sorted);

        List<Allele> expected = Arrays.asList(instance0, instance1, instance2, instance3, instance4, instance5, instance6);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testGenerateKey() {
        Allele instance = new Allele(1, 123456, "A", "C");
        assertThat(instance.generateKey(), equalTo("1-123456-A-C"));
    }

    @Test
    public void testRsId() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.setRsId(".");
        assertThat(instance.getRsId(), equalTo("."));
    }

    @Test
    public void testGenerateInfoFieldNoRsId() {
        Allele instance = new Allele(1, 123456, "A", "C");
        assertThat(instance.generateInfoField(), equalTo(""));
    }

    @Test
    public void testGenerateInfoFieldRsIdEmpty() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.setRsId(".");
        assertThat(instance.generateInfoField(), equalTo(""));
    }

    @Test
    public void testGenerateInfoFieldWithRsId() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.setRsId("rs23456");
        assertThat(instance.generateInfoField(), equalTo("RS=rs23456"));
    }

    @Test
    public void testGenerateInfoFieldWithRsIdKgEsp() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.setRsId("rs23456");
        instance.addValue(AlleleProperty.ESP_EA, 0.03f);
        instance.addValue(AlleleProperty.KG, 0.12f);
        assertThat(instance.generateInfoField(), equalTo("RS=rs23456;KG=0.12;ESP_EA=0.03"));
    }

    @Test
    public void testAddValue() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.addValue(AlleleProperty.KG, 0.12f);
        System.out.println(instance);
        assertThat(instance.getValue(AlleleProperty.KG), equalTo(0.12f));
        assertThat(instance.getValues().size(), equalTo(1));
    }

    @Test
    public void testEquality() {
        Allele instance0 = new Allele(1, 123456, "A", "C");
        Allele instance1 = new Allele(1, 123456, "A", "C");
        assertThat(instance0, equalTo(instance1));
    }

    @Test
    public void testToString() {
        System.out.println(new Allele(1, 123435, "A", "C"));
    }
}