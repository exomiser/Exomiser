/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import static de.charite.compbio.exomiser.core.ExomiserSettings.OUT_FORMAT_OPTION;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutFormatOptionMarshaller extends AbstractOptionMarshaller {
    
    private static final Logger logger = LoggerFactory.getLogger(OutFormatOptionMarshaller.class);

    public OutFormatOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("type")
                .withType(OutputFormat.class)
                .withValueSeparator(',')
                .withDescription("Comma separated list of format options: HTML, VCF or TAB. Defaults to HTML if not specified. e.g. --out-format=TAB or --out-format=TAB,HTML,VCF")
                .withLongOpt(OUT_FORMAT_OPTION)
                .create("f");
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
                case "TAB":
                    outputFormats.add(OutputFormat.TSV);
                    break;
                case "TSV":
                    outputFormats.add(OutputFormat.TSV);
                    break;
                case "VCF":
                    outputFormats.add(OutputFormat.VCF);
                    break;
                default:
                    logger.info("{} is not a recognised output format. Please choose one or more of HTML, TAB, VCF - defaulting to HTML", outputFormatString);
                    outputFormats.add(OutputFormat.HTML);
                    break;
            }
        }
        logger.debug("Setting output formats: {}", outputFormats);
        return EnumSet.copyOf(outputFormats);
    }

}
