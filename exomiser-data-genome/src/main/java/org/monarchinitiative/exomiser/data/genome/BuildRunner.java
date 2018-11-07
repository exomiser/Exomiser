/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main logic for building the exomiser hg37 genome data distribution.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class BuildRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BuildRunner.class);

    private final Path buildDir;
    private final Path hg19GenomePath;
    private final Map<String, AlleleResource> hg19AlleleResources;
    private final Path hg38GenomePath;
    private final Map<String, AlleleResource> hg38AlleleResources;

    public BuildRunner(Path buildDir, Path hg19GenomePath, Map<String, AlleleResource> hg19AlleleResources, Path hg38GenomePath, Map<String, AlleleResource> hg38AlleleResources) {
        this.buildDir = buildDir;

        this.hg19GenomePath = hg19GenomePath;
        this.hg19AlleleResources = hg19AlleleResources;

        this.hg38GenomePath = hg38GenomePath;
        this.hg38AlleleResources = hg38AlleleResources;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Running {}", args.getOptionNames());
        args.getOptionNames().forEach(name -> logger.debug("{}: {}", name, args.getOptionValues(name)));

        // --assembly=hg19
        // --version=1711
        // --resources=exac,gnomad-exome
        // --build-dir=

        if (!args.containsOption("assembly")){
            throw new IllegalArgumentException("Missing assembly argument");
        }
        String assemblyOption = args.getOptionValues("assembly").get(0);
        GenomeAssembly assembly = GenomeAssembly.fromValue(assemblyOption);

        if (!args.containsOption("version")){
            throw new IllegalArgumentException("Missing version argument");
        }
        String version = args.getOptionValues("version").get(0);

        BuildInfo buildInfo = BuildInfo.of(assembly, version);
        String buildString = buildInfo.getBuildString();

        logger.info("Build set to {}", buildString);
        Path outPath = buildDir.resolve(buildString);
        logger.info("Build directory set to {}", outPath);
        if (!outPath.toFile().exists()) {
            Files.createDirectory(outPath);
        }

        logger.info("Building {}", buildInfo.getBuildString());
        Path genomePath = getGenomePathForAssembly(assembly);
        logger.info("Genome Path: {}", genomePath);
        Map<String, AlleleResource> alleleResources = getAlleleResourcesForAssembly(assembly);

        List<AlleleResource> userDefinedAlleleResources = getUserDefinedResources(args, alleleResources);

        logger.info("Downloading variant resources");
        userDefinedAlleleResources.parallelStream().forEach(AlleleResourceDownloader::download);

        logger.info("Building variant database...");
        VariantDatabaseBuildRunner variantDatabaseBuildRunner = new VariantDatabaseBuildRunner(buildInfo, outPath, userDefinedAlleleResources);
        variantDatabaseBuildRunner.run();

        logger.info("Building genome database...");
        GenomeDatabaseBuildRunner genomeDatabaseBuildRunner = new GenomeDatabaseBuildRunner(buildInfo, genomePath, outPath);
        genomeDatabaseBuildRunner.run();

        //build jannovar - can this be done via an API?
        //zip build archive
        // this should work but there are logging conflicts and it simply fails without an error
//        logger.info("Building Jannovar data... ");
//        logger.info("Jannovar version: {}", Jannovar.getVersion());
//        String[] arguments = new String[] {"download",  "-d", "hg19/refseq"};
//        Jannovar.main(arguments);
        // Convert to new proto format:
//        List<String> resourcesNames = ImmutableList.of("ensembl", "refseq", "ucsc");
//
//        resourcesNames.parallelStream().forEach(resourceName -> {
//            System.out.printf("Converting %s_%s%n", assembly, resourceName);
//            String inputName = String.format("%s_%s.ser", assembly, resourceName);
//            JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(Paths.get(inputName));
//            String outputName = String.format("%s_transcripts_%s.ser", buildString, resourceName);
//            JannovarDataProtoSerialiser.save(Paths.get(outputName), jannovarData);
//        });


        logger.info("Finished build {}", buildInfo.getBuildString());
    }

    private Path getGenomePathForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19GenomePath : this.hg38GenomePath;
    }

    private Map<String, AlleleResource> getAlleleResourcesForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19AlleleResources : this.hg38AlleleResources;
    }

    private List<AlleleResource> getUserDefinedResources(ApplicationArguments args, Map<String, AlleleResource> alleleResources) {
        if (args.containsOption("resources")) {
            List<String> resources = args.getOptionValues("resources");
            logger.info("Creating resources: {}", resources.get(0));
            return Arrays.stream(resources.get(0).split(","))
                    .filter(alleleResources::containsKey)
                    .map(alleleResources::get)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(alleleResources.values());
    }

}
