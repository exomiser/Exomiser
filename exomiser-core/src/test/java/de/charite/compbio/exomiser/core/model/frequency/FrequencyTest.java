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

import org.junit.Test;

import java.util.Locale;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyTest {
    
    private Frequency instance;
    
    @Test
    public void testfrequencyOnlyConstructor(){
        float frequency = 1.0f;
        instance = Frequency.valueOf(frequency, UNKNOWN);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(FrequencySource.UNKNOWN));
        System.out.println(instance);
    }
    
    @Test
    public void testfrequencySourceInConstructor(){
        float frequency = 1.0f;
        FrequencySource source = EXAC_NON_FINNISH_EUROPEAN;
        
        instance = Frequency.valueOf(frequency, source);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(source));
        System.out.println(instance);
    }
    
    @Test
    public void testFrequencyIsOverThreshold() {
        float threshold = 2.0f;
        instance = Frequency.valueOf(4.0f, ESP_AFRICAN_AMERICAN);
        
        assertThat(instance.isOverThreshold(threshold), is(true));
    }
    
    @Test
    public void testFrequencyIsNotOverThreshold() {
        float threshold = 2.0f;
        instance = Frequency.valueOf(1.0f, ESP_AFRICAN_AMERICAN);
        
        assertThat(instance.isOverThreshold(threshold), is(false));
    }
    
    @Test
    public void testNotEqualToOtherFrequencyOfDifferentSource() {
        Frequency other = Frequency.valueOf(1.0f, UNKNOWN);
        instance = Frequency.valueOf(1.0f, ESP_AFRICAN_AMERICAN);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFrequencyOfSameSourceAndFrequecy() {
        Frequency other = Frequency.valueOf(1.0f, UNKNOWN);
        instance = Frequency.valueOf(1.0f, UNKNOWN);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testHashCodeEqual() {
        Frequency other = Frequency.valueOf(1.0f, UNKNOWN);
        instance = Frequency.valueOf(1.0f, UNKNOWN);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEqual() {
        Frequency other = Frequency.valueOf(1.0f, UNKNOWN);
        instance = Frequency.valueOf(1.1f, UNKNOWN);
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }
    
    @Test
    public void testToString() {
        float frequency = 1.0f;
        instance = Frequency.valueOf(frequency, UNKNOWN);
        assertThat(instance.toString(), equalTo(String.format(Locale.UK, "Frequency{%s source=UNKNOWN}", frequency)));
    }
}
