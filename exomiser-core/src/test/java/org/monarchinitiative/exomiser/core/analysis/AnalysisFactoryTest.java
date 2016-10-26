/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.AnalysisFactory.AnalysisBuilder;
import org.monarchinitiative.exomiser.core.factories.VariantDataService;
import org.monarchinitiative.exomiser.core.factories.VariantDataServiceStub;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.PassAllVariantEffectsFilter;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisFactoryTest {

    private AnalysisFactory instance;
    private PriorityFactory priorityFactory;

    private AnalysisBuilder analysisBuilder;
    private List<AnalysisStep> steps;

    private List<String> hpoIds;


    @Before
    public void setUp() {
        VariantDataService stubVariantDataService = new VariantDataServiceStub();
        priorityFactory = new NoneTypePriorityFactoryStub();

        instance = new AnalysisFactory(null, priorityFactory, stubVariantDataService);

        hpoIds = Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055");
        steps = new ArrayList<>();

        Path vcfPath = Paths.get("test.vcf");
        analysisBuilder = instance.getAnalysisBuilder(vcfPath);
    }

    private List<AnalysisStep> analysisSteps() {
        return analysisBuilder.build().getAnalysisSteps();
    }

    @Test
    public void testCanMakeFullAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.FULL);
        assertThat(SimpleAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testCanMakeSparseAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.SPARSE);
        assertThat(SparseAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testCanMakePassOnlyAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.PASS_ONLY);
        assertThat(PassOnlyAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testAnalysisBuilderPedPath_default() {
        assertThat(analysisBuilder.build().getPedPath(), nullValue());
    }

    @Test
    public void testAnalysisBuilderPedPath() {
        Path pedPath = Paths.get("ped.ped");
        analysisBuilder.pedPath(pedPath);
        assertThat(analysisBuilder.build().getPedPath(), equalTo(pedPath));
    }

    @Test
    public void testAnalysisBuilderHpoIds_default() {
        assertThat(analysisBuilder.build().getHpoIds(), equalTo(Collections.<String>emptyList()));
    }

    @Test
    public void testAnalysisBuilderHpoIds() {
        analysisBuilder.hpoIds(hpoIds);
        assertThat(analysisBuilder.build().getHpoIds(), equalTo(hpoIds));
    }

    @Test
    public void testAnalysisBuilderModeOfInheritance_default() {
        assertThat(analysisBuilder.build().getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED));
    }

    @Test
    public void testAnalysisBuilderModeOfInheritance() {
        analysisBuilder.modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(analysisBuilder.build().getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    @Test
    public void testAnalysisBuilderScoringMode_default() {
        assertThat(analysisBuilder.build().getScoringMode(), equalTo(ScoringMode.RAW_SCORE));
    }

    @Test
    public void testAnalysisBuilderScoringMode() {
        analysisBuilder.scoringMode(ScoringMode.RANK_BASED);
        assertThat(analysisBuilder.build().getScoringMode(), equalTo(ScoringMode.RANK_BASED));
    }

    @Test
    public void testAnalysisBuilderAnalysisMode_default() {
        assertThat(analysisBuilder.build().getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void testAnalysisBuilderAnalysisMode() {
        analysisBuilder.analysisMode(AnalysisMode.FULL);
        assertThat(analysisBuilder.build().getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testAnalysisBuilderFrequencySources_default() {
        assertThat(analysisBuilder.build().getFrequencySources(), equalTo(Collections.<FrequencySource>emptySet()));
    }

    @Test
    public void testAnalysisBuilderFrequencySources() {
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN, FrequencySource.EXAC_EAST_ASIAN);
        analysisBuilder.frequencySources(frequencySources);
        assertThat(analysisBuilder.build().getFrequencySources(), equalTo(frequencySources));
    }

    @Test
    public void testAnalysisBuilderPathogenicitySources_default() {
        assertThat(analysisBuilder.build().getPathogenicitySources(), equalTo(Collections.<PathogenicitySource>emptySet()));
    }

    @Test
    public void testAnalysisBuilderPathogenicitySources() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        analysisBuilder.pathogenicitySources(pathogenicitySources);
        assertThat(analysisBuilder.build().getPathogenicitySources(), equalTo(pathogenicitySources));
    }

    @Test
    public void testAnalysisBuilderCanBuildCompleteAnalysis() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN, FrequencySource.EXAC_EAST_ASIAN);
        FrequencyFilter frequencyFilter = new FrequencyFilter(1f);
        PhivePriority phivePrioritiser = priorityFactory.makePhivePrioritiser(hpoIds);

        analysisBuilder.hpoIds(hpoIds)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .analysisMode(AnalysisMode.FULL)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .addAnalysisStep(frequencyFilter)
                .addPhivePrioritiser(hpoIds);

        Analysis analysis = analysisBuilder.build();
        assertThat(analysis.getHpoIds(), equalTo(hpoIds));
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        assertThat(analysis.getAnalysisSteps(), hasItem(frequencyFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(phivePrioritiser));
        //check that the order of analysis steps is preserved
        assertThat(analysis.getAnalysisSteps(), equalTo(Arrays.asList(frequencyFilter, phivePrioritiser)));
    }

    @Test
    public void testCanSpecifyOmimPrioritiser() {
        Prioritiser prioritiser = priorityFactory.makeOmimPrioritiser();

        analysisBuilder.addOmimPrioritiser();

        assertThat(analysisSteps(), hasItem(prioritiser));
    }

    @Test
    public void testCanSpecifyPhivePrioritiser() {
        steps.add(priorityFactory.makePhivePrioritiser(hpoIds));

        analysisBuilder.addPhivePrioritiser(hpoIds);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiser_noOptions() {
        steps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, HiPhiveOptions.DEFAULT));

        analysisBuilder.addHiPhivePrioritiser(hpoIds);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiser_withOptions() {
        HiPhiveOptions options = HiPhiveOptions.builder()
                .diseaseId("DISEASE:123")
                .candidateGeneSymbol("GENE1")
                .runParams("human,mouse,fish,ppi")
                .build();

        steps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, options));

        analysisBuilder.addHiPhivePrioritiser(hpoIds, options);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyPhenixPrioritiser() {
        steps.add(priorityFactory.makePhenixPrioritiser(hpoIds));

        analysisBuilder.addPhenixPrioritiser(hpoIds);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyExomeWalkerPrioritiser() {
        List<Integer> seedGenes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        steps.add(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));

        analysisBuilder.addExomeWalkerPrioritiser(seedGenes);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyTwoPrioritisers() {
        steps.add(priorityFactory.makeOmimPrioritiser());
        steps.add(priorityFactory.makePhivePrioritiser(hpoIds));

        analysisBuilder.addOmimPrioritiser();
        analysisBuilder.addPhivePrioritiser(hpoIds);

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanAddFilterStep() {
        AnalysisStep filter = new PassAllVariantEffectsFilter();
        steps.add(filter);

        analysisBuilder.addAnalysisStep(filter);

        assertThat(analysisSteps(), equalTo(steps));
    }
}
