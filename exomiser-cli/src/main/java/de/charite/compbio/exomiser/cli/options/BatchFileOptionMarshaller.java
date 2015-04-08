/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileOptionMarshaller  extends AbstractOptionMarshaller {
    
    private static final String BATCH_FILE = "batch-file";
    
    public BatchFileOptionMarshaller() {
        option = OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription("Path to batch file. This should contain a list of fully qualified path names for the settings files you wish to process. There should be one file name on each line.")
                .withLongOpt(BATCH_FILE)
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //not direct ExomiserSettings value to set
    }
}
