/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.pathogenicity;

import jannovar.common.VariantType;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class VariantTypePathogenicityScores {
    
    /**
     * This is the pathogenicity value we will give to missense (nonsynonymous)
     * variants for which we cannot find values for mutationTaster, polyphen2,
     * or SIFT.
     * Average CADD score for missense = 15.08 => ~0.97 
     */
    public static final float DEFAULT_MISSENSE_SCORE = 0.97f;
    /**
     * Pathogenicity score for a mutation class such as INTERGENIC where we are
     * entirely sure it is nonpathogenic (for the purposes of this software).
     */
    public static final float NON_PATHOGENIC_SCORE = 0.00f;
    /**
     * Assumed pathogenicity score for a frameshift mutation.
     * Average CADD score from running an exome
     */
    public static final float FRAMESHIFT_SCORE = 0.85f;
    /**
     * Assumed pathogenicity score for a nonframeshift indel mutation.
     * Need to run more exomes to get a proper average - for now set to 0.1 less than
     * frameshift
     */
    public static final float NONFRAMESHIFT_INDEL_SCORE = 0.75f;
    /**
     * Assumed pathogenicity score for a nonsense mutation.
     * Average CADD score = 31.67 => ~0.999 
     */
    public static final float NONSENSE_SCORE = 0.999f;
    /**
     * Assumed pathogenicity score for a splice site mutation.
     * Average CADD score from running an exome
     */
    public static final float SPLICING_SCORE = 0.85f;
    /**
     * Assumed pathogenicity score for a synonymous mutation.
     */
    public static final float SYNONYMOUS_SCORE = 0.10f;
    /**
     * Assumed pathogenicity score for a stoploss mutation.
     * Average CADD score for missense = 13.84 => ~0.94  
     */
    public static final float STOPLOSS_SCORE = 0.94f;
    /**
     * Assumed pathogenicity score for a variant that causes the start codon to
     * be lost.
     * Average CADD score for missense = 14.11 => ~0.94
     */
    public static final float STARTLOSS_SCORE = 0.94f;



    public static final float getPathogenicityScoreOf(VariantType variantType) {
        if (!variantType.isTopPriorityVariant()) {
            return NON_PATHOGENIC_SCORE;
        }
        switch (variantType) {
            case MISSENSE:
                return DEFAULT_MISSENSE_SCORE;
            case FS_DELETION:
                return FRAMESHIFT_SCORE;
            case FS_INSERTION:
                return FRAMESHIFT_SCORE;
            case NON_FS_SUBSTITUTION:
                return NONFRAMESHIFT_INDEL_SCORE;
            case FS_SUBSTITUTION:
                return FRAMESHIFT_SCORE;
            case NON_FS_DELETION:
                return NONFRAMESHIFT_INDEL_SCORE;
            case NON_FS_INSERTION:
                return NONFRAMESHIFT_INDEL_SCORE;
            case SPLICING:
                return SPLICING_SCORE;
            case STOPGAIN:
                return NONSENSE_SCORE;
            case STOPLOSS:
                return STOPLOSS_SCORE;
            //Note, the frameshift duplication get the FRAMESHIFT default score
            case FS_DUPLICATION:
                return FRAMESHIFT_SCORE;
            case NON_FS_DUPLICATION:
                return NONFRAMESHIFT_INDEL_SCORE;
            case START_LOSS:
                return STARTLOSS_SCORE;
            default:
                //(the remainder should be the ).
                return NON_PATHOGENIC_SCORE;
        }
    }

}
