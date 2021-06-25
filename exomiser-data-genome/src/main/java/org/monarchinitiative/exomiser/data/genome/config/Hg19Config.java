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

package org.monarchinitiative.exomiser.data.genome.config;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.indexers.DbVarDeDupOutputFileIndexer;
import org.monarchinitiative.exomiser.data.genome.indexers.OutputFileIndexer;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;
import org.monarchinitiative.exomiser.data.genome.model.archive.TabixArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.DbNsfpColumnIndex;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.*;
import org.monarchinitiative.exomiser.data.genome.model.resource.*;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class Hg19Config extends ResourceConfig {

    private final Environment environment;

    public Hg19Config(Environment environment) {
        super(environment);
        this.environment = environment;
    }

    @Bean
    public Path buildDir() {
        return getPathForProperty("build-dir");
    }

    @Bean
    public AssemblyResources hg19AssemblyResources() {
        Map<String, AlleleResource> hg19AlleleResources = hg19AlleleResources();
        List<SvResource> hg19SvResources = hg19SvResources();
        Path hg19GenomePath = hg19GenomePath();
        return new AssemblyResources(GenomeAssembly.HG19, hg19GenomePath, hg19AlleleResources, hg19SvResources);
    }

    public Path hg19GenomePath() {
        return getPathForProperty("hg19.genome-dir");
    }

    private Path getPathForProperty(String propertyKey) {
        String property = environment.getProperty(propertyKey, "");

        if (property.isEmpty()) {
            throw new IllegalArgumentException(propertyKey + " has not been specified!");
        }
        return Paths.get(property);
    }

    public Map<String, AlleleResource> hg19AlleleResources() {
        ImmutableMap.Builder<String, AlleleResource> alleleResources = new ImmutableMap.Builder<>();

        alleleResources.put("gnomad-genome", gnomadGenomeAlleleResource());
        alleleResources.put("gnomad-exome", gnomadExomeAlleleResource());
        // TOPMed removed as this is now part of dbSNP
        alleleResources.put("dbsnp", dbSnpAlleleResource());
        alleleResources.put("uk10k", uk10kAlleleResource());
        alleleResources.put("exac", exacAlleleResource());
        alleleResources.put("esp", espAlleleResource());
        alleleResources.put("dbnsfp", dbnsfpAlleleResource());
        alleleResources.put("clinvar", clinVarAlleleResource());

        return alleleResources.build();
    }

    public DbSnpAlleleResource dbSnpAlleleResource() {
        return alleleResource(DbSnpAlleleResource.class, "hg19.dbsnp");
    }

    public ClinVarAlleleResource clinVarAlleleResource() {
        return alleleResource(ClinVarAlleleResource.class, "hg19.clinvar");
    }

    public EspHg19AlleleResource espAlleleResource() {
        return alleleResource(EspHg19AlleleResource.class, "hg19.esp");
    }

    public ExacExomeAlleleResource exacAlleleResource() {
        return alleleResource(ExacExomeAlleleResource.class, "hg19.exac");
    }

    public AlleleResource dbnsfpAlleleResource() {
        String namespacePrefix = "hg19.dbnsfp";
        ResourceProperties resourceProperties = getResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getResourcePath();
        URL resourceUrl = resourceProperties.getResourceUrl();
        if (resourcePath.toString().contains("dbNSFP4.")) {
            return new DbNsfp4AlleleResource(namespacePrefix, resourceUrl, resourcePath, DbNsfpColumnIndex.HG19);
        }
        return new DbNsfp3AlleleResource(namespacePrefix, resourceUrl, resourcePath, DbNsfpColumnIndex.HG19);
    }

    public TopMedAlleleResource topmedAlleleResource() {
        return alleleResource(TopMedAlleleResource.class, "hg19.topmed");
    }

    public Uk10kAlleleResource uk10kAlleleResource() {
        return alleleResource(Uk10kAlleleResource.class, "hg19.uk10k");
    }

    public GnomadGenomeAlleleResource gnomadGenomeAlleleResource() {
        return alleleResource(GnomadGenomeAlleleResource.class, "hg19.gnomad-genome");
    }

    public GnomadExomeAlleleResource gnomadExomeAlleleResource() {
        return alleleResource(GnomadExomeAlleleResource.class, "hg19.gnomad-exome");
    }

    private List<SvResource> hg19SvResources() {
        return List.of(
                clinvarSvResource(),
                dbVarFrequencyResource(),
                gnomadSvFrequencyResource(),
                gonlSvFrequencyResource(),
                dgvSvResource(),
                decipherSvResource()
        );
    }


    public ClinVarSvResource clinvarSvResource() {
        try {
            return new ClinVarSvResource("hg19.clinvar-sv",
                    new URL("https://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz"),
                    new FileArchive(hg19GenomePath().resolve("variant_summary.txt.gz")),
                    new ClinVarSvParser(GenomeAssembly.HG19),
                    new OutputFileIndexer<>(hg19GenomePath().resolve("clinvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public DbVarSvResource dbVarFrequencyResource() {
        try {
            return new DbVarSvResource("hg19.dbvar",
                    new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/dbVar/data/Homo_sapiens/by_assembly/GRCh37/vcf/GRCh37.variant_call.all.vcf.gz"),
                    new TabixArchive(hg19GenomePath().resolve("GRCh37.variant_call.all.vcf.gz")),
                    new DbVarFreqParser(),
                    new DbVarDeDupOutputFileIndexer(hg19GenomePath().resolve("dbvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GnomadSvResource gnomadSvFrequencyResource() {
        try {
            // https://doi.org/10.1038/s41586-020-2287-8
            //
            return new GnomadSvResource("hg19.gnomad-sv",
                    new URL("https://storage.googleapis.com/gnomad-public/papers/2019-sv/gnomad_v2.1_sv.sites.vcf.gz"),
                    new TabixArchive(hg19GenomePath().resolve("gnomad_v2.1_sv.sites.vcf.gz")),
                    new GnomadSvVcfFreqParser(),
                    new OutputFileIndexer<>(hg19GenomePath().resolve("gnomad-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GonlSvResource gonlSvFrequencyResource() {
        try {
            return new GonlSvResource("hg19.gonl",
                    new URL("https://molgenis26.gcc.rug.nl/downloads/gonl_public/variants/release6.1/20161013_GoNL_AF_genotyped_SVs.vcf.gz"),
                    new FileArchive(hg19GenomePath().resolve("20161013_GoNL_AF_genotyped_SVs.vcf.gz")),
                    new GonlSvFreqParser(),
                    new OutputFileIndexer<>(hg19GenomePath().resolve("gonl-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public DgvSvResource dgvSvResource() {
        try {
            return new DgvSvResource("hg19.dgv-sv",
                    new URL("http://dgv.tcag.ca/dgv/docs/GRCh37_hg19_variants_2020-02-25.txt"),
                    new FileArchive(hg19GenomePath().resolve("dgv-hg19-variants-2020-02-25.txt")),
                    new DgvSvFreqParser(),
                    new OutputFileIndexer<>(hg19GenomePath().resolve("dgv-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public DecipherSvResource decipherSvResource() {
        try {
            return new DecipherSvResource("hg19.decipher-sv",
                    new URL("https://www.deciphergenomics.org/files/downloads/population_cnv_grch37.txt.gz"),
                    new FileArchive(hg19GenomePath().resolve("decipher_population_cnv_grch37.txt.gz")),
                    new DecipherSvFreqParser(),
                    new OutputFileIndexer<>(hg19GenomePath().resolve("decipher-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
