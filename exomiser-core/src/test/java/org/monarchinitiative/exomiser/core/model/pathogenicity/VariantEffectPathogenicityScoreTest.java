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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.VariantEffectPathogenicityScore.*;

/**
 *
 * @author jj8
 */
public class VariantEffectPathogenicityScoreTest {

    @Test
    public void testGetPathogenicityScoreForDefaultMissense() {
        VariantEffect variantEffect = VariantEffect.MISSENSE_VARIANT;
        assertThat(getPathogenicityScoreOf(variantEffect), equalTo(DEFAULT_MISSENSE_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForStartLoss() {
        VariantEffect variantEffect = VariantEffect.START_LOST;
        assertThat(getPathogenicityScoreOf(variantEffect), equalTo(STARTLOSS_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForNonPathogenicVariantType() {
        VariantEffect variantEffect = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        assertThat(getPathogenicityScoreOf(variantEffect), equalTo(NON_PATHOGENIC_SCORE));
    }


    @Test
    public void testGetPathogenicityScoreForUnListedVariantEffect() {
        assertThat(getPathogenicityScoreOf(VariantEffect.COPY_NUMBER_CHANGE), equalTo(DEFAULT_HIGH_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForInversion() {
        assertThat(getPathogenicityScoreOf(VariantEffect.INVERSION), equalTo(INVERSION_SCORE));
    }
}
