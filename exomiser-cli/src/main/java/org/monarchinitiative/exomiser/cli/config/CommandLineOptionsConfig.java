/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.cli.config;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for setting-up the command-line options. If you want a
 * new option on the command line, add it here.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class CommandLineOptionsConfig {

    @Bean
    public Options options() {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Shows this help"));

        options.addOption(Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to analysis script file. This should be in yaml format.")
                .longOpt("analysis")
                .build());

        options.addOption(Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to analysis batch file. This should be in plain text file with the path to a single analysis script file in yaml format on each line.")
                .longOpt("analysis-batch")
                .build());

        return options;
    }

}
