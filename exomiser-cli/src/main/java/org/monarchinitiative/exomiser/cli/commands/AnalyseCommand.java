package org.monarchinitiative.exomiser.cli.commands;


import com.google.protobuf.Message;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.cli.CommandLineParseError;
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
import picocli.CommandLine;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@CommandLine.Command(name = "analyse", description = "Runs an Exomiser analysis using the parameters provided")
public final class AnalyseCommand implements ExomiserCommand {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseCommand.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help and exit")
    boolean help;

    @CommandLine.ArgGroup(validate = true, heading = "Sample options%n--------------%nThese describe the patient phenotypes, exome/genome and pedigree. It is possible to include all this in a phenopacket `sample` option, or include/override the vcf, assembly and pedigree independently using the specific options.%n")
    SampleOptions sampleOptions = new SampleOptions();

    static class SampleOptions {

        @CommandLine.Option(names = "--sample", description = "Path to sample or phenopacket file. This should be in JSON or YAML format.")
        Path samplePath;

        @CommandLine.Option(names = "--ped", description = "Path to sample PED file.")
        Path pedPath;

        @Override
        public String toString() {
            return "SampleOptions{" +
                   "samplePath=" + samplePath +
                   ", pedPath=" + pedPath +
                   '}';
        }
    }

    @CommandLine.ArgGroup(exclusive = false, heading = "VCF options%n-----------%nRequired if not included in the sample or will override the VCF path specified in the sample.%n")
    VcfParameters vcfParameters;

    @CommandLine.ArgGroup(multiplicity = "0..1", exclusive = true, heading = "Analysis options%n----------------%nExclusive options used to specify how an analysis should be run. If nothing is specified, the EXOME preset will be run.%n")
    AnalysisGroup analysisGroup = new AnalysisGroup();

    static class AnalysisGroup {

        @CommandLine.Option(names = "--preset", description = "The Exomiser analysis preset for the input sample. One of 'exome', 'genome' or 'phenotype_only'. (default: ${DEFAULT-VALUE})", defaultValue = "EXOME")
        AnalysisProto.Preset preset;

        @CommandLine.Option(names = "--analysis", description = "Path to an Exomiser analysis script")
        Path analysisPath;

        @CommandLine.Option(names = "--job", hidden = true, description = "Path to job file. This should be in JSON or YAML format.")
        Path jobPath;

        @Override
        public String toString() {
            return "AnalysisGroup{" +
                   "preset=" + preset +
                   ", analysisPath=" + analysisPath +
                   ", jobPath=" + jobPath +
                   '}';
        }
    }

    @CommandLine.ArgGroup(validate = false, heading = "Output options%n--------------%nSpecifies where and in what format Exomiser should output any analysis results. Optional. Will default to writing output files to the `results` directory of the exomiser installation.%n")
    OutputOptions outputOptions = new OutputOptions();

    static class OutputOptions {

        @CommandLine.Option(names = "--output", description = "Path to outputOptions file. This should be in JSON or YAML format.")
        Path outputOptionsPath;

        @CommandLine.Option(names = "--output-directory", description = "Directory where the output files should be written.")
        Path outputDirectory;

        @CommandLine.Option(names = "--output-filename", description = "Filename prefix for the output files. Will be generated from the input VCF filename if not specified.")
        String outputFilename;

        @CommandLine.Option(names = "--output-format", description = "A list of comma separated output format(s) e.g. HTML or HTML,JSON. Valid options include [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]. Note that HTML is the most human-friendly, JSON is the most detailed. (default: ${DEFAULT-VALUE})", split = ",", defaultValue = "HTML,JSON")
        List<OutputFormat> outputFormats;

        @CommandLine.Option(names = "--output-prefix", hidden = true, description = "Path/filename without an extension to be prepended to the output file format options." +
                                                                                    " This option is EXCLUSIVE to the --output-directory and --output-filename options. DEPRECATED! Use --output-directory and/or --output-filename instead.")
        @Deprecated
        String outputPrefix;

        @Override
        public String toString() {
            return "OutputOptions{" +
                   "outputOptionsPath=" + outputOptionsPath +
                   ", outputDirectory=" + outputDirectory +
                   ", outputFilename='" + outputFilename + '\'' +
                   ", outputFormats=" + outputFormats +
                   '}';
        }
    }

    @Override
    public String toString() {
        return "AnalyseCommand{" +
               sampleOptions +
               ", " + analysisGroup +
               ", " + outputOptions +
               '}';
    }

    static class VcfParameters {

        @CommandLine.Option(names = "--vcf", description = "Path to sample VCF file. Also requires `--assembly` option to be defined.", required = true)
        Path vcfPath;

        @CommandLine.Option(names = "--assembly", description = "Genome assembly of sample VCF file. Either `GRCh37` or `GRCh38`", required = true, converter = GenomeAssemblyConverter.class)
        GenomeAssembly assembly;

        @Override
        public String toString() {
            return "VcfParameters{" +
                   "vcfPath=" + vcfPath +
                   ", assembly='" + assembly + '\'' +
                   '}';
        }
    }

    @Override
    public boolean validate() {
        try {
            readJob();
            return true;
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return false;
    }

    public JobProto.Job readJob() {
        // old cli option - expect an old-style analysis where the sample is specified in the analysis
        // this is maintained for backwards-compatibility
        if (analysisGroup.analysisPath != null && sampleOptions.samplePath == null) {
            Path analysisPath = analysisGroup.analysisPath;
            return JobReader.readJob(analysisPath);
        } else if (analysisGroup.jobPath != null) {
            return JobReader.readJob(analysisGroup.jobPath);
        }
        return handleMultipleUserOptions();
    }

    private JobProto.Job handleMultipleUserOptions() {
        JobProto.Job.Builder jobBuilder = newDefaultJobBuilder();

        // parse the analysis first as this could be a legacy analysis (which contains the sample, analysis and output)
        // or it could just be a new analysis without the sample data.
        if (analysisGroup.analysisPath != null) {
            handleAnalysisOption(analysisGroup.analysisPath, jobBuilder);
        }
        if (analysisGroup.analysisPath == null && analysisGroup.jobPath == null) {
            handlePresetOption(analysisGroup.preset, jobBuilder);
        }

        // handle SampleOptions
        if (sampleOptions.samplePath != null) {
            handleSampleOption(sampleOptions.samplePath, jobBuilder);
        }
        // post-process these optional commands for cases where the user wants to override/add a different VCF or PED
//        VcfParameters vcfParameters = sampleOptions.vcfParameters;
        if (vcfParameters != null) {
            handleVcfAndAssemblyOptions(vcfParameters.vcfPath, vcfParameters.assembly, jobBuilder);
        }
        if (sampleOptions.pedPath != null) {
            handlePedOption(sampleOptions.pedPath, jobBuilder);
        }
        // Handle OutputOptions
        if (outputOptions.outputOptionsPath != null) {
            handleOutputOption(outputOptions.outputOptionsPath, jobBuilder);
        }
        if (outputOptions.outputDirectory != null) {
            handleOutputDirectoryOption(outputOptions.outputDirectory, jobBuilder);
        }
        if (outputOptions.outputFilename != null && !outputOptions.outputFilename.isEmpty()) {
            handleOutputFileNameOption(outputOptions.outputFilename, jobBuilder);
        }
        if (outputOptions.outputPrefix != null) {
            logger.warn("Use of deprecated --output-prefix option - ignoring value");
        }
        // Make sure Exomiser will return some results! Adding defaults to the initial builder will result in potential
        // duplicates of the default output file types.
        handleOutputFormat(outputOptions.outputFormats, jobBuilder);
//        ensureOutputSettingsSpecifyOutputFormat(jobBuilder);

        if (!jobBuilder.hasSample() && !jobBuilder.hasPhenopacket() && !jobBuilder.hasFamily()) {
            throw new CommandLineParseError("No sample specified!");
        }

        logger.debug("Built Exomiser job: {}", jobBuilder);
        return jobBuilder.build();
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

    private void handleOutputFormat(List<OutputFormat> outputFormatOptions, JobProto.Job.Builder jobBuilder) {
        // override any settings from an analysis file referring to other output_formats
        OutputProto.OutputOptions.Builder optionsBuilder = jobBuilder.getOutputOptionsBuilder();
        optionsBuilder.clearOutputFormats();
        Set<String> outputFormats = outputFormatOptions.stream()
                .map(OutputFormat::toString)
                .collect(Collectors.toSet());
        optionsBuilder.addAllOutputFormats(outputFormats);
    }

    private void ensureOutputSettingsSpecifyOutputFormat(JobProto.Job.Builder jobBuilder) {
        if (jobBuilder.getOutputOptions().getOutputFormatsList().isEmpty()) {
            jobBuilder.getOutputOptionsBuilder()
                    .addOutputFormats(OutputFormat.HTML.toString())
                    .addOutputFormats(OutputFormat.JSON.toString());
        }
    }

    private void handleSampleOption(Path samplePath, JobProto.Job.Builder jobBuilder) {
        // This could be a Sample a Phenopacket or a Family
        JobProto.Job sampleJob = readSampleJob(samplePath);
        jobBuilder.mergeFrom(sampleJob);
    }

    private void handleAnalysisOption(Path analysisPath, JobProto.Job.Builder jobBuilder) {
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

    private void handleVcfAndAssemblyOptions(Path vcfPath, GenomeAssembly genomeAssembly, JobProto.Job.Builder jobBuilder) {
        logger.debug("Handling VCF/assembly option {} {}", vcfPath, genomeAssembly);
        if (genomeAssembly == null) {
            // Using the incorrect assembly would lead to *very* incorrect results.
            throw new CommandLineParseError("assembly must be included when specifying vcf!");
        }
        String assembly = genomeAssembly.toGrcString();
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

    private void handlePedOption(Path pedPath, JobProto.Job.Builder jobBuilder) {
        logger.debug("Got a PED option {}", pedPath);
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

    private void handlePresetOption(AnalysisProto.Preset preset, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setPreset(preset);
    }

    private void handleOutputOption(Path outputOptionsPath, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setOutputOptions(readOutputOptions(outputOptionsPath));
    }

    private void handleOutputPrefixOption(String outputPrefixOptionValue, JobProto.Job.Builder jobBuilder) {
        Path outputPrefixOptionPath = Path.of(outputPrefixOptionValue);
        logger.debug("Setting output-prefix to {}", outputPrefixOptionPath);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .setOutputPrefix(outputPrefixOptionPath.toString());
        jobBuilder.setOutputOptions(builder);
    }

    private void handleOutputDirectoryOption(Path outputDirectoryOptionPath, JobProto.Job.Builder jobBuilder) {
        logger.debug("Setting output-directory to {}", outputDirectoryOptionPath);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .clearOutputPrefix()
                .setOutputDirectory(outputDirectoryOptionPath.toString());
        jobBuilder.setOutputOptions(builder);
    }

    private void handleOutputFileNameOption(String outputFileNameOptionValue, JobProto.Job.Builder jobBuilder) {
        if (outputFileNameOptionValue.contains(FileSystems.getDefault().getSeparator())) {
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