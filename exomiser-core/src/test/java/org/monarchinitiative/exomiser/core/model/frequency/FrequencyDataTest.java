/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.model.frequency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;

/**
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDataTest {

    private static final float FREQ_THRESHOLD = 0.1f;
    private static final float PASS_FREQ = FREQ_THRESHOLD - 0.02f;
    private static final float FAIL_FREQ = FREQ_THRESHOLD + 1.0f;

    private static final Frequency ESP_ALL_PASS = Frequency.of(ESP_ALL, PASS_FREQ);
    private static final Frequency ESP_AA_PASS = Frequency.of(ESP_AFRICAN_AMERICAN, PASS_FREQ);
    private static final Frequency ESP_EA_PASS = Frequency.of(ESP_EUROPEAN_AMERICAN, PASS_FREQ);
    private static final Frequency DBSNP_PASS = Frequency.of(THOUSAND_GENOMES, PASS_FREQ);

    private static final String RS_ID = "rs12335";

    private static final FrequencyData FREQUENCY_DATA = FrequencyData.of(RS_ID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS);
    private static final FrequencyData RS_ID_ONLY_DATA = FrequencyData.of(RS_ID);
    private static final FrequencyData EMPTY_DATA = FrequencyData.empty();


    @Test
    public void testEmptyData() {
        assertThat(EMPTY_DATA.getRsId(), equalTo(""));
        assertThat(EMPTY_DATA.getKnownFrequencies().isEmpty(), is(true));
        assertThat(EMPTY_DATA.isRepresentedInDatabase(), is(false));
    }

    @Test
    public void testReplacesNullRsIdWithEmptyValue() {
        assertThat(FrequencyData.of(null, Collections.emptyList()), equalTo(FrequencyData.empty()));
        assertThat(FrequencyData.of(Collections.emptyList()), equalTo(FrequencyData.empty()));
    }

    @Test
    public void testEmptyInputValuesReturnsEmpty() {
        FrequencyData instance = FrequencyData.of();
        assertThat(instance, equalTo(FrequencyData.empty()));
    }

    @Test
    public void testNoRsIdNoFrequencyEqualToEmpty() {
        FrequencyData localFrequency = FrequencyData.of();
        assertThat(localFrequency, equalTo(FrequencyData.empty()));
    }

    @Test
    public void testNoRsIdSpecifiedSingleFrequencyValue() {
        FrequencyData localFrequency = FrequencyData.of(Frequency.of(FrequencySource.LOCAL, 0.001f));
        assertThat(localFrequency.hasKnownFrequency(), is(true));
    }

    @Test
    public void testSingleFrequencyValue() {
        FrequencyData localFrequency = FrequencyData.of(Frequency.of(FrequencySource.LOCAL, 0.001f));
        assertThat(localFrequency.hasKnownFrequency(), is(true));
    }

    @Test
    public void testInputWithNullValues() {
        List<Frequency> listWithNull = new ArrayList<>();
        listWithNull.add(ESP_AA_PASS);
        listWithNull.add(null);
        listWithNull.add(ESP_ALL_PASS);
        listWithNull.add(Frequency.of(FrequencySource.LOCAL, 0.001f));
        assertThrows(NullPointerException.class, () -> FrequencyData.of("", listWithNull));
    }

    @Test
    public void testGetRsId() {
        assertThat(FREQUENCY_DATA.getRsId(), equalTo(RS_ID));
    }

    @Test
    public void testGetNonExistentFrequency() {
        assertThat(EMPTY_DATA.getFrequencyForSource(THOUSAND_GENOMES), equalTo(null));
    }

    @Test
    public void testGetDbSnpMaf() {
        assertThat(FREQUENCY_DATA.getFrequencyForSource(THOUSAND_GENOMES), equalTo(DBSNP_PASS));
    }

    @Test
    public void testGetEspEaMaf() {
        assertThat(FREQUENCY_DATA.getFrequencyForSource(ESP_EUROPEAN_AMERICAN), equalTo(ESP_EA_PASS));
    }

    @Test
    public void testGetEspAaMaf() {
        assertThat(FREQUENCY_DATA.getFrequencyForSource(ESP_AFRICAN_AMERICAN), equalTo(ESP_AA_PASS));
    }

    @Test
    public void testGetEspAllMaf() {
        assertThat(FREQUENCY_DATA.getFrequencyForSource(ESP_ALL), equalTo(ESP_ALL_PASS));
    }

    @Test
    public void testNotRepresentedInDatabase() {
        assertThat(EMPTY_DATA.isRepresentedInDatabase(), is(false));
    }

    @Test
    public void testRepresentedInDatabaseEspAllOnly() {
        FrequencyData instance = FrequencyData.of(ESP_ALL_PASS);
        assertThat(instance.isRepresentedInDatabase(), is(true));
    }

    @Test
    public void testRepresentedInDatabaseRsIdOnly() {
        assertThat(RS_ID_ONLY_DATA.isRepresentedInDatabase(), is(true));
    }

    @Test
    public void testHasDbSnpData() {
        assertThat(FREQUENCY_DATA.hasDbSnpData(), is(true));
    }

    @Test
    public void testHasNoDbSnpData() {
        assertThat(EMPTY_DATA.hasDbSnpData(), is(false));
    }

    @Test
    public void testHasDbSnpRsIdTrue() {
        assertThat(RS_ID_ONLY_DATA.hasDbSnpRsID(), is(true));
    }

    @Test
    public void testHasDbSnpRsIdFalse() {
        assertThat(EMPTY_DATA.hasDbSnpRsID(), is(false));
    }

    @Test
    public void testHasEspDataTrue() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS);
        assertThat(instance.hasEspData(), is(true));
    }

    @Test
    public void testHasEspDataFalseWhenEmpty() {
        assertThat(EMPTY_DATA.hasEspData(), is(false));
    }

    @Test
    public void testHasEspDataIsFalseWhenOnlyNonEspFrequenciesArePresent() {
        FrequencyData instance = FrequencyData.of(RS_ID, Frequency.of(EXAC_FINNISH, PASS_FREQ));
        assertThat(instance.hasEspData(), is(false));
    }

    @Test
    public void testHasEspDataIsTrueWhenOtherNonEspFrequenciesArePresent() {
        FrequencyData instance = FrequencyData.of(RS_ID, Frequency.of(THOUSAND_GENOMES, PASS_FREQ), ESP_AA_PASS, Frequency
                .of(EXAC_FINNISH, PASS_FREQ));
        assertThat(instance.hasEspData(), is(true));
    }

    @Test
    public void testHasExacDataTrue() {
        FrequencyData instance = FrequencyData.of(RS_ID, Frequency.of(EXAC_AFRICAN_INC_AFRICAN_AMERICAN, PASS_FREQ));
        assertThat(instance.hasExacData(), is(true));
    }

    @Test
    public void testHasExacDataFalse() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS);
        assertThat(instance.hasExacData(), is(false));
    }

    @Test
    public void testHasKnownFrequencyFalse() {
        assertThat(EMPTY_DATA.hasKnownFrequency(), is(false));
    }

    @Test
    public void testHasKnownFrequencyTrue() {
        assertThat(FREQUENCY_DATA.hasKnownFrequency(), is(true));
    }

    @Test
    public void testGetKnownFrequenciesNoFrequencyData() {
        assertThat(EMPTY_DATA.getKnownFrequencies(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetKnownFrequencies() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS, ESP_EA_PASS);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_EA_PASS);
        expResult.add(ESP_ALL_PASS);

        assertThat(instance.getKnownFrequencies(), equalTo(expResult));
    }

    @Test
    public void testGetKnownFrequenciesIsImmutable() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS);
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
        assertThat(EMPTY_DATA.getMaxFreq(), equalTo(maxFreq));
    }

    @Test
    public void testGetMaxFreqWithData() {
        float maxFreq = 89.5f;
        Frequency maxFrequency = Frequency.of(UNKNOWN, maxFreq);
        FrequencyData instance = FrequencyData.of(RS_ID, DBSNP_PASS, maxFrequency, ESP_AA_PASS, ESP_EA_PASS);
        assertThat(instance.getMaxFreq(), equalTo(maxFreq));
    }

    @Test
    public void testHasFrequencyOverPercentageValue() {
        float maxFreq = 0.05f;
        Frequency upper = Frequency.of(UNKNOWN, maxFreq);
        Frequency lower = Frequency.of(UK10K, 0.0001f);
        FrequencyData instance = FrequencyData.of(upper, lower);
        assertThat(instance.hasFrequencyOverPercentageValue(maxFreq - 0.01f), is(true));
        assertThat(instance.hasFrequencyOverPercentageValue(maxFreq + 0.01f), is(false));
    }

    @Test
    public void testGetScoreReallyRareVariant() {
        assertThat(EMPTY_DATA.getScore(), equalTo(1f));
    }

    @Test
    public void testGetScoreCommonVariant() {
        float maxFreq = 100.0f;
        Frequency maxFrequency = Frequency.of(THOUSAND_GENOMES, maxFreq);
        FrequencyData instance = FrequencyData.of(RS_ID, maxFrequency);
        assertThat(instance.getScore(), equalTo(0f));
    }

    @Test
    public void testGetScoreRareVariant() {
        float maxFreq = 0.1f;
        Frequency maxFrequency = Frequency.of(UNKNOWN, maxFreq);
        FrequencyData instance = FrequencyData.of(maxFrequency);
        assertThat(instance.getScore(), equalTo(0.9857672f));
    }

}
