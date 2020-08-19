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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class ZpoResourceReader implements ResourceReader<List<OboOntologyTerm>> {

    private static final Logger logger = LoggerFactory.getLogger(ZpoResourceReader.class);

    private final Resource zpResource;

    public ZpoResourceReader(Resource zpResource) {
        this.zpResource = zpResource;
    }

    @Override
    public List<OboOntologyTerm> read() {
        logger.info("Reading ZP from {}", zpResource);
        List<OboOntologyTerm> zpTerms = new ArrayList<>();
        try (BufferedReader reader = zpResource.newBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                if (fields[0].startsWith("ZP:")) {
                    zpTerms.add(OboOntologyTerm.builder().id(fields[0]).label(fields[1]).build());
                }
            }
        } catch (Exception ex) {
            logger.error("Unable to read ZP resource {}", zpResource, ex);
        }
        logger.info("Read {} ZP terms", zpTerms.size());
        return zpTerms;
    }
}
