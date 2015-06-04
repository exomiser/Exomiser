/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import static de.charite.compbio.exomiser.core.ExomiserSettings.OUT_FILE_PREFIX_OPTION;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutFilePrefixOptionMarshaller extends AbstractOptionMarshaller {
    
    public OutFilePrefixOptionMarshaller() {
        option = new Option("o", OUT_FILE_PREFIX_OPTION, true, "Out file prefix. Will default to vcf-filename-exomiser-results");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.outputPrefix(values[0]);
    }
      
}
