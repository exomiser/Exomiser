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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.data.genome.archive.AlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.DbNsfpAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.EspAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.parsers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Hg19Config.class})
@TestPropertySource("/application-test.properties")
public class Hg19ConfigTest {

    @Autowired
    Hg19Config instance;

    @Test
    public void testResources() {
        List<AlleleResource> alleleResources = instance.hg19AlleleResources();

        List<AlleleResource> expectedResources = ImmutableList.of(
                instance.gnomadGenomeAlleleResource(),
                instance.gnomadExomeAlleleResource(),
                instance.topmedAlleleResource(),
                instance.dbSnpAlleleResource(),
                instance.uk10kAlleleResource(),
                instance.exacAlleleResource(),
                instance.espAlleleResource(),
                instance.dbnsfpAlleleResource(),
                instance.clinVarAlleleResource()
                );
        assertThat(alleleResources.size(), equalTo(expectedResources.size()));
        for (int i = 0; i < alleleResources.size(); i++) {
            AlleleResource actual = alleleResources.get(i);
            AlleleResource expected = expectedResources.get(i);
            assertThat(actual.getName(), equalTo(expected.getName()));
            assertThat(actual.getAlleleParser(), instanceOf(expected.getAlleleParser().getClass()));
            assertThat(actual.getAlleleArchive(), equalTo(expected.getAlleleArchive()));
        }
    }

    @Test
    public void dbSnpAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.dbSnpAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/00-All.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(DbSnpAlleleParser.class));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testEspAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.espAlleleResource();

        AlleleArchive expectedArchive = new EspAlleleArchive(Paths.get("src/test/resources/hg19/variants/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(EspAlleleParser.class));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testDbNsfpAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.dbnsfpAlleleResource();

        AlleleArchive expectedArchive = new DbNsfpAlleleArchive(Paths.get("src/test/resources/hg19/variants/dbNSFPv3.4a.zip")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(DbNsfpAlleleParser.class));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void exacAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.exacAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/ExAC.r0.3.1.sites.vep.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(ExacAlleleParser.class));
        ExacAlleleParser exacAlleleParser = (ExacAlleleParser) alleleResource.getAlleleParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(ExacPopulationKey.EXAC_EXOMES));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void gnomadGenomeAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.gnomadGenomeAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/gnomad.genomes.r2.0.1.sites.noVEP.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(ExacAlleleParser.class));
        ExacAlleleParser exacAlleleParser = (ExacAlleleParser) alleleResource.getAlleleParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(ExacPopulationKey.GNOMAD_GENOMES));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void gnomadExomeAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.gnomadExomeAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/gnomad.exomes.r2.0.1.sites.noVEP.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(ExacAlleleParser.class));
        ExacAlleleParser exacAlleleParser = (ExacAlleleParser) alleleResource.getAlleleParser();
        assertThat(exacAlleleParser.getPopulationKeys(), equalTo(ExacPopulationKey.GNOMAD_EXOMES));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testTopmedAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.topmedAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/TOPMED_GRCh37.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(TopMedAlleleParser.class));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

    @Test
    public void testU10kAlleleResource() throws Exception {
        AlleleResource alleleResource = instance.uk10kAlleleResource();

        AlleleArchive expectedArchive = new TabixAlleleArchive(Paths.get("src/test/resources/hg19/variants/UK10K_COHORT.20160215.sites.vcf.gz")
                .toAbsolutePath());
        assertThat(alleleResource.getAlleleParser(), instanceOf(Uk10kAlleleParser.class));
        assertThat(alleleResource.getAlleleArchive(), equalTo(expectedArchive));
    }

}