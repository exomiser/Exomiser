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
public class OutputPassOnlyVariantsOptionMarshaller extends AbstractOptionMarshaller {

    public static final String OUTPUT_PASS_VARIANTS_ONLY_OPTION = "output-pass-variants-only";

    public OutputPassOnlyVariantsOptionMarshaller () {
        option = OptionBuilder
                .hasOptionalArg()
                .withType(Boolean.class)
                .withArgName("true/false")
                .withDescription("Only write out PASS variants in TSV and VCF files.")
                .withLongOpt(OUTPUT_PASS_VARIANTS_ONLY_OPTION) 
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        if (values == null) {
            //default is to output all variants, regardless of their filtered status.
            //having this triggered from the command line is the same as saying values[0] == true
            settingsBuilder.outputPassVariantsOnly(true);
        } else {
            //but the json/properties file specify true or false
            settingsBuilder.outputPassVariantsOnly(Boolean.parseBoolean(values[0]));
        }
    }
}
