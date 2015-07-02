/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import htsjdk.tribble.readers.TabixReader;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jj8
 */
@Repository
public class TabixPathogenicityDao implements PathogenicityDao{

    private final Logger logger = LoggerFactory.getLogger(PathogenicityDao.class);
    private final TabixReader inDelTabixReader;
    private final TabixReader snvTabixReader;
    

    public TabixPathogenicityDao(TabixReader inDelTabixReader, TabixReader snvTabixReader) {
        this.inDelTabixReader = inDelTabixReader;
        this.snvTabixReader = snvTabixReader;
    }
    
    
        public PathogenicityData getPathogenicityData(Variant variant) {
        return processResults(variant);
    }

    PathogenicityData processResults(Variant variant) {
        try {
            String chromosome = Integer.toString(variant.getChromosome());
            if (chromosome.equals("23")) {
                chromosome = "X";
            }
            if (chromosome.equals("24")) {
                chromosome = "Y";
            }
            String ref = variant.getRef();
            String alt = variant.getAlt();
            int start = variant.getPosition();
            // deal with fact that deletion coordinates are handled differently by Jannovar
            // TODO ? if this is working
            if (alt.equals("-")) {
                start = start - 1;
            }

            float cadd = Float.NaN;
            CaddScore caddScore = null;
            String line;
            
                
            // indel
            if (ref.equals("-") || alt.equals("-")) {
                TabixReader.Iterator results = inDelTabixReader.query(chromosome + ":" + start + "-" + start);
                while ((line = results.next()) != null) {
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
                        cadd = Float.parseFloat(elements[5]);
                        cadd = 1 - (float) Math.pow(10, -(cadd / 10));//rescale log10-Phred based score to a value between 0 and 1
                        continue;
                    }
                }
                if (!Float.isNaN(cadd)) {
                    caddScore = new CaddScore(cadd);
                }
            } else {// query SNV file
                TabixReader.Iterator results = snvTabixReader.query(chromosome + ":" + start + "-" + start);
                while ((line = results.next()) != null) {
                    String[] elements = line.split("\t");
                    String caddRef = elements[2];
                    String caddAlt = elements[3];
                    if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                        cadd = Float.parseFloat(elements[5]);
                        cadd = 1 - (float) Math.pow(10, -(cadd / 10));//rescale log10-Phred based score to a value between 0 and 1
                        continue;
                    }
                }
                if (!Float.isNaN(cadd)) {
                    caddScore = new CaddScore(cadd);
                }
            }
            return new PathogenicityData(caddScore);

        } catch (IOException e) {
        }

        return new PathogenicityData();
    }
}
