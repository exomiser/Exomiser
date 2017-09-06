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
