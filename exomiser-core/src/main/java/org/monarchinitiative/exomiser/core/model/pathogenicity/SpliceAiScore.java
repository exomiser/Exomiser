package org.monarchinitiative.exomiser.core.model.pathogenicity;

/**
 * The SpliceAI (<a href="https://doi.org/10.1016/j.cell.2018.12.015">Jaganathan et al, Cell 2019</a>) max delta score.
 * This is the max score from the DL/DG/AL/AG scores >= minScore. The min score for this was set to 0.2, so scores lower
 * than this will likely not be present in the database, unless guidance changes later.
 * <blockquote>
 * Delta score of a variant, defined as the maximum of (DS_AG, DS_AL, DS_DG, DS_DL), ranges from 0 to 1 and can be interpreted as the probability of the variant being splice-altering. In the paper, a detailed characterization is provided for 0.2 (high recall), 0.5 (recommended), and 0.8 (high precision) cutoffs. Delta position conveys information about the location where splicing changes relative to the variant position (positive values are downstream of the variant, negative values are upstream).
 * </blockquote>
 * <cite>
 * These annotations are free for academic and not-for-profit use; other use requires a commercial license from Illumina, Inc.
 * </cite>
 * <a href="https://github.com/Illumina/SpliceAI">https://github.com/Illumina/SpliceAI</a>
 **/
public class SpliceAiScore extends BasePathogenicityScore {

    public static float NON_SPLICEOGENIC_SCORE = 0.1f;
    public static float PERMISSIVE_SPLICEOGENIC_SCORE = 0.2f;
    public static float DEFAULT_SPLICEOGENIC_SCORE = 0.5f;
    public static float HIGH_CONFIDENCE_SPLICEOGENIC_SCORE = 0.8f;

    public static SpliceAiScore of(float score) {
        return new SpliceAiScore(score);
    }

    private SpliceAiScore(float score) {
        super(PathogenicitySource.SPLICE_AI, score);
    }
}
