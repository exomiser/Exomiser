package org.monarchinitiative.exomiser.cli.command;

import com.google.protobuf.Message;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto.Preset;
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
import picocli.CommandLine.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static picocli.CommandLine.*;

@Command(name = "analysis",
        description = "Runs a single Exomiser analysis.",
        sortOptions = false,
        sortSynopsis = false
)
public final class AnalysisCommand implements JobParserCommand, ExomiserCommand {
    private static final String PHENOPACKET_SCHEMA_URL = "https://phenopacket-schema.readthedocs.io/en/latest";

    private static final Logger logger = LoggerFactory.getLogger(AnalysisCommand.class);

    @Option(names = { "-h", "--help"}, usageHelp = true, description = "Display this help and exit")
    private boolean help;

    @ArgGroup(exclusive = false, order = 1, multiplicity = "1", heading = "Analysis options:%nRequires at least an analysis or a sample to be specified for an analysis to be run. If only specifying the sample, an exome analysis using the default settings will be run (equivalent to specifying --preset exome).%n")
    AnalysisOptions analysisOptions;

    @Option(names = "--preset",
            description = "The Exomiser analysis preset for the input sample. One of 'exome', 'genome' or 'phenotype-only'. (default = exome)",
            converter = PresetConverter.class
    )
    Preset preset = Preset.UNRECOGNIZED;

    @Option(names = "--ped", order = 4, description = "Path to sample PED file. Required for multi-sample VCF files, " +
            "unless included in the sample. The sample option needs to be encoded as a phenopacket-schema `Family` message " +
            "for the PED file to be omitted. See " + PHENOPACKET_SCHEMA_URL + "/family.html")
    Path pedPath;

    @ArgGroup(exclusive = false, order = 3, heading = "VCF input options:%n")
    VcfOptions vcfOptions;

    @Option(names = "--output", description = "Path to outputOptions file. This should be in JSON or YAML format.")
    Path outputOptionsPath;

    @Option(names = "--output-prefix", description = "Path/filename without an extension to be prepended to the output file format options.")
    Path outputPrefixPath;

    @Option(names = "--output-format", description = "A list of comma separated output format(s) e.g. HTML or HTML,JSON." +
            " Valid options include [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]. Note that HTML is the most human-friendly," +
            " JSON is the most detailed. (default = HTML,JSON)", split = ",")
    List<OutputProto.OutputFormat> outputFormats;


    static class AnalysisOptions {
        @Option(names = "--analysis", description = "Path to analysis script file. This should be in YAML format.", hidden = true)
        Path analysisPath;

        @Option(names = "--sample", description = "Path to sample or phenopacket file. This should be in JSON or YAML " +
                "format. See " + PHENOPACKET_SCHEMA_URL + "/phenopacket.html for details. Exomiser " +
                "only requires the `subject` and `phenotypicFeatures` fields of the phenopacket to be present.")
        Path samplePath;

        @Override
        public String toString() {
            return "AnalysisOptions{" +
                    "analysisPath=" + analysisPath +
                    ", samplePath=" + samplePath +
                    '}';
        }
    }

    static class VcfOptions {

        @Option(names = "--vcf", description = "Path to sample VCF file. Also requires 'assembly' option to be defined.", required = true)
        Path vcfPath;

        @Option(names = "--assembly", description = "Genome assembly of sample VCF file. Either 'GRCh37' or 'GRCh38'.", required = true,
                converter = GenomeAssemblyConverter.class)
        GenomeAssembly assembly;

        @Override
        public String toString() {
            return "VcfOptions{" +
                    "vcfPath=" + vcfPath +
                    ", assembly=" + assembly +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AnalysisCommand{" +
                "analysisOptions=" + analysisOptions +
                ", preset=" + preset +
                ", vcfOptions=" + vcfOptions +
                ", pedPath=" + pedPath +
                '}';
    }

    public List<JobProto.Job> parseJobs() {
        if (analysisOptions.analysisPath != null && analysisOptions.samplePath == null) {
            if (Files.notExists(analysisOptions.analysisPath)) {
                throw new IllegalArgumentException("Analysis file not found: " + analysisOptions.analysisPath);
            }
            JobProto.Job job = JobReader.readJob(analysisOptions.analysisPath);
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
        if (analysisOptions.samplePath != null) {
            return handleMultipleUserOptions();
        }
        throw new CommandLineParseError("No sample specified!");
    }

    private List<JobProto.Job> handleMultipleUserOptions() {
        validate();
        JobProto.Job.Builder jobBuilder = newDefaultJobBuilder();
        // parse the analysis first as this could be a legacy analysis (which contains the sample, analysis and output)
        // or it could just be a new analysis without the sample data.
        if (analysisOptions.analysisPath != null) {
            handleAnalysisOption(analysisOptions.analysisPath, jobBuilder);
        }
        if (analysisOptions.samplePath != null) {
            handleSampleOption(analysisOptions.samplePath, jobBuilder);
        }
        if (preset != Preset.UNRECOGNIZED) {
            handlePresetOption(preset, jobBuilder);
        } else if (analysisOptions.analysisPath == null && preset == Preset.UNRECOGNIZED) {
            handlePresetOption(Preset.EXOME, jobBuilder);
        }
        if (outputOptionsPath != null) {
            handleOutputOption(outputOptionsPath, jobBuilder);
        }
        if (outputFormats != null) {
            handleOutputFormatOption(outputFormats, jobBuilder);
        }
        // post-process these optional commands for cases where the user wants to override/add a different VCF or PED
        if (vcfOptions != null) {
            handleVcfAndAssemblyOptions(vcfOptions, jobBuilder);
        }
        if (pedPath != null) {
            handlePedOption(pedPath, jobBuilder);
        }
        if (outputPrefixPath != null) {
            handleOutputPrefixOption(outputPrefixPath, jobBuilder);
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

    private void ensureOutputSettingsSpecifyOutputFormat(JobProto.Job.Builder jobBuilder) {
        if (jobBuilder.getOutputOptions().getOutputFormatsList().isEmpty()) {
            jobBuilder.getOutputOptionsBuilder()
                    .addOutputFormats(OutputFormat.HTML.toString())
                    .addOutputFormats(OutputFormat.JSON.toString());
        }
    }

    private void handleSampleOption(Path samplePath, JobProto.Job.Builder jobBuilder) {
        chekExistsOrThrowError(samplePath);
        // This could be a Sample a Phenopacket or a Family
        JobProto.Job sampleJob = readSampleJob(samplePath);
        jobBuilder.mergeFrom(sampleJob);
    }

    private void
    handleAnalysisOption(Path analysisPath, JobProto.Job.Builder jobBuilder) {
        chekExistsOrThrowError(analysisPath);
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

    private void handleVcfAndAssemblyOptions(VcfOptions vcfOptions, JobProto.Job.Builder jobBuilder) {
        logger.debug("Handling VCF/assembly option {} {}", vcfOptions.vcfPath, vcfOptions.assembly);
        Path vcfPath = vcfOptions.vcfPath;
        chekExistsOrThrowError(vcfPath);

        String assembly = vcfOptions.assembly.toGrcString();
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
        chekExistsOrThrowError(pedPath);

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

    private static void chekExistsOrThrowError(Path path) {
        if (Files.notExists(path)){
            throw new CommandLineParseError(String.format("Specified file '%s' not found", path));
        }
    }

    private void handlePresetOption(AnalysisProto.Preset preset, JobProto.Job.Builder jobBuilder) {
        jobBuilder.setPreset(preset);
    }

    private void handleOutputOption(Path outputOptionPath, JobProto.Job.Builder jobBuilder) {
//        Path outputOptionPath = Path.of(outputOptionValue);
        chekExistsOrThrowError(outputOptionPath);

        jobBuilder.setOutputOptions(readOutputOptions(outputOptionPath));
    }

    private void handleOutputPrefixOption(Path outputPrefixOptionPath, JobProto.Job.Builder jobBuilder) {
//        Path outputPrefixOptionPath = Path.of(outputPrefixOptionValue);
        logger.debug("Setting output-prefix to {}", outputPrefixOptionPath);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .setOutputPrefix(outputPrefixOptionPath.toString());
        jobBuilder.setOutputOptions(builder);
    }

    private void handleOutputFormatOption(List<OutputProto.OutputFormat> formats, JobProto.Job.Builder jobBuilder) {
        logger.debug("Setting output-prefix to {}", formats);
        OutputProto.OutputOptions.Builder builder = jobBuilder
                .getOutputOptions().toBuilder()
                .addAllOutputFormats(formats.stream().map(String::valueOf).toList());
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

    private OutputProto.OutputOptions readOutputOptions(Path outputOptionsPath) {
        OutputProto.OutputOptions outputOptions = ProtoParser.parseFromJsonOrYaml(OutputProto.OutputOptions.newBuilder(), outputOptionsPath)
                .build();
        if (outputOptions.equals(OutputProto.OutputOptions.getDefaultInstance())) {
            throw new IllegalArgumentException("Unable to parse outputOptions from file " + outputOptionsPath + " please check the format");
        }
        return outputOptions;
    }

    static class PresetConverter implements ITypeConverter<AnalysisProto.Preset> {
        @Override
        public Preset convert(String value) throws Exception {
            return switch (value.toLowerCase()) {
                case "exome" -> Preset.EXOME;
                case "genome" -> Preset.GENOME;
                case "phenotype-only" -> Preset.PHENOTYPE_ONLY;
                default -> throw new IllegalArgumentException("Unrecognised preset option: " + value);
            };
        }
    }

    static class GenomeAssemblyConverter implements ITypeConverter<GenomeAssembly> {
        @Override
        public GenomeAssembly convert(String value) throws Exception {
            return GenomeAssembly.parseAssembly(value);
        }
    }

    public void validate() {
        if (analysisOptions.analysisPath != null && preset != Preset.UNRECOGNIZED) {
            throw new CommandLineParseError("preset and analysis options are mutually exclusive");
        }
//        if (commandLine.hasOption("analysis") && commandLine.hasOption("preset")) {
//            throw new CommandLineParseError("preset and analysis options are mutually exclusive");
//        }
//
//        if (commandLine.hasOption("sample") && commandLine.hasOption("assembly") && !commandLine.hasOption("vcf")) {
//            throw new CommandLineParseError("assembly present without vcf option");
//        }
//
//        if (commandLine.hasOption("vcf") && !commandLine.hasOption("assembly")) {
//            throw new CommandLineParseError("--assembly option required when specifying vcf!");
//        }
//        if (commandLine.hasOption("assembly")) {
//            try {
//                GenomeAssembly.parseAssembly(commandLine.getOptionValue("assembly"));
//            } catch (Exception e) {
//                throw new CommandLineParseError(e.getMessage());
//            }
//        }
//
//        if (!hasInputFileOption(commandLine)) {
//            throw new CommandLineParseError("Missing an input file option!");
//        }
//        //check file paths exist before launching.
//        checkFilesExist(commandLine);
    }
}

