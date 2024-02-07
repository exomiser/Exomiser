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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.RemmScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.IOException;

import static org.monarchinitiative.svart.VariantType.INS;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class RemmDao implements PathogenicityDao {

    private final Logger logger = LoggerFactory.getLogger(RemmDao.class);

    private final TabixDataSource remmTabixDataSource;

    public RemmDao(TabixDataSource remmTabixDataSource) {
        this.remmTabixDataSource = remmTabixDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.remm", key = "#variant.alleleKey()", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.remm", key = "#variant.alleleKey()", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        logger.debug("Getting REMM data for {}", variant);
        // REMM has not been trained on missense variants so skip these
        if (variant.getVariantEffect() == VariantEffect.MISSENSE_VARIANT) {
            return PathogenicityData.empty();
        }
        String chromosome = variant.contigName();
        int start = variant.start();
        // test bases either side of insertion or all bases of a SNV/MNV/DEL
        int end = variant.variantType() == INS ? variant.end() + 1 : variant.end();
        return getRemmData(chromosome, start, end);
    }

    private synchronized PathogenicityData getRemmData(String chromosome, int start, int end) {
        try {
            float score = Float.NaN;
            String line;
            TabixReader.Iterator results = remmTabixDataSource.query(chromosome, start, end);
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                if (Float.isNaN(score)) {
                    score = Float.parseFloat(elements[2]);
                } else {
                    score = Math.max(score, Float.parseFloat(elements[2]));
                }
            }
            if (!Float.isNaN(score)) {
                return PathogenicityData.of(RemmScore.of(score));
            }
        } catch (IOException e) {
            logger.error("Unable to read from REMM tabix file {}", remmTabixDataSource.getSource(), e);
        }
        return PathogenicityData.empty();
    }

}
