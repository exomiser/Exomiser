/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package config;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.exomiser.core.analysis.AnalysisFactory;
import de.charite.compbio.exomiser.core.analysis.SettingsParser;
import de.charite.compbio.exomiser.core.config.EnableExomiser;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@PropertySource({"classpath:exomiser.properties", "classpath:application.properties"})
@EnableExomiser
public class TestExomiserConfig {
    
    Logger logger = LoggerFactory.getLogger(TestExomiserConfig.class);
    
    @Autowired
    private Environment env;
    
    @Bean
    public SettingsParser mockSettingsParser() {
        return Mockito.mock(SettingsParser.class);
    }
    
    @Bean
    public AnalysisFactory mockAnalysisFactory() {
        return Mockito.mock(AnalysisFactory.class);
    }
    
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("dataDir"));
        logger.debug("Root data source directory set to: {}", dataPath.toAbsolutePath());
        
        return dataPath;
    }

    @Bean
    public Path ucscFilePath() {
        Path ucscFilePath = dataPath().resolve(env.getProperty("ucscFileName"));
        logger.debug("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }

    @Bean
    public Path phenixDataDirectory() {
        Path phenixDataDirectory = dataPath().resolve(env.getProperty("phenomizerDataDir"));
        logger.info("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    public Path hpoOntologyFilePath() {
        Path hpoOntologyFilePath = phenixDataDirectory().resolve(env.getProperty("hpoOntologyFile"));
        logger.debug("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }

    @Bean
    public Path hpoAnnotationFilePath() {
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(env.getProperty("hpoAnnotationFile"));
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    @Bean
    public JannovarData jannovarData() {
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.<TranscriptModel>of());
    }

    @Bean
    public VariantFactory variantFactory() {
        return new VariantFactory(jannovarData());
    }
 
    @Bean
    public SampleDataFactory sampleDataFactory() {
        return Mockito.mock(SampleDataFactory.class);
    }
    
//    @Bean
//    public PriorityFactory prioritserFactory() {
//        return Mockito.mock(PriorityFactory.class);
//    }
    
    //cacheable beans
    @Bean
    public FrequencyDao frequencyDao() {
        return Mockito.mock(FrequencyDao.class);
    }

    @Bean
    public PathogenicityDao pathogenicityDao() {
        return Mockito.mock(PathogenicityDao.class);
    }

    @Bean 
    public SparseVariantFilterRunner sparseVariantFilterer() {
        return Mockito.mock(SparseVariantFilterRunner.class);
    }
    
    @Bean
    public VariantDataService variantDataService() {
        return Mockito.mock(VariantDataService.class);
    }
    
//    @Bean
//    PriorityService priorityService() {
//        return new PriorityService();
//    }
//
//    @Bean
//    ModelService modelService() {
//        return new ModelServiceImpl();
//    }
//
//    @Bean
//    OntologyService ontologyService() {
//        return new OntologyServiceImpl();
//    }
//
//    @Bean
//    DiseaseDao diseaseDao() {
//        return new DefaultDiseaseDao();
//    }
//
//    @Bean
//    HumanPhenotypeOntologyDao humanPhenotypeOntologyDao() {
//        return new HumanPhenotypeOntologyDao();
//    }
//
//    @Bean
//    MousePhenotypeOntologyDao mousePhenotypeOntologyDao() {
//        return new MousePhenotypeOntologyDao();
//    }
//
//    @Bean
//    ZebraFishPhenotypeOntologyDao zebraFishPhenotypeOntologyDao() {
//        return new ZebraFishPhenotypeOntologyDao();
//    }
}
