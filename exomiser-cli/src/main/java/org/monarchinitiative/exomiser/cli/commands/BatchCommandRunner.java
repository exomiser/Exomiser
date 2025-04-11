package org.monarchinitiative.exomiser.cli.commands;

import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.cli.commands.batch.BatchFileReader;
import org.monarchinitiative.exomiser.cli.commands.batch.BatchFileValidationResults;
import org.monarchinitiative.exomiser.cli.commands.batch.SampleValidationError;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisDurationFormatter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.JobParser;
import org.monarchinitiative.exomiser.core.analysis.sample.PedigreeSampleValidator;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.NoOpVcfReader;
import org.monarchinitiative.exomiser.core.genome.VcfFileReader;
import org.monarchinitiative.exomiser.core.genome.VcfReader;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleIdentifiers;
import org.monarchinitiative.exomiser.core.writers.AnalysisResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Component
public class BatchCommandRunner implements CommandRunner<BatchCommand> {

    private static final Logger logger = LoggerFactory.getLogger(BatchCommandRunner.class);

    private final JobParser jobParser;
    private final Exomiser exomiser;


    public BatchCommandRunner(JobParser jobParser, Exomiser exomiser) {
        this.jobParser = jobParser;
        this.exomiser = exomiser;
    }

    public Integer run(BatchCommand batchCommand) {
        logger.info("Running command {}", batchCommand);
        if (batchCommand.dryRun) {
            return doDryRun(batchCommand);
        } else {
            List<JobProto.Job> jobs = BatchFileReader.readJobsFromBatchFile(batchCommand.batchFilePath);
            Instant timeStart = Instant.now();
            int counter = 0;
            for (JobProto.Job job : jobs) {
                logger.info("Running job {} of {}", ++counter, jobs.size());
                AnalysisResults analysisResults = exomiser.run(job);
                logger.info("Writing results...");
                AnalysisResultsWriter.writeToFile(analysisResults, job.getOutputOptions());
            }
            Duration duration = Duration.between(timeStart, Instant.now());
            long ms = duration.toMillis();
            String formatted = AnalysisDurationFormatter.format(duration);
            logger.info("Finished batch of {} samples in {} ({} ms)", jobs.size(), formatted, ms);
        }
        return 0;
    }

    private int doDryRun(BatchCommand batchCommand) {
        BatchValidator batchValidator = new BatchValidator(jobParser);
        BatchFileValidationResults validationResults = batchValidator.validateBatchFile(batchCommand.batchFilePath);
        if (validationResults.errorCount() > 0) {
            Path errorFile = Path.of(batchCommand.batchFilePath + ".errors");
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(errorFile)) {
                validationResults.sampleValidationErrors()
                        .forEach(sampleValidationError -> {
                            try {
                                String errorMessage = String.format(
                                        """
                                        ERROR:
                                          line: %s
                                          cause: %s
                                        """, sampleValidationError.inputLine(), sampleValidationError.errorMessage());
                                bufferedWriter.write(errorMessage);
                                bufferedWriter.flush();
                            } catch (IOException e) {
                                logger.error("Error writing to file {}", errorFile, e);
                            }
                        });
            } catch (IOException e) {
                logger.error("Unable to write error file {}", errorFile, e);
                return 1;
            }
            logger.warn("Found {} errors in {} analyses. Written errors to {}", validationResults.errorCount(), validationResults.checkedCount(), errorFile.toAbsolutePath());
            return 1;
        }
        logger.info("Checked {} analyses, with no problems found.", validationResults.checkedCount());
        return 0;
    }


    record BatchValidator(JobParser jobParser) {

        BatchFileValidationResults validateBatchFile(Path batchFilePath) {
            AtomicInteger checkedCounter = new AtomicInteger();
            AtomicInteger errCounter = new AtomicInteger();
            List<SampleValidationError> sampleValidationErrors = Collections.emptyList();
            try (Stream<String> lines = Files.lines(batchFilePath, StandardCharsets.UTF_8)) {
                sampleValidationErrors = lines
                        .filter(commentLines())
                        .filter(emptyLines())
                        .map(validateLine(checkedCounter))
                        .filter(Result::isErr)
                        .map(errorResult -> {
                            errCounter.incrementAndGet();
                            return errorResult.err();
                        })
                        .toList();
            } catch (IOException ex) {
                logger.error("Unable to read batch file {}", batchFilePath, ex);
            }
            int errors = errCounter.get();
            int checked = checkedCounter.get();

            return new BatchFileValidationResults(checked, errors, sampleValidationErrors);
        }

        private Predicate<String> commentLines() {
            return line -> !line.startsWith("#");
        }

        private Predicate<String> emptyLines() {
            return line -> !line.isEmpty();
        }

        private Function<String, Result<Sample, SampleValidationError>> validateLine(AtomicInteger checkedCounter) {
            return line -> {
                checkedCounter.incrementAndGet();
                JobProto.Job job = null;
                try {
                    var commandLine = new CommandLine(new AnalyseCommand()).setCaseInsensitiveEnumValuesAllowed(true);
                    CommandLine.ParseResult parseResult = commandLine.parseArgs(line.split("\\s+"));
                    CommandLine parsed = parseResult.asCommandLineList().getFirst();
                    if (parsed != null) {
                        AnalyseCommand analyseCommand = parsed.getCommand();
                        job = analyseCommand.readJob();
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    try {
                        return Result.err(new SampleValidationError(line, ex.getMessage()));
                    } catch (Exception ex2) {
                        return Result.err(new SampleValidationError(line, ex2.getMessage()));
                    }
                }
                return validateJob(line, job);
            };
        }

        private Result<Sample, SampleValidationError> validateJob(String line, JobProto.Job protoJob) {
            Sample sample;
            try {
                sample = jobParser.parseSample(protoJob);
            } catch (Exception ex) {
                return Result.err(new SampleValidationError(line, ex.getMessage()));
            }
            try {
                Path vcfPath = sample.getVcfPath();
                VcfReader vcfReader = vcfPath == null ? new NoOpVcfReader() : new VcfFileReader(vcfPath);
                List<String> sampleNames = vcfReader.readSampleIdentifiers();
                String probandIdentifier = SampleIdentifiers.checkProbandIdentifier(sample.getProbandSampleName(), sampleNames);
                Pedigree validatedPedigree = PedigreeSampleValidator.validate(sample.getPedigree(), probandIdentifier, sampleNames);
                return Result.ok(sample);
            } catch (Exception ex) {
                return Result.err(new SampleValidationError(line, ex.getMessage()));
            }
        }
    }

    record Result<T, E>(T ok, E err) {
        Result {
            if (ok != null && err != null) {
                throw new IllegalStateException();
            }
        }

        static <T, E> Result<T, E> ok(T t) {
            Objects.requireNonNull(t);
            return new Result<>(t, null);
        }

        static <T, E> Result<T, E> err(E err) {
            Objects.requireNonNull(err);
            return new Result<>(null, err);
        }

        boolean isOk() {
            return ok != null;
        }

        boolean isErr() {
            return err != null;
        }

        T get() {
            return ok;
        }
    }
}
