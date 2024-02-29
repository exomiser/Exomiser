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

package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreTool;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
        String fileName = buildPath.resolve(buildInfo.getBuildString() + "_variants.mv.db").toString();
        MVStore mvStore = new MVStore.Builder()
                .fileName(fileName)
                .compress()
                .open();
        // this is key to keep the size of the store down when building otherwise it gets enormous
        mvStore.setVersionsToKeep(0);
        // This is threadsafe and can be run in parallel. However, the throughput is significantly slower,
        // to the extent that the overall time is the same, at least on my machine (4 cores) it is.
        // This holds true both using parallelStream and a fixed thread pool executor with only 2 threads.
        try (Indexer<Allele> alleleIndexer = new MvStoreAlleleIndexer(mvStore)) {
            alleleResources.forEach(alleleIndexer::index);
        } catch (IOException e ) {
            throw new IllegalStateException("Error writing to MVStore " + fileName, e);
        }

        // super-important step for producing as small a store as possible, Could double (or more?) when this is in progress
        logger.info("Compacting store...");
        MVStoreTool.compact(fileName, true);
    }
}
