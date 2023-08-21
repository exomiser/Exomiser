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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseasePhenotype;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DiseasePhenotypeReaderTest {

    @Test
    void read() {
        Resource phenotypeAnnotationResource = Resource.of("src/test/resources/data/phenotype_test.hpoa");

        DiseasePhenotypeReader instance = new DiseasePhenotypeReader(phenotypeAnnotationResource);
        List<DiseasePhenotype> output = instance.read();

        DiseasePhenotype first = new DiseasePhenotype("DECIPHER:1", Set.of("HP:0001249", "HP:0001250", "HP:0001252", "HP:0001518", "HP:0000252"));
        DiseasePhenotype last = new DiseasePhenotype("ORPHA:230800", Set.of("HP:0000508",
                "HP:0000651",
                "HP:0001324",
                "HP:0002015",
                "HP:0002019",
                "HP:0002094",
                "HP:0002747",
                "HP:0003470",
                "HP:0006597",
                "HP:0006824",
                "HP:0011499",
                "HP:0100021"
        ));

        assertThat(output.get(0), equalTo(first));
        assertThat(output.get(output.size() - 1), equalTo(last));
    }
}