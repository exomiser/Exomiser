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

package org.monarchinitiative.exomiser.data.genome.config;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.data.genome.archive.DbNsfpAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.EspAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.parsers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class Hg19Config {

    @Autowired
    public Environment environment;

    @Bean
    public Path buildDir() {
        return getPathForProperty("build-dir");
    }

    @Bean
    public Path hg19GenomePath() {
        return getPathForProperty("hg19.genome-dir");
    }

    private Path getPathForProperty(String propertyKey) {
        String genomeDir = environment.getProperty(propertyKey, "");

        if (genomeDir.isEmpty()) {
            throw new IllegalArgumentException(propertyKey + " has not been specified!");
        }
        return Paths.get(genomeDir);
    }

    @Bean
    public Map<String, AlleleResource> hg19AlleleResources() {
        ImmutableMap.Builder<String, AlleleResource> alleleResources = new ImmutableMap.Builder<>();

        alleleResources.put("gnomad-genome", gnomadGenomeAlleleResource());
        alleleResources.put("gnomad-exome", gnomadExomeAlleleResource());
        // TOPMed now part of dbSNP
        alleleResources.put("topmed", topmedAlleleResource());
        alleleResources.put("dbsnp", dbSnpAlleleResource());
        alleleResources.put("uk10k", uk10kAlleleResource());
        alleleResources.put("exac", exacAlleleResource());
        alleleResources.put("esp", espAlleleResource());
        alleleResources.put("dbnsfp", dbnsfpAlleleResource());
        alleleResources.put("clinvar", clinVarAlleleResource());

        return alleleResources.build();
    }

    public AlleleResource dbSnpAlleleResource() {
        String namespacePrefix = "hg19.dbsnp";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new DbSnpAlleleParser());
    }

    public AlleleResource clinVarAlleleResource() {
        String namespacePrefix = "hg19.clinvar";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new ClinVarAlleleParser());
    }

    public AlleleResource espAlleleResource() {
        String namespacePrefix = "hg19.esp";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new EspAlleleArchive(resourcePath), new EspAlleleParser());
    }

    public AlleleResource exacAlleleResource() {
        String namespacePrefix = "hg19.exac";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.EXAC_EXOMES));
    }

    public AlleleResource dbnsfpAlleleResource() {
        String namespacePrefix = "hg19.dbnsfp";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new DbNsfpAlleleArchive(resourcePath), new DbNsfpAlleleParser(DbNsfpColumnIndex.HG19));
    }

    public AlleleResource topmedAlleleResource() {
        String namespacePrefix = "hg19.topmed";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new TopMedAlleleParser());
    }

    public AlleleResource uk10kAlleleResource() {
        String namespacePrefix = "hg19.uk10k";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new Uk10kAlleleParser());
    }

    public AlleleResource gnomadGenomeAlleleResource() {
        String namespacePrefix = "hg19.gnomad-genome";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.GNOMAD_GENOMES));
    }

    public AlleleResource gnomadExomeAlleleResource() {
        String namespacePrefix = "hg19.gnomad-exome";
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getAlleleResourcePath();
        URL resourceUrl = resourceProperties.getAlleleResourceUrl();
        return new AlleleResource(namespacePrefix, resourceUrl, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.GNOMAD_EXOMES));
    }

    private AlleleResourceProperties getAlleleResourceProperties(String namespacePrefix) {
        String fileName = environment.getProperty(namespacePrefix + ".file-name");
        Path fileDir = Paths.get(environment.getProperty(namespacePrefix + ".file-dir"));
        String fileUrl = environment.getProperty(namespacePrefix + ".file-url");
        return new AlleleResourceProperties(fileName, fileDir, fileUrl);
    }
}
