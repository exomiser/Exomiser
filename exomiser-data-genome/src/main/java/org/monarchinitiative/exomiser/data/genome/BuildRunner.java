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

package org.monarchinitiative.exomiser.data.genome;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataFactory;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Main logic for building the exomiser hg37 genome data distribution.
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

    private final Path buildDir;
    private final Path hg19GenomePath;
    private final Map<String, AlleleResource> hg19AlleleResources;
    private final Path hg38GenomePath;
    private final Map<String, AlleleResource> hg38AlleleResources;
    private final JannovarDataFactory jannovarDataFactory;

    public BuildRunner(Path buildDir, Path hg19GenomePath, Map<String, AlleleResource> hg19AlleleResources, Path hg38GenomePath, Map<String, AlleleResource> hg38AlleleResources, JannovarDataFactory jannovarDataFactory) {
        this.buildDir = buildDir;

        this.hg19GenomePath = hg19GenomePath;
        this.hg19AlleleResources = hg19AlleleResources;

        this.hg38GenomePath = hg38GenomePath;
        this.hg38AlleleResources = hg38AlleleResources;

        this.jannovarDataFactory = jannovarDataFactory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Running {}", args.getOptionNames());
        args.getOptionNames().forEach(name -> logger.debug("{}: {}", name, args.getOptionValues(name)));

        // --assembly=hg19
        // --version=1711
        // --build-dir=
        // --resources=exac,gnomad-exome

        if (!args.containsOption("assembly")){
            throw new IllegalArgumentException("Missing assembly argument");
        }
        String assemblyOption = args.getOptionValues("assembly").get(0);
        GenomeAssembly assembly = GenomeAssembly.parseAssembly(assemblyOption);

        if (!args.containsOption("version")){
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

        Map<String, AlleleResource> alleleResources = getAlleleResourcesForAssembly(assembly);

        Set<String> optionalArgs = Set.of(BUILD_TRANSCRIPT, BUILD_CLINVAR, BUILD_VARIANT_DB, BUILD_GENOME_DB);
        if (shouldBuildAllData(args, optionalArgs)) {
            logger.info("BUILDING ALLL THIe THINGS!");
            buildTranscriptData(buildInfo, outPath, List.of(TranscriptSource.values()));
            buildClinVarData(buildInfo, outPath, alleleResources.get("clinvar"));
            buildVariantData(buildInfo, outPath, new ArrayList<>(alleleResources.values()));
            buildGenomeData(buildInfo, outPath);
        }

        if (args.containsOption(BUILD_TRANSCRIPT)) {
            List<String> optionValues = parseOptionValues(args.getOptionValues(BUILD_TRANSCRIPT));
            List<TranscriptSource> transcriptSources = getTranscriptSources(optionValues);
            buildTranscriptData(buildInfo, outPath, transcriptSources);
        }

        if (args.containsOption(BUILD_CLINVAR)) {
            AlleleResource clinVarResource = alleleResources.get("clinvar");
            buildClinVarData(buildInfo, outPath, clinVarResource);
        }

        if (args.containsOption(BUILD_VARIANT_DB)) {
            List<String> optionValues = parseOptionValues(args.getOptionValues(BUILD_VARIANT_DB));
            List<AlleleResource> userDefinedAlleleResources = getUserDefinedResources(optionValues, alleleResources);
            buildVariantData(buildInfo, outPath, userDefinedAlleleResources);
        }

        if (args.containsOption(BUILD_GENOME_DB)) {
            buildGenomeData(buildInfo, outPath);
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

    private void buildClinVarData(BuildInfo buildInfo, Path outPath, AlleleResource clinVarResource) {
        logger.info("Creating ClinVar variant whitelist");
        ResourceDownloader.download(clinVarResource);
        ClinVarWhiteListBuildRunner clinVarWhiteListBuildRunner = new ClinVarWhiteListBuildRunner(buildInfo, outPath, clinVarResource);
        clinVarWhiteListBuildRunner.run();
    }

    private void buildVariantData(BuildInfo buildInfo, Path outPath, List<AlleleResource> userDefinedAlleleResources) {
        logger.info("Downloading variant resources - {}", userDefinedAlleleResources.stream()
                .map(AlleleResource::getName)
                .collect(toList()));
        userDefinedAlleleResources.parallelStream().forEach(ResourceDownloader::download);
        logger.info("Building variant database...");
        VariantDatabaseBuildRunner variantDatabaseBuildRunner = new VariantDatabaseBuildRunner(buildInfo, outPath, userDefinedAlleleResources);
        variantDatabaseBuildRunner.run();
    }

    private void buildGenomeData(BuildInfo buildInfo, Path outPath) {
        logger.info("Building genome database...");
        Path genomePath = getGenomePathForAssembly(buildInfo.getAssembly());
        logger.info("Genome Path: {}", genomePath);
        GenomeDatabaseBuildRunner genomeDatabaseBuildRunner = new GenomeDatabaseBuildRunner(buildInfo, genomePath, outPath);
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

    private Path getGenomePathForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19GenomePath : this.hg38GenomePath;
    }

    private Map<String, AlleleResource> getAlleleResourcesForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19AlleleResources : this.hg38AlleleResources;
    }

    private List<TranscriptSource> getTranscriptSources(List<String> optionValues) {
        if (optionValues.isEmpty()) {
            return Arrays.asList(TranscriptSource.values());
        }
        return optionValues.stream()
                .map(TranscriptSource::parseValue)
                .collect(toList());
    }

    private List<AlleleResource> getUserDefinedResources(List<String> optionValues, Map<String, AlleleResource> alleleResources) {
        if (optionValues.isEmpty()) {
            return new ArrayList<>(alleleResources.values());
        }
        logger.info("Creating resources: {}", optionValues);
        return optionValues.stream()
                .filter(alleleResources::containsKey)
                .map(alleleResources::get)
                .collect(toList());
    }
}
