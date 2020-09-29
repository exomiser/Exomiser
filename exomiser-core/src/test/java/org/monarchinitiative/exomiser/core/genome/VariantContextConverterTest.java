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

import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.ConfidenceInterval;
import org.monarchinitiative.exomiser.core.model.VariantAllele;
import org.monarchinitiative.exomiser.core.model.VariantType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantContextConverterTest {

    private VariantContext parseVariantContext(String vcfLine) {
        return TestVcfParser.forSamples("Sample1")
                .toVariantContext(vcfLine);
    }

    @Test
    public void snv() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tT\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("G"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.SNV));

        assertThat(variantAllele.getStartContigId(), equalTo(10));
        assertThat(variantAllele.getStartContigName(), equalTo("10"));
        assertThat(variantAllele.getStart(), equalTo(123256215));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getStartMin(), equalTo(123256215));
        assertThat(variantAllele.getStartMax(), equalTo(123256215));

        assertThat(variantAllele.getLength(), equalTo(0));

        assertThat(variantAllele.getEnd(), equalTo(123256215));
        assertThat(variantAllele.getEndContigId(), equalTo(10));
        assertThat(variantAllele.getEndContigName(), equalTo("10"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(123256215));
        assertThat(variantAllele.getEndMax(), equalTo(123256215));
    }

    @Test
    public void smallInsertion() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tT\tGA\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("GA"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.INDEL));

        assertThat(variantAllele.getStartContigId(), equalTo(10));
        assertThat(variantAllele.getStartContigName(), equalTo("10"));
        assertThat(variantAllele.getStart(), equalTo(123256215));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getStartMin(), equalTo(123256215));
        assertThat(variantAllele.getStartMax(), equalTo(123256215));

        assertThat(variantAllele.getLength(), equalTo(1));

        assertThat(variantAllele.getEnd(), equalTo(123256215));
        assertThat(variantAllele.getEndContigId(), equalTo(10));
        assertThat(variantAllele.getEndContigName(), equalTo("10"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(123256215));
        assertThat(variantAllele.getEndMax(), equalTo(123256215));
    }

    @Test
    public void smallDeletion() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tTA\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getRef(), equalTo("TA"));
        assertThat(variantAllele.getAlt(), equalTo("G"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.INDEL));

        assertThat(variantAllele.getStartContigId(), equalTo(10));
        assertThat(variantAllele.getStartContigName(), equalTo("10"));
        assertThat(variantAllele.getStart(), equalTo(123256215));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getStartMin(), equalTo(123256215));
        assertThat(variantAllele.getStartMax(), equalTo(123256215));

        assertThat(variantAllele.getLength(), equalTo(-1));

        assertThat(variantAllele.getEnd(), equalTo(123256216));
        assertThat(variantAllele.getEndContigId(), equalTo(10));
        assertThat(variantAllele.getEndContigName(), equalTo("10"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(123256216));
        assertThat(variantAllele.getEndMax(), equalTo(123256216));
    }

    @Test
    public void smallMnv() {
        VariantContext variantContext = parseVariantContext("10\t123256215\t.\tTA\tGC\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getRef(), equalTo("TA"));
        assertThat(variantAllele.getAlt(), equalTo("GC"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.MNV));

        assertThat(variantAllele.getStartContigId(), equalTo(10));
        assertThat(variantAllele.getStartContigName(), equalTo("10"));
        assertThat(variantAllele.getStart(), equalTo(123256215));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getStartMin(), equalTo(123256215));
        assertThat(variantAllele.getStartMax(), equalTo(123256215));

        assertThat(variantAllele.getLength(), equalTo(0));

        assertThat(variantAllele.getEnd(), equalTo(123256216));
        assertThat(variantAllele.getEndContigId(), equalTo(10));
        assertThat(variantAllele.getEndContigName(), equalTo("10"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(123256216));
        assertThat(variantAllele.getEndMax(), equalTo(123256216));
    }

    // Structural tests
    @Test
    void preciseDeletionWithKnownBreakpoint() {
        // 1.  A precise deletion with known breakpoint, a one base micro-homology, and a sample that is homozygous for the deletion.
        VariantContext variantContext = parseVariantContext("1       2827694 rs2376870 CGTGGATGCGGGGAC  C            .    PASS   SVTYPE=DEL;END=2827708;HOMLEN=1;HOMSEQ=G;SVLEN=-14                 GT:GQ        1/1:14");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);


        assertThat(variantAllele.getRef(), equalTo("CGTGGATGCGGGGAC"));
        assertThat(variantAllele.getAlt(), equalTo("C"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.INDEL));

        assertThat(variantAllele.getStartContigId(), equalTo(1));
        assertThat(variantAllele.getStartContigName(), equalTo("1"));
        assertThat(variantAllele.getStart(), equalTo(2827694));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getStartMin(), equalTo(2827694));
        assertThat(variantAllele.getStartMax(), equalTo(2827694));

        assertThat(variantAllele.getLength(), equalTo(-14));

        assertThat(variantAllele.getEnd(), equalTo(2827708));
        assertThat(variantAllele.getEndContigId(), equalTo(1));
        assertThat(variantAllele.getEndContigName(), equalTo("1"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(2827708));
        assertThat(variantAllele.getEndMax(), equalTo(2827708));

    }

    @Test
    void impreciseDeletion() {
        // 2.  An imprecise deletion of approximately 205 bp.
        VariantContext variantContext = parseVariantContext("2       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;END=321887;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62         GT:GQ        0/1:12");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("<DEL>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DEL));

        assertThat(variantAllele.getStartContigId(), equalTo(2));
        assertThat(variantAllele.getStartContigName(), equalTo("2"));
        assertThat(variantAllele.getStart(), equalTo(321682));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-56, 20)));
        assertThat(variantAllele.getStartMin(), equalTo(321682 - 56));
        assertThat(variantAllele.getStartMax(), equalTo(321682 + 20));

        assertThat(variantAllele.getLength(), equalTo(-205));

        assertThat(variantAllele.getEnd(), equalTo(321887));
        assertThat(variantAllele.getEndContigId(), equalTo(2));
        assertThat(variantAllele.getEndContigName(), equalTo("2"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(-10, 62)));
        assertThat(variantAllele.getEndMin(), equalTo(321887 - 10));
        assertThat(variantAllele.getEndMax(), equalTo(321887 + 62));
    }

    @Test
    void impreciseAluDeletion() {
        // 3.  An imprecise deletion of an ALU element relative to the reference.
        VariantContext variantContext = parseVariantContext("2     14477084 .         C                <DEL:ME:ALU> 12   PASS   SVTYPE=DEL;END=14477381;SVLEN=-297;CIPOS=-22,18;CIEND=-12,32       GT:GQ        0/1:12");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);

        assertThat(variantAllele.getRef(), equalTo("C"));
        assertThat(variantAllele.getAlt(), equalTo("<DEL:ME:ALU>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DEL_ME_ALU));

        assertThat(variantAllele.getStartContigId(), equalTo(2));
        assertThat(variantAllele.getStartContigName(), equalTo("2"));
        assertThat(variantAllele.getStart(), equalTo(14477084));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-22, 18)));
        assertThat(variantAllele.getStartMin(), equalTo(14477084 - 22));
        assertThat(variantAllele.getStartMax(), equalTo(14477084 + 18));

        assertThat(variantAllele.getLength(), equalTo(-297));

        assertThat(variantAllele.getEnd(), equalTo(14477381));
        assertThat(variantAllele.getEndContigId(), equalTo(2));
        assertThat(variantAllele.getEndContigName(), equalTo("2"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(-12, 32)));
        assertThat(variantAllele.getEndMin(), equalTo(14477381 - 12));
        assertThat(variantAllele.getEndMax(), equalTo(14477381 + 32));
    }

    @Test
    void impreciseL1Insertion() {
        // 4.  An imprecise insertion of an L1 element relative to the reference.
        VariantContext variantContext = parseVariantContext("3      9425916 .         C                <INS:ME:L1>  23   PASS   SVTYPE=INS;END=9425916;SVLEN=6027;CIPOS=-16,22                     GT:GQ        1/1:15");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);

        assertThat(variantAllele.getRef(), equalTo("C"));
        assertThat(variantAllele.getAlt(), equalTo("<INS:ME:L1>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.INS_ME));

        assertThat(variantAllele.getStartContigId(), equalTo(3));
        assertThat(variantAllele.getStartContigName(), equalTo("3"));
        assertThat(variantAllele.getStart(), equalTo(9425916));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-16, 22)));
        assertThat(variantAllele.getStartMin(), equalTo(9425916 - 16));
        assertThat(variantAllele.getStartMax(), equalTo(9425916 + 22));

        assertThat(variantAllele.getLength(), equalTo(6027));

        assertThat(variantAllele.getEnd(), equalTo(9425916));
        assertThat(variantAllele.getEndContigId(), equalTo(3));
        assertThat(variantAllele.getEndContigName(), equalTo("3"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(variantAllele.getEndMin(), equalTo(9425916));
        assertThat(variantAllele.getEndMax(), equalTo(9425916));
    }

    @Test
    void impreciseDuplication() {
//        5.  An imprecise duplication of approximately 21Kb.  The sample genotype is copy number 3 (one extra copy of the duplicated sequence).
//
        VariantContext variantContext = parseVariantContext("3     12665100 .         A                <DUP>        14   PASS   SVTYPE=DUP;END=12686200;SVLEN=21100;CIPOS=-500,500;CIEND=-500,500  GT:GQ:CN:CNQ ./.:0:3:16.2");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);

        assertThat(variantAllele.getRef(), equalTo("A"));
        assertThat(variantAllele.getAlt(), equalTo("<DUP>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DUP));

        assertThat(variantAllele.getStartContigId(), equalTo(3));
        assertThat(variantAllele.getStartContigName(), equalTo("3"));
        assertThat(variantAllele.getStart(), equalTo(12665100));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-500, 500)));
        assertThat(variantAllele.getStartMin(), equalTo(12665100 - 500));
        assertThat(variantAllele.getStartMax(), equalTo(12665100 + 500));

        assertThat(variantAllele.getLength(), equalTo(21100));

        assertThat(variantAllele.getEnd(), equalTo(12686200));
        assertThat(variantAllele.getEndContigId(), equalTo(3));
        assertThat(variantAllele.getEndContigName(), equalTo("3"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(-500, 500)));
        assertThat(variantAllele.getEndMin(), equalTo(12686200 - 500));
        assertThat(variantAllele.getEndMax(), equalTo(12686200 + 500));
    }

    @Test
    void impreciseTandemDuplication() {
//        6.  An imprecise tandem duplication of 76bp.  The sample genotype is copy number 5 (but the two haplotypes are not known).
        VariantContext variantContext = parseVariantContext("4     18665128 .         T                <DUP:TANDEM> 11   PASS   SVTYPE=DUP;END=18665204;SVLEN=76;CIPOS=-10,10;CIEND=-10,10         GT:GQ:CN:CNQ ./.:0:5:8.3");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));
        System.out.println(variantAllele);

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("<DUP:TANDEM>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DUP_TANDEM));

        assertThat(variantAllele.getStartContigId(), equalTo(4));
        assertThat(variantAllele.getStartContigName(), equalTo("4"));
        assertThat(variantAllele.getStart(), equalTo(18665128));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-10, 10)));
        assertThat(variantAllele.getStartMin(), equalTo(18665128 - 10));
        assertThat(variantAllele.getStartMax(), equalTo(18665128 + 10));

        assertThat(variantAllele.getLength(), equalTo(76));

        assertThat(variantAllele.getEnd(), equalTo(18665204));
        assertThat(variantAllele.getEndContigId(), equalTo(4));
        assertThat(variantAllele.getEndContigName(), equalTo("4"));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(-10, 10)));
        assertThat(variantAllele.getEndMin(), equalTo(18665204 - 10));
        assertThat(variantAllele.getEndMax(), equalTo(18665204 + 10));
    }

    @Test
    void impreciseStructuralVariantNoLengthSpecifiedCalculatesLength() {
        VariantContext variantContext = parseVariantContext("1 212471179 esv3588749 T <CN0> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVTYPE=DEL;VT=SV GT 0|1");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getStart(), equalTo(212471179));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-471, 0)));
        assertThat(variantAllele.getStartMin(), equalTo(212471179 - 471));
        assertThat(variantAllele.getStartMax(), equalTo(212471179));

        assertThat(variantAllele.getEnd(), equalTo(212472619));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(0, 444)));
        assertThat(variantAllele.getEndMin(), equalTo(212472619));
        assertThat(variantAllele.getEndMax(), equalTo(212472619 + 444));

        assertThat(variantAllele.getLength(), equalTo(1440));

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("<CN0>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DEL));
    }

    @Test
    void impreciseStructuralVariantLengthSpecifiedReturnsLengthFromVariantContext() {
        VariantContext variantContext = parseVariantContext("1 212471179 esv3588749 T <CN0> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVLEN=200;SVTYPE=DEL;VT=SV GT 0|1");

        VariantAllele variantAllele = VariantContextConverter.toVariantAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variantAllele.getStart(), equalTo(212471179));
        assertThat(variantAllele.getStartCi(), equalTo(ConfidenceInterval.of(-471, 0)));
        assertThat(variantAllele.getStartMin(), equalTo(212471179 - 471));
        assertThat(variantAllele.getStartMax(), equalTo(212471179));

        assertThat(variantAllele.getEnd(), equalTo(212472619));
        assertThat(variantAllele.getEndCi(), equalTo(ConfidenceInterval.of(0, 444)));
        assertThat(variantAllele.getEndMin(), equalTo(212472619));
        assertThat(variantAllele.getEndMax(), equalTo(212472619 + 444));

        assertThat(variantAllele.getLength(), equalTo(200));

        assertThat(variantAllele.getRef(), equalTo("T"));
        assertThat(variantAllele.getAlt(), equalTo("<CN0>"));
        assertThat(variantAllele.getVariantType(), equalTo(VariantType.DEL));
    }

}