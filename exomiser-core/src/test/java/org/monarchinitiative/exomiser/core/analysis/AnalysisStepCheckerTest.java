
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.InheritanceFilter;
import org.monarchinitiative.exomiser.core.filters.KnownVariantFilter;
import org.monarchinitiative.exomiser.core.filters.PriorityScoreFilter;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriority;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisStepCheckerTest {

    private AnalysisStepChecker instance;

    private List<AnalysisStep> analysisSteps;
    private List<AnalysisStep> expectedSteps;

    private static final KnownVariantFilter KNOWN_VARIANT_FILTER = new KnownVariantFilter();
    private static final FrequencyFilter FREQUENCY_FILTER = new FrequencyFilter(0.1f);
    private static final InheritanceFilter INHERITANCE_FILTER = new InheritanceFilter(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    private static final OmimPriority OMIM_PRIORITISER = new OmimPriority(TestPriorityServiceFactory.STUB_SERVICE);
    private static final PriorityScoreFilter OMIM_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.OMIM_PRIORITY, 0f);
    private static final MockPrioritiser NONE_TYPE_PRIORITISER = new MockPrioritiser(PriorityType.NONE, new HashMap<>());
    private static final PriorityScoreFilter NONE_TYPE_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.NONE, 0f);
    private static final MockPrioritiser PHIVE_PRIORITISER = new MockPrioritiser(PriorityType.PHIVE_PRIORITY, new HashMap<>());
    private static final PriorityScoreFilter PHIVE_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0f);

    @BeforeEach
    public void setUp() {
        instance = new AnalysisStepChecker();

        analysisSteps = new ArrayList<>();
        expectedSteps = new ArrayList<>();
    }

    @Test
    public void testCheckEmptyListIsUnchanged() {
        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckOneStepIsUnchanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoStepsInLegalOrderIsUnchanged() {
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoVariantFilterStepsInIllegalOrderIsReordered() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoVariantFilterStepsAndInheritanceFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoVariantFilterStepsAndInheritanceFilterInIllegalOrderIsReordered() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoInheritanceModeDependentStepsInLegalOrderIsUnchanged() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoInheritanceModeDependentStepsInIllegalOrderIsReordered() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoInheritanceModeDependentStepsAndVariantFilterInIllegalOrderIsReordered() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheckInheritanceModeDependentStepsRunAfterPrioritiserAndVariantFilter() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoInheritanceModeDependentStepsAndTwoVariantFiltersInIllegalOrderIsReordered() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoPrioritisersAndVariantFiltersInLegalOrderIsUnchanged() {
        //in principle prioritisers other than OMIM can run at any time as they do not require variants
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITISER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckPrioritiserAndCorrectPriorityScoreFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(NONE_TYPE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckPrioritiserAndCorrectPriorityScoreFilterInIllegalOrderIsReordered() {
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITISER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckTwoPrioritisersAndCorrectPriorityScoreFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(NONE_TYPE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckPrioritisersAndInCorrectPriorityScoreFilterRemovesUnmatchedPriorityScoreFilter() {
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckPrioritisersAndPriorityScoreFilterRemovesUnmatchedPriorityScoreFilter() {
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckEverythingWrongGetsMadeRight() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(OMIM_PRIORITY_SCORE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);
        expectedSteps.add(OMIM_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheckEverythingWrongGetsMadeRightMoreInsanity() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(OMIM_PRIORITY_SCORE_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);
        expectedSteps.add(OMIM_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheckEverythingWrongGetsMadeRightIdiocyStrikesAgain() {
        analysisSteps.add(OMIM_PRIORITY_SCORE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);
        expectedSteps.add(OMIM_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheckOriginalExomiserAlgorithmIsntChanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITISER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheckGenomiserAlgorithmIsntChanged() {
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheckSlightlyOddYetNotIncorrectSetupIsntChanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(PHIVE_PRIORITISER);
        expectedSteps.add(PHIVE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
}
