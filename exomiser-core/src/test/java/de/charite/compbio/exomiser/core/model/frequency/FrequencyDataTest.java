/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.frequency;

import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class FrequencyDataTest {
    
    
    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;
    
    private static final Frequency ESP_ALL_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_ALL_FAIL = new Frequency(FAIL_FREQ);
    
    private static final Frequency ESP_AA_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_AA_FAIL = new Frequency(FAIL_FREQ);
    
    private static final Frequency ESP_EA_PASS = new Frequency(PASS_FREQ);
    private static final Frequency ESP_EA_FAIL = new Frequency(FAIL_FREQ);
    
    private static final Frequency DBSNP_PASS = new Frequency(PASS_FREQ);
    private static final Frequency DBSNP_FAIL = new Frequency(FAIL_FREQ);    
    
    private static final RsId RSID = new RsId(12335);
    private static final FrequencyData realPassData = new FrequencyData(RSID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS,null, null,null,null,null,null,null);

    private static final FrequencyData espAllPassData = new FrequencyData(null, null, ESP_ALL_PASS, null, null,null, null,null,null,null,null,null);
    private static final FrequencyData espAllFailData = new FrequencyData(null, null, ESP_ALL_FAIL, null, null,null, null,null,null,null,null,null);
    private static final FrequencyData espAaPassData = new FrequencyData(null, null, null, ESP_AA_PASS, null,null, null,null,null,null,null,null);
    private static final FrequencyData espEaPassData = new FrequencyData(null, null, null, null, ESP_EA_PASS,null, null,null,null,null,null,null);
    private static final FrequencyData dbSnpPassData = new FrequencyData(null, DBSNP_PASS, null, null, null,null, null,null,null,null,null,null);
    private static final FrequencyData rsIdOnlyData = new FrequencyData(RSID, null, null, null, null,null, null,null,null,null,null,null);
    private static final FrequencyData noFreqData = new FrequencyData(null, null, null, null, null,null, null,null,null,null,null,null);
    
    public FrequencyDataTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testGetRsId() {
        FrequencyData instance = realPassData;
        assertThat(instance.getRsId(), equalTo(RSID));
    }

    @Test
    public void testGetDbSnpMaf() {
        FrequencyData instance = realPassData;
        assertThat(instance.getDbSnpMaf(), equalTo(DBSNP_PASS));
    }

    @Test
    public void testGetEspEaMaf() {
        FrequencyData instance = realPassData;
        assertThat(instance.getEspEaMaf(), equalTo(ESP_EA_PASS));
    }

    @Test
    public void testGetEspAaMaf() {
        FrequencyData instance = realPassData;
        assertThat(instance.getEspAaMaf(), equalTo(ESP_AA_PASS));
    }

    @Test
    public void testGetEspAllMaf() {
        FrequencyData instance = realPassData;
        assertThat(instance.getEspAllMaf(), equalTo(ESP_ALL_PASS));
    }

    @Test
    public void testNotRepresentedInDatabase() {
        FrequencyData instance = noFreqData;
        assertThat(instance.representedInDatabase(), is(false));
    }
    
    @Test
    public void testRepresentedInDatabaseRsIdOnly() {
        FrequencyData instance = new FrequencyData(RSID, null, null, null, null,null, null,null,null,null,null,null);
        assertThat(instance.representedInDatabase(), is(true));
    }
    
     @Test
    public void testRepresentedInDatabaseEspAllOnly() {
        FrequencyData instance = espAllFailData;
        assertThat(instance.representedInDatabase(), is(true));
    }

    @Test
    public void testHasDbSnpData() {
        FrequencyData instance = dbSnpPassData;
        assertThat(instance.hasDbSnpData(), is(true));
    }

    @Test
    public void testHasDbSnpRsIdTrue() {
        FrequencyData instance = rsIdOnlyData;
        assertThat(instance.hasDbSnpRsID(), is(true));
    }
    
    @Test
    public void testHasDbSnpRsIdFalse() {
        //When
        FrequencyData instance = noFreqData;
        //Then
        assertThat(instance.hasDbSnpRsID(), is(false));
    }

    @Test
    public void testHasEspDataTrue() {
        FrequencyData instance = espAllPassData;
        assertThat(instance.hasEspData(), is(true));
    }
    
    @Test
    public void testHasEspDataFalse() {
        FrequencyData instance = noFreqData;
        assertThat(instance.hasEspData(), is(false));
    }

    @Test
    public void testGetKnownFrequencies() {
        System.out.println("getKnownFrequencies");
        FrequencyData instance = new FrequencyData(RSID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS,null, null,null,null,null,null,null);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_ALL_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_EA_PASS);
        
        List<Frequency> result = instance.getKnownFrequencies();
        
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetMaxFreqWhenNoData() {
        FrequencyData instance = noFreqData;
        float maxFreq = 0.0F;
        assertThat(instance.getMaxFreq(), equalTo(maxFreq));
    }
    
    @Test
    public void testGetMaxFreqWithData() {
        float maxFreq = 89.5F;
        Frequency maxFrequency = new Frequency(maxFreq);
        FrequencyData instance = new FrequencyData(RSID, DBSNP_PASS, maxFrequency, ESP_AA_PASS, ESP_EA_PASS,null, null,null,null,null,null,null);
        assertThat(instance.getMaxFreq(), equalTo(maxFreq));
    }
}
