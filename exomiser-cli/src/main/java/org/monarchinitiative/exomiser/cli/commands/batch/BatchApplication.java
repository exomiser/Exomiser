package org.monarchinitiative.exomiser.cli.commands.batch;

import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfiguration;
import org.monarchinitiative.exomiser.cli.ExomiserCli;
import org.monarchinitiative.exomiser.cli.commands.AnnotateCommand;
import org.monarchinitiative.exomiser.cli.commands.BatchCommand;
import org.monarchinitiative.exomiser.cli.commands.annotate.AnnotateCommandRunner;
import org.monarchinitiative.exomiser.cli.config.MainConfig;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({MainConfig.class, ExomiserAutoConfiguration.class})
public class BatchApplication {

    @Bean
    public CommandLineRunner batchCommandLineRunner(BatchCommandRunner runner) {
        return args -> {
            var result = new CommandParser<>(ExomiserCli.newExomiserCommandLine()).parseArgs(args);
            if (result.command() instanceof BatchCommand cmd) {
                runner.run(cmd);
            }
        };
    }
}
