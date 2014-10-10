/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutFileOptionMarshaller extends AbstractOptionMarshaller {
    
    public OutFileOptionMarshaller() {
        option = new Option("o", ExomiserSettings.OUT_FILE_OPTION, true, "name of out file. Will default to vcf-filename-exomiser-results.html");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.outFileName(values[0]);
    }
      
}
