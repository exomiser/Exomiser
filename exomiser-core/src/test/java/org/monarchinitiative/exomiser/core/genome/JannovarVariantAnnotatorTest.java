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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotatorTest {

    private final JannovarVariantAnnotator instance = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());

    private List<VariantAnnotation> annotate(VariantAnnotator instance, String contig, int start, String ref, String alt) {
        GenomicVariant variant = variant(contig, start, ref, alt);
        return instance.annotate(variant);
    }

    private GenomicVariant variant(String contig, int start, String ref, String alt) {
        VariantTrimmer.VariantPosition variantPosition = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()).trim(Strand.POSITIVE, start, ref, alt);
        return GenomicVariant.of(GenomeAssembly.HG19.getContigByName(contig), Strand.POSITIVE, CoordinateSystem.ONE_BASED, variantPosition.start(), variantPosition.ref(), variantPosition.alt());
    }

    @Test
    void testGetAnnotationsForUnknownContigVariant() {
        assertThrows(CoordinatesOutOfBoundsException.class, () -> annotate(instance, "UNKNOWN", 1, "A", "T"));
    }

    @Test
    void testAnnotateMissenseVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = annotate(instance, "10", 123256215, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(0));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.123256215T>G"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.1694A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.(Glu565Ala)"));
    }

    @Test
    void testAnnotateSpliceAcceptorVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = annotate(instance, "10", 123243319, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.SPLICE_ACCEPTOR_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(0));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.123243319T>G"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.2196-2A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.?"));
    }

    @Test
    void testAnnotateMitochondrialAlternateCodonStopGainVariant() {
        TranscriptModel tmMTCYB = new GeneTranscriptModelBuilder("MT-CYB", "HGNC:7427", "ENST00000361789.2", 25, de.charite.compbio.jannovar.reference.Strand.FWD,
                "ATGACCCCAATACGCAAAACTAACCCCCTAATAAAATTAATTAACCACTCATTCATCGAC" +
                        "CTCCCCACCCCATCCAACATCTCCGCATGATGAAACTTCGGCTCACTCCTTGGCGCCTGC" +
                        "CTGATCCTCCAAATCACCACAGGACTATTCCTAGCCATGCACTACTCACCAGACGCCTCA" +
                        "ACCGCCTTTTCATCAATCGCCCACATCACTCGAGACGTAAATTATGGCTGAATCATCCGC" +
                        "TACCTTCACGCCAATGGCGCCTCAATATTCTTTATCTGCCTCTTCCTACACATCGGGCGA" +
                        "GGCCTATATTACGGATCATTTCTCTACTCAGAAACCTGAAACATCGGCATTATCCTCCTG" +
                        "CTTGCAACTATAGCAACAGCCTTCATAGGCTATGTCCTCCCGTGAGGCCAAATATCATTC" +
                        "TGAGGGGCCACAGTAATTACAAACTTACTATCCGCCATCCCATACATTGGGACAGACCTA" +
                        "GTTCAATGAATCTGAGGAGGCTACTCAGTAGACAGTCCCACCCTCACACGATTCTTTACC" +
                        "TTTCACTTCATCTTGCCCTTCATTATTGCAGCCCTAGCAACACTCCACCTCCTATTCTTG" +
                        "CACGAAACGGGATCAAACAACCCCCTAGGAATCACCTCCCATTCCGATAAAATCACCTTC" +
                        "CACCCTTACTACACAATCAAAGACGCCCTCGGCTTACTTCTCTTCCTTCTCTCCTTAATG" +
                        "ACATTAACACTATTCTCACCAGACCTCCTAGGCGACCCAGACAATTATACCCTAGCCAAC" +
                        "CCCTTAAACACCCCTCCCCACATCAAGCCCGAATGATATTTCCTATTCGCCTACACAATT" +
                        "CTCCGATCCGTCCCTAACAAACTAGGAGGCGTCCTTGCCCTATTACTATCCATCCTCATC" +
                        "CTAGCAATAATCCCCATCCTCCATATATCCAAACAACAAAGCATAATATTTCGCCCACTA" +
                        "AGCCAATCACTTTATTGACTCCTAGCCGCAGACCTCCTCATTCTAACCTGAATCGGAGGA" +
                        "CAACCAGTAAGCTACCCTTTTACCATCATTGGACAAGTAGCATCCGTACTATACTTCACA" +
                        "ACAATCCTAATCCTAATACCAACTATCTCCCTAATTGAAAACAAAATACTCAAATGGGCC" +
                        "T")
                .buildTxRegion(14746, 15887) // zero-based coordinates
                .buildCdsRegion(14746, 15887)
                .addExon(14746, 15887)
                .build();
        JannovarData jannovarData = new JannovarData(TestFactory.getDefaultRefDict(), ImmutableList.of(tmMTCYB));;

        JannovarVariantAnnotator jannovarVariantAnnotator = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), jannovarData, ChromosomalRegionIndex.empty());

        // see https://www.mitomap.org/foswiki/bin/view/MITOMAP/VariantsCoding for know variants
        // MT-CYB m.15150G>A p.(Trp135*)
        // MT-CYB m.15722T>A p.(Trp326*)
        List<VariantAnnotation> annotations = annotate(jannovarVariantAnnotator, "MT", 15150, "G", "A");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("HGNC:7427"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("MT-CYB"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.STOP_GAINED));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("MT-CYB"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("ENST00000361789.2"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(0));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.15150G>A")); // should be NC_012920.1:m.15150G>A
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.404G>A"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.(Trp135*)")); // p.(Ter135=) using the standard eukaryotic codon table
    }

    @Test
    void testAnnotateDownstreamVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = annotate(instance, "10", 123237800, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.DOWNSTREAM_GENE_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(-120171));
    }

    @Test
    void testAnnotateUpstreamVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = annotate(instance, "10", 123357973, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(120130));
    }

    @Test
    void testAnnotateIntergenicVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = annotate(instance, "10", 123458888, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(123458888 - 123237843));
    }

    @Test
    void testUpstreamGeneIntergenicVariantsInRegulatoryRegion() {
        GenomicVariant upstreamVariant = variant("10", 123357973, "T", "G");
        //Without the regulatory regions in the annotator
        List<VariantAnnotation> upstreamVariantAnnots = instance.annotate(upstreamVariant);
        assertThat(upstreamVariantAnnots.size(), equalTo(1));
        VariantAnnotation upstreamAnnotations = upstreamVariantAnnots.get(0);

        assertThat(upstreamAnnotations.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));

        GenomicVariant intergenicVariant = variant("10", 123458888, "T", "G");
        List<VariantAnnotation> intergenicVariantAnnots = annotate(instance, "10", 123458888, "T", "G");
        assertThat(intergenicVariantAnnots.size(), equalTo(1));
        VariantAnnotation intergenicAnnotations = intergenicVariantAnnots.get(0);

        assertThat(intergenicAnnotations.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));

        //Regulatory regions containing the variants
        RegulatoryFeature enhancer = new RegulatoryFeature(10, upstreamVariant.start(), upstreamVariant.end(), RegulatoryFeature.FeatureType.ENHANCER);
        RegulatoryFeature tfBindingSite = new RegulatoryFeature(10, intergenicVariant.start(), intergenicVariant.end(), RegulatoryFeature.FeatureType.TF_BINDING_SITE);

        //Create new annotator with the regulatory regions
        VariantAnnotator annotatorWithRegulatoryRegions = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(),
                TestFactory.buildDefaultJannovarData(),
                ChromosomalRegionIndex.of(ImmutableList.of(enhancer, tfBindingSite))
        );

        //Annotate the original positions using the new annotator...
        List<VariantAnnotation> wasUpstream = annotate(annotatorWithRegulatoryRegions, upstreamVariant.contigName(), upstreamVariant
                .start(), upstreamVariant.ref(), upstreamVariant.alt());
        assertThat(wasUpstream.get(0).getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
        //... and lo! They are designated as regulatory region variants!
        List<VariantAnnotation> wasIntergenic = annotate(annotatorWithRegulatoryRegions, intergenicVariant.contigName(), intergenicVariant
                .start(), intergenicVariant.ref(), intergenicVariant.alt());
        assertThat(wasIntergenic.get(0).getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
    }

    @Test
    void testGetAnnotationsForUnTrimmedDeletionReturnsValueOfAllelePositionTrim() {

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

        List<VariantAnnotation> annotations = annotate(instance, "X", pos, ref, alt);
        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

    @Test
    void testGetAnnotationsForUnTrimmedDeletionTrimmedWithAllelePosition() {

        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        List<VariantAnnotation> annotations = annotate(instance, "X", pos, ref, alt);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

    @Test
    void testAnnotateStructuralVariantAsSnv() {
        Contig chrX = GenomeAssembly.HG19.getContigByName("X");
        int pos = 118608470;
        String ref = "A";
        String alt = "<INS>";

        GenomicVariant variant = GenomicVariant.of(chrX, Strand.POSITIVE, CoordinateSystem.ONE_BASED, pos, pos, ref, alt, 100);
        List<VariantAnnotation> annotations = instance.annotate(variant);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);

        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.STRUCTURAL_VARIANT));
    }

    @Test
    void testTwoOverlappingGenesModerateImpact() {
        // These two transcripts are for two overlapping genes on GRCh38
        // Region view - http://www.ensembl.org/Homo_sapiens/Location/View?db=core;g=ENSG00000258947;r=16:89916965-89942476;t=ENST00000315491
        // AC092143 has 5 exons which appear to be a fusion of MC1R (not shown) and TUBB3.
        // These two transcripts have the same final 3 exons but AC092143 exons 1-2 and TUBB3 exon 1 do not overlap as shown below:
        // AC092143  ENST00000556922  ------   --                         --   --   ------
        // TUBB3     ENST00000315491                  --                  --   --   ------
        GeneIdentifier AC092143 = GeneIdentifier.builder().geneSymbol("AC092143.1").build();
        TranscriptModel ENST00000556922 = TestTranscriptModelFactory.builder()
                .geneIdentifier(AC092143)
                .knownGeneLine("ENST00000556922.1\tchr16\t+\t89919164\t89936092\t89919258\t89935804\t5\t89919164,89920589,89932570,89933467,89934728,\t89920208,89920737,89932679,89933578,89936092,\tA0A0B4J269\tuc002fpf.3")
                .mRnaSequence("GGCAGCACCATGAACTAAGCAGGACACCTGGAGGGGAAGAACTGTGGGGACCTGGAGGCCTCCAACGACTCCTTCCTGCTTCCTGGACAGGACTATGGCTGTGCAGGGATCCCAGAGAAGACTTCTGGGCTCCCTCAACTCCACCCCCACAGCCATCCCCCAGCTGGGGCTGGCTGCCAACCAGACAGGAGCCCGGTGCCTGGAGGTGTCCATCTCTGACGGGCTCTTCCTCAGCCTGGGGCTGGTGAGCTTGGTGGAGAACGCGCTGGTGGTGGCCACCATCGCCAAGAACCGGAACCTGCACTCACCCATGTACTGCTTCATCTGCTGCCTGGCCTTGTCGGACCTGCTGGTGAGCGGGAGCAACGTGCTGGAGACGGCCGTCATCCTCCTGCTGGAGGCCGGTGCACTGGTGGCCCGGGCTGCGGTGCTGCAGCAGCTGGACAATGTCATTGACGTGATCACCTGCAGCTCCATGCTGTCCAGCCTCTGCTTCCTGGGCGCCATCGCCGTGGACCGCTACATCTCCATCTTCTACGCACTGCGCTACCACAGCATCGTGACCCTGCCGCGGGCGCGGCGAGCCGTTGCGGCCATCTGGGTGGCCAGTGTCGTCTTCAGCACGCTCTTCATCGCCTACTACGACCACGTGGCCGTCCTGCTGTGCCTCGTGGTCTTCTTCCTGGCTATGCTGGTGCTCATGGCCGTGCTGTACGTCCACATGCTGGCCCGGGCCTGCCAGCACGCCCAGGGCATCGCCCGGCTCCACAAGAGGCAGCGCCCGGTCCACCAGGGCTTTGGCCTTAAAGGCGCTGTCACCCTCACCATCCTGCTGGGCATTTTCTTCCTCTGCTGGGGCCCCTTCTTCCTGCATCTCACACTCATCGTCCTCTGCCCCGAGCACCCCACGTGCGGCTGCATCTTCAAGAACTTCAACCTCTTTCTCGCCCTCATCATCTGCAATGCCATCATCGACCCCCTCATCTACGCCTTCCACAGCCAGGAGCTCCGCAGGACGCTCAAGGAGGTGCTGACATGCTCCTGCTCTCAGGACCGTGCCCTCGTCAGCTGGGATGTGAAGTCTCTGGGTGGAAGTGTGTGCCAAGAGCTACTCCCACAGCAGCCCCAGGAGAAGGGGCTTTGTGACCAGAAAGCTTCATCCACAGCCTTGCAGCGGCTCCTGCAAAAGGAGTTCTGGGAAGTCATCAGTGATGAGCATGGCATCGACCCCAGCGGCAACTACGTGGGCGACTCGGACTTGCAGCTGGAGCGGATCAGCGTCTACTACAACGAGGCCTCTTCTCACAAGTACGTGCCTCGAGCCATTCTGGTGGACCTGGAACCCGGAACCATGGACAGTGTCCGCTCAGGGGCCTTTGGACATCTCTTCAGGCCTGACAATTTCATCTTTGGTCAGAGTGGGGCCGGCAACAACTGGGCCAAGGGTCACTACACGGAGGGGGCGGAGCTGGTGGATTCGGTCCTGGATGTGGTGCGGAAGGAGTGTGAAAACTGCGACTGCCTGCAGGGCTTCCAGCTGACCCACTCGCTGGGGGGCGGCACGGGCTCCGGCATGGGCACGTTGCTCATCAGCAAGGTGCGTGAGGAGTATCCCGACCGCATCATGAACACCTTCAGCGTCGTGCCCTCACCCAAGGTGTCAGACACGGTGGTGGAGCCCTACAACGCCACGCTGTCCATCCACCAGCTGGTGGAGAACACGGATGAGACCTACTGCATCGACAACGAGGCGCTCTACGACATCTGCTTCCGCACCCTCAAGCTGGCCACGCCCACCTACGGGGACCTCAACCACCTGGTATCGGCCACCATGAGCGGAGTCACCACCTCCTTGCGCTTCCCGGGCCAGCTCAACGCTGACCTGCGCAAGCTGGCCGTCAACATGGTGCCCTTCCCGCGCCTGCACTTCTTCATGCCCGGCTTCGCCCCCCTCACAGCCCGGGGCAGCCAGCAGTACCGGGCCCTGACCGTGCCCGAGCTCACCCAGCAGATGTTCGATGCCAAGAACATGATGGCCGCCTGCGACCCGCGCCACGGCCGCTACCTGACGGTGGCCACCGTGTTCCGGGGCCGCATGTCCATGAAGGAGGTGGACGAGCAGATGCTGGCCATCCAGAGCAAGAACAGCAGCTACTTCGTGGAGTGGATCCCCAACAACGTGAAGGTGGCCGTGTGTGACATCCCGCCCCGCGGCCTCAAGATGTCCTCCACCTTCATCGGGAACAGCACGGCCATCCAGGAGCTGTTCAAGCGCATCTCCGAGCAGTTCACGGCCATGTTCCGGCGCAAGGCCTTCCTGCACTGGTACACGGGCGAGGGCATGGACGAGATGGAGTTCACCGAGGCCGAGAGCAACATGAACGACCTGGTGTCCGAGTACCAGCAGTACCAGGACGCCACGGCCGAGGAAGAGGGCGAGATGTACGAAGACGACGAGGAGGAGTCGGAGGCCCAGGGCCCCAAGTGAAGCTGCTCGCAGCTGGAGTGAGAGGCAGGTGGCGGCCGGGGCCGAAGCCAGCAGTGTCTAAACCCCCGGAGCCATCTTGCTGCCGACACCCTGCTTTCCCCTCGCCCTAGGGCTCCCTTGCCGCCCTCCTGCAGTATTTATGGCCTCGTCCTCCCCACCTAGGCCACGTGTGAGCTGCTCCTGTCTCTGTCTTATTGCAGCTCCAGGCCTGACGTTTTACGGTTTTGTTTTTTACTGGTTTGTGTTTATATTTTCGGGGATACTTAATAAATCTATTGCTGTCAGATA")
                .build();
        GeneIdentifier TUBB3 = GeneIdentifier.builder().geneSymbol("TUBB3").build();
        TranscriptModel ENST00000315491 = TestTranscriptModelFactory.builder()
                .geneIdentifier(TUBB3)
                .knownGeneLine("ENST00000315491.11\tchr16\t+\t89923278\t89936097\t89923401\t89935804\t4\t89923278,89932570,89933467,89934728,\t89923458,89932679,89933578,89936097,\tQ13509\tuc002fph.2")
                .mRnaSequence("GACATCAGCCGATGCGAAGGGCGGGGCCGCGGCTATAAGAGCGCGCGGCCGCGGTCCCCGACCCTCAGCAGCCAGCCCGGCCCGCCCGCGCCCGTCCGCAGCCGCCCGCCAGACGCGCCCAGTATGAGGGAGATCGTGCACATCCAGGCCGGCCAGTGCGGCAACCAGATCGGGGCCAAGTTCTGGGAAGTCATCAGTGATGAGCATGGCATCGACCCCAGCGGCAACTACGTGGGCGACTCGGACTTGCAGCTGGAGCGGATCAGCGTCTACTACAACGAGGCCTCTTCTCACAAGTACGTGCCTCGAGCCATTCTGGTGGACCTGGAACCCGGAACCATGGACAGTGTCCGCTCAGGGGCCTTTGGACATCTCTTCAGGCCTGACAATTTCATCTTTGGTCAGAGTGGGGCCGGCAACAACTGGGCCAAGGGTCACTACACGGAGGGGGCGGAGCTGGTGGATTCGGTCCTGGATGTGGTGCGGAAGGAGTGTGAAAACTGCGACTGCCTGCAGGGCTTCCAGCTGACCCACTCGCTGGGGGGCGGCACGGGCTCCGGCATGGGCACGTTGCTCATCAGCAAGGTGCGTGAGGAGTATCCCGACCGCATCATGAACACCTTCAGCGTCGTGCCCTCACCCAAGGTGTCAGACACGGTGGTGGAGCCCTACAACGCCACGCTGTCCATCCACCAGCTGGTGGAGAACACGGATGAGACCTACTGCATCGACAACGAGGCGCTCTACGACATCTGCTTCCGCACCCTCAAGCTGGCCACGCCCACCTACGGGGACCTCAACCACCTGGTATCGGCCACCATGAGCGGAGTCACCACCTCCTTGCGCTTCCCGGGCCAGCTCAACGCTGACCTGCGCAAGCTGGCCGTCAACATGGTGCCCTTCCCGCGCCTGCACTTCTTCATGCCCGGCTTCGCCCCCCTCACAGCCCGGGGCAGCCAGCAGTACCGGGCCCTGACCGTGCCCGAGCTCACCCAGCAGATGTTCGATGCCAAGAACATGATGGCCGCCTGCGACCCGCGCCACGGCCGCTACCTGACGGTGGCCACCGTGTTCCGGGGCCGCATGTCCATGAAGGAGGTGGACGAGCAGATGCTGGCCATCCAGAGCAAGAACAGCAGCTACTTCGTGGAGTGGATCCCCAACAACGTGAAGGTGGCCGTGTGTGACATCCCGCCCCGCGGCCTCAAGATGTCCTCCACCTTCATCGGGAACAGCACGGCCATCCAGGAGCTGTTCAAGCGCATCTCCGAGCAGTTCACGGCCATGTTCCGGCGCAAGGCCTTCCTGCACTGGTACACGGGCGAGGGCATGGACGAGATGGAGTTCACCGAGGCCGAGAGCAACATGAACGACCTGGTGTCCGAGTACCAGCAGTACCAGGACGCCACGGCCGAGGAAGAGGGCGAGATGTACGAAGACGACGAGGAGGAGTCGGAGGCCCAGGGCCCCAAGTGAAGCTGCTCGCAGCTGGAGTGAGAGGCAGGTGGCGGCCGGGGCCGAAGCCAGCAGTGTCTAAACCCCCGGAGCCATCTTGCTGCCGACACCCTGCTTTCCCCTCGCCCTAGGGCTCCCTTGCCGCCCTCCTGCAGTATTTATGGCCTCGTCCTCCCCACCTAGGCCACGTGTGAGCTGCTCCTGTCTCTGTCTTATTGCAGCTCCAGGCCTGACGTTTTACGGTTTTGTTTTTTACTGGTTTGTGTTTATATTTTCGGGGATACTTAATAAATCTATTGCTGTCAGATACCCTT")
                .build();

        JannovarData jannovarData = TestFactory.buildJannovarData(ENST00000556922, ENST00000315491);
        JannovarVariantAnnotator variantAnnotator = new JannovarVariantAnnotator(GenomeAssembly.HG38, jannovarData, ChromosomalRegionIndex.empty());

        List<VariantAnnotation> overlappingMissenseAnnotations = annotate(variantAnnotator, "16", 89935214, "G", "A");
        assertThat(overlappingMissenseAnnotations.size(), equalTo(2));
        overlappingMissenseAnnotations.forEach(variantAnnotation -> {
            assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
            // the transcript annotations should be split into new variant annotations for each gene
            assertThat(variantAnnotation.getTranscriptAnnotations().size(), equalTo(1));
        });

        List<VariantAnnotation> annotations = annotate(variantAnnotator, "16", 89923407, "G", "TA");
        assertThat(annotations.size(), equalTo(1));
        annotations.forEach(variantAnnotation -> {
            assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.FRAMESHIFT_ELONGATION));
            // the transcript annotations should remain together as the position on the AC092143 transcript
            assertThat(variantAnnotation.getTranscriptAnnotations().size(), equalTo(2));
        });

        Map<String, List<TranscriptAnnotation>> annotationsByGeneSymbol = annotations.stream()
                .flatMap(variantAnnotation -> variantAnnotation.getTranscriptAnnotations().stream())
                .collect(groupingBy(TranscriptAnnotation::getGeneSymbol));

        annotationsByGeneSymbol.get("TUBB3")
                .forEach(transcriptAnnotation -> assertThat(transcriptAnnotation.getVariantEffect(), equalTo(VariantEffect.FRAMESHIFT_ELONGATION)));

        annotationsByGeneSymbol.get("AC092143.1")
                .forEach(transcriptAnnotation -> assertThat(transcriptAnnotation.getVariantEffect(), equalTo(VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT)));

    }

    @Test
    public void testMassivePreciseVariantIsTreatedAsStructural() {
        VariantContext variantContext = TestVcfReader.forSamples("sample").readVariantContext("10      123352331   .      GGCGCCTGTAGTCCCAGCTACTTGGGAGGCTGAGGCTCGAGAATCGCTTGAACCTAGGAGGGGGAGGTTGCAGTGAGCCGAGATCGTGCCACTGCACTCCAGCTTGGCAACAGAGCAAGACTCCATCTCAAAAAAAAAAAAAAAATTGTGTCTATGTATTATAAGCCATATCCTTTGGGAAGCAGACAAGATATAAATAATAAATAACTGTAATAACACATTCTATACATTAAATCATTTCATCTACTACTAAATTACAATACTTATTTTACAGCACTTTATGAAAGTGTGCTCACCTGAAATTTGCTAAAAGGAGCTCAAAAGAGCTAGGGAGAGATGCAAATCAATACCCAAGGGACAGATTAAGACAGAGGCAGGCATCAGAGCTAAAGTATACAAACTAACATGGAACTATTAGGAAATTTTACTGGTTACATTCTCAGAATGATGGCTCTAGGTACACACTGGCTTTTGGCTCACAGTGTAAGCTAATCACAATACTGAGTTATGCCCATTAAAATCATGACTATCCTGAAATGGAACCCTGGCATTAACCTTTTAAGACCAACCTGAAGGGCACTGCACACTGTGATTTCAGGTGTTCTCAAAACAGGGATTTGCTGATGTTTATTCACTAAAGTCTAGGACTAAAATTCTGTAAGTATGTGACTAAGTTGCAAGGAGTATTCCTTAAACCTAAGTGCAGCCGTACTGCAGAAATGAAGACTTCTCTGCTAAATATCAAGGCTGAGTGCTCTCTTGGCAAAAACTTAGCAACAACTAATACAAAATCTAGAAGTTGTCAAGAATACACATACATTTTCTGTTTCTGTTAATCAAATATCATCCACAACCTGAAAATTCCTTTCATTGCCACACAAACTTAATTTTGCATAGAACTTCTTGGGCATAAAATTATTCTGATCCCATCCTACTAAATATCACATGAATATCCCTTTTATTTCTGTCTATTAAGTATTCAAGTTGCGGACTCTAAATTAGCAATTTGATTTTAAATTCTACTAGCTCCTGGATTACTTCTAATGTTAATGAAGATTAGACAATAGGCTTAAAAAGTAGGACTTTTCTGGGTGGGTTCTGACCAATTCTTTCCCCCTTAATATTCCAGAATGATTAAATGCATTCATTGTTATTAAAGCAGTGGTCTATTGAGTCACATACGGTACCTTGGGGCCATGTGGGAAGTCAAACAGGTAAGTCATAATTTTCTGGAAAAAAAAATTTAACATAAGGTCCATATACTACTTTTGCAAAAGGTATATAACCTAAAGAAATTACAAGCTTTTCACAAAACATGTCTTTTTCAACATAGGACTCACCACATTCTTGTTTCCTCTAAATTTTATGAAATCATGGCAGTGGAAGCCAGAAATTAATGCTTTACCATATACCAAAAAGAAAAAAAGGCTTCTGACATTCTCAGGGAGGATACATACTTCCTCTGGAAGATGTTTTTGAACACACATTTGGAGGAAAGGAGCATATGAGGTAGGGGTATAGAGAAAACTAATGACTCACACAGAAATAACCTATCACCTTGGCTTCGCTACTGCCATCCCTAGACCAACTAAGTCAACAAACCAACGGTTTATGTAAGACTGTTTACTACAAATCACCAGGTGTCTAAATCAAGTTTACATGTACAGCGAATTGGGGGAAGTGGAACTTCTTGCCAGTACTATAAATTTTTAAGTGTCTCAGCAAAAGTAAGATGAAGTTAAGGAGTTAGATCAGTTTTTCCACATGCTTTAATCATGGGAAAAAACTGTTTTTAAGAGGTAGTAAATTTTGGGCTGGGCACAGTGACTCACGCCTGTAATCCCAGCACTTTGGGAGGCCAAGGTGGGTGGATCACGAGGTCAGGAGTTCAAGACCAGCCTGTCCAAGATGTTAAAACCTCGTCTCTACTAAAAATACAAAAAAATTAGCCAGGCGCAGTGGCAGGT        G       .       PASS    .  GT    1/1");
        GenomeAssembly hg19 = GenomeAssembly.HG19;
        VariantContextConverter variantContextConverter = VariantContextConverter.of(hg19.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        GenomicVariant variant = variantContextConverter.convertToVariant(variantContext, variantContext.getAlternateAllele(0));
        List<VariantAnnotation> variantAnnotations = instance.annotate(variant);

        assertThat(variantAnnotations.size(), equalTo(1));
        assertThat(variantAnnotations.get(0).getVariantEffect(), equalTo(VariantEffect.EXON_LOSS_VARIANT));
    }

    @Test
    public void testMassivePreciseDownstreamGeneInsertionVariantIsTreatedAsStructural() {
        VariantContext variantContext = TestVcfReader.forSamples("sample").readVariantContext("10      123361399   .      C    CCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGAGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGCCAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGCCAGGCGGGGTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGGCAGGCGGGCTGAGGGTCAGAGGAAGGGCCAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCACAGGAAGGGACAGGCGGGGTGAGGGTCAGAGGAAGGGCCAGGCGGGCTGAGGGTCA       .       PASS    .  GT    1/1");
        GenomeAssembly hg19 = GenomeAssembly.HG19;
        VariantContextConverter variantContextConverter = VariantContextConverter.of(hg19.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        GenomicVariant variant = variantContextConverter.convertToVariant(variantContext, variantContext.getAlternateAllele(0));
        List<VariantAnnotation> variantAnnotations = instance.annotate(variant);

        assertThat(variantAnnotations.size(), equalTo(1));
        assertThat(variantAnnotations.get(0).getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));
    }
}