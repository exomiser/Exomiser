/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.ConfidenceInterval;
import org.monarchinitiative.exomiser.core.model.StructuralType;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class JannovarStructuralVariantAnnotatorTest {

    private JannovarStructuralVariantAnnotator instance = new JannovarStructuralVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());

    @Test
    void testAnnotateStructuralVariant() {
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = instance.annotate("10", 123237843, "T", "<DEL>", StructuralType.DEL, 120129, ConfidenceInterval
                .of(0, 0), "10", 123357972, ConfidenceInterval.of(0, 0));
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123237843));
        assertThat(variantAnnotation.getEnd(), equalTo(123357972));
        assertThat(variantAnnotation.getLength(), equalTo(120129));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("<DEL>"));
        assertThat(variantAnnotation.getStructuralType(), equalTo(StructuralType.DEL));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.EXON_LOSS_VARIANT));
    }


}