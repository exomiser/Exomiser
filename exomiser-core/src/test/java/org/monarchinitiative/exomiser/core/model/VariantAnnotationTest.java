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
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.svart.Contig;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotationTest {

    Contig chr1 = GenomeAssembly.HG19.getContigByName("1");

    @Test
    public void empty() {
        VariantAnnotation instance = VariantAnnotation.builder(GenomeAssembly.HG19, 0, 1, 0, "", "", 0).build();
        assertThat(VariantAnnotation.empty(), equalTo(instance));
    }

    @Test
    public void assembly() {
        VariantAnnotation instance = VariantAnnotation.builder(GenomeAssembly.HG38, 1, 1, "A", "T").build();
        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void getContig() {
        VariantAnnotation instance = VariantAnnotation.builder(GenomeAssembly.HG19, 23, 1, "A", "T").build();
        assertThat(instance.contigId(), equalTo(23));
    }

    @Test
    public void getContigName() {
        VariantAnnotation instance = VariantAnnotation.builder(GenomeAssembly.HG19, 25, 1, "A", "T").build();
        assertThat(instance.contigName(), equalTo("MT"));
    }

    @Test
    public void setsLengthFromAlleleLength() {
        VariantAnnotation snp = VariantAnnotation.builder(GenomeAssembly.HG19, 1, 1, "A", "G").build();
        assertThat(snp.length(), equalTo(1));
        assertThat(snp.changeLength(), equalTo(0));


        VariantAnnotation insertion = VariantAnnotation.builder(GenomeAssembly.HG19, 1, 1, "AT", "GTT").build();
        assertThat(insertion.length(), equalTo(2));
        assertThat(insertion.changeLength(), equalTo(1));

        VariantAnnotation deletion = VariantAnnotation.builder(GenomeAssembly.HG19, 1, 1, "ACT", "G").build();
        assertThat(deletion.length(), equalTo(3));
        assertThat(deletion.changeLength(), equalTo(-2));

        VariantAnnotation mnv = VariantAnnotation.builder(GenomeAssembly.HG19, 1, 1, "ATC", "GTT").build();
        assertThat(mnv.length(), equalTo(3));
        assertThat(mnv.changeLength(), equalTo(0));
    }

    @Test
    public void getGeneSymbol() {
        VariantAnnotation instance = VariantAnnotation.builder(GenomeAssembly.HG19, 1, 1, "A", "T")
                .geneId("ENSG:12233455")
                .geneSymbol("GENE1")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .annotations(Collections.singletonList(TranscriptAnnotation.empty()))
                .build();
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("T"));
        assertThat(instance.getGeneId(), equalTo("ENSG:12233455"));
        assertThat(instance.getGeneSymbol(), equalTo("GENE1"));
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(instance.getTranscriptAnnotations(), equalTo(Collections.singletonList(TranscriptAnnotation.empty())));
    }

    @Test
    public void testToString() {
        assertThat(VariantAnnotation.empty()
                .toString(), equalTo("VariantAnnotation{genomeAssembly=hg19, chromosome=0, contig='na', strand=+, start=1, end=0, length=0, ref='', alt='', geneSymbol='', geneId='', variantEffect=SEQUENCE_VARIANT, annotations=[]}"));
    }
}