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

package org.monarchinitiative.exomiser.core.filters;


import org.monarchinitiative.exomiser.core.model.Filterable;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

/**
 * Package private helper class for collecting pass/fail stats from filters run by an {@link FilterRunner}. These need
 * to be collected at the source of the filter runner as these may discard failed variants along the way which will lead
 * to missing failed variant counts if the stats are collected at the end of a filter group run. The methods for logging
 * the {@link FilterResult}s are threadsafe.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 12.0.0
 */
class FilterResultsCounter {

    private final Map<FilterType, PassFailCount> passFailCounts = new EnumMap<>(FilterType.class);
    // filtersRun requires an ordered map.
    private final Set<FilterType> filtersRun = new LinkedHashSet<>();

    /**
     * @param filter
     * @param gene
     * @since 13.4.0
     */
    protected void logResultsForFilter(Filter<?> filter, Gene gene) {
        FilterType filterType = filter.getFilterType();
        if (filter.isOnlyGeneDependent()) {
            // Cater for the case where the PriorityScoreFilter is run before any variants are loaded
            // don't add variant filter counts here as they can get mixed with genes which did have variants
            // so the numbers don't add up correctly. The alternative is to implement FilterStats::addGeneResult
            // but this also gets messy
            logFilterResult(filterType, gene);
        } else {
            logVariantFilterStats(filterType, gene);
        }
    }

    private void logVariantFilterStats(FilterType filterType, Gene gene) {
        for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
            logFilterResult(filterType, variantEvaluation);
        }
    }

    /**
     * @param filterType
     * @param filterable
     * @since 13.4.0
     */
    protected void logFilterResult(FilterType filterType, Filterable filterable) {
        if (filterable.passedFilter(filterType)) {
            logPassResult(filterType);
        } else if (filterable.failedFilter(filterType)) {
            logFailResult(filterType);
        }
    }

    protected void logResult(FilterResult result) {
        FilterType filterType = result.getFilterType();
        if (result.passed()) {
            logPassResult(filterType);
        } else if (result.failed()) {
            logFailResult(filterType);
        }
    }

    // these are synchronised as it is possible they may be run in parallel threads
    private synchronized void logPassResult(FilterType filterType) {
        PassFailCount passFailCount = getPassFailCount(filterType);
        passFailCount.passCount++;
    }

    private synchronized void logFailResult(FilterType filterType) {
        PassFailCount passFailCount = getPassFailCount(filterType);
        passFailCount.failCount++;
    }

    private PassFailCount getPassFailCount(FilterType filterType) {
        filtersRun.add(filterType);
        return passFailCounts.compute(filterType, (k, v) -> v == null ? new PassFailCount() : v);
    }

    protected List<FilterType> filtersRun() {
        return List.copyOf(filtersRun);
    }

    /**
     * @since 13.0.0
     */
    protected boolean isEmpty() {
        return filtersRun.isEmpty();
    }


    protected int passCountForFilter(FilterType filterType) {
        PassFailCount passFailCount = passFailCounts.get(filterType);
        return passFailCount == null ? 0 : passFailCount.getPassCount();
    }

    protected int failCountForFilter(FilterType filterType) {
        PassFailCount passFailCount = passFailCounts.get(filterType);
        return passFailCount == null ? 0 : passFailCount.getFailCount();
    }

    /**
     * The final pass/fail counts for each {@link FilterType}. These are intended to be used for display purposes in the
     * log and the HTML output files.
     *
     * @since 13.0.0
     */
    public List<FilterResultCount> filterResultCounts() {
        // this method is not synchronised as it should be called after any parallel threads have finally been joined
        List<FilterResultCount> filterResultCounts = new ArrayList<>();
        for (FilterType type : filtersRun) {
            PassFailCount passFailCount = passFailCounts.get(type);
            FilterResultCount filterResultCount = new FilterResultCount(type, passFailCount.passCount, passFailCount.failCount);
            filterResultCounts.add(filterResultCount);
        }
        return List.copyOf(filterResultCounts);
    }

    public FilterResultCount filterResultCount(FilterType filterType) {
        PassFailCount passFailCount = passFailCounts.get(filterType);
        return passFailCount == null ? new FilterResultCount(filterType, 0, 0) : new FilterResultCount(filterType, passFailCount.passCount, passFailCount.failCount);
    }

    private static class PassFailCount {
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
