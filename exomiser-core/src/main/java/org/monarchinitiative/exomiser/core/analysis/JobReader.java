/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis;

import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.proto.ProtoParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Class for reading {@link JobProto.Job} objects from disk or input string. Will accept a legacy (pre-version 13.0.0)
 * analysis.yaml or a v13.0.0 job.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class JobReader {

    private static final Logger logger = LoggerFactory.getLogger(JobReader.class);

    private JobReader() {
    }

    /**
     * Reads a job or legacy analysis as used in versions 8.0.0-12.1.0. The legacy analysis contains the sample and
     * the analysis in the same object. This functionality is maintained for backwards compatibility.
     *
     * @param jobString
     * @return a Job object
     */
    public static JobProto.Job readJob(String jobString) {
        JobProto.Job parsedJob = parseJob(jobString);
        return getOrMigrateJob(parsedJob);
    }

    private static JobProto.Job parseJob(String jobString) {
        JobProto.Job job = ProtoParser.parseFromJsonOrYaml(JobProto.Job.newBuilder(), jobString).build();
        if (job.equals(JobProto.Job.getDefaultInstance())) {
            throw new IllegalArgumentException("Unable to parse job from input. Please check the format.");
        }
        return job;
    }

    /**
     * Reads a job or legacy analysis file as used in versions 8.0.0-12.1.0. The legacy analysis contains the sample and
     * the analysis in the same object. This functionality is maintained for backwards compatibility.
     *
     * @param jobPath
     * @return a Job object
     */
    public static JobProto.Job readJob(Path jobPath) {
        JobProto.Job parsedJob = parseJob(jobPath);
        return getOrMigrateJob(parsedJob);
    }

    private static JobProto.Job parseJob(Path jobPath) {
        JobProto.Job job = ProtoParser.parseFromJsonOrYaml(JobProto.Job.newBuilder(), jobPath).build();
        if (job.equals(JobProto.Job.getDefaultInstance())) {
            throw new IllegalArgumentException("Unable to parse job from file " + jobPath + " please check the format.");
        }
        return job;
    }

    private static JobProto.Job getOrMigrateJob(JobProto.Job parsedJob) {
        if (parsedJob.hasSample() || parsedJob.hasPhenopacket() || parsedJob.hasFamily()) {
            logger.debug("New Job - {}", parsedJob);
            return parsedJob;
        }
        logger.debug("Legacy Job - {}", parsedJob);
        return migrateLegacyAnalysisToJob(parsedJob);
    }

    private static JobProto.Job migrateLegacyAnalysisToJob(JobProto.Job parsedJob) {
        JobProto.Job.Builder jobBuilder = parsedJob.toBuilder();

        // the legacy analysis contains the sample information, which is different to the newer analysis which does not.
        AnalysisProto.Analysis.Builder jobAnalysisBuilder = jobBuilder.getAnalysisBuilder();
        // extract Sample from legacy Analysis
        SampleProto.Sample sample = extractSample(jobAnalysisBuilder);
        jobBuilder.setSample(sample);

        // these fields are deprecated, but are maintained for backwards compatibility of input, hence
        // we need to clear them before returning the job
        jobAnalysisBuilder.clearGenomeAssembly();
        jobAnalysisBuilder.clearVcf();
        jobAnalysisBuilder.clearPed();
        jobAnalysisBuilder.clearProband();
        jobAnalysisBuilder.clearHpoIds();

        return jobBuilder.build();
    }

    private static SampleProto.Sample extractSample(AnalysisProto.Analysis.Builder jobAnalysisBuilder) {
        return SampleProto.Sample.newBuilder()
                .setGenomeAssembly(jobAnalysisBuilder.getGenomeAssembly())
                .setVcf(jobAnalysisBuilder.getVcf())
                .setPed(jobAnalysisBuilder.getPed())
                .setProband(jobAnalysisBuilder.getProband())
                .addAllHpoIds(jobAnalysisBuilder.getHpoIdsList())
                .build();
    }

}
