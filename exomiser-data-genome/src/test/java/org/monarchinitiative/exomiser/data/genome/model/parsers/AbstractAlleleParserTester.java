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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test utility class for wrapping an {@link AlleleParser} and calling/testing the {@code AlleleParser#parseLine} method.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractAlleleParserTester<T extends AlleleParser> {

    public abstract T newInstance();

    public List<Allele> parseLine(String line) {
        return newInstance().parseLine(line);
    }

    public void assertParseLineEquals(String line, List<Allele> expectedAlleles) {
        List<Allele> alleles = parseLine(line);
        assertParseResultsEquals(alleles, expectedAlleles);
    }

    public static void assertParseResultsEquals(List<Allele> alleles, List<Allele> expectedAlleles) {
        assertThat(alleles.size(), equalTo(expectedAlleles.size()));
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            Allele expected = expectedAlleles.get(i);
            assertThat(allele, equalTo(expected));
            assertThat(allele.getRsId(), equalTo(expected.getRsId()));
            assertThat(allele.getClinVarData(), equalTo(expected.getClinVarData()));
            assertThat(allele.getValues(), equalTo(expected.getValues()));
            assertThat(allele.getFrequencies(), equalTo(expected.getFrequencies()));
            assertThat(allele.getPathogenicityScores(), equalTo(expected.getPathogenicityScores()));
        }
    }


}

