/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleGenotypeTest {

    @Test
    public void testUnobserved() {
        assertThat(SampleGenotype.of(), equalTo(SampleGenotype.empty()));
        assertThat(SampleGenotype.phased(), equalTo(SampleGenotype.empty()));
    }

    @Test
    public void testHetUnphased() {
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT).getCalls(), equalTo(ImmutableList.of(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF).getCalls(), equalTo(ImmutableList.of(AlleleCall.REF, AlleleCall.ALT)));
    }

    @Test
    public void testHetPhased() {
        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT).getCalls(), equalTo(ImmutableList.of(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF).getCalls(), equalTo(ImmutableList.of(AlleleCall.ALT, AlleleCall.REF)));
    }

    @Test
    public void testToString() {
        assertThat(SampleGenotype.empty().toString(), equalTo("NA"));

        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL).toString(), equalTo("./."));
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.REF).toString(), equalTo("./0"));
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.ALT).toString(), equalTo("./1"));
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.OTHER_ALT).toString(), equalTo("./-"));

        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("0/1"));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF).toString(), equalTo("0/1"));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.REF).toString(), equalTo("0/0"));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT).toString(), equalTo("1/1"));

        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT).toString(), equalTo("0/-"));
        assertThat(SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.REF).toString(), equalTo("0/-"));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT).toString(), equalTo("-/1"));
        assertThat(SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT).toString(), equalTo("-/1"));
        assertThat(SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT).toString(), equalTo("-/-"));


        assertThat(SampleGenotype.phased(AlleleCall.NO_CALL, AlleleCall.NO_CALL).toString(), equalTo(".|."));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.NO_CALL).toString(), equalTo("1|."));
        assertThat(SampleGenotype.phased(AlleleCall.NO_CALL, AlleleCall.REF).toString(), equalTo(".|0"));
        assertThat(SampleGenotype.phased(AlleleCall.NO_CALL, AlleleCall.OTHER_ALT).toString(), equalTo(".|-"));

        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("0|1"));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF).toString(), equalTo("1|0"));
        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.REF).toString(), equalTo("0|0"));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.ALT).toString(), equalTo("1|1"));

        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.OTHER_ALT).toString(), equalTo("0|-"));
        assertThat(SampleGenotype.phased(AlleleCall.OTHER_ALT, AlleleCall.REF).toString(), equalTo("-|0"));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.OTHER_ALT).toString(), equalTo("1|-"));
        assertThat(SampleGenotype.phased(AlleleCall.OTHER_ALT, AlleleCall.ALT).toString(), equalTo("-|1"));
        assertThat(SampleGenotype.phased(AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT).toString(), equalTo("-|-"));

    }

    @Test
    public void testMonoploid() {
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL).toString(), equalTo("."));

        assertThat(SampleGenotype.of(AlleleCall.ALT).toString(), equalTo("1"));
        assertThat(SampleGenotype.of(AlleleCall.REF).toString(), equalTo("0"));

        assertThat(SampleGenotype.phased(AlleleCall.ALT).toString(), equalTo("1"));
        assertThat(SampleGenotype.phased(AlleleCall.REF).toString(), equalTo("0"));
    }

    @Test
    public void testTriploid() {
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL, AlleleCall.NO_CALL).toString(), equalTo("././."));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("0/1/1"));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("1|0|1"));
    }

    @Test
    public void testEquals(){
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)));

        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT), equalTo(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF), not(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));

        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF), not(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));
    }
}