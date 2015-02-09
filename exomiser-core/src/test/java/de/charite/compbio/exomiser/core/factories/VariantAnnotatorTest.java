/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantAnnotatorTest {
    
    private VariantAnnotator instance;
    
    JannovarData jannovarData;

    @Mock
    private VariantContext variant;
    @Mock
    private VariantContext unknownChromosomeVariant;
    @Mock
    private VariantContext unknownPositionVariant;
    
    private static final Byte CHR_1 = 1;
    private static final Byte UNKNOWN_CHR = 0;
    
    private static final int POSITION = 1;
    private static final int UNKNOWN_POSITION = 0;
    
    private static final String REF = "A";
    private static final String ALT = "T";
    
    @Mock
    private final AnnotationList annotationList = new AnnotationList(new ArrayList<Annotation>());

    @Before
    public void setUp() throws Exception {
        jannovarData = new JannovarData(HG19RefDictBuilder.build(), ImmutableList.<TranscriptModel> of());
        instance = new VariantAnnotator(jannovarData);
        
        // setUpMocks();
    }
    
    // FIXME(holtgrew): Uncomment tests again.

    // private void setUpMocks() throws Exception {
    // Mockito.when(chromosome.getAnnotationList(POSITION, REF, ALT)).thenReturn(annotationList);
    // Mockito.when(chromosome.getAnnotationList(UNKNOWN_POSITION, REF, ALT)).thenThrow(AnnotationException.class);
    // Mockito.when(annotationList.getVariantType()).thenReturn(VariantType.MISSENSE);
    // }
    //
    // @Test(expected = NullPointerException.class)
    // public void testAnnotationOfNullThrowsNullPointer() {
    // instance.annotateVariantContext(null);
    // }
    //
    // @Test
    // public void testAnnotationOfVariantAtUnknownPositionReturnsOriginalVariant() {
    // assertThat(unknownPositionVariant.getVariantTypeConstant(), equalTo(VariantEffect.ERROR));
    // instance.annotateVariantContext(unknownPositionVariant);
    // assertThat(unknownPositionVariant.getVariantTypeConstant(), equalTo(VariantEffect.ERROR));
    // }
    //
    // @Test
    // public void testAnnotationOfVariantOfUnKnownChromosomeReturnsOriginalVariant() {
    // assertThat(unknownChromosomeVariant.getVariantTypeConstant(), equalTo(VariantEffect.ERROR));
    // instance.annotateVariantContext(unknownChromosomeVariant);
    // assertThat(unknownChromosomeVariant.getVariantTypeConstant(), equalTo(VariantEffect.ERROR));
    // }
    //
    // @Test
    // public void testAnnotationOfKnownVariantSetsAnnotationList() {
    // assertThat(variant.getVariantEffect(), equalTo(VariantEffect.ERROR));
    // instance.annotateVariantContext(variant);
    // assertThat(variant.getVariantEffect(), equalTo(VariantEffect.MISSENSE));
    // }

}
