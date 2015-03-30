/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import static de.charite.compbio.exomiser.core.ExomiserSettings.OUT_FILE_FORMAT_OPTION;
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
public class OutFileFormatOptionMarshaller extends AbstractOptionMarshaller {
    
    private static final Logger logger = LoggerFactory.getLogger(OutFileFormatOptionMarshaller.class);

    public OutFileFormatOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("type")
                .withType(OutputFormat.class)
                .withValueSeparator(',')
                .withDescription("Comma separated list of format options: HTML, VCF, TAB-GENE or TAB-VARIANT,. Defaults to HTML if not specified. e.g. --out-format=TAB-VARIANT or --out-format=TAB-GENE,TAB-VARIANT,HTML,VCF")
                .withLongOpt(OUT_FILE_FORMAT_OPTION)
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
