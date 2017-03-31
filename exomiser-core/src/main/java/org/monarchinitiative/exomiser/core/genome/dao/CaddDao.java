/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.CaddScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
 
/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class CaddDao {
 
    private final Logger logger = LoggerFactory.getLogger(CaddDao.class);
 
    private final TabixReader inDelTabixReader;
    private final TabixReader snvTabixReader;
 
    @Autowired
    public CaddDao(TabixReader inDelTabixReader, TabixReader snvTabixReader) {
        this.inDelTabixReader = inDelTabixReader;
        this.snvTabixReader = snvTabixReader;
    }
 
    @Cacheable(value = "cadd", key = "#variant.hgvsGenome")
    public PathogenicityData getPathogenicityData(Variant variant) {
        return processResults(variant);
    }
 
    PathogenicityData processResults(Variant variant) {
        String chromosome = variant.getChromosomeName();
        String ref = variant.getRef();
        String alt = variant.getAlt();
        int start = variant.getPosition();
        if (isIndel(ref, alt)) {
            // deal with fact that deletion coordinates are handled differently by Jannovar
            if (alt.equals("-")) {
                start -= 1;
            }
            return getIndelCaddPathogenicityData(chromosome, start, ref, alt);
        }
        
        return getSnvCaddPathogenicityData(chromosome, start, ref, alt);
    }
 
    private boolean isIndel(String ref, String alt) {
        return ref.equals("-") || alt.equals("-");
    }
 
    private PathogenicityData getIndelCaddPathogenicityData(String chromosome, int start, String ref, String alt) {
        try {
            TabixReader.Iterator results = inDelTabixReader.query(chromosome + ":" + start + "-" + start);
            String line;
            //there can be 0 - N results
            while ((line = results.next()) != null) {
                //return format:
                //chr   pos ref alt rawS    wantedScore 
                //1 2000    A   T   -0.324  3.21
                String[] elements = line.split("\t");
                // deal with fact that Jannovar represents indels differently
                String caddRef = elements[2].substring(1);
                String caddAlt = elements[3].substring(1);
                if (caddRef.equals("")) {
                    caddRef = "-";
                }
                if (caddAlt.equals("")) {
                    caddAlt = "-";
                }
                if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                    return makeCaddPathData(elements[5]);
                }
                
            }
        } catch (IOException e) {
            logger.error("Unable to read from Indel tabix file {}", inDelTabixReader.getSource(), e);
        }
        return PathogenicityData.EMPTY_DATA;
    }
 
    private PathogenicityData getSnvCaddPathogenicityData(String chromosome, int start, String ref, String alt) {
        try {
            // query SNV file
            TabixReader.Iterator results = snvTabixReader.query(chromosome + ":" + start + "-" + start);
            String line;
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String caddRef = elements[2];
                String caddAlt = elements[3];
                if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                    return makeCaddPathData(elements[5]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from SNV tabix file {}", snvTabixReader.getSource(), e);
        }        
        return PathogenicityData.EMPTY_DATA;
    }
 
    private PathogenicityData makeCaddPathData(String phredScaledCaddScore) {
        CaddScore caddScore = parseCaddScore(phredScaledCaddScore);
        return new PathogenicityData(caddScore);
    }
 
    private CaddScore parseCaddScore(String phredScaledCaddScore) throws NumberFormatException {
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