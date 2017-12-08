/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.data.phenotype.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides configuration details from the app.properties file located in the
 * classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:app.properties"})
public class AppConfig {

    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    Environment env;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("data.path"));
        logger.info("Root data working directory set to: {}", dataPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (dataPath.toFile().mkdir()) {
            logger.info("Made new data directory: {}", dataPath.toAbsolutePath());
        }
        return dataPath;
    }

    @Bean
    public Path downloadPath() {
        Path downloadPath = Paths.get(dataPath().toString(), env.getProperty("download.path"));
        logger.info("Data download directory set to: {}", downloadPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (downloadPath.toFile().mkdir()) {
            logger.info("Made new download directory: {}", downloadPath.toAbsolutePath());
        }
        return downloadPath;
    }

    /**
     * Dirty hack to copy the static data into the data directory.
     */
    @Bean
    public boolean copyPheno2GeneResource() {
        Resource resource = new ClassPathResource("data/pheno2gene.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
             BufferedWriter writer = Files.newBufferedWriter(downloadPath().resolve("pheno2gene.txt"), Charset.forName("UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                writer.write(line + "\n");
            }
            logger.info("Copied {} to {}", resource, downloadPath());
        } catch (IOException ex) {
            logger.error("Unable to copy resource {}", resource, ex);
        }
        return true;
    }

    @Bean
    public Path processPath() {
        Path processPath = dataPath().resolve(env.getProperty("process.path"));
        logger.info("Data process directory set to: {}", processPath.toAbsolutePath());
        //this is needed for anything to work correctly, so make sure it exists.
        if (processPath.toFile().mkdir()) {
            logger.info("Made new process directory: {}", processPath.toAbsolutePath());
        }
        return processPath;
    }

    @Bean
    public boolean downloadResources() {
        boolean download = Boolean.parseBoolean(env.getProperty("downloadResources"));
        logger.info("Setting application to download resources: {}", download);
        return download;
    }

    @Bean
    public boolean extractResources() {
        boolean extract = Boolean.parseBoolean(env.getProperty("extractResources"));
        logger.info("Setting application to extract resources: {}", extract);
        return extract;
    }

    @Bean
    public boolean parseResources() {
        boolean parse = Boolean.parseBoolean(env.getProperty("parseResources"));
        logger.info("Setting application to parse resources: {}", parse);
        return parse;
    }

    @Bean
    public boolean migrateH2() {
        boolean migrateH2 = Boolean.parseBoolean(env.getProperty("migrateH2"));
        logger.info("Setting application to migrate H2 database: {}", migrateH2);
        return migrateH2;
    }
}
