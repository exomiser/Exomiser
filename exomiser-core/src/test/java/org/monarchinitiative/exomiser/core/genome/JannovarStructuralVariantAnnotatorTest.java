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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class JannovarStructuralVariantAnnotatorTest {

    private final JannovarStructuralVariantAnnotator instance = new JannovarStructuralVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());

    private final Contig chr10 = GenomeAssembly.HG19.getContigById(10);

    @Test
    void testAnnotateStructuralVariant() {
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        GenomicVariant variantCoordinates = variant(chr10, 123237843, 123357972, "T", "<DEL>", -120129);
        List<VariantAnnotation> annotations = instance.annotate(variantCoordinates);
        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.TRANSCRIPT_ABLATION));
    }

    private GenomicVariant variant(Contig contig, int start, int end, String ref, String alt, int changeLength) {
        return GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end, ref, alt, changeLength);
    }

    @Test
    public void downstreamInsertion() {
        GenomicVariant variant = variant(chr10, 123237843, 123237843, "T", "<INS>", 200);
        List<VariantAnnotation> annotations = instance.annotate(variant);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.DOWNSTREAM_GENE_VARIANT));
    }

    @Test
    public void exonicInsertion() {
        GenomicVariant variant = variant(GenomeAssembly.HG19.getContigById(1), 145508025, 145508025, "T", "<INS>", 200);
        List<VariantAnnotation> annotations = instance.annotate(variant);
        assertThat(annotations.size(), equalTo(2));

        for (VariantAnnotation variantAnnotation : annotations) {
            assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
            // Jannovar would add an artificially HIGH impact INSERTION annotation to all non-intergenic insertions which
            // leads to hugely over-inflated variant effect scores.
            if (variantAnnotation.getGeneSymbol().equals("RBM8A")) {
                assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.CODING_SEQUENCE_VARIANT));
            }
            if (variantAnnotation.getGeneSymbol().equals("GNRHR2")) {
                assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.DOWNSTREAM_GENE_VARIANT));
            }
        }
    }

    @Test
    public void exonicDeletion() {
        // Exon 2 loss
        GenomicVariant variant = variant(chr10, 123353221, 123353480, "T", "<DEL>", -259);
        List<VariantAnnotation> annotations = instance.annotate(variant);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        // this is an EXON_LOSS
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.START_LOST));
    }

    @Test
    public void wholeGeneInversion() {
        // FGFR2 10:123237848-123357972
        GenomicVariant variant = variant(chr10, 123237800, 123358000, "T", "<INV>", 0);
        List<VariantAnnotation> annotations = instance.annotate(variant);
        System.out.println(annotations);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));

        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.CODING_TRANSCRIPT_VARIANT));
    }

    @Test
    public void singleExonInversion() {
        // FGFR2 10:123237848-123357972
        GenomicVariant variant = variant(chr10, 123357475, 123357972, "T", "<INV>", 0);
        List<VariantAnnotation> annotations = instance.annotate(variant);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        // this is an EXON_LOSS
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.CODING_TRANSCRIPT_VARIANT));
    }

    @Test
    public void upstreamGeneInversion() {
        // FGFR2 10:123237848-123357972
        GenomicVariant variant = variant(chr10, 123358000, 123359000, "T", "<INV>", 0);
        List<VariantAnnotation> annotations = instance.annotate(variant);

        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));

        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
    }

    @Test
    void testBnd() {
        VariantContextConverter variantContextConverter = VariantContextConverter.of(GenomeAssembly.HG19.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        VariantContext variantContext = TestVcfReader.forSamples("sample").readVariantContext("1\t243097603\tMantaBND:12652:0:1:1:1:0:0\tA\t]Y:13954151]A\t428.00\tMaxDepth\tSVTYPE=BND;MATEID=MantaBND:12652:0:1:1:1:0:1;BND_PAIR_COUNT=10;PAIR_COUNT=9;CIPOS=0,12;HOMLEN=12;HOMSEQ=ATAATAATAATA;BND_DEPTH=31;MATE_BND_DEPTH=47\tGT:GQ:PR:SR\t0/1:428:26,4:13,13");
        GenomicVariant variant = variantContextConverter.convertToVariant(variantContext, variantContext.getAlternateAllele(0));
        assertThat(variant, is(nullValue()));
//        List<VariantAnnotation> variantAnnotations = instance.annotate(variant);
//        System.out.println(variantAnnotations);
    }

    @Disabled
    @Test
    public void preciseStructuralVariant() {

        VariantContext variantContext = TestVcfReader.forSamples("sample").readVariantContext("CM000663.2      30912   pbsv.DEL.0      CTCTCTCTCTCGCTATCTCATTTT        C       .       PASS    SVTYPE=DEL;END=30935;SVLEN=-23  GT:AD:DP:SAC    0/1:45,15:60:28,17,9,6");
        System.out.println(variantContext);

        GenomeAssembly hg38 = GenomeAssembly.HG38;
        VariantContextConverter variantContextConverter = VariantContextConverter.of(hg38.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

        GenomicVariant variant = variantContextConverter.convertToVariant(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variant);

        System.out.println("RefSeq SmallAnnotator");
        JannovarData refseqHg38JannovatrData = JannovarDataSourceLoader.loadJannovarData(Path.of("/home/hhx640/Documents/exomiser-data/2007_hg38_transcripts_refseq.ser"));
        VariantAnnotator refseqHg38Smallannotator = new JannovarSmallVariantAnnotator(hg38, refseqHg38JannovatrData, ChromosomalRegionIndex.empty());
        refseqHg38Smallannotator.annotate(variant).forEach(System.out::println);

        System.out.println("RefSeq SVannotator");
        VariantAnnotator refseqHg38SvAnnotator = new JannovarStructuralVariantAnnotator(hg38, refseqHg38JannovatrData, ChromosomalRegionIndex.empty());
        refseqHg38SvAnnotator.annotate(variant).forEach(System.out::println);

        System.out.println("Ensembl SmallAnnotator");
        JannovarData ensemblJannovarData = JannovarDataSourceLoader.loadJannovarData(Path.of("/home/hhx640/Documents/exomiser-data/2007_hg38_transcripts_ensembl.ser"));
        VariantAnnotator ensemblannotator = new JannovarSmallVariantAnnotator(hg38, ensemblJannovarData, ChromosomalRegionIndex.empty());
        ensemblannotator.annotate(variant).forEach(System.out::println);

        System.out.println("Ensembl SVAnnotator");
        VariantAnnotator ensemblSvAnnotator = new JannovarStructuralVariantAnnotator(hg38, ensemblJannovarData, ChromosomalRegionIndex.empty());
        ensemblSvAnnotator.annotate(variant).forEach(System.out::println);
    }
}