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
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.EntrezIdGeneSymbol;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class HgncCompleteSetEntrezGeneSymbolReaderTest {

    @Test
    void read() {
        Resource hgncCompleteSetResource = Resource.of("src/test/resources/data/hgnc_complete_set_test.txt");

        HgncCompleteSetEntrezGeneSymbolReader instance = new HgncCompleteSetEntrezGeneSymbolReader(hgncCompleteSetResource);
        List<EntrezIdGeneSymbol> entrezIdGeneSymbols = instance.read();

        List<EntrezIdGeneSymbol> expected = List.of(
                new EntrezIdGeneSymbol(1, "A1BG"),
                new EntrezIdGeneSymbol(29974, "A1CF"),
                new EntrezIdGeneSymbol(144571, "A2M-AS1"),
                new EntrezIdGeneSymbol(3, "A2MP1")
        );
        assertThat(entrezIdGeneSymbols, equalTo(expected));
    }
}