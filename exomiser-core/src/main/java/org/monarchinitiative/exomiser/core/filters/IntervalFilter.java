/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
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
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @version 0.08 (April 28, 2013)
 */
public class IntervalFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(IntervalFilter.class);

    private static final FilterType filterType = FilterType.INTERVAL_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    // Storing a copy of the input intervals for use in the equals, hashCode and toString methods as the
    // Jannovar class underlying the ChromosomalRegionIndex does not implement these.
    private final List<ChromosomalRegion> intervals;
    private final ChromosomalRegionIndex<ChromosomalRegion> intervalIndex;

    /**
     * Constructor defining the genetic interval.
     *
     * @param interval the chromosomal region within which a variant will pass the filter.
     */
    public IntervalFilter(ChromosomalRegion interval) {
        this(ImmutableList.of(interval));
    }

    /**
     * Constructor whose argument defines a set of {@link ChromosomalRegion} within which a variant will pass the filter.
     * Variants falling outside of one of these regions will fail. It is acceptable for regions to overlap, or be nested.
     *
     * @param chromosomalRegions the chromosomal regions within which a variant will pass the filter.
     * @since 10.1.0
     */
    public IntervalFilter(Collection<ChromosomalRegion> chromosomalRegions) {
        Objects.requireNonNull(chromosomalRegions);
        //an empty collection will result in nothing ever passing
        assertNotEmpty(chromosomalRegions);
        this.intervals = copySortDeDup(chromosomalRegions);
        this.intervalIndex = ChromosomalRegionIndex.of(chromosomalRegions);
    }

    private void assertNotEmpty(Collection<ChromosomalRegion> chromosomalRegions) {
        if (chromosomalRegions.isEmpty()) {
            throw new IllegalStateException("chromosomalRegions cannot be empty");
        }
    }

    private List<ChromosomalRegion> copySortDeDup(Collection<ChromosomalRegion> geneticIntervals) {
        return geneticIntervals.stream().distinct().sorted().collect(ImmutableList.toImmutableList());
    }

    /**
     * A sorted list of {@code ChromosomalRegion} for which this filter will pass variants.
     *
     * @return a sorted list of {@code ChromosomalRegion}
     * @since 10.1.0
     */
    public List<ChromosomalRegion> getChromosomalRegions() {
        return intervals;
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
        if (intervalIndex.hasRegionContainingVariant(variantEvaluation)) {
            logger.trace("{} passes filter", variantEvaluation);
            return PASS;
        }
        logger.trace("{} fails filter", variantEvaluation);
        return FAIL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalFilter that = (IntervalFilter) o;
        return Objects.equals(intervals, that.intervals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervals);
    }

    @Override
    public String toString() {
        return "IntervalFilter{" +
                "intervals=" + intervals +
                '}';
    }
}
