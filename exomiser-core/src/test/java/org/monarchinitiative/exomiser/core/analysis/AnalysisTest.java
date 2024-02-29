
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisTest {

    private static final Analysis DEFAULT_ANALYSIS = Analysis.builder().build();

    private Analysis.Builder newBuilder() {
        return Analysis.builder();
    }

    private List<AnalysisStep> getAnalysisSteps() {
        VariantFilter geneIdFilter = new GeneSymbolFilter(new HashSet<>());
        Prioritiser<?> noneTypePrioritiser = new NoneTypePrioritiser();
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.ANY);
        VariantFilter targetFilter = new PassAllVariantEffectsFilter();

        return List.of(geneIdFilter, noneTypePrioritiser, inheritanceFilter, targetFilter);
    }

    @Test
    public void modeOfInheritanceDefaultsToEmpty() {
        assertThat(DEFAULT_ANALYSIS.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.empty()));
    }

    @Test
    public void canSetModeOfInheritanceDefaults() {
        Analysis instance = newBuilder()
                .inheritanceModeOptions(InheritanceModeOptions.defaults())
                .build();
        assertThat(instance.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
    }

    @Test
    public void testCanMakeAnalysisWithInheritanceModesFromMap() {
        Map<SubModeOfInheritance, Float> inheritanceMap = Map.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);

        Analysis instance = newBuilder()
                .inheritanceModeOptions(inheritanceMap)
                .build();

        assertThat(instance.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.of(inheritanceMap)));
    }

    @Test
    public void testCanMakeAnalysisWithInheritanceModesFromInheritanceModeOptions() {
        Map<SubModeOfInheritance, Float> inheritanceMap = Map.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);
        InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.of(inheritanceMap);

        Analysis instance = newBuilder()
                .inheritanceModeOptions(inheritanceModeOptions)
                .build();

        assertThat(instance.getInheritanceModeOptions(), equalTo(inheritanceModeOptions));
    }

    @Test
    public void analysisModeDefaultsToPassOnly() {
        assertThat(DEFAULT_ANALYSIS.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void analysisCanSpecifyAlternativeAnalysisMode() {
        Analysis instance = newBuilder()
                .analysisMode(AnalysisMode.FULL)
                .build();
        assertThat(instance.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testFrequencySourcesAreEmptyByDefault() {
        assertThat(DEFAULT_ANALYSIS.getFrequencySources().isEmpty(), is(true));
    }

    @Test
    public void canSpecifyFrequencySources() {
        Set<FrequencySource> sources = EnumSet.allOf(FrequencySource.class);
        Analysis instance = newBuilder()
                .frequencySources(sources)
                .build();
        assertThat(instance.getFrequencySources(), equalTo(sources));
    }

    @Test
    public void testPathogenicitySourcesAreEmptyByDefault() {
        assertThat(DEFAULT_ANALYSIS.getPathogenicitySources().isEmpty(), is(true));
    }

    @Test
    public void canSpecifyPathogenicitySources() {
        Set<PathogenicitySource> sources = EnumSet.allOf(PathogenicitySource.class);
        Analysis instance = newBuilder()
                .pathogenicitySources(sources)
                .build();
        assertThat(instance.getPathogenicitySources(), equalTo(sources));
    }

    @Test
    public void testGetAnalysisStepsReturnsEmptyListWhenNoStepsHaveBeenAdded() {
        List<AnalysisStep> steps = Collections.emptyList();
        assertThat(DEFAULT_ANALYSIS.getAnalysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanAddVariantFilterAsAnAnalysisStep() {
        VariantFilter variantFilter = new PassAllVariantEffectsFilter();
        Analysis instance = newBuilder()
                .addStep(variantFilter)
                .build();
        assertThat(instance.getAnalysisSteps(), hasItem(variantFilter));
    }

    @Test
    public void testCanAddGeneFilterAsAnAnalysisStep() {
        GeneFilter geneFilter = new InheritanceFilter(ModeOfInheritance.ANY);
        Analysis instance = newBuilder()
                .addStep(geneFilter)
                .build();
        assertThat(instance.getAnalysisSteps(), hasItem(geneFilter));
    }

    @Test
    public void testCanAddPrioritiserAsAnAnalysisStep() {
        Prioritiser<? extends PriorityResult> prioritiser = new NoneTypePrioritiser();
        Analysis instance = newBuilder()
                .addStep(prioritiser)
                .build();
        assertThat(instance.getAnalysisSteps(), hasItem(prioritiser));
    }

    @Test
    public void testGetMainPrioritiserType() {
        Prioritiser<? extends PriorityResult> prioritiser = new NoneTypePrioritiser();
        Analysis instance = newBuilder()
                .addStep(new OmimPriority(TestPriorityServiceFactory.stubPriorityService()))
                .addStep(prioritiser)
                .build();
        assertThat(instance.getMainPrioritiserType(), equalTo(prioritiser.getPriorityType()));
    }

    @Test
    public void testGetMainPrioritiserTypeNoPrioritisersSet() {
        Analysis instance = newBuilder()
                .build();
        assertThat(instance.getMainPrioritiserType(), equalTo(PriorityType.NONE));
    }

    @Test
    public void testGetMainPrioritiser() {
        Prioritiser<? extends PriorityResult> prioritiser = new NoneTypePrioritiser();
        Analysis instance = newBuilder()
                .addStep(new OmimPriority(TestPriorityServiceFactory.stubPriorityService()))
                .addStep(prioritiser)
                .build();
        assertThat(instance.getMainPrioritiser(), equalTo(prioritiser));
    }

    @Test
    public void testGetMainPrioritiserNoPrioritisersSet() {
        Analysis instance = newBuilder()
                .build();
        assertThat(instance.getMainPrioritiser(), equalTo(null));
    }

    @Test
    public void testGetAnalysisStepsReturnsListOfStepsAdded() {
        List<AnalysisStep> steps = getAnalysisSteps();

        Analysis.Builder builder = newBuilder();
        steps.forEach(builder::addStep);

        Analysis instance = builder.build();

        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }

    @Test
    public void testBuilderCanSetAllSteps() {
        List<AnalysisStep> steps = getAnalysisSteps();

        Analysis instance = newBuilder()
                .steps(steps)
                .build();

        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }

    @Test
    public void testCopyReturnsBuilder() {
        Analysis.Builder analysisBuilder = DEFAULT_ANALYSIS.copy();
        Analysis copy = analysisBuilder.build();
        assertThat(copy, equalTo(DEFAULT_ANALYSIS));
    }

    @Test
    public void testCopyBuilderCanChangeFrequencySources() {
        Analysis.Builder analysisBuilder = DEFAULT_ANALYSIS.copy();
        Analysis copy = analysisBuilder
                .frequencySources(EnumSet.of(FrequencySource.ESP_ALL))
                .build();
        assertThat(copy.getFrequencySources(), equalTo(EnumSet.of(FrequencySource.ESP_ALL)));
    }

    @Test
    public void testCopyBuilderCanAddNewAnalysisStep() {
        // this is likely a nonsense thing to do - the order of the steps is important so copying and adding a new
        // one is probably a silly thing to do.
        Analysis copy = DEFAULT_ANALYSIS.copy()
                .addStep(new NoneTypePrioritiser())
                .build();
    }

    @Test
    public void testCopyBuilderCanSetAnalysisSteps() {
        List<AnalysisStep> newSteps = getAnalysisSteps();
        Analysis copy = DEFAULT_ANALYSIS.copy()
                .steps(newSteps)
                .build();
        assertThat(copy.getAnalysisSteps(), equalTo(newSteps));
    }
}
