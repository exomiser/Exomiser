/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceStub;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisBuilderTest {

    private final PriorityFactory priorityFactory = new NoneTypePriorityFactoryStub();
    private final VariantDataService variantDataService = new VariantDataServiceStub();

    private AnalysisBuilder analysisBuilder;

    private List<String> hpoIds = Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055");

    @Before
    public void setUp() {
        analysisBuilder = new AnalysisBuilder(priorityFactory, variantDataService);
    }

    private List<AnalysisStep> analysisSteps() {
        return analysisBuilder.build().getAnalysisSteps();
    }

    @Test
    public void analysisStepsAreCheckedAndCorrectedOnBuild() {
        List<AnalysisStep> correct = Arrays.asList(
                priorityFactory.makePhivePrioritiser(),
                new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0.501f),
                new QualityFilter(500.0),
                new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT),
                priorityFactory.makeOmimPrioritiser()
        );

        //These are specified in a non-functional order.
        analysisBuilder.hpoIds(hpoIds)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .addPriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0.501f)
                .addPhivePrioritiser()
                .addOmimPrioritiser()
                .addInheritanceFilter()
                .addQualityFilter(500.0);

        assertThat(buildAndGetSteps(), equalTo(correct));
    }

    @Test
    public void testAnalysisBuilderVcfPath() {
        Path vcfPath = Paths.get("test.vcf");
        analysisBuilder.vcfPath(vcfPath);
        assertThat(analysisBuilder.build().getVcfPath(), equalTo(vcfPath));
    }

    @Test
    public void testAnalysisBuilderPedPath() {
        Path pedPath = Paths.get("ped.ped");
        analysisBuilder.pedPath(pedPath);
        assertThat(analysisBuilder.build().getPedPath(), equalTo(pedPath));
    }

    @Test
    public void testProbandSampleName() {
        String sampleName = "Zaphod Beeblebrox";
        analysisBuilder.probandSampleName(sampleName);
        assertThat(analysisBuilder.build().getProbandSampleName(), equalTo(sampleName));
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
        assertThat(analysisBuilder.build().getModeOfInheritance(), equalTo(ModeOfInheritance.ANY));
    }

    @Test
    public void testAnalysisBuilderModeOfInheritance() {
        analysisBuilder.modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(analysisBuilder.build().getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
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

    private List<AnalysisStep> buildAndGetSteps() {
        return analysisBuilder.build().getAnalysisSteps();
    }

    @Test
    public void testAddFailedVariantFilter() {
        analysisBuilder.addFailedVariantFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new FailedVariantFilter())));
    }

    @Test
    public void testAddIntervalFilter() {
        GeneticInterval geneticInterval = new GeneticInterval(1, 1234, 6789);
        analysisBuilder.addIntervalFilter(geneticInterval);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new IntervalFilter(geneticInterval))));
    }

    @Test
    public void testAddEntrezGeneIdFilter() {
        Set<Integer> entrezIds = Sets.newHashSet(123345, 67890);
        analysisBuilder.addGeneIdFilter(entrezIds);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new EntrezGeneIdFilter(entrezIds))));
    }

    @Test
    public void testAddVariantEffectFilter() {
        Set<VariantEffect> variantEffects = EnumSet.of(VariantEffect.MISSENSE_VARIANT, VariantEffect.REGULATORY_REGION_VARIANT);
        analysisBuilder.addVariantEffectFilter(variantEffects);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new VariantEffectFilter(variantEffects))));
    }

    @Test
    public void testAddQualityFilter() {
        double cutoff = 500.0;
        analysisBuilder.addQualityFilter(cutoff);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new QualityFilter(cutoff))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddKnownVariantFilterThrowsExceptionWhenFrequencySourcesAreNotDefined() {
        analysisBuilder.addKnownVariantFilter();
    }

    @Test
    public void testAddKnownVariantFilter() {
        analysisBuilder.frequencySources(EnumSet.allOf(FrequencySource.class));
        analysisBuilder.addKnownVariantFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new KnownVariantFilter())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFrequencyFilterThrowsExceptionWhenFrequencySourcesAreNotDefined() {
        analysisBuilder.addFrequencyFilter(0.01f);
    }

    @Test
    public void testAddFrequencyFilter() {
        Set<FrequencySource> sources = EnumSet.of(FrequencySource.ESP_ALL, FrequencySource.THOUSAND_GENOMES);
        analysisBuilder.frequencySources(sources);
        float cutOff = 0.01f;
        analysisBuilder.addFrequencyFilter(cutOff);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new FrequencyFilter(cutOff))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPathogenicityFilterStepThrowsExceptionWhenPathogenicitySourcesAreNotDefined() {
        analysisBuilder.addPathogenicityFilter(true);
    }

    @Test
    public void testAddPathogenicityFilter() {
        Set<PathogenicitySource> sources = EnumSet.allOf(PathogenicitySource.class);
        analysisBuilder.pathogenicitySources(sources);
        boolean keepNonPathogenic = true;
        analysisBuilder.addPathogenicityFilter(keepNonPathogenic);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new PathogenicityFilter(keepNonPathogenic))));
    }

    @Test
    public void testAddPriorityScoreFilter() {
        PriorityType priorityType = PriorityType.HIPHIVE_PRIORITY;
        float minPriorityScore = 0.501f;
        analysisBuilder.addPriorityScoreFilter(priorityType, minPriorityScore);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new PriorityScoreFilter(priorityType, minPriorityScore))));
    }

    @Test
    public void testAddRegulatoryFeatureFilter() {
        analysisBuilder.addRegulatoryFeatureFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new RegulatoryFeatureFilter())));
    }

    @Test
    public void testAddInheritanceModeFilterNotAddedWhenModeUndefined() {
        analysisBuilder.addInheritanceFilter();
        assertThat(buildAndGetSteps(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testAddInheritanceModeFilter() {
        ModeOfInheritance autosomalDominant = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        analysisBuilder.modeOfInheritance(autosomalDominant);
        analysisBuilder.addInheritanceFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new InheritanceFilter(autosomalDominant))));
    }

    @Test
    public void testAnalysisBuilderCanBuildCompleteAnalysis() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN, FrequencySource.EXAC_EAST_ASIAN);
        float frequencyCutOff = 1f;
        FrequencyFilter frequencyFilter = new FrequencyFilter(frequencyCutOff);

        PhivePriority phivePrioritiser = priorityFactory.makePhivePrioritiser();

        PriorityType priorityType = phivePrioritiser.getPriorityType();
        float minPriorityScore = 0.501f;
        PriorityScoreFilter priorityScoreFilter = new PriorityScoreFilter(priorityType, minPriorityScore);
        RegulatoryFeatureFilter regulatoryFeatureFilter = new RegulatoryFeatureFilter();

        analysisBuilder.hpoIds(hpoIds)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .analysisMode(AnalysisMode.FULL)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .addPhivePrioritiser()
                .addPriorityScoreFilter(priorityType, minPriorityScore)
                .addRegulatoryFeatureFilter()
                .addFrequencyFilter(frequencyCutOff);

        Analysis analysis = analysisBuilder.build();
        assertThat(analysis.getHpoIds(), equalTo(hpoIds));
        assertThat(analysis.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        assertThat(analysis.getAnalysisSteps(), hasItem(priorityScoreFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(frequencyFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(phivePrioritiser));
        assertThat(analysis.getAnalysisSteps(), hasItem(regulatoryFeatureFilter));
        //check that the order of analysis steps is preserved
        assertThat(analysis.getAnalysisSteps(), equalTo(Arrays.asList(phivePrioritiser, priorityScoreFilter, regulatoryFeatureFilter, frequencyFilter)));
    }

    @Test
    public void testCanSpecifyOmimPrioritiser() {
        Prioritiser prioritiser = priorityFactory.makeOmimPrioritiser();

        analysisBuilder.addOmimPrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPhivePrioritiserThrowsExcptionWhenHpoIdsNotDefined() {
        analysisBuilder.addPhivePrioritiser();
        assertThat(analysisSteps(), equalTo(singletonList(priorityFactory.makePhivePrioritiser())));
    }

    @Test
    public void testCanSpecifyPhivePrioritiser() {
        Prioritiser prioritiser = priorityFactory.makePhivePrioritiser();

        analysisBuilder.hpoIds(hpoIds);
        analysisBuilder.addPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHiPhivePrioritiserThrowsExceptionWhenNoHpoIdsDefined() {
        Prioritiser prioritiser = priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.DEFAULT);

        analysisBuilder.addHiPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiser_noOptions() {
        Prioritiser prioritiser = priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.DEFAULT);

        analysisBuilder.hpoIds(hpoIds)
                .addHiPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiser_withOptions() {
        HiPhiveOptions options = HiPhiveOptions.builder()
                .diseaseId("DISEASE:123")
                .candidateGeneSymbol("GENE1")
                .runParams("human,mouse,fish,ppi")
                .build();

        Prioritiser prioritiser = priorityFactory.makeHiPhivePrioritiser(options);

        analysisBuilder.hpoIds(hpoIds)
                .addHiPhivePrioritiser(options);

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPhenixPrioritiserThrowsExceptionWhenNoHpoIdsDefined() {
        Prioritiser prioritiser = priorityFactory.makeLegacyPhenixPrioritiser();

        analysisBuilder.addPhenixPrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyPhenixPrioritiser() {
        Prioritiser prioritiser = priorityFactory.makeLegacyPhenixPrioritiser();

        analysisBuilder.hpoIds(hpoIds)
                .addPhenixPrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExomeWalkerPrioritiserThrowsExceptionWithEmptyList() {
        analysisBuilder.addExomeWalkerPrioritiser(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExomeWalkerPrioritiserThrowsExceptionWithNullList() {
        analysisBuilder.addExomeWalkerPrioritiser(null);
    }

    @Test
    public void testCanSpecifyExomeWalkerPrioritiser() {
        List<Integer> seedGenes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        Prioritiser prioritiser = priorityFactory.makeExomeWalkerPrioritiser(seedGenes);

        analysisBuilder.addExomeWalkerPrioritiser(seedGenes);

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyTwoPrioritisers() {
        Prioritiser omimPrioritiser = priorityFactory.makeOmimPrioritiser();
        Prioritiser phivePrioritiser = priorityFactory.makePhivePrioritiser();
        List<AnalysisStep> steps = Arrays.asList(omimPrioritiser, phivePrioritiser);

        analysisBuilder.hpoIds(hpoIds)
                .addOmimPrioritiser()
                .addPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanAddFilterStep() {
        AnalysisStep filter = new PassAllVariantEffectsFilter();

        analysisBuilder.addAnalysisStep(filter);

        assertThat(analysisSteps(), equalTo(singletonList(filter)));
    }
}