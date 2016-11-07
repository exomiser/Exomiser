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
public class HiPhiveOptionMarshaller extends AbstractOptionMarshaller {

    public static final String HIPHIVE_PARAMS_OPTION = "hiphive-params";

    public HiPhiveOptionMarshaller() {
        option = Option.builder("E")
                .hasArgs()
                .argName("type")
                .type(String.class)
                .valueSeparator(',')
                .desc(String.format("Comma separated list of optional parameters for %s: human, mouse, fish, ppi. "
                                + "e.g. --%s=human or --%s=human,mouse,ppi", "hiphive", HIPHIVE_PARAMS_OPTION, HIPHIVE_PARAMS_OPTION))
                .longOpt(HIPHIVE_PARAMS_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.hiPhiveParams(parseHiPhiveParams(values));
    }

    private String parseHiPhiveParams(String[] values) {
        String hiPhiveParams = "";
        if (values.length == 0) {
            return hiPhiveParams;
        }
        for (String token : values) {
            token = token.trim();
            if (hiPhiveParams.equals("")) {
                hiPhiveParams = token;
            } else {
                hiPhiveParams = hiPhiveParams + "," + token;
            }
        }
        return hiPhiveParams;
    }
}
