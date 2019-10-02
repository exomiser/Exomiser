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

import de.charite.compbio.jannovar.annotation.PutativeImpact;
import org.junit.jupiter.api.Test;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.VariantEffectPathogenicityScore.*;

/**
 *
 * @author jj8
 */
public class VariantEffectPathogenicityScoreTest {

    @Test
    void sequenceVariantScore() {
        assertThat(getPathogenicityScoreOf(SEQUENCE_VARIANT), equalTo(NON_PATHOGENIC_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForDefaultMissense() {
        assertThat(getPathogenicityScoreOf(MISSENSE_VARIANT), equalTo(DEFAULT_MISSENSE_SCORE));
    }

    @Test
    void synonymousVariantScore() {
        assertThat(getPathogenicityScoreOf(SYNONYMOUS_VARIANT), equalTo(SYNONYMOUS_SCORE));
    }

    @Test
    void frameShiftScores() {
        assertThat(getPathogenicityScoreOf(FRAMESHIFT_ELONGATION), equalTo(FRAMESHIFT_SCORE));
        assertThat(getPathogenicityScoreOf(FRAMESHIFT_TRUNCATION), equalTo(FRAMESHIFT_SCORE));
        assertThat(getPathogenicityScoreOf(FRAMESHIFT_VARIANT), equalTo(FRAMESHIFT_SCORE));
    }

    @Test
    void nonFrameShiftIndelScores() {
        assertThat(getPathogenicityScoreOf(MNV), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(FEATURE_TRUNCATION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(DISRUPTIVE_INFRAME_DELETION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(DISRUPTIVE_INFRAME_INSERTION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(INFRAME_DELETION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(INFRAME_INSERTION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(INTERNAL_FEATURE_ELONGATION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
        assertThat(getPathogenicityScoreOf(COMPLEX_SUBSTITUTION), equalTo(NONFRAMESHIFT_INDEL_SCORE));
    }

    @Test
    void spliceAcceptorDonorScore() {
        assertThat(getPathogenicityScoreOf(SPLICE_ACCEPTOR_VARIANT), equalTo(SPLICE_DONOR_ACCEPTOR_SCORE));
        assertThat(getPathogenicityScoreOf(SPLICE_DONOR_VARIANT), equalTo(SPLICE_DONOR_ACCEPTOR_SCORE));
    }

    @Test
    void testSpliceRegionScore() {
        assertThat(SPLICE_REGION_VARIANT.getImpact(), equalTo(PutativeImpact.LOW));
        // LOW impact scores usually have a score of 0
        assertThat(getPathogenicityScoreOf(SPLICE_REGION_VARIANT), equalTo(SPLICE_REGION_SCORE));
    }

    @Test
    void startLossScore() {
        assertThat(getPathogenicityScoreOf(START_LOST), equalTo(STARTLOSS_SCORE));
    }

    @Test
    void stopLossScore() {
        assertThat(getPathogenicityScoreOf(STOP_LOST), equalTo(STOPLOSS_SCORE));
    }

    @Test
    void stopGainScore() {
        assertThat(getPathogenicityScoreOf(STOP_GAINED), equalTo(NONSENSE_SCORE));
    }

    @Test
    public void inversionScore() {
        assertThat(getPathogenicityScoreOf(INVERSION), equalTo(INVERSION_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForNonPathogenicVariantType() {
        assertThat(getPathogenicityScoreOf(DOWNSTREAM_GENE_VARIANT), equalTo(NON_PATHOGENIC_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForUnListedHighImpactVariantEffect() {
        assertThat(COPY_NUMBER_CHANGE.getImpact(), equalTo(PutativeImpact.HIGH));
        assertThat(getPathogenicityScoreOf(COPY_NUMBER_CHANGE), equalTo(DEFAULT_HIGH_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForUnListedModerateImpactVariantEffect() {
        assertThat(THREE_PRIME_UTR_TRUNCATION.getImpact(), equalTo(PutativeImpact.MODERATE));
        assertThat(getPathogenicityScoreOf(THREE_PRIME_UTR_TRUNCATION), equalTo(DEFAULT_MISSENSE_SCORE));
    }

    @Test
    public void testGetPathogenicityScoreForUnListedLowImpactVariantEffect() {
        assertThat(CODING_TRANSCRIPT_INTRON_VARIANT.getImpact(), equalTo(PutativeImpact.LOW));
        assertThat(getPathogenicityScoreOf(CODING_TRANSCRIPT_INTRON_VARIANT), equalTo(NON_PATHOGENIC_SCORE));
    }
}
