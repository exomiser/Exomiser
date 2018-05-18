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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultPathogenicityDaoMvStoreProto implements PathogenicityDao {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPathogenicityDaoMvStoreProto.class);

    private final MVMap<AlleleKey, AlleleProperties> map;

    public DefaultPathogenicityDaoMvStoreProto(MVStore mvStore) {
        map = MvStoreUtil.openAlleleMVMap(mvStore);
    }

    @Cacheable(value = "pathogenicity", keyGenerator = "variantKeyGenerator")
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        AlleleKey key = MvStoreUtil.generateAlleleKey(variant);
        // Prior to version 10.1.0 this would only look-up MISSENSE variants, but this would miss out scores for stop/start
        // gain/loss an other possible SNV scores from the bundled pathogenicity databases as well as any ClinVar annotations.
        AlleleProperties alleleProperties = map.getOrDefault(key, AlleleProperties.getDefaultInstance());
        logger.debug("{} {}", key, alleleProperties);
        return AlleleProtoAdaptor.toPathogenicityData(alleleProperties);
    }
}
