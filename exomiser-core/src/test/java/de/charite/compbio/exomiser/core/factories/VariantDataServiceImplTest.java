/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.CADDDao;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.dao.RegulatoryFeatureDao;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantDataServiceImplTest {

    @InjectMocks
    private VariantDataServiceImpl instance;
    @Mock
    private PathogenicityDao mockPathogenicityDao;
    @Mock
    private FrequencyDao mockFrequencyDao;
    @Mock
    private CADDDao mockCADDDao;
    @Mock
    private RegulatoryFeatureDao mockRegulatoryFeatureDao;
    
    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImplTest.class);
    private static final PathogenicityData PATH_DATA = new PathogenicityData(new PolyPhenScore(1), new MutationTasterScore(1), new SiftScore(0));
    private static final FrequencyData FREQ_DATA = new FrequencyData(new RsId(1234567), new Frequency(100.0f, FrequencySource.ESP_AFRICAN_AMERICAN));
    private static final PathogenicityData CADD_DATA = new PathogenicityData(new CaddScore(1));
    private static final VariantEffect REG_DATA = VariantEffect.REGULATORY_REGION_VARIANT;


    private VariantEvaluation varEval;

    @Before
    public void setUp() {
        varEval = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();

        MockitoAnnotations.initMocks(this);
        Mockito.when(mockPathogenicityDao.getPathogenicityData(varEval)).thenReturn(PATH_DATA);
        Mockito.when(mockFrequencyDao.getFrequencyData(varEval)).thenReturn(FREQ_DATA);
        Mockito.when(mockCADDDao.getPathogenicityData(varEval)).thenReturn(CADD_DATA);
        Mockito.when(mockRegulatoryFeatureDao.getRegulatoryFeatureData(varEval)).thenReturn(REG_DATA);

    }

    @Test
    public void testInstanceIsNotNull() {
        assertThat(instance, notNullValue());
    }

    /*
     * Ignore until sort out how VariantDataService should behave for CADD vs
     * PathogenictyFilter swithc
     */
    @Test
    public void serviceAddsPathogenicityDataToAVariantEvaluation() {
        instance.setVariantPathogenicityData(varEval);
        assertThat(varEval.getPathogenicityData(), equalTo(PATH_DATA));
    }

    /*
     * Ignore until sort out how VariantDataService should behave for CADD vs
     * PathogenictyFilter swithc
     */
    @Test
    public void serviceReturnsPathogenicityDataForAVariantEvaluation() {
        PathogenicityData result = instance.getVariantPathogenicityData(varEval);
        assertThat(result, equalTo(PATH_DATA));
    }
    /*
     * Ignore until sort out how VariantDataService should behave for CADD vs
     * PathogenictyFilter swithc
     */

    @Ignore
    @Test
    public void serviceAddsBothFrequencyAndPAthogenicityDataToAVariantEvaluation() {
        instance.setVariantFrequencyAndPathogenicityData(varEval);
        assertThat(varEval.getPathogenicityData(), equalTo(PATH_DATA));
        assertThat(varEval.getFrequencyData(), equalTo(FREQ_DATA));
        assertThat(varEval.getPathogenicityData(), equalTo(CADD_DATA));
        assertThat(varEval.getVariantEffect(), equalTo(REG_DATA));
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
