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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Hg19GenomeAnalysisServiceAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void genomeAnalysisService() throws Exception {

        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg19.data-version=1710");

        GenomeAnalysisService genomeAnalysisService = (GenomeAnalysisService) context.getBean("hg19genomeAnalysisService");
        assertThat(genomeAnalysisService.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));

        assertThat(context.getBean("hg19jannovarData"), instanceOf(JannovarData.class));
        assertThat(context.getBean("hg19mvStore"), instanceOf(MVStore.class));
        assertThat(context.getBean("hg19clinVarStore"), instanceOf(MVStore.class));
        assertThat(context.getBean("hg19variantAnnotator"), instanceOf(VariantAnnotator.class));
        assertThat(context.getBean("hg19variantDataService"), instanceOf(VariantDataService.class));
        assertThat(context.getBean("hg19genomeDataService"), instanceOf(GenomeDataService.class));
        assertThat(context.getBean("hg19VariantWhiteList"), instanceOf(VariantWhiteList.class));

        assertThat(context.getBean("hg19allelePropertiesDao"), instanceOf(AllelePropertiesDao.class));

        assertThat(context.getBean("hg19remmDao"), instanceOf(RemmDao.class));
        assertThat(context.getBean("hg19caddDao"), instanceOf(CaddDao.class));
        assertThat(context.getBean("hg19localFrequencyDao"), instanceOf(LocalFrequencyDao.class));
    }

    @Test
    public void genomeAnalysisServiceWithOptionalTestPathDao() throws Exception {

        String testPathogenicitySourcePath = TEST_DATA.resolve("remm/remmData.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg19.data-version=1710", "exomiser.hg19.test-pathogenicity-score-path=" + testPathogenicitySourcePath);

        assertThat(context.getBean("hg19testPathDao"), instanceOf(TestPathogenicityScoreDao.class));
    }

    @Configuration
    @ImportAutoConfiguration(value = Hg19GenomeAnalysisServiceAutoConfiguration.class)
    protected static class EmptyConfiguration {
    }
}