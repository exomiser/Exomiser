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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.indexers.OutputFileIndexer;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;
import org.monarchinitiative.exomiser.data.genome.model.archive.TabixArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.genome.EnsemblEnhancerParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.genome.FantomEnhancerParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.*;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main class for handling downloading, parsing and migrating the non-allele resources for the genome database.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDatabaseBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(GenomeDatabaseBuildRunner.class);

    private final BuildInfo buildInfo;
    private final Path genomeDataPath;
    private final Path outputPath;

    public GenomeDatabaseBuildRunner(BuildInfo buildInfo, Path genomeDataPath, Path outputPath) {
        this.buildInfo = buildInfo;
        this.genomeDataPath = genomeDataPath;
        this.outputPath = outputPath;
    }

    public void run() {

        logger.info("Parsing ENSEMBL enhancers...");
        Path ensemblEnhancersFile = genomeDataPath.resolve("ensembl_enhancers.tsv");
        String martQuery = getMartQueryString("genome/ensembl_enhancer_biomart_query.xml");
//        downloadEnsemblEnhancers(buildInfo.getAssembly(), martQuery, ensemblEnhancersFile);
        EnsemblEnhancerParser ensemblEnhancerParser = new EnsemblEnhancerParser(ensemblEnhancersFile, genomeDataPath.resolve("ensembl_enhancers.pg"));
        ensemblEnhancerParser.parse();

        logger.info("Parsing FANTOM 5 enhancers...");
        Path fantomBedPath = genomeDataPath.resolve("fantom_enhancers.bed");
        downloadClassPathResource(String.format("genome/%s_fantom_permissive_enhancer_usage.bed", buildInfo.getAssembly()), fantomBedPath);

        FantomEnhancerParser fantomEnhancerParser = new FantomEnhancerParser(fantomBedPath, genomeDataPath.resolve("fantom_enhancers.pg"));
        fantomEnhancerParser.parse();

        // extract hg19_tad.pg from the jar to genomeDataPath as tad.pg
        logger.info("Extracting TAD resource...");
        downloadClassPathResource(String.format("genome/%s_tad.pg", buildInfo.getAssembly()), genomeDataPath.resolve("tad.pg"));

        // can do SV build here
        SvFrequencyResource dbVarResource = dbVarFrequencyResource();
        SvFrequencyResource gnomadSvResource = gnomadSvFrequencyResource();
        SvFrequencyResource gonlSvResource = gonlSvFrequencyResource();
        SvFrequencyResource dgvSvResource = dgvSvResource();
        SvFrequencyResource decipherSvResource = decipherSvResource();

        SvPathogenicityResource clinVarSvResource = clinvarSvResource();

        List<SvResource<?>> svFrequencyResources = List.of(
                clinVarSvResource,
                dbVarResource,
                gnomadSvResource,
                gonlSvResource,
                dgvSvResource,
                decipherSvResource
        );


        // download sv/genome resources - e.g. dbvar, gnomAD-SV, gnomAD pLOF
        // TODO: Do this on a dedicated thread pool
        svFrequencyResources.parallelStream().forEach(ResourceDownloader::download);
        // process with something like an SvAlleleWriter - write out one .pg file per resource - very similar to the phenotype build process
        // dbvar, dgv, decipher, gonl, gnomad-sv
        //        alleleResources.forEach(alleleIndexer::index);
        // read in resources as a migration and do mega union all sort SQL in migration script to produce the
        // sv_freq and sv_path tables
        // e.g. V1.3.0__insert_sv_freq
        // e.g. V1.3.1__insert_sv_path
        // truncate and drop original resource tables

        // TODO: ADD gnomAD pLOF, pLI, HI, triplosensitivity, genic intolerance, gene constraint scores.
        //                    // TODO: do we want to do this? Don't we want each resource to handle its own sources, parsing and writing?
        //                    //  e.g. dbVar has 3-4 nr_ source files, clinVar has one file for both assemblies and only the correct
        //                    //  lines should be converted and written.
        //                    try (OutputFileIndexer indexer = new OutputFileIndexer(svFrequencyResource)) {
        //                        indexer.index(svFrequencyResource);
        //                    } catch (IOException e) {
        //                        throw new IllegalStateException(e.getMessage());
        //                    }
        svFrequencyResources.parallelStream()
                .forEach(SvResource::indexResource);


        //build genome.h2.db
        Path databasePath = outputPath.resolve(String.format("%s_genome", buildInfo.getBuildString()));
        DataSource dataSource = createDataSource(databasePath);
        logger.info("Created database: {}", databasePath);
        migrateDatabase(dataSource);
        logger.info("Finished importing genome data");
    }

    private DbVarSvResource dbVarFrequencyResource() {
        try {
            return new DbVarSvResource("hg19.dbvar",
                    new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/dbVar/data/Homo_sapiens/by_assembly/GRCh37/vcf/GRCh37.variant_call.all.vcf.gz"),
                    new TabixArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/GRCh37.variant_call.all.vcf.gz")),
                    new DbVarFreqParser(),
                    new OutputFileIndexer<>(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/dbvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private GnomadSvResource gnomadSvFrequencyResource() {
        try {
            // https://doi.org/10.1038/s41586-020-2287-8
            //
            return new GnomadSvResource("hg19.gnomad-sv",
                    new URL("https://storage.googleapis.com/gnomad-public/papers/2019-sv/gnomad_v2.1_sv.sites.vcf.gz"),
                    new TabixArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/gnomad_v2.1_sv.sites.vcf.gz")),
                    new GnomadSvVcfFreqParser(),
                    new OutputFileIndexer<>(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/gnomad-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private GonlSvResource gonlSvFrequencyResource() {
        try {
            return new GonlSvResource("hg19.gonl",
                    new URL("http://molgenis26.target.rug.nl/downloads/gonl_public/variants/release6.1/20161013_GoNL_AF_genotyped_SVs.vcf.gz"),
                    new FileArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/20161013_GoNL_AF_genotyped_SVs.vcf.gz")),
                    new GonlSvFreqParser(),
                    new OutputFileIndexer<>(genomeDataPath.resolve("gonl-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private ClinVarSvResource clinvarSvResource() {
        try {
            return new ClinVarSvResource("hg19.clinvar-sv",
                    new URL("https://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz"),
                    new FileArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/variant_summary.txt.gz")),
                    new ClinVarSvParser(GenomeAssembly.HG19),
                    new OutputFileIndexer<>(genomeDataPath.resolve("clinvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private DgvSvResource dgvSvResource() {
        try {
            return new DgvSvResource("hg19.dgv-sv",
                    new URL("http://dgv.tcag.ca/dgv/docs/GRCh37_hg19_variants_2020-02-25.txt"),
                    new FileArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/dgv-hg19-variants-2020-02-25.txt")),
                    new DgvSvFreqParser(),
                    new OutputFileIndexer<>(genomeDataPath.resolve("dgv-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private DecipherSvResource decipherSvResource() {
        try {
            return new DecipherSvResource("hg19.decipher-sv",
                    new URL("https://www.deciphergenomics.org/files/downloads/population_cnv_grch37.txt.gz"),
                    new FileArchive(Path.of("/home/hhx640/Documents/exomiser-build/hg19/genome/decipher_population_cnv_grch37.txt.gz")),
                    new DecipherSvFreqParser(),
                    new OutputFileIndexer<>(genomeDataPath.resolve("decipher-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getMartQueryString(String martQueryResourcePath) {
        try {
            Resource martQuery = new ClassPathResource(martQueryResourcePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(martQuery.getInputStream()))) {
                String xml = reader.lines().collect(Collectors.joining("\n"));
                return URLEncoder.encode(xml, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return null;
    }

    private void downloadEnsemblEnhancers(GenomeAssembly genomeAssembly, String martQuery, Path destination) {
        if (genomeAssembly == GenomeAssembly.HG19) {
            downloadResource("http://grch37.ensembl.org/biomart/martservice?query=" + martQuery, destination);
        } else {
            downloadResource("http://ensembl.org/biomart/martservice?query=" + martQuery, destination);
        }
    }

    private void downloadResource(String urlString, Path destination) {
        try {
            downloadResource(new URL(urlString), destination);
        } catch (MalformedURLException e) {
            logger.error("", e);
        }
    }

    private void downloadClassPathResource(String resourcePath, Path destination) {
        try {
            URL resourceUrl = new ClassPathResource(resourcePath).getURL();
            downloadResource(resourceUrl, destination);
        } catch (IOException e) {
            logger.error("Unable to download classpath resource", e);
        }
    }

    private void downloadResource(URL source, Path destination) {
        try {
            logger.info("Downloading resource from: {}", source);
            FileUtils.copyURLToFile(source, destination.toFile(), 2500, 15000);
        } catch (IOException ex) {
            logger.error("Unable to download resource {} to {}", source, destination, ex);
        }
    }

    private DataSource createDataSource(Path databasePath) {
        String initSql = "MODE=PostgreSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE;";
        String url = String.format("jdbc:h2:file:%s;%s", databasePath.toAbsolutePath(), initSql);
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username("sa")
                .build();
    }

    private void migrateDatabase(DataSource dataSource) {
        Map<String, String> propertyPlaceHolders = new HashMap<>();
        propertyPlaceHolders.put("import.path", genomeDataPath.toString());

        logger.info("Migrating {} genome database...", buildInfo.getBuildString());
        Flyway h2Flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("EXOMISER")
                .locations("classpath:db/migration")
                .placeholders(propertyPlaceHolders)
                .load();
        h2Flyway.clean();
        h2Flyway.migrate();
    }
}
