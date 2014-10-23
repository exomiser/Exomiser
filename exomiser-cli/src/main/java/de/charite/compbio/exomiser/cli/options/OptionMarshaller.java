/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OptionMarshaller {

    public String getCommandLineParameter();
    
    public Option getOption();
    
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder);
}
