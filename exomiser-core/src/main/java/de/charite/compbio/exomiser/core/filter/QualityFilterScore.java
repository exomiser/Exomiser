package de.charite.compbio.exomiser.core.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter Variants on the basis of the PHRED quality score for the variant that
 * was derived from the VCF file (QUAL field).
 *
 * @author Peter N Robinson
 * @version 0.09 (18 December, 2013).
 */
public class QualityFilterScore implements FilterScore {

    private static final Logger logger = LoggerFactory.getLogger(QualityFilterScore.class);

    /**
     * The score as a result of filtering.
     */
    private final float score;    
    
    /**
     * @param score.
     */
    public QualityFilterScore(float score) {
        this.score = score;
    }
    @Override
    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "QualityFilterScore{" + "score=" + score + '}';
    }
    
}
