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

import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class FrequencyTest {

    @Test
    void createNullType() {
        Frequency emptyAnAc = Frequency.of(UNKNOWN, 0, 0, 0);
        assertThat(emptyAnAc.frequency(), is(0f));

        Frequency emptyFreq = Frequency.of(UNKNOWN, 0f);
        assertThat(emptyFreq.frequency(), is(0f));

        Frequency emptyAnAcFreqFactory = Frequency.of(UNKNOWN, 0f, 0 , 0 , 0);
        assertThat(emptyAnAcFreqFactory.frequency(), is(0f));

        Frequency emptyAnAcFreq = new Frequency(UNKNOWN, 0f, 0 , 0 , 0);
        assertThat(emptyAnAcFreq.frequency(), is(0f));
    }

    @Test
    void testRecordConstructorFrequencyCountMismatch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                Frequency.of(UNKNOWN, 1.0f, 2, 100, 0)
        );
        assertThat(exception.getMessage(), equalTo("UNKNOWN frequency does not match given AC/AN! Expected 2/100 = 2.0%, got 1.0%"));
    }

    @Test
    void testRecordConstructorAcGreaterThanAnMismatch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                Frequency.of(UNKNOWN, 0f, 100, 2, 0)
        );
        assertThat(exception.getMessage(), equalTo("UNKNOWN AN must be >= 0, AC must be < AN and HOM must be < AC. Got AC=100, AN=2, hom=0"));
    }

    @Test
    void testRecordConstructorMoreHomsThanAcMismatch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                Frequency.of(UNKNOWN, 0f, 0, 2, 1)
        );
        assertThat(exception.getMessage(), equalTo("UNKNOWN AN must be >= 0, AC must be < AN and HOM must be < AC. Got AC=0, AN=2, hom=1"));
    }

    @Test
    void testFrequencyOnlyConstructor(){
        float frequency = 1.0f;
        Frequency instance = Frequency.of(UNKNOWN, frequency);
        assertThat(instance.frequency(), equalTo(frequency));
        assertThat(instance.source(), equalTo(FrequencySource.UNKNOWN));
        assertThat(instance.ac(), equalTo(0));
        assertThat(instance.an(), equalTo(0));
        assertThat(instance.homs(), equalTo(0));}

    @Test
    void testFrequencyAcAnHomConstructor(){
        Frequency instance = Frequency.of(UNKNOWN, 2, 200, 1);
        assertThat(instance.frequency(), equalTo(1.0f));
        assertThat(instance.source(), equalTo(FrequencySource.UNKNOWN));
        assertThat(instance.ac(), equalTo(2));
        assertThat(instance.an(), equalTo(200));
        assertThat(instance.homs(), equalTo(1));
    }

//    @Test
//    void testFrequencyAcAnHomConstructorAcGreaterThanAn() {
//        Exception exception = assertThrows(IllegalArgumentException.class, () ->
//                Frequency.of(UNKNOWN, 100, 2, 0)
//        );
//        assertThat(exception.getMessage(), equalTo("UNKNOWN AN must be >= 0, AC must be < AN and HOM must be < AC. Got AC=100, AN=2, hom=0"));
//    }
//
//    @Test
//    void testFrequencyAcAnHomConstructorHomsGreaterThanAn() {
//        Exception exception = assertThrows(IllegalArgumentException.class, () ->
//                Frequency.of(UNKNOWN, 2, 3, 4)
//        );
//        assertThat(exception.getMessage(), equalTo("UNKNOWN AN must be >= 0, AC must be < AN and HOM must be < AC. Got AC=2, AN=3, hom=4"));
//    }

    @Test
    void testFrequencySourceInConstructor(){
        float frequency = 1.0f;
        FrequencySource source = EXAC_NON_FINNISH_EUROPEAN;

        Frequency instance = Frequency.of(source, frequency);
        assertThat(instance.frequency(), equalTo(frequency));
        assertThat(instance.source(), equalTo(source));
    }
    
    @Test
    void testFrequencyIsOverThreshold() {
        float threshold = 2.0f;
        Frequency instance = Frequency.of(ESP_AA, 4.0f);
        
        assertThat(instance.isOverThreshold(threshold), is(true));
    }
    
    @Test
    void testFrequencyIsNotOverThreshold() {
        float threshold = 2.0f;
        Frequency instance = Frequency.of(ESP_AA, 1.0f);
        
        assertThat(instance.isOverThreshold(threshold), is(false));
    }
    
    @Test
    void testNotEqualToOtherFrequencyOfDifferentSource() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(ESP_AA, 1.0f);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    void testEqualToOtherFrequencyOfSameSourceAndFrequecy() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.0f);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    void testHashCodeEqual() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.0f);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test
    void testHashCodeNotEqual() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.1f);
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }

    @Test
    void testToString() {
        float frequency = 1.0f;
        Frequency instance = Frequency.of(UNKNOWN, frequency);
        assertThat(instance.toString(), equalTo(String.format(Locale.UK, "Frequency{UNKNOWN=%s}", frequency)));

        assertThat(Frequency.of(GNOMAD_E_AFR, 100, 2000, 1).toString(), equalTo("Frequency{GNOMAD_E_AFR=5.0(100|2000|1)}"));
    }
}
