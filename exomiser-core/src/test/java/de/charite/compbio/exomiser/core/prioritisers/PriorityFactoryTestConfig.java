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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.dao.*;
import de.charite.compbio.exomiser.core.prioritisers.util.*;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jblas.FloatMatrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
    PriorityFactoryImpl priorityFactory() {
        return new PriorityFactoryImpl();
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
    PriorityService priorityService() {
        return new PriorityService(ontologyService(), modelService(), diseaseDao());
    }
    
    @Bean
    ModelService modelService() {
        return new ModelServiceImpl();
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
