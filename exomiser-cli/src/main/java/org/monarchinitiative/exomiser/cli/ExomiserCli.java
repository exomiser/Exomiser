package org.monarchinitiative.exomiser.cli;

import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.commands.AnnotateCommand;
import org.monarchinitiative.exomiser.cli.commands.BatchCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "exomiser",
        mixinStandardHelpOptions = true,
        version = "15.0.0",
        subcommands = {
                AnalyseCommand.class,
                AnnotateCommand.class,
                BatchCommand.class,
        }
)
public class ExomiserCli {

        public static CommandLine newExomiserCommandLine() {
                return new CommandLine(new ExomiserCli())
                        .setCaseInsensitiveEnumValuesAllowed(true)
//                        .setStopAtUnmatched(true)
                        ;
        }

        @Override
        public String toString() {
                return "ExomiserCli{}";
        }
}
