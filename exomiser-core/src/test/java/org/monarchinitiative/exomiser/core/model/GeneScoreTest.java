/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GeneScoreTest {

    @Test
    public void testEmpty() {
        assertThat(GeneScore.empty(), equalTo(GeneScore.builder().build()));
    }

    @Test
    public void getGeneIdentifier() {
        GeneIdentifier testIdentifier = GeneIdentifier.builder().geneSymbol("GENE:1").geneId("HGNC:12345").build();

        GeneScore instance = GeneScore.builder().geneIdentifier(testIdentifier).build();
        assertThat(instance.getGeneIdentifier(), equalTo(testIdentifier));
    }

    @Test
    public void getModeOfInheritance() {
        GeneScore instance = GeneScore.builder()
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        assertThat(instance.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    @Test
    public void getCombinedScore() {
        GeneScore instance = GeneScore.builder()
                .combinedScore(1f)
                .build();
        assertThat(instance.getCombinedScore(), equalTo(1f));
    }

    @Test
    public void getPhenotypeScore() {
        GeneScore instance = GeneScore.builder()
                .phenotypeScore(1f)
                .build();
        assertThat(instance.getPhenotypeScore(), equalTo(1f));
    }

    @Test
    public void getVariantScore() {
        GeneScore instance = GeneScore.builder()
                .variantScore(1f)
                .build();
        assertThat(instance.getVariantScore(), equalTo(1f));
    }

    @Test
    public void getContributingVariants() {
        List<VariantEvaluation> contributingVariants = Arrays.asList(
                VariantEvaluation.builder(1, 12335, "T", "C").build(),
                VariantEvaluation.builder(1, 23446, "A", "T").build()
        );

        GeneScore instance = GeneScore.builder()
                .contributingVariants(contributingVariants)
                .build();
        assertThat(instance.getContributingVariants(), equalTo(contributingVariants));
    }

    @Test
    public void equals() {
        assertThat(GeneScore.builder().build(), equalTo(GeneScore.builder().build()));
    }

    @Test
    public void testHashCode() {
    }

    @Test
    public void testToString() {
        GeneScore instance = GeneScore.builder()
                .geneIdentifier(GeneIdentifier.builder().geneSymbol("TEST1").geneId("HGNC:12345").build())
                .combinedScore(1f)
                .phenotypeScore(1f)
                .variantScore(1f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        System.out.println(instance);
    }
}