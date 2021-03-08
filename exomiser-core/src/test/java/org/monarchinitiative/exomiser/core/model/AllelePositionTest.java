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

package org.monarchinitiative.exomiser.core.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.model.AllelePosition.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePositionTest {

    @Test
    public void testNullRef() {
        assertThrows(NullPointerException.class, () -> trim(1, null, "A"));
    }

    @Test
    public void testNullAlt() {
        assertThrows(NullPointerException.class, () -> trim(1, "T", null));
    }

    @Test
    void nonVariant() {
        AllelePosition instance = trim(1, "AAT", ".");
        assertThat(instance, equalTo(AllelePosition.of(1, "AAT", ".")));
    }

    @Test
    public void testEmptyRef() {
        AllelePosition instance = trim(1, "", "TA");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.ref(), equalTo(""));
        assertThat(instance.alt(), equalTo("TA"));
        assertThat(instance.length(), equalTo(0));
        assertThat(instance.changeLength(), equalTo(2));
    }

    @Test
    public void testEmptyAlt() {
        AllelePosition instance = trim(1, "TA", "");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.ref(), equalTo("TA"));
        assertThat(instance.alt(), equalTo(""));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.changeLength(), equalTo(-2));
    }

    @Test
    void ends() {
        assertThat(endOf(1, "", ""), equalTo(1));
        assertThat(endOf(1, "", "T"), equalTo(1));
        assertThat(endOf(1, "", "AA"), equalTo(1));
        assertThat(endOf(1, "A", ""), equalTo(1));
        assertThat(endOf(1, "AA", ""), equalTo(2));
        assertThat(endOf(1, "AA", "G"), equalTo(2));
        assertThat(endOf(1, "AA", "GT"), equalTo(2));
        assertThat(endOf(200, "AA", "GT"), equalTo(201));
        assertThat(endOf(200, "AAT", "AT"), equalTo(201));
    }

    private int endOf(int start, String ref, String alt) {
        AllelePosition trimmed = trim(start, ref, alt);
        return trimmed.end();
    }

    @Test
    public void testIsSnv() {
        assertThat(isSnv("A", "T"), is(true));
        assertThat(isSnv("AA", "GT"), is(false));
        assertThat(isSnv("ATT", "A"), is(false));
        assertThat(isSnv("T", "TTA"), is(false));
    }

    @Test
    public void testIsInsertion() {
        assertThat(isInsertion("A", "T"), is(false));
        assertThat(isInsertion("AA", "GT"), is(false));
        assertThat(isInsertion("ATT", "A"), is(false));
        assertThat(isInsertion("T", "TTA"), is(true));
    }

    @Test
    public void testIsDeletion() {
        assertThat(isDeletion("A", "T"), is(false));
        assertThat(isDeletion("AA", "GT"), is(false));
        assertThat(isDeletion("ATT", "A"), is(true));
        assertThat(isDeletion("T", "TTA"), is(false));
    }

    @Test
    void testLength() {
        assertThat(length("A", "T"), equalTo(1));
        assertThat(length("AA", "GT"), equalTo(2));
        assertThat(length("ATT", "A"), equalTo(3));
        assertThat(length("T", "TTA"), equalTo(1));
    }

    @Test
    void testChangeLength() {
        assertThat(AllelePosition.of(1, "A", "T").changeLength(), equalTo(0));
        assertThat(AllelePosition.of(1, "AA", "GT").changeLength(), equalTo(0));
        assertThat(AllelePosition.of(1, "ATT", "A").changeLength(), equalTo(-2));
        assertThat(AllelePosition.of(1, "T", "TTA").changeLength(), equalTo(2));
    }

    @Test
    public void testEvaOne() {
        AllelePosition instance = trim(1000, "AGTTC", "AGCC");

        assertThat(instance.start(), equalTo(1002));
        assertThat(instance.end(), equalTo(1003));
        assertThat(instance.ref(), equalTo("TT"));
        assertThat(instance.alt(), equalTo("C"));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.changeLength(), equalTo(-1));
        assertThat(instance.isSymbolic(), is(false));
    }

    /*
     * Taken from https://macarthurlab.org/2014/04/28/converting-genetic-variants-to-their-minimal-representation/
     * #CHROM POS ID  REF ALT
     * 1   1001    .   CTCC    CCC,C,CCCC
     */
    @Test
    public void testMcArthurCoLocatedOne() {
        AllelePosition instance = trim(1001, "CTCC", "CCC");

        assertThat(instance.start(), equalTo(1001));
        assertThat(instance.ref(), equalTo("CT"));
        assertThat(instance.alt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedTwo() {
        AllelePosition instance = trim(1001, "CTCC", "C");

        assertThat(instance.start(), equalTo(1001));
        assertThat(instance.ref(), equalTo("CTCC"));
        assertThat(instance.alt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedThree() {
        AllelePosition instance = trim(1001, "CTCC", "CCCC");

        assertThat(instance.start(), equalTo(1002));
        assertThat(instance.ref(), equalTo("T"));
        assertThat(instance.alt(), equalTo("C"));
    }

    /**
     * Cases taken from
     * https://github.com/ericminikel/minimal_representation/blob/master/test_minimal_representation.py
     */
    @Test
    public void testMcArthurMinimals() {
        assertThat(trim(1001, "CTCC", "CCCC"), equalTo(AllelePosition.of(1002, "T", "C")));
        assertThat(trim(1001, "CTCC", "CCC"), equalTo(AllelePosition.of(1001, "CT", "C")));
        assertThat(trim(1001, "CTCC", "CTC"), equalTo(AllelePosition.of(1002, "TC", "T")));
        assertThat(trim(1001, "CTAG", "CTG"), equalTo(AllelePosition.of(1002, "TA", "T")));
        assertThat(trim(1001, "CTCC", "CTACC"), equalTo(AllelePosition.of(1002, "T", "TA")));
        assertThat(trim(1001, "TCAGCAGCAG", "TCAGCAG"), equalTo(AllelePosition.of(1001, "TCAG", "T")));
        assertThat(trim(1001, "CTT", "CTTT"), equalTo(AllelePosition.of(1001, "C", "CT")));
        assertThat(trim(1001, "CTT", "C"), equalTo(AllelePosition.of(1001, "CTT", "C")));
        assertThat(trim(1001, "CTT", "CT"), equalTo(AllelePosition.of(1001, "CT", "C")));
        assertThat(trim(1001, "AAAATATATATAT", "A"), equalTo(AllelePosition.of(1001, "AAAATATATATAT", "A")));
        assertThat(trim(1001, "AAAATATATATAT", "AATAT"), equalTo(AllelePosition.of(1001, "AAAATATAT", "A")));
        assertThat(trim(1001, "ACACACACAC", "AACAC"), equalTo(AllelePosition.of(1001, "ACACAC", "A")));
    }


    @Test
    public void testEvaCoLocatedOne() {
        AllelePosition instance = trim(1000, "TGACGTAACGATT", "T");

        assertThat(instance.start(), equalTo(1000));
        assertThat(instance.ref(), equalTo("TGACGTAACGATT"));
        assertThat(instance.alt(), equalTo("T"));
    }

    @Test
    public void testEvaCoLocatedTwo() {
        AllelePosition instance = trim(1000, "TGACGTAACGATT", "TGACGTAACGGTT");

        assertThat(instance.start(), equalTo(1010));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("G"));
    }

    @Test
    public void testEvaCoLocatedThree() {
        AllelePosition instance = trim(1000, "TGACGTAACGATT", "TGACGTAATAC");

        assertThat(instance.start(), equalTo(1008));
        assertThat(instance.ref(), equalTo("CGATT"));
        assertThat(instance.alt(), equalTo("TAC"));
    }

    @Test
    public void testOriginalReferencesNotMutated() {
        int pos = 2;
        String ref = "GGCA";
        String alt = "GG";

        AllelePosition instance = trim(pos, ref, alt);
        assertThat(instance.start(), equalTo(3));
        assertThat(instance.ref(), equalTo("GCA"));
        assertThat(instance.alt(), equalTo("G"));

        assertThat(pos, equalTo(2));
        assertThat(ref, equalTo("GGCA"));
        assertThat(alt, equalTo("GG"));
    }

    @Test
    public void testSnv() {
        AllelePosition instance = trim(1, "A", "T");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("T"));
    }

    @Test
    public void testTrimLeftRef() {
        AllelePosition instance = trim(1, "AT", "AA");

        assertThat(instance.start(), equalTo(2));
        assertThat(instance.ref(), equalTo("T"));
        assertThat(instance.alt(), equalTo("A"));
    }

    @Test
    public void testTrimLeftAlt() {
        AllelePosition instance = trim(1, "AA", "AT");

        assertThat(instance.start(), equalTo(2));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("T"));
    }

    @Test
    public void testTrimRightRef() {
        AllelePosition instance = trim(1, "TA", "AA");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("T"));
        assertThat(instance.alt(), equalTo("A"));
    }

    @Test
    public void testTrimRightAlt() {
        AllelePosition instance = trim(1, "AA", "TA");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("T"));
    }

    @Test
    public void testMnpTrimLeft() {
        AllelePosition instance = trim(4, "GCAT", "GTGC");

        assertThat(instance.start(), equalTo(5));
        assertThat(instance.ref(), equalTo("CAT"));
        assertThat(instance.alt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimRight() {
        AllelePosition instance = trim(5, "CATG", "TGCG");

        assertThat(instance.start(), equalTo(5));
        assertThat(instance.ref(), equalTo("CAT"));
        assertThat(instance.alt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimLeftAndRight() {
        AllelePosition instance = trim(4, "GCATG", "GTGCG");

        assertThat(instance.start(), equalTo(5));
        assertThat(instance.ref(), equalTo("CAT"));
        assertThat(instance.alt(), equalTo("TGC"));
    }

    @Test
    public void testInsertionLeftBaseDifferent() {
        AllelePosition instance = trim(1, "T", "ATG");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("T"));
        assertThat(instance.alt(), equalTo("ATG"));
    }

    @Test
    public void testInsertionLeftBaseEqual() {
        AllelePosition instance = trim(1, "T", "TATG");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("T"));
        assertThat(instance.alt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqual() {
        AllelePosition instance = trim(1, "G", "TATG");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("G"));
        assertThat(instance.alt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqualDuplicated() {
        AllelePosition instance = trim(1, "GG", "TATGG");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("G"));
        assertThat(instance.alt(), equalTo("TATG"));
    }

//    a variant is normalized, we simply need to manipulate the variant till the rightmost ends of the alleles are not the same,
// and apply truncation to the superfluous nucleotides on the left side of the variant to obtain a normalized variant.

// The algorithm to normalize a biallelic or multiallelic variant is as follows
//        1.   The alleles end with at least two different nucleotides.
//        2.   The alleles start with at least two different nucleotides, or the shortest allele has length 1

    @Test
    public void testTrimRightDeletion() {
        AllelePosition instance = trim(3, "GCACA", "GCA");

        assertThat(instance.start(), equalTo(3));
        assertThat(instance.ref(), equalTo("GCA"));
        assertThat(instance.alt(), equalTo("G"));
    }

    @Test
    public void testTrimLeftDeletion() {
        AllelePosition instance = trim(2, "GGCA", "GG");

        assertThat(instance.start(), equalTo(3));
        assertThat(instance.ref(), equalTo("GCA"));
        assertThat(instance.alt(), equalTo("G"));
    }

    @Test
    public void testParsimoniousInsertion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = trim(1, "C", "CAC");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("C"));
        assertThat(instance.alt(), equalTo("CAC"));
    }

    @Test
    public void testParsimoniousDeletion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = trim(1, "CAC", "C");

        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("CAC"));
        assertThat(instance.alt(), equalTo("C"));
    }

    @Test
    public void testMnpCouldBeSplit() {
        AllelePosition instance = trim(1, "TCT", "CCC");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.ref(), equalTo("TCT"));
        assertThat(instance.alt(), equalTo("CCC"));
    }

    @Test
    public void testLongerSnvNeedsRightAndLeftTrim() {
        AllelePosition instance = trim(1, "TTTTATATGCATTCTTATCTTTTTATATGCATTCTTA", "TTTTATATGCATTCTTACCCTTTTATATGCATTCTTA");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.start(), equalTo(18));
        assertThat(instance.ref(), equalTo("TCT"));
        assertThat(instance.alt(), equalTo("CCC"));
    }

    @Test
    public void testStructuralVariant() {
        assertThat(trim(4477084, "C", "<DEL:ME:ALU>"), equalTo(AllelePosition.of(4477084, "C", "<DEL:ME:ALU>")));
    }

    @Test
    public void testRearrangementBreakends() {
        assertThat(trim(321681, "G", "G]17:198982]"), equalTo(AllelePosition.of(321681, "G", "G]17:198982]")));
        assertThat(trim(321682, "T", "]13:123456]T"), equalTo(AllelePosition.of(321682, "T", "]13:123456]T")));
        assertThat(trim(123456, "C", "C[2:321682["), equalTo(AllelePosition.of(123456, "C", "C[2:321682[")));
        assertThat(trim(123457, "A", "[17:198983[A"), equalTo(AllelePosition.of(123457, "A", "[17:198983[A")));
        assertThat(trim(198982, "A", "A]2:321681]"), equalTo(AllelePosition.of(198982, "A", "A]2:321681]")));
        assertThat(trim(198983, "C", "[13:123457[C"), equalTo(AllelePosition.of(198983, "C", "[13:123457[C")));
    }

    @Test
    public void testInsertionBreakends() {
        assertThat(trim(32168, "T", "]13 : 123456]AGTNNNNNCAT"), equalTo(AllelePosition.of(32168, "T", "]13 : 123456]AGTNNNNNCAT")));
        assertThat(trim(123456, "C", "CAGTNNNNNCA[2 : 321682["), equalTo(AllelePosition.of(123456, "C", "CAGTNNNNNCA[2 : 321682[")));
    }

    @Test
    public void testTelomericBreakends() {
        assertThat(trim(0, "N", ".[13 : 123457["), equalTo(AllelePosition.of(0, "N", ".[13 : 123457[")));
        assertThat(trim(1, "T", "]13 : 123456]T"), equalTo(AllelePosition.of(1, "T", "]13 : 123456]T")));
        assertThat(trim(123456, "C", "C[1 : 1["), equalTo(AllelePosition.of(123456, "C", "C[1 : 1[")));
        assertThat(trim(123457, "A", "]1 : 0]A"), equalTo(AllelePosition.of(123457, "A", "]1 : 0]A")));
    }

    @Test
    void testIsSymbolic() {
        assertThat(AllelePosition.of(4477084, "C", "<DEL:ME:ALU>").isSymbolic(), is(true));
        assertThat(AllelePosition.of(321681, "G", "G]17:198982]").isSymbolic(), is(true));
        assertThat(AllelePosition.of(0, "N", ".[13 : 123457[").isSymbolic(), is(true));
        assertThat(AllelePosition.of(0, "C", "C.").isSymbolic(), is(true));
        // Having a symbolic ref allele isn't mention in the VCF 4.2 spec, but we'll make sure though
        assertThat(AllelePosition.of(4477084, "<INS>", "A").isSymbolic(), is(true));
    }

    @Test
    void testBreakends() {
        assertThat(AllelePosition.of(321681, "G", "G]17:198982]").isBreakend(), is(true));
        assertThat(AllelePosition.of(0, "N", ".[13 : 123457[").isBreakend(), is(true));
        assertThat(AllelePosition.of(0, "C", "C.").isBreakend(), is(true));
        assertThat(AllelePosition.of(4477084, "<INS>", "A").isBreakend(), is(false));
        assertThat(AllelePosition.of(1, "A", "C").isBreakend(), is(false));
    }

    /**
     * Nirvana style trimming:
     * https://github.com/Illumina/Nirvana/blob/master/VariantAnnotation/Algorithms/BiDirectionalTrimmer.cs
     */
    static AllelePosition trimNirvana(int pos, String ref, String alt) {
        // do not trim if ref and alt are same
        if (ref.equals(alt)) return AllelePosition.of(pos, ref, alt);

        if (ref == null) ref = "";
        if (alt == null) alt = "";

        // trimming at the start
        int i = 0;
        while (i < ref.length() && i < alt.length() && ref.charAt(i) == alt.charAt(i)) {
            i++;
        }

        if (i > 0) {
            pos += i;
            alt = alt.substring(i);
            ref = ref.substring(i);
        }

        // trimming at the end
        int j = 0;
        while (j < ref.length() && j < alt.length() && ref.charAt(ref.length() - j - 1) == alt.charAt(alt.length() - j - 1)) {
            j++;
        }

        if (j <= 0) {
            return AllelePosition.of(pos, ref, alt);
        }

        alt = alt.substring(0, alt.length() - j);
        ref = ref.substring(0, ref.length() - j);
        return AllelePosition.of(pos, ref, alt);
    }

    /**
     * Jannovar style trimming:
     * https://github.com/charite/jannovar/blob/master/jannovar-core/src/main/java/de/charite/compbio/jannovar/reference/VariantDataCorrector.java
     */
    AllelePosition trimJannovar(int pos, String ref, String alt) {
        int idx = 0;
        // beginning
        while (idx < ref.length() && idx < alt.length() && ref.charAt(idx) == alt.charAt(idx)) {
            idx++;
        }
        if (idx == ref.length() && idx == alt.length() && idx > 0)
            idx -= 1;
        pos += idx;
        ref = ref.substring(idx);
        alt = alt.substring(idx);

        // end
        int xdi = ref.length();
        int diff = ref.length() - alt.length();
        while (xdi > 0 && xdi - diff > 0 && ref.charAt(xdi - 1) == alt.charAt(xdi - 1 - diff)) {
            xdi--;
        }
        if (xdi == 0 && ref.length() > 0 && alt.length() > 0)
            xdi += 1;
        ref = xdi == 0 ? "" : ref.substring(0, xdi);
        alt = xdi - diff == 0 ? "" : alt.substring(0, xdi - diff);

        return AllelePosition.of(pos, ref, alt);
    }
    //See also GATK 4:
    // https://github.com/broadinstitute/gatk/blob/master/src/main/java/org/broadinstitute/hellbender/utils/variant/GATKVariantContextUtils.java#L1033
}