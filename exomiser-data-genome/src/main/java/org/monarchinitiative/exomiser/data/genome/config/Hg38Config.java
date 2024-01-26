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
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.ClinVarSvParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.DbVarFreqParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.DecipherSvFreqParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.DgvSvFreqParser;
import org.monarchinitiative.exomiser.data.genome.model.resource.*;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class Hg38Config extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(Hg38Config.class);

    public Hg38Config(Environment environment) {
        super(environment);
    }

    @Bean
    public AssemblyResources hg38AssemblyResources() {
        ClinVarAlleleResource clinVarAlleleResource = clinVarAlleleResource();
        Path genomeDataPath = genomeDataPath();
        Path genomeProcessPath = genomeProcessPath();
        Map<String, AlleleResource> alleleResources = hg38AlleleResources();
        List<SvResource> svResources = hg38SvResources(genomeProcessPath);
        return new AssemblyResources(GenomeAssembly.HG38, genomeDataPath, genomeProcessPath, clinVarAlleleResource, alleleResources, svResources);
    }

    public Path genomeDataPath() {
        return getPathForProperty("hg38.genome-dir");
    }

    public Path genomeProcessPath() {
        return getPathForProperty("hg38.genome-processed-dir");
    }

    public Map<String, AlleleResource> hg38AlleleResources() {
        ImmutableMap.Builder<String, AlleleResource> alleleResources = new ImmutableMap.Builder<>();

        // thousand genomes removed as this is part of gnomAD v2.1
        alleleResources.put("gnomad-genome", gnomadGenomeAlleleResource());
        alleleResources.put("gnomad-exome", gnomadExomeAlleleResource());
        alleleResources.put("gnomad-mito", gnomadMitoAlleleResource());
        alleleResources.put("alfa", alfaAlleleResource());
        // TOPMed removed as this is part of gnomAD v2.1
        // dbSNP removed as this mostly adds a lot of empty data with only rsids
        alleleResources.put("uk10k", uk10kAlleleResource());
        // ExAC removed as this is part of gnomad-exomes v2.1
        // ESP removed as this is part of gnomad-exomes v4
        alleleResources.put("dbnsfp", dbnsfpAlleleResource());
        // CLinVar removed - now handled as a separate data source

        return alleleResources.build();
    }

    public DbSnpAlleleResource dbSnpAlleleResource() {
        return alleleResource(DbSnpAlleleResource.class, "hg38.dbsnp");
    }

    public ClinVarAlleleResource clinVarAlleleResource() {
        return alleleResource(ClinVarAlleleResource.class, "hg38.clinvar");
    }

    public EspHg38AlleleResource espAlleleResource() {
        return alleleResource(EspHg38AlleleResource.class, "hg38.esp");
    }

    public ExacExomeAlleleResource exacAlleleResource() {
        return alleleResource(ExacExomeAlleleResource.class, "hg38.exac");
    }

    public AlleleResource dbnsfpAlleleResource() {
        String namespacePrefix = "hg38.dbnsfp";
        ResourceProperties resourceProperties = getResourceProperties(namespacePrefix);
        Path resourcePath = resourceProperties.getResourcePath();
        URL resourceUrl = resourceProperties.getResourceUrl();
        if (resourcePath.toString().contains("dbNSFP4.")) {
            return new DbNsfp4AlleleResource(namespacePrefix, resourceUrl, resourcePath, DbNsfpColumnIndex.HG38);
        }
        return new DbNsfp3AlleleResource(namespacePrefix, resourceUrl, resourcePath, DbNsfpColumnIndex.HG38);
    }

    public TopMedAlleleResource topmedAlleleResource() {
        return alleleResource(TopMedAlleleResource.class, "hg38.topmed");
    }

    public Uk10kAlleleResource uk10kAlleleResource() {
        return alleleResource(Uk10kAlleleResource.class, "hg38.uk10k");
    }

    public Gnomad4GenomeAlleleResource gnomadGenomeAlleleResource() {
        return alleleResource(Gnomad4GenomeAlleleResource.class, "hg38.gnomad-genome");
    }

    public Gnomad4ExomeAlleleResource gnomadExomeAlleleResource() {
        return alleleResource(Gnomad4ExomeAlleleResource.class, "hg38.gnomad-exome");
    }

    public Gnomad3MitoAlleleResource gnomadMitoAlleleResource() {
        return alleleResource(Gnomad3MitoAlleleResource.class, "hg38.gnomad-mito");
    }

    public AlfaAlleleResource alfaAlleleResource() {
        return alleleResource(AlfaAlleleResource.class, "hg38.alfa");
    }

    public List<SvResource> hg38SvResources(Path genomeProcessPath) {
        return List.of(
                clinvarSvResource(genomeProcessPath),
                dbVarFrequencyResource(genomeProcessPath),
                dgvSvResource(genomeProcessPath),
                decipherSvResource(genomeProcessPath),
                // GoNL is hg19 only, this is a no-op method to avoid db migration errors
                gonlSvFrequencyResource(genomeProcessPath),
                // gnomAD hg38 is part of dbVar, this is a no-op method to avoid db migration errors
                gnomadSvFrequencyResource(genomeProcessPath)
        );
    }

    public ClinVarSvResource clinvarSvResource(Path genomeProcessPath) {
        try {
            return new ClinVarSvResource("hg38.clinvar-sv",
                    new URL("https://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz"),
                    new FileArchive(genomeDataPath().resolve("variant_summary.txt.gz")),
                    new ClinVarSvParser(GenomeAssembly.HG38),
                    new OutputFileIndexer<>(genomeProcessPath.resolve("clinvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public DbVarSvResource dbVarFrequencyResource(Path genomeProcessPath) {
        try {
            return new DbVarSvResource("hg38.dbvar",
                    new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/dbVar/data/Homo_sapiens/by_assembly/GRCh38/vcf/GRCh38.variant_call.all.vcf.gz"),
                    new TabixArchive(genomeDataPath().resolve("GRCh38.variant_call.all.vcf.gz")),
                    new DbVarFreqParser(),
                    new DbVarDeDupOutputFileIndexer(genomeProcessPath.resolve("dbvar-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public NoOpSvResource gnomadSvFrequencyResource(Path genomeProcessPath) {
        // gnomAD hg38 is part of dbVar
        return new NoOpSvResource("hg38.gnomad-sv",
                new FileArchive(genomeDataPath().resolve("gnomad-sv.stub")),
                genomeProcessPath.resolve("gnomad-sv.pg"));
        // https://doi.org/10.1038/s41586-020-2287-8
        //
//            return new GnomadSvResource("hg38.gnomad-sv",
//                    new URL("https://storage.googleapis.com/gcp-public-data--gnomad/papers/2019-sv/gnomad_v2.1_sv.sites.vcf.gz"),
//                    new TabixArchive(genomeDataPath().resolve("gnomad_v2.1_sv.sites.vcf.gz")),
//                    new GnomadSvVcfFreqParser(),
//                    new OutputFileIndexer<>(genomeProcessPath.resolve("gnomad-sv.pg")));
    }

    // TODO: Externalise these in the application.properties
    public NoOpSvResource gonlSvFrequencyResource(Path genomeProcessPath) {
        // GoNL is hg19 only
        return new NoOpSvResource("hg38.gonl",
                new FileArchive(genomeDataPath().resolve("gonl-sv.stub")),
                genomeProcessPath.resolve("gonl-sv.pg"));
//            return new GonlSvResource("hg38.gonl",
//                    new URL("https://molgenis26.gcc.rug.nl/downloads/gonl_public/variants/release6.1/20161013_GoNL_AF_genotyped_SVs.vcf.gz"),
//                    new FileArchive(genomeDataPath().resolve("20161013_GoNL_AF_genotyped_SVs.vcf.gz")),
//                    new GonlSvFreqParser(),
//                    new OutputFileIndexer<>(genomeProcessPath.resolve("gonl-sv.pg")));
    }

    public DgvSvResource dgvSvResource(Path genomeProcessPath) {
        try {
            return new DgvSvResource("hg38.dgv-sv",
                    new URL("http://dgv.tcag.ca/dgv/docs/GRCh38_hg38_variants_2020-02-25.txt"),
                    new FileArchive(genomeDataPath().resolve("dgv-hg38-variants-2020-02-25.txt")),
                    new DgvSvFreqParser(),
                    new OutputFileIndexer<>(genomeProcessPath.resolve("dgv-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public DecipherSvResource decipherSvResource(Path genomeProcessPath) {
        try {
            return new DecipherSvResource("hg38.decipher-sv",
                    new URL("https://www.deciphergenomics.org/files/downloads/population_cnv_grch38.txt.gz"),
                    new FileArchive(genomeDataPath().resolve("decipher_population_cnv_grch38.txt.gz")),
                    new DecipherSvFreqParser(),
                    new OutputFileIndexer<>(genomeProcessPath.resolve("decipher-sv.pg")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
