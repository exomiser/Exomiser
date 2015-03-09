/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.config;

import de.charite.compbio.exomiser.web.dao.ExomiserDao;
import de.charite.compbio.exomiser.web.dao.JdbcExomiserDao;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class DaoConfig {
    
    Logger logger = LoggerFactory.getLogger(DaoConfig.class);

    @Bean
    public DataSource dataSource() {
        JndiTemplate jndiTemplate = new JndiTemplate();
        DataSource dataSource = null;
        try {
            dataSource = jndiTemplate.lookup("java:comp/env/jdbc/exomiserDataSource", DataSource.class);
        } catch (NamingException ex) {
            logger.error(null, ex);
        }
        return dataSource;
    }

    @Bean
    public ExomiserDao exomiserDao() {
        return new JdbcExomiserDao();
    }
}
