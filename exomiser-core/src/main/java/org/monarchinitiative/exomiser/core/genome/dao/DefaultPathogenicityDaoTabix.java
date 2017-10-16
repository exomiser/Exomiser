/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultPathogenicityDaoTabix implements PathogenicityDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPathogenicityDaoTabix.class);

    private static final String EMPTY_FIELD = ".";

    private final TabixDataSource tabixDataSource;

    public DefaultPathogenicityDaoTabix(TabixDataSource tabixDataSource) {
        this.tabixDataSource = tabixDataSource;
    }

    @Cacheable(value = "pathogenicity")
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        //if a variant is not classified as missense then we don't need to hit
        //the database as we're going to assign it a constant pathogenicity score.
        VariantEffect variantEffect = variant.getVariantEffect();
        if (variantEffect != VariantEffect.MISSENSE_VARIANT) {
            return PathogenicityData.empty();
        }

        //the exomiser tabix files use the integer representation of the chromosome
        String chromosome = Integer.toString(variant.getChromosome());
//        String chromosome = variant.getChromosomeName();
        int start = variant.getPosition();
        int end = variant.getPosition();
        return getPathogenicityData(chromosome, start, end, variant.getRef(), variant.getAlt());
    }

    private PathogenicityData getPathogenicityData(String chromosome, int start, int end, String ref, String alt) {

        List<String> results = getTabixLines(chromosome, start, end);
        for (String line : results) {
            String[] elements = line.split("\t");
            if (ref.equals(elements[3]) && alt.equals(elements[4])) {
                if (EMPTY_FIELD.equals(elements[2]) && EMPTY_FIELD.equals(elements[7])) {
                    return PathogenicityData.empty();
                }
                Map<String, Float> values = infoFieldToMap(elements[7]);
                return parsePathogenicityData(values);
            }
        }
        return PathogenicityData.empty();
    }

    private Map<String, Float> infoFieldToMap(String info) {
        String[] infoFields = info.split(";");
        Map<String, Float> values = new HashMap<>();
        for (String infoField : infoFields) {
            String[] keyValue = infoField.split("=");
            if (keyValue.length == 2) {
                values.put(keyValue[0], Float.valueOf(keyValue[1]));
            }
        }
        return values;
    }

    private PathogenicityData parsePathogenicityData(Map<String, Float> values) {
        List<PathogenicityScore> pathogenicityScores = new ArrayList<>();
        for (Map.Entry<String, Float> field : values.entrySet()) {
            String key = field.getKey();
            Float value = field.getValue();
            if (key.startsWith("SIFT")) {
                pathogenicityScores.add(SiftScore.valueOf(value));
            }
            if (key.startsWith("POLYPHEN")) {
                pathogenicityScores.add(PolyPhenScore.valueOf(value));
            }
            if (key.startsWith("MUT_TASTER")) {
                pathogenicityScores.add(MutationTasterScore.valueOf(value));
            }
        }
        return PathogenicityData.of(pathogenicityScores);
    }

    private List<String> getTabixLines(String chromosome, int start, int end) {
        List<String> lines = new ArrayList<>();
        try {
            String line;
            TabixReader.Iterator results = tabixDataSource.query(chromosome + ":" + start + "-" + end);
            while ((line = results.next()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            logger.error("Unable to read from exomiser tabix file {}", tabixDataSource.getSource(), e);
        }
        return lines;
    }
}
