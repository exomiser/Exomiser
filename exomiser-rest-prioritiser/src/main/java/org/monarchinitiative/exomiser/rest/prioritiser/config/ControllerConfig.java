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

package org.monarchinitiative.exomiser.rest.prioritiser.config;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ControllerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ControllerConfig.class);

    private final GenomeAnalysisService genomeAnalysisService;

    public ControllerConfig(GenomeAnalysisService genomeAnalysisService) {
        this.genomeAnalysisService = genomeAnalysisService;
    }

    @Bean
    public Map<Integer, GeneIdentifier> getGeneIdentifiers() {
        Map<Integer, GeneIdentifier> geneIdentifierMap = new HashMap<>();
        for (GeneIdentifier geneIdentifier : genomeAnalysisService.getKnownGeneIdentifiers()) {
            // Don't add GeneIdentifiers without HGNC identifiers as these are superceeded by others with the same
            // entrez id which will create duplicate key errors and out of date gene symbols etc.
            if (geneIdentifier.hasEntrezId() && !geneIdentifier.getHgncId().isEmpty()) {
                GeneIdentifier previous = geneIdentifierMap.put(geneIdentifier.getEntrezIdAsInteger(), geneIdentifier);
                if (previous != null) {
                    logger.warn("Duplicate key added {} - was {}", geneIdentifier, previous);
                }
            }
        }
        return ImmutableMap.copyOf(geneIdentifierMap);
    }

//    private final Environment environment;
//
//    public ControllerConfig(Environment environment) {
//        this.environment = environment;
//    }
//
//    @Bean
//    public Path hgncFilePath() {
//        String pathString = Objects.requireNonNull(environment.getProperty("hgnc.path"));
//        return Paths.get(pathString).toAbsolutePath();
//    }
//
//    @Bean
//    public Map<Integer, GeneIdentifier> getGeneIdentifiers(Path hgncFilePath) {
//        // TODO This should be able to replace the GenomeAnalysisService, but this causes issues with the
//        //  AnalysisServiceAutoconfiguration
//        HgncParser hgncParser = new HgncParser(hgncFilePath);
//        return hgncParser.parseGeneIdentifiers()
//                .filter(GeneIdentifier::hasEntrezId)
//                .collect(toMap(GeneIdentifier::getEntrezIdAsInteger, Function.identity()));
//    }

}
