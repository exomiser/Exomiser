package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.model.AllelePosition.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePositionTest {

    @Test(expected = NullPointerException.class)
    public void testNullRef() {
        AllelePosition instance = trim(1, null, "A");
    }

    @Test(expected = NullPointerException.class)
    public void testNullAlt() {
        AllelePosition instance = trim(1, "T", null);
    }

    @Test
    public void testEmptyRef() {
        AllelePosition instance = trim(1, "", "TA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo(""));
        assertThat(instance.getAlt(), equalTo("TA"));
    }

    @Test
    public void testEmptyAlt() {
        AllelePosition instance = trim(1, "TA", "");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("TA"));
        assertThat(instance.getAlt(), equalTo(""));
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
    public void testEvaOne() {
        AllelePosition instance = trim(1000, "AGTTC", "AGCC");

        assertThat(instance.getPos(), equalTo(1002));
        assertThat(instance.getRef(), equalTo("TT"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    /*
     * Taken from https://macarthurlab.org/2014/04/28/converting-genetic-variants-to-their-minimal-representation/
     * #CHROM POS ID  REF ALT
     * 1   1001    .   CTCC    CCC,C,CCCC
     */
    @Test
    public void testMcArthurCoLocatedOne() {
        AllelePosition instance = trim(1001, "CTCC", "CCC");

        assertThat(instance.getPos(), equalTo(1001));
        assertThat(instance.getRef(), equalTo("CT"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedTwo() {
        AllelePosition instance = trim(1001, "CTCC", "C");

        assertThat(instance.getPos(), equalTo(1001));
        assertThat(instance.getRef(), equalTo("CTCC"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedThree() {
        AllelePosition instance = trim(1001, "CTCC", "CCCC");

        assertThat(instance.getPos(), equalTo(1002));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("C"));
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

        assertThat(instance.getPos(), equalTo(1000));
        assertThat(instance.getRef(), equalTo("TGACGTAACGATT"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testEvaCoLocatedTwo() {
        AllelePosition instance = trim(1000, "TGACGTAACGATT", "TGACGTAACGGTT");

        assertThat(instance.getPos(), equalTo(1010));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testEvaCoLocatedThree() {
        AllelePosition instance = trim(1000, "TGACGTAACGATT", "TGACGTAATAC");

        assertThat(instance.getPos(), equalTo(1008));
        assertThat(instance.getRef(), equalTo("CGATT"));
        assertThat(instance.getAlt(), equalTo("TAC"));
    }

    @Test
    public void testOriginalReferencesNotMutated() {
        int pos = 2;
        String ref = "GGCA";
        String alt = "GG";

        AllelePosition instance = trim(pos, ref, alt);
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(3));
        assertThat(instance.getRef(), equalTo("GCA"));
        assertThat(instance.getAlt(), equalTo("G"));

        assertThat(pos, equalTo(2));
        assertThat(ref, equalTo("GGCA"));
        assertThat(alt, equalTo("GG"));
    }

    @Test
    public void testSnv() {
        AllelePosition instance = trim(1, "A", "T");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testTrimLeftRef() {
        AllelePosition instance = trim(1, "AT", "AA");

        assertThat(instance.getPos(), equalTo(2));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("A"));
    }

    @Test
    public void testTrimLeftAlt() {
        AllelePosition instance = trim(1, "AA", "AT");

        assertThat(instance.getPos(), equalTo(2));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testTrimRightRef() {
        AllelePosition instance = trim(1, "TA", "AA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("A"));
    }

    @Test
    public void testTrimRightAlt() {
        AllelePosition instance = trim(1, "AA", "TA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testMnpTrimLeft() {
        AllelePosition instance = trim(4, "GCAT", "GTGC");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimRight() {
        AllelePosition instance = trim(5, "CATG", "TGCG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimLeftAndRight() {
        AllelePosition instance = trim(4, "GCATG", "GTGCG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testInsertionLeftBaseDifferent() {
        AllelePosition instance = trim(1, "T", "ATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("ATG"));
    }

    @Test
    public void testInsertionLeftBaseEqual() {
        AllelePosition instance = trim(1, "T", "TATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqual() {
        AllelePosition instance = trim(1, "G", "TATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("G"));
        assertThat(instance.getAlt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqualDuplicated() {
        AllelePosition instance = trim(1, "GG", "TATGG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("G"));
        assertThat(instance.getAlt(), equalTo("TATG"));
    }

//    a variant is normalized, we simply need to manipulate the variant till the rightmost ends of the alleles are not the same,
// and apply truncation to the superfluous nucleotides on the left side of the variant to obtain a normalized variant.

// The algorithm to normalize a biallelic or multiallelic variant is as follows
//        1.   The alleles end with at least two different nucleotides.
//        2.   The alleles start with at least two different nucleotides, or the shortest allele has length 1

    @Test
    public void testTrimRightDeletion() {
        AllelePosition instance = trim(3, "GCACA", "GCA");

        assertThat(instance.getPos(), equalTo(3));
        assertThat(instance.getRef(), equalTo("GCA"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testTrimLeftDeletion() {
        AllelePosition instance = trim(2, "GGCA", "GG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(3));
        assertThat(instance.getRef(), equalTo("GCA"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testParsimoniousInsertion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = trim(1, "C", "CAC");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("C"));
        assertThat(instance.getAlt(), equalTo("CAC"));
    }

    @Test
    public void testParsimoniousDeletion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = trim(1, "CAC", "C");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("CAC"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMnpCouldBeSplit() {
        AllelePosition instance = trim(1, "TCT", "CCC");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("TCT"));
        assertThat(instance.getAlt(), equalTo("CCC"));
    }

    @Test
    public void testLongerSnvNeedsRightAndLeftTrim() {
        AllelePosition instance = trim(1, "TTTTATATGCATTCTTATCTTTTTATATGCATTCTTA", "TTTTATATGCATTCTTACCCTTTTATATGCATTCTTA");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.getPos(), equalTo(18));
        assertThat(instance.getRef(), equalTo("TCT"));
        assertThat(instance.getAlt(), equalTo("CCC"));
    }

    @Test
    public void testStructutalVariant() {
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