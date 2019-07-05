/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.*;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ChromosomalRegionIndexTest {

    private final VariantCoordinates variant = new SimpleVariantCoordinates(GenomeAssembly.HG19, 1, 50, "A", "T");

    @Test
    public void empty() {
        ChromosomalRegionIndex<TopologicalDomain> emptyTopoDomainIndex = ChromosomalRegionIndex.empty();
        ChromosomalRegionIndex<RegulatoryFeature> emptyRegulatoryFeatureIndex = ChromosomalRegionIndex.empty();

        assertThat(emptyTopoDomainIndex.getRegionsContainingVariant(variant), equalTo(ImmutableList.of()));
        assertThat(emptyRegulatoryFeatureIndex.getRegionsContainingVariant(variant), equalTo(ImmutableList.of()));

        assertThat(emptyRegulatoryFeatureIndex.hasRegionContainingVariant(variant), is(false));
        assertThat(emptyTopoDomainIndex.hasRegionContainingVariant(variant), is(false));
    }

    @Test
    public void emptyListIntoOfConstructor() {
        ChromosomalRegionIndex<RegulatoryFeature> empty = ChromosomalRegionIndex.of(ImmutableList.of());
    }

    @Test
    public void multipleRegionsInChromosomes() {
        ChromosomalRegion region1 = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        ChromosomalRegion region2 = new TopologicalDomain(1, 150, 300, ImmutableMap.of());
        ChromosomalRegion region3 = new TopologicalDomain(1, 250, 350, ImmutableMap.of());
        ChromosomalRegion region4 = new TopologicalDomain(1, 400, 500, ImmutableMap.of());
        ChromosomalRegion region5 = new TopologicalDomain(2, 600, 800, ImmutableMap.of());

        ImmutableList<ChromosomalRegion> chromosomalRegions = ImmutableList.of(region1, region2, region3, region4, region5);

        ChromosomalRegionIndex<ChromosomalRegion> instance = ChromosomalRegionIndex.of(chromosomalRegions);

        assertThat(instance.size(), equalTo(chromosomalRegions.size()));
    }

    @Test
    public void testGetTadsContainingVariantSingleTad() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(ImmutableList.of(tad)));
        assertThat(instance.hasRegionContainingVariant(variant), is(true));
    }

    @Test
    public void testGetTadsContainingVariantSingleTadVariantNotInTad() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 10, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(ImmutableList.of()));
        assertThat(instance.hasRegionContainingVariant(variant), is(false));
        assertThat(instance.hasRegionContainingPosition(variant.getChromosome(), variant.getStart()), is(false));
    }

    @Test
    public void testRegionContainingPositionSingleTad() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.hasRegionContainingPosition(1, 2), is(true));
        assertThat(instance.hasRegionContainingPosition(2, 2), is(false));
    }

    @Test
    public void testGetTadsContainingVariantSingleTadVariantNotInChromosomeIndex() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 10, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsContainingVariant(new SimpleVariantCoordinates(GenomeAssembly.HG19, 100, 50, "A", "T")), equalTo(Collections
                .emptyList()));
    }

    @Test
    public void testGetTadsContainingPositionPositionOneBeforeStartOfRegion() {
        TopologicalDomain tad = new TopologicalDomain(1, 10, 12, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsOverlappingPosition(1, 9), equalTo(ImmutableList.of()));
    }

    @Test
    public void testGetTadsContainingPositionPositionAtStartOfRegion() {
        TopologicalDomain tad = new TopologicalDomain(1, 10, 12, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsOverlappingPosition(1, 10), equalTo(ImmutableList.of(tad)));
    }

    @Test
    public void testGetTadsContainingPositionPositionMiddleOfRegion() {
        TopologicalDomain tad = new TopologicalDomain(1, 10, 12, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsOverlappingPosition(1, 11), equalTo(ImmutableList.of(tad)));
    }

    @Test
    public void testGetTadsContainingPositionPositionAtEndOfRegion() {
        TopologicalDomain tad = new TopologicalDomain(1, 10, 12, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsOverlappingPosition(1, 12), equalTo(ImmutableList.of(tad)));
    }

    @Test
    public void testGetTadsContainingPositionPositionOneAfterEndOfRegion() {
        TopologicalDomain tad = new TopologicalDomain(1, 10, 12, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad));

        assertThat(instance.getRegionsOverlappingPosition(1, 13), equalTo(ImmutableList.of()));
    }

    @Test
    public void testGetTadsContainingVariantTwoNonOverlappingTads() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        TopologicalDomain tad1 = new TopologicalDomain(2, 200, 300, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad, tad1));

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(ImmutableList.of(tad)));
    }

    @Test
    public void testGetTadsContainingVariantTwoOverlappingTadsVariantInBoth() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        TopologicalDomain tad1 = new TopologicalDomain(1, 25, 75, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad, tad1));

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(ImmutableList.of(tad, tad1)));
    }

    @Test
    public void testGetTadsContainingVariantTwoOverlappingTadsVariantInOne() {
        TopologicalDomain tad = new TopologicalDomain(1, 1, 100, ImmutableMap.of());
        TopologicalDomain tad1 = new TopologicalDomain(1, 75, 200, ImmutableMap.of());
        ChromosomalRegionIndex<TopologicalDomain> instance = ChromosomalRegionIndex.of(ImmutableList.of(tad, tad1));

        assertThat(instance.getRegionsContainingVariant(variant), equalTo(ImmutableList.of(tad)));
    }

}