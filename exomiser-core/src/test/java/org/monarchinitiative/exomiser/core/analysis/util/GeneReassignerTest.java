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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GeneTranscriptModelBuilder;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVcfParser;
import org.monarchinitiative.exomiser.core.genome.VariantFactory;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneReassignerTest {

    private static final Logger logger = LoggerFactory.getLogger(GeneReassignerTest.class);

    private GeneReassigner instance;

    private Map<String, Gene> allGenes;

    private Gene gene1;
    private Gene gene2;

    @BeforeEach
    public void setUp() {
        gene1 = new Gene("GENE1", 1111);
        gene2 = new Gene("GENE2", 2222);

        allGenes = new HashMap<>();
        allGenes.put(gene1.getGeneSymbol(), gene1);
        allGenes.put(gene2.getGeneSymbol(), gene2);
    }

    private static Matcher<VariantEvaluation> isAssignedTo(final Gene gene) {
        return new TypeSafeDiagnosingMatcher<VariantEvaluation>() {
            @Override
            public void describeTo(final Description description) {
                description.appendText("variant with geneSymbol=").appendValue(gene.getGeneSymbol());
                description.appendText(" geneId=").appendValue(gene.getEntrezGeneID());
            }

            @Override
            protected boolean matchesSafely(final VariantEvaluation variantEvaluation, final Description mismatchDescription) {
                mismatchDescription.appendText("was variant with geneSymbol=")
                        .appendValue(variantEvaluation.getGeneSymbol());
                mismatchDescription.appendText(" geneId=").appendValue(variantEvaluation.getGeneId());

                return gene.getGeneId().equals(variantEvaluation.getGeneId())
                        && gene.getGeneSymbol().equals(variantEvaluation.getGeneSymbol());
            }
        };
    }

    private VariantEvaluation regulatoryVariantInTad(TopologicalDomain tad, Gene associatedGene) {
        return variant(tad.getStartContigId(), getMiddlePosition(tad), "A", "T", VariantEffect.REGULATORY_REGION_VARIANT, associatedGene);
    }

    private VariantEvaluation.Builder regulatoryVariantBuilderInTad(TopologicalDomain tad, Gene associatedGene) {
        return variantBuilder(tad.getStartContigId(), getMiddlePosition(tad), "A", "T", VariantEffect.REGULATORY_REGION_VARIANT, associatedGene);
    }

    private VariantEvaluation.Builder variantBuilderInTadWithEffect(TopologicalDomain tad, VariantEffect variantEffect, Gene associatedGene) {
        return variantBuilder(tad.getStartContigId(), getMiddlePosition(tad), "A", "T", variantEffect, associatedGene);
    }

    private int getMiddlePosition(TopologicalDomain tad) {
        return (tad.getStart() + tad.getEnd()) / 2;
    }

    private VariantEvaluation variant(int chr, int pos, String ref, String alt, VariantEffect variantEffect, Gene gene) {
        return variantBuilder(chr, pos, ref, alt, variantEffect, gene)
                .variantEffect(variantEffect)
                .geneId(gene.getGeneId())
                .geneSymbol(gene.getGeneSymbol())
                .build();
    }

    private VariantEvaluation.Builder variantBuilder(int chr, int pos, String ref, String alt, VariantEffect variantEffect, Gene gene) {
        return VariantEvaluation.builder(chr, pos, ref, alt)
                .variantContext(buildVariantContext(chr, pos, ref, alt))
                .variantEffect(variantEffect)
                .geneId(gene.getGeneId())
                .geneSymbol(gene.getGeneSymbol());
    }

    private VariantContext buildVariantContext(int chr, int pos, String ref, String alt) {
        return new VariantContextBuilder()
                .chr(String.valueOf(chr))
                .start(pos)
                .stop(pos - 1 + ref.length())
                .alleles(ref, alt)
                .make();
    }

    private TopologicalDomain makeTad(int chr, int start, int end, Gene... genes) {
        Map<String, Integer> genesInTad = Arrays.stream(genes)
                .collect(toMap(Gene::getGeneSymbol, Gene::getEntrezGeneID));
        return new TopologicalDomain(chr, start, end, genesInTad);
    }


    private GeneReassigner makeInstance(PriorityType hiphivePriority, TopologicalDomain... tads) {
        ChromosomalRegionIndex<TopologicalDomain> tadIndex = ChromosomalRegionIndex.of(Arrays.asList(tads));
        return new GeneReassigner(hiphivePriority, allGenes, tadIndex);
    }


    private void addPriorityResultWithScore(Gene gene, double score) {
        gene.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), score));
    }

    /**
     * This is the simplest case happy path test .
     */
    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchvariantOriginallyAssociatedWithBestCandidateGene() {

        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchvariantNotOriginallyAssociatedWithBestCandidateGene() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene2);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);
        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchvariantOriginallyAssociatedWithBestCandidateGeneKeepsOriginalAnnotations() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation gene1Annotation = TranscriptAnnotation.builder().geneSymbol(gene1.getGeneSymbol()).build();
        TranscriptAnnotation gene2Annotation = TranscriptAnnotation.builder().geneSymbol(gene2.getGeneSymbol()).build();
        List<TranscriptAnnotation> originalAnnotations = Arrays.asList(gene2Annotation, gene1Annotation);

        VariantEvaluation variant = regulatoryVariantBuilderInTad(tad, gene1)
                .annotations(originalAnnotations)
                .build();

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(originalAnnotations));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchVariantNotOriginallyAssociatedWithBestCandidateGeneHasMatchingAnnotationsTransferred() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation gene2Annotation = TranscriptAnnotation.builder().geneSymbol(gene2.getGeneSymbol()).build();
        TranscriptAnnotation gene1Annotation = TranscriptAnnotation.builder().geneSymbol(gene1.getGeneSymbol()).build();

        VariantEvaluation variant = regulatoryVariantBuilderInTad(tad, gene2)
                .annotations(Arrays.asList(gene2Annotation, gene1Annotation))
                .build();

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(Collections.singletonList(gene1Annotation)));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchVariantInTadButNotAssignedToKnownGene() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        Gene noKnownGene = new Gene(".", -1);

        VariantEvaluation variant = regulatoryVariantInTad(tad, noKnownGene);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchVariantNotInKnownTadAndNotAssignedToKnownGene() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        Gene noKnownGene = new Gene(".", -1);

        VariantEvaluation variant = variant(1, tad.getEnd() + 1000, "A", "T", VariantEffect.REGULATORY_REGION_VARIANT, noKnownGene);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(noKnownGene));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchGeneInTadNotInKnownGenes() {

        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        Gene noKnownGene = new Gene(".", -1);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2, noKnownGene);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void noneTypePrioritiser() {
        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.NONE, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void variantInGeneNotAssociatedAnyTad() {

        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = variant(2, 999999, "A", "G", VariantEffect.REGULATORY_REGION_VARIANT, gene2);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchVariantAssociatedWithGeneInOtherTad() {

        addPriorityResultWithScore(gene1, 1d);
        TopologicalDomain tad1 = makeTad(1, 1, 20000, gene1);

        addPriorityResultWithScore(gene2, 0d);
        TopologicalDomain tad2 = makeTad(1, 40000, 80000, gene2);

        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad1, tad2);

        VariantEvaluation variant = regulatoryVariantInTad(tad2, gene2);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchIgnoresNonRegulatoryVariant() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = variantBuilderInTadWithEffect(tad, VariantEffect.MISSENSE_VARIANT, gene2).build();

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatchVariantNotMovedWhenAllGenesHaveEqualScore() {
        addPriorityResultWithScore(gene1, 0.5d);
        addPriorityResultWithScore(gene2, 0.5d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotationsVariantInUnKnownGeneIsNotAltered() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        Gene noKnownGene = new Gene(".", -1);

        VariantEvaluation variant = regulatoryVariantInTad(tad, noKnownGene);
        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);
        System.out.println(reassigned.getVariantContext());
        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotationsVariantEffectIsNullIsNotAltered() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation nullEffectAnnotation = TranscriptAnnotation.builder()
                .geneSymbol(gene2.getGeneSymbol())
                .variantEffect(null)
                .build();

        VariantEvaluation variant = regulatoryVariantBuilderInTad(tad, gene2)
                .annotations(Collections.singletonList(nullEffectAnnotation))
                .build();

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getVariantEffect(), equalTo(variant.getVariantEffect()));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotationsHandlesFusionProteinAnnotationsToTwoKnownGenes() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation upstream = TranscriptAnnotation.builder()
                .geneSymbol(gene1.getGeneSymbol())
                .variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT)
                .build();

        TranscriptAnnotation downstream = TranscriptAnnotation.builder()
                .geneSymbol(gene2.getGeneSymbol())
                .variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT)
                .build();

        TranscriptAnnotation fusionProteinAnnotation = TranscriptAnnotation.builder()
                .geneSymbol(gene2.getGeneSymbol() + "-" + gene1.getGeneSymbol())
                .variantEffect(VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT)
                .build();

        VariantEvaluation variant = variantBuilderInTadWithEffect(tad, VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT, gene2)
                .annotations(ImmutableList.of(upstream, downstream, fusionProteinAnnotation))
                .build();

        logVariantInfo(variant);

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        logVariantInfo(reassigned);
        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(ImmutableList.of(upstream)));
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotationsMissenseAnnotationsAreNotReassigned() {
        Stream<VariantContext> variantContext = TestVcfParser
                .forSamples("Adam", "Eve")
                .parseVariantContext("1 145510730 . T C,A 123.15 PASS GENE=GNRHR2 GT 1/1 0/2");

        VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory();
        List<VariantEvaluation> variants = variantFactory.createVariantEvaluations(variantContext).collect(toList());

        Gene GNRHR2 = TestFactory.newGeneGNRHR2();
        GNRHR2.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, GNRHR2.getEntrezGeneID(), GNRHR2.getGeneSymbol(), 1.0));
        allGenes.put(GNRHR2.getGeneSymbol(), GNRHR2);

        Gene RBM8A = TestFactory.newGeneRBM8A();
        RBM8A.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, RBM8A.getEntrezGeneID(), RBM8A.getGeneSymbol(), 0.5));
        allGenes.put(RBM8A.getGeneSymbol(), RBM8A);

        TopologicalDomain unimportantForThisTestTad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, unimportantForThisTestTad);

        variants.forEach(variantEvaluation -> {
            logVariantInfo(variantEvaluation);
            VariantEffect originalVariantEffect = VariantEffect.MISSENSE_VARIANT;
            List<TranscriptAnnotation> originalAnnotations = new ArrayList<>(variantEvaluation.getTranscriptAnnotations());

            assertThat(variantEvaluation, isAssignedTo(GNRHR2));
            assertThat(variantEvaluation.getVariantEffect(), equalTo(originalVariantEffect));
            assertThat(variantEvaluation.getTranscriptAnnotations(), equalTo(originalAnnotations));

            VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variantEvaluation);

            assertThat(reassigned, isAssignedTo(GNRHR2));
            assertThat(reassigned.getVariantEffect(), equalTo(originalVariantEffect));
            assertThat(reassigned.getTranscriptAnnotations(), equalTo(originalAnnotations));
        });
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotationsAnnotationsOverlapTwoGenesShouldOnlyHaveTopPhenotypeGeneMatchAnnotations() {
        Gene topPhenotypeMatchGene = TestFactory.newGeneRBM8A();
        Gene GNRHR2 = TestFactory.newGeneGNRHR2();
        allGenes.put(topPhenotypeMatchGene.getGeneSymbol(), topPhenotypeMatchGene);
        allGenes.put(GNRHR2.getGeneSymbol(), GNRHR2);

        addPriorityResultWithScore(topPhenotypeMatchGene, 1.0d);
        addPriorityResultWithScore(GNRHR2, 0d);

        TopologicalDomain topologicalDomain = makeTad(1, 1, 20000, GNRHR2, topPhenotypeMatchGene);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, topologicalDomain);

        VariantEvaluation intergenicAndUpstreamVar = VariantEvaluation.builder(1, 1000, "A", "T")
                .variantEffect(VariantEffect.INTERGENIC_VARIANT)
                .variantContext(buildVariantContext(1, 1000, "A", "T"))
                .geneSymbol(GNRHR2.getGeneSymbol())
                .geneId(GNRHR2.getGeneId())
                .annotations(ImmutableList.of(
                        TranscriptAnnotation.builder()
                                .geneSymbol(GNRHR2.getGeneSymbol())
                                .variantEffect(VariantEffect.INTERGENIC_VARIANT)
                                .build(),
                        TranscriptAnnotation.builder()
                                .geneSymbol(topPhenotypeMatchGene.getGeneSymbol())
                                .variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT)
                                .build()
                ))
                .build();


        logger.info("Before re-assigning variant to best phenotype match ({})", topPhenotypeMatchGene.getGeneSymbol());
        logVariantInfo(intergenicAndUpstreamVar);

        assertThat(intergenicAndUpstreamVar, isAssignedTo(GNRHR2));
        assertThat(intergenicAndUpstreamVar.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));

        List<TranscriptAnnotation> reassignedAnnotations = intergenicAndUpstreamVar.getTranscriptAnnotations().stream()
                .filter(transcriptAnnotation -> transcriptAnnotation.getGeneSymbol()
                        .equals(topPhenotypeMatchGene.getGeneSymbol()))
                .collect(toList());

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(intergenicAndUpstreamVar);

        logger.info("After re-assigning variant to best phenotype match ({})", topPhenotypeMatchGene.getGeneSymbol());
        logVariantInfo(reassigned);

        assertThat(reassigned, isAssignedTo(topPhenotypeMatchGene));
        assertThat(reassigned.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(reassignedAnnotations));
    }

    @Test
    void singleTranscriptAnnotationReturnsOriginalInstance() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation upstream = TranscriptAnnotation.builder()
                .geneSymbol(gene1.getGeneSymbol())
                .variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT)
                .build();

        VariantEvaluation variant = variantBuilderInTadWithEffect(tad, VariantEffect.UPSTREAM_GENE_VARIANT, gene2)
                .annotations(ImmutableList.of(upstream))
                .build();

        logVariantInfo(variant);

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        logVariantInfo(reassigned);
        assertThat(reassigned, isAssignedTo(gene2));
        assertThat(reassigned.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(ImmutableList.of(upstream)));
    }

    @Test
    void noUniqueScoringGenesReturnsOriginalInstance() {
        addPriorityResultWithScore(gene1, 0d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        TranscriptAnnotation upstream = TranscriptAnnotation.builder()
                .geneSymbol(gene1.getGeneSymbol())
                .variantEffect(VariantEffect.UPSTREAM_GENE_VARIANT)
                .build();

        TranscriptAnnotation downstream = TranscriptAnnotation.builder()
                .geneSymbol(gene2.getGeneSymbol())
                .variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT)
                .build();

        TranscriptAnnotation intergenic = TranscriptAnnotation.builder()
                .geneSymbol(gene1.getGeneSymbol())
                .variantEffect(VariantEffect.INTERGENIC_VARIANT)
                .build();

        VariantEvaluation variant = variantBuilderInTadWithEffect(tad, VariantEffect.UPSTREAM_GENE_VARIANT, gene1)
                .annotations(ImmutableList.of(upstream, downstream, intergenic))
                .build();

        logVariantInfo(variant);

        VariantEvaluation reassigned = instance.reassignRegulatoryAndNonCodingVariantAnnotations(variant);

        logVariantInfo(reassigned);
        assertThat(reassigned, isAssignedTo(gene1));
        assertThat(reassigned.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(reassigned.getTranscriptAnnotations(), equalTo(ImmutableList.of(upstream, downstream, intergenic)));
    }

    private void logVariantInfo(VariantEvaluation variant) {
        logger.info("{} {}:{} {} {} {}", variant.getGeneSymbol(), variant.getStartContigId(), variant.getStart(),
                variant.getRef(), variant.getAlt(), variant.getVariantEffect());
        variant.getTranscriptAnnotations()
                .forEach(transcriptAnnotation -> logger.info("{} {}:{} {}", variant.getGeneSymbol(), variant.getStartContigId(),
                        variant.getStart(), transcriptAnnotation));
    }

    @Test
    public void testBuildGeneTranscriptModel() {
        String gene1Exon1Utr5 = "ATATATATTT";
        String gene1Exon1Cds = "ATGCCCATAGCCTGACCTAT";
        String gene1Intron1 = "ATTACGTATA";
        String gene1Exon2Cds = "ATGCCCATAGCCTGACCTAT";
        String gene1Exon2Utr3 = "CCCTTTTAAAAAAAAAAAAA";

        String gene1transcript = gene1Exon1Utr5 + gene1Exon1Cds + gene1Intron1 + gene1Exon2Cds + gene1Exon2Utr3;

        TranscriptModel gene1TranscriptModel = new GeneTranscriptModelBuilder("GENE1", "ENTREZ1111", "transcript1", 1, Strand.FWD, gene1transcript)
                .buildTxRegion(100, 200)
                .buildCdsRegion(125, 175)
                .addExon(115, 135)
                .addExon(156, 196)
                .build();

        VariantFactory variantFactory = TestFactory.buildVariantFactory(gene1TranscriptModel);

        String gene1VarUpstream = "1 50 . A T 0 . GENE=GENE1 GT 0/1";
        String gene1VarUtr5 = "1 117 . A T 0 . GENE=GENE1 GT 0/1";
        String gene1VarMissense1Exon1 = "1 129 . C A 0 . GENE=GENE1 GT 0/1";
        String gene1Var1SpliceRegion1 = "1 149 . A C 0 . GENE=GENE1 GT 0/1";
        String gene1Var1Intron1 = "1 151 . G C 0 . GENE=GENE1 GT 0/1";
        String gene1VarMissense1Exon2 = "1 160 . C T 0 . GENE=GENE1 GT 0/1";
        String gene1VarUtr3 = "1 181 . A TGTT 0 . GENE=GENE1 GT 0/1";

        Stream<VariantContext> variantContexts = TestVcfParser.forSamples("Sample")
                .parseVariantContext(gene1VarUpstream,
                        gene1VarUtr5,
                        gene1VarMissense1Exon1,
                        gene1Var1SpliceRegion1,
                        gene1Var1Intron1,
                        gene1VarMissense1Exon2,
                        gene1VarUtr3);

        variantFactory.createVariantEvaluations(variantContexts)
                .peek(variantContext -> logger.info("{}", variantContext))
                .forEach(variantEvaluation -> logger.info("{} {}", variantEvaluation, variantEvaluation.getTranscriptAnnotations()));

    }
//    gene lies within two overlapping TADs

    //variant lies two overlapping TADs, but the gene is in one only
}