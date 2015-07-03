/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NumGenesOptionMarshaller extends AbstractOptionMarshaller {

    public static final String NUM_GENES_OPTION = "num-genes";

    public NumGenesOptionMarshaller() {
        option = new Option(null, NUM_GENES_OPTION, true, "Number of genes to show in output");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.numberOfGenesToShow(Integer.parseInt(values[0]));
    }
    
}
