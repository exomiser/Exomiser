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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.archive.*;
import org.monarchinitiative.exomiser.data.genome.model.parsers.*;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@SpringJUnitConfig(classes = {Hg19Config.class})
@TestPropertySource("/application-test.properties")
public class Hg19ConfigTest {

    @Autowired
    Hg19Config instance;

    @Test
    void assemblyResources() {
        AssemblyResources assemblyResources = instance.hg19AssemblyResources();
        assertThat(assemblyResources.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
        assertFalse(assemblyResources.getAlleleResources().isEmpty());
        assertThat(assemblyResources.getGenomeDataPath(), equalTo(instance.genomeDataPath()));
    }

    @Test
    void testClinVarResource() throws Exception {
        ClinVarAlleleResource clinVarResource = instance.clinVarAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/clinvar.vcf.gz"));
        URL expectedUrl = new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh37/clinvar.vcf.gz");

        assertThat(clinVarResource.getName(), equalTo("hg19.clinvar"));
        assertThat(clinVarResource.getParser(), instanceOf(ClinVarAlleleParser.class));
        assertThat(clinVarResource.getArchive(), equalTo(expectedArchive));
        assertThat(clinVarResource.getResourceUrl(), equalTo(expectedUrl));
    }

    @Test
    public void testResources() {
        Map<String, AlleleResource> actualResources = instance.hg19AlleleResources();

        ImmutableMap.Builder<String, AlleleResource> alleleResources = new ImmutableMap.Builder<>();

        alleleResources.put("gnomad-genome", instance.gnomadGenomeAlleleResource());
        alleleResources.put("gnomad-exome", instance.gnomadExomeAlleleResource());
        alleleResources.put("gnomad-mito", instance.gnomadMitoAlleleResource());
        alleleResources.put("dbsnp", instance.dbSnpAlleleResource());
        alleleResources.put("uk10k", instance.uk10kAlleleResource());
        // exac removed as this is part of gnomad
        alleleResources.put("esp", instance.espAlleleResource());
        alleleResources.put("dbnsfp", instance.dbnsfpAlleleResource());

        Map<String, AlleleResource> expectedResources = alleleResources.build();

        assertThat(actualResources.keySet(), equalTo(expectedResources.keySet()));
        for (String key : expectedResources.keySet()) {
            AlleleResource actual = actualResources.get(key);
            AlleleResource expected = expectedResources.get(key);
            assertThat(actual.getName(), equalTo(expected.getName()));
            assertThat(actual.getParser(), instanceOf(expected.getParser().getClass()));
            assertThat(actual.getArchive(), equalTo(expected.getArchive()));
        }
    }

    @Test
    public void dbSnpAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.dbSnpAlleleResource();

        Archive expectedArchive = new DbSnpArchive(Paths.get("src/test/resources/hg19/variants/00-All.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(DbSnpAlleleParser.class));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testEspAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.espAlleleResource();

        Archive expectedArchive = new EspArchive(Paths.get("src/test/resources/hg19/variants/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz"));
        assertThat(alleleResource.getParser(), instanceOf(EspHg19AlleleParser.class));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testDbNsfpAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.dbnsfpAlleleResource();

        Archive expectedArchive = new DbNsfp3Archive(Paths.get("src/test/resources/hg19/variants/dbNSFPv3.4a.zip"));
        assertThat(alleleResource.getParser(), instanceOf(DbNsfpAlleleParser.class));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Disabled("deprecated resource")
    @Test
    public void exacAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.exacAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/ExAC.r0.3.1.sites.vep.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(ExacAlleleParser.class));
        ExacExomeAlleleParser exacAlleleParser = (ExacExomeAlleleParser) alleleResource.getParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(ExacPopulationKey.EXAC_EXOMES));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void gnomadGenomeAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.gnomadGenomeAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/gnomad.genomes.r2.0.1.sites.noVEP.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(GnomadAlleleParser.class));
        Gnomad2GenomeAlleleParser exacAlleleParser = (Gnomad2GenomeAlleleParser) alleleResource.getParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(GnomadPopulationKey.GNOMAD_V2_1_GENOMES));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void gnomadExomeAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.gnomadExomeAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/gnomad.exomes.r2.0.1.sites.noVEP.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(GnomadAlleleParser.class));
        Gnomad2ExomeAlleleParser exacAlleleParser = (Gnomad2ExomeAlleleParser) alleleResource.getParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(GnomadPopulationKey.GNOMAD_V2_1_EXOMES));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testTopmedAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.topmedAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/TOPMED_GRCh37.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(TopMedAlleleParser.class));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testU10kAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.uk10kAlleleResource();

        Archive expectedArchive = new TabixArchive(Paths.get("src/test/resources/hg19/variants/UK10K_COHORT.20160215.sites.vcf.gz"));
        assertThat(alleleResource.getParser(), instanceOf(Uk10kAlleleParser.class));
        assertThat(alleleResource.getArchive(), equalTo(expectedArchive));
    }

}