/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TargetFilterOptionMarshaller extends AbstractOptionMarshaller {

    public static final String KEEP_OFF_TARGET_OPTION = "keep-off-target";

    public TargetFilterOptionMarshaller() {
        option = Option.builder("T")
                .optionalArg(true)
                .type(Boolean.class)
                .argName("true/false")
                .desc("Keep the off-target variants that are normally removed by default. "
                        + "These are defined as intergenic, intronic, upstream, downstream or intronic ncRNA variants. "
                        + "This setting can optionally take a true/false argument. Not including the argument is equivalent to specifying 'true'.")
                .longOpt(KEEP_OFF_TARGET_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //the default should be to remove the off-target variants
        if (values == null) {
            settingsBuilder.keepOffTargetVariants(true);
        } else {
            //but the json/properties file specifies true or false, hence the optionArg
            settingsBuilder.keepOffTargetVariants(Boolean.parseBoolean(values[0]));
        }
    }

}
