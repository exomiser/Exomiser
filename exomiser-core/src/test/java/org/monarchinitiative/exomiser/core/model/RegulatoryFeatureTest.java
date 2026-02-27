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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class RegulatoryFeatureTest {

    private final RegulatoryFeature instance = new RegulatoryFeature(1, 10, 100, RegulatoryFeature.FeatureType.ENHANCER);

    @Test
    void contigId() {
        assertThat(instance.contigId(), equalTo(1));
    }

    @Test
    void start() {
        assertThat(instance.start(), equalTo(10));
    }

    @Test
    void end() {
        assertThat(instance.end(), equalTo(100));
    }

    @Test
    void testFeatureType() {
        assertThat(instance.featureType(), equalTo(RegulatoryFeature.FeatureType.ENHANCER));
    }

    @Test
    void testVariantEffect() {
        assertThat(instance.variantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
    }

}
