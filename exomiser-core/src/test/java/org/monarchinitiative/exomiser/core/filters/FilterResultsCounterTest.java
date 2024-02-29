/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Filterable;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class FilterResultsCounterTest {

    @Test
    void canLogPassCountForSingleFilter() {
        FilterResultsCounter instance = new FilterResultsCounter();

        instance.logResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        FilterResultCount expected = new FilterResultCount(FilterType.FREQUENCY_FILTER, 1, 0);
        assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(expected));
    }

    @Test
    void canLogMultiplePassCountsForSingleFilter() {
        FilterResultsCounter instance = new FilterResultsCounter();
        int numPasses = 10;

        for (int i = 0; i < numPasses; i++) {
            instance.logResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        }

        FilterResultCount expected = new FilterResultCount(FilterType.FREQUENCY_FILTER, numPasses, 0);
        assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(expected));
    }

    @Test
    void canLogMultiplePassAndFailCountsForSingleFilter() {
        FilterResultsCounter instance = new FilterResultsCounter();
        int numPasses = 10;
        for (int i = 0; i < numPasses; i++) {
            instance.logResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        }

        int numFails = 99;
        for (int i = 0; i < numFails; i++) {
            instance.logResult(FilterResult.fail(FilterType.FREQUENCY_FILTER));
        }

        FilterResultCount expected = new FilterResultCount(FilterType.FREQUENCY_FILTER, numPasses, numFails);
        assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(expected));
    }

    @Test
    void canLogPassAndFailCountsForSingleVariant() {
        Filterable filterable = TestFactory.variantBuilder(1, 2, "A", "G").build();
        filterable.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        filterable.addFilterResult(FilterResult.fail(FilterType.FREQUENCY_FILTER));

        FilterResultsCounter instance = new FilterResultsCounter();
        instance.logFilterResult(FilterType.VARIANT_EFFECT_FILTER, filterable);
        instance.logFilterResult(FilterType.FREQUENCY_FILTER, filterable);


        FilterResultCount varEffResultCount = new FilterResultCount(FilterType.VARIANT_EFFECT_FILTER, 1, 0);
        assertThat(instance.filterResultCount(FilterType.VARIANT_EFFECT_FILTER), equalTo(varEffResultCount));

        FilterResultCount freqResultCount = new FilterResultCount(FilterType.FREQUENCY_FILTER, 0, 1);
        assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(freqResultCount));
    }

    @Test
    void addStatsForFilter() {
        VariantEvaluation variant = TestFactory.variantBuilder(1, 2, "A", "G").build();
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.addFilterResult(FilterResult.fail(FilterType.FREQUENCY_FILTER));

        Gene gene = new Gene("GENE", 12345);
        gene.addVariant(variant);

        FilterResultsCounter instance = new FilterResultsCounter();
        instance.logResultsForFilter(new VariantEffectFilter(Set.of()), gene);
        instance.logResultsForFilter(new FrequencyFilter(0.1f), gene);

        instance.filterResultCounts().forEach(System.out::println);
        assertThat(instance.passCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(1));
        assertThat(instance.failCountForFilter(FilterType.VARIANT_EFFECT_FILTER), equalTo(0));

        assertThat(instance.passCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(0));
        assertThat(instance.failCountForFilter(FilterType.FREQUENCY_FILTER), equalTo(1));
    }

    @Test
    void addStatsForGeneOnlyFilter() {
        Gene gene1 = new Gene("GENE1", 12345);
        gene1.addFilterResult(FilterResult.pass(FilterType.PRIORITY_SCORE_FILTER));

        Gene gene2 = new Gene("GENE2", 12345);
        gene2.addFilterResult(FilterResult.fail(FilterType.PRIORITY_SCORE_FILTER));

        FilterResultsCounter instance = new FilterResultsCounter();
        PriorityScoreFilter priorityScoreFilter = new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501);
        instance.logResultsForFilter(priorityScoreFilter, gene1);
        instance.logResultsForFilter(priorityScoreFilter, gene2);

        FilterResultCount expected = new FilterResultCount(FilterType.PRIORITY_SCORE_FILTER, 1, 1);
        assertThat(instance.filterResultCount(FilterType.PRIORITY_SCORE_FILTER), equalTo(expected));
    }

    @Test
    void canLogMultiplePassAndFailCountsForMultipleFilters() {
        FilterResultsCounter instance = new FilterResultsCounter();

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

        FilterResultCount freqResultCount = new FilterResultCount(FilterType.FREQUENCY_FILTER, numFrequencyPasses, numFrequencyFails);
        FilterResultCount varEffResultCount = new FilterResultCount(FilterType.VARIANT_EFFECT_FILTER, numVarEffPasses, numVarEffFails);
        FilterResultCount pathResultCount = new FilterResultCount(FilterType.PATHOGENICITY_FILTER, numPathPasses, numPathFails);

        assertThat(instance.filtersRun(), equalTo(List.of(FilterType.FREQUENCY_FILTER, FilterType.VARIANT_EFFECT_FILTER, FilterType.PATHOGENICITY_FILTER)));
        assertThat(instance.filterResultCounts(), equalTo(List.of(freqResultCount, varEffResultCount, pathResultCount)));
    }

    @Test
    void logsFiltersInOrderOfRunning() {
        FilterResultsCounter instance = new FilterResultsCounter();

        List<FilterType> filters = List.of(FilterType.PRIORITY_SCORE_FILTER, FilterType.VARIANT_EFFECT_FILTER, FilterType.QUALITY_FILTER, FilterType.FREQUENCY_FILTER, FilterType.PATHOGENICITY_FILTER, FilterType.INHERITANCE_FILTER);
        filters.forEach(filterType -> new FilterRunner(filterType, 1, 1, instance).run());

        assertThat(instance.filtersRun(), equalTo(filters));
    }

    @Test
    void getFilterCountsReturnedInOrder() {
        FilterResultsCounter instance = new FilterResultsCounter();

        List<FilterType> filters = List.of(FilterType.PRIORITY_SCORE_FILTER, FilterType.VARIANT_EFFECT_FILTER, FilterType.QUALITY_FILTER, FilterType.FREQUENCY_FILTER, FilterType.PATHOGENICITY_FILTER, FilterType.INHERITANCE_FILTER);
        filters.forEach(filterType -> new FilterRunner(filterType, 1, 2, instance).run());

        List<FilterResultCount> filterResultCounts = instance.filterResultCounts();
        assertThat(filterResultCounts.size(), equalTo(filters.size()));
        for (int i = 0; i < filterResultCounts.size(); i++) {
            assertThat(filterResultCounts.get(i).filterType(), equalTo(filters.get(i)));
        }
    }

    @Test
    void isEmpty() {
        FilterResultsCounter instance = new FilterResultsCounter();
        assertThat(instance.isEmpty(), equalTo(true));

        instance.logResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        assertThat(instance.isEmpty(), equalTo(false));
    }


    @Test
    void canLogMultiplePassAndFailCountsForMultipleFiltersWithMultipleThreads() {
        for (int i = 0; i < 1000; i++) {
            FilterResultsCounter instance = new FilterResultsCounter();

            int numFrequencyPasses = 10;
            int numFrequencyFails = 99;
            FilterRunner freqFilterRunner = new FilterRunner(FilterType.FREQUENCY_FILTER, numFrequencyPasses, numFrequencyFails, instance);

            int numVarEffPasses = 123;
            int numVarEffFails = 321;
            FilterRunner varEffFilterRunner = new FilterRunner(FilterType.VARIANT_EFFECT_FILTER, numVarEffPasses, numVarEffFails, instance);

            int numPathPasses = 542;
            int numPathFails = 453;
            FilterRunner pathFilterRunner = new FilterRunner(FilterType.PATHOGENICITY_FILTER, numPathPasses, numPathFails, instance);

            List.of(freqFilterRunner, varEffFilterRunner, pathFilterRunner)
                    .parallelStream()
                    .forEach(FilterRunner::run);

            FilterResultCount freqResultCount = new FilterResultCount(FilterType.FREQUENCY_FILTER, numFrequencyPasses, numFrequencyFails);
            FilterResultCount varEffResultCount = new FilterResultCount(FilterType.VARIANT_EFFECT_FILTER, numVarEffPasses, numVarEffFails);
            FilterResultCount pathResultCount = new FilterResultCount(FilterType.PATHOGENICITY_FILTER, numPathPasses, numPathFails);

            assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(freqResultCount));
            assertThat(instance.filterResultCount(FilterType.VARIANT_EFFECT_FILTER), equalTo(varEffResultCount));
            assertThat(instance.filterResultCount(FilterType.PATHOGENICITY_FILTER), equalTo(pathResultCount));

            List<FilterResultCount> filterResultCounts = instance.filterResultCounts();
            assertThat(filterResultCounts.size(), equalTo(3));
            for (FilterResultCount filterResultCount : filterResultCounts) {
                if (filterResultCount.filterType() == FilterType.FREQUENCY_FILTER) {
                    assertThat(filterResultCount, equalTo(freqResultCount));
                }
                if (filterResultCount.filterType() == FilterType.VARIANT_EFFECT_FILTER) {
                    assertThat(varEffResultCount, equalTo(varEffResultCount));
                }
                if (filterResultCount.filterType() == FilterType.PATHOGENICITY_FILTER) {
                    assertThat(filterResultCount, equalTo(pathResultCount));
                }
            }
        }
    }

    @Test
    void canLogMultiplePassAndFailCountsForSameFilterWithMultipleThreads() {

        for (int i = 0; i < 1000; i++) {
            FilterResultsCounter instance = new FilterResultsCounter();

            int threadOnePasses = 10;
            int threadOneFails = 99;
            FilterRunner threadOneFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadOnePasses, threadOneFails, instance);

            int threadTwoPasses = 123;
            int threadTwoFails = 321;
            FilterRunner threadTwoFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadTwoPasses, threadTwoFails, instance);

            int threadThreePasses = 542;
            int threadThreeFails = 453;
            FilterRunner threadThreeFilter = new FilterRunner(FilterType.FREQUENCY_FILTER, threadThreePasses, threadThreeFails, instance);

            List.of(threadOneFilter, threadTwoFilter, threadThreeFilter)
                    .parallelStream()
                    .forEach(FilterRunner::run);
            FilterResultCount expected = new FilterResultCount(FilterType.FREQUENCY_FILTER, threadOnePasses + threadTwoPasses + threadThreePasses, threadOneFails + threadTwoFails + threadThreeFails);
            assertThat(instance.filterResultCount(FilterType.FREQUENCY_FILTER), equalTo(expected));
            assertThat(instance.filtersRun(), equalTo(List.of(FilterType.FREQUENCY_FILTER)));
        }
    }

    /**
     * Utility class for testing FilterStats in a multi-threaded environment.
     */
    private static class FilterRunner implements Runnable {

        private final FilterType filterType;

        private final int numPass;
        private final int numFail;

        private final FilterResultsCounter filterResultsCounter;

        FilterRunner(FilterType filterType, int numPass, int numFail, FilterResultsCounter filterResultsCounter) {
            this.filterType = filterType;
            this.numPass = numPass;
            this.numFail = numFail;
            this.filterResultsCounter = filterResultsCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < numPass; i++) {
                filterResultsCounter.logResult(FilterResult.pass(filterType));
            }

            for (int i = 0; i < numFail; i++) {
                filterResultsCounter.logResult(FilterResult.fail(filterType));
            }
        }
    }
}