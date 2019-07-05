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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotatorTest {

    private JannovarVariantAnnotator instance = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(), TestFactory
            .buildDefaultJannovarData(), ChromosomalRegionIndex.empty());


    @Test
    void testGetAnnotationsForUnknownContigVariant() {
        List<VariantAnnotation> annotations = instance.annotate("UNKNOWN", 1, "A", "T");
        System.out.println(annotations);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosome(), equalTo(0));
        assertThat(variantAnnotation.getStart(), equalTo(1));
        assertThat(variantAnnotation.getRef(), equalTo("A"));
        assertThat(variantAnnotation.getAlt(), equalTo("T"));
        assertThat(variantAnnotation.getGeneId(), equalTo(""));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("."));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

    @Test
    void testAnnotateMissenseVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = instance.annotate("10", 123256215, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123256215));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(Integer.MIN_VALUE));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.12278533A>C"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.1694A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.(Glu565Ala)"));
    }

    @Test
    void testAnnotateSpliceAcceptorVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = instance.annotate("10", 123243319, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123243319));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.SPLICE_ACCEPTOR_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(Integer.MIN_VALUE));
        assertThat(transcriptAnnotation.getHgvsGenomic(), equalTo("g.12291429A>C"));
        assertThat(transcriptAnnotation.getHgvsCdna(), equalTo("c.2196-2A>C"));
        assertThat(transcriptAnnotation.getHgvsProtein(), equalTo("p.?"));
    }

    @Test
    void testAnnotateDownstreamVariant() {
        // This transcript is on the negative strand
        // TranscriptModel Gene=FGFR2 accession=uc021pzz.1 Chr10 Strand=- seqLen=4654
        // txRegion=123237843-123357972(120129 bases) CDS=123239370-123353331(113961 bases)
        List<VariantAnnotation> annotations = instance.annotate("10", 123237800, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123237800));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
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
        List<VariantAnnotation> annotations = instance.annotate("10", 123357973, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(123357973));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
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
        List<VariantAnnotation> annotations = instance.annotate("10", 150000000, "T", "G");
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getStart(), equalTo(150000000));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
        assertThat(transcriptAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(transcriptAnnotation.getAccession(), equalTo("uc021pzz.1"));
        assertThat(transcriptAnnotation.getDistanceFromNearestGene(), equalTo(26762157));
    }

    @Test
    void testUpstreamGeneIntergenicVariantsInRegulatoryRegion() {
        //Without the regulatory regions in the annotator
        List<VariantAnnotation> upstreamVariantAnnots = instance.annotate("10", 123357973, "T", "G");
        assertThat(upstreamVariantAnnots.size(), equalTo(1));
        VariantAnnotation upstreamVariant = upstreamVariantAnnots.get(0);

        assertThat(upstreamVariant.getVariantEffect(), equalTo(VariantEffect.UPSTREAM_GENE_VARIANT));

        List<VariantAnnotation> intergenicVariantAnnots = instance.annotate("10", 150000000, "T", "G");
        assertThat(intergenicVariantAnnots.size(), equalTo(1));
        VariantAnnotation intergenicVariant = intergenicVariantAnnots.get(0);

        assertThat(intergenicVariant.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));

        //Regulatory regions containing the variants
        RegulatoryFeature enhancer = new RegulatoryFeature(10, upstreamVariant.getStart(), upstreamVariant.getStart(), RegulatoryFeature.FeatureType.ENHANCER);
        RegulatoryFeature tfBindingSite = new RegulatoryFeature(10, intergenicVariant.getStart(), intergenicVariant.getStart(), RegulatoryFeature.FeatureType.TF_BINDING_SITE);

        //Create new annotator with the regulatory regions
        VariantAnnotator annotatorWithRegulatoryRegions = new JannovarVariantAnnotator(TestFactory.getDefaultGenomeAssembly(),
                TestFactory.buildDefaultJannovarData(),
                ChromosomalRegionIndex.of(ImmutableList.of(enhancer, tfBindingSite))
        );

        //Annotate the original positions using the new annotator...
        List<VariantAnnotation> wasUpstream = annotatorWithRegulatoryRegions.annotate(upstreamVariant.getChromosomeName(), upstreamVariant.getStart(), upstreamVariant.getRef(), upstreamVariant.getAlt());
        assertThat(wasUpstream.get(0).getVariantEffect(), equalTo(VariantEffect.REGULATORY_REGION_VARIANT));
        //... and lo! They are designated as regulatory region variants!
        List<VariantAnnotation> wasIntergenic = annotatorWithRegulatoryRegions.annotate(intergenicVariant.getChromosomeName(), intergenicVariant.getStart(), intergenicVariant.getRef(), intergenicVariant.getAlt());
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

        List<VariantAnnotation> annotations = instance.annotate("X", pos, ref, alt);
        assertThat(annotations.size(), equalTo(1));
        VariantAnnotation variantAnnotation = annotations.get(0);

        System.out.println(variantAnnotation);

        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);

        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n",
                variantAnnotation.getStart(), variantAnnotation.getRef(), variantAnnotation.getAlt());
        System.out.println(allelePosition);

        assertThat(variantAnnotation.getChromosome(), equalTo(23));
        assertThat(variantAnnotation.getStart(), equalTo(allelePosition.getStart()));
        assertThat(variantAnnotation.getRef(), equalTo(allelePosition.getRef()));
        assertThat(variantAnnotation.getAlt(), equalTo(allelePosition.getAlt()));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

    @Test
    void testGetAnnotationsForUnTrimmedDeletionTrimmedWithAllelePosition() {

        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        AllelePosition trimmed = AllelePosition.trim(pos, ref, alt);

        List<VariantAnnotation> annotations = instance.annotate("X", pos, ref, alt);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        System.out.println(variantAnnotation);
        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n",
                variantAnnotation.getStart(), variantAnnotation.getRef(), variantAnnotation.getAlt());
        System.out.println("Trimmed: " + trimmed);

        assertThat(variantAnnotation.getChromosome(), equalTo(23));
        assertThat(variantAnnotation.getStart(), equalTo(trimmed.getStart()));
        assertThat(variantAnnotation.getRef(), equalTo(trimmed.getRef()));
        assertThat(variantAnnotation.getAlt(), equalTo(trimmed.getAlt()));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(false));
    }

    @Test
    void testStructuralVariant() {
        int pos = 118608470;
        String ref = "A";
        String alt = "<INS>";

        AllelePosition trimmed = AllelePosition.trim(pos, ref, alt);

        List<VariantAnnotation> annotations = instance.annotate("X", pos, ref, alt);
        assertThat(annotations.size(), equalTo(1));

        VariantAnnotation variantAnnotation = annotations.get(0);
        assertThat(variantAnnotation.getChromosome(), equalTo(23));
        assertThat(variantAnnotation.getStart(), equalTo(trimmed.getStart()));
        assertThat(variantAnnotation.getRef(), equalTo(trimmed.getRef()));
        assertThat(variantAnnotation.getAlt(), equalTo(trimmed.getAlt()));
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

        List<VariantAnnotation> overlappingMissenseAnnotations = variantAnnotator.annotate("16", 89935214, "G", "A");
        assertThat(overlappingMissenseAnnotations.size(), equalTo(2));
        overlappingMissenseAnnotations.forEach(variantAnnotation -> {
            assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
            // the transcript annotations should be split into new variant annotations for each gene
            assertThat(variantAnnotation.getTranscriptAnnotations().size(), equalTo(1));
        });

        List<VariantAnnotation> annotations = variantAnnotator.annotate("16", 89923407, "G", "TA");
        assertThat(annotations.size(), equalTo(1));
        annotations.forEach(variantAnnotation -> {
            System.out.println(variantAnnotation);
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
}