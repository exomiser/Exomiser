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

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutputPassOnlyVariantsOptionMarshaller extends AbstractOptionMarshaller {

    public static final String OUTPUT_PASS_VARIANTS_ONLY_OPTION = "output-pass-variants-only";

    public OutputPassOnlyVariantsOptionMarshaller () {
        option = Option.builder()
                .optionalArg(true)
                .type(Boolean.class)
                .argName("true/false")
                .desc("Only write out PASS variants in TSV and VCF files.")
                .longOpt(OUTPUT_PASS_VARIANTS_ONLY_OPTION)
                .build();
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
