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
public class VcfFileOptionMarshaller extends AbstractOptionMarshaller  {
            
    public static final String VCF_OPTION = "vcf";

    public VcfFileOptionMarshaller() {
        option = Option.builder("v")
                .argName("file")
                .hasArg()                
                .desc("Path to VCF file with mutations to be analyzed. Can be either for an individual or a family.")
                .longOpt(VCF_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        if (values[0].isEmpty()) {
            settingsBuilder.vcfFilePath(null);
        } else {
            settingsBuilder.vcfFilePath(Paths.get(values[0]));
        }
    }    

}
