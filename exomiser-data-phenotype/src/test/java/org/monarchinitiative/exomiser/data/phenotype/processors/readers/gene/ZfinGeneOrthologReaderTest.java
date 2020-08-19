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
class ZfinGeneOrthologReaderTest {

    @Test
    void read() {
        Resource fishGeneOrthologResource = Resource.of("src/test/resources/data/fish/human_orthos_test.txt");
        ZfinGeneOrthologReader instance = new ZfinGeneOrthologReader(fishGeneOrthologResource);
        List<GeneOrtholog> fishGeneOrthologs = instance.read();

        List<GeneOrtholog> expected = List.of(
                new GeneOrtholog("ZFIN:ZDB-GENE-000112-47", "ppardb", "PPARD", 5467),
                new GeneOrtholog("ZFIN:ZDB-GENE-000125-12", "igfbp2a", "IGFBP2", 3485),
                new GeneOrtholog("ZFIN:ZDB-GENE-000125-4", "dlc", "DLL3", 10683),
                new GeneOrtholog("ZFIN:ZDB-GENE-000128-11", "dbx1b", "DBX1", 120237),
                new GeneOrtholog("ZFIN:ZDB-GENE-000128-13", "dbx2", "DBX2", 440097),
                new GeneOrtholog("ZFIN:ZDB-GENE-000128-8", "dbx1a", "DBX1", 120237),
                new GeneOrtholog("ZFIN:ZDB-GENE-000201-18", "pbx4", "PBX4", 80714),
                new GeneOrtholog("ZFIN:ZDB-SNORNAG-120314-7", "snord31.3", "SNORD31", 9298),
                new GeneOrtholog("ZFIN:ZDB-SNORNAG-150916-2", "snord7", "SNORD7", 692076)
        );
        assertThat(fishGeneOrthologs, equalTo(expected));
    }
}