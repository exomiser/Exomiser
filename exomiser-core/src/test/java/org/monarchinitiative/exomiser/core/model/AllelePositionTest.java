package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.model.AllelePosition.minimise;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePositionTest {

    @Test(expected = NullPointerException.class)
    public void testNullRef() {
        AllelePosition instance = minimise(1, null, "A");
    }

    @Test(expected = NullPointerException.class)
    public void testNullAlt() {
        AllelePosition instance = minimise(1, "T", null);
    }

    @Test
    public void testEmptyRef() {
        AllelePosition instance = minimise(1, "", "TA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo(""));
        assertThat(instance.getAlt(), equalTo("TA"));
    }

    @Test
    public void testEmptyAlt() {
        AllelePosition instance = minimise(1, "TA", "");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("TA"));
        assertThat(instance.getAlt(), equalTo(""));
    }

    @Test
    public void testEvaOne() {
        AllelePosition instance = minimise(1000, "AGTTC", "AGCC");

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
        AllelePosition instance = minimise(1001, "CTCC", "CCC");

        assertThat(instance.getPos(), equalTo(1001));
        assertThat(instance.getRef(), equalTo("CT"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedTwo() {
        AllelePosition instance = minimise(1001, "CTCC", "C");

        assertThat(instance.getPos(), equalTo(1001));
        assertThat(instance.getRef(), equalTo("CTCC"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMcArthurCoLocatedThree() {
        AllelePosition instance = minimise(1001, "CTCC", "CCCC");

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
        assertThat(minimise(1001, "CTCC", "CCCC"), equalTo(AllelePosition.of(1002, "T", "C")));
        assertThat(minimise(1001, "CTCC", "CCC"), equalTo(AllelePosition.of(1001, "CT", "C")));
        assertThat(minimise(1001, "CTCC", "CTC"), equalTo(AllelePosition.of(1002, "TC", "T")));
        assertThat(minimise(1001, "CTAG", "CTG"), equalTo(AllelePosition.of(1002, "TA", "T")));
        assertThat(minimise(1001, "CTCC", "CTACC"), equalTo(AllelePosition.of(1002, "T", "TA")));
        assertThat(minimise(1001, "TCAGCAGCAG", "TCAGCAG"), equalTo(AllelePosition.of(1001, "TCAG", "T")));
        assertThat(minimise(1001, "CTT", "CTTT"), equalTo(AllelePosition.of(1001, "C", "CT")));
        assertThat(minimise(1001, "CTT", "C"), equalTo(AllelePosition.of(1001, "CTT", "C")));
        assertThat(minimise(1001, "CTT", "CT"), equalTo(AllelePosition.of(1001, "CT", "C")));
        assertThat(minimise(1001, "AAAATATATATAT", "A"), equalTo(AllelePosition.of(1001, "AAAATATATATAT", "A")));
        assertThat(minimise(1001, "AAAATATATATAT", "AATAT"), equalTo(AllelePosition.of(1001, "AAAATATAT", "A")));
        assertThat(minimise(1001, "ACACACACAC", "AACAC"), equalTo(AllelePosition.of(1001, "ACACAC", "A")));
    }


    @Test
    public void testEvaCoLocatedOne() {
        AllelePosition instance = minimise(1000, "TGACGTAACGATT", "T");

        assertThat(instance.getPos(), equalTo(1000));
        assertThat(instance.getRef(), equalTo("TGACGTAACGATT"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testEvaCoLocatedTwo() {
        AllelePosition instance = minimise(1000, "TGACGTAACGATT", "TGACGTAACGGTT");

        assertThat(instance.getPos(), equalTo(1010));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testEvaCoLocatedThree() {
        AllelePosition instance = minimise(1000, "TGACGTAACGATT", "TGACGTAATAC");

        assertThat(instance.getPos(), equalTo(1008));
        assertThat(instance.getRef(), equalTo("CGATT"));
        assertThat(instance.getAlt(), equalTo("TAC"));
    }

    @Test
    public void testOriginalReferencesNotMutated() {
        int pos = 2;
        String ref = "GGCA";
        String alt = "GG";

        AllelePosition instance = minimise(pos, ref, alt);
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
        AllelePosition instance = minimise(1, "A", "T");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testTrimLeftRef() {
        AllelePosition instance = minimise(1, "AT", "AA");

        assertThat(instance.getPos(), equalTo(2));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("A"));
    }

    @Test
    public void testTrimLeftAlt() {
        AllelePosition instance = minimise(1, "AA", "AT");

        assertThat(instance.getPos(), equalTo(2));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testTrimRightRef() {
        AllelePosition instance = minimise(1, "TA", "AA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("A"));
    }

    @Test
    public void testTrimRightAlt() {
        AllelePosition instance = minimise(1, "AA", "TA");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
    }

    @Test
    public void testMnpTrimLeft() {
        AllelePosition instance = minimise(4, "GCAT", "GTGC");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimRight() {
        AllelePosition instance = minimise(5, "CATG", "TGCG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testMnpTrimLeftAndRight() {
        AllelePosition instance = minimise(4, "GCATG", "GTGCG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(5));
        assertThat(instance.getRef(), equalTo("CAT"));
        assertThat(instance.getAlt(), equalTo("TGC"));
    }

    @Test
    public void testInsertionLeftBaseDifferent() {
        AllelePosition instance = minimise(1, "T", "ATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("ATG"));
    }

    @Test
    public void testInsertionLeftBaseEqual() {
        AllelePosition instance = minimise(1, "T", "TATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("T"));
        assertThat(instance.getAlt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqual() {
        AllelePosition instance = minimise(1, "G", "TATG");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("G"));
        assertThat(instance.getAlt(), equalTo("TATG"));
    }

    @Test
    public void testInsertionRightBaseEqualDuplicated() {
        AllelePosition instance = minimise(1, "GG", "TATGG");

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
        AllelePosition instance = minimise(3, "GCACA", "GCA");

        assertThat(instance.getPos(), equalTo(3));
        assertThat(instance.getRef(), equalTo("GCA"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testTrimLeftDeletion() {
        AllelePosition instance = minimise(2, "GGCA", "GG");
        System.out.println(instance);
        assertThat(instance.getPos(), equalTo(3));
        assertThat(instance.getRef(), equalTo("GCA"));
        assertThat(instance.getAlt(), equalTo("G"));
    }

    @Test
    public void testParsimoniousInsertion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = minimise(1, "C", "CAC");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("C"));
        assertThat(instance.getAlt(), equalTo("CAC"));
    }

    @Test
    public void testParsimoniousDeletion() {
        //http://genome.sph.umich.edu/wiki/Variant_Normalization
        //this should really be left aligned, but we can't do that as we have nothing to align against
        AllelePosition instance = minimise(1, "CAC", "C");

        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("CAC"));
        assertThat(instance.getAlt(), equalTo("C"));
    }

    @Test
    public void testMnpCouldBeSplit() {
        AllelePosition instance = minimise(1, "TCT", "CCC");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.getPos(), equalTo(1));
        assertThat(instance.getRef(), equalTo("TCT"));
        assertThat(instance.getAlt(), equalTo("CCC"));
    }

    @Test
    public void testLongerSnvNeedsRightAndLeftTrim() {
        AllelePosition instance = minimise(1, "TTTTATATGCATTCTTATCTTTTTATATGCATTCTTA", "TTTTATATGCATTCTTACCCTTTTATATGCATTCTTA");
        //note - this can be expressed as two alleles:
        //1 T C
        //3 T C
        assertThat(instance.getPos(), equalTo(18));
        assertThat(instance.getRef(), equalTo("TCT"));
        assertThat(instance.getAlt(), equalTo("CCC"));
    }
}