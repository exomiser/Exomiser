/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TranscriptAnnotationTest {

    @Test
    public void testEmpty() {
        assertThat(TranscriptAnnotation.empty(), equalTo(TranscriptAnnotation.builder().build()));
    }

    @Test
    public void testVariantEffect() {
        VariantEffect value = VariantEffect.MISSENSE_VARIANT;
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .variantEffect(value)
                .build();
        assertThat(annotation.getVariantEffect(), equalTo(value));
    }

    @Test
    public void testGeneSymbol() {
        String value = "FGFR2";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .geneSymbol(value)
                .build();
        assertThat(annotation.getGeneSymbol(), equalTo(value));
    }

    @Test
    public void testTranscriptAccession() {
        String value = "uc021pzz.1";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .accession(value)
                .build();
        assertThat(annotation.getAccession(), equalTo(value));
    }

    @Test
    public void testHgvsGeString() {
        String value = "chr10:g.123256215T>G";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .hgvsGenomic(value)
                .build();
        assertThat(annotation.getHgvsGenomic(), equalTo(value));
    }

    @Test
    public void testHgvsCdsString() {
        String value = "c.1694A>C";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .hgvsCdna(value)
                .build();
        assertThat(annotation.getHgvsCdna(), equalTo(value));
    }

    @Test
    public void testHgvsProteinString() {
        String value = "p.(Glu565Ala)";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .hgvsProtein(value)
                .build();
        assertThat(annotation.getHgvsProtein(), equalTo(value));
    }

    @Test
    public void testDistanceFromNearestGene() {
        int value = 0;
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .distanceFromNearestGene(value)
                .build();
        assertThat(annotation.getDistanceFromNearestGene(), equalTo(value));
    }

    @Test
    public void testExonRank() {
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(2)
                .rankTotal(5)
                .build();
        // i.e. Exon 2 of 5
        assertThat(annotation.getRankType(), equalTo(TranscriptAnnotation.RankType.EXON));
        assertThat(annotation.getRank(), equalTo(2));
        assertThat(annotation.getRankTotal(), equalTo(5));
    }
}