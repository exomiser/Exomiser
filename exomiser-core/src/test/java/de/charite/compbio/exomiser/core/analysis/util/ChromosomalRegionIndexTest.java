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

package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.SimpleVariantCoordinates;
import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.VariantCoordinates;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ChromosomalRegionIndexTest {

    private ChromosomalRegionIndex<TopologicalDomain> instance;

    private final VariantCoordinates variant = new SimpleVariantCoordinates(1, 50, "A", "T");

    private void createInstance(TopologicalDomain... tad) {
        instance = new ChromosomalRegionIndex<>(Arrays.asList(tad));
    }

    @Test
    public void testGetTadsContainingVariant_SingleTad() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, new HashMap<>());
        createInstance(tad);

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(Arrays.asList(tad)));
    }

    @Test
    public void testGetTadsContainingVariant_SingleTadVariantNotInTad() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 10, new HashMap<>());
        createInstance(tad);

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(Collections.<TopologicalDomain>emptyList()));
    }

    @Test
    public void testGetTadsContainingVariant_SingleTadVariantNotInChromosomeIndex() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 10, new HashMap<>());
        createInstance(tad);

        assertThat(instance.getRegionsContainingVariant(new SimpleVariantCoordinates(100, 50, "A", "T")), equalTo(Collections.<TopologicalDomain>emptyList()));
    }

    @Test
    public void testGetTadsContainingVariant_TwoNonOverlappingTads() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, new HashMap<>());
        TopologicalDomain tad1 = new TopologicalDomain(2, 200, 300, new HashMap<>());
        createInstance(tad, tad1);

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(Arrays.asList(tad)));
    }

    @Test
    public void testGetTadsContainingVariant_TwoOverlappingTadsVariantInBoth() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, new HashMap<>());
        TopologicalDomain tad1 = new TopologicalDomain(1, 25, 75, new HashMap<>());
        createInstance(tad, tad1);

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(Arrays.asList(tad, tad1)));
    }

    @Test
    public void testGetTadsContainingVariant_TwoOverlappingTadsVariantInOne() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, new HashMap<>());
        TopologicalDomain tad1 = new TopologicalDomain(1, 75, 200, new HashMap<>());
        createInstance(tad, tad1);

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(Arrays.asList(tad)));
    }

}