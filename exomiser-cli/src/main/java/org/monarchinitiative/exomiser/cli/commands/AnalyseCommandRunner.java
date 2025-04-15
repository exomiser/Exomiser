package org.monarchinitiative.exomiser.cli.commands;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.writers.AnalysisResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class AnalyseCommandRunner implements CommandRunner<AnalyseCommand> {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseCommandRunner.class);

    private final Exomiser exomiser;

    public AnalyseCommandRunner(Exomiser exomiser) {
        this.exomiser = exomiser;
    }

    public Integer run(AnalyseCommand analyseCommand) {
        logger.info("Running {}", analyseCommand);
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
        return 0;
    }

}
