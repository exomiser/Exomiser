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
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultFrequencyDaoMvStoreProto implements FrequencyDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFrequencyDaoMvStoreProto.class);

    private final MVMap<AlleleKey, AlleleProperties> map;

    private static final Map<String, FrequencySource> FREQUENCY_SOURCE_MAP = FrequencySource.FREQUENCY_SOURCE_MAP;

    public DefaultFrequencyDaoMvStoreProto(MVStore mvStore) {
        String frequencyMapName = "alleles";
        if (!mvStore.hasMap(frequencyMapName)) {
            logger.warn("MVStore does not contain map {}", frequencyMapName);
        }

        this.map = mvStore.openMap(frequencyMapName, MvStoreUtil.alleleMapBuilder());

        if (map.isEmpty()) {
            logger.warn("MVStore map {} does not contain any data", frequencyMapName);
        } else {
            logger.info("MVStore map {} opened with {} entries", frequencyMapName, map.size());
        }
    }

    @Cacheable(value = "frequency", keyGenerator = "variantKeyGenerator")
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        AlleleKey key = MvStoreUtil.generateAlleleKey(variant);
        AlleleProperties info = map.getOrDefault(key, AlleleProperties.getDefaultInstance());
        logger.debug("{} {}", key, info);
        if (info.equals(AlleleProperties.getDefaultInstance())) {
            return FrequencyData.empty();
        }
        return parseFrequencyData(info);
    }

    private FrequencyData parseFrequencyData(AlleleProperties info) {
        RsId rsId = RsId.valueOf(info.getRsId());
        List<Frequency> frequencies = parseFrequencyData(info.getPropertiesMap());
        return FrequencyData.of(rsId, frequencies);
    }

    private List<Frequency> parseFrequencyData(Map<String, Float> values) {
        List<Frequency> frequencies = new ArrayList<>();
        for (Map.Entry<String, Float> field : values.entrySet()) {
            String key = field.getKey();
            if (FREQUENCY_SOURCE_MAP.containsKey(key)) {
                float value = field.getValue();
                FrequencySource source = FREQUENCY_SOURCE_MAP.get(key);
                frequencies.add(Frequency.valueOf(value, source));
            }
        }
        return frequencies;
    }
}
