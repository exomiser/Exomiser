/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis;

import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class FilterStats {

    private final Map<FilterType, FilterCounter> filterCounters = new EnumMap<>(FilterType.class);
    // filtersRun requires an ordered map.
    private final Set<FilterType> filtersRun = new LinkedHashSet<>();

    public synchronized void addResult(FilterResult result) {
        FilterType filterType = result.getFilterType();
        filtersRun.add(filterType);

        FilterCounter counter = filterCounters.getOrDefault(filterType, new FilterCounter());
        if (result.passed()) {
            counter.passCount++;
        } else if (result.failed()) {
            counter.failCount++;
        }
        filterCounters.put(filterType, counter);
    }

    public int getPassCountForFilter(FilterType filterType) {
        FilterCounter filterCounter = filterCounters.get(filterType);
        return filterCounter == null ? 0 : filterCounter.getPassCount();
    }

    public int getFailCountForFilter(FilterType filterType) {
        FilterCounter filterCounter = filterCounters.get(filterType);
        return filterCounter == null ? 0 : filterCounter.getFailCount();
    }

    public List<FilterType> getFilters() {
        return new ArrayList<>(filtersRun);
    }

    /**
     * @since 13.0.0
     */
    public boolean isEmpty() {
        return filtersRun.isEmpty();
    }

    /**
     * @since 13.0.0
     */
    public List<FilterCount> getFilterCounts() {
        return filtersRun.stream()
                .map(filterType -> new FilterCount(filterType, filterCounters.get(filterType)))
                .collect(Collectors.toList());
    }

    /**
     * @since 13.0.0
     */
    public static class FilterCount {

        private final FilterType filterType;
        private final int passCount;
        private final int failCount;

        private FilterCount(FilterType filterType, FilterCounter filterCounter) {
            this.filterType = filterType;
            this.passCount = filterCounter.passCount;
            this.failCount = filterCounter.failCount;
        }

        public FilterType getFilterType() {
            return filterType;
        }

        public int getPassCount() {
            return passCount;
        }

        public int getFailCount() {
            return failCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterCount)) return false;
            FilterCount that = (FilterCount) o;
            return passCount == that.passCount &&
                    failCount == that.failCount &&
                    filterType == that.filterType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(filterType, passCount, failCount);
        }

        @Override
        public String toString() {
            return "FilterStat{" +
                    "filterType=" + filterType +
                    ", passCount=" + passCount +
                    ", failCount=" + failCount +
                    '}';
        }
    }

    private static class FilterCounter {
        int passCount = 0;
        int failCount = 0;

        int getPassCount() {
            return passCount;
        }

        int getFailCount() {
            return failCount;
        }
    }

}
