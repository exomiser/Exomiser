package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriority;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisStepTest {

    private static final AnalysisStep PHIVE_PRIORITY = new PhivePriority(new ArrayList<>());
    private static final AnalysisStep PRIORITY_SCORE_FILTER = new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0.6f);
    private static final AnalysisStep KNOWN_VARIANT_FILTER = new KnownVariantFilter();
    private static final AnalysisStep OMIM_PRIORITY = new OMIMPriority();
    private static final AnalysisStep INHERITANCE_FILTER = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
    
    @Test
    public void testIsInheritanceModeDependent_OMIMPriority() {
        assertThat(OMIM_PRIORITY.isInheritanceModeDependent(), is(true));
    }

    @Test
    public void testIsInheritanceModeDependent_InheritanceModeFilter() {
        assertThat(INHERITANCE_FILTER.isInheritanceModeDependent(), is(true));
    }

    @Test
    public void testIsInheritanceModeDependent_notInheritanceModeDependant() {
        assertThat(KNOWN_VARIANT_FILTER.isInheritanceModeDependent(), is(false));
    }

    @Test
    public void testIsOnlyGeneDependent_inheritanceModeDependantGeneFilter() {
        assertThat(INHERITANCE_FILTER.isOnlyGeneDependent(), is(false));
    }

    @Test
    public void testIsOnlyGeneDependent_inheritanceModeDependantPrioritiser() {
        assertThat(OMIM_PRIORITY.isOnlyGeneDependent(), is(false));
    }

    @Test
    public void testIsOnlyGeneDependent_variantFilter() {
        assertThat(KNOWN_VARIANT_FILTER.isOnlyGeneDependent(), is(false));
    }

    @Test
    public void testIsOnlyGeneDependent_otherPrioritiser() {
        assertThat(PHIVE_PRIORITY.isOnlyGeneDependent(), is(true));
    }

    @Test
    public void testIsOnlyGeneDependent_priorityScoreFilter() {
        assertThat(PRIORITY_SCORE_FILTER.isOnlyGeneDependent(), is(true));
    }

    @Test
    public void testIsVariantFilter_variantFilter() {
        assertThat(KNOWN_VARIANT_FILTER.isVariantFilter(), is(true));
    }

    @Test
    public void testIsVariantFilter_geneFilter() {
        assertThat(INHERITANCE_FILTER.isVariantFilter(), is(false));
    }

    @Test
    public void testIsVariantFilter_prioritiser() {
        assertThat(OMIM_PRIORITY.isVariantFilter(), is(false));
    }

}