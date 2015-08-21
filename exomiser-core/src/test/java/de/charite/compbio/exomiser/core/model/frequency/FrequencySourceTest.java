/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.frequency;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencySourceTest {

    @Test
    public void testValues() {
        FrequencySource[] values = {
            UNKNOWN,
            LOCAL,
            THOUSAND_GENOMES,
            ESP_AFRICAN_AMERICAN,
            ESP_EUROPEAN_AMERICAN,
            ESP_ALL,
            EXAC_AFRICAN_INC_AFRICAN_AMERICAN,
            EXAC_AMERICAN,
            EXAC_EAST_ASIAN,
            EXAC_SOUTH_ASIAN,
            EXAC_FINNISH,
            EXAC_NON_FINNISH_EUROPEAN,
            EXAC_OTHER};

        assertThat(FrequencySource.values(), equalTo(values));
    }

    @Test
    public void testValueOf() {
        assertThat(FrequencySource.valueOf("THOUSAND_GENOMES"), equalTo(THOUSAND_GENOMES));
    }

    @Test
    public void testGetSource() {
        assertThat(LOCAL.getSource(), equalTo("Local"));
    }

}
