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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class JannovarStructuralVariantAnnotatorTest {

    private final JannovarStructuralVariantAnnotator instance = new JannovarStructuralVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());

    @Test
    void testAnnotateStructuralVariant() {
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantCoordinates variantCoordinates = VariantAllele.of("10", 123237843, 123357972, "T", "<DEL>", 120129, VariantType.DEL, "10", ConfidenceInterval
                .precise(), ConfidenceInterval.precise());
        List<VariantAnnotation> annotations = instance.annotate(variantCoordinates);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
        assertThat(variantAnnotation.getStartContigId(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123237843));
        assertThat(variantAnnotation.getEnd(), equalTo(123357972));
        assertThat(variantAnnotation.getLength(), equalTo(120129));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("<DEL>"));
        assertThat(variantAnnotation.getVariantType(), equalTo(VariantType.DEL));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.EXON_LOSS_VARIANT));
    }

    @Test
    public void exonicInsertion() {
        VariantCoordinates variantCoordinates = VariantAllele.of("10", 123237843, 123237843, "T", "<INS>", 200, VariantType.INS, "10", ConfidenceInterval
                .precise(), ConfidenceInterval.precise());
        List<VariantAnnotation> annotations = instance.annotate(variantCoordinates);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        System.out.println(variantAnnotation);

        assertThat(variantAnnotation.getStartContigId(), equalTo(10));
        assertThat(variantAnnotation.getStartContigName(), equalTo("10"));
        assertThat(variantAnnotation.getStart(), equalTo(123237843));
        assertThat(variantAnnotation.getEnd(), equalTo(123237843));
        assertThat(variantAnnotation.getLength(), equalTo(200));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("<INS>"));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        System.out.println(variantAnnotation.getTranscriptAnnotations());
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.INSERTION));
    }

    @Test
    public void exonicDeletion() {
        // Exon 2 loss
        VariantCoordinates variantCoordinates = VariantAllele.of("10", 123353221, 123353480, "T", "<DEL>", 259, VariantType.DEL, "10", ConfidenceInterval
                .precise(), ConfidenceInterval.precise());
        List<VariantAnnotation> annotations = instance.annotate(variantCoordinates);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        System.out.println(variantAnnotation);

        assertThat(variantAnnotation.getStartContigId(), equalTo(10));
        assertThat(variantAnnotation.getStartContigName(), equalTo("10"));
        assertThat(variantAnnotation.getStart(), equalTo(123353221));
        assertThat(variantAnnotation.getEnd(), equalTo(123353480));
        assertThat(variantAnnotation.getLength(), equalTo(259));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("<DEL>"));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        System.out.println(variantAnnotation.getTranscriptAnnotations());
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        // this is an EXON_LOSS
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.START_LOST));
    }

}