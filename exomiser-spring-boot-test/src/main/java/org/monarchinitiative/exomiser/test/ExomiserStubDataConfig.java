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

package org.monarchinitiative.exomiser.test;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import htsjdk.tribble.readers.TabixReader;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Configuration to provide a stub classes for exomiser beans which require on-disk files to operate on.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ExomiserStubDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserStubDataConfig.class);

    @Bean
    public GenomeAnalysisService genomeAnalysisService() {
        return new GenomeAnalysisServiceImpl(GenomeAssembly.HG19, Mockito.mock(GenomeAnalysisService.class), Mockito.mock(VariantDataService.class), Mockito.mock(VariantAnnotator.class));
    }

    @Bean
    public HumanPhenotypeOntologyDao hpoDao(){
        HumanPhenotypeOntologyDao hpoDao = Mockito.mock(HumanPhenotypeOntologyDao.class);
        Mockito.when(hpoDao.getAllTerms()).thenReturn(Collections.emptySet());
        Mockito.when(hpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());
        Mockito.when(hpoDao.getIdToPhenotypeTerms()).thenReturn(Collections.emptyMap());
        logger.info("Mocking hpDao");
        return hpoDao;
    }

    @Bean
    public MousePhenotypeOntologyDao mpoDao(){
        MousePhenotypeOntologyDao mpoDao = Mockito.mock(MousePhenotypeOntologyDao.class);
        Mockito.when(mpoDao.getAllTerms()).thenReturn(Collections.emptySet());
        Mockito.when(mpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());
        logger.info("Mocking mpDao");
        return mpoDao;
    }

    @Bean
    public ZebraFishPhenotypeOntologyDao zpoDao() {
        ZebraFishPhenotypeOntologyDao zpoDao = Mockito.mock(ZebraFishPhenotypeOntologyDao.class);
        Mockito.when(zpoDao.getAllTerms()).thenReturn(Collections.emptySet());
        Mockito.when(zpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());
        logger.info("Mocking zpoDao");
        return zpoDao;
    }

    @Bean("phenotypeDataSource")
    public HikariDataSource phenotypeDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public HikariConfig h2Config() {
        logger.info("Creating in memory H2 database");
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:exomiser");
        config.setMaximumPoolSize(3);
        config.setPoolName("exomiser-H2-mem");
        return config;
    }

    @Bean
    public JannovarData jannovarData() {
        logger.info("Stubbing Jannovar data");
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
    }

    /**
     * Provides a mock TabixReader in place of a TabixReader for a specific tabix file.
     * @return a mock TabixReader
     */
    @Bean
    public TabixReader inDelTabixReader() {
        logger.info("Mocking inDelTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader snvTabixReader() {
        logger.info("Mocking snvTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader remmTabixReader() {
        logger.info("Mocking remmTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader localFrequencyTabixReader() {
        logger.info("Mocking localFrequencyTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public DataMatrix dataMatrix() {
        logger.info("Stubbing dataMatrix");
        return DataMatrix.empty();
    }

    @Bean
    Path phenixDataDirectory() {
        logger.info("Stubbing phenixDataDirectory");
        return Paths.get("phenix");
    }
}
