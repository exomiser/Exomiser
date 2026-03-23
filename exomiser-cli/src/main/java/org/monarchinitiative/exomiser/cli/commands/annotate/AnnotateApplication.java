package org.monarchinitiative.exomiser.cli.commands.annotate;

import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfiguration;
import org.monarchinitiative.exomiser.cli.ExomiserCli;
import org.monarchinitiative.exomiser.cli.commands.AnnotateCommand;
import org.monarchinitiative.exomiser.cli.config.MainConfig;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({MainConfig.class, ExomiserAutoConfiguration.class})
public class AnnotateApplication {

    @Bean
    public CommandLineRunner annotateCommandLineRunner(AnnotateCommandRunner runner) {
        return args -> {
            var result = new CommandParser<>(ExomiserCli.newExomiserCommandLine()).parseArgs(args);
            if (result.command() instanceof AnnotateCommand cmd) {
                runner.run(cmd);
            }
        };
    }

}
