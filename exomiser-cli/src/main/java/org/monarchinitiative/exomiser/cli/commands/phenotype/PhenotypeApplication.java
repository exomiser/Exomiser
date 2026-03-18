package org.monarchinitiative.exomiser.cli.commands.phenotype;

import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.genome.GenomeAnalysisServiceAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.phenotype.PrioritiserAutoConfiguration;
import org.monarchinitiative.exomiser.cli.ExomiserCli;
import org.monarchinitiative.exomiser.cli.commands.*;
import org.monarchinitiative.exomiser.cli.config.MainConfig;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        ExomiserAutoConfiguration.class,
        GenomeAnalysisServiceAutoConfiguration.class
    }
)
@Import({MainConfig.class, PrioritiserAutoConfiguration.class})
public class PhenotypeApplication {

    @Bean
    public CommandLineRunner phenoCommandLineRunner(PhenotypeCommandRunner runner) {
        return args -> {
            var result = new CommandParser<ExomiserCommand>(ExomiserCli.newExomiserCommandLine()).parseArgs(args);
            if (result.command() instanceof PhenotypeCommand cmd) {
                runner.run(cmd);
            }
        };
    }
}
