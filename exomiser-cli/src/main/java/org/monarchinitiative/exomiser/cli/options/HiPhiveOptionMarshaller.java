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
