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

package org.monarchinitiative.exomiser.autoconfigure.phenotype;

import com.zaxxer.hikari.HikariConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Ignore
public class PhenotypeDataSourceAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void exomiserH2DefaultConfig() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(3));
        assertThat(hikariConfig.getUsername(), equalTo("sa"));
        assertThat(hikariConfig.getPassword(), equalTo(""));
        assertThat(hikariConfig.getJdbcUrl(), startsWith("jdbc:h2:file:" + TEST_DATA.toAbsolutePath() + "/exomiser;"));
    }

    @Test
    public void exomiserH2ConfigUserDefinedH2PathNoUrlDefined() {
        Path userDefined = Paths.get("src/test/resources/user-defined").toAbsolutePath();
        load(EmptyConfiguration.class, TEST_DATA_ENV,
                String.format("exomiser.h2.directory=%s", userDefined),
                "exomiser.h2.user=sa",
                "exomiser.h2.password=",
                "exomiser.h2.max-connections=999");

        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(999));
        assertThat(hikariConfig.getUsername(), equalTo("sa"));
        assertThat(hikariConfig.getPassword(), equalTo(""));
        assertThat(hikariConfig.getJdbcUrl(), startsWith(String.format("jdbc:h2:file:%s/exomiser;", userDefined)));
    }

    @Test
    public void exomiserH2ConfigUserDefinedH2PathUrlDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV,
                "exomiser.h2.directory=wibble",
                "exomiser.h2.user=wibble",
                "exomiser.h2.password=wibble",
                "exomiser.h2.url=jdbc:h2:mem:exomiser",
                "exomiser.h2.max-connections=999");

        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(999));
        assertThat(hikariConfig.getUsername(), equalTo("wibble"));
        assertThat(hikariConfig.getPassword(), equalTo("wibble"));
        assertThat(hikariConfig.getJdbcUrl(), equalTo("jdbc:h2:mem:exomiser"));
    }

    @Test
    public void exomiserH2ConfigCanBeOveridden() {
        load(PhenotypeDataSouceOverrideConfiguration.class, TEST_DATA_ENV);
        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(3));
        assertThat(hikariConfig.getJdbcUrl(), startsWith("jdbc:h2:mem:exomiser"));
    }

    @Test
    public void dataSource() throws Exception {
        load(PhenotypeDataSouceOverrideConfiguration.class, TEST_DATA_ENV);
        DataSource phenotypeDataSource = (DataSource) context.getBean("dataSource");
        assertThat(phenotypeDataSource, not(nullValue()));
        assertThat(phenotypeDataSource.getConnection().isValid(1), is(true));
    }

    @Configuration
    protected static class EmptyConfiguration {
    }

    @Configuration
    @Import(EmptyConfiguration.class)
    protected static class PhenotypeDataSouceOverrideConfiguration {

        @Bean
        public HikariConfig h2Config() {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:exomiser");
            config.setMaximumPoolSize(3);
            config.setPoolName("exomiser-H2-mem");
            return config;
        }
    }
}