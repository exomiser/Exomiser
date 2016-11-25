package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TranscriptAnnotationTest {

    @Test
    public void testEmpty() {
        assertThat(TranscriptAnnotation.EMPTY, equalTo(TranscriptAnnotation.builder().build()));
    }

    @Test
    public void testVariantEffect() {
        VariantEffect value = VariantEffect.MISSENSE_VARIANT;
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .variantEffect(value)
                .build();
        assertThat(annotation.getVariantEffect(), equalTo(value));
    }

    @Test
    public void testGeneSymbol() {
        String value = "FGFR2";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .geneSymbol(value)
                .build();
        assertThat(annotation.getGeneSymbol(), equalTo(value));
    }

    @Test
    public void testTranscriptAccession() {
        String value = "uc021pzz.1";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .accession(value)
                .build();
        assertThat(annotation.getAccession(), equalTo(value));
    }

    @Test
    public void testHgvsCdsString() {
        String value = "c.1694A>C";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .hgvsCdna(value)
                .build();
        assertThat(annotation.getHgvsCdna(), equalTo(value));
    }

    @Test
    public void testHgvsProteinString() {
        String value = "p.(Glu565Ala)";
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .hgvsProtein(value)
                .build();
        assertThat(annotation.getHgvsProtein(), equalTo(value));
    }

    @Test
    public void testDistanceFromNearestGene() {
        int value = 0;
        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .distanceFromNearestGene(value)
                .build();
        assertThat(annotation.getDistanceFromNearestGene(), equalTo(value));
    }

    @Test
    public void testToString() {
        System.out.println(TranscriptAnnotation.builder().build());

        TranscriptAnnotation annotation = TranscriptAnnotation.builder()
                .geneSymbol("FGFR")
                .accession("uc021pzz.1")
                .hgvsCdna("c.1694A>C")
                .hgvsProtein("p.(Glu565Ala)")
                .distanceFromNearestGene(0)
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        System.out.println(annotation);
    }


}