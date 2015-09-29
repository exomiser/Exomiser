/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.Settings.SettingsBuilder;
import java.nio.file.Paths;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfFileOptionMarshaller extends AbstractOptionMarshaller  {
            
    public static final String VCF_OPTION = "vcf";

    public VcfFileOptionMarshaller() {
        option = OptionBuilder
                .withArgName("file")
                .hasArg()                
                .withDescription("Path to VCF file with mutations to be analyzed. Can be either for an individual or a family.")
                .withLongOpt(VCF_OPTION)
                .create("v");
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
