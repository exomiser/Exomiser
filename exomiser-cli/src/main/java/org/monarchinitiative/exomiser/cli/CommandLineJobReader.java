/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

import com.google.protobuf.Message;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.JobReader;
import org.monarchinitiative.exomiser.core.analysis.sample.PhenopacketPedigreeReader;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.proto.ProtoParser;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads {@link org.monarchinitiative.exomiser.api.v1.JobProto.Job} instances from the {@link CommandLine}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
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
            Path analysisPath = Path.of(commandLine.getOptionValue("analysis"));
            JobProto.Job job = JobReader.readJob(analysisPath);
            return List.of(job);
        }
        // old cli option for running a batch of analyses
        if (userOptions.equals(Set.of("analysis-batch"))) {
            Path analysisBatchFile = Path.of(commandLine.getOptionValue("analysis-batch"));
            List<Path> analysisScripts = BatchFileReader.readPathsFromBatchFile(analysisBatchFile);
            return analysisScripts.stream().map(JobReader::readJob).toList();
        }
        // new batch option which will parse each line as a cli command
        if (userOptions.equals(Set.of("batch"))) {
            Path analysisBatchFile = Path.of(commandLine.getOptionValue("batch"));
            return BatchFileReader.readJobsFromBatchFile(analysisBatchFile);
        }

        // new option replacing the analysis with job. These are functionally equivalent, but the sample is separated
        // from the analysis part to allow for greater flexibility

        if (userOptions.equals(Set.of("job"))) {
            Path jobPath = Path.of(commandLine.getOptionValue("job"));
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
        // "sample", "vcf", "output"
        // "sample", "vcf", "ped", "output"
        // "sample", "vcf", "ped", "output", "output-prefix"
        if (userOptions.contains("sample") || userOptions.contains("analysis")) {
            return handleMultipleUserOptions(commandLine, userOptions);
        }

        throw new CommandLineParseError("No sample specified!");
    }

    private List<JobProto.Job> handleMultipleUserOptions(CommandLine commandLine, Set<String> userOptions) {
        JobProto.Job.Builder jobBuilder = newDefaultJobBuilder();
        // parse the analysis first as this could be a legacy analysis (which contains the sample, analysis and output)
        // or it could just be a new analysis without the sample data.
        if (userOptions.contains("analysis")) {
            handleAnalysisOption(commandLine.getOptionValue("analysis"), jobBuilder);
        }
        for (String option : userOptions) {
            String optionValue = commandLine.getOptionValue(option);
            if ("sample".equals(option)) {
                handleSampleOption(optionValue, jobBuilder);
            }
            if ("preset".equals(option)) {
                handlePresetOption(optionValue, jobBuilder);
            }
            if ("output".equals(option)) {
                handleOutputOption(optionValue, jobBuilder);
            }
            if ("output-format".equals(option)) {
                String[] outputFormatStrings = commandLine.getOptionValues(option);
                handleOutputFormat(outputFormatStrings, jobBuilder);
            }
        }
        // post-process these optional commands for cases where the user wants to override/add a different VCF or PED
        if (userOptions.contains("vcf")) {
            handleVcfAndAssemblyOptions(commandLine.getOptionValue("vcf"), commandLine.getOptionValue("assembly"), jobBuilder);
        }
        if (userOptions.contains("ped")) {
            handlePedOption(commandLine.getOptionValue("ped"), jobBuilder);
        }
        if (userOptions.contains("output-prefix")) {
            logger.warn("output-prefix option is now DEPRECATED in favour of the output-directory and output-file-name options and will be removed in the next major version.");
            handleOutputPrefixOption(commandLine.getOptionValue("output-prefix"), jobBuilder);
        }
        if (userOptions.contains("output-directory")) {
            handleOutputDirectoryOption(commandLine.getOptionValue("output-directory"), jobBuilder);
        }
        if (userOptions.contains("output-filename")) {
            handleOutputFileNameOption(commandLine.getOptionValue("output-filename"), jobBuilder);
        }
        if (!jobBuilder.hasSample() && !jobBuilder.hasPhenopacket() && !jobBuilder.hasFamily()) {
            throw new CommandLineParseError("No sample specified!");
        }
        // Make sure Exomiser will return some results! Adding defaults to the initial builder will result in potential
        // duplicates of the default output file types.
        ensureOutputSettingsSpecifyOutputFormat(jobBuilder);
        logger.debug("Submitting Exomiser job: {}", jobBuilder);
        return List.of(jobBuilder.build());
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
                .setNumGenes(0)
                .setOutputContributingVariantsOnly(false)
                .build();
    }

    private void handleOutputFormat(String[] outputFormatStrings, JobProto.Job.Builder jobBuilder) {
        // override any settings from an analysis file referring to other output_formats
        OutputProto.OutputOptions.Builder optionsBuilder = jobBuilder.getOutputOptionsBuilder();
        optionsBuilder.clearOutputFormats();
        Set<String> outputFormats = new LinkedHashSet<>();
        for (String outputFormatString : outputFormatStrings) {
            try {
                OutputFormat outputFormat = OutputFormat.valueOf(outputFormatString);
                outputFormats.add(outputFormat.toString());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown output format: '" + outputFormatString + "'. Valid formats are " + List.of(OutputFormat.values()));
            }
        }
        optionsBuilder.addAllOutputFormats(outputFormats);
    }

    private void ensureOutputSettingsSpecifyOutputFormat(JobProto.Job.Builder jobBuilder) {
        if (jobBuilder.getOutputOptions().getOutputFormatsList().isEmpty()) {
            jobBuilder.getOutputOptionsBuilder()
                    .addOutputFormats(OutputFormat.HTML.toString())
                    .addOutputFormats(OutputFormat.JSON.toString());
        }
    }

    private void handleSampleOption(String sampleOptionValue, JobProto.Job.Builder jobBuilder) {
        Path samplePath = Path.of(sampleOptionValue);
        // This could be a Sample a Phenopacket or a Family
        JobProto.Job sampleJob = readSampleJob(samplePath);
        jobBuilder.mergeFrom(sampleJob);
    }

    private void handleAnalysisOption(String analysisOptionValue, JobProto.Job.Builder jobBuilder) {
        Path analysisPath = Path.of(analysisOptionValue);
        boolean isLegacyAnalysis = false;
        try {
            JobProto.Job job = JobReader.readJob(analysisPath);
            jobBuilder.mergeFrom(job);
            isLegacyAnalysis = true;
            logger.debug("{} is a legacy analysis format", analysisPath);
        } catch (IllegalArgumentException e) {
            // not a legacy analysis job
        }
        if (!isLegacyAnalysis) {
            jobBuilder.setAnalysis(readAnalysis(analysisPath));
        }
    }

    private void handleVcfAndAssemblyOptions(String vcfOptionValue, String assemblyOptionValue, JobProto.Job.Builder jobBuilder) {
        logger.debug("Handling VCF/assembly option {} {}", vcfOptionValue, assemblyOptionValue);
        Path vcfPath = Path.of(vcfOptionValue);
        if (assemblyOptionValue == null || assemblyOptionValue.isEmpty()) {
            // Using the incorrect assembly would lead to *very* incorrect results.
            throw new CommandLineParseError("assembly must be included when specifying vcf!");
        }
        String assembly = GenomeAssembly.parseAssembly(assemblyOptionValue).toGrcString();
        if (jobBuilder.hasPhenopacket()) {
            Phenopacket.Builder phenopacketBuilder = jobBuilder.getPhenopacketBuilder();
            if (phenopacketBuilder.getHtsFilesCount() != 0) {
                phenopacketBuilder.clearHtsFiles();
            }
            phenopacketBuilder.addHtsFiles(buildHtsFile(vcfPath, assembly));
        } else if (jobBuilder.hasFamily()) {
            Family.Builder familyBuilder = jobBuilder.getFamilyBuilder();
            if (familyBuilder.getHtsFilesCount() != 0) {
                familyBuilder.clearHtsFiles();
            }
            familyBuilder.addHtsFiles(buildHtsFile(vcfPath, assembly));
        } else {
            jobBuilder.getSampleBuilder()
                    .setVcf(vcfPath.toAbsolutePath().toString())
                    .setGenomeAssembly(assembly);
        }
    }

    private HtsFile.Builder buildHtsFile(Path vcfPath, String assembly) {
        logger.debug("Building HtsFile VCF {} {}", vcfPath, assembly);
        return HtsFile.newBuilder()
                .setHtsFormat(HtsFile.HtsFormat.VCF)
                .setUri(vcfPath.toUri().toString())
                .setGenomeAssembly(assembly);
    }

    private void handlePedOption(String pedOptionValue, JobProto.Job.Builder jobBuilder) {
        logger.debug("Got a PED option {}", pedOptionValue);
        Path pedPath = Path.of(pedOptionValue);
        if (jobBuilder.hasPhenopacket()) {
            // upgrade to family
            Phenopacket.Builder phenopacketBuilder = jobBuilder.getPhenopacketBuilder();
            Pedigree pedigree = PhenopacketPedigreeReader.readPedFile(pedPath);
            List<HtsFile> htsFilesList = phenopacketBuilder.getHtsFilesList();

            Family.Builder familyBuilder = Family.newBuilder()
                    .setId(phenopacketBuilder.getId())
                    .setProband(phenopacketBuilder.clearHtsFiles().build())
                    .addAllHtsFiles(htsFilesList)
                    .setPedigree(pedigree);

            jobBuilder.setFamily(familyBuilder);
        } else if (jobBuilder.hasFamily()) {
            Pedigree pedigree = PhenopacketPedigreeReader.readPedFile(pedPath);
            jobBuilder.getFamilyBuilder().setPedigree(pedigree);
        } else {
            jobBuilder.getSampleBuilder().setPed(pedPath.toAbsolutePath().toString());
        }
    }

    private void handlePresetOption(String presetValue, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setPreset(parsePreset(presetValue));
    }

    private void handleOutputOption(String outputOptionValue, JobProto.Job.Builder jobBuilder) {
        Path outputOptionPath = Path.of(outputOptionValue);
        jobBuilder.setOutputOptions(readOutputOptions(outputOptionPath));
    }

    private void handleOutputPrefixOption(String outputPrefixOptionValue, JobProto.Job.Builder jobBuilder) {
        Path outputPrefixOptionPath = Path.of(outputPrefixOptionValue);
        logger.debug("Setting output-prefix to {}", outputPrefixOptionPath);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .setOutputPrefix(outputPrefixOptionPath.toString());
        jobBuilder.setOutputOptions(builder);
    }

    private void handleOutputDirectoryOption(String outputDirectoryOptionValue, JobProto.Job.Builder jobBuilder) {
        Path outputDirectoryOptionPath = Path.of(outputDirectoryOptionValue);
        logger.debug("Setting output-directory to {}", outputDirectoryOptionPath);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .clearOutputPrefix()
                .setOutputDirectory(outputDirectoryOptionPath.toString());
        jobBuilder.setOutputOptions(builder);
    }

    private void handleOutputFileNameOption(String outputFileNameOptionValue, JobProto.Job.Builder jobBuilder) {
        if (outputFileNameOptionValue.contains(System.getProperty("file.separator"))) {
            throw new IllegalArgumentException("output-filename option should not contain a filesystem separator: " + outputFileNameOptionValue);
        }
        logger.debug("Setting output-filename to {}", outputFileNameOptionValue);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .clearOutputPrefix()
                .setOutputFileName(outputFileNameOptionValue);
        jobBuilder.setOutputOptions(builder);
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
        logger.debug("Reading sample from {}", samplePath);
        JobProto.Job.Builder jobBuilder = JobProto.Job.newBuilder();
        SampleProto.Sample sampleProto = tryParseJsonOrYaml(SampleProto.Sample.newBuilder(), samplePath).build();
        if (!sampleProto.equals(SampleProto.Sample.getDefaultInstance())) {
            jobBuilder.setSample(sampleProto);
            return jobBuilder.build();
        }
        //try phenopacket:
        Phenopacket phenopacket = tryParseJsonOrYaml(Phenopacket.newBuilder(), samplePath).build();
        // note that the underlying ProtoParser uses permissive parsing so it is possible to extract an imperfectly
        // formed phenopacket from a family message so these need to be checked before returning.
        if (!phenopacket.equals(Phenopacket.getDefaultInstance()) && !phenopacket.getPhenotypicFeaturesList()
                .isEmpty()) {
            jobBuilder.setPhenopacket(phenopacket);
            return jobBuilder.build();
        }
        //try family:
        Family family = tryParseJsonOrYaml(Family.newBuilder(), samplePath).build();
        if (!family.equals(Family.getDefaultInstance())) {
            jobBuilder.setFamily(family);
            return jobBuilder.build();
        }
        throw new IllegalArgumentException("Unable to parse sample from file " + samplePath + " please check the format");
    }

    private <U extends Message.Builder> U tryParseJsonOrYaml(U messageBuilder, Path path) {
        try {
            return ProtoParser.parseFromJsonOrYaml(messageBuilder, path);
        } catch (Exception exception) {
            logger.debug("{} not parsable as a {} ...", path, messageBuilder.getClass().getName());
        }
        return messageBuilder;
    }

    private AnalysisProto.Preset parsePreset(String presetValue) {
        return switch (presetValue.toLowerCase()) {
            case "exome" -> AnalysisProto.Preset.EXOME;
            case "genome" -> AnalysisProto.Preset.GENOME;
            case "phenotype-only" -> AnalysisProto.Preset.PHENOTYPE_ONLY;
            default -> throw new IllegalArgumentException("Unrecognised preset option: " + presetValue);
        };
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
