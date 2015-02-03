/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.KEEP_OFF_TARGET_OPTION;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterOptionMarshaller extends AbstractOptionMarshaller {

    public TargetFilterOptionMarshaller() {
        option = OptionBuilder
                .hasOptionalArg()
                .withType(Boolean.class)
                .withArgName("true/false")
                .withDescription("Keep off-target variants. These are defined as intergenic, intronic, upstream, downstream, synonymous or intronic ncRNA variants.")
                .withLongOpt(KEEP_OFF_TARGET_OPTION) 
                .create("T");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        //the default should be to remove the off-target variants
        if (values == null) {
            settingsBuilder.keepOffTargetVariants(true);
        } else {
            //but the json/properties file specifies true or false, hence the optionArg
            settingsBuilder.keepOffTargetVariants(Boolean.parseBoolean(values[0]));
        }
    }

}
