package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.AllelePosition;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotatorTest {

    private JannovarVariantAnnotator instance = new JannovarVariantAnnotator(TestFactory.buildDefaultJannovarData());

    @Test
    public void testGetAnnotationsForUnknownContigVariant() {
        VariantAnnotations annotations = instance.getVariantAnnotations("UNKNOWN", 1, "A", "T");
        System.out.println(annotations);
        assertThat(annotations, not(nullValue()));
        assertThat(annotations.hasAnnotation(), is(false));
        assertThat(annotations.getChr(), equalTo(0));
        assertThat(annotations.getPos(), equalTo(0)); //Jannovar uses and returns 0-based coordinates.
        assertThat(annotations.getRef(), equalTo("A"));
        assertThat(annotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForKnownContigVariant() {
        VariantAnnotations annotations = instance.getVariantAnnotations("chr1", 1, "A", "T");
        System.out.println(annotations);
        assertThat(annotations.hasAnnotation(), is(true));
        assertThat(annotations.getChr(), equalTo(1));
        assertThat(annotations.getPos(), equalTo(0));  //Jannovar uses and returns 0-based coordinates.
        assertThat(annotations.getRef(), equalTo("A"));
        assertThat(annotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForKnownExonicVariant() {
        int pos = 123256215;
        String ref = "T";
        String alt = "G";

        VariantAnnotations annotations = instance.getVariantAnnotations("10", pos, ref, alt);

        System.out.println(annotations);

        assertThat(annotations.hasAnnotation(), is(true));
        assertThat(annotations.getChr(), equalTo(10));
        assertThat(annotations.getPos(), equalTo(pos - 1)); //Jannovar uses and returns 0-based coordinates.
        assertThat(annotations.getRef(), equalTo(ref));
        assertThat(annotations.getAlt(), equalTo(alt));
        assertThat(annotations.getHighestImpactEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedInsertionJannovarShiftsRight() {
        int pos = 118608471;
        String ref = "GT";
        String alt = "GTT";

        VariantAnnotations annotations = instance.getVariantAnnotations("X", pos, ref, alt);

        System.out.println(annotations);

        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);

        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", annotations.getPos(), annotations.getRef(), annotations
                .getAlt());
        System.out.println(allelePosition);

        assertThat(annotations.hasAnnotation(), is(false));
        assertThat(annotations.getChr(), equalTo(23));
        assertThat(annotations.getPos(), equalTo(118608471 + 1)); //Jannovar trims from the left first i.e. right-shifts.
        assertThat(annotations.getRef(), equalTo(""));
        assertThat(annotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedInsertionTrimmedWithAllelePosition() {
        int pos = 118608470;
        String ref = "AGT";
        String alt = "AGTT";

        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);
        VariantAnnotations annotations = instance.getVariantAnnotations("X", allelePosition);
        System.out.println(annotations);

        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", annotations.getPos(), annotations.getRef(), annotations
                .getAlt());
        System.out.println(allelePosition);

        assertThat(annotations.hasAnnotation(), is(false));
        assertThat(annotations.getChr(), equalTo(23));
        assertThat(annotations.getPos(), equalTo(allelePosition.getPos()));
        assertThat(annotations.getRef(), equalTo(""));
        assertThat(annotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedDeletionJannovarShiftsRight() {
        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        VariantAnnotations annotations = instance.getVariantAnnotations("X", pos, ref, alt);

        System.out.println(annotations);

        AllelePosition allelePosition = AllelePosition.trim(pos, ref, alt);

        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", annotations.getPos(), annotations.getRef(), annotations
                .getAlt());
        System.out.println(allelePosition);

        assertThat(annotations.hasAnnotation(), is(false));
        assertThat(annotations.getChr(), equalTo(23));
        assertThat(annotations.getPos(), equalTo(118608471 + 1)); //Jannovar trims from the left first i.e. right-shifts.
        assertThat(annotations.getRef(), equalTo("T"));
        assertThat(annotations.getAlt(), equalTo(""));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedDeletionTrimmedWithAllelePosition() {

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

        AllelePosition exomiserTrimmedAllelePosition = AllelePosition.trim(pos, ref, alt);

        VariantAnnotations exomiserTrimmedAnnotations = instance.getVariantAnnotations("X", exomiserTrimmedAllelePosition);
        System.out.println(exomiserTrimmedAnnotations);
        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", exomiserTrimmedAnnotations.getPos(), exomiserTrimmedAnnotations
                .getRef(), exomiserTrimmedAnnotations.getAlt());
        System.out.println("Trimmed: " + exomiserTrimmedAllelePosition);

        assertThat(exomiserTrimmedAnnotations.hasAnnotation(), is(false));
        assertThat(exomiserTrimmedAnnotations.getChr(), equalTo(23));
        assertThat(exomiserTrimmedAnnotations.getPos(), equalTo(118608471));
        assertThat(exomiserTrimmedAnnotations.getRef(), equalTo("T"));
        assertThat(exomiserTrimmedAnnotations.getAlt(), equalTo(""));


        AllelePosition unTrimmedAllelePosition = AllelePosition.of(pos, ref, alt);

        VariantAnnotations jannovarTrimmedAnnotations = instance.getVariantAnnotations("X", unTrimmedAllelePosition);
        System.out.println(jannovarTrimmedAnnotations);
        System.out.printf("AnnotationList{pos=%d, ref='%s', alt='%s'}%n", jannovarTrimmedAnnotations.getPos(), jannovarTrimmedAnnotations
                .getRef(), jannovarTrimmedAnnotations.getAlt());
        System.out.println("Untrimmed: " + unTrimmedAllelePosition);

        assertThat(jannovarTrimmedAnnotations.hasAnnotation(), is(false));
        assertThat(jannovarTrimmedAnnotations.getChr(), equalTo(23));
        assertThat(jannovarTrimmedAnnotations.getPos(), equalTo(exomiserTrimmedAnnotations.getPos() + 1));
        assertThat(jannovarTrimmedAnnotations.getRef(), equalTo("T"));
        assertThat(jannovarTrimmedAnnotations.getAlt(), equalTo(""));
    }

}