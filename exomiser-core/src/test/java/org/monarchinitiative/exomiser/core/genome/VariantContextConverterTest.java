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

import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantContextConverterTest {

    private final VariantContextConverter instance = VariantContextConverter.of(GenomeAssembly.HG19.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));


    private GenomicVariant parseVcfRecord(String vcfRecord) {
        VariantContext variantContext = parseVariantContext(vcfRecord);
        return instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));
    }

    private VariantContext parseVariantContext(String vcfLine) {
        return TestVcfReader.forSamples("Sample1")
                .readVariantContext(vcfLine);
    }

    @Test
    void doesNotConvertUnassembledMolecules() {
        // this is dependant on the provided GenomicAssembly only containing assembled molecules
        VariantContextConverter variantContextConverter = VariantContextConverter.of(GenomeAssembly.HG19.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        VariantContext variantContext = parseVariantContext("chrUn_KI270424v1\t123256215\t.\tT\tG\t100\tPASS\t.\tGT\t1|0");
        assertThat(variantContextConverter.convertToVariant(variantContext, variantContext.getAlternateAllele(0)), is(nullValue()));
    }

    @Test
    void warnsAndReturnsNullOnSymbolicError() {
        Contig chr1 = GenomeAssembly.HG19.getContigById(1);
        // This symbolic variant has been called past the end of the contig, which will cause svart to complain and explode
        VariantContext variantContext = parseVariantContext("1\t123256215\t.\tT\t<DEL>\t100\tPASS\tSVYTPE=DEL;END=" + (chr1.length() + 1) + "\tGT\t1|0");
        assertThat(instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0)), is(nullValue()));
    }

    @Test
    public void snv() {
        GenomicVariant variantAllele = parseVcfRecord("10\t123256215\t.\tT\tG\t100\tPASS\t.\tGT\t1|0");

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("G"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.SNV));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.start(), equalTo(123256215));
        assertThat(variantAllele.end(), equalTo(123256215));

        assertThat(variantAllele.length(), equalTo(1));
    }

    @Test
    public void smallInsertion() {
        GenomicVariant variantAllele = parseVcfRecord("10\t123256215\t.\tG\tGA\t100\tPASS\t.\tGT\t1|0");

        assertThat(variantAllele.ref(), equalTo("G"));
        assertThat(variantAllele.alt(), equalTo("GA"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.INS));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.start(), equalTo(123256215));
        assertThat(variantAllele.end(), equalTo(123256215));

        assertThat(variantAllele.length(), equalTo(1));
    }

    @Test
    public void smallDeletion() {
        GenomicVariant variantAllele = parseVcfRecord("10\t123256215\t.\tGA\tG\t100\tPASS\t.\tGT\t1|0");

        assertThat(variantAllele.ref(), equalTo("GA"));
        assertThat(variantAllele.alt(), equalTo("G"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.start(), equalTo(123256215));
        assertThat(variantAllele.end(), equalTo(123256216));
        assertThat(variantAllele.changeLength(), equalTo(-1));

    }

    @Test
    public void smallMnv() {
        GenomicVariant variantAllele = parseVcfRecord("10\t123256215\t.\tTA\tGC\t100\tPASS\t.\tGT\t1|0");

        assertThat(variantAllele.ref(), equalTo("TA"));
        assertThat(variantAllele.alt(), equalTo("GC"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.MNV));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.start(), equalTo(123256215));
        assertThat(variantAllele.end(), equalTo(123256216));
        assertThat(variantAllele.length(), equalTo(2));
    }

    // Structural tests
    @Test
    void preciseDeletionWithKnownBreakpoint() {
        // 1.  A precise deletion with known breakpoint, a one base micro-homology, and a sample that is homozygous for the deletion.
        GenomicVariant variantAllele = parseVcfRecord("1       2827694 rs2376870 CGTGGATGCGGGGAC  C            .    PASS   SVTYPE=DEL;END=2827708;HOMLEN=1;HOMSEQ=G;SVLEN=-14                 GT:GQ        1/1:14");

        assertThat(variantAllele.ref(), equalTo("CGTGGATGCGGGGAC"));
        assertThat(variantAllele.alt(), equalTo("C"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(1));
        assertThat(variantAllele.contigName(), equalTo("1"));
        assertThat(variantAllele.start(), equalTo(2827694));
        assertThat(variantAllele.end(), equalTo(2827708));
        assertThat(variantAllele.changeLength(), equalTo(-14));
        assertThat(variantAllele.length(), equalTo(15));
    }

    @Test
    void impreciseDeletion() {
        // 2.  An imprecise deletion of approximately 205 bp.
        GenomicVariant variantAllele = parseVcfRecord("2       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;END=321887;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62         GT:GQ        0/1:12");

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DEL>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(2));
        assertThat(variantAllele.contigName(), equalTo("2"));
        assertThat(variantAllele.coordinates(), equalTo(Coordinates.oneBased(321682, ConfidenceInterval.of(-56, 20), 321887, ConfidenceInterval.of(-10, 62))));
//        assertThat(variantAllele.start(), equalTo(Position.of(321682, -56, 20)));
//        assertThat(variantAllele.end(), equalTo(Position.of(321887, -10, 62)));
        assertThat(variantAllele.changeLength(), equalTo(-205));
    }

    @Test
    void impreciseAluDeletion() {
        // 3.  An imprecise deletion of an ALU element relative to the reference.
        GenomicVariant variantAllele = parseVcfRecord("2     14477084 .         C                <DEL:ME:ALU> 12   PASS   SVTYPE=DEL;END=14477381;SVLEN=-297;CIPOS=-22,18;CIEND=-12,32       GT:GQ        0/1:12");

        assertThat(variantAllele.ref(), equalTo("C"));
        assertThat(variantAllele.alt(), equalTo("<DEL:ME:ALU>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL_ME_ALU));

        assertThat(variantAllele.contigId(), equalTo(2));
        assertThat(variantAllele.contigName(), equalTo("2"));
        assertThat(variantAllele.coordinates(),
                equalTo(Coordinates.oneBased(14477084, ConfidenceInterval.of(-22, 18),
                14477381, ConfidenceInterval.of(-12, 32))));
        assertThat(variantAllele.changeLength(), equalTo(-297));
    }

    @Test
    void impreciseL1Insertion() {
        // 4.  An imprecise insertion of an L1 element relative to the reference.
        GenomicVariant variantAllele = parseVcfRecord("3      9425916 .         C                <INS:ME:L1>  23   PASS   SVTYPE=INS;END=9425916;SVLEN=6027;CIPOS=-16,22                     GT:GQ        1/1:15");

        assertThat(variantAllele.ref(), equalTo("C"));
        assertThat(variantAllele.alt(), equalTo("<INS:ME:L1>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.INS_ME));

        assertThat(variantAllele.contigId(), equalTo(3));
        assertThat(variantAllele.contigName(), equalTo("3"));
        assertThat(variantAllele.coordinates(),
                equalTo(Coordinates.oneBased(9425916, ConfidenceInterval.of(-16, 22),
                        9425916, ConfidenceInterval.precise())));
        assertThat(variantAllele.changeLength(), equalTo(6027));

    }

    @Test
    void impreciseDuplication() {
        // 5.  An imprecise duplication of approximately 21Kb.  The sample genotype is copy number 3 (one extra copy of the duplicated sequence).
        GenomicVariant variantAllele = parseVcfRecord("3     12665100 .         A                <DUP>        14   PASS   SVTYPE=DUP;END=12686200;SVLEN=21100;CIPOS=-500,500;CIEND=-500,500  GT:GQ:CN:CNQ ./.:0:3:16.2");

        assertThat(variantAllele.ref(), equalTo("A"));
        assertThat(variantAllele.alt(), equalTo("<DUP>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DUP));

        assertThat(variantAllele.contigId(), equalTo(3));
        assertThat(variantAllele.contigName(), equalTo("3"));
        assertThat(variantAllele.start(), equalTo(12665100));
        assertThat(variantAllele.startConfidenceInterval(), equalTo(ConfidenceInterval.of(-500, 500)));

        assertThat(variantAllele.changeLength(), equalTo(21100));

        assertThat(variantAllele.end(), equalTo(12686200));
        assertThat(variantAllele.endConfidenceInterval(), equalTo(ConfidenceInterval.of(-500, 500)));
    }

    @Test
    void impreciseTandemDuplication() {
        // 6.  An imprecise tandem duplication of 76bp.  The sample genotype is copy number 5 (but the two haplotypes are not known).
        GenomicVariant variantAllele = parseVcfRecord("4     18665128 .         T                <DUP:TANDEM> 11   PASS   SVTYPE=DUP;END=18665204;SVLEN=76;CIPOS=-10,10;CIEND=-10,10         GT:GQ:CN:CNQ ./.:0:5:8.3");

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DUP:TANDEM>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DUP_TANDEM));

        assertThat(variantAllele.contigId(), equalTo(4));
        assertThat(variantAllele.contigName(), equalTo("4"));
        assertThat(variantAllele.coordinates(),
                equalTo(Coordinates.oneBased(18665128, ConfidenceInterval.of(-10, 10),
                        18665204, ConfidenceInterval.of(-10, 10))));
        assertThat(variantAllele.changeLength(), equalTo(76));
    }

    @Test
    void impreciseStructuralVariantNoLengthSpecifiedCalculatesLength() {
        GenomicVariant variantAllele = parseVcfRecord("1 212471179 esv3588749 T <DEL> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVTYPE=DEL;VT=SV GT 0|1");

        assertThat(variantAllele.coordinates(),
                equalTo(Coordinates.oneBased(212471179, ConfidenceInterval.of(-471, 0),
                        212472619, ConfidenceInterval.of(0, 444))));
        assertThat(variantAllele.endMax(), equalTo(212472619 + 444));

        assertThat(variantAllele.changeLength(), equalTo(-1441));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DEL>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));
    }

    @Test
    void impreciseStructuralVariantLengthSpecifiedReturnsLengthFromVariantContext() {
        GenomicVariant variantAllele = parseVcfRecord("1 212471179 esv3588749 T <CNV> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVLEN=200;SVTYPE=DEL;VT=SV GT 0|1");

        assertThat(variantAllele.coordinates(),
                equalTo(Coordinates.oneBased(212471179, ConfidenceInterval.of(-471, 0),
                        212472619, ConfidenceInterval.of(0, 444))));
        assertThat(variantAllele.changeLength(), equalTo(200));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<CNV>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
    }

    @Test
    void dup() {
        GenomicVariant variantAllele = parseVcfRecord("3       138946021       .       N       <DUP>   50      .       SVTYPE=DUP;END=138946051        GT      0/1");

        assertThat(variantAllele.ref(), equalTo("N"));
        assertThat(variantAllele.alt(), equalTo("<DUP>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DUP));

        assertThat(variantAllele.contigId(), equalTo(3));
        assertThat(variantAllele.contigName(), equalTo("3"));
        assertThat(variantAllele.start(), equalTo(138946021));
        assertThat(variantAllele.changeLength(), equalTo(31));

        assertThat(variantAllele.end(), equalTo(138946051));
    }

    @Nested
    class MantaTests {

        @Test
        void mantaDeletion() {
            GenomicVariant variantAllele = parseVcfRecord("chr1   15725445      MantaDEL:1022:0:1:0:0:0     C      <DEL>  87     PASS       END=15728944;SVTYPE=DEL;SVLEN=-3499;SVINSLEN=15;SVINSSEQ=GGGCCGGCTAATATA;   GT:FT:GQ:PL:PR:SR    0/1:PASS:87:137,0,999:11,2:25,4");

            assertThat(variantAllele.ref(), equalTo("C"));
            assertThat(variantAllele.alt(), equalTo("<DEL>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(15725445));
            assertThat(variantAllele.changeLength(), equalTo(-3499));

            assertThat(variantAllele.end(), equalTo(15728944));
        }

        @Test
        void mantaInsertion() {
            GenomicVariant variantAllele = parseVcfRecord("1      40935372      MantaINS:3084:0:0:0:4:0     A      <INS>  58.00  PASS       END=40935373;SVTYPE=INS;UPSTREAM_PAIR_COUNT=0;DOWNSTREAM_PAIR_COUNT=0;PAIR_COUNT=0;LEFT_SVINSSEQ=AAAATATATATATATATATATATATATATATATATATATATATATATATTT;RIGHT_SVINSSEQ=ATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAATATATATATATATATATATATATATATATATATATATAT       GT:GQ:PR:SR   0/1:58:0,0:3,6");

            assertThat(variantAllele.ref(), equalTo("A"));
            assertThat(variantAllele.alt(), equalTo("<INS>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.INS));

            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(40935372));
            assertThat(variantAllele.changeLength(), equalTo(2));

            //TODO : check whether INS should have start == end
            assertThat(variantAllele.end(), equalTo(40935373));
        }

        @Test
        void mantaBreakend() {
            GenomicVariant variantAllele = parseVcfRecord("chr1    2683545 MantaBND:159:22:23:0:0:0:0      A       A]KN707905.1:547]       999     PASS    SVTYPE=BND;MATEID=MantaBND:159:22:23:0:0:0:1;IMPRECISE;CIPOS=-181,181;EVENT=MantaBND:159:22:23:0:0:0:0;JUNCTION_QUAL=0;BND_DEPTH=60;MATE_BND_DEPTH=120;CSQT=1|TTC34|ENST00000401095|    GT:FT:GQ:PL:PR  0/1:PASS:999:999,0,999:32,4");
            assertThat(variantAllele, is(nullValue()));
//        assertThat(variantAllele.ref(), equalTo("C"));
//        assertThat(variantAllele.alt(), equalTo("<DEL>"));
//        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));
//
//        assertThat(variantAllele.contigId(), equalTo(1));
//        assertThat(variantAllele.contigName(), equalTo("1"));
//        assertThat(variantAllele.start(), equalTo(15825445));
//        assertThat(variantAllele.changeLength(), equalTo(-3499));
//
//        assertThat(variantAllele.end(), equalTo(15828944));
        }
    }

    @Nested
    class CanvasTests {
        @Test
        void canvasGain() {
            GenomicVariant variantAllele = parseVcfRecord("1\t10006065\tCanvas:GAIN:1:10006065:10014592\tN\t<CNV>\t4.00\tq10;CLT10kb\tSVTYPE=CNV;END=10014592;\tGT:RC:BC:CN\t0/1:186:7:4");

            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(10006065));
            assertThat(variantAllele.end(), equalTo(10014592));
            assertThat(variantAllele.changeLength(), equalTo(8528));

            assertThat(variantAllele.ref(), equalTo("N"));
            assertThat(variantAllele.alt(), equalTo("<CNV>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
        }

        @Test
        void canvasGainNoGenotype() {
            GenomicVariant variantAllele = parseVcfRecord("1\t10006065\tCanvas:GAIN:1:10006065:10014592\tN\t<CNV>\t4.00\tq10;CLT10kb\tSVTYPE=CNV;END=10014592;\tCN\t4");
            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(10006065));
            assertThat(variantAllele.end(), equalTo(10014592));
            assertThat(variantAllele.changeLength(), equalTo(8528));

            assertThat(variantAllele.ref(), equalTo("N"));
            assertThat(variantAllele.alt(), equalTo("<CNV>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
        }

        @Test
        void canvasLoss() {
            GenomicVariant variantAllele = parseVcfRecord("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tGT:RC:BC:CN\t0/1:51:41:1\n");
            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(13195138));
            assertThat(variantAllele.end(), equalTo(13239068));
            assertThat(variantAllele.changeLength(), equalTo(43931));

            assertThat(variantAllele.ref(), equalTo("N"));
            assertThat(variantAllele.alt(), equalTo("<CNV>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
        }

        @Test
        void canvasLossCN1NoGenotype() {
            GenomicVariant variantAllele = parseVcfRecord("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tCN\t1\n");
            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(13195138));
            assertThat(variantAllele.end(), equalTo(13239068));
            assertThat(variantAllele.changeLength(), equalTo(43931));

            assertThat(variantAllele.ref(), equalTo("N"));
            assertThat(variantAllele.alt(), equalTo("<CNV>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
        }

        @Test
        void canvasLossCN0() {
            GenomicVariant variantAllele = parseVcfRecord("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tCN\t0\n");
            assertThat(variantAllele.contigId(), equalTo(1));
            assertThat(variantAllele.contigName(), equalTo("1"));
            assertThat(variantAllele.start(), equalTo(13195138));
            assertThat(variantAllele.end(), equalTo(13239068));
            assertThat(variantAllele.changeLength(), equalTo(43931));

            assertThat(variantAllele.ref(), equalTo("N"));
            assertThat(variantAllele.alt(), equalTo("<CNV>"));
            assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
        }
    }

    @Nested
    class ThousandGenomesCopyNumberTest {
        @Test
        void testCn0() {
            GenomicVariant variant = parseVcfRecord("15\t23671395\tesv3635841\tA\t<CN0>\t100\tPASS\tAC=2;AF=0.978235;AFR_AF=0.9198;AMR_AF=0.9971;AN=2;CIEND=-500,1000;CIPOS=-1000,500;CS=DEL_union;DP=11333;EAS_AF=0.999;END=23674750;EUR_AF=1;NS=2504;SAS_AF=1;SVTYPE=DEL;VT=SV\tGT\t1|1");
            assertThat(variant.variantType(), equalTo(VariantType.CNV));
        }
    }
    // breakend tests

    @Disabled
    @Test
    void preciseBreakend() {
        GenomicVariant variant = parseVcfRecord("2 321681 bnd_W G G]17:198982] 6 PASS SVTYPE=BND .");

        assertThat(variant.isBreakend(), equalTo(true));
        assertThat(variant.id(), equalTo("bnd_W"));
        assertThat(variant.contigId(), equalTo(2));
        assertThat(variant.start(), equalTo(321681));
        assertThat(variant.end(), equalTo(321681));

        assertThat(variant.length(), equalTo(1));

        assertThat(variant.ref(), equalTo("G"));
        assertThat(variant.alt(), equalTo(""));
        assertThat(variant.variantType(), equalTo(VariantType.BND));
    }
}