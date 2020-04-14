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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.InheritanceFilter;
import org.monarchinitiative.exomiser.core.filters.PathogenicityFilter;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AnalysisGroupTest {

    @Test
    void throwsErrorWithEmptyConstructor() {
        assertThrows(IllegalArgumentException.class, AnalysisGroup::of);
    }

    @Test
    void throwsErrorWhenSuppliedWithEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> AnalysisGroup.of(List.of()));
    }

    @Test
    void throwsErrorWhenStepsAreNotOfSameGroupType() {
        AnalysisStep filterStep = new PathogenicityFilter(true);
        AnalysisStep inheritanceModeDependant = new InheritanceFilter();
        assertThrows(IllegalArgumentException.class, () -> AnalysisGroup.of(filterStep, inheritanceModeDependant));
    }

    @Test
    void createSingleStepGroup() {
        AnalysisStep filterStep = new PathogenicityFilter(true);
        assertThat(AnalysisGroup.of(filterStep).getAnalysisSteps(), equalTo(List.of(filterStep)));
    }

    @Test
    void createMultiStepGroup() {
        AnalysisStep frequencyFilter = new FrequencyFilter(0.1f);
        AnalysisStep pathogenicityFilter = new PathogenicityFilter(true);
        assertThat(AnalysisGroup.of(frequencyFilter, pathogenicityFilter).getAnalysisSteps(),
                equalTo(List.of(frequencyFilter, pathogenicityFilter)));
    }
}