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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.service;

import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.List;
import java.util.Set;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OntologyService {

    Set<PhenotypeTerm> getHpoTerms();

    Set<PhenotypeTerm> getMpoTerms();

    Set<PhenotypeTerm> getZpoTerms();

    Set<PhenotypeMatch> getHpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    Set<PhenotypeMatch> getMpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    Set<PhenotypeMatch> getZpoMatchesForHpoTerm(PhenotypeTerm hpoTerm);

    PhenotypeTerm getPhenotypeTermForHpoId(String hpoId);

    /**
     * Checks the input list of HPO ids and returns a new list of HPO ids where any obsolete or outdated ids are
     * replaced with the current version.
     *
     * @since 12.0.0
     * @param hpoIds list of HPO ids to be checked
     * @return a list of current HPO ids generated form the input
     */
    List<String> getCurrentHpoIds(List<String> hpoIds);
}
