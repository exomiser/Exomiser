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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class HpoResourceReaderTest {

    @Test
    void read() {
        Resource hpResource = Resource.of("src/test/resources/data/hp_test.obo");
        HpoResourceReader instance = new HpoResourceReader(hpResource);
        List<OboOntologyTerm> current = List.of(
                OboOntologyTerm.builder().id("HP:0000001").label("All").build(),
                OboOntologyTerm.builder().id("HP:0000002").label("Abnormality of body height").build(),
                OboOntologyTerm.builder().id("HP:0000003").label("Multicystic kidney dysplasia").addAltId("HP:0004715").build()
        );

        OboOntology ontology = instance.read();

        assertThat(ontology.getCurrentOntologyTerms(), equalTo(current));
    }
}