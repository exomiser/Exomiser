package org.monarchinitiative.exomiser.cli;

import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.commands.AnnotateCommand;
import org.monarchinitiative.exomiser.cli.commands.BatchCommand;
import org.monarchinitiative.exomiser.cli.pico.ManifestVersionProvider;
import picocli.CommandLine;

@CommandLine.Command(name = "exomiser",
        mixinStandardHelpOptions = true,
        versionProvider = ManifestVersionProvider.class,
        header = "Exomiser: Phenotype-driven variant prioritisation for rare Mendelian disorders.",
        footer = {"Documentation: https://exomiser.readthedocs.io/en/latest/", "Source: https://github.com/exomiser/Exomiser"},
        subcommands = {
                AnalyseCommand.class,
                AnnotateCommand.class,
                BatchCommand.class,
        }

)
/**
 * @since 15.0.0
 */
public class ExomiserCli {

        public static CommandLine newExomiserCommandLine() {
                return new CommandLine(new ExomiserCli())
                        .setCaseInsensitiveEnumValuesAllowed(true)
                        ;
        }

        @Override
        public String toString() {
                return "ExomiserCli{}";
        }
}
