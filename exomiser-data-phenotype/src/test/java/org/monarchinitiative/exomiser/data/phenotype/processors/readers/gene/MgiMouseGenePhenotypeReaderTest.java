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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MgiMouseGenePhenotypeReaderTest {

    @Test
    void read() {
        Resource mgiGenePhenoResource = Resource.of("src/test/resources/data/mouse/MGI_GenePheno_test.rpt");

        MgiMouseGenePhenotypeReader instance = new MgiMouseGenePhenotypeReader(mgiGenePhenoResource);

        List<GenePhenotype> expected = List.of(
                new GenePhenotype("MGI:6263466", "MGI:2671987", Set.of("MP:0001513", "MP:0002644")),
                new GenePhenotype("MGI:2166381", "MGI:96522", Set.of("MP:0001614", "MP:0000364")),
                new GenePhenotype("MGI:3713641", "MGI:96522", Set.of("MP:0001800", "MP:0002190", "MP:0000295", "MP:0005025")),
                new GenePhenotype("MGI:2166359", "MGI:97874", Set.of("MP:0000600", "MP:0001716", "MP:0001698"))
        );
        assertThat(instance.read(), equalTo(expected));
    }
}