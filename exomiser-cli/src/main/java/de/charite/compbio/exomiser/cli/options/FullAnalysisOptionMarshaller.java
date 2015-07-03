/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FullAnalysisOptionMarshaller extends AbstractOptionMarshaller {

    public static final String RUN_FULL_ANALYSIS_OPTION = "full-analysis";

    public FullAnalysisOptionMarshaller() {
        option = OptionBuilder
                .hasArg()
                .withArgName("true/false")
                .withDescription("Run the analysis such that all variants are run through all filters. This will take longer, but give more complete results. Default is false")
                .withLongOpt(RUN_FULL_ANALYSIS_OPTION)
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //default is false
        settingsBuilder.runFullAnalysis(Boolean.parseBoolean(values[0]));
    }

}
