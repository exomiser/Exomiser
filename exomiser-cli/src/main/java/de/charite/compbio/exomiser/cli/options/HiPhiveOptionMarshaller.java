/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptionMarshaller extends AbstractOptionMarshaller {

    public static final String HIPHIVE_PARAMS_OPTION = "hiphive-params";

    public HiPhiveOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("type")
                .withType(String.class)
                .withValueSeparator(',')
                .withDescription(String.format("Comma separated list of optional parameters for %s: human, mouse, fish, ppi. "
                                + "e.g. --%s=human or --%s=human,mouse,ppi", "hiphive", HIPHIVE_PARAMS_OPTION, HIPHIVE_PARAMS_OPTION))
                .withLongOpt(HIPHIVE_PARAMS_OPTION)
                .create("E");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
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
