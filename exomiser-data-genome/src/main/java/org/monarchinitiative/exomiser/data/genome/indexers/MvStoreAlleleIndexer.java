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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MVStore} backed {@link AlleleIndexer} implementation.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreAlleleIndexer extends AbstractAlleleIndexer {

    private final MVStore mvStore;
    private final MVMap<AlleleKey, AlleleProperties> map;

    public MvStoreAlleleIndexer(MVStore mvStore) {
        this.mvStore = mvStore;
        this.map = mvStore.openMap("alleles", MvStoreUtil.alleleMapBuilder());
    }

    @Override
    public void writeAllele(Allele allele) {
        AlleleKey key = toAlleleKey(allele);
        AlleleProperties properties = toAlleleProperties(allele);
        if (map.containsKey(key)) {
            AlleleProperties originalProperties = getOriginalProperties(key);
            AlleleProperties mergedValue = mergeProperties(originalProperties, properties);
            map.put(key, mergedValue);
        } else {
            map.put(key, properties);
        }
    }

    private AlleleProperties getOriginalProperties(AlleleKey key) {
        return map.get(key);
    }

    private AlleleProperties mergeProperties(AlleleProperties originalProperties, AlleleProperties properties) {
        String updatedRsId = (originalProperties.getRsId()
                .isEmpty()) ? properties.getRsId() : originalProperties.getRsId();
        return AlleleProperties.newBuilder()
                .mergeFrom(originalProperties)
                .mergeFrom(properties)
                //original rsid would have been overwritten by the new one - we don't necessarily want that, so re-set it now.
                .setRsId(updatedRsId)
                .build();
    }

    private AlleleProperties toAlleleProperties(Allele allele) {
        Map<String, Float> properties = convertToStringKeyMap(allele.getValues());
        return AlleleProperties.newBuilder()
                .setRsId(allele.getRsId())
                .putAllProperties(properties)
                .build();
    }

    private Map<String, Float> convertToStringKeyMap(Map<AlleleProperty, Float> values) {
        Map<String, Float> properties = new HashMap<>();
        for (Map.Entry<AlleleProperty, Float> entry : values.entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue());
        }
        return properties;
    }

    private AlleleKey toAlleleKey(Allele allele) {
        return AlleleKey.newBuilder()
                .setChr(allele.getChr())
                .setPosition(allele.getPos())
                .setRef(allele.getRef())
                .setAlt(allele.getAlt())
                .build();
    }

    @Override
    public long count() {
        return map.size();
    }

    @Override
    public void close() {
        mvStore.close();
    }

}
