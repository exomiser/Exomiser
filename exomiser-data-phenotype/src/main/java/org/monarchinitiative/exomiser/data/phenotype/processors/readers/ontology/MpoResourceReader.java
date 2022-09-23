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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class MpoResourceReader implements ResourceReader<List<OboOntologyTerm>> {

    private static final Logger logger = LoggerFactory.getLogger(MpoResourceReader.class);

    private final Resource mpResource;

    public MpoResourceReader(Resource mpResource) {
        this.mpResource = mpResource;
    }

    @Override
    public List<OboOntologyTerm> read() {
        OboOntology oboOntology = OboOntologyParser.parseOboFile(mpResource.getResourcePath());
        logger.info("Reading MP version: {}", oboOntology.getDataVersion());

        int currPlusObs = oboOntology.getCurrentOntologyTerms().size() + oboOntology.getObsoleteOntologyTerms().size();
        List<OboOntologyTerm> allTerms = new ArrayList<>(currPlusObs);
        allTerms.addAll(oboOntology.getCurrentOntologyTerms());
        allTerms.addAll(oboOntology.getObsoleteOntologyTerms());

        allTerms.sort(Comparator.comparing(OboOntologyTerm::getId));
        // CAUTION! In the HPO parser only the current terms are added to the map and written out - in the case of the MP
        // both obsolete and current terms were included. It's not clear why this is the case, but this behaviour has been
        // retained following refactoring this class. If this functionality is undesired the allTerms should be replaced
        // with oboOntology.getCurrentOntologyTerms()
        List<OboOntologyTerm> zpTerms = allTerms.stream()
                // there are a few terms with identifiers such as 'after', 'during', 'occurs_in'
                .filter(oboOntologyTerm -> oboOntologyTerm.getId().startsWith("MP:"))
                .toList();

        logger.info("Read {} MP terms", zpTerms.size());
        return zpTerms;
    }
}
