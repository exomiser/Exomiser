/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@Import(DataDirectoryAutoConfiguration.class)
@EnableConfigurationProperties(ExomiserProperties.class)
public class PhenotypeDataSourceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PhenotypeDataSourceAutoConfiguration.class);

    private final ExomiserProperties exomiserProperties;
    private final Path exomiserDataDirectory;

    public PhenotypeDataSourceAutoConfiguration(ExomiserProperties exomiserProperties, Path exomiserDataDirectory) {
        this.exomiserProperties = exomiserProperties;
        this.exomiserDataDirectory = exomiserDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(HikariConfig h2Config) {
        HikariDataSource dataSource = new HikariDataSource(h2Config);
        logger.info("DataSource using maximum of {} database connections", dataSource.getMaximumPoolSize());
        logger.info("Returning a new {} DataSource pool to URL {} user: {}", dataSource.getPoolName(), dataSource.getJdbcUrl(), dataSource
                .getUsername());
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public HikariConfig h2Config() {

        ExomiserProperties.H2 h2 = exomiserProperties.getH2();

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(determineH2Url(h2));
        config.setUsername(h2.getUser());
        config.setPassword(h2.getPassword());
        config.setMaximumPoolSize(h2.getMaxConnections());
        config.setPoolName("exomiser-H2");
        return config;
    }

    private String determineH2Url(ExomiserProperties.H2 h2) {
        //the data path is the default place for the exomiser H2 database to be found.
        if (h2.getDirectory().isEmpty()) {
            logger.info("H2 path not set. Using default data path: {}", exomiserDataDirectory);
            return resolveH2UrlPathPlaceholder(h2.getUrl(), exomiserDataDirectory.toAbsolutePath().toString());
        } else {
            logger.info("Using user defined H2 path: {}", h2.getDirectory());
            return resolveH2UrlPathPlaceholder(h2.getUrl(), h2.getDirectory());
        }
    }

    private String resolveH2UrlPathPlaceholder(String h2Url, String h2AbsolutePath) {
        return h2Url.replace("${h2Path}", h2AbsolutePath);
    }

}
