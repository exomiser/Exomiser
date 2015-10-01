/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.apache.commons.cli.Option;
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
        switch (value) {
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
                logger.info("value {} is not one of AR, AD or X - inheritance mode has not been set", value);
                return ModeOfInheritance.UNINITIALIZED;
        }
    }

}
