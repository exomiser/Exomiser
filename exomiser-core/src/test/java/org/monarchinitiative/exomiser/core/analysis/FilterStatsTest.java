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

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class FilterStatsTest {

    @Test
    void canLogPassCountForSingleFilter() {
        FilterStats instance = new FilterStats();

        instance.addResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));

        assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(1));
    }

    @Test
    void canLogMultiplePassCountsForSingleFilter() {
        FilterStats instance = new FilterStats();
        int numPasses = 10;

        for (int i = 0; i < numPasses; i++) {
            instance.addResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        }

        assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numPasses));
    }

    @Test
    void canLogMultiplePassAndFailCountsForSingleFilter() {
        FilterStats instance = new FilterStats();
        int numPasses = 10;
        for (int i = 0; i < numPasses; i++) {
            instance.addResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        }

        int numFails = 99;
        for (int i = 0; i < numFails; i++) {
            instance.addResult(FilterResult.fail(FilterType.FREQUENCY_FILTER));
        }

        assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numPasses));
        assertThat(instance.getFailCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numFails));
    }

    @Test
    void canLogMultiplePassAndFailCountsForMultipleFilters() {
        FilterStats instance = new FilterStats();

        int numFrequencyPasses = 10;
        int numFrequencyFails = 99;
        FilterRunner freqFilterRunner = new FilterRunner(FilterType.FREQUENCY_FILTER, numFrequencyPasses, numFrequencyFails, instance);
        freqFilterRunner.run();

        int numVarEffPasses = 123;
        int numVarEffFails = 321;

        FilterRunner varEffFilterRunner = new FilterRunner(FilterType.VARIANT_EFFECT_FILTER, numVarEffPasses, numVarEffFails, instance);
        varEffFilterRunner.run();

        int numPathPasses = 542;
        int numPathFails = 453;
        FilterRunner pathFilterRunner = new FilterRunner(FilterType.PATHOGENICITY_FILTER, numPathPasses, numPathFails, instance);
        pathFilterRunner.run();

        assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numFrequencyPasses));
        assertThat(instance.getFailCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numFrequencyFails));

        assertThat(instance.getPassCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(numVarEffPasses));
        assertThat(instance.getFailCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(numVarEffFails));

        assertThat(instance.getPassCountForFilter(FilterType.PATHOGENICITY_FILTER), equalTo(numPathPasses));
        assertThat(instance.getFailCountForFilter(FilterType.PATHOGENICITY_FILTER), equalTo(numPathFails));

        assertThat(instance.getFilters(), equalTo(ImmutableList.of(FilterType.FREQUENCY_FILTER, FilterType.VARIANT_EFFECT_FILTER, FilterType.PATHOGENICITY_FILTER)));
    }

    @Test
    void logsFiltersInOrderOfRunning() {
        FilterStats instance = new FilterStats();

        List<FilterType> filters = ImmutableList.of(FilterType.PRIORITY_SCORE_FILTER, FilterType.VARIANT_EFFECT_FILTER, FilterType.QUALITY_FILTER, FilterType.FREQUENCY_FILTER, FilterType.PATHOGENICITY_FILTER, FilterType.INHERITANCE_FILTER);
        filters.forEach(filterType -> new FilterRunner(filterType, 1, 1, instance).run());

        assertThat(instance.getFilters(), equalTo(filters));
    }

    @Test
    void canLogMultiplePassAndFailCountsForMultipleFiltersWithMultipleThreads() throws Exception {
        for (int i = 0; i < 1000; i++) {
            FilterStats instance = new FilterStats();

            int numFrequencyPasses = 10;
            int numFrequencyFails = 99;
            FilterRunner freqFilterRunner = new FilterRunner(FilterType.FREQUENCY_FILTER, numFrequencyPasses, numFrequencyFails, instance);

            int numVarEffPasses = 123;
            int numVarEffFails = 321;
            FilterRunner varEffFilterRunner = new FilterRunner(FilterType.VARIANT_EFFECT_FILTER, numVarEffPasses, numVarEffFails, instance);

            int numPathPasses = 542;
            int numPathFails = 453;
            FilterRunner pathFilterRunner = new FilterRunner(FilterType.PATHOGENICITY_FILTER, numPathPasses, numPathFails, instance);

            ImmutableList.of(freqFilterRunner, varEffFilterRunner, pathFilterRunner)
                    .parallelStream()
                    .forEach(FilterRunner::run);

            assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numFrequencyPasses));
            assertThat(instance.getFailCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(numFrequencyFails));

            assertThat(instance.getPassCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(numVarEffPasses));
            assertThat(instance.getFailCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(numVarEffFails));

            assertThat(instance.getPassCountForFilter(FilterType.PATHOGENICITY_FILTER), equalTo(numPathPasses));
            assertThat(instance.getFailCountForFilter(FilterType.PATHOGENICITY_FILTER), equalTo(numPathFails));
        }
    }

    @Test
    void canLogMultiplePassAndFailCountsForSameFilterWithMultipleThreads() throws Exception {

        for (int i = 0; i < 1000; i++) {
            FilterStats instance = new FilterStats();

            int threadOnePasses = 10;
            int threadOneFails = 99;
            FilterRunner threadOneFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadOnePasses, threadOneFails, instance);

            int threadTwoPasses = 123;
            int threadTwoFails = 321;
            FilterRunner threadTwoFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadTwoPasses, threadTwoFails, instance);

            int threadThreePasses = 542;
            int threadThreeFails = 453;
            FilterRunner threadThreeFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadThreePasses, threadThreeFails, instance);

            ImmutableList.of(threadOneFilter, threadTwoFilter, threadThreeFilter)
                    .parallelStream()
                    .forEach(FilterRunner::run);

            assertThat(instance.getPassCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(threadOnePasses + threadTwoPasses + threadThreePasses));
            assertThat(instance.getFailCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(threadOneFails + threadTwoFails + threadThreeFails));
            assertThat(instance.getFilters(), equalTo(ImmutableList.of(FilterType.FREQUENCY_FILTER)));
        }
    }

    /**
     * Utility class for testing FilterStats in a multi-threaded environment.
     */
    private class FilterRunner implements Runnable {

        private final FilterType filterType;

        private final int numPass;
        private final int numFail;

        private final FilterStats filterStats;

        FilterRunner(FilterType filterType, int numPass, int numFail, FilterStats filterStats) {
            this.filterType = filterType;
            this.numPass = numPass;
            this.numFail = numFail;
            this.filterStats = filterStats;
        }

        @Override
        public void run() {
            for (int i = 0; i < numPass; i++) {
                filterStats.addResult(FilterResult.pass(filterType));
            }

            for (int i = 0; i < numFail; i++) {
                filterStats.addResult(FilterResult.fail(filterType));
            }
        }
    }
}