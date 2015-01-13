package de.charite.compbio.exomiser.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter Variants on the basis of the PHRED quality score for the variant that
 * was derived from the VCF file (QUAL field).
 *
 * @author Peter N Robinson
 * @version 0.09 (18 December, 2013).
 */
public class QualityFilterResult extends GenericFilterResult {

    private static final Logger logger = LoggerFactory.getLogger(QualityFilterResult.class);

    private static final FilterType FILTER_TYPE = FilterType.QUALITY_FILTER;
    
    /**
     * @param score.
     * @param resultStatus
     */
    public QualityFilterResult(float score, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, score, resultStatus);
    }

}
