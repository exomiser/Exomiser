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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotatorTest {

    private JannovarVariantAnnotator instance = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());


    @Test
    public void testGetAnnotationsForUnknownContigVariant() {
        VariantAnnotation annotations = instance.annotate("UNKNOWN", 1, "A", "T");
        System.out.println(annotations);
        assertThat(annotations, not(nullValue()));
        assertThat(annotations.getChromosome(), equalTo(0));
        assertThat(annotations.getPosition(), equalTo(1));
        assertThat(annotations.getRef(), equalTo("A"));
        assertThat(annotations.getAlt(), equalTo("T"));
        assertThat(annotations.getGeneId(), equalTo(""));
        assertThat(annotations.getGeneSymbol(), equalTo("."));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(false));
    }

    @Test
    public void testAnnotateMissenseVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantAnnotation annotations = instance.annotate("10", 123256215, "T", "G");
        assertThat(annotations.getChromosomeName(), equalTo("10"));
        assertThat(annotations.getChromosome(), equalTo(10));
        assertThat(annotations.getPosition(), equalTo(123256215));
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo("G"));
        assertThat(annotations.getGeneId(), equalTo("2263"));
        assertThat(annotations.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = annotations.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(Integer.MIN_VALUE));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.12278533A>C"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.1694A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.(Glu565Ala)"));
    }

    @Test
    public void testAnnotateSpliceAcceptorVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantAnnotation annotations = instance.annotate("10", 123243319, "T", "G");
        assertThat(annotations.getChromosomeName(), equalTo("10"));
        assertThat(annotations.getChromosome(), equalTo(10));
        assertThat(annotations.getPosition(), equalTo(123243319));
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo("G"));
        assertThat(annotations.getGeneId(), equalTo("2263"));
        assertThat(annotations.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.SPLICE_ACCEPTOR_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = annotations.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(Integer.MIN_VALUE));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.12291429A>C"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.2196-2A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.?"));
    }

    @Test
    public void testAnnotateDownstreamVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantAnnotation annotations = instance.annotate("10", 123237800, "T", "G");
        assertThat(annotations.getChromosomeName(), equalTo("10"));
        assertThat(annotations.getChromosome(), equalTo(10));
        assertThat(annotations.getPosition(), equalTo(123237800));
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo("G"));
        assertThat(annotations.getGeneId(), equalTo("2263"));
        assertThat(annotations.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.DOWNSTREAM_GENE_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = annotations.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(-120171));
    }

    @Test
    public void testAnnotateUpstreamVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantAnnotation annotations = instance.annotate("10", 123357973, "T", "G");
        assertThat(annotations.getChromosomeName(), equalTo("10"));
        assertThat(annotations.getChromosome(), equalTo(10));
        assertThat(annotations.getPosition(), equalTo(123357973));
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo("G"));
        assertThat(annotations.getGeneId(), equalTo("2263"));
        assertThat(annotations.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = annotations.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(120130));
    }

    @Test
    public void testAnnotateIntergenicVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        VariantAnnotation annotations = instance.annotate("10", 150000000, "T", "G");
        assertThat(annotations.getChromosomeName(), equalTo("10"));
        assertThat(annotations.getChromosome(), equalTo(10));
        assertThat(annotations.getPosition(), equalTo(150000000));
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo("G"));
        assertThat(annotations.getGeneId(), equalTo("2263"));
        assertThat(annotations.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(annotations.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(annotations.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = annotations.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(26762157));
    }

    @Test
    public void testUpstreamGeneIntergenicVariantsInRegulatoryRegion() {
        //Without the regulatory regions in the annotator
        VariantAnnotation upstreamVariant = instance.annotate("10", 123357973, "T", "G");
        assertThat(upstreamVariant.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));

        VariantAnnotation intergenicVariant = instance.annotate("10", 150000000, "T", "G");
        assertThat(intergenicVariant.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));

        //Regulatory regions containing the variants
        RegulatoryFeature enhancer = new RegulatoryFeature(10, upstreamVariant.getPosition(), upstreamVariant.getPosition(), RegulatoryFeature.FeatureType.ENHANCER);
        RegulatoryFeature tfBindingSite = new RegulatoryFeature(10, intergenicVariant.getPosition(), intergenicVariant.getPosition(), RegulatoryFeature.FeatureType.TF_BINDING_SITE);

        //Create new annotator with the regulatory regions
        VariantAnnotator annotatorWithRegulatoryRegions = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(),
                TestFactory.buildDefaultJannovarData(),
                ChromosomalRegionIndex.of(ImmutableList.of(enhancer, tfBindingSite))
        );

        //Annotate the original positions using the new annotator...
        VariantAnnotation wasUpstream = annotatorWithRegulatoryRegions.annotate(upstreamVariant.getChromosomeName(), upstreamVariant.getPosition(), upstreamVariant.getRef(), upstreamVariant.getAlt());
        assertThat(wasUpstream.getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
        //... and lo! They are designated as regulatory region variants!
        VariantAnnotation wasIntergenic = annotatorWithRegulatoryRegions.annotate(intergenicVariant.getChromosomeName(), intergenicVariant.getPosition(), intergenicVariant.getRef(), intergenicVariant.getAlt());
        assertThat(wasIntergenic.getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedDeletionReturnsValueOfAllelePositionTrim() {

        //Given a single allele from a multi-positional site, they might not be fully trimmed. In cases where there is repetition, depending on the program used, the final
        // variant allele will be different.
//        VCF:      X-118887583-TCAAAA-TCAAAACAAAA
//        Exomiser: X-118887583-T     -TCAAAA
//        CellBase: X-118887584--     - CAAAA
//        Jannovar: X-118887588-      -      CAAAA
//        Nirvana:  X-118887589-      -      CAAAA

        //trimming first with Exomiser, then annotating with Jannovar, constrains the Jannovar annotation to the same position as Exomiser.
//        VCF:      X-118887583-TCAAAA-TCAAAACAAAA
//        Exomiser: X-118887583-T     -TCAAAA
//        CellBase: X-118887584--     - CAAAA
//        Jannovar: X-118887583-      - CAAAA      (Jannovar is zero-based)
//        Nirvana:  X-118887584-      - CAAAA

//        Cellbase:
//        https://github.com/opencb/biodata/blob/develop/biodata-tools/src/main/java/org/opencb/biodata/tools/variant/VariantNormalizer.java
//        http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/genomic/variant/X:118887583:TCAAAA:TCAAAACAAAA/annotation?assembly=grch37&limit=-1&skip=-1&count=false&Output format=json&normalize=true

//        Nirvana style trimming:
//        https://github.com/Illumina/Nirvana/blob/master/VariantAnnotation/Algorithms/BiDirectionalTrimmer.cs

//        Jannovar:
//        https://github.com/charite/jannovar/blob/master/jannovar-core/src/main/java/de/charite/compbio/jannovar/reference/VariantDataCorrector.java

        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        VariantAnnotation annotations = instance.annotate("X", pos, ref, alt);

        System.out.println(annotations);

        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);

        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", annotations.getPosition(), annotations.getRef(), annotations
                .getAlt());
        System.out.println(allelePosition);

        assertThat(annotations.getChromosome(), equalTo(23));
        assertThat(annotations.getPosition(), equalTo(allelePosition.getPos()));
        assertThat(annotations.getRef(), equalTo(allelePosition.getRef()));
        assertThat(annotations.getAlt(), equalTo(allelePosition.getAlt()));
        assertThat(annotations.hasTranscriptAnnotations(), is(false));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedDeletionTrimmedWithAllelePosition() {

        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        AllelePosition trimmed = AllelePosition.trim(pos, ref, alt);

        VariantAnnotation variantAnnotation = instance.annotate("X", pos, ref, alt);

        System.out.println(variantAnnotation);
        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", variantAnnotation.getPosition(), variantAnnotation
                .getRef(), variantAnnotation.getAlt());
        System.out.println("Trimmed: " + trimmed);

        assertThat(variantAnnotation.getChromosome(), equalTo(23));
        assertThat(variantAnnotation.getPosition(), equalTo(trimmed.getPos()));
        assertThat(variantAnnotation.getRef(), equalTo(trimmed.getRef()));
        assertThat(variantAnnotation.getAlt(), equalTo(trimmed.getAlt()));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

}