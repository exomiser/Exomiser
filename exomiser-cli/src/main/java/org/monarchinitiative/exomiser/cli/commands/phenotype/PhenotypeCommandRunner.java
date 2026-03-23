package org.monarchinitiative.exomiser.cli.commands.phenotype;

import org.monarchinitiative.exomiser.cli.commands.CommandRunner;
import org.monarchinitiative.exomiser.cli.commands.PhenotypeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PhenotypeCommandRunner implements CommandRunner<PhenotypeCommand> {

    private static final Logger logger = LoggerFactory.getLogger(PhenotypeCommandRunner.class);

    private int exitCode = 0;

    @Override
    public Integer run(PhenotypeCommand command) {
        logger.info("Running {}", command.phenopacketPath);
        return exitCode;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
