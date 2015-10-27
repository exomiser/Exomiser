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

package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.BasePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.hamcrest.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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
        return new VariantEvaluation.VariantBuilder(chr, pos, ref, alt)
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
        return new GeneReassigner(tadIndex, hiphivePriority);
    }

    /**
     * This is the simplest case happy path test .
     */
    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantOriginallyAssociatedWithBestCandidateGene() {

        gene1.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 1f));
        gene2.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0f));

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        instance.reassignVariantToMostPhenotypicallySimilarGeneInTad(variant, allGenes);

        assertThat(variant, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantNotOriginallyAssociatedWithBestCandidateGene() {
        gene1.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 1f));
        gene2.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0f));

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene2);
        instance.reassignVariantToMostPhenotypicallySimilarGeneInTad(variant, allGenes);

        assertThat(variant, isAssignedTo(gene1));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantAssociatedWithGeneInOtherTad() {

        gene1.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 1f));
        TopologicalDomain tad1 = makeTad(1, 1, 20000, gene1);

        gene2.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0f));
        TopologicalDomain tad2 = makeTad(1, 40000, 80000, gene2);

        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad1, tad2);

        VariantEvaluation variant = regulatoryVariantInTad(tad2, gene2);
        instance.reassignVariantToMostPhenotypicallySimilarGeneInTad(variant, allGenes);

        assertThat(variant, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_ignoresNonRegulatoryVariant() {
        gene1.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 1f));
        gene2.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0f));

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = variantInTadWithEffect(tad, VariantEffect.MISSENSE_VARIANT, gene2);
        instance.reassignVariantToMostPhenotypicallySimilarGeneInTad(variant, allGenes);

        assertThat(variant, isAssignedTo(gene2));
    }

    @Test
    public void assignsRegulatoryVariantToBestPhenotypicMatch_variantNotMovedWhenAllGenesHaveEqualScore() {
        gene1.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0.5f));
        gene2.addPriorityResult(new BasePriorityResult(PriorityType.HIPHIVE_PRIORITY, 0.5f));

        TopologicalDomain tad = makeTad(1, 1, 20000, gene1, gene2);
        instance = makeInstance(PriorityType.HIPHIVE_PRIORITY, tad);

        VariantEvaluation variant = regulatoryVariantInTad(tad, gene1);
        instance.reassignVariantToMostPhenotypicallySimilarGeneInTad(variant, allGenes);

        assertThat(variant, isAssignedTo(gene1));
    }


}