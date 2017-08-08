/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
