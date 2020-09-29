/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class Hg38GenomePropertiesTest extends AbstractAutoConfigurationTest {

    @Test
    void genomeDataSourceProperties() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg38.data-version=1710");
        assertThat(context.getBean("hg38genomeDataSourceProperties"), instanceOf(DataSourceProperties.class));
    }

    @Test
    void genomeDataSource() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg38.data-version=1710");
        assertThat(context.getBean("hg38genomeDataSource"), instanceOf(HikariDataSource.class));
    }

    @Test
    void svDataSourceProperties() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg38.data-version=1710");
        assertThat(context.getBean("hg38svDataSourceProperties"), instanceOf(DataSourceProperties.class));
    }

    @Test
    void svDataSource() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg38.data-version=1710");
        assertThat(context.getBean("hg38svDataSource"), instanceOf(HikariDataSource.class));
    }

    @Configuration
    @ImportAutoConfiguration(value = Hg38GenomeProperties.class)
    protected static class EmptyConfiguration {
    }
}