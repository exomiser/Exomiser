/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.GenomicVariant;

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
        assertThat(instance.contigId(), equalTo(1));
    }

    @Test
    public void testGetStart() {
        instance = new TopologicalDomain(0, 1, 0, new HashMap<>());
        assertThat(instance.start(), equalTo(1));
    }

    @Test
    public void testGetEnd() {
        instance = new TopologicalDomain(0, 0, 1, new HashMap<>());
        assertThat(instance.end(), equalTo(1));
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
        GenomicVariant variant = SimpleVariantCoordinates.of(1, 5, "A", "T");
        instance = new TopologicalDomain(1, 1, 10, new HashMap<>());
        assertThat(instance.containsPosition(variant), is(true));
    }

    @Test
    public void testVariantIsOutsideDomain() {
        GenomicVariant variant = SimpleVariantCoordinates.of(1, 5, "A", "T");
        instance = new TopologicalDomain(1, 1000, 10000, new HashMap<>());
        assertThat(instance.containsPosition(variant), is(false));
    }

}
