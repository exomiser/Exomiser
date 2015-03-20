/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import jannovar.exome.Variant;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author jj8
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantEvaluationDataServiceTest {

    @InjectMocks
    private VariantEvaluationDataService instance;
    @Mock
    private PathogenicityDao mockPathogenicityDao;
    @Mock
    private FrequencyDao mockFrequencyDao;

    private static final PathogenicityData PATH_DATA = new PathogenicityData(new PolyPhenScore(1), new MutationTasterScore(1), new SiftScore(0), new CaddScore(1));
    private static final FrequencyData FREQ_DATA = new FrequencyData(new RsId(1234567), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f), new Frequency(100.0f));
    
    private VariantEvaluation varEval;
    private Variant variant;

    @Before
    public void setUp() {
        variant = new Variant((byte) 1, 1, "C", "A", null, 5f, null);
        varEval = new VariantEvaluation(variant);
        
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockPathogenicityDao.getPathogenicityData(variant)).thenReturn(PATH_DATA);
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(FREQ_DATA);

    }

    @Test
    public void testInstanceIsNotNull() {
        assertThat(instance, notNullValue());
    }

    @Test
    public void serviceAddsPathogenicityDataToAVariantEvaluation() {
        instance.setVariantPathogenicityData(varEval);
        assertThat(varEval.getPathogenicityData(), equalTo(PATH_DATA));
    }
    
    @Test
    public void serviceReturnsPathogenicityDataForAVariantEvaluation() {
        PathogenicityData result = instance.getVariantPathogenicityData(varEval);
        assertThat(result, equalTo(PATH_DATA));
    }
    
    @Test
    public void serviceAddsBothFrequencyAndPAthogenicityDataToAVariantEvaluation() {
        instance.setVariantFrequencyAndPathogenicityData(varEval);
        assertThat(varEval.getPathogenicityData(), equalTo(PATH_DATA));
        assertThat(varEval.getFrequencyData(), equalTo(FREQ_DATA));
    }
    @Test
    public void serviceAddsFrequencyDataToAVariantEvaluation() {
        instance.setVariantFrequencyData(varEval);
        assertThat(varEval.getFrequencyData(), equalTo(FREQ_DATA));
    }
    
    @Test
    public void serviceReturnsFrequencyDataForAVariantEvaluation() {
        FrequencyData result = instance.getVariantFrequencyData(varEval);
        assertThat(result, equalTo(FREQ_DATA));
    }
}
