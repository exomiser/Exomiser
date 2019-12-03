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
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@ConfigurationProperties("exomiser.hg38")
public class Hg38GenomeProperties extends AbstractGenomeProperties {

    public Hg38GenomeProperties() {
        super(GenomeAssembly.HG38);
    }

    @Bean
    @ConfigurationProperties("exomiser.hg38.genome.datasource")
    public DataSourceProperties hg38genomeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "hg38genomeDataSource")
    @ConfigurationProperties("exomiser.hg38.genome.datasource.hikari")
    public HikariDataSource genomeDataSource() {
        return hg38genomeDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    // Structural variants database
    @Bean
    @ConfigurationProperties("exomiser.hg38.sv.datasource")
    public DataSourceProperties hg38svDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "hg38svDataSource")
    @ConfigurationProperties("exomiser.hg38.sv.datasource.hikari")
    public HikariDataSource svDataSource() {
        return hg38svDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
