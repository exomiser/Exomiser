/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.prioritisers.MockPrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    private static final InheritanceFilter INHERITANCE_FILTER = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT);
    private static final OMIMPriority OMIM_PRIORITISER = new OMIMPriority();
    private static final PriorityScoreFilter OMIM_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.OMIM_PRIORITY, 0f);
    private static final MockPrioritiser NONE_TYPE_PRIORITISER = new MockPrioritiser(PriorityType.NONE, new HashMap<>());
    private static final PriorityScoreFilter NONE_TYPE_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.NONE, 0f);
    private static final MockPrioritiser PHIVE_PRIORITISER = new MockPrioritiser(PriorityType.PHIVE_PRIORITY, new HashMap<>());
    private static final PriorityScoreFilter PHIVE_PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0f);

    @Before
    public void setUp() {
        instance = new AnalysisStepChecker();

        analysisSteps = new ArrayList<>();
        expectedSteps = new ArrayList<>();
    }

    @Test
    public void testCheck_EmptyListIsUnchanged() {
        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_OneStepIsUnchanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoStepsInLegalOrderIsUnchanged() {
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(KNOWN_VARIANT_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoVariantFilterStepsInIllegalOrderIsReordered() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoVariantFilterStepsAndInheritanceFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoVariantFilterStepsAndInheritanceFilterInIllegalOrderIsReordered() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(FREQUENCY_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(FREQUENCY_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoInheritanceModeDependentStepsInLegalOrderIsUnchanged() {
        analysisSteps.add(INHERITANCE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoInheritanceModeDependentStepsInIllegalOrderIsReordered() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoInheritanceModeDependentStepsAndVariantFilterInIllegalOrderIsReordered() {
        analysisSteps.add(OMIM_PRIORITISER);
        analysisSteps.add(KNOWN_VARIANT_FILTER);
        analysisSteps.add(INHERITANCE_FILTER);

        expectedSteps.add(KNOWN_VARIANT_FILTER);
        expectedSteps.add(INHERITANCE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }
    
    @Test
    public void testCheck_InheritanceModeDependentStepsRunAfterPrioritiserAndVariantFilter() {
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
    public void testCheck_TwoInheritanceModeDependentStepsAndTwoVariantFiltersInIllegalOrderIsReordered() {
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
    public void testCheck_TwoPrioritisersAndVariantFiltersInLegalOrderIsUnchanged() {
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
    public void testCheck_PrioritiserAndCorrectPriorityScoreFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(NONE_TYPE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_PrioritiserAndCorrectPriorityScoreFilterInIllegalOrderIsReordered() {
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(NONE_TYPE_PRIORITISER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_TwoPrioritisersAndCorrectPriorityScoreFilterInLegalOrderIsUnchanged() {
        analysisSteps.add(NONE_TYPE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        analysisSteps.add(OMIM_PRIORITISER);

        expectedSteps.add(NONE_TYPE_PRIORITISER);
        expectedSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);
        expectedSteps.add(OMIM_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_PrioritisersAndInCorrectPriorityScoreFilterRemovesUnmatchedPriorityScoreFilter() {
        analysisSteps.add(PHIVE_PRIORITISER);
        analysisSteps.add(NONE_TYPE_PRIORITY_SCORE_FILTER);

        expectedSteps.add(PHIVE_PRIORITISER);

        assertThat(instance.check(analysisSteps), equalTo(expectedSteps));
    }

    @Test
    public void testCheck_PrioritisersAndPriorityScoreFilterRemovesUnmatchedPriorityScoreFilter() {
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
    public void testCheck_EverythingWrongGetsMadeRight() {
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
    public void testCheck_EverythingWrongGetsMadeRightMoreInsanity() {
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
    public void testCheck_EverythingWrongGetsMadeRightIdiocyStrikesAgain() {
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
    public void testCheck_OriginalExomiserAlgorithmIsntChanged() {
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
    public void testCheck_GenomiserAlgorithmIsntChanged() {
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
    public void testCheck_SlightlyOddYetNotIncorrectSetupIsntChanged() {
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
