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
public class SettingsFileOptionMarshaller extends AbstractOptionMarshaller {
    
    public static final String SETTINGS_FILE_OPTION = "settings-file";
    
    public SettingsFileOptionMarshaller() {
        option = Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to settings file. Any settings specified in the file will be overidden by parameters added on the command-line.")
                .longOpt(SETTINGS_FILE_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //no direct ExomiserSettings value to set
    }
    
}
