/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import htsjdk.tribble.readers.TabixReader;
import jannovar.exome.Variant;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jj8
 */
@Repository
public class TabixPathogenicityDao implements PathogenicityDao {

    private final Logger logger = LoggerFactory.getLogger(PathogenicityDao.class);

    public PathogenicityData getPathogenicityData(Variant variant) {
        return processResults(variant);
    }

    PathogenicityData processResults(Variant variant) {
        try {
            String chromosome = Integer.toString(variant.get_chromosome());
            if (chromosome.equals("23")) {
                chromosome = "X";
            }
            if (chromosome.equals("24")) {
                chromosome = "Y";
            }
            String ref = variant.get_ref();
            String alt = variant.get_alt();
            int start = variant.get_position();
            // deal with fact that deletion coordinates are handled differently by Jannovar
            // TODO ? if this is working
            if (alt.equals("-")) {
                start = start - 1;
            }

            float cadd = Float.NaN;
            CaddScore caddScore = null;
            String line;
            
            System.out.println("LOOKING FOR " + chromosome + ":" + start + "-" + start + " " + ref + " " + alt);
                
            // indel
            if (ref.equals("-") || alt.equals("-")) {
                //TabixReader tabixReader = new TabixReader("/Users/ds5/exomiser-testing/exomiser-cli-4.0.1/InDels.tsv.gz");
                TabixReader tabixReader = new TabixReader("/warehouse/team110_wh01/ds5/InDels.tsv.gz");
                TabixReader.Iterator results = tabixReader.query(chromosome + ":" + start + "-" + start);
                while ((line = results.next()) != null) {
                    String[] elements = line.split("\t");
                    // deal with fact that Jannovar represents indels differently
                    String caddRef = elements[2];
                    //if (elements[2].length() > 1)
                    caddRef = elements[2].substring(1);
                    String caddAlt = elements[2];
                    //if (elements[3].length() > 1)
                    caddAlt = elements[3].substring(1);
                    if (caddRef.equals("")) {
                        caddRef = "-";
                    }
                    if (caddAlt.equals("")) {
                        caddAlt = "-";
                    }
                    if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                        cadd = Float.parseFloat(elements[5]);
                        cadd = 1 - (float) Math.pow(10, -(cadd / 10));//rescale log10-Phred based score to a value between 0 and 1
                        //System.out.println("FOUND INDEL " + chromosome + ":" + start + "-" + start + " " + ref + " " + alt);
                        continue;
                    }
                }
                if (!Float.isNaN(cadd)) {
                    caddScore = new CaddScore(cadd);
                }
            } else {// query SNV file
                TabixReader tabixReader = new TabixReader("/warehouse/team110_wh01/ds5/whole_genome_SNVs.tsv.gz");
                TabixReader.Iterator results = tabixReader.query(chromosome + ":" + start + "-" + start);
                while ((line = results.next()) != null) {
                    String[] elements = line.split("\t");
                    String caddRef = elements[2];
                    String caddAlt = elements[3];
                    if (caddRef.equals(ref) && caddAlt.equals(alt)) {
                        cadd = Float.parseFloat(elements[5]);
                        cadd = 1 - (float) Math.pow(10, -(cadd / 10));//rescale log10-Phred based score to a value between 0 and 1
                        //System.out.println("FOUND SNV " + chromosome + ":" + start + "-" + start + " " + ref + " " + alt);
                        continue;
                    }
                }

                if (!Float.isNaN(cadd)) {
                    caddScore = new CaddScore(cadd);
                }
            }
            return new PathogenicityData(null, null, null, caddScore);

        } catch (IOException e) {
        }

        return null;
    }
}
