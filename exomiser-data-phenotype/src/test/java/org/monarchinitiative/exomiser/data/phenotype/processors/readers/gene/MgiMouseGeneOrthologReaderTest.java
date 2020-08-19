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
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MgiMouseGeneOrthologReaderTest {

    @Test
    void read() {
        Resource mgiHomMouseHumanSequenceResource = Resource.of("src/test/resources/data/mouse/HOM_MouseHumanSequence_test.rpt");
        MgiMouseGeneOrthologReader instance = new MgiMouseGeneOrthologReader(mgiHomMouseHumanSequenceResource);
        List<GeneOrtholog> output = instance.read();
        List<GeneOrtholog> expected = List.of(
                // one-to-one
                new GeneOrtholog("MGI:87867", "Acadm", "ACADM", 34),
                new GeneOrtholog("MGI:895149", "Acadvl", "ACADVL", 37),
                // one-to-many
                new GeneOrtholog("MGI:1915220", "Slx1b", "SLX1A", 548593),
                new GeneOrtholog("MGI:1915220", "Slx1b", "SLX1B", 79008),
                // many-to-one
                new GeneOrtholog("MGI:3031035", "Olfr1201", "OR4C11", 219429),
                new GeneOrtholog("MGI:3031039", "Olfr1205", "OR4C11", 219429),
                new GeneOrtholog("MGI:3031040", "Olfr1206", "OR4C11", 219429),
                // unique to mouse/human should not appear

                // integration test cases
                new GeneOrtholog("MGI:97874", "Rb1", "RB1", 5925),
                new GeneOrtholog("MGI:96522", "Rbpj", "RBPJ", 3516),
                new GeneOrtholog("MGI:2671987", "Shank2", "SHANK2", 22941),
                new GeneOrtholog("MGI:101762", "Elk3", "ELK3", 2004),
                new GeneOrtholog("MGI:102556", "Tbx4", "TBX4", 9496),
                new GeneOrtholog("MGI:1202301", "Itch", "ITCH", 83737)
        );
        assertThat(output, equalTo(expected));
    }
}