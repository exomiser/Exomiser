/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.dao.DefaultDiseaseDao;
import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.OntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyService;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyServiceImpl;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jblas.FloatMatrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class PriorityFactoryTestConfig {
    
    @Bean
    DataSource dataSource() {
        String url = "jdbc:h2:mem:exomiser;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE;";
        String user = "sa";
        String password = "sa";
        
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, user, password);
        return dataSource;
    }
    
    @Bean
    PriorityFactory priorityFactory() {
        return new PriorityFactory();
    }
    
    @Bean
    DataMatrix randomWalkMatrix() {
        Map<Integer, Integer> stubMatrixIndex = new HashMap<>();
        return new DataMatrix(FloatMatrix.EMPTY, stubMatrixIndex);
    }
    
    @Bean
    Path phenixDataDirectory() {
        return Paths.get("stubPhenixDataDir");
    }
    
    @Bean
    OntologyService ontologyService() {
        return new OntologyServiceImpl();
    }
    
    @Bean
    DiseaseDao diseaseDao() {
        return new DefaultDiseaseDao();
    }
    
    @Bean
    HumanPhenotypeOntologyDao humanPhenotypeOntologyDao() {
        return new HumanPhenotypeOntologyDao();
    }
    
    @Bean
    MousePhenotypeOntologyDao mousePhenotypeOntologyDao() {
        return new MousePhenotypeOntologyDao();
    }
    
    @Bean
    ZebraFishPhenotypeOntologyDao zebraFishPhenotypeOntologyDao() {
        return new ZebraFishPhenotypeOntologyDao();
    }
}
