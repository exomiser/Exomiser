/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.REMOVE_PATHOGENICITY_FILTER_CUTOFF;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityFilterCutOffOptionMarshaller extends AbstractOptionMarshaller {

    public PathogenicityFilterCutOffOptionMarshaller() {
        option = new Option("P", REMOVE_PATHOGENICITY_FILTER_CUTOFF, true, "Keep all variants, regardless of predicted pathogenicity or variant type. Default: false");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        //default is true
        settingsBuilder.removePathFilterCutOff(Boolean.parseBoolean(values[0]));
    }

}
