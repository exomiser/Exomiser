package de.charite.compbio.exomiser.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter Variants on the basis of the predicted pathogenicity. This class
 * filters both on variant class (NONSENSE, MISSENSE, INTRONIC) etc., as well as
 * on the basis of MutationTaster/Polyphen2/SIFT scores for mutations.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.17 (3 February, 2014)
 *
 */
public class CADDFilterResult extends GenericFilterResult {

    private static final Logger logger = LoggerFactory.getLogger(CADDFilterResult.class);

    private static final FilterType FILTER_TYPE = FilterType.CADD_FILTER;
            
    public CADDFilterResult(float pathogenicityScore, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, pathogenicityScore, resultStatus);
    }
    
    /**
     * @return return a float representation of the filter result [0..1]. Note
     * that 0 means predicted to be non-pathogenic, and 1.0 means maximally
     * pathogenic prediction.
     */
    @Override
    public float getScore() {
        return super.getScore();
    }

}
