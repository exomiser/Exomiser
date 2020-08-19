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
class ImpcMouseGenePhenotypeReaderTest {

    @Test
    void read() {
        Resource impcResource = Resource.of("src/test/resources/data/mouse/ALL_genotype_phenotype_test.csv.gz");
        ImpcMouseGenePhenotypeReader instance = new ImpcMouseGenePhenotypeReader(impcResource);
        List<GenePhenotype> expected = List.of(
                new GenePhenotype("IMPC:IP00006940a_het", "MGI:101762", Set.of("MP:0001556", "MP:0002966", "MP:0002968", "MP:0005178")),
                new GenePhenotype("IMPC:Net_hom", "MGI:101762", Set.of("MP:0001488", "MP:0002092", "MP:0002792", "MP:0010098")),

                new GenePhenotype("IMPC:BL3511_het", "MGI:102556", Set.of("MP:0001697", "MP:0003232", "MP:0003720", "MP:0003864", "MP:0006108")),
                new GenePhenotype("IMPC:BL3511_hom", "MGI:102556", Set.of("MP:0001697", "MP:0001722", "MP:0004086", "MP:0011100", "MP:0013293")),

                new GenePhenotype("IMPC:H-ITCH-E10-TM1B_hom", "MGI:1202301", Set.of("MP:0000063", "MP:0000194", "MP:0000434", "MP:0000467", "MP:0000691", "MP:0000745", "MP:0001486", "MP:0002100", "MP:0002966", "MP:0003068", "MP:0003795", "MP:0003960", "MP:0004924", "MP:0005560", "MP:0005568", "MP:0009342", "MP:0010053", "MP:0010088", "MP:0010124")),
                new GenePhenotype("IMPC:JR25849_het", "MGI:96522", Set.of("MP:0000063", "MP:0000186", "MP:0005179", "MP:0005344", "MP:0010052", "MP:0010124", "MP:0011275")),
                new GenePhenotype("IMPC:JR25849_hom", "MGI:96522", Set.of("MP:0011100"))
        );
        assertThat(instance.read(), equalTo(expected));
    }
}