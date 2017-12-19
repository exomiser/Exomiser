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

package org.monarchinitiative.exomiser.data.genome.config;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.data.genome.archive.DbNsfpAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.EspAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.parsers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class Hg38Config {

    @Autowired
    public Environment environment;

    @Bean
    public List<AlleleResource> hg38AlleleResources() {
        return ImmutableList.of(
                gnomadGenomeAlleleResource(),
                gnomadExomeAlleleResource(),
                topmedAlleleResource(),
                dbSnpAlleleResource(),
                uk10kAlleleResource(),
                exacAlleleResource(),
                espAlleleResource(),
                dbnsfpAlleleResource()
        );
    }

    public AlleleResource dbSnpAlleleResource() {
        String namespacePrefix = "hg38.dbsnp";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new DbSnpAlleleParser());
    }

    public AlleleResource espAlleleResource() {
        String namespacePrefix = "hg38.esp";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new EspAlleleArchive(resourcePath), new EspHg38AlleleParser());
    }

    public AlleleResource exacAlleleResource() {
        String namespacePrefix = "hg38.exac";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.EXAC_EXOMES));
    }

    public AlleleResource dbnsfpAlleleResource() {
        String namespacePrefix = "hg38.dbnsfp";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new DbNsfpAlleleArchive(resourcePath), new DbNsfpAlleleParser(DbNsfpColumnIndex.HG38));
    }

    public AlleleResource topmedAlleleResource() {
        String namespacePrefix = "hg38.topmed";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new TopMedAlleleParser());
    }

    public AlleleResource uk10kAlleleResource() {
        String namespacePrefix = "hg38.uk10k";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new Uk10kAlleleParser());
    }

    public AlleleResource gnomadGenomeAlleleResource() {
        String namespacePrefix = "hg38.gnomad-genome";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.GNOMAD_GENOMES));
    }

    public AlleleResource gnomadExomeAlleleResource() {
        String namespacePrefix = "hg38.gnomad-exome";
        Path resourcePath = getAlleleResourcePath(namespacePrefix);
        return new AlleleResource(namespacePrefix, new TabixAlleleArchive(resourcePath), new ExacAlleleParser(ExacPopulationKey.GNOMAD_EXOMES));
    }

    private Path getAlleleResourcePath(String namespacePrefix) {
        AlleleResourceProperties resourceProperties = getAlleleResourceProperties(namespacePrefix);
        Path fileDirectory = resourceProperties.getFileDirectory();
        return fileDirectory.resolve(resourceProperties.getFileName());
    }

    private AlleleResourceProperties getAlleleResourceProperties(String namespacePrefix) {
        AlleleResourceProperties properties = new AlleleResourceProperties();
        properties.setFileName(environment.getProperty(namespacePrefix + ".file-name"));
        properties.setFileDirectory(Paths.get(environment.getProperty(namespacePrefix + ".file-dir")));
        properties.setFileUrl(environment.getProperty(namespacePrefix + ".file-url"));
        return properties;
    }
}
