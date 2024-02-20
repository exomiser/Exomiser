/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.cli;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Locale;


/**
 * Main class for calling off the command line in the Exomiser package.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Parse the input to check for help etc. in order to fail fast before launching the context.
        // This does mean the input needs parsing twice - once here and again in the application CommandLineRunner.
        CommandLine commandLine = CommandLineOptionsParser.parse(args);
        if (commandLine.hasOption("help") || commandLine.getOptions().length == 0) {
            CommandLineOptionsParser.printHelp();
            System.exit(0);
        }

        // all ok so far - try launching the app
        Locale.setDefault(Locale.UK);
        int exitCode = SpringApplication.exit(SpringApplication.run(Main.class, args));
        logger.info("Exomising finished - Bye!");
        System.exit(exitCode);
    }

}
