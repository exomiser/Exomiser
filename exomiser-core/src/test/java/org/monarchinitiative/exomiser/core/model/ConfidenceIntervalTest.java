/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ConfidenceIntervalTest {

    @Test
    void checkIllegalLowerBoundInput() {
        assertThrows(IllegalArgumentException.class, () -> ConfidenceInterval.of(10, 0));
    }

    @Test
    void checkIllegalUpperBoundInput() {
        assertThrows(IllegalArgumentException.class, () -> ConfidenceInterval.of(0, -10));
    }

    @Test
    void empty() {
        assertThat(ConfidenceInterval.empty(), equalTo(ConfidenceInterval.of(0, 0)));
    }

    @Test
    void getLowerBound() {
        int lowerBound = -10;
        ConfidenceInterval instance = ConfidenceInterval.of(lowerBound, 0);
        assertThat(instance.getLowerBound(), equalTo(lowerBound));
    }

    @Test
    void getUpperBound() {
        int upperBound = 20;
        ConfidenceInterval instance = ConfidenceInterval.of(0, upperBound);
        assertThat(instance.getUpperBound(), equalTo(upperBound));
    }

    @Test
    void getMin() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.getMinPos(200), equalTo(190));
    }


    @Test
    void getMax() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.getMaxPos(200), equalTo(220));
    }

    @Test
    void testToString() {
        System.out.println(ConfidenceInterval.empty());
    }
}