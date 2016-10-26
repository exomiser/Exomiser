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
public class FullAnalysisOptionMarshaller extends AbstractOptionMarshaller {

    public static final String RUN_FULL_ANALYSIS_OPTION = "full-analysis";

    public FullAnalysisOptionMarshaller() {
        option = Option.builder()
                .hasArg()
                .argName("true/false")
                .desc("Run the analysis such that all variants are run through all filters. This will take longer, but give more complete results. Default is false")
                .longOpt(RUN_FULL_ANALYSIS_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        //default is false
        settingsBuilder.runFullAnalysis(Boolean.parseBoolean(values[0]));
    }

}
