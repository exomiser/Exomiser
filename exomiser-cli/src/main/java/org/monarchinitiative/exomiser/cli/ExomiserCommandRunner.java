package org.monarchinitiative.exomiser.cli;

import org.monarchinitiative.exomiser.cli.commands.*;
import org.monarchinitiative.exomiser.cli.pico.CommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class ExomiserCommandRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserCommandRunner.class);

    private final AnalyseCommandRunner analyseCommandRunner;
    private final AnnotateCommandRunner annotateCommandRunner;
    private final BatchCommandRunner batchCommandRunner;

    private int exitCode = 0;

    public ExomiserCommandRunner(AnalyseCommandRunner analyseCommandRunner, AnnotateCommandRunner annotateCommandRunner, BatchCommandRunner batchCommandRunner) {
        this.analyseCommandRunner = analyseCommandRunner;
        this.annotateCommandRunner = annotateCommandRunner;
        this.batchCommandRunner = batchCommandRunner;
    }

    @Override
    public void run(String... args) throws Exception {
        var commandLine = ExomiserCli.newExomiserCommandLine();
        var commandParser = new CommandParser<ExomiserCommand>(commandLine);
        var commandParserResult = commandParser.parseArgs(args);
        exitCode = switch (commandParserResult.command()) {
            case AnalyseCommand analyseCommand -> analyseCommandRunner.run(analyseCommand);
            case AnnotateCommand annotateCommand -> annotateCommandRunner.run(annotateCommand);
            case BatchCommand batchCommand -> batchCommandRunner.run(batchCommand);
            case null -> commandParserResult.code();
        };
        logger.info("Exomising finished - Bye!");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
