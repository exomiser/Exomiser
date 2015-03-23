/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.frequency;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataTest {
    
    private FrequencyData instance;
    private FrequencyData rsIdOnlyData;
    private FrequencyData noFreqData;
    
    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;
    
    private static final Frequency ESP_ALL_PASS = new Frequency(PASS_FREQ, ESP_ALL);
    private static final Frequency ESP_AA_PASS = new Frequency(PASS_FREQ, ESP_AFRICAN_AMERICAN);
    private static final Frequency ESP_EA_PASS = new Frequency(PASS_FREQ, ESP_EUROPEAN_AMERICAN);
    private static final Frequency DBSNP_PASS = new Frequency(PASS_FREQ, THOUSAND_GENOMES);
    
    private static final RsId RSID = new RsId(12335);
       
    @Before
    public void setUp() {
         instance = new FrequencyData(RSID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS);
         rsIdOnlyData = new FrequencyData(RSID);
         noFreqData = new FrequencyData(null);
    }

    @Test
    public void testGetRsId() {
        assertThat(instance.getRsId(), equalTo(RSID));
    }

    @Test
    public void testHasFrequencyDataForSource() {
//        assertThat(realPassData.hasDataFromSource(FrequencySource.ESP_AFRICAN_AMERICAN), is(true));
    }
    
    @Test
    public void testGetDbSnpMaf() {
        assertThat(instance.getDbSnpMaf(), equalTo(DBSNP_PASS));
    }

    @Test
    public void testGetEspEaMaf() {
        assertThat(instance.getEspEaMaf(), equalTo(ESP_EA_PASS));
    }

    @Test
    public void testGetEspAaMaf() {
        assertThat(instance.getEspAaMaf(), equalTo(ESP_AA_PASS));
    }

    @Test
    public void testGetEspAllMaf() {
        assertThat(instance.getEspAllMaf(), equalTo(ESP_ALL_PASS));
    }

    @Test
    public void testNotRepresentedInDatabase() {
        assertThat(noFreqData.representedInDatabase(), is(false));
    }
    
     @Test
    public void testRepresentedInDatabaseEspAllOnly() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS);
        assertThat(instance.representedInDatabase(), is(true));
    }

    @Test
    public void testHasDbSnpData() {
        assertThat(instance.hasDbSnpData(), is(true));
    }

    @Test
    public void testRepresentedInDatabaseRsIdOnly() {
        assertThat(rsIdOnlyData.representedInDatabase(), is(true));
    }
    
    @Test
    public void testHasDbSnpRsIdTrue() {
        assertThat(rsIdOnlyData.hasDbSnpRsID(), is(true));
    }
    
    @Test
    public void testHasDbSnpRsIdFalse() {
        assertThat(noFreqData.hasDbSnpRsID(), is(false));
    }

    @Test
    public void testHasEspDataTrue() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS);
        assertThat(instance.hasEspData(), is(true));
    }
    
    @Test
    public void testHasExacDataTrue() {
        instance = new FrequencyData(RSID, new Frequency(PASS_FREQ, EXAC_AFRICAN_INC_AFRICAN_AMERICAN));
        assertThat(instance.hasExacData(), is(true));
    }
    
    @Test
    public void testHasExacDataFalse() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS);
        assertThat(instance.hasExacData(), is(false));
    }
    
    @Test
    public void testHasEspDataFalse() {
        instance = noFreqData;
        assertThat(instance.hasEspData(), is(false));
    }

    @Test
    public void testGetKnownFrequencies() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS, ESP_EA_PASS);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_EA_PASS);
        expResult.add(ESP_ALL_PASS);
        
        List<Frequency> result = instance.getKnownFrequencies();
        
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetMaxFreqWhenNoData() {
        float maxFreq = 0.0F;
        assertThat(noFreqData.getMaxFreq(), equalTo(maxFreq));
    }
    
    @Test
    public void testGetMaxFreqWithData() {
        float maxFreq = 89.5F;
        Frequency maxFrequency = new Frequency(maxFreq);
        instance = new FrequencyData(RSID, DBSNP_PASS, maxFrequency, ESP_AA_PASS, ESP_EA_PASS);
        assertThat(instance.getMaxFreq(), equalTo(maxFreq));
    }
}
