/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeOptionMarshaller.class);

    public static final String MODE_OF_INHERITANCE_OPTION = "inheritance-mode";

    public InheritanceModeOptionMarshaller() {
        option = new Option("I", MODE_OF_INHERITANCE_OPTION, true, "Filter variants for inheritance pattern (AR, AD, X)");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.modeOfInheritance(parseInheritanceMode(values[0]));
    }

    private ModeOfInheritance parseInheritanceMode(String value) {
        switch (value.toUpperCase()) {
            case "AR":
            case "AUTOSOMAL_RECESSIVE":
                return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case "AD":
            case "AUTOSOMAL_DOMINANT":
                return ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case "X":
            case "X_RECESSIVE":
                return ModeOfInheritance.X_RECESSIVE;
            default:
                String message = "'" + value + "' is not a valid mode of inheritance. Use one of AUTOSOMAL_RECESSIVE (AR), AUTOSOMAL_DOMINANT (AD) or X_RECESSIVE (X).";
                logger.error(message);
                throw new CommandLineParseError(message);
        }
    }

}
