/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyTest {

    @Test
    public void testfrequencyOnlyConstructor(){
        float frequency = 1.0f;
        Frequency instance = Frequency.of(UNKNOWN, frequency);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(FrequencySource.UNKNOWN));
        System.out.println(instance);
    }
    
    @Test
    public void testfrequencySourceInConstructor(){
        float frequency = 1.0f;
        FrequencySource source = EXAC_NON_FINNISH_EUROPEAN;

        Frequency instance = Frequency.of(source, frequency);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(source));
        System.out.println(instance);
    }
    
    @Test
    public void testFrequencyIsOverThreshold() {
        float threshold = 2.0f;
        Frequency instance = Frequency.of(ESP_AFRICAN_AMERICAN, 4.0f);
        
        assertThat(instance.isOverThreshold(threshold), is(true));
    }
    
    @Test
    public void testFrequencyIsNotOverThreshold() {
        float threshold = 2.0f;
        Frequency instance = Frequency.of(ESP_AFRICAN_AMERICAN, 1.0f);
        
        assertThat(instance.isOverThreshold(threshold), is(false));
    }
    
    @Test
    public void testNotEqualToOtherFrequencyOfDifferentSource() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(ESP_AFRICAN_AMERICAN, 1.0f);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFrequencyOfSameSourceAndFrequecy() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.0f);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testHashCodeEqual() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.0f);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEqual() {
        Frequency other = Frequency.of(UNKNOWN, 1.0f);
        Frequency instance = Frequency.of(UNKNOWN, 1.1f);
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }
    
    @Test
    public void testToString() {
        float frequency = 1.0f;
        Frequency instance = Frequency.of(UNKNOWN, frequency);
        assertThat(instance.toString(), equalTo(String.format(Locale.UK, "Frequency{UNKNOWN=%s}", frequency)));
    }
}
