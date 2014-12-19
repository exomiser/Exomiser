/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.factories.VariantAnnotator;
import jannovar.annotation.Annotation;
import jannovar.annotation.AnnotationList;
import jannovar.common.VariantType;
import jannovar.exception.AnnotationException;
import jannovar.exome.Variant;
import jannovar.reference.Chromosome;
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

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantAnnotatorTest {
    
    private VariantAnnotator instance;
    
    private Map<Byte, Chromosome> chromosomeMap;   
    
    @Mock
    private Chromosome chromosome;
    private Variant variant;
    private Variant unknownChromosomeVariant;
    private Variant unknownPositionVariant;
    
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
        chromosomeMap = new HashMap<>();
        chromosomeMap.put(CHR_1, chromosome);
        instance = new VariantAnnotator(chromosomeMap);
        
        variant = new Variant(CHR_1, POSITION, REF, ALT, null, 0f, "known variant");
        unknownChromosomeVariant = new Variant(UNKNOWN_CHR, POSITION, REF, ALT, null, 0f, "unknown chromosome variant");
        unknownPositionVariant = new Variant(CHR_1, UNKNOWN_POSITION, REF, ALT, null, 0f, "unknown position variant");
        
        setUpMocks();
    }
    
    private void setUpMocks() throws Exception {
        Mockito.when(chromosome.getAnnotationList(POSITION, REF, ALT)).thenReturn(annotationList);
        Mockito.when(chromosome.getAnnotationList(UNKNOWN_POSITION, REF, ALT)).thenThrow(AnnotationException.class);
        Mockito.when(annotationList.getVariantType()).thenReturn(VariantType.MISSENSE);
    }

    @Test(expected = NullPointerException.class)
    public void testAnnotationOfNullThrowsNullPointer() {
        instance.annotateVariant(null);
    }
    
    @Test
    public void testAnnotationOfVariantAtUnknownPositionReturnsOriginalVariant() {
        assertThat(unknownPositionVariant.getVariantTypeConstant(), equalTo(VariantType.ERROR));
        instance.annotateVariant(unknownPositionVariant);
        assertThat(unknownPositionVariant.getVariantTypeConstant(), equalTo(VariantType.ERROR));
    }
    
    @Test
    public void testAnnotationOfVariantOfUnKnownChromosomeReturnsOriginalVariant() {
        assertThat(unknownChromosomeVariant.getVariantTypeConstant(), equalTo(VariantType.ERROR));
        instance.annotateVariant(unknownChromosomeVariant);
        assertThat(unknownChromosomeVariant.getVariantTypeConstant(), equalTo(VariantType.ERROR));
    }
    
    @Test
    public void testAnnotationOfKnownVariantSetsAnnotationList() {
        assertThat(variant.getVariantTypeConstant(), equalTo(VariantType.ERROR));
        instance.annotateVariant(variant);
        assertThat(variant.getVariantTypeConstant(), equalTo(VariantType.MISSENSE));
    }

}
