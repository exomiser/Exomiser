/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.priority;

import de.charite.compbio.exomiser.priority.util.DataMatrix;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jblas.DoubleMatrix;
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
        return new DataMatrix(DoubleMatrix.EMPTY, stubMatrixIndex);
    }
    
    @Bean
    Path phenomizerDataDirectory() {
        return Paths.get("stubPhenomiserDataDir");
    }
}
