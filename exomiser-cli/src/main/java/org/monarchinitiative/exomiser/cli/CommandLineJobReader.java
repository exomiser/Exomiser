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

package org.monarchinitiative.exomiser.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.JobReader;
import org.monarchinitiative.exomiser.core.proto.ProtoParser;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CommandLineJobReader {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineJobReader.class);

    public List<JobProto.Job> readJobs(CommandLine commandLine) {
        Set<String> userOptions = Arrays.stream(commandLine.getOptions())
                .map(Option::getLongOpt)
                .collect(Collectors.toSet());
        logger.debug("Parsed options: {}", userOptions);

        // old cli option - expect an old-style analysis where the sample is specified in the analysis
        // this is maintained for backwards-compatibility
        if (userOptions.equals(Set.of("analysis"))) {
            Path analysisPath = Paths.get(commandLine.getOptionValue("analysis"));
            JobProto.Job job = JobReader.readJob(analysisPath);
            return List.of(job);
        }
        // old cli option for running a batch of analyses
        if (userOptions.equals(Set.of("analysis-batch"))) {
            Path analysisBatchFile = Paths.get(commandLine.getOptionValue("analysis-batch"));
            List<Path> analysisScripts = BatchFileReader.readPathsFromBatchFile(analysisBatchFile);
            return analysisScripts.stream().map(JobReader::readJob).collect(Collectors.toList());
        }
        // new option replacing the analysis with job. These are functionally equivalent, but the sample is separated
        // from the analysis part to allow for greater flexibility

        //TODO: do we need a job option if the analysis and analysis-batch options can read either a new job or old
        // analysis?
        if (userOptions.equals(Set.of("job"))) {
            Path jobPath = Paths.get(commandLine.getOptionValue("job"));
            JobProto.Job job = JobReader.readJob(jobPath);
            return List.of(job);
        }

        // Once here must contain a reference to sample, all other options (analysis, output, preset) are optional.
        // Legal options are:
        // "sample"
        // "sample", "analysis"
        // "sample", "analysis", "output"
        // "sample", "preset"
        // "sample", "preset", "output"
        // "sample", "output"
        if (userOptions.contains("sample")) {
            // TODO: check the sample input has at least a list of phenotypes or a VCF? Need to decide how to handle
            //  VCF-only at this point as there will be memory issues if the data isn't streamed and written to disk.
            JobProto.Job.Builder jobBuilder = newDefaultJobBuilder();
            for (String option : userOptions) {
                String optionValue = commandLine.getOptionValue(option);
                if ("sample".equals(option)) {
                    handleSampleOption(optionValue, jobBuilder);
                }
                if ("preset".equals(option)) {
                    handlePresetOption(optionValue, jobBuilder);
                }
                if ("analysis".equals(option)) {
                    handleAnalysisOption(optionValue, jobBuilder);
                }
                if ("output".equals(option)) {
                    handleOutputOption(optionValue, jobBuilder);
                }
            }
            ///TODO: check output prefix here and use the filename of the input sample as the output?
            return List.of(jobBuilder.build());
        }

        throw new CommandLineParseError("No sample specified!");
    }

    private JobProto.Job.Builder newDefaultJobBuilder() {
        // set defaults - these will be overridden if defined in the command line options
        return JobProto.Job.newBuilder()
                .setPreset(AnalysisProto.Preset.EXOME)
                .setOutputOptions(createDefaultOutputOptions());
    }

    private OutputProto.OutputOptions createDefaultOutputOptions() {
        return OutputProto.OutputOptions.newBuilder()
                .setOutputPrefix("")
                .addOutputFormats(OutputFormat.HTML.toString())
                .addOutputFormats(OutputFormat.JSON.toString())
                .setNumGenes(0)
                .setOutputContributingVariantsOnly(false)
                .build();
    }

    private void handleSampleOption(String sampleOptionValue, JobProto.Job.Builder jobBuilder) {
        Path samplePath = Paths.get(sampleOptionValue);
        // This could be a Sample a Phenopacket or a Family
        JobProto.Job sampleJob = readSampleJob(samplePath);
        jobBuilder.mergeFrom(sampleJob);
    }

    private void handleAnalysisOption(String analysisOptionValue, JobProto.Job.Builder jobBuilder) {
        Path analysisPath = Paths.get(analysisOptionValue);
        jobBuilder.setAnalysis(readAnalysis(analysisPath));
    }

    private void handlePresetOption(String presetValue, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setPreset(parsePreset(presetValue));
    }

    private void handleOutputOption(String outputOptionValue, JobProto.Job.Builder jobBuilder) {
        Path outputOptionPath = Paths.get(outputOptionValue);
        jobBuilder.setOutputOptions(readOutputOptions(outputOptionPath));
    }

    private AnalysisProto.Analysis readAnalysis(Path analysisPath) {
        AnalysisProto.Analysis analysis = ProtoParser.parseFromJsonOrYaml(AnalysisProto.Analysis.newBuilder(), analysisPath)
                .build();
        if (analysis.equals(AnalysisProto.Analysis.getDefaultInstance())) {
            throw new IllegalArgumentException("Unable to parse analysis from file " + analysisPath + " please check the format");
        }
        return analysis;
    }

    private JobProto.Job readSampleJob(Path samplePath) {
        JobProto.Job.Builder jobBuilder = JobProto.Job.newBuilder();
        SampleProto.Sample sampleProto = ProtoParser.parseFromJsonOrYaml(SampleProto.Sample.newBuilder(), samplePath)
                .build();
        if (!sampleProto.equals(SampleProto.Sample.getDefaultInstance())) {
            jobBuilder.setSample(sampleProto);
            return jobBuilder.build();
        }
        //try phenopacket:
        Phenopacket phenopacket = ProtoParser.parseFromJsonOrYaml(Phenopacket.newBuilder(), samplePath).build();
        if (!phenopacket.equals(Phenopacket.getDefaultInstance())) {
            jobBuilder.setPhenopacket(phenopacket);
            return jobBuilder.build();
        }
        //try family:
        Family family = ProtoParser.parseFromJsonOrYaml(Family.newBuilder(), samplePath).build();
        if (!family.equals(Family.getDefaultInstance())) {
            jobBuilder.setFamily(family);
            return jobBuilder.build();
        }
        throw new IllegalArgumentException("Unable to parse sample from file " + samplePath + " please check the format");
    }

    private AnalysisProto.Preset parsePreset(String presetValue) {
        switch (presetValue.toLowerCase()) {
            case "exome":
                return AnalysisProto.Preset.EXOME;
            case "genome":
                return AnalysisProto.Preset.GENOME;
            default:
                throw new IllegalArgumentException("Unrecognised preset option: " + presetValue);
        }
    }

    private OutputProto.OutputOptions readOutputOptions(Path outputOptionsPath) {
        OutputProto.OutputOptions outputOptions = ProtoParser.parseFromJsonOrYaml(OutputProto.OutputOptions.newBuilder(), outputOptionsPath)
                .build();
        if (outputOptions.equals(OutputProto.OutputOptions.getDefaultInstance())) {
            throw new IllegalArgumentException("Unable to parse outputOptions from file " + outputOptionsPath + " please check the format");
        }
        return outputOptions;
    }
}
