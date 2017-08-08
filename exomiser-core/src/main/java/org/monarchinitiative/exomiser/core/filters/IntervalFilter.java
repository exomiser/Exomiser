/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    private final GeneticInterval interval;

    /**
     * Constructor defining the genetic interval.
     *
     * @param interval the interval based on a String such as chr2:12345-67890.
     */
    public IntervalFilter(GeneticInterval interval) {
        this.interval = interval;

    }

    public GeneticInterval getGeneticInterval() {
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
            return FAIL;
        }
        return PASS;
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
        return "IntervalFilter{" +
                "interval=" + interval +
                '}';
    }
}
