/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.EXOMISER2_PARAMS_OPTION;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserAllSpeciesOptionMarshaller extends AbstractOptionMarshaller {

    public ExomiserAllSpeciesOptionMarshaller() {
        option = OptionBuilder
                .hasOptionalArgs()
                .withArgName("type")
                .withType(OutputFormat.class)
                .withValueSeparator(',')
                .withDescription(
                        String.format("Comma separated list of Optional parameters for phive-allspecies: human, mouse, fish, ppi. "
                                + "e.g. --%s=human or --%s=human,mouse,ppi", EXOMISER2_PARAMS_OPTION, EXOMISER2_PARAMS_OPTION))
                .withLongOpt(EXOMISER2_PARAMS_OPTION)
                .create("E");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        settingsBuilder.exomiser2Params(parseExomiser2Params(values));
    }

    private String parseExomiser2Params(String[] values) {
        String exomiser2Params = "";
        if (values.length == 0) {
            return exomiser2Params;
        }
        for (String token : values) {
            token = token.trim();
            if (exomiser2Params.equals("")) {
                exomiser2Params = token;
            } else {
                exomiser2Params = exomiser2Params + "," + token;
            }
        }
        return exomiser2Params;
    }
}
