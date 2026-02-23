/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.rest.prioritiser;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.genome.GenomeAnalysisServiceAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        ExomiserAutoConfiguration.class,
        GenomeAnalysisServiceAutoConfiguration.class
})
@OpenAPIDefinition(
        info = @Info(
                title = "Exomiser Prioritiser API",
                version = "1.0.0",
                description = "API for prioritising genes based on phenotype semantic similarity")

)
public class ExomiserPrioritiserServer {

    public static void main(String[] args) {
        SpringApplication.run(ExomiserPrioritiserServer.class, args);
    }

}
