package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters variants according to their predicted pathogenicity.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (29 December, 2012).
 */
public class PathogenicityFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(PathogenicityFilter.class);
    private static final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    private final boolean keepNonPathogenic;

    /**
     * Produces a Pathogenicity filter using a user-defined pathogenicity
     * threshold. The keepNonPathogenic parameter will apply the pathogenicity
     * scoring, but no further filtering will be applied so all variants will
     * pass irrespective of their score.
     *
     * @param keepNonPathogenic
     */
    public PathogenicityFilter(boolean keepNonPathogenic) {
        this.keepNonPathogenic = keepNonPathogenic;
    }

    public boolean keepNonPathogenic() {
        return keepNonPathogenic;
    }
    
    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * VariantFilter variants based on their calculated pathogenicity. Those
     * that pass have a pathogenicity score assigned to them. The failed ones
     * are deemed to be non-pathogenic and marked as such.
     *     
*/
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {

        float variantPathogenicityScore = variantEvaluation.getPathogenicityScore();

        if (keepNonPathogenic) {
            return passesFilter;
        }
        if (variantEvaluation.isPredictedPathogenic()) {
            return passesFilter;
        }
        return failsFilter;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(PathogenicityFilter.filterType);
        hash = 97 * hash + (this.keepNonPathogenic ? 1 : 0);
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
        final PathogenicityFilter other = (PathogenicityFilter) obj;
        return this.keepNonPathogenic == other.keepNonPathogenic;
    }

    @Override
    public String toString() {
        return "PathogenicityFilter{" + "keepNonPathogenic=" + keepNonPathogenic + '}';
    }
}
