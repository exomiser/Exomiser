/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;

import java.util.Collections;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneDiseaseModelTest {

    @Test
    public void testToString() {
        //TODO: consider GeneOrthologModel, GeneDiseaseModel, PatientModel, AbstractModel, phenotypeIds, score, bestPhenotypeMatchForTerms
        //TODO: see https://github.com/exomiser/Exomiser/issues/138
        //Disease:
//        private final String modelId;
//        private final Organism organism;
//
//        private final int entrezGeneId;
//        private final String humanGeneSymbol;
//
//        private final String diseaseId;
//        private final String diseaseTerm;
//
//        private final DiseaseType diseaseType;
//        private final InheritanceMode inheritanceMode;
//
//        private final List<String> phenotypeIds;

        //Gene:
//        private final String modelId;
//        private final Organism organism;
//
//        private final int entrezGeneId;
//        private final String humanGeneSymbol;
//
//        private final String modelGeneId;
//        private final String modelGeneSymbol;
//
//        private final List<String> phenotypeIds;

        System.out.println(new GeneDiseaseModel("modelId", Organism.HUMAN, 12345, "GENE1", "DISEASE:1", "Thing's disease", Collections.emptyList()));
    }

}