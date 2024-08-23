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

import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProtoFormatter;

import java.util.*;

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
    private static final Frequency ESP_AA_PASS = Frequency.of(ESP_AA, PASS_FREQ);
    private static final Frequency ESP_EA_PASS = Frequency.of(ESP_EA, PASS_FREQ);
    private static final Frequency DBSNP_PASS = Frequency.of(THOUSAND_GENOMES, PASS_FREQ);

    private static final String RS_ID = "rs12335";

    private static final FrequencyData FREQUENCY_DATA = FrequencyData.of(RS_ID, DBSNP_PASS, ESP_ALL_PASS, ESP_AA_PASS, ESP_EA_PASS);
    private static final FrequencyData RS_ID_ONLY_DATA = FrequencyData.of(RS_ID);
    private static final FrequencyData EMPTY_DATA = FrequencyData.empty();


    @Test
    public void testEmptyData() {
        assertThat(EMPTY_DATA.getRsId(), equalTo(""));
        assertThat(EMPTY_DATA.frequencies().isEmpty(), is(true));
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
        assertThat(EMPTY_DATA.frequency(THOUSAND_GENOMES), equalTo(null));
    }

    @Test
    public void testGetDbSnpMaf() {
        assertThat(FREQUENCY_DATA.frequency(THOUSAND_GENOMES), equalTo(DBSNP_PASS));
    }

    @Test
    public void testGetEspEaMaf() {
        assertThat(FREQUENCY_DATA.frequency(ESP_EA), equalTo(ESP_EA_PASS));
    }

    @Test
    public void testGetEspAaMaf() {
        assertThat(FREQUENCY_DATA.frequency(ESP_AA), equalTo(ESP_AA_PASS));
    }

    @Test
    public void testGetEspAllMaf() {
        assertThat(FREQUENCY_DATA.frequency(ESP_ALL), equalTo(ESP_ALL_PASS));
    }

    @Test
    public void testGetFrequencyForUnavailableSource() {
        assertThat(FREQUENCY_DATA.frequency(GNOMAD_E_NFE), equalTo(null));
    }

    @Test
    public void testGetFrequencyForUnavailableSourceBefore() {
        assertThat(FREQUENCY_DATA.frequency(UNKNOWN), equalTo(null));
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
        assertThat(EMPTY_DATA.frequencies(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetKnownFrequencies() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS, ESP_EA_PASS);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_EA_PASS);
        expResult.add(ESP_ALL_PASS);

        assertThat(instance.frequencies(), equalTo(expResult));
    }

    @Test
    public void testGetKnownFrequenciesIsImmutable() {
        FrequencyData instance = FrequencyData.of(RS_ID, ESP_ALL_PASS, DBSNP_PASS, ESP_AA_PASS);
        List<Frequency> expResult = new ArrayList<>();
        expResult.add(DBSNP_PASS);
        expResult.add(ESP_AA_PASS);
        expResult.add(ESP_ALL_PASS);

        //try and add another score to the instance post-construction
        assertThrows(UnsupportedOperationException.class, () -> instance.frequencies().add(ESP_EA_PASS));

        assertThat(instance.frequencies(), equalTo(expResult));
    }

    @Test
    public void testGetMaxFreqWhenNoData() {
        assertThat(EMPTY_DATA.maxFreq(), equalTo(0.0f));
    }

    @Test
    public void testGetMaxFreqWithData() {
        float maxFreq = 89.5f;
        Frequency maxFrequency = Frequency.of(UNKNOWN, maxFreq);
        FrequencyData instance = FrequencyData.of(RS_ID, DBSNP_PASS, maxFrequency, ESP_AA_PASS, ESP_EA_PASS);
        assertThat(instance.maxFreq(), equalTo(maxFreq));
    }

    @Test
    public void testGetMaxFrequencyWhenNoData() {
        assertThat(EMPTY_DATA.maxFrequency(), equalTo(null));
    }

    @Test
    public void testGetMaxFrequencyWithData() {
        Frequency maxFrequency = Frequency.of(GNOMAD_E_OTH, 89, 100, 5);
        Frequency minFrequency = Frequency.of(GNOMAD_G_AFR, 0.0002f);
        Frequency midFrequency = Frequency.of(GNOMAD_E_AMR, 25.5f);
        FrequencyData instance = FrequencyData.of(RS_ID, minFrequency, maxFrequency, midFrequency);
        assertThat(instance.maxFrequency(), equalTo(maxFrequency));
    }

    @Test
    public void testGetMaxFrequencyWhenZeroData() {
        Frequency maxFrequency = Frequency.of(GNOMAD_E_AFR, 0, 1000, 0);
        Frequency minFrequency = Frequency.of(TOPMED, 0f);
        FrequencyData instance = FrequencyData.of("rs545662810", minFrequency, maxFrequency);

        assertThat(instance.maxFrequency(), equalTo(null));
        assertThat(instance.maxFreq(), equalTo(0f));
        assertThat(instance.frequencies(), equalTo(List.of(minFrequency, maxFrequency)));
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
        assertThat(EMPTY_DATA.frequencyScore(), equalTo(1f));
    }

    @Test
    public void testGetScoreCommonVariant() {
        float maxFreq = 100.0f;
        Frequency maxFrequency = Frequency.of(THOUSAND_GENOMES, maxFreq);
        FrequencyData instance = FrequencyData.of(RS_ID, maxFrequency);
        assertThat(instance.frequencyScore(), equalTo(0f));
    }

    @Test
    public void testGetScoreRareVariant() {
        float maxFreq = 0.1f;
        Frequency maxFrequency = Frequency.of(UNKNOWN, maxFreq);
        FrequencyData instance = FrequencyData.of(maxFrequency);
        assertThat(instance.frequencyScore(), equalTo(0.9857672f));
    }

    @Test
    void testBuilderEquivalentToStaticConstructor() {
        FrequencyData actual = FrequencyData.builder()
                .rsId("rs12345")
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .build();

        FrequencyData expected = FrequencyData.of("rs12345", Frequency.of(GNOMAD_E_AFR, 0.0005f), Frequency.of(GNOMAD_E_AMR, 0.0002f));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void testBuilder() {
        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .build();
        assertThat(instance.frequencies(), equalTo(List.of(Frequency.of(GNOMAD_E_AFR, 0.0005f), Frequency.of(GNOMAD_E_AMR, 0.0002f))));
    }

    @Test
    void testBuilderEmpty() {
        FrequencyData instance = FrequencyData.builder()
                .build();
        assertThat(instance, equalTo(FrequencyData.empty()));
    }

    @Test
    void testBuilderIsEmpty() {
        FrequencyData instance = FrequencyData.builder()
                .build();
        assertThat(instance.isEmpty(), equalTo(true));
    }

    @Test
    void testToBuilder() {
        FrequencyData frequencyData = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .build();

        FrequencyData instance = frequencyData.toBuilder().build();

        assertThat(instance, equalTo(frequencyData));
    }

    @Test
    void testBuilderRetainSourcesEmpty() {
        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .filterSources(Set.of())
                .build();
        assertThat(instance, equalTo(FrequencyData.empty()));
    }

    @Test
    void testBuilderRetainSources() {
        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .filterSources(Set.of(GNOMAD_E_AMR))
                .build();

        FrequencyData expected = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .build();

        assertThat(instance, equalTo(expected));
    }

    @Test
    void testBuilderRetainSourcesAllRemoved() {
        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .filterSources(Set.of(GNOMAD_E_SAS))
                .build();
        assertThat(instance, equalTo(FrequencyData.empty()));
    }

    @Test
    void testBuilderMergeEmpty() {
        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .mergeFrequencyData(FrequencyData.empty())
                .build();

        FrequencyData expected = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .build();

        assertThat(instance, equalTo(expected));
    }

    @Test
    void testBuilderMergeNewFrequencyIntoEmpty() {
        FrequencyData toMerge = FrequencyData.builder()
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .build();

        FrequencyData instance = FrequencyData.builder()
                .mergeFrequencyData(toMerge)
                .build();

        FrequencyData expected = FrequencyData.builder()
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .build();

        assertThat(instance, equalTo(expected));
    }

    @Test
    void testBuilderMergeNewFrequency() {
        FrequencyData toMerge = FrequencyData.builder()
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .addFrequency(GNOMAD_G_AMI, 1, 200, 1)
                .build();

        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .mergeFrequencyData(toMerge)
                .build();

        FrequencyData expected = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .addFrequency(GNOMAD_E_AFR, 0.0005f)
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .addFrequency(GNOMAD_G_AMI, 1, 200, 1)
                .build();

        assertThat(instance, equalTo(expected));
    }

    @Test
    void testBuilderMergeSameFrequency() {
        FrequencyData toMerge = FrequencyData.builder()
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .build();

        FrequencyData instance = FrequencyData.builder()
                .addFrequency(GNOMAD_E_EAS, 0.0005f)
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .mergeFrequencyData(toMerge)
                .build();

        FrequencyData expected = FrequencyData.builder()
                .addFrequency(GNOMAD_E_AMR, 0.0002f)
                .addFrequency(GNOMAD_E_EAS, 0.0001f)
                .build();

        assertThat(instance, equalTo(expected));
    }

    @Test
    void floatArray() {
        var data = new float[4];
        var ac = 110;
        var an = 10000;
        var hom = 10;
        var af = Frequency.percentageFrequency(ac, an);

        data[0] = (float) ac;
        data[1] = (float) an;
        data[2] = (float) hom;
        data[3] = (float) af;

        assertThat(Frequency.percentageFrequency((int) data[0], (int) data[1]), equalTo((float) data[3]));
    }

}
