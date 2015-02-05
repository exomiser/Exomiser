/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.GENETIC_INTERVAL_OPTION;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneticIntervalOptionMarshaller extends AbstractOptionMarshaller {

    Logger logger = LoggerFactory.getLogger(GeneticIntervalOptionMarshaller.class);
    
    public GeneticIntervalOptionMarshaller() {
        option = new Option("R", GENETIC_INTERVAL_OPTION, true, "Restrict to region/interval (e.g., chr2:12345-67890)");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        if (values == null || values.length == 0 || values[0].isEmpty()) {
            //use the default builder value
            return;
        }
        settingsBuilder.geneticInterval(GeneticInterval.parseString(values[0]));
    }
    
    
}
