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
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.parsers.genome.EnsemblEnhancerParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.genome.FantomEnhancerParser;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.SvResource;
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
    private final Path genomeProcessedPath;
    private final Path outputPath;
    private final List<SvResource> svResources;

    public GenomeDatabaseBuildRunner(BuildInfo buildInfo, Path genomeDataPath, Path genomeProcessedPath, Path outputPath, List<SvResource> svResources) {
        this.buildInfo = buildInfo;
        this.genomeDataPath = genomeDataPath;
        this.genomeProcessedPath = genomeProcessedPath;
        this.outputPath = outputPath;
        this.svResources = svResources;
    }

    public void run() {

        logger.info("Parsing ENSEMBL enhancers...");
        Path ensemblEnhancersFile = genomeDataPath.resolve("ensembl_enhancers.tsv");
        String martQuery = getMartQueryString("genome/ensembl_enhancer_biomart_query.xml");
        downloadEnsemblEnhancers(buildInfo.getAssembly(), martQuery, ensemblEnhancersFile);
        EnsemblEnhancerParser ensemblEnhancerParser = new EnsemblEnhancerParser(ensemblEnhancersFile, genomeProcessedPath.resolve("ensembl_enhancers.pg"));
        ensemblEnhancerParser.parse();

        logger.info("Parsing FANTOM 5 enhancers...");
        Path fantomBedPath = genomeDataPath.resolve("fantom_enhancers.bed");
        downloadClassPathResource(String.format("genome/%s_fantom_permissive_enhancer_usage.bed", buildInfo.getAssembly()), fantomBedPath);

        FantomEnhancerParser fantomEnhancerParser = new FantomEnhancerParser(fantomBedPath, genomeProcessedPath.resolve("fantom_enhancers.pg"));
        fantomEnhancerParser.parse();

        // extract hg19_tad.pg from the jar to genomeDataPath as tad.pg
        logger.info("Extracting TAD resource...");
        downloadClassPathResource(String.format("genome/%s_tad.pg", buildInfo.getAssembly()), genomeProcessedPath.resolve("tad.pg"));

        logger.info("Downloading and indexing SV resources...");
        svResources.parallelStream().forEach(ResourceDownloader::download);
        svResources.parallelStream().forEach(SvResource::indexResource);

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


        //build genome.h2.db
        Path databasePath = outputPath.resolve(String.format("%s_genome", buildInfo.getBuildString()));
        DataSource dataSource = createDataSource(databasePath);
        logger.info("Created database: {}", databasePath);
        migrateDatabase(dataSource);
        logger.info("Finished importing genome data");
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
        // adding DB_CLOSE_ON_EXIT=FALSE option required to prevent errors being thrown when using the shutdown_compact
        // migration to reduce the database size when we're finished loading it.
        String initSql = "MODE=PostgreSQL;CACHE_SIZE=65536;LOCK_MODE=0;DB_CLOSE_ON_EXIT=FALSE;";
        String url = String.format("jdbc:h2:file:%s;%s", databasePath.toAbsolutePath(), initSql);
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username("sa")
                .build();
    }

    private void migrateDatabase(DataSource dataSource) {
        Map<String, String> propertyPlaceHolders = new HashMap<>();
        propertyPlaceHolders.put("import.path", genomeProcessedPath.toString());

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
