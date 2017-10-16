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

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
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
public class DefaultFrequencyDaoTabix implements FrequencyDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFrequencyDaoTabix.class);

    private static final String EMPTY_FIELD = ".";
    private static final Map<String, FrequencySource> FREQUENCY_SOURCE_MAP;

    static {
        FREQUENCY_SOURCE_MAP = new HashMap<>();
        FREQUENCY_SOURCE_MAP.put("KG", FrequencySource.THOUSAND_GENOMES);
        FREQUENCY_SOURCE_MAP.put("ESP_AA", FrequencySource.ESP_AFRICAN_AMERICAN);
        FREQUENCY_SOURCE_MAP.put("ESP_EA", FrequencySource.ESP_EUROPEAN_AMERICAN);
        FREQUENCY_SOURCE_MAP.put("ESP_ALL", FrequencySource.ESP_ALL);
        FREQUENCY_SOURCE_MAP.put("EXAC_AFR", FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN);
        FREQUENCY_SOURCE_MAP.put("EXAC_AMR", FrequencySource.EXAC_AMERICAN);
        FREQUENCY_SOURCE_MAP.put("EXAC_EAS", FrequencySource.EXAC_EAST_ASIAN);
        FREQUENCY_SOURCE_MAP.put("EXAC_FIN", FrequencySource.EXAC_FINNISH);
        FREQUENCY_SOURCE_MAP.put("EXAC_NFE", FrequencySource.EXAC_NON_FINNISH_EUROPEAN);
        FREQUENCY_SOURCE_MAP.put("EXAC_SAS", FrequencySource.EXAC_SOUTH_ASIAN);
        FREQUENCY_SOURCE_MAP.put("EXAC_OTH", FrequencySource.EXAC_OTHER);
    }

    private final TabixDataSource tabixDataSource;

    public DefaultFrequencyDaoTabix(TabixDataSource tabixDataSource) {
        this.tabixDataSource = tabixDataSource;
    }

    @Cacheable(value = "frequency")
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        //the exomiser tabix files use the integer representation of the chromosome
        String chromosome = Integer.toString(variant.getChromosome());
//        String chromosome = variant.getChromosomeName();
        int start = variant.getPosition();
        int end = variant.getPosition();
        return getFrequencyData(chromosome, start, end, variant.getRef(), variant.getAlt());
    }

    private FrequencyData getFrequencyData(String chromosome, int start, int end, String ref, String alt) {

        List<String> results = getTabixLines(chromosome, start, end);
        for (String line : results) {
            String[] elements = line.split("\t");
            if (ref.equals(elements[3]) && alt.equals(elements[4])) {
                if (EMPTY_FIELD.equals(elements[2]) && EMPTY_FIELD.equals(elements[7])) {
                    return FrequencyData.empty();
                }
                Map<String, Float> values = infoFieldToMap(elements[7]);
                RsId rsId = RsId.valueOf(elements[2]);
                List<Frequency> frequencies = parseFrequencyData(values);
                return FrequencyData.of(rsId, frequencies);
            }
        }
        return FrequencyData.empty();
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

    private List<Frequency> parseFrequencyData(Map<String, Float> values) {
        List<Frequency> frequencies = new ArrayList<>();
        for (Map.Entry<String, Float> field : values.entrySet()) {
            String key = field.getKey();
            Float value = field.getValue();
            if (FREQUENCY_SOURCE_MAP.containsKey(key)) {
                FrequencySource source = FREQUENCY_SOURCE_MAP.get(key);
                frequencies.add(Frequency.valueOf(value, source));
            }
        }
        return frequencies;
    }

    //add this to the TabixDataSource? List<String> TabixDataSource.query(String chromosome, int start, int end)
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
