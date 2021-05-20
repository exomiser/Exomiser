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

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;
import org.monarchinitiative.svart.VariantType;

import java.util.ArrayList;
import java.util.List;

public class DecipherSvFreqParser implements Parser<SvFrequency> {

    @Override
    public List<SvFrequency> parseLine(String line) {
        if (line.startsWith("#")) {
            return List.of();
        }

        String[] tokens = line.split("\t");
        // #population_cnv_id      chr     start   end     deletion_observations   deletion_frequency      deletion_standard_error duplication_observations        duplication_frequency   duplication_standard_error      observations    frequency       standard_error  type    sample_size     study
        // 8       1       40718   731985  38      0.044970414     0.158531882     49      0.057988166     0.138653277     87      0.10295858      0.101542213     0       845     DDD
        String decipherId = tokens[0];
        int chr = Contigs.parseId(tokens[1]);
        int start = Integer.parseInt(tokens[2]);
        int end = Integer.parseInt(tokens[3]);

        int del_obs = Integer.parseInt(tokens[4]);
        int dup_obs = Integer.parseInt(tokens[7]);
        int obs = Integer.parseInt(tokens[10]);

        int sampleSize = Integer.parseInt(tokens[14]);

        List<SvFrequency> alleles = new ArrayList<>(2);
        if (del_obs > 0) {
            alleles.add(new SvFrequency(chr, start, end, 0, VariantType.DEL, "", "DECIPHER", decipherId, del_obs, sampleSize));
        }
        if (dup_obs > 0) {
            alleles.add(new SvFrequency(chr, start, end, 0, VariantType.DUP, "", "DECIPHER", decipherId, dup_obs, sampleSize));
        }

        return List.copyOf(alleles);
    }
}
