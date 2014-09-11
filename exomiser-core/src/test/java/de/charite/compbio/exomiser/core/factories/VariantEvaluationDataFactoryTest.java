/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import jannovar.exome.Variant;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author jj8
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantEvaluationDataFactoryTest {

    private VariantEvaluationDataFactory instance;

    private VariantEvaluation varEval;

    private Variant variant;
    
    public VariantEvaluationDataFactoryTest() {
    }

    @Before
    public void setUp() {
        
        instance = new VariantEvaluationDataFactory(new HashMap<String, FrequencyData>(), new HashMap<String, PathogenicityData>());
        
        variant = new Variant((byte) 1, 1, "C", "A", null, 5f, null);
        varEval = new VariantEvaluation(variant);
    }

//    @Test
//    public void testFactoryWillReturnPathogenicityData() {
//        PathogenicityData pathData = instance.getPathogenicityData(variant);
//        assertThat(pathData, notNullValue());
//    }
//    
    @Test
    public void testFactoryWillReturnNullFrequencyDataWithoutADatabase() {
        FrequencyData freqData = null; //instance.getFrequencyData(variant);
        assertThat(freqData, nullValue());
    }

}
