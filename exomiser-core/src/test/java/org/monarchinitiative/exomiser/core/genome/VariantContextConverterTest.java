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
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantContextConverterTest {

    private final VariantContextConverter instance = VariantContextConverter.of(GenomicAssemblies.GRCh37p13(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

    private VariantContext parseVariantContext(String vcfLine) {
        return TestVcfParser.forSamples("Sample1")
                .toVariantContext(vcfLine);
    }

    @Test
    public void snv() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tT\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("G"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.SNV));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(123256215)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(123256215)));

        assertThat(variantAllele.length(), equalTo(1));
    }

    @Test
    public void smallInsertion() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tT\tGA\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("GA"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.INS));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(123256215)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(123256215)));

        assertThat(variantAllele.length(), equalTo(1));
    }

    @Test
    public void smallDeletion() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tTA\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("TA"));
        assertThat(variantAllele.alt(), equalTo("G"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(123256215)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(123256216)));
        assertThat(variantAllele.changeLength(), equalTo(-1));

    }

    @Test
    public void smallMnv() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tTA\tGC\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("TA"));
        assertThat(variantAllele.alt(), equalTo("GC"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.MNV));

        assertThat(variantAllele.contigId(), equalTo(10));
        assertThat(variantAllele.contigName(), equalTo("10"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(123256215)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(123256216)));
        assertThat(variantAllele.length(), equalTo(2));
    }

    // Structural tests
    @Test
    void preciseDeletionWithKnownBreakpoint() {
        // 1.  A precise deletion with known breakpoint, a one base micro-homology, and a sample that is homozygous for the deletion.
        VariantContext variantContext = parseVariantContext("1       2827694 rs2376870 CGTGGATGCGGGGAC  C            .    PASS   SVTYPE=DEL;END=2827708;HOMLEN=1;HOMSEQ=G;SVLEN=-14                 GT:GQ        1/1:14");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("CGTGGATGCGGGGAC"));
        assertThat(variantAllele.alt(), equalTo("C"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(1));
        assertThat(variantAllele.contigName(), equalTo("1"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(2827694)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(2827708)));
        assertThat(variantAllele.changeLength(), equalTo(-14));
        assertThat(variantAllele.length(), equalTo(15));
    }

    @Test
    void impreciseDeletion() {
        // 2.  An imprecise deletion of approximately 205 bp.
        VariantContext variantContext = parseVariantContext("2       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;END=321887;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62         GT:GQ        0/1:12");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DEL>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.contigId(), equalTo(2));
        assertThat(variantAllele.contigName(), equalTo("2"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(321682, -56, 20)));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(321887, -10, 62)));
        assertThat(variantAllele.changeLength(), equalTo(-205));
    }

    @Test
    void impreciseAluDeletion() {
        // 3.  An imprecise deletion of an ALU element relative to the reference.
        VariantContext variantContext = parseVariantContext("2     14477084 .         C                <DEL:ME:ALU> 12   PASS   SVTYPE=DEL;END=14477381;SVLEN=-297;CIPOS=-22,18;CIEND=-12,32       GT:GQ        0/1:12");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("C"));
        assertThat(variantAllele.alt(), equalTo("<DEL:ME:ALU>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL_ME_ALU));

        assertThat(variantAllele.contigId(), equalTo(2));
        assertThat(variantAllele.contigName(), equalTo("2"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(14477084, ConfidenceInterval.of(-22, 18))));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(14477381, ConfidenceInterval.of(-12, 32))));
        assertThat(variantAllele.changeLength(), equalTo(-297));
    }

    @Test
    void impreciseL1Insertion() {
        // 4.  An imprecise insertion of an L1 element relative to the reference.
        VariantContext variantContext = parseVariantContext("3      9425916 .         C                <INS:ME:L1>  23   PASS   SVTYPE=INS;END=9425916;SVLEN=6027;CIPOS=-16,22                     GT:GQ        1/1:15");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("C"));
        assertThat(variantAllele.alt(), equalTo("<INS:ME:L1>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.INS_ME));

        assertThat(variantAllele.contigId(), equalTo(3));
        assertThat(variantAllele.contigName(), equalTo("3"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(9425916, ConfidenceInterval.of(-16, 22))));
        assertThat(variantAllele.changeLength(), equalTo(6027));

        assertThat(variantAllele.endPosition(), equalTo(Position.of(9425916)));
    }

    @Test
    void impreciseDuplication() {
//        5.  An imprecise duplication of approximately 21Kb.  The sample genotype is copy number 3 (one extra copy of the duplicated sequence).
//
        VariantContext variantContext = parseVariantContext("3     12665100 .         A                <DUP>        14   PASS   SVTYPE=DUP;END=12686200;SVLEN=21100;CIPOS=-500,500;CIEND=-500,500  GT:GQ:CN:CNQ ./.:0:3:16.2");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("A"));
        assertThat(variantAllele.alt(), equalTo("<DUP>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DUP));

        assertThat(variantAllele.contigId(), equalTo(3));
        assertThat(variantAllele.contigName(), equalTo("3"));
        assertThat(variantAllele.start(), equalTo(12665100));
//        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-500, 500)));
//        assertThat(variantAllele.getStartMin(), equalTo(12665100 - 500));
//        assertThat(variantAllele.getStartMax(), equalTo(12665100 + 500));

        assertThat(variantAllele.changeLength(), equalTo(21100));

        assertThat(variantAllele.end(), equalTo(12686200));
//        assertThat(variantAllele.getEndContigId(), equalTo(3));
//        assertThat(variantAllele.getEndContigName(), equalTo("3"));
//        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(-500, 500)));
//        assertThat(variantAllele.getEndMin(), equalTo(12686200 - 500));
//        assertThat(variantAllele.getEndMax(), equalTo(12686200 + 500));
    }

    @Test
    void impreciseTandemDuplication() {
//        6.  An imprecise tandem duplication of 76bp.  The sample genotype is copy number 5 (but the two haplotypes are not known).
        VariantContext variantContext = parseVariantContext("4     18665128 .         T                <DUP:TANDEM> 11   PASS   SVTYPE=DUP;END=18665204;SVLEN=76;CIPOS=-10,10;CIEND=-10,10         GT:GQ:CN:CNQ ./.:0:5:8.3");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DUP:TANDEM>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DUP_TANDEM));

        assertThat(variantAllele.contigId(), equalTo(4));
        assertThat(variantAllele.contigName(), equalTo("4"));
        assertThat(variantAllele.startPosition(), equalTo(Position.of(18665128, ConfidenceInterval.of(-10, 10))));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(18665204, ConfidenceInterval.of(-10, 10))));
        assertThat(variantAllele.changeLength(), equalTo(76));
    }

    @Test
    void impreciseStructuralVariantNoLengthSpecifiedCalculatesLength() {
        VariantContext variantContext = parseVariantContext("1 212471179 esv3588749 T <DEL> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVTYPE=DEL;VT=SV GT 0|1");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.startPosition(), equalTo(Position.of(212471179, ConfidenceInterval.of(-471, 0))));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(212472619, ConfidenceInterval.of(0, 444))));
        assertThat(variantAllele.endPosition().maxPos(), equalTo(212472619 + 444));

        assertThat(variantAllele.changeLength(), equalTo(-1440));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<DEL>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.DEL));
    }

    @Test
    void impreciseStructuralVariantLengthSpecifiedReturnsLengthFromVariantContext() {
        VariantContext variantContext = parseVariantContext("1 212471179 esv3588749 T <CNV> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVLEN=200;SVTYPE=DEL;VT=SV GT 0|1");

        Variant variantAllele = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.startPosition(), equalTo(Position.of(212471179, ConfidenceInterval.of(-471, 0))));
        assertThat(variantAllele.endPosition(), equalTo(Position.of(212472619, ConfidenceInterval.of(0, 444))));
        assertThat(variantAllele.changeLength(), equalTo(200));

        assertThat(variantAllele.ref(), equalTo("T"));
        assertThat(variantAllele.alt(), equalTo("<CNV>"));
        assertThat(variantAllele.variantType(), equalTo(VariantType.CNV));
    }

    // breakend tests

    @Disabled
    @Test
    void preciseBreakend() {
        VariantContext variantContext = parseVariantContext("2 321681 bnd_W G G]17:198982] 6 PASS SVTYPE=BND .");

        Variant variant = instance.convertToVariant(variantContext, variantContext.getAlternateAllele(0));

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