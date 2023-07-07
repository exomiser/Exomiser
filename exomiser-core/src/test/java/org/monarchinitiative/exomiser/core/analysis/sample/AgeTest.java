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
import java.util.regex.Pattern;

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
    void canUseNegativeValues() {
        Age age = Age.parse("-P12W");
        // TODO: CAUTION! Using negatives to denote prenatal ages can be error-prone as P1Y-52W returns a period of -364 days
        // TODO: probably should not allow this and add a isPrenatal flag
        assertThat(age.days(), equalTo(Math.negateExact(12 * 7)));
    }

    @Test
    void gestationalAge() {
        Age instance = Age.gestational(5, 2);
        assertThat(instance.years(), equalTo(0));
        assertThat(instance.months(), equalTo(0));
        assertThat(instance.days(), equalTo(37));
    }

    @Test
    void parseWeeks() {
        Age age = Age.parse("P3W");
        assertThat(age.years(), equalTo(0));
        assertThat(age.months(), equalTo(0));
        assertThat(age.days(), equalTo(21));
    }

    @Test
    void ageUnknown() {
        Age age = Age.unknown();
        assertThat(age.isUnknown(), is(true));
        Age adult = Age.of(18, 0, 0);
        assertThat(adult.isUnknown(), is(false));

        assertThat(age.years(), equalTo(0));
        assertThat(age.months(), equalTo(0));
        assertThat(age.days(), equalTo(0));
    }

    @Test
    void getYears() {
        Age adult = Age.of(18, 0, 0);
        assertThat(adult.years(), equalTo(18));
    }

    @Test
    void toPeriod() {
        Age age = Age.of(1, 2, 3);
        assertThat(age.toPeriod(), equalTo(Period.of(1, 2, 3)));
    }

    @Test
    void unknown() {
        assertThat(Age.unknown().isUnknown(), is(true));
    }

    @Test
    void cachesUnknownObject() {
        assertThat(Age.of(0, 0, 0).isUnknown(), is(true));
    }

    @Test
    void gestationalAgeString() {
        Age gestationalAge = Age.gestational(12, 0);
        assertThat(gestationalAge.toString(), equalTo("12+0"));
    }

    @Test
    void testGestationalAgePattern() {
        Pattern pattern = Pattern.compile("\\d{1,2}\\+[0-7]");
        assertThat(pattern.matcher("250+0").matches(), is(false));
        assertThat(pattern.matcher("25+0").matches(), is(true));
        assertThat(pattern.matcher("3+1").matches(), is(true));
        assertThat(pattern.matcher("3+5").matches(), is(true));
        assertThat(pattern.matcher("3+7").matches(), is(true));
        assertThat(pattern.matcher("3+8").matches(), is(false));
    }
}