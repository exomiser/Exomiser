package org.monarchinitiative.exomiser.core.model.pathogenicity;

public class SpliceAiScore extends BasePathogenicityScore {

    public static SpliceAiScore of(float score) {
        return new SpliceAiScore(score);
    }

    private SpliceAiScore(float score) {
        super(PathogenicitySource.SPLICE_AI, score);
    }
}
