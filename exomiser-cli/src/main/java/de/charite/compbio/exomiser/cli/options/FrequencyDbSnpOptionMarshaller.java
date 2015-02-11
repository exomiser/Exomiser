/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.REMOVE_DBSNP_OPTION;
import static de.charite.compbio.exomiser.core.ExomiserSettings.REMOVE_PATHOGENICITY_FILTER_CUTOFF;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyDbSnpOptionMarshaller extends AbstractOptionMarshaller {

    public FrequencyDbSnpOptionMarshaller() {
        option = OptionBuilder
                .hasOptionalArg()
                .withType(Boolean.class)
                .withArgName("true/false")
                .withDescription("Filter out all variants with an entry in dbSNP/ESP (regardless of frequency).")
                .withLongOpt(REMOVE_DBSNP_OPTION) 
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        if (values == null) {
            //default is not to remove variants with a DbSNP rsId
            //having this triggered from the command line is the same as saying values[0] == true
            settingsBuilder.removeDbSnp(true);
        } else {
            //but the json/properties file specify true or false
            settingsBuilder.removeDbSnp(Boolean.parseBoolean(values[0]));
        }
    }

}
