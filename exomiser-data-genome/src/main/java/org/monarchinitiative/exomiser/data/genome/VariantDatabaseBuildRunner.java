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

package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.indexers.AlleleIndexer;
import org.monarchinitiative.exomiser.data.genome.indexers.MvStoreAlleleIndexer;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantDatabaseBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(VariantDatabaseBuildRunner.class);

    private final Path buildPath;
    private final String buildString;
    private final List<AlleleResource> alleleResources;

    public VariantDatabaseBuildRunner(Path buildPath, String buildString, List<AlleleResource> alleleResources) {
        this.buildPath = buildPath;
        this.buildString = buildString;
        this.alleleResources = alleleResources;
    }

    public void run() {
        MVStore mvStore = new MVStore.Builder()
                .fileName(buildPath.resolve(buildString + "_variants_temp.mv.db").toString())
                .compress()
                .open();

        AlleleIndexer alleleIndexer = new MvStoreAlleleIndexer(mvStore);
        alleleResources.forEach(alleleIndexer::index);

        MVStore finalStore = new MVStore.Builder()
                .fileName(buildPath.resolve(buildString + "_variants.mv.db").toString())
                .compress()
                .open();

        // MVStore stands for Multi-VersionStore. Alleles appearing in multiple datasets will have a new version stored
        // for each new dataset added.
        // These multiple versions can make the initial store much larger than we need as we only want the final version
        // of the allele. So as a workaround we're copying the entries from the original store to a new store which
        // will only contain one version of each allele. This leads to significant space savings on disk - e.g. 25 GB original
        // is only 12 GB when the final version is copied over. This operation takes about 40 min for 0.5 billion alleles.
        copyToNewInstance(mvStore, finalStore);

        mvStore.close();
        finalStore.close();
    }

    private void copyToNewInstance(MVStore mvStore, MVStore newStore) {
        MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> map = mvStore.openMap("alleles", MvStoreUtil.alleleMapBuilder());

        MVMap<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> newMap = newStore.openMap("alleles", MvStoreUtil.alleleMapBuilder());

        logger.info("Copying {} entries from temp store to final store",map.size());
        int count = 0;
        for (Map.Entry<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
            count++;
            if (count % 10000000 == 0) {
                logger.info("Written {} alleles", count);
            }
        }
        logger.info("Finished copying {} entries to new map", newMap.size());
    }
}
