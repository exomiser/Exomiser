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

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultFrequencyDaoMvStore implements FrequencyDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFrequencyDaoMvStore.class);

    private final MVMap<String, String> map;

    private static final Map<String, FrequencySource> FREQUENCY_SOURCE_MAP = FrequencySource.FREQUENCY_SOURCE_MAP;

    public DefaultFrequencyDaoMvStore(MVStore mvStore) {
        String frequencyMapName = "alleles";
        if (!mvStore.hasMap(frequencyMapName)) {
            logger.warn("MVStore does not contain map {}", frequencyMapName);
        }

        this.map = mvStore.openMap(frequencyMapName);

        if (map.isEmpty()) {
            logger.warn("MVStore map {} does not contain any data", frequencyMapName);
        }
    }

    @Cacheable(value = "frequency", keyGenerator = "variantKeyGenerator")
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        String info = getInfoField(variant);
        logger.debug(info);
        if (info.isEmpty()) {
            return FrequencyData.empty();
        }
        return parseFrequencyData(info);
    }

    private String getInfoField(Variant variant) {
        String key = VariantSerialiser.generateKey(variant);
        logger.debug("Getting FREQ data for {}", key);
        return map.getOrDefault(key, "");
    }

    private FrequencyData parseFrequencyData(String info) {
        Map<String, String> values = VariantSerialiser.infoFieldToMap(info);

        RsId rsId = RsId.valueOf(values.get("RS"));
        List<Frequency> frequencies = parseFrequencyData(values);
        return FrequencyData.of(rsId, frequencies);
    }

    private List<Frequency> parseFrequencyData(Map<String, String> values) {
        List<Frequency> frequencies = new ArrayList<>();
        for (Map.Entry<String, String> field : values.entrySet()) {
            String key = field.getKey();
            if (FREQUENCY_SOURCE_MAP.containsKey(key)) {
                float value = Float.parseFloat(field.getValue());
                FrequencySource source = FREQUENCY_SOURCE_MAP.get(key);
                frequencies.add(Frequency.valueOf(value, source));
            }
        }
        return frequencies;
    }
}
