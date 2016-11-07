/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;

import java.nio.file.Paths;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedFileOptionMarshaller extends AbstractOptionMarshaller {

    public static final String PED_OPTION = "ped";

    public PedFileOptionMarshaller() {
        option = Option.builder("p")
                .argName("file")
                .hasArg()
                .desc("Path to pedigree (ped) file. Required if the vcf file is for a family.")
                .longOpt(PED_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.pedFilePath(Paths.get(values[0]));
    }
    
}
