/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.REMOVE_DBSNP_OPTION;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDbSnpOptionMarshaller extends AbstractOptionMarshaller {

    public FrequencyDbSnpOptionMarshaller() {
        option = new Option(null, REMOVE_DBSNP_OPTION, false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency).  Default: false");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        if (values == null || values.length == 0 || values[0].isEmpty()) {
            //default is false
            //the command line is just a switch
            settingsBuilder.removeDbSnp(true);
        } else {
            //but the json/properties file specify true or false
            settingsBuilder.removeDbSnp(Boolean.parseBoolean(values[0]));
        }
    }

}
