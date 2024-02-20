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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.*;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AnalysisBuilderTest {

    private final PriorityFactory priorityFactory = new NoneTypePriorityFactoryStub();
    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(TestFactory
            .buildDefaultHg19GenomeAnalysisService());
    private final OntologyService ontologyService = TestOntologyService.builder().build();

    private AnalysisBuilder analysisBuilder;

    private final List<String> hpoIds = Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055");

    @BeforeEach
    public void setUp() {
        analysisBuilder = new AnalysisBuilder(genomeAnalysisServiceProvider, priorityFactory, ontologyService);
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
                new InheritanceFilter(InheritanceModeOptions.defaults().getDefinedModes()),
                priorityFactory.makeOmimPrioritiser()
        );

        //These are specified in a non-functional order.
        analysisBuilder
                .inheritanceModes(InheritanceModeOptions.defaults())
                .addPriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0.501f)
                .addPhivePrioritiser()
                .addOmimPrioritiser()
                .addInheritanceFilter()
                .addQualityFilter(500.0);

        assertThat(buildAndGetSteps(), equalTo(correct));
    }

    @Test
    public void testAnalysisBuilderModeOfInheritanceDefault() {
        assertThat(analysisBuilder.build().getInheritanceModeOptions(), equalTo(InheritanceModeOptions.empty()));
    }

    @Test
    public void testAnalysisBuilderModeOfInheritance() {
        analysisBuilder.inheritanceModes(InheritanceModeOptions.defaults());
        assertThat(analysisBuilder.build().getInheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
    }

    @Test
    public void testAnalysisBuilderAnalysisModeDefault() {
        assertThat(analysisBuilder.build().getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void testAnalysisBuilderAnalysisMode() {
        analysisBuilder.analysisMode(AnalysisMode.FULL);
        assertThat(analysisBuilder.build().getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testAnalysisBuilderFrequencySourcesDefault() {
        assertThat(analysisBuilder.build().getFrequencySources(), equalTo(Collections.<FrequencySource>emptySet()));
    }

    @Test
    public void testAnalysisBuilderFrequencySources() {
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AA, FrequencySource.EXAC_EAST_ASIAN);
        analysisBuilder.frequencySources(frequencySources);
        assertThat(analysisBuilder.build().getFrequencySources(), equalTo(frequencySources));
    }

    @Test
    public void testAnalysisBuilderPathogenicitySourcesDefault() {
        assertThat(analysisBuilder.build().getPathogenicitySources(), equalTo(Collections.<PathogenicitySource>emptySet()));
    }

    @Test
    public void testAnalysisBuilderPathogenicitySources() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        analysisBuilder.pathogenicitySources(pathogenicitySources);
        assertThat(analysisBuilder.build().getPathogenicitySources(), equalTo(pathogenicitySources));
    }

    private List<AnalysisStep> buildAndGetSteps() {
        Analysis analysis = analysisBuilder.build();
        return analysis.getAnalysisSteps();
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
        Set<String> entrezIds = Sets.newHashSet("FGFR1", "FGFR2");
        analysisBuilder.addGeneIdFilter(entrezIds);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new GeneSymbolFilter(entrezIds))));
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

    @Test
    public void testAddKnownVariantFilterThrowsExceptionWhenFrequencySourcesAreNotDefined() {
        assertThrows(IllegalArgumentException.class, () -> analysisBuilder.addKnownVariantFilter());
    }

    @Test
    public void testAddKnownVariantFilter() {
        analysisBuilder.frequencySources(EnumSet.allOf(FrequencySource.class));
        analysisBuilder.addKnownVariantFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new KnownVariantFilter())));
    }

    @Test
    public void testAddFrequencyFilterThrowsExceptionWhenFrequencySourcesAreNotDefined() {
        assertThrows(IllegalArgumentException.class, () -> analysisBuilder.addFrequencyFilter(0.01f));
    }

    @Test
    public void testAddFrequencyFilter() {
        float cutOff = 0.01f;
        analysisBuilder = analysisBuilderWithFrequencyFilter(cutOff);
        assertThat(buildAndGetSteps(), equalTo(singletonList(new FrequencyFilter(cutOff))));
    }

    @Test
    public void testAddFrequencyFilterDoesntChangeIfInheritanceModeOptionsIsEmpty() {
        float cutOff = 0.01f;
        analysisBuilder = analysisBuilderWithFrequencyFilter(cutOff);
        analysisBuilder.inheritanceModes(InheritanceModeOptions.empty());
        assertThat(buildAndGetSteps(), equalTo(singletonList(new FrequencyFilter(cutOff))));
    }

    private AnalysisBuilder analysisBuilderWithFrequencyFilter(float cutOff) {
        analysisBuilder.frequencySources(EnumSet.of(FrequencySource.ESP_ALL, FrequencySource.THOUSAND_GENOMES));
        analysisBuilder.addFrequencyFilter(cutOff);
        return analysisBuilder;
    }

    @Test
    public void testAddNoArgsFrequencyUsingInheritanceModeCutoffMaxFrequency() {
        analysisBuilder.frequencySources(EnumSet.of(FrequencySource.ESP_ALL, FrequencySource.THOUSAND_GENOMES));
        analysisBuilder.inheritanceModes(InheritanceModeOptions.defaults());
        analysisBuilder.addFrequencyFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new FrequencyFilter(InheritanceModeOptions.defaults().getMaxFreq()))));
    }

    @Test
    public void testAddNoArgsFrequencyWhenInheritanceModeOptionsAreEmpty() {
        analysisBuilder.frequencySources(EnumSet.of(FrequencySource.ESP_ALL, FrequencySource.THOUSAND_GENOMES));
        analysisBuilder.inheritanceModes(InheritanceModeOptions.empty());
        assertThrows(IllegalArgumentException.class, () -> analysisBuilder.addFrequencyFilter());
    }

    @Test
    public void testAddPathogenicityFilter() {
        analysisBuilder.pathogenicitySources(EnumSet.allOf(PathogenicitySource.class));
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
        analysisBuilder.inheritanceModes(InheritanceModeOptions.of(ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f)));
        analysisBuilder.addInheritanceFilter();
        assertThat(buildAndGetSteps(), equalTo(singletonList(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT))));
    }

    @Test
    public void testAnalysisBuilderCanBuildCompleteAnalysis() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AA, FrequencySource.EXAC_EAST_ASIAN);
        float frequencyCutOff = 2f;
        FrequencyFilter frequencyFilter = new FrequencyFilter(frequencyCutOff);
        GeneBlacklistFilter geneBlacklistFilter = new GeneBlacklistFilter();

        PhivePriority phivePrioritiser = priorityFactory.makePhivePrioritiser();

        PriorityType priorityType = phivePrioritiser.getPriorityType();
        float minPriorityScore = 0.501f;
        PriorityScoreFilter priorityScoreFilter = new PriorityScoreFilter(priorityType, minPriorityScore);
        RegulatoryFeatureFilter regulatoryFeatureFilter = new RegulatoryFeatureFilter();

        analysisBuilder
                .inheritanceModes(InheritanceModeOptions.defaults())
                .analysisMode(AnalysisMode.FULL)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .addPhivePrioritiser()
                .addPriorityScoreFilter(priorityType, minPriorityScore)
                .addRegulatoryFeatureFilter()
                .addFrequencyFilter(frequencyCutOff)
                .addGeneBlacklistFilter();

        Analysis analysis = analysisBuilder.build();
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
        assertThat(analysis.getFrequencySources(), equalTo(frequencySources));
        assertThat(analysis.getPathogenicitySources(), equalTo(pathogenicitySources));
        assertThat(analysis.getAnalysisSteps(), hasItem(priorityScoreFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(frequencyFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(phivePrioritiser));
        assertThat(analysis.getAnalysisSteps(), hasItem(regulatoryFeatureFilter));
        assertThat(analysis.getAnalysisSteps(), hasItem(geneBlacklistFilter));
        //check that the order of analysis steps is preserved
        assertThat(analysis.getAnalysisSteps(), equalTo(Arrays.asList(phivePrioritiser, priorityScoreFilter, regulatoryFeatureFilter, frequencyFilter, geneBlacklistFilter)));
    }

    @Test
    public void testCanSpecifyOmimPrioritiser() {
        OmimPriority prioritiser = priorityFactory.makeOmimPrioritiser();

        analysisBuilder.addOmimPrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyPhivePrioritiser() {
        PhivePriority prioritiser = priorityFactory.makePhivePrioritiser();

        analysisBuilder.addPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiserNoOptions() {
        HiPhivePriority prioritiser = priorityFactory.makeHiPhivePrioritiser(HiPhiveOptions.defaults());

        analysisBuilder.addHiPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyHiPhivePrioritiserWithOptions() {
        HiPhiveOptions options = HiPhiveOptions.builder()
                .diseaseId("DISEASE:123")
                .candidateGeneSymbol("GENE1")
                .runParams("human,mouse,fish,ppi")
                .build();

        HiPhivePriority prioritiser = priorityFactory.makeHiPhivePrioritiser(options);

        analysisBuilder.addHiPhivePrioritiser(options);

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyPhenixPrioritiser() {
        PhenixPriority prioritiser = priorityFactory.makePhenixPrioritiser();

        analysisBuilder.addPhenixPrioritiser();

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testExomeWalkerPrioritiserThrowsExceptionWithEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> analysisBuilder.addExomeWalkerPrioritiser(Collections.emptyList()));
    }

    @Test
    public void testExomeWalkerPrioritiserThrowsExceptionWithNullList() {
        assertThrows(IllegalArgumentException.class, () -> analysisBuilder.addExomeWalkerPrioritiser(null));
    }

    @Test
    public void testCanSpecifyExomeWalkerPrioritiser() {
        List<Integer> seedGenes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        ExomeWalkerPriority prioritiser = priorityFactory.makeExomeWalkerPrioritiser(seedGenes);

        analysisBuilder.addExomeWalkerPrioritiser(seedGenes);

        assertThat(analysisSteps(), equalTo(singletonList(prioritiser)));
    }

    @Test
    public void testCanSpecifyTwoPrioritisers() {
        OmimPriority omimPrioritiser = priorityFactory.makeOmimPrioritiser();
        PhivePriority phivePrioritiser = priorityFactory.makePhivePrioritiser();
        List<AnalysisStep> steps = Arrays.asList(omimPrioritiser, phivePrioritiser);

        analysisBuilder
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

    @Test
    public void testCanAddGeneblacklistFilter() {
        AnalysisStep blacklistfilter = new GeneBlacklistFilter();
        analysisBuilder.addAnalysisStep(blacklistfilter);
        assertThat(analysisSteps(), equalTo(List.of(blacklistfilter)));
    }

}