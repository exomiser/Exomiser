/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class PathogenicityDaoTestConfig {
    

    @Bean
    public DataSource dataSource() {
        String url = "jdbc:h2:mem:exomiser;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE;INIT=runscript from 'src/test/resources/sql/pathogenicityDaoTest.sql'";
        String user = "sa";
        String password = "sa";
        
        JdbcConnectionPool dataSource = JdbcConnectionPool.create(url, user, password);
        return dataSource;
    }
    
    @Bean
    public DefaultPathogenicityDao defaultPathogenicityDao() {
        return new DefaultPathogenicityDao();
    }
    
}
