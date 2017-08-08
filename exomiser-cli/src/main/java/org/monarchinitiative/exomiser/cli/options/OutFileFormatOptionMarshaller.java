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
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutFileFormatOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(OutFileFormatOptionMarshaller.class);

    public static final String OUT_FILE_FORMAT_OPTION = "out-format";

    public OutFileFormatOptionMarshaller() {
        option = Option.builder("f")
                .hasArgs()
                .argName("type")
                .type(OutputFormat.class)
                .valueSeparator(',')
                .desc("Comma separated list of format options: HTML, VCF, TAB-GENE or TAB-VARIANT,. Defaults to HTML if not specified. e.g. --out-format=TAB-VARIANT or --out-format=TAB-GENE,TAB-VARIANT,HTML,VCF")
                .longOpt(OUT_FILE_FORMAT_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.outputFormats(parseOutputFormat(values));
    }

    private Set<OutputFormat> parseOutputFormat(String[] values) {
        List<OutputFormat> outputFormats = new ArrayList<>();
        logger.debug("Parsing output options: {}", values);

        for (String outputFormatString : values) {
            switch (outputFormatString.trim()) {
                case "HTML":
                    outputFormats.add(OutputFormat.HTML);
                    break;
                case "TSV_GENE":
                case "TAB-GENE":
                case "TSV-GENE":
                    outputFormats.add(OutputFormat.TSV_GENE);
                    break;
                case "TSV_VARIANT":
                case "TAB-VARIANT":
                case "TSV-VARIANT":
                    outputFormats.add(OutputFormat.TSV_VARIANT);
                    break;
                case "VCF":
                    outputFormats.add(OutputFormat.VCF);
                    break;
                case "PHENOGRID":
                    outputFormats.add(OutputFormat.PHENOGRID);
                    break;
                default:
                    logger.info("{} is not a recognised output format. Please choose one or more of HTML, TAB-GENE, TAB-VARIANT, VCF - defaulting to HTML", outputFormatString);
                    outputFormats.add(OutputFormat.HTML);
                    break;
            }
        }
        logger.debug("Setting output formats: {}", outputFormats);
        return EnumSet.copyOf(outputFormats);
    }

}
