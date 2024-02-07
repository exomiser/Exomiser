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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProtoFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * MVStore implementation of the {@link AllelePropertiesDao}
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */

public class AllelePropertiesDaoMvStore implements AllelePropertiesDao {

    private static final Logger logger = LoggerFactory.getLogger(AllelePropertiesDaoMvStore.class);

    private final MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> map;

    public AllelePropertiesDaoMvStore(MVStore mvStore) {
        map = MvStoreUtil.openAlleleMVMap(mvStore);
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.allele", key = "#alleleKey", condition = "#genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.allele", key = "#alleleKey", condition = "#genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public AlleleProto.AlleleProperties getAlleleProperties(AlleleProto.AlleleKey alleleKey, GenomeAssembly genomeAssembly) {
        AlleleProto.AlleleProperties alleleProperties = map.getOrDefault(alleleKey, AlleleProto.AlleleProperties.getDefaultInstance());
        if (logger.isDebugEnabled()) {
            logger.debug("{} {}", AlleleProtoFormatter.format(alleleKey), AlleleProtoFormatter.format(alleleProperties));
        }
        return alleleProperties;
    }

    @Override
    public AlleleProto.AlleleProperties getAlleleProperties(Variant variant) {
        return getAlleleProperties(variant.alleleKey(), variant.getGenomeAssembly());
    }

}
