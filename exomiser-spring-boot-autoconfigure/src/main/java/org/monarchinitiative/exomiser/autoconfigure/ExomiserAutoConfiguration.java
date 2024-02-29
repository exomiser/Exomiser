/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.autoconfigure.genome.GenomeAnalysisServiceAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.phenotype.PrioritiserAutoConfiguration;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@AutoConfiguration
@ConditionalOnClass({Exomiser.class, AnalysisFactory.class})
@EnableConfigurationProperties({ExomiserProperties.class})
@Import({
        ExomiserConfigReporter.class,
        PrioritiserAutoConfiguration.class,
        GenomeAnalysisServiceAutoConfiguration.class
})
@ComponentScan(basePackageClasses = {Exomiser.class}, basePackages = {"org.monarchinitiative.exomiser.core.analysis"})
public class ExomiserAutoConfiguration {

}
