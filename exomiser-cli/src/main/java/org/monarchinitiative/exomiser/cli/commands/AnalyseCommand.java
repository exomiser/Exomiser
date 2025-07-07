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
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Command(name = "analyse", description = "Runs an Exomiser analysis using the parameters provided")
public final class AnalyseCommand implements ExomiserCommand {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseCommand.class);

    @Spec
    CommandSpec spec;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help and exit")
    boolean help;

    @ArgGroup(exclusive = false, heading = "Sample options%n--------------%nThese describe the patient phenotypes, exome/genome and pedigree. It is possible to include all this in a phenopacket `sample` option, or include/override the vcf, assembly and pedigree independently using the specific options.%n")
    SampleOptions sampleOptions = new SampleOptions();

    static class SampleOptions {

        @Option(names = "--sample", description = "Path to sample phenopacket individual or family file. This should be in JSON or YAML format.")
        Path samplePath;

        @Option(names = "--ped", description = "Path to sample PED file. Only required for multisample VCF files, but can be omitted if the --sample option specifies a phenopacket family containing a pedigree.")
        Path pedPath;

        @Override
        public String toString() {
            return "SampleOptions{" +
                   "samplePath=" + samplePath +
                   ", pedPath=" + pedPath +
                   '}';
        }

        public boolean wasSpecified() {
            return samplePath != null || pedPath != null;
        }
    }

    @ArgGroup(exclusive = false, heading = "VCF options%n-----------%nRequired if not included in the sample or will override the VCF path specified in the sample.%n")
    VcfParameters vcfParameters;

    @ArgGroup(multiplicity = "0..1", exclusive = true, heading = "Analysis options%n----------------%nExclusive options used to specify how an analysis should be run. If nothing is specified, the EXOME preset will be run.%n")
    AnalysisGroup analysisGroup = new AnalysisGroup();

    static class AnalysisGroup {

        @Option(names = "--preset", description = "The Exomiser analysis preset for the input sample. One of 'exome', 'genome' or 'phenotype_only'. (default: ${DEFAULT-VALUE})", defaultValue = "EXOME")
        AnalysisProto.Preset preset;

        @Option(names = "--analysis", description = "Path to an Exomiser analysis script")
        Path analysisPath;

        @Option(names = "--job", hidden = true, description = "Path to job file. This should be in JSON or YAML format.")
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

    @ArgGroup(validate = false, heading = "Output options%n--------------%nSpecifies where and in what format Exomiser should output any analysis results. Optional. Will default to writing output files to the `results` directory of the exomiser installation.%n")
    OutputOptions outputOptions = new OutputOptions();

    static class OutputOptions {

        static final List<String> DEFAULT_OUTPUT_FORMATS = List.of(OutputFormat.HTML.toString(), OutputFormat.JSON.toString());

        @Option(names = "--output", description = "Path to outputOptions file. This should be in JSON or YAML format.")
        Path outputOptionsPath;

        @Option(names = "--output-directory", description = "Directory where the output files should be written.")
        Path outputDirectory;

        @Option(names = "--output-filename", description = "Filename prefix for the output files. Will be generated from the input VCF filename if not specified.")
        String outputFilename;

        // Don't specify defaults here as this will break the logic for lots of things. Defaults are set in the readJob() method below.
        @Option(names = "--output-format", description = "A list of comma separated output format(s) e.g. HTML or HTML,JSON. Valid options include [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]. Note that HTML is the most human-friendly, JSON is the most detailed. (default: \"HTML,JSON\")", split = ",")
        List<OutputFormat> outputFormats;

        @Option(names = "--output-prefix", hidden = true, description = "Path/filename without an extension to be prepended to the output file format options." +
                                                                        " This option is EXCLUSIVE to the --output-directory and --output-filename options. DEPRECATED! Use --output-directory and/or --output-filename instead.")
        @Deprecated
        String outputPrefix;

        boolean wasSpecified() {
            return outputOptionsPath != null || outputDirectory != null || outputFilename != null || outputFormats != null;
        }

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

        @Option(names = "--vcf", description = "Path to sample VCF file. Also requires `--assembly` option to be defined.", required = true)
        Path vcfPath;

        @Option(names = "--assembly", description = "Genome assembly of sample VCF file. Either `GRCh37` or `GRCh38`", required = true, converter = GenomeAssemblyConverter.class)
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
        if (sampleOptions.samplePath == null && analysisGroup.analysisPath == null && analysisGroup.jobPath == null) {
            throw new ParameterException(spec.commandLine(), "No sample specified!");
        }
        if (analysisGroup.jobPath != null && (vcfParameters != null || sampleOptions.wasSpecified() || outputOptions.wasSpecified())) {
            throw new MutuallyExclusiveArgsException(spec.commandLine(), "--job option is exclusive to all other options!");
        }
        if (analysisGroup.jobPath != null) {
            return JobReader.readJob(analysisGroup.jobPath);
        }
        return handleMultipleUserOptions();
    }

    private JobProto.Job handleMultipleUserOptions() {
        JobProto.Job.Builder jobBuilder = newDefaultJobBuilder();

        // parse the analysis first as this could be a legacy analysis (which contains the sample, analysis and output)
        // or it could just be a new analysis without the sample or output options data.
        if (analysisGroup.analysisPath != null) {
            handleAnalysisOption(analysisGroup.analysisPath, jobBuilder);
        }
        // analysisGroup.preset is EXOME by default
        if (analysisGroup.analysisPath == null && analysisGroup.jobPath == null) {
            handlePresetOption(analysisGroup.preset, jobBuilder);
        }
        // handle SampleOptions
        if (sampleOptions.samplePath != null) {
            handleSampleOption(sampleOptions.samplePath, jobBuilder);
        }
        // post-process these optional commands for cases where the user wants to override/add a different VCF or PED
        handleVcfAndPedOptions(vcfParameters, sampleOptions.pedPath, jobBuilder);
        // Handle OutputOptions
        // Make sure Exomiser will return some results!
        handleOutputOptions(outputOptions, jobBuilder);

        if (!jobBuilder.hasSample() && !jobBuilder.hasPhenopacket() && !jobBuilder.hasFamily()) {
            throw new ParameterException(spec.commandLine(), "Missing --sample option!");
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
                .setNumGenes(0)
                .setOutputContributingVariantsOnly(false)
                // Don't add default output options here as this causes issues...
                .build();
    }

    private void handleVcfAndPedOptions(VcfParameters vcfParameters, Path pedPath, JobProto.Job.Builder jobBuilder) {
        if (vcfParameters != null) {
            handleVcfAndAssemblyOptions(vcfParameters.vcfPath, vcfParameters.assembly, jobBuilder);
        }
        if (pedPath != null) {
            handlePedOption(pedPath, jobBuilder);
        }
    }

    private void handleOutputOptions(OutputOptions outputOptions, JobProto.Job.Builder jobBuilder) {
        logger.debug("Handling output options {}", outputOptions);
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
        handleOutputFormat(outputOptions.outputFormats, jobBuilder);
    }

    private void handleSampleOption(Path samplePath, JobProto.Job.Builder jobBuilder) {
        // This could be a Sample a Phenopacket or a Family
        JobProto.Job sampleJob = readSampleJob(samplePath);
        jobBuilder.mergeFrom(sampleJob);
    }

    private void handleAnalysisOption(Path analysisPath, JobProto.Job.Builder jobBuilder) {
        AnalysisProto.Analysis analysis = readAnalysis(analysisPath);
        if (!AnalysisProto.Analysis.getDefaultInstance().equals(analysis)) {
            logger.debug("New analysis format file: {}", analysisPath);
            jobBuilder.setAnalysis(analysis);
        } else {
            try {
                JobProto.Job job = JobReader.readJob(analysisPath);
                if (job.hasSample() || job.hasPhenopacket() || job.hasFamily()) {
                    logger.debug("Legacy analysis format file: {}", analysisPath);
                    jobBuilder.clear().mergeFrom(job);
                }
            } catch (IllegalArgumentException e) {
                // not a legacy analysis job
                logger.error("{}", e.getMessage());
            }
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

    private void handleOutputFormat(List<OutputFormat> outputFormatOptions, JobProto.Job.Builder jobBuilder) {
        // override any settings from an analysis file referring to other output_formats
        OutputProto.OutputOptions.Builder optionsBuilder = jobBuilder.getOutputOptionsBuilder();
        if (optionsBuilder.getOutputFormatsList().isEmpty() && (outputFormatOptions == null || outputFormatOptions.isEmpty())) {
            // add defaults as none are present
            optionsBuilder.addAllOutputFormats(OutputOptions.DEFAULT_OUTPUT_FORMATS);
            logger.debug("Output formats - none specified, using defaults: {}", optionsBuilder);
        } else if (outputFormatOptions == null || outputFormatOptions.isEmpty()) {
            // should be set in YAML
            logger.debug("Output formats - using YAML specified options: {}", optionsBuilder);
        } else {
            // override jobBuilder formats with those provided from CLI
            optionsBuilder.clearOutputFormats();
            Set<String> outputFormats = outputFormatOptions.stream()
                    .map(OutputFormat::toString)
                    .collect(Collectors.toSet());
            optionsBuilder.addAllOutputFormats(outputFormats);
            logger.debug("Output formats - using CLI specified options: {}", optionsBuilder);
        }
    }

    private void handleOutputOption(Path outputOptionsPath, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setOutputOptions(readOutputOptions(outputOptionsPath));
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
        var parseResult = tryParseJsonOrYaml(AnalysisProto.Analysis.newBuilder(), analysisPath);
        if (parseResult.isErr()) {
            throw new IllegalArgumentException("Unable to parse analysis from file " + analysisPath + " please check the format.", parseResult.err());
        }
        return parseResult.ok().build();
    }

    private JobProto.Job readSampleJob(Path samplePath) {
        logger.debug("Reading sample from {}", samplePath);
        JobProto.Job.Builder jobBuilder = JobProto.Job.newBuilder();
        Result<SampleProto.Sample.Builder, Exception> sampleParseResult = tryParseJsonOrYaml(SampleProto.Sample.newBuilder(), samplePath);
        if (sampleParseResult.isOk()) {
            SampleProto.Sample sampleProto = sampleParseResult.ok().build();
            if (!SampleProto.Sample.getDefaultInstance().equals(sampleProto)) {
                jobBuilder.setSample(sampleProto);
                return jobBuilder.build();
            }
        }
        //try phenopacket:
        var phenopacketResult = tryParseJsonOrYaml(Phenopacket.newBuilder(), samplePath);
        if (phenopacketResult.isOk()) {
            Phenopacket phenopacket = phenopacketResult.ok().build();
            // note that the underlying ProtoParser uses permissive parsing so it is possible to extract an imperfectly
            // formed phenopacket from a family message so these need to be checked before returning.
            if (!Phenopacket.getDefaultInstance().equals(phenopacket) && !phenopacket.getPhenotypicFeaturesList()
                    .isEmpty()) {
                jobBuilder.setPhenopacket(phenopacket);
                return jobBuilder.build();
            }
        }
        //try family:
        var familyResult = tryParseJsonOrYaml(Family.newBuilder(), samplePath);
        if (familyResult.isOk()) {
            Family family = familyResult.ok().build();
            if (!Family.getDefaultInstance().equals(family)) {
                jobBuilder.setFamily(family);
                return jobBuilder.build();
            }
        }
        throw new IllegalArgumentException("Unable to parse sample from file " + samplePath + " please check the format");
    }

    private OutputProto.OutputOptions readOutputOptions(Path outputOptionsPath) {
        var outputOpionsParseResult = tryParseJsonOrYaml(OutputProto.OutputOptions.newBuilder(), outputOptionsPath);
        if (outputOpionsParseResult.isOk()) {
            OutputProto.OutputOptions outputOptions = outputOpionsParseResult.ok().build();
            if (OutputProto.OutputOptions.getDefaultInstance().equals(outputOptions)) {
                throw new IllegalArgumentException("Unable to parse outputOptions from file " + outputOptionsPath + " please check the format");
            }
            return outputOptions;
        }
        throw new IllegalArgumentException("Unable to parse outputOptions from file " + outputOptionsPath + " please check the format");
    }

    private <U extends Message.Builder> Result<U, Exception> tryParseJsonOrYaml(U messageBuilder, Path path) {
        try {
            return Result.ok(ProtoParser.parseFromJsonOrYaml(messageBuilder, path));
        } catch (Exception exception) {
            logger.debug("{} not parsable as a {} ...", path, messageBuilder.getClass().getName());
            return Result.err(exception);
        }
    }
}