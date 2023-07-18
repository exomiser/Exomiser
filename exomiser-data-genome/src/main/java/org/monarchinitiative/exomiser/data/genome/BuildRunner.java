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

package org.monarchinitiative.exomiser.data.genome;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataFactory;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;
import org.monarchinitiative.exomiser.data.genome.config.AssemblyResources;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.SvResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Main logic for building the exomiser genome data distribution.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class BuildRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BuildRunner.class);

    // input options
    public static final String BUILD_TRANSCRIPT = "transcripts";
    public static final String BUILD_CLINVAR = "clinvar";
    public static final String BUILD_VARIANT_DB = "variants";
    public static final String BUILD_GENOME_DB = "genome";
    private static final String MERGE_ONLY = "merge-only";
    private static final String PROCESS_ONLY = "process-only";
    // e.g. --build-dir=/data/exomiser-build/2210 --assembly=hg38 --version=2210 --variants=clinvar,gnomad-genome --just-merge
    private final Path buildDir;
    private final AssemblyResources hg19Resources;
    private final AssemblyResources hg38Resources;
    private final JannovarDataFactory jannovarDataFactory;

    public BuildRunner(Path buildDir, AssemblyResources hg19AssemblyResources, AssemblyResources hg38AssemblyResources, JannovarDataFactory jannovarDataFactory) {
        this.buildDir = buildDir;

        this.hg19Resources = hg19AssemblyResources;
        this.hg38Resources = hg38AssemblyResources;

        this.jannovarDataFactory = jannovarDataFactory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Running {}", args.getOptionNames());
        args.getOptionNames().forEach(name -> logger.info("{}: {}", name, args.getOptionValues(name)));

        // --assembly=hg19
        // --version=1711
        // --build-dir=
        // AND OPTIONALLY
        // --variants
        // OR
        // --variants=uk10k,gnomad-exome,dbnsfp.. [--merge-only || --process-only]
        //  merge-only will merge any existing mv stores into the final variants store
        //  process-only will produce a separate mv store file for each processed variant resource but not merge them
        //  These steps are used for creating the initial stores which can be updated and merged independently to create
        //  the new variants mv.db. The rationale being that the source files (mostly VCF) are usually tens or hundreds
        //  of GB which rarely, if ever get updated from which we only want a few fields that once processed can be
        //  stored in a .mv.db file for a fraction of the disk space and is a lot quicker to further process.
        //  Typically, for a data release only clinvar will have changed, so it's much quicker to process that and add it
        //  as the final 'layer' on top of all the other pre-merged data-sources e.g.:
        // --variants=clinvar (assuming the base {assembly}/variants/processed/{assembly}_variants.mv.db file is present to merge into)
        // --transcripts
        // OR
        // --transcripts=ensembl,ucsc

        if (!args.containsOption("assembly")) {
            throw new IllegalArgumentException("Missing assembly argument");
        }
        String assemblyOption = args.getOptionValues("assembly").get(0);
        GenomeAssembly assembly = GenomeAssembly.parseAssembly(assemblyOption);

        if (!args.containsOption("version")) {
            throw new IllegalArgumentException("Missing version argument");
        }
        String version = args.getOptionValues("version").get(0);

        BuildInfo buildInfo = BuildInfo.of(assembly, version);
        String buildString = buildInfo.getBuildString();

        logger.info("Building version {}", buildString);
        Path outPath = buildDir.resolve(buildString);
        logger.info("Build directory set to {}", outPath);
        if (!outPath.toFile().exists()) {
            Files.createDirectory(outPath);
        }

        AssemblyResources assemblyResources = getAssemblyResourcesForAssembly(assembly);
        Map<String, AlleleResource> alleleResources = assemblyResources.getAlleleResources();

        Set<String> optionalArgs = Set.of(BUILD_TRANSCRIPT, BUILD_CLINVAR, BUILD_VARIANT_DB, BUILD_GENOME_DB);
        if (shouldBuildAllData(args, optionalArgs)) {
            logger.info("BUILDING ALLL THIe THINGS!");
            buildTranscriptData(buildInfo, outPath, List.of(TranscriptSource.values()));
            buildClinVarData(buildInfo, outPath, assemblyResources.getClinVarResource());
            buildVariantData(buildInfo, outPath, new ArrayList<>(alleleResources.values()), assemblyResources.variantProcessedPath());
            buildGenomeData(buildInfo, outPath, assemblyResources);
        }

        if (args.containsOption(BUILD_TRANSCRIPT)) {
            List<String> optionValues = parseOptionValues(args.getOptionValues(BUILD_TRANSCRIPT));
            List<TranscriptSource> transcriptSources = getTranscriptSources(optionValues);
            buildTranscriptData(buildInfo, outPath, transcriptSources);
        }

        if (args.containsOption(BUILD_CLINVAR)) {
            ClinVarAlleleResource clinVarResource = assemblyResources.getClinVarResource();
            buildClinVarData(buildInfo, outPath, clinVarResource);
        }

        // --build-dir=/data/exomiser-build/2210 --assembly=hg38 --version=2210 --variants=gnomad-mito,alfa [--merge-only || --process-only]
        if (args.containsOption(MERGE_ONLY)) {
            mergeVariantData(buildInfo, outPath, assemblyResources.variantProcessedPath());
        } else if (args.containsOption(BUILD_VARIANT_DB)) {
            List<String> optionValues = parseOptionValues(args.getOptionValues(BUILD_VARIANT_DB));
            List<AlleleResource> userDefinedAlleleResources = assemblyResources.getUserDefinedResources(optionValues);
            buildVariantData(buildInfo, outPath, userDefinedAlleleResources, assemblyResources.variantProcessedPath());
            // --build-dir=/data/exomiser-build/2210 --assembly=hg38 --version=2210 --variants=gnomad-mito,alfa --process-only
            if (!args.containsOption(PROCESS_ONLY)) {
                // --build-dir=/data/exomiser-build/2210 --assembly=hg38 --version=2210 --variants=gnomad-mito,alfa
                mergeVariantData(buildInfo, outPath, assemblyResources.variantProcessedPath());
            }
        }

        if (args.containsOption(BUILD_GENOME_DB)) {
            buildGenomeData(buildInfo, outPath, assemblyResources);
        }

        logger.info("Finished build {}", buildInfo.getBuildString());
    }

    private boolean shouldBuildAllData(ApplicationArguments args, Set<String> optionalArgs) {
        for (String arg : args.getOptionNames()) {
            if (optionalArgs.contains(arg)) {
                return false;
            }
        }
        return true;
    }

    private void buildTranscriptData(BuildInfo buildInfo, Path outPath, List<TranscriptSource> transcriptSources) {
        logger.info("Building Jannovar transcript data sources - {}", transcriptSources);
        TranscriptDataBuildRunner transcriptDataBuildRunner = new TranscriptDataBuildRunner(buildInfo, jannovarDataFactory, outPath);
        transcriptDataBuildRunner.run();
    }

    private void buildClinVarData(BuildInfo buildInfo, Path outPath, ClinVarAlleleResource clinVarResource) {
        logger.info("Creating ClinVar database...");
        ResourceDownloader.download(clinVarResource);
        ClinVarBuildRunner clinVarWhiteListBuildRunner = new ClinVarBuildRunner(buildInfo, outPath, clinVarResource);
        clinVarWhiteListBuildRunner.run();
    }

    private void buildVariantData(BuildInfo buildInfo, Path outPath, List<AlleleResource> userDefinedAlleleResources, Path processedPath) {
        logger.info("Downloading variant resources - {}", userDefinedAlleleResources.stream()
                .map(AlleleResource::getName)
                .toList());
        userDefinedAlleleResources.parallelStream().forEach(ResourceDownloader::download);
        logger.info("Building variant database...");
        VariantDatabaseBuildRunner variantDatabaseBuildRunner = new VariantDatabaseBuildRunner(buildInfo, outPath, userDefinedAlleleResources, processedPath);
        variantDatabaseBuildRunner.run();
    }

    private void mergeVariantData(BuildInfo buildInfo, Path outPath, Path variantProcessedPath) {
        logger.info("Merging variant stores...");
        VariantStoreMergeRunner variantStoreMergeRunner = new VariantStoreMergeRunner(buildInfo, outPath, variantProcessedPath);
        variantStoreMergeRunner.run();
    }

    private void buildGenomeData(BuildInfo buildInfo, Path outPath, AssemblyResources assemblyResources) {
        logger.info("Building genome database...");
        Path genomePath = assemblyResources.getGenomeDataPath();
        logger.info("Genome Path: {}", genomePath);
        List<SvResource> svResources = assemblyResources.getSvResources();
        Path genomeProcessedPath = assemblyResources.getGenomeProcessedPath();
        GenomeDatabaseBuildRunner genomeDatabaseBuildRunner = new GenomeDatabaseBuildRunner(buildInfo, genomePath, genomeProcessedPath, outPath, svResources);
        genomeDatabaseBuildRunner.run();
    }

    private List<String> parseOptionValues(List<String> optionValues) {
        Set<String> cleanedOptions = new LinkedHashSet<>();
        for (String optionValue : optionValues) {
            if (optionValue.contains(",")) {
                String[] splitValues = optionValue.split(",");
                for (String value : splitValues) {
                    cleanedOptions.add(value.trim());
                }
            } else {
                cleanedOptions.add(optionValue.trim());
            }
        }
        return new ArrayList<>(cleanedOptions);
    }

    private AssemblyResources getAssemblyResourcesForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19Resources : this.hg38Resources;
    }

    private List<TranscriptSource> getTranscriptSources(List<String> optionValues) {
        if (optionValues.isEmpty()) {
            return Arrays.asList(TranscriptSource.values());
        }
        return optionValues.stream()
                .map(TranscriptSource::parseValue)
                .toList();
    }
}
