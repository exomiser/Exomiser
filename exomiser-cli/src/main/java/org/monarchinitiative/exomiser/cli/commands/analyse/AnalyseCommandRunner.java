package org.monarchinitiative.exomiser.cli.commands.analyse;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.monarchinitiative.exomiser.cli.commands.AnalyseCommand;
import org.monarchinitiative.exomiser.cli.commands.CommandRunner;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.writers.AnalysisResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public final class AnalyseCommandRunner implements CommandRunner<AnalyseCommand>, ExitCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseCommandRunner.class);

    private final Exomiser exomiser;
    private int exitCode = 0;

    public AnalyseCommandRunner(Exomiser exomiser) {
        this.exomiser = exomiser;
    }

    public Integer run(AnalyseCommand analyseCommand) {
        logger.info("Running {}", analyseCommand);
        try {
            var job = analyseCommand.readJob();
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Running job {}", JsonFormat.printer().print(job));
                } catch (InvalidProtocolBufferException e) {
                    throw new IllegalStateException(e);
                }
            }
            AnalysisResults analysisResults = exomiser.run(job);
            logger.info("Writing results...");
            AnalysisResultsWriter.writeToFile(analysisResults, job.getOutputOptions());
            logger.info("Exomising finished - Bye!");
        } catch (Exception e) {
            logger.error("Analysis failed", e);
            exitCode = 1;
        }
        return exitCode;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
