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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

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
        this.map = MvStoreUtil.openAlleleMVMap(mvStore);
    }

    @Override
    public void writeAllele(Allele allele) {
        AlleleKey key = AlleleConverter.toAlleleKey(allele);
        AlleleProperties properties = AlleleConverter.toAlleleProperties(allele);
        AlleleProperties originalProperties = map.get(key);
        if (originalProperties != null) {
            AlleleProperties mergedValue = AlleleConverter.mergeProperties(originalProperties, properties);
            map.put(key, mergedValue);
        } else {
            map.put(key, properties);
        }
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
