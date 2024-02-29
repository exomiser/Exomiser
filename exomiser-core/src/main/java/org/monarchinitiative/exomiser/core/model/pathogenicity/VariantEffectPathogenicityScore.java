
/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import de.charite.compbio.jannovar.annotation.PutativeImpact;
import de.charite.compbio.jannovar.annotation.VariantEffect;

/**
 * Set of constants for use as default pathogenicity scores for a given {@link VariantEffect}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public final class VariantEffectPathogenicityScore {

    /**
     * This is the pathogenicity value we will give to missense (nonsynonymous)
     * variants for which we cannot find values for mutationTaster, polyphen2,
     * or SIFT.
     */
    public static final float DEFAULT_MISSENSE_SCORE = 0.6f;

    public static final float DEFAULT_HIGH_SCORE = 1.0f;
    /**
     * Pathogenicity score for a mutation class such as INTERGENIC where we are
     * entirely sure it is nonpathogenic (for the purposes of this software).
     */
    public static final float NON_PATHOGENIC_SCORE = 0.00f;
    /**
     * Assumed pathogenicity score for a frameshift mutation.
     */
    public static final float FRAMESHIFT_SCORE = 1.00f;
    /**
     * Assumed pathogenicity score for a nonframeshift indel mutation.
     */
    public static final float NONFRAMESHIFT_INDEL_SCORE = 0.85f;
    /**
     * Assumed pathogenicity score for a nonsense mutation.
     */
    public static final float NONSENSE_SCORE = 1.00f;
    /**
     * Assumed pathogenicity score for a splice site mutation.
     */
    public static final float SPLICE_DONOR_ACCEPTOR_SCORE = 1.00f;
    /**
     * Default pathogenicity score for variants in the a splice region, but not a donor or acceptor.
     */
    public static final float SPLICE_REGION_SCORE = 0.8f;   /**
     * Assumed pathogenicity score for a synonymous mutation.
     */
    public static final float SYNONYMOUS_SCORE = 0.10f;
    /**
     * Assumed pathogenicity score for a stoploss mutation.
     */
    public static final float STOPLOSS_SCORE = 1.00f;
    /**
     * Assumed pathogenicity score for a variant that causes the start codon to
     * be lost.
     */
    public static final float STARTLOSS_SCORE = 1.00f;

    public static final float INVERSION_SCORE = 0.6f;

    private VariantEffectPathogenicityScore() {
        //Uninstantiable.  This class should be an enum, but then it doesn't code nicely :(
    }

    public static float pathogenicityScoreOf(VariantEffect variantEffect) {
        switch (variantEffect) {
            case SEQUENCE_VARIANT:
                return NON_PATHOGENIC_SCORE;
            case MISSENSE_VARIANT:
                return DEFAULT_MISSENSE_SCORE;
            case SYNONYMOUS_VARIANT:
                return SYNONYMOUS_SCORE;
            case FRAMESHIFT_ELONGATION:
            case FRAMESHIFT_TRUNCATION:
            case FRAMESHIFT_VARIANT:
                return FRAMESHIFT_SCORE;
            case MNV:
            case FEATURE_TRUNCATION:
            case DISRUPTIVE_INFRAME_DELETION:
            case DISRUPTIVE_INFRAME_INSERTION:
            case INFRAME_DELETION:
            case INFRAME_INSERTION:
            case INTERNAL_FEATURE_ELONGATION:
            case COMPLEX_SUBSTITUTION:
                return NONFRAMESHIFT_INDEL_SCORE;
            case SPLICE_ACCEPTOR_VARIANT:
            case SPLICE_DONOR_VARIANT:
                return SPLICE_DONOR_ACCEPTOR_SCORE;
            case SPLICE_REGION_VARIANT:
                return SPLICE_REGION_SCORE;
            case START_LOST:
                return STARTLOSS_SCORE;
            case STOP_LOST:
                return STOPLOSS_SCORE;
            case STOP_GAINED:
                return NONSENSE_SCORE;
            case INVERSION:
                // down-ranking this from HIGH to MODERATE as we're not certain of the impact unless it affects the
                // transcript e.g. transcript ablation if the inversion happens in the middle of a gene.
                return INVERSION_SCORE;
            default:
                // Hopefully shouldn't get to here, but in case we do...
                return defaultImpactScore(variantEffect.getImpact());
        }
    }

    private static float defaultImpactScore(PutativeImpact putativeImpact) {
        // guard against overlooking MODERATE and HIGH impact effects
        int effectOrdinal = putativeImpact.ordinal();
        if (effectOrdinal == PutativeImpact.MODERATE.ordinal()) {
            return DEFAULT_MISSENSE_SCORE;
        }
        if (effectOrdinal == PutativeImpact.HIGH.ordinal()) {
            return DEFAULT_HIGH_SCORE;
        }
        return NON_PATHOGENIC_SCORE;
    }

}
