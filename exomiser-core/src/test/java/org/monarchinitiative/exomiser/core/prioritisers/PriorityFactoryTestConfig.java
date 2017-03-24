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
package org.monarchinitiative.exomiser.core.prioritisers;

import org.h2.jdbcx.JdbcConnectionPool;
import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactoryTestConfig.class);

    @Bean
    DataSource dataSource() {
        String url = "jdbc:h2:mem:exomiser;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE;";
        String user = "sa";
        String password = "sa";

        return JdbcConnectionPool.create(url, user, password);
    }

//    @Bean
//    PriorityFactoryImpl priorityFactory() {
//        return new PriorityFactoryImpl();
//    }
    
    @Bean
    DataMatrix randomWalkMatrix() {
        logger.info("Loading random walk matrix bean...");
        Map<Integer, Integer> stubMatrixIndex = new HashMap<>();
        return new DataMatrix(FloatMatrix.EMPTY, stubMatrixIndex);
    }
    
    @Bean
    Path phenixDataDirectory() {
        return Paths.get("stubPhenixDataDir");
    }

}
