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

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantAnnotationTest {

    @Test
    public void empty() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .genomeAssembly(GenomeAssembly.HG19)
                .chromosome(0)
                .chromosomeName("")
                .position(0)
                .ref("")
                .alt("")
                .geneId("")
                .geneSymbol("")
                .variantEffect(VariantEffect.SEQUENCE_VARIANT)
                .annotations(Collections.emptyList())
                .build();
        assertThat(VariantAnnotation.empty(), equalTo(instance));
    }

    @Test
    public void assembly() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .genomeAssembly(GenomeAssembly.HG38)
                .build();
        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void getChromosome() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .chromosome(23)
                .build();
        assertThat(instance.getChromosome(), equalTo(23));
    }

    @Test
    public void getChromosomeName() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .chromosomeName("MT")
                .build();
        assertThat(instance.getChromosomeName(), equalTo("MT"));
    }

    @Test
    public void position() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .position(123456)
                .build();
        assertThat(instance.getPosition(), equalTo(123456));
    }

    @Test
    public void ref() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .ref("A")
                .build();
        assertThat(instance.getRef(), equalTo("A"));
    }

    @Test
    public void alt() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .alt("T")
                .build();
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void getGeneId() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .geneId("ENSG:12233455")
                .build();
        assertThat(instance.getGeneId(), equalTo("ENSG:12233455"));
    }

    @Test
    public void getGeneSymbol() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .geneSymbol("GENE1")
                .build();
        assertThat(instance.getGeneSymbol(), equalTo("GENE1"));
    }

    @Test
    public void getVariantEffect() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    public void getAnnotations() {
        VariantAnnotation instance = VariantAnnotation.builder()
                .annotations(Collections.singletonList(TranscriptAnnotation.empty()))
                .build();
        assertThat(instance.getAnnotations(), equalTo(Collections.singletonList(TranscriptAnnotation.empty())));
    }

    @Test
    public void testToString() throws Exception {
        System.out.println(VariantAnnotation.empty());
        assertThat(VariantAnnotation.empty()
                .toString(), equalTo("VariantAnnotation{genomeAssembly=hg19, chromosome=0, chromosomeName='', position=0, ref='', alt='', geneSymbol='', geneId='', variantEffect=SEQUENCE_VARIANT, annotations=[]}"));
    }
}