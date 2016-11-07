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
public class OutFilePrefixOptionMarshaller extends AbstractOptionMarshaller {
    
    public static final String OUT_FILE_PREFIX_OPTION = "out-prefix";

    public OutFilePrefixOptionMarshaller() {
        option = new Option("o", OUT_FILE_PREFIX_OPTION, true, "Out file prefix. Will default to vcf-filename-exomiser-results");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.outputPrefix(values[0]);
    }
      
}
