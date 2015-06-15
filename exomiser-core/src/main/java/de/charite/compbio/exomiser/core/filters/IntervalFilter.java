package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.Variant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to a linkage interval. For instance, if the
 * interval is chr2:12345-67890, then we would only keep variants located
 * between positions 12345 and 67890 on chromosome 2. All other variants are
 * discarded.
 * <P>
 * The interval must be given as chr2:12345-67890 (format), otherwise, an error
 * message is given and no filtering is done.
 *
 * @author Peter N Robinson
 * @version 0.08 (April 28, 2013)
 */
public class IntervalFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(IntervalFilter.class);

    private static final FilterType filterType = FilterType.INTERVAL_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passFilterResult = new IntervalFilterResult(1f, FilterResultStatus.PASS);
    private final FilterResult failedFilterResult = new IntervalFilterResult(0f, FilterResultStatus.FAIL);

    private final GeneticInterval interval;

    /**
     * Constructor defining the genetic interval.
     *
     * @param interval the interval based on a String such as chr2:12345-67890.
     */
    public IntervalFilter(GeneticInterval interval) {
        this.interval = interval;

    }

    public GeneticInterval getInterval() {
        return interval;
    }

    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (variantIsNotWithinInterval(variantEvaluation)) {
            return failedFilterResult;
        }
        return passFilterResult;
    }

    private boolean variantIsNotWithinInterval(Variant variant) {
        if (variantNotOnSameChromosomeAsInterval(variant.getChromosome())) {
            return true;
        } else {
            return variantPositionOutsideOfIntervalBounds(variant.getPosition());
        }
    }

    private boolean variantNotOnSameChromosomeAsInterval(int variantChromosome) {
        return variantChromosome != interval.getChromosome();
    }

    private boolean variantPositionOutsideOfIntervalBounds(int variantPosition) {
        return variantPosition < interval.getStart() || variantPosition > interval.getEnd();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(IntervalFilter.filterType);
        hash = 97 * hash + Objects.hashCode(this.interval);
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
        final IntervalFilter other = (IntervalFilter) obj;
        return Objects.equals(this.interval, other.interval);
    }

    @Override
    public String toString() {
        return filterType + " filter chromosome=" + interval.getChromosome() + ", from=" + interval.getStart() + ", to=" + interval.getEnd() + ", interval=" + interval;
    }

}
