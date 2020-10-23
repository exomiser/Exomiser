/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.data.genome.indexers.Indexer;
import org.monarchinitiative.exomiser.data.genome.indexers.MvStoreAlleleIndexer;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Main class for handling parsing of the {@link AlleleResource} and reading these into the variants.mv.db database.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantDatabaseBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(VariantDatabaseBuildRunner.class);

    private final Path buildPath;
    private final BuildInfo buildInfo;
    private final List<AlleleResource> alleleResources;

    public VariantDatabaseBuildRunner(BuildInfo buildInfo, Path buildPath, List<AlleleResource> alleleResources) {
        this.buildPath = buildPath;
        this.buildInfo = buildInfo;
        this.alleleResources = alleleResources;
    }

    public void run() {
        MVStore mergeStore = new MVStore.Builder()
                .fileName(buildPath.resolve(buildInfo.getBuildString() + "_variants_temp.mv.db").toString())
                .compress()
                .open();

        // This is threadsafe and can be run in parallel. However, the throughput is significantly slower,
        // to the extent that the overall time is the same, at least on my machine (4 cores) it is.
        // This holds true both using parallelStream and a fixed thread pool executor with only 2 threads.
        Indexer<Allele> alleleIndexer = new MvStoreAlleleIndexer(mergeStore);
        alleleResources.forEach(alleleIndexer::index);

        MVStore finalStore = new MVStore.Builder()
                .fileName(buildPath.resolve(buildInfo.getBuildString() + "_variants.mv.db").toString())
                .compress()
                .open();

        // MVStore stands for Multi-Version Store. Alleles appearing in multiple datasets will have a new version stored
        // for each new dataset added.
        // These multiple versions can make the initial store much larger than we need as we only want the final version
        // of the allele. So as a workaround we're copying the entries from the original store to a new store which
        // will only contain one version of each allele. This leads to significant space savings on disk - e.g. 25 GB original
        // is only 12 GB when the final version is copied over. This operation takes about 40 min for 0.5 billion alleles.
        copyToNewInstance(mergeStore, finalStore);

        mergeStore.close();
        finalStore.close();
    }

    private void copyToNewInstance(MVStore mvStore, MVStore newStore) {
        MVMap<AlleleKey, AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);

        MVMap<AlleleKey, AlleleProperties> newMap = MvStoreUtil.openAlleleMVMap(newStore);

        logger.info("Copying {} entries from temp store {} to final store {}", map.size(), mvStore.getFileStore().getFileName(), newStore.getFileStore().getFileName());
        int count = 0;
        for (Map.Entry<AlleleKey, AlleleProperties> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
            count++;
            if (count % 10000000 == 0) {
                logger.info("Written {} alleles", count);
            }
        }
        logger.info("Finished copying {} entries to new map", newMap.size());
    }
}
