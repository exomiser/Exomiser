/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.Settings.SettingsBuilder;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityFilterCutOffOptionMarshaller extends AbstractOptionMarshaller {

    public static final String KEEP_NON_PATHOGENIC_VARIANTS_OPTION = "keep-non-pathogenic";

    public PathogenicityFilterCutOffOptionMarshaller() {
        option = OptionBuilder
                .hasOptionalArg()
                .withType(Boolean.class)
                .withArgName("true/false")
                .withDescription("Keep the predicted non-pathogenic variants that are normally removed by default. "
                        + "These are defined as syonymous, intergenic, intronic, upstream, downstream or intronic ncRNA variants. "
                        + "This setting can optionally take a true/false argument. Not including the argument is equivalent to specifying 'true'.")
                .withLongOpt(KEEP_NON_PATHOGENIC_VARIANTS_OPTION) 
                .create("P");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        if (values == null) {
            //default is to remove the non-pathogenic variants, so this should be false
            settingsBuilder.removePathFilterCutOff(true);
        } else {
            //but the json/properties file specifies true or false, hence the optionArg
            settingsBuilder.removePathFilterCutOff(Boolean.parseBoolean(values[0]));
        }
    }

}
