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
public class BatchFileOptionMarshaller extends AbstractOptionMarshaller {
    
    private static final String BATCH_FILE_OPTION = "batch-file";
    
    public BatchFileOptionMarshaller() {
        option = Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to batch file. This should contain a list of fully qualified path names for the settings files you wish to process. There should be one file name on each line.")
                .longOpt(BATCH_FILE_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //not direct ExomiserSettings value to set
    }
}
