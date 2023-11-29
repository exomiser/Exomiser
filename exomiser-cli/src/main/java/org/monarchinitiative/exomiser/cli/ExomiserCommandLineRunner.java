/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.cli;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.cli.CommandLine;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisDurationFormatter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.writers.AnalysisResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class ExomiserCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserCommandLineRunner.class);

    private final Exomiser exomiser;

    public ExomiserCommandLineRunner(Exomiser exomiser) {
        this.exomiser = exomiser;
    }

    @Override
    public void run(String... args) {
        CommandLine commandLine = CommandLineOptionsParser.parse(args);
        CommandLineJobReader jobReader = new CommandLineJobReader();
        List<JobProto.Job> jobs = jobReader.readJobs(commandLine);
        logger.info("Exomiser running...");
        runJobs(jobs);
    }

    private void runJobs(List<JobProto.Job> jobs) {
        if (jobs.size() == 1) {
            runJob(jobs.get(0));
        }
        if (jobs.size() > 1) {
            Instant timeStart = Instant.now();
            //this *could* be run in parallel using parallelStream() at the expense of RAM in order to hold all the variants in memory.
            //HOWEVER there may be threading issues so this needs investigation.
            for (int i = 0; i < jobs.size(); i++) {
                logger.info("Running job {} of {}", i + 1, jobs.size());
                runJob(jobs.get(i));
            }
            Duration duration = Duration.between(timeStart, Instant.now());
            long ms = duration.toMillis();
            String formatted = AnalysisDurationFormatter.format(duration);
            logger.info("Finished batch of {} samples in {} ({} ms)", jobs.size(), formatted, ms);
        }
    }

    private void runJob(JobProto.Job job) {
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
    }
}
