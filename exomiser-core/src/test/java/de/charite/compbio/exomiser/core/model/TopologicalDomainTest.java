/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package de.charite.compbio.exomiser.core.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TopologicalDomainTest {

    private TopologicalDomain instance;

    @Test
    public void testGetChromosome() {
        instance = new TopologicalDomain(1, 0, 0, new HashMap<>());
        assertThat(instance.getChromosome(), equalTo(1));
    }

    @Test
    public void testGetStart() {
        instance = new TopologicalDomain(0, 1, 0, new HashMap<>());
        assertThat(instance.getStart(), equalTo(1));
    }

    @Test
    public void testGetEnd() {
        instance = new TopologicalDomain(0, 0, 1, new HashMap<>());
        assertThat(instance.getEnd(), equalTo(1));
    }

    @Test
    public void testGetGenes() {
        Map<String, Integer> genes = new HashMap<>();
        genes.put("GENE1", 12345);
        genes.put("GENE2", 23456);
        instance = new TopologicalDomain(0, 0, 0, genes);
        assertThat(instance.getGenes(), equalTo(genes));
    }

    @Test
    public void testVariantIsWithinDomain() {
        VariantCoordinates variant = new SimpleVariantCoordinates(1, 5, "A", "T");
        instance = new TopologicalDomain(1, 1, 10, new HashMap<>());
        assertThat(instance.containsPosition(variant), is(true));
    }

    @Test
    public void testVariantIsOutsideDomain() {
        VariantCoordinates variant = new SimpleVariantCoordinates(1, 5, "A", "T");
        instance = new TopologicalDomain(1, 1000, 10000, new HashMap<>());
        assertThat(instance.containsPosition(variant), is(false));
    }

    @Test
    public void testToString() {
        Map<String, Integer> genes = new HashMap<>();
        genes.put("GENE1", 12345);
        genes.put("GENE2", 23456);
        instance = new TopologicalDomain(1, 987, 123456, genes);
        System.out.println(instance);
    }

    private class SimpleVariantCoordinates implements VariantCoordinates {

        private final int chr;
        private final int pos;
        private final String ref;
        private final String alt;

        public SimpleVariantCoordinates(int chr, int pos, String ref, String alt) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
        }

        @Override
        public int getChromosome() {
            return chr;
        }

        @Override
        public String getChromosomeName() {
            switch (chr) {
                case 23:
                    return "X";
                case 24:
                    return "Y";
                case 25:
                    return "M";
                default:
                    return String.valueOf(chr);
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public String getRef() {
            return ref;
        }

        @Override
        public String getAlt() {
            return alt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleVariantCoordinates that = (SimpleVariantCoordinates) o;

            if (chr != that.chr) return false;
            if (pos != that.pos) return false;
            if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
            return !(alt != null ? !alt.equals(that.alt) : that.alt != null);

        }

        @Override
        public int hashCode() {
            int result = chr;
            result = 31 * result + pos;
            result = 31 * result + (ref != null ? ref.hashCode() : 0);
            result = 31 * result + (alt != null ? alt.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SimpleVariantCoordinates{" +
                    "chr=" + chr +
                    ", pos=" + pos +
                    ", ref='" + ref + '\'' +
                    ", alt='" + alt + '\'' +
                    '}';
        }
    }
}
