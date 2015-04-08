/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.frequency;

import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import java.util.Locale;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyTest {
    
    private Frequency instance;
    
    @Test
    public void testfrequencyOnlyConstructor(){
        float frequency = 1.0f;
        instance = new Frequency(frequency);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(FrequencySource.UNKNOWN));
        System.out.println(instance);
    }
    
    @Test
    public void testfrequencySourceInConstructor(){
        float frequency = 1.0f;
        FrequencySource source = EXAC_NON_FINISH_EUROPEAN;
        
        instance = new Frequency(frequency, source);
        assertThat(instance.getFrequency(), equalTo(frequency));
        assertThat(instance.getSource(), equalTo(source));
        System.out.println(instance);
    }
    
    @Test
    public void testFrequencyIsOverThreshold() {
        float threshold = 2.0f;
        instance = new Frequency(4.0f, ESP_AFRICAN_AMERICAN);
        
        assertThat(instance.isOverThreshold(threshold), is(true));
    }
    
    @Test
    public void testFrequencyIsNotOverThreshold() {
        float threshold = 2.0f;
        instance = new Frequency(1.0f, ESP_AFRICAN_AMERICAN);
        
        assertThat(instance.isOverThreshold(threshold), is(false));
    }
    
    @Test
    public void testNotEqualToOtherFrequencyOfDifferentSource() {
        Frequency other = new Frequency(1.0f, UNKNOWN);
        instance = new Frequency(1.0f, ESP_AFRICAN_AMERICAN);
        assertThat(instance, not(equalTo(other)));
    }
    
    @Test
    public void testEqualToOtherFrequencyOfSameSourceAndFrequecy() {
        Frequency other = new Frequency(1.0f, UNKNOWN);
        instance = new Frequency(1.0f, UNKNOWN);
        assertThat(instance, equalTo(other));
    }
    
    @Test
    public void testHashCodeEqual() {
        Frequency other = new Frequency(1.0f, UNKNOWN);
        instance = new Frequency(1.0f, UNKNOWN);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test
    public void testHashCodeNotEqual() {
        Frequency other = new Frequency(1.0f, UNKNOWN);
        instance = new Frequency(1.1f, UNKNOWN);
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }
    
    @Test
    public void testToString() {
        float frequency = 1.0f;
        instance = new Frequency(frequency, UNKNOWN);
        assertThat(instance.toString(), equalTo(String.format(Locale.UK, "Frequency{%s source=UNKNOWN}", frequency)));
    }
}
