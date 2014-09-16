package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.exome.Variant;
import java.util.List;
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

    private final FilterType filterType = FilterType.INTERVAL_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterScore passedScore = new IntervalFilterScore(1f);        
    private final FilterScore failedScore = new IntervalFilterScore(0f);

    private final GeneticInterval interval;

    /**
     * Constructor defining the genetic interval.
     *
     * @param interval the interval based on a String such as chr2:12345-67890.
     */
    public IntervalFilter(GeneticInterval interval) {
        this.interval = interval;

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

    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter, flag it as failed.
     *
     * @param variantList
     */
    @Override
    public void filter(List<VariantEvaluation> variantList) {

        for (VariantEvaluation ve : variantList) {
            filter(ve);
        }
    }

    @Override
    public boolean filter(VariantEvaluation variantEvaluation) {
        Variant v = variantEvaluation.getVariant();
        
        int c = v.get_chromosome();
        if (c != interval.getChromosome()) {
            return variantEvaluation.addFailedFilter(filterType, failedScore);
        }
        /* If we get here, we are on the same chromosome */
        int pos = v.get_position();
        if (pos < interval.getStart() || pos > interval.getEnd()) {
            return variantEvaluation.addFailedFilter(filterType, failedScore);
        }
        return variantEvaluation.addPassedFilter(filterType, passedScore);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.filterType);
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
        if (this.filterType != other.filterType) {
            return false;
        }
        if (!Objects.equals(this.interval, other.interval)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return filterType + " filter chromosome=" + interval.getChromosome() + ", from=" + interval.getStart() + ", to=" + interval.getEnd() + ", interval=" + interval;
    }

}
