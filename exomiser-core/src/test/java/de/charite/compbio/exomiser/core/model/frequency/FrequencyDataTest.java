/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model.frequency;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

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
    
    private static final Frequency ESP_ALL_PASS = Frequency.valueOf(PASS_FREQ, ESP_ALL);
    private static final Frequency ESP_AA_PASS = Frequency.valueOf(PASS_FREQ, ESP_AFRICAN_AMERICAN);
    private static final Frequency ESP_EA_PASS = Frequency.valueOf(PASS_FREQ, ESP_EUROPEAN_AMERICAN);
    private static final Frequency DBSNP_PASS = Frequency.valueOf(PASS_FREQ, THOUSAND_GENOMES);
    
    private static final RsId RSID = RsId.valueOf(12335);
       
    @Before
    public void setUp() {
         instance = new FrequencyData(RSID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS);
         rsIdOnlyData = new FrequencyData(RSID);
         noFreqData = new FrequencyData(null);
    }

    @Test
    public void testEmptyData() {
        instance = FrequencyData.EMPTY_DATA;
        assertThat(instance.getRsId(), nullValue());
        assertThat(instance.getKnownFrequencies().isEmpty(), is(true));
        assertThat(instance.isRepresentedInDatabase(), is(false));
        System.out.println(instance);
    }

    @Test
    public void testGetRsId() {
        assertThat(instance.getRsId(), equalTo(RSID));
    }

    @Test
    public void testGetDbSnpMaf() {
        assertThat(instance.getFrequencyForSource(THOUSAND_GENOMES), equalTo(DBSNP_PASS));
    }

    @Test
    public void testGetEspEaMaf() {
        assertThat(instance.getFrequencyForSource(ESP_EUROPEAN_AMERICAN), equalTo(ESP_EA_PASS));
    }

    @Test
    public void testGetEspAaMaf() {
        assertThat(instance.getFrequencyForSource(ESP_AFRICAN_AMERICAN), equalTo(ESP_AA_PASS));
    }

    @Test
    public void testGetEspAllMaf() {
        assertThat(instance.getFrequencyForSource(ESP_ALL), equalTo(ESP_ALL_PASS));
    }

    @Test
    public void testNotRepresentedInDatabase() {
        assertThat(noFreqData.isRepresentedInDatabase(), is(false));
    }
    
     @Test
    public void testRepresentedInDatabaseEspAllOnly() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS);
        assertThat(instance.isRepresentedInDatabase(), is(true));
    }

    @Test
    public void testHasDbSnpData() {
        assertThat(instance.hasDbSnpData(), is(true));
    }

    @Test
    public void testRepresentedInDatabaseRsIdOnly() {
        assertThat(rsIdOnlyData.isRepresentedInDatabase(), is(true));
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
    public void testHasEspDataFalseWhenEmpty() {
        instance = noFreqData;
        assertThat(instance.hasEspData(), is(false));
    }

    @Test
    public void testHasEspDataIsFalseWhenOnlyNonEspFrequenciesArePresent() {
        instance = new FrequencyData(RSID, Frequency.valueOf(PASS_FREQ, EXAC_FINNISH));
        assertThat(instance.hasEspData(), is(false));
    }

    @Test
    public void testHasEspDataIsTrueWhenOtherNonEspFrequenciesArePresent() {
        instance = new FrequencyData(RSID, Frequency.valueOf(PASS_FREQ, THOUSAND_GENOMES), ESP_AA_PASS, Frequency.valueOf(PASS_FREQ, EXAC_FINNISH));
        assertThat(instance.hasEspData(), is(true));
    }

    @Test
    public void testHasExacDataTrue() {
        instance = new FrequencyData(RSID, Frequency.valueOf(PASS_FREQ, EXAC_AFRICAN_INC_AFRICAN_AMERICAN));
        assertThat(instance.hasExacData(), is(true));
    }
    
    @Test
    public void testHasExacDataFalse() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS);
        assertThat(instance.hasExacData(), is(false));
    }

    @Test
    public void testGetKnownFrequencies_noFrequencyData() {
        instance = FrequencyData.EMPTY_DATA;
        
        List<Frequency> result = instance.getKnownFrequencies();
        
        assertThat(result, equalTo(new ArrayList<>()));
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
    public void testGetKnownFrequencies_isImmutable() {
        instance = new FrequencyData(RSID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_ALL_PASS);
        
        //try and add another score to the instance post-construction
        instance.getKnownFrequencies().add(ESP_EA_PASS);
                
        assertThat(instance.getKnownFrequencies(), equalTo(expResult));
    }

    @Test
    public void testGetMaxFreqWhenNoData() {
        float maxFreq = 0.0f;
        assertThat(noFreqData.getMaxFreq(), equalTo(maxFreq));
    }
    
    @Test
    public void testGetMaxFreqWithData() {
        float maxFreq = 89.5f;
        Frequency maxFrequency = Frequency.valueOf(maxFreq, UNKNOWN);
        instance = new FrequencyData(RSID, DBSNP_PASS, maxFrequency, ESP_AA_PASS, ESP_EA_PASS);
        assertThat(instance.getMaxFreq(), equalTo(maxFreq));
    }

    @Test
    public void testGetScore_reallyRareVariant() {
        assertThat(noFreqData.getScore(), equalTo(1f));
    }

    @Test
    public void testGetScore_commonVariant() {
        float maxFreq = 100.0f;
        Frequency maxFrequency = Frequency.valueOf(maxFreq, THOUSAND_GENOMES);
        instance = new FrequencyData(RSID, maxFrequency);
        assertThat(instance.getScore(), equalTo(0f));
    }

    @Test
    public void testGetScore_rareVariant() {
        float maxFreq = 0.1f;
        Frequency maxFrequency = Frequency.valueOf(maxFreq, UNKNOWN);
        instance = new FrequencyData(null, maxFrequency);
        assertThat(instance.getScore(), equalTo(0.8504372f));
    }
}
