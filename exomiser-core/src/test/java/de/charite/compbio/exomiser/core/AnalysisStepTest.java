package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriority;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisStepTest {

    public static final PhivePriority PHIVE_PRIORITY = new PhivePriority(new ArrayList<>());
    public static final KnownVariantFilter KNOWN_VARIANT_FILTER = new KnownVariantFilter();
    public static final OMIMPriority OMIM_PRIORITY = new OMIMPriority();
    public static final InheritanceFilter INHERITANCE_FILTER = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);

    private AnalysisStep instance;

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
    public void testOnlyRequiresGenes_inheritanceModeDependantGeneFilter() {
        assertThat(INHERITANCE_FILTER.onlyRequiresGenes(), is(false));
    }

    @Test
    public void testOnlyRequiresGenes_inheritanceModeDependantPrioritiser() {
        assertThat(OMIM_PRIORITY.onlyRequiresGenes(), is(false));
    }

    @Test
    public void testOnlyRequiresGenes_variantFilter() {
        assertThat(KNOWN_VARIANT_FILTER.onlyRequiresGenes(), is(false));
    }

    @Test
    public void testOnlyRequiresGenes_otherPrioritiser() {
        assertThat(PHIVE_PRIORITY.onlyRequiresGenes(), is(true));
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

    @Test
    public void testIsGeneFilter_variantFilter() {
        assertThat(KNOWN_VARIANT_FILTER.isGeneFilter(), is(false));
    }

    @Test
    public void testIsGeneFilter_geneFilter() {
        assertThat(INHERITANCE_FILTER.isGeneFilter(), is(true));
    }
    @Test
    public void testIsGeneFilter_prioritiser() {
        assertThat(PHIVE_PRIORITY.isGeneFilter(), is(false));
    }

    @Test
    public void testIsPrioritiser_variantFilter() {
        assertThat(KNOWN_VARIANT_FILTER.isPrioritiser(), is(false));
    }

    @Test
    public void testIsPrioritiser_geneFilter() {
        assertThat(INHERITANCE_FILTER.isPrioritiser(), is(false));
    }

    @Test
    public void testIsPrioritiser_prioritiser() {
        assertThat(PHIVE_PRIORITY.isPrioritiser(), is(true));
    }
}