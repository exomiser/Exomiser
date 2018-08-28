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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.CaddScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.IOException;
 
/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CaddDao {
 
    private final Logger logger = LoggerFactory.getLogger(CaddDao.class);

    private final TabixDataSource caddInDelTabixDataSource;
    private final TabixDataSource caddSnvTabixDataSource;

    public CaddDao(TabixDataSource caddInDelTabixDataSource, TabixDataSource caddSnvTabixDataSource) {
        this.caddInDelTabixDataSource = caddInDelTabixDataSource;
        this.caddSnvTabixDataSource = caddSnvTabixDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.cadd", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.cadd", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    public PathogenicityData getPathogenicityData(Variant variant) {
        logger.debug("Getting CADD data for {}", variant);
        return processResults(variant);
    }

    private PathogenicityData processResults(Variant variant) {
        String chromosome = variant.getChromosomeName();
        String ref = variant.getRef();
        String alt = variant.getAlt();
        int start = variant.getPosition();
        if (AllelePosition.isSnv(ref, alt)) {
            return getCaddPathogenicityData(caddSnvTabixDataSource, chromosome, start, ref, alt);
        }
        return getCaddPathogenicityData(caddInDelTabixDataSource, chromosome, start, ref, alt);
    }

    private PathogenicityData getCaddPathogenicityData(TabixDataSource tabixDataSource, String chromosome, int start, String ref, String alt) {
        try {
            TabixReader.Iterator results = tabixDataSource.query(chromosome + ":" + start + "-" + start);
            String line;
            //there can be 0 - N results in this format:
            //#Chrom  Pos     Ref     Alt     RawScore        PHRED
            //2       14962   C       CA      -0.138930       1.458
            //2       14962   C       CAA     -0.155009       1.356
            //2       14962   CA      C       0.194173        4.618
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String caddRef = elements[2];
                String caddAlt = elements[3];
                if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                    return makeCaddPathData(elements[5]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from CADD tabix file {}", tabixDataSource.getSource(), e);
        }
        return PathogenicityData.empty();
    }
 
    private PathogenicityData makeCaddPathData(String phredScaledCaddScore) {
        CaddScore caddScore = parseCaddScore(phredScaledCaddScore);
        return PathogenicityData.of(caddScore);
    }

    private CaddScore parseCaddScore(String phredScaledCaddScore) {
        float score = Float.parseFloat(phredScaledCaddScore);
        float cadd = rescaleLogTenBasedScore(score);
        return CaddScore.valueOf(cadd);
    }
 
    /**
     * rescales a log10-Phred based score to a value between 0 and 1
     */
    private float rescaleLogTenBasedScore(float caddRaw) {
        return 1 - (float) Math.pow(10, -(caddRaw / 10));
    }
}