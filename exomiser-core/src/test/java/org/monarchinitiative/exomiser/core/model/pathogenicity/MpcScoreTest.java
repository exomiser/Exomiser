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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MpcScoreTest {

    @Test
    void inputOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> MpcScore.of(-0.1f));
        assertThrows(IllegalArgumentException.class, () -> MpcScore.of(5.1f));
    }

    @Test
    void constructor() {
        MpcScore instance = MpcScore.of(1f);
        assertThat(instance.getSource(), equalTo(PathogenicitySource.MPC));
        assertThat(instance.getScore(), equalTo(0.2f));
        assertThat(instance.getRawScore(), equalTo(1f));
    }

    @Test
    void testScalingZero() {
        MpcScore instance = MpcScore.of(0f);
        assertThat(instance.getScore(), equalTo(0f));
        assertThat(instance.getRawScore(), equalTo(0f));
    }

    @Test
    void testScalingHalfWay() {
        MpcScore instance = MpcScore.of(2.5f);
        assertThat(instance.getScore(), equalTo(0.5f));
        assertThat(instance.getRawScore(), equalTo(2.5f));
    }

    @Test
    void testScalingFive() {
        MpcScore instance = MpcScore.of(5f);
        assertThat(instance.getScore(), equalTo(1f));
        assertThat(instance.getRawScore(), equalTo(5f));
    }
}