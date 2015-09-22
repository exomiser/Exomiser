/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.NcdsScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.tribble.readers.TabixReader;
import htsjdk.variant.variantcontext.VariantContext;
import java.io.IOException;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class NcdsDaoTest {
    
    private NcdsDao instance;
    
    @Mock
    private TabixReader ncdsTabixReader;
    
    private MockTabixIterator mockIterator;
    
    @Before
    public void setUp() {
        mockIterator = new MockTabixIterator();
        instance = new NcdsDao(ncdsTabixReader);
    }

    private static VariantEvaluation variant(int chr, int pos, String ref, String alt) {
        if (ref.equals("-") || alt.equals("-")) {
            //this is used to get round the fact that in real life the variant evaluation 
            //is built from a variantContext and some variantAnnotations
            return new VariantEvaluation.VariantBuilder(chr, pos, ref, alt)
                    .variantContext(Mockito.mock(VariantContext.class))
                    .build();
        }
        return new VariantEvaluation.VariantBuilder(chr, pos, ref, alt).variantEffect(VariantEffect.REGULATORY_REGION_VARIANT).build();
    }
    
    @Test
    public void testGetPathogenicityData_missenseVariant() {
        //missense variants are by definition protein-coding and therefore cannot be non-coding so we expect nothing 
        VariantEvaluation missenseVariant = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        assertThat(instance.getPathogenicityData(missenseVariant), equalTo(new PathogenicityData()));
    }
    
    @Test
    public void testGetPathogenicityData_unableToReadFromSource() {
        Mockito.when(ncdsTabixReader.query("1:1-1")).thenThrow(IOException.class);
        assertThat(instance.getPathogenicityData(variant(1, 1, "A", "T")), equalTo(new PathogenicityData()));
    }
    
    @Test
    public void testGetPathogenicityData_singleNucleotideVariationNoData() {
        mockIterator.setValues(Arrays.asList());
        Mockito.when(ncdsTabixReader.query("1:1-1")).thenReturn(mockIterator);

        assertThat(instance.getPathogenicityData(variant(1, 1, "A", "T")), equalTo(new PathogenicityData()));
    }
    
    @Test
    public void testGetPathogenicityData_singleNucleotideVariation() {
        mockIterator.setValues(Arrays.asList("1\t1\t1.0"));
        Mockito.when(ncdsTabixReader.query("1:1-1")).thenReturn(mockIterator);

        assertThat(instance.getPathogenicityData(variant(1, 1, "A", "T")), equalTo(new PathogenicityData(new NcdsScore(1f))));
    }
    
    @Test
    public void testGetPathogenicityData_insertion() {
        mockIterator.setValues(Arrays.asList("1\t1\t0.0", "1\t2\t1.0"));
        Mockito.when(ncdsTabixReader.query("1:1-2")).thenReturn(mockIterator);

        assertThat(instance.getPathogenicityData(variant(1, 1, "-", "TTT")), equalTo(new PathogenicityData(new NcdsScore(1f))));
    }
    
    @Test
    public void testGetPathogenicityData_deletion() {
        mockIterator.setValues(Arrays.asList("1\t1\t0.0", "1\t2\t0.5", "1\t3\t1.0"));
        Mockito.when(ncdsTabixReader.query("1:1-4")).thenReturn(mockIterator);

        assertThat(instance.getPathogenicityData(variant(1, 1, "TTT", "-")), equalTo(new PathogenicityData(new NcdsScore(1f))));
    }
}
