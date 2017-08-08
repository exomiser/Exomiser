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

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RegulatoryFeatureTest {

    private RegulatoryFeature instance;

    @Before
    public void setUp() {
        //do we want types or polymorphism? Start with type as this is pretty much just a DTO at the moment?
        int chr = 1;
        int start = 10;
        int end = 100;
        RegulatoryFeature.FeatureType featureType = RegulatoryFeature.FeatureType.ENHANCER;
        instance = new RegulatoryFeature(chr, start, end, featureType);
    }

    @Test
    public void getChromosome() {
        assertThat(instance.getChromosome(), equalTo(1));
    }

    @Test
    public void getStart() {
        assertThat(instance.getStart(), equalTo(10));
    }

    @Test
    public void getEnd() {
        assertThat(instance.getEnd(), equalTo(100));
    }

    @Test
    public void testGetFeatureType() {
        assertThat(instance.getFeatureType(), equalTo(RegulatoryFeature.FeatureType.ENHANCER));
    }

    @Test
    public void testGetVariantEffect() {
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }

}
