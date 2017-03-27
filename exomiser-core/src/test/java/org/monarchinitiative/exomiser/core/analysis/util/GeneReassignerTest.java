/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.GeneTranscriptModelBuilder;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.VariantContextBuilder;
import org.monarchinitiative.exomiser.core.genome.VariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneReassignerTest {

    private static final Logger logger = LoggerFactory.getLogger(GeneReassignerTest.class);

    private GeneReassigner instance;

    private Map<String, Gene> allGenes = new HashMap<>();

    private Gene gene1;
    private Gene gene2;

    @Before
    public void setUp() {
        gene1 = new Gene("GENE1", 1111);
        gene2 = new Gene("GENE2", 2222);

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
                mismatchDescription.appendText("was variant with geneSymbol=").appendValue(variantEvaluation.getGeneSymbol());
                mismatchDescription.appendText(" geneId=").appendValue(variantEvaluation.getEntrezGeneId());

                return gene.getEntrezGeneID() == variantEvaluation.getEntrezGeneId() && gene.getGeneSymbol() == variantEvaluation.getGeneSymbol();
            }
        };
    }

    private VariantEvaluation regulatoryVariantInTad(TopologicalDomain tad, Gene associatedGene) {
        return variant(tad.getChromosome(), getMiddlePosition(tad), "A", "T", VariantEffect.REGULATORY_REGION_VARIANT, associatedGene);
    }

    private VariantEvaluation variantInTadWithEffect(TopologicalDomain tad, VariantEffect variantEffect, Gene associatedGene) {
        return variant(tad.getChromosome(), getMiddlePosition(tad), "A", "T", variantEffect, associatedGene);
    }

    private int getMiddlePosition(TopologicalDomain tad) {
        return (tad.getStart() + tad.getEnd()) / 2;
    }

    private VariantEvaluation variant(int chr, int pos, String ref, String alt, VariantEffect variantEffect, Gene gene) {
        return new VariantEvaluation.Builder(chr, pos, ref, alt)
                .variantEffect(variantEffect)
                .geneId(gene.getEntrezGeneID())
                .geneSymbol(gene.getGeneSymbol())
                .build();
    }


    private TopologicalDomain makeTad(int chr, int start, int end, Gene... genes) {
        Map<String, Integer> genesInTad = Arrays.asList(genes).stream().collect(toMap(Gene::getGeneSymbol, Gene::getEntrezGeneID));
        return new TopologicalDomain(chr, start, end, genesInTad);
    }


    private GeneReassigner makeInstance(PriorityType hiphivePriority, TopologicalDomain... tads) {
        ChromosomalRegionIndex<TopologicalDomain> tadIndex = new ChromosomalRegionIndex<>(Arrays.asList(tads));
        return new GeneReassigner(hiphivePriority, allGenes, tadIndex);
    }


    private void addPriorityResultWithScore(Gene gene, double score) {
        gene.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), score));
    }

    /**
     * This is the simplest case happy path test .
     */
    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantOriginallyAssociatedWithBestCandidateGene() {

        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantNotOriginallyAssociatedWithBestCandidateGene() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene2);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene1));
    }


    @Test
    public void noneTypePrioritiser() {
        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.NONE, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene1));
    }

    @Test
    public void variantInGeneNotAssociatedAnyTad() {

        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = variant(2, 999999, "A", "G", VariantEffect.REGULATORY_REGION_VARIANT, gene2);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantAssociatedWithGeneInOtherTad() {

        addPriorityResultWithScore(gene1, 1d);
        TopologicalDomain tad1 = makeTad(1, 1, 20000, gene1);

        addPriorityResultWithScore(gene2, 0d);
        TopologicalDomain tad2 = makeTad(1, 40000, 80000, gene2);

        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad1, tad2);

        VariantEvaluation variant = regulatoryVariantInTad(tad2, gene2);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_ignoresNonRegulatoryVariant() {
        addPriorityResultWithScore(gene1, 1d);
        addPriorityResultWithScore(gene2, 0d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = variantInTadWithEffect(tad, VariantEffect.MISSENSE_VARIANT, gene2);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantNotMovedWhenAllGenesHaveEqualScore() {
        addPriorityResultWithScore(gene1, 0.5d);
        addPriorityResultWithScore(gene2, 0.5d);

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        instance.reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(variant);

        assertThat(variant, isAssignedTo(gene1));
    }

    @Test
    public void testReassignGeneToMostPhenotypicallySimilarGeneInAnnotations_MissenseAnnotationsAreNotReassigned() {
        VariantContextBuilder multiSampleBuilder = new VariantContextBuilder("Adam", "Eve");
        VariantContext variantContext = multiSampleBuilder.build("1 145510730 . T C,A 123.15 PASS GENE=GNRHR2 GT 1/1 0/2");

        VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory();
        List<VariantEvaluation> variants = variantFactory.streamVariantEvaluations(Stream.of(variantContext)).collect(toList());

        Gene GNRHR2 = new Gene("GNRHR2", 114814);
        allGenes.put(GNRHR2.getGeneSymbol(), GNRHR2);

        TopologicalDomain unimportantForThisTestTad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, unimportantForThisTestTad);

        variants.forEach(variantEvaluation -> logger.info("{} {}:{} {} {} {} {}", variantEvaluation.getGeneSymbol(), variantEvaluation.getChromosome(), variantEvaluation.getPosition(), variantEvaluation.getRef(), variantEvaluation.getAlt(), variantEvaluation.getVariantEffect(), variantEvaluation.getAnnotations().size()));
        variants.forEach(variantEvaluation -> {
            assertThat(variantEvaluation.getGeneSymbol(), equalTo("GNRHR2"));
            assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
            instance.reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(variantEvaluation);
            assertThat(variantEvaluation.getGeneSymbol(), equalTo("GNRHR2"));
        });

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

        VariantContextBuilder sampleBuilder = new VariantContextBuilder("Sample");
        String gene1VarUpstream = "1 50 . A T 0 . GENE=GENE1 GT 0/1";
        String gene1VarUtr5 = "1 117 . A T 0 . GENE=GENE1 GT 0/1";
        String gene1VarMissense1Exon1 = "1 129 . C A 0 . GENE=GENE1 GT 0/1";
        String gene1Var1SpliceRegion1 = "1 149 . A C 0 . GENE=GENE1 GT 0/1";
        String gene1Var1Intron1 = "1 151 . G C 0 . GENE=GENE1 GT 0/1";
        String gene1VarMissense1Exon2 = "1 160 . C T 0 . GENE=GENE1 GT 0/1";
        String gene1VarUtr3 = "1 181 . A TGTT 0 . GENE=GENE1 GT 0/1";

        List<VariantContext> variantContexts = sampleBuilder.build(gene1VarUpstream, gene1VarUtr5, gene1VarMissense1Exon1, gene1Var1SpliceRegion1, gene1Var1Intron1, gene1VarMissense1Exon2, gene1VarUtr3);
        variantContexts.forEach(variantContext -> logger.info("{}", variantContext));

        List<VariantEvaluation> variantEvaluations = variantFactory.streamVariantEvaluations(variantContexts.stream()).collect(toList());

        variantEvaluations.forEach(variantEvaluation -> logger.info("{} {}", variantEvaluation, variantEvaluation.getAnnotations()));

    }
//    gene lies within two overlapping TADs

    //variant lies two overlapping TADs, but the gene is in one only
}