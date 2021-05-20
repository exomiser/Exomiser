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

package org.monarchinitiative.exomiser.data.genome.model.parsers.sv;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.svart.VariantType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DecipherSvFreqParserTest implements SvParserTest {

    private final DecipherSvFreqParser instance = new DecipherSvFreqParser();


    @Test
    public void testIgnoresHeader() {
        String header = "#population_cnv_id\tchr\tstart\tend\tdeletion_observations\tdeletion_frequency\tdeletion_standard_error duplication_observations\tduplication_frequency\tduplication_standard_error\tobservations\tfrequency\tstandard_error\ttype\tsample_size\tstudy\n";
        assertThat(instance.parseLine(header), equalTo(List.of()));
    }

    @Test
    public void multiAllelic() {
        List<SvFrequency> result = instance.parseLine("8\t1\t40718\t731985\t38\t0.044970414\t0.158531882\t49\t0.057988166\t0.138653277\t87\t0.10295858\t0.101542213\t0\t845\tDDD\n");
        List<SvFrequency> expected = List.of(
                new SvFrequency(1, 40718, 731985, -691268, VariantType.DEL, "", "DECIPHER", "8", 38, 845),
                new SvFrequency(1, 40718, 731985, 691268, VariantType.DUP, "", "DECIPHER", "8", 49, 845)
        );
        assertThat(result, equalTo(expected));
    }

    @Test
    @Override
    public void deletion() {
        List<SvFrequency> result = instance.parseLine("51\t1\t738636\t742073\t137\t0.072410148\t0.082284439\t0\t0\t1\t137\t0.072410148\t0.082284439\t-1\t1892\t1G del genotyped\n");
        assertThat(result, equalTo(List.of(new SvFrequency(1, 738636, 742073, -3438, VariantType.DEL, "", "DECIPHER", "51", 137, 1892))));
    }

    @Test
    @Override
    public void duplication() {
        List<SvFrequency> result = instance.parseLine("41932\t13\t113516402\t113520302\t0\t0\t1\t11\t0.275\t0.256727659\t11\t0.275\t0.256727659\t1\t40\t42M calls\n");
        assertThat(result, equalTo(List.of(new SvFrequency(13, 113516402, 113520302, 3901, VariantType.DUP, "", "DECIPHER", "41932", 11, 40))));
    }

    @Override
    public void insertion() {
        // empty - not present in dataset
    }

    @Override
    public void inversion() {
        // empty - not present in dataset
    }

    @Override
    public void cnvGain() {
        // empty - not present in dataset
    }

    @Override
    public void cnvLoss() {
        // empty - not present in dataset
    }

    @Override
    public void breakend() {
        // empty - not present in dataset
    }
}