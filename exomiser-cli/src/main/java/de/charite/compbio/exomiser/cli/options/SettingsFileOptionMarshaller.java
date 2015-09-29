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
public class SettingsFileOptionMarshaller extends AbstractOptionMarshaller {
    
    public static final String SETTINGS_FILE_OPTION = "settings-file";
    
    public SettingsFileOptionMarshaller() {
        option = OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription("Path to settings file. Any settings specified in the file will be overidden by parameters added on the command-line.")
                .withLongOpt(SETTINGS_FILE_OPTION)
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //no direct ExomiserSettings value to set
    }
    
}
