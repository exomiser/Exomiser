/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GeneConstraintsTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '\t',
            value = {
            // #gene	transcript	mane_select	lof.oe	lof.pLI	lof.oe_ci.lower	lof.oe_ci.upper	mis.z_score	syn.z_score
                "ZZZ3\tENST00000370801\ttrue\t1.8520e-01\t1.0000e+00\t1.2100e-01\t2.9000e-01\t2.4446e+00\t1.0101e+00",
                "A1BG\tENST00000263100\ttrue\t1.0463e+00\t1.7129e-16\t8.2300e-01\t1.3420e+00\t-8.6948e-01\t-6.4437e-01"
    })
    void geneContraint(String geneSymbol, String transcriptId, boolean isManeSelect, double loeuf,  double pLi, double loeufLower, double loeufUpper, double missenseZ, double synonymousZ) {
        assertThat(GeneConstraints.geneConstraint(geneSymbol), equalTo(new GeneConstraint(geneSymbol, transcriptId, pLi, loeuf, loeufLower, loeufUpper, missenseZ, synonymousZ)));
    }

    @Test
    void constraints() {
        assertThat(GeneConstraints.geneConstraints().size(), equalTo(17454));
    }

}