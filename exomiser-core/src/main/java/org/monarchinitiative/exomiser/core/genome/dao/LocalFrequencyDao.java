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

package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.IOException;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class LocalFrequencyDao implements FrequencyDao {

    private final Logger logger = LoggerFactory.getLogger(LocalFrequencyDao.class);

    private final TabixDataSource tabixDataSource;

    public LocalFrequencyDao(TabixDataSource localFrequencyTabixDataSource) {
        this.tabixDataSource = localFrequencyTabixDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.local", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.local", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        logger.debug("Getting LOCAL_FREQ data for {}", variant);
        return processResults(variant);
    }

    private FrequencyData processResults(Variant variant) {
//        logger.info("Fetching data for {}", variant);
        String chromosome = variant.getChromosomeName();
        String ref = variant.getRef();
        String alt = variant.getAlt();
        int start = variant.getPosition();

        return getPositionFrequencyData(chromosome, start, ref, alt);
    }

    private FrequencyData getPositionFrequencyData(String chromosome, int start, String ref, String alt) {
        //Local frequency file defined as tab-delimited lines in 'VCF-lite' format:
        //chr   pos ref alt freq(%)
        //1 12345   A   T   23.0  (an A->T SNP on chr1 at position 12345 with frequency of 23.0%)
        //1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with frequency of 0.01%)
        //note in the usual VCF format these would be on a single line
        //1 12345   AT   G   0.02  (an AT->G deletion on chr1 at position 12345 with frequency of 0.02%)
        //1 12345   T   .   0.03  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with frequency of 0.03%)
        try {
            TabixReader.Iterator results = tabixDataSource.query(chromosome + ":" + start + "-" + start);
            String line;
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String refField = elements[2];
                String altField = elements[3];
                if (refField.equals(ref) && altField.equals(alt)) {
                    return parseLocalFrequency(elements[4]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from local frequency tabix file {}", tabixDataSource.getSource(), e);
        }
        return FrequencyData.empty();
    }

    private FrequencyData parseLocalFrequency(String frequencyInPercentField) {
        float value = Float.parseFloat(frequencyInPercentField);
        Frequency localFreq = Frequency.valueOf(value, FrequencySource.LOCAL);
        return FrequencyData.of(RsId.empty(), localFreq);
    }
}
