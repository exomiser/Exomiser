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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SampleGenotypesTest {

    @Test
    public void testMapConstructorSingleSample() {
        SampleGenotypes instance = SampleGenotypes.of("Bart", SampleGenotype.het());
        assertThat(instance.size(), equalTo(1));
        assertThat(instance.getSampleGenotype("Bart"), equalTo(SampleGenotype.het()));
        assertThat(instance.getSampleCopyNumber("Bart"), equalTo(CopyNumber.empty()));
    }

    @Test
    public void testMapConstructorMultiSample() {
        SampleGenotypes instance = SampleGenotypes.of("Bart", SampleGenotype.het(), "Lisa", SampleGenotype.homRef());
        assertThat(instance.size(), equalTo(2));
        assertThat(instance.getSampleGenotype("Bart"), equalTo(SampleGenotype.het()));
        assertThat(instance.getSampleCopyNumber("Bart"), equalTo(CopyNumber.empty()));

        assertThat(instance.getSampleGenotype("Lisa"), equalTo(SampleGenotype.homRef()));
        assertThat(instance.getSampleCopyNumber("Lisa"), equalTo(CopyNumber.empty()));
    }

    @Test
    public void testConstructorWithSampleData() {
        SampleData bart = SampleData.of("Bart", SampleGenotype.het(), 4);
        SampleData lisa = SampleData.of("Lisa", SampleGenotype.homRef(), 2);
        SampleGenotypes instance = SampleGenotypes.of(bart, lisa);

        assertThat(instance.size(), equalTo(2));
        assertThat(instance.getSampleGenotype("Bart"), equalTo(SampleGenotype.het()));
        assertThat(instance.getSampleCopyNumber("Bart"), equalTo(CopyNumber.of(4)));

        assertThat(instance.getSampleGenotype("Lisa"), equalTo(SampleGenotype.homRef()));
        assertThat(instance.getSampleCopyNumber("Lisa"), equalTo(CopyNumber.of(2)));


    }

    @Test
    void testGetSampleData() {
        SampleData bart = SampleData.of("Bart", SampleGenotype.het(), 4);
        SampleData lisa = SampleData.of("Lisa", SampleGenotype.homRef(), 2);
        SampleGenotypes instance = SampleGenotypes.of(bart, lisa);

        assertThat(instance.getSampleData(), equalTo(List.of(bart, lisa)));
        assertThat(instance.getSampleData("Bart"), equalTo(bart));
    }
}