/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.NcdsScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.tribble.readers.TabixReader;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jj8
 */
@Repository
public class NCDSDao {

    private final Logger logger = LoggerFactory.getLogger(NCDSDao.class);
    private TabixReader ncdsTabixReader;

    public NCDSDao(TabixReader ncdsTabixReader) {
        this.ncdsTabixReader = ncdsTabixReader;
    }

    @Cacheable(value = "mncds", key = "#variant.chromosomalVariant")
    public PathogenicityData getPathogenicityData(Variant variant) {
        // MNCDS has not been trained on missense variants so skip
        if (variant.getVariantEffect() == VariantEffect.MISSENSE_VARIANT) {
            return new PathogenicityData();
        }
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
            int end = variant.getPosition();
            // TODO - checking dealing correctly with fact that deletion coordinates are handled differently by Jannovar
            if (alt.equals("-")) {// deletion
                //start = start - 1;
                end = end + ref.length();// test all deleted bases
            } else if (ref.equals("-")) {// insertion
                end = end + 1;// test bases either side of insertion
            }
            float ncds = Float.NaN;
            NcdsScore ncdsScore = null;
            String line;
            //logger.info("Running tabix with " + chromosome + ":" + start + "-" + end);
            TabixReader.Iterator results = ncdsTabixReader.query(chromosome + ":" + start + "-" + end);
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                //logger.info(elements[2]);
                if (Float.isNaN(ncds)) {
                    ncds = Float.parseFloat(elements[2]);
                }
                else {
                    ncds = Math.max(ncds, Float.parseFloat(elements[2]));
                }
            }
            //logger.info("Final score " + ncds);
            if (!Float.isNaN(ncds)) {
                ncdsScore = new NcdsScore(ncds);
            }

            return new PathogenicityData(ncdsScore);

        } catch (IOException e) {
        }

        return new PathogenicityData();
    }
}
