package org.monarchinitiative.exomiser.cli.command;


import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static picocli.CommandLine.*;

@Command(name = "batch",
        description = "Runs a batch of Exomiser analyses. This will be more efficient than running multiple analysis" +
                " jobs in parallel. However, if you have hundreds of analyses it would be more efficient to run multiple" +
                " batches in parallel."
)
public final class BatchCommand implements JobParserCommand, ExomiserCommand {

    private static final Logger logger = LoggerFactory.getLogger(BatchCommand.class);

    @Parameters(arity = "1", description = "Path to Exomiser batch input file. Each line of the file should be an 'analysis' command. See 'analysis' for details.")
    public Path batchFile;

    @Override
    public List<JobProto.Job> parseJobs() {
        if (batchFile == null) {
            return List.of();
        }
        logger.info("Processing batch file {}", batchFile);

        try (Stream<String> lines = Files.lines(batchFile, StandardCharsets.UTF_8)) {
            return lines
                    .filter(commentLines())
                    .filter(emptyLines())
                    .flatMap(line -> CommandLineParser.parseArgs(new AnalysisCommand(), line.split("\\s+")).stream())
                    .flatMap(analysisCommand -> analysisCommand.parseJobs().stream())
                    .toList();
        } catch (IOException ex) {
            logger.error("Unable to read batch file {}", batchFile, ex);
        }
        return List.of();
    }

    private static Predicate<String> commentLines() {
        return line -> !line.startsWith("#");
    }

    private static Predicate<String> emptyLines() {
        return line -> !line.isEmpty();
    }

}

