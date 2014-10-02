/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import static de.charite.compbio.exomiser.core.model.ExomiserSettings.REMOVE_OFF_TARGET_OPTION;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterOptionMarshaller extends AbstractOptionMarshaller {

    public TargetFilterOptionMarshaller() {
        option = new Option("T", REMOVE_OFF_TARGET_OPTION, false, "Keep off-target variants. These are defined as intergenic, intronic, upstream, downstream, synonymous or intronic ncRNA variants. Default: true");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        //default is true
        if (values == null || values.length == 0 || values[0].isEmpty()) {
            //the command line is just a switch
            settingsBuilder.removeOffTargetVariants(false);
        } else {
            //but the json/properties file specify true or false
            settingsBuilder.removeOffTargetVariants(Boolean.parseBoolean(values[0]));
        }
    }

}
