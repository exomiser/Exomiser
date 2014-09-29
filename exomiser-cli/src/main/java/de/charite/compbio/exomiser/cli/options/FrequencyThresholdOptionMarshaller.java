/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import static de.charite.compbio.exomiser.core.model.ExomiserSettings.MAX_FREQ_OPTION;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyThresholdOptionMarshaller extends AbstractOptionMarshaller {

    public FrequencyThresholdOptionMarshaller() {
        option = new Option("F", MAX_FREQ_OPTION, true, "Maximum frequency threshold for variants to be retained. e.g. 100.00 will retain all variants. Default: 100.00");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        settingsBuilder.maximumFrequency(Float.parseFloat(values[0]));
    }
    
}
