package de.charite.compbio.exomiser.core.filter;

import java.util.List;

import jannovar.exome.Variant;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter Variants on the basis of the PHRED quality score for the variant that
 was derived from the VCF file (QUAL field).
 *
 * @author Peter N Robinson
 * @version 0.09 (18 December, 2013).
 */
public class QualityFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(QualityFilter.class);

    private final FilterType filterType = FilterType.QUALITY_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterScore passedScore = new QualityFilterScore(1f);        
    private final FilterScore failedScore = new QualityFilterScore(0f);
    
    /**
     * Threshold for filtering. Retain only those variants whose PHRED variant
     * call quality is at least as good. The default is 1.
     */
    private float mimimumQualityThreshold = 1.0f;
    /**
     * Minimum number of reads supporting the ALT call. There must be at least
     * this number of reads in each direction.
     */
    private int minAltReadThresold = 0;

    /**
     * Constructs a VariantFilter for removing variants which do not pass the defined
 PHRED score.
     *
     * n.b. We are no longer filtering by requiring a minimum number of reads
     * for each DP4 field (alt/ref in both directions). Instead, we are just
     * filtering on the overall PHRED variant call quality.
     *
     * @param mimimumQualityThreshold The minimum PHRED quality threshold (e.g.
     * 30) under which a variant will be filtered out.
     */
    public QualityFilter(float mimimumQualityThreshold) {
        if (mimimumQualityThreshold < 0f) {
            throw new IllegalArgumentException(String.format("Illegal value for minimum quality threshold: %2f. Minimum quality threshold must be greater than 0.0", mimimumQualityThreshold));
        }
        this.mimimumQualityThreshold = mimimumQualityThreshold;
    }

    /**
     * Flag for output field representing the QUAL column of the VCF file.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public void filter(List<VariantEvaluation> variantList) {
        for (VariantEvaluation ve : variantList) {
            filter(ve);
        }
    }

    @Override
    public boolean filter(VariantEvaluation variantEvaluation) {
        Variant v = variantEvaluation.getVariant();
        float phredScore = v.getVariantPhredScore();
        if (passesFilter(phredScore)) {
            // We passed the filter (Variant has good enough quality).
            return variantEvaluation.addPassedFilter(filterType, passedScore);
        }
        // Variant is not of good quality, mark it as failed.
        //add a token failed score - this is essentially a boolean pass/fail so we're using 0 here.
        return variantEvaluation.addFailedFilter(filterType, failedScore);
    }

    protected boolean passesFilter(float qualityScore) {
        return qualityScore >= mimimumQualityThreshold;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.filterType);
        hash = 29 * hash + Float.floatToIntBits(this.mimimumQualityThreshold);
        hash = 29 * hash + this.minAltReadThresold;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QualityFilter other = (QualityFilter) obj;
        if (this.filterType != other.filterType) {
            return false;
        }
        if (Float.floatToIntBits(this.mimimumQualityThreshold) != Float.floatToIntBits(other.mimimumQualityThreshold)) {
            return false;
        }
        if (this.minAltReadThresold != other.minAltReadThresold) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return filterType + " filter mimimumQualityThreshold=" + mimimumQualityThreshold;
    }

}
