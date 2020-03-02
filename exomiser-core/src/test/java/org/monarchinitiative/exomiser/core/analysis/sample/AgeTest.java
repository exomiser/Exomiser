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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AgeTest {

    @Test
    void ageIsNormalised() {
        Age expected = Age.of(2, 3, 4);
        assertThat(Age.parse("P1Y15M4D"), equalTo(expected));
        assertThat(Age.of(1, 15, 4), equalTo(expected));
    }

    @Test
    void periodConstructorIsNormalised() {
        Period period = Period.parse("P1Y15M4D");
        assertThat(Age.of(period), equalTo(Age.of(2, 3, 4)));
    }

    @Test
    void parseWeeks() {
        Age age = Age.parse("P3W");
        assertThat(age.getYears(), equalTo(0));
        assertThat(age.getMonths(), equalTo(0));
        assertThat(age.getDays(), equalTo(21));
    }

    @Test
    void ageUnknown() {
        Age age = Age.unknown();
        assertThat(age.isUnknown(), is(true));
        Age adult = Age.of(18, 0, 0);
        assertThat(adult.isUnknown(), is(false));

        assertThat(age.getYears(), equalTo(0));
        assertThat(age.getMonths(), equalTo(0));
        assertThat(age.getDays(), equalTo(0));
    }

    @Test
    void getYears() {
        Age adult = Age.of(18, 0, 0);
        assertThat(adult.getYears(), equalTo(18));
    }

    @Test
    void toPeriod() {
        Age age = Age.of(1, 2, 3);
        assertThat(age.toPeriod(), equalTo(Period.of(1, 2, 3)));
    }
}