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

import org.monarchinitiative.exomiser.cli.commands.*;
import org.monarchinitiative.exomiser.cli.commands.PhenotypeCommand;
import org.monarchinitiative.exomiser.cli.commands.analyse.AnalyseApplication;
import org.monarchinitiative.exomiser.cli.commands.annotate.AnnotateApplication;
import org.monarchinitiative.exomiser.cli.commands.batch.BatchApplication;
import org.monarchinitiative.exomiser.cli.commands.phenotype.PhenotypeApplication;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.springframework.boot.SpringApplication;

import java.util.Locale;


/**
 * Main class for calling off the command line in the Exomiser package.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.UK);
        // Parse the input to check for help etc. in order to fail fast before launching the context.
        // This does mean the input needs parsing twice - once here and again in the application CommandLineRunner.
        var commandParser = new CommandParser<ExomiserCommand>(ExomiserCli.newExomiserCommandLine());
        var parserResult = commandParser.parseArgs(args);
        if (!parserResult.isCommand()) {
            System.exit(parserResult.exitCode());
        }

        ExomiserCommand command = parserResult.command();
        if (command == null || !command.validate()) {
            System.exit(1);
        }

        // all OK so far - try launching the app
        // Select application configuration based on command type
        //  this will selectively load the Spring components and resources
        //  required by each command.
        Class<?> configClass = switch (command) {
            case AnalyseCommand ignored -> AnalyseApplication.class;
            case AnnotateCommand ignored -> AnnotateApplication.class;
            case BatchCommand ignored -> BatchApplication.class;
            case PhenotypeCommand ignored -> PhenotypeApplication.class;
        };

        int exitCode = SpringApplication.exit(SpringApplication.run(configClass, args));
        System.exit(exitCode);
    }
}