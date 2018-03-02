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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main logic for building the exomiser hg37 genome data distribution.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class Hg37BuildRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Hg37BuildRunner.class);

    private static final GenomeAssembly ASSEMBLY = GenomeAssembly.HG19;

//    private final Path dataPath;
//    private Path outPath;
    private final List<AlleleResource> alleleResources;

    public Hg37BuildRunner(List<AlleleResource> hg19AlleleResources) {
//        this.dataPath = hg19DataPath;
//        this.outPath = hg19OutPath;
        this.alleleResources = hg19AlleleResources;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //--assembly=hg19 --working-dir= --zip=false --download=true

        Path dataPath = Paths.get("C:/Users/hhx640/Documents/exomiser-build/hg19");
        Path outPath = Paths.get("C:/Users/hhx640/Documents/exomiser-build/hg19");

        String buildString = ASSEMBLY.toString();

        if (args.containsOption("version")) {
            List<String> version = args.getOptionValues("version");
            buildString = version.get(0) + "_" + ASSEMBLY;
            logger.info("Build set to {}", buildString);
            outPath = dataPath.resolve(buildString);
            if (!outPath.toFile().exists()) {
                Files.createDirectory(outPath);
            }
        }

        logger.info("{}", args.getNonOptionArgs());
        if (args.getNonOptionArgs().contains("-hg19")) {
            logger.info("Building {}", ASSEMBLY);

            //ClinVar? ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh37/
            //http://ftp.ensembl.org/pub/data_files/homo_sapiens/GRCh38/variation_genotype/
            //ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/VCF/00-All.vcf.gz
            //ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh38p7/VCF/00-All.vcf.gz

            VariantDatabaseBuildRunner variantDatabaseBuildRunner = new VariantDatabaseBuildRunner(outPath, buildString, alleleResources);
            variantDatabaseBuildRunner.run();

//            EnsemblEnhancerParser ensemblEnhancerParser = new EnsemblEnhancerParser(dataPath.resolve("genome"), dataPath.resolve("genome"));
//            ensemblEnhancerParser.download();
//            ensemblEnhancerParser.parse();

//            logger.info("Parsing FANTOM 5 enhancers");
//            FantomEnhancerToBedConverter fantomEnhancerParser = new FantomEnhancerToBedConverter(dataPath.resolve("genome"), dataPath.resolve("genome"));
//            fantomEnhancerParser.download();
//            fantomEnhancerParser.run();

//            //build genome.h2.db
//            GenomeDatabaseBuildRunner genomeDatabaseBuildRunner = new GenomeDatabaseBuildRunner(buildString, dataPath.resolve("genome"), outPath);
//            genomeDatabaseBuildRunner.run();

            //build jannovar - can this be done via an API?
            //zip build archive
            logger.info("Finished build {}", buildString);
        }

    }

}
