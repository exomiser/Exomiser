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
    private final Path processedDir;

    public VariantDatabaseBuildRunner(BuildInfo buildInfo, Path buildPath, List<AlleleResource> alleleResources, Path processedDir) {
        this.buildPath = buildPath;
        this.buildInfo = buildInfo;
        this.alleleResources = alleleResources;
        this.processedDir = processedDir;
    }

    public void run() {
        // process all alleleResources into separate .mv stores in the buildDir/processed directory
        for (AlleleResource alleleResource : alleleResources) {
            processResource(alleleResource, processedDir.resolve(buildInfo.getVersion() + "_" + alleleResource.getName() + ".mv.db"));
        }
    }

    private void processResource(AlleleResource alleleResource, Path processedResource) {
        try (MVStore mvStore = VariantMvStores.openMvStore(processedResource);
             Indexer<Allele> alleleIndexer = new MvStoreAlleleIndexer(mvStore)) {
            alleleIndexer.index(alleleResource);
            logger.info("Written {} alleles to {} store", alleleIndexer.count(), alleleResource.getName());
            mvStore.compactMoveChunks();
            logger.info("Closing {} store", alleleResource.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Error processing resource " + alleleResource.getName(), e);
        }
        // super-important step for producing as small a store as possible, Could double (or more?) when this is in progress
        logger.info("Compacting {} store...", alleleResource.getName());
        MVStoreTool.compact(processedResource.toString(), true);
    }
}
