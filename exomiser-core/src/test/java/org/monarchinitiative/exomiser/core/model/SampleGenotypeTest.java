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

package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void testConvenienceConstructors() {
        assertThat(SampleGenotype.noCall(), equalTo(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL)));
        assertThat(SampleGenotype.het(), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.homRef(), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.REF)));
        assertThat(SampleGenotype.homAlt(), equalTo(SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT)));
    }

    @Test
    void testIsHet() {
        assertThat(SampleGenotype.het().isHet(), is(true));

        assertThat(SampleGenotype.of(AlleleCall.REF).isHet(), is(false));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.NO_CALL).isHet(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT).isHet(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT, AlleleCall.REF).isHet(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT).isHet(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT).isHet(), is(true));
        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.OTHER_ALT).isHet(), is(true));

        assertThat(SampleGenotype.empty().isHet(), is(false));
        assertThat(SampleGenotype.homRef().isHet(), is(false));
        assertThat(SampleGenotype.homAlt().isHet(), is(false));
    }

    @Test
    void testIsHomRef() {
        assertThat(SampleGenotype.homRef().isHomRef(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF).isHomRef(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.REF).isHomRef(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.REF, AlleleCall.REF).isHomRef(), is(true));
        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.REF).isHomRef(), is(true));

        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.NO_CALL).isHomRef(), is(false));
        assertThat(SampleGenotype.empty().isHomRef(), is(false));
        assertThat(SampleGenotype.het().isHomRef(), is(false));
        assertThat(SampleGenotype.homAlt().isHomRef(), is(false));
    }

    @Test
    void testIsHomAlt() {
        assertThat(SampleGenotype.homAlt().isHomAlt(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.ALT).isHomAlt(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT).isHomAlt(), is(true));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT, AlleleCall.ALT).isHomAlt(), is(true));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.ALT).isHomAlt(), is(true));

        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.NO_CALL).isHomAlt(), is(false));
        assertThat(SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.NO_CALL).isHomAlt(), is(false));
        assertThat(SampleGenotype.empty().isHomAlt(), is(false));
        assertThat(SampleGenotype.het().isHomAlt(), is(false));
        assertThat(SampleGenotype.homRef().isHomAlt(), is(false));
    }

    @Test
    void testIsPhased() {
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT).isPhased(), is(false));
        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT).isPhased(), is(true));
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
        assertThat(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL, AlleleCall.NO_CALL)
                .toString(), equalTo("././."));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("0/1/1"));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF, AlleleCall.ALT).toString(), equalTo("1|0|1"));
    }

    @Test
    void testNumCalls() {
        assertThat(SampleGenotype.empty().numCalls(), equalTo(0));
        SampleGenotype monoploid = SampleGenotype.of(AlleleCall.REF);
        assertThat(monoploid.numCalls(), equalTo(1));

        SampleGenotype diploid = SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT);
        assertThat(diploid.numCalls(), equalTo(2));

        SampleGenotype triploid = SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL, AlleleCall.NO_CALL);
        assertThat(triploid.numCalls(), equalTo(3));
    }

    @Test
    public void testEquals() {
        assertThat(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)));

        assertThat(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT), equalTo(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF), not(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));

        assertThat(SampleGenotype.of(AlleleCall.ALT, AlleleCall.REF), not(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));
    }

    @Test
    void testParseGenotype() {
        // empty
        assertThat(SampleGenotype.parseGenotype(null), equalTo(SampleGenotype.empty()));
        assertThat(SampleGenotype.parseGenotype(""), equalTo(SampleGenotype.empty()));
        assertThat(SampleGenotype.parseGenotype("NA"), equalTo(SampleGenotype.empty()));

        // no call
        assertThat(SampleGenotype.parseGenotype("null"), equalTo(SampleGenotype.empty()));

        // monoploid
        assertThat(SampleGenotype.parseGenotype("-"), equalTo(SampleGenotype.of(AlleleCall.OTHER_ALT)));
        assertThat(SampleGenotype.parseGenotype("."), equalTo(SampleGenotype.of(AlleleCall.NO_CALL)));
        assertThat(SampleGenotype.parseGenotype("0"), equalTo(SampleGenotype.of(AlleleCall.REF)));
        assertThat(SampleGenotype.parseGenotype("1"), equalTo(SampleGenotype.of(AlleleCall.ALT)));

        // diploid
        assertThat(SampleGenotype.parseGenotype("1/-"), equalTo(SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT)));
        assertThat(SampleGenotype.parseGenotype("./."), equalTo(SampleGenotype.noCall()));
        assertThat(SampleGenotype.parseGenotype("0/0"), equalTo(SampleGenotype.homRef()));
        assertThat(SampleGenotype.parseGenotype("0/1"), equalTo(SampleGenotype.het()));
        assertThat(SampleGenotype.parseGenotype("1/1"), equalTo(SampleGenotype.homAlt()));

        // diploid phased
        assertThat(SampleGenotype.parseGenotype("1|-"), equalTo(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.OTHER_ALT)));
        assertThat(SampleGenotype.parseGenotype(".|."), equalTo(SampleGenotype.phased(AlleleCall.NO_CALL, AlleleCall.NO_CALL)));
        assertThat(SampleGenotype.parseGenotype("0|0"), equalTo(SampleGenotype.phased(AlleleCall.REF, AlleleCall.REF)));
        assertThat(SampleGenotype.parseGenotype("0|1"), equalTo(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT)));
        assertThat(SampleGenotype.parseGenotype("1|0"), equalTo(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF)));
        assertThat(SampleGenotype.parseGenotype("1|1"), equalTo(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.ALT)));
        assertThat(SampleGenotype.parseGenotype("1|2"), equalTo(SampleGenotype.phased(AlleleCall.ALT, AlleleCall.OTHER_ALT)));

        // triploid
        assertThat(SampleGenotype.parseGenotype("-/-/-"), equalTo(SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT)));
        assertThat(SampleGenotype.parseGenotype("././."), equalTo(SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL, AlleleCall.NO_CALL)));
        assertThat(SampleGenotype.parseGenotype("0/1/0"), equalTo(SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT, AlleleCall.REF)));
        assertThat(SampleGenotype.parseGenotype("1/1/1"), equalTo(SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT, AlleleCall.ALT)));
        assertThat(SampleGenotype.parseGenotype("0|1|0"), equalTo(SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT, AlleleCall.REF)));
    }

    @Test
    void testIsNoCall() {
        assertThat(SampleGenotype.parseGenotype("").isNoCall(), equalTo(true));
        assertThat(SampleGenotype.parseGenotype(".").isNoCall(), equalTo(true));
        assertThat(SampleGenotype.parseGenotype("./.").isNoCall(), equalTo(true));
        assertThat(SampleGenotype.parseGenotype("././.").isNoCall(), equalTo(true));

        assertThat(SampleGenotype.parseGenotype("-").isNoCall(), equalTo(false));
        assertThat(SampleGenotype.parseGenotype("0/1").isNoCall(), equalTo(false));
        assertThat(SampleGenotype.parseGenotype("1/1/2").isNoCall(), equalTo(false));
    }
}