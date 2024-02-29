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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDataSourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(GenomeDataSourceLoader.class);

    private final GenomeProperties genomeProperties;
    private final GenomeDataResolver genomeDataResolver;

    private final JannovarData jannovarData;
    private final MVStore allelePropsMvStore;
    private final MVStore clinVarMvStore;

    private final VariantWhiteList variantWhiteList;

    //TabixDataSources
    private final TabixDataSource localFrequencyTabixDataSource;
    private final TabixDataSource caddSnvTabixDataSource;
    private final TabixDataSource caddIndelTabixDataSource;
    private final TabixDataSource remmTabixDataSource;
    private final TabixDataSource testPathogenicityTabixDataSource;

    public GenomeDataSourceLoader(GenomeProperties genomeProperties, GenomeDataResolver genomeDataResolver) {
        logger.debug("Loading {} genome data sources...", genomeProperties.getAssembly());
        this.genomeProperties = genomeProperties;
        this.genomeDataResolver = genomeDataResolver;

        this.jannovarData = loadJannovarData();
        // n.b. the JannovarData can be loaded asynchronously, but it takes about a second longer to do so. Meanwhile the rest
        // of the data requiring loading here takes about 1-2 secs, so there isn't really a lot to gain from all the concurrency
        // shenanigans, but I've left the code here as a reminder.
        // start this here as it'll take a while longer than all the others put together
//        CompletableFuture<JannovarData> jannovarDataFuture = loadJannovarDataAsync();
        this.allelePropsMvStore = loadAllelePropsMvStore();
        this.clinVarMvStore = loadClinVarMvStore();
        this.variantWhiteList = loadVariantWhiteList();

        this.localFrequencyTabixDataSource = getTabixDataSourceOrDefault("LOCAL", genomeProperties.getLocalFrequencyPath());
        this.caddSnvTabixDataSource = getTabixDataSourceOrDefault("CADD snv", genomeProperties.getCaddSnvPath());
        this.caddIndelTabixDataSource = getTabixDataSourceOrDefault("CADD InDel", genomeProperties.getCaddInDelPath());
        this.remmTabixDataSource = getTabixDataSourceOrDefault("REMM", genomeProperties.getRemmPath());
        this.testPathogenicityTabixDataSource = getTabixDataSourceOrDefault("TEST", genomeProperties.getTestPathogenicityScorePath());
//        this.jannovarData = jannovarDataFuture.join();
        logger.debug("{} genome data sources loaded", genomeProperties.getAssembly());
    }

    private CompletableFuture<JannovarData> loadJannovarDataAsync() {
        // Can't wait for Project Loom to arrive!
//        try (ExecutorService executorService = Executors.newUnboundedVirtualThreadExecutor()) {
//            return CompletableFuture.supplyAsync(this::loadJannovarData, executorService);
//        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture<JannovarData> jannovarDataFuture = CompletableFuture.supplyAsync(this::loadJannovarData, executorService);
        executorService.shutdown();
        return jannovarDataFuture;
    }

    private JannovarData loadJannovarData() {
        Path transcriptFilePath = genomeDataResolver.getTranscriptFilePath();
        logger.debug("Loading transcript data from {}", transcriptFilePath);
        return JannovarDataSourceLoader.loadJannovarData(transcriptFilePath);
    }

    private MVStore loadAllelePropsMvStore() {
        Path mvStoreAbsolutePath = genomeDataResolver.getVariantsMvStorePath();
        logger.debug("Opening variants MVStore from {}", mvStoreAbsolutePath);
        return MvStoreDataSourceLoader.openMvStore(mvStoreAbsolutePath);
    }

    private MVStore loadClinVarMvStore() {
        Path clinVarMvStoreAbsolutePath = genomeDataResolver.getClinVarMvStorePath();
        logger.debug("Opening ClinVar MVStore from {}", clinVarMvStoreAbsolutePath);
        return MvStoreDataSourceLoader.openMvStore(clinVarMvStoreAbsolutePath);
    }

    private VariantWhiteList loadVariantWhiteList() {
        Path variantWhiteListPath = genomeDataResolver.resolvePathOrNullIfEmpty(genomeProperties.getVariantWhiteListPath());
        Set<AlleleProto.AlleleKey> clinVarWhiteList = genomeProperties.useClinVarWhiteList() ? ClinVarWhiteListReader.readVariantWhiteList(clinVarMvStore) : Set.of();
        Set<AlleleProto.AlleleKey> userWhiteList = variantWhiteListPath != null ? VariantWhiteListReader.readVariantWhiteList(variantWhiteListPath) : Set.of();
        // merge clinvar and user whitelists into final whitelist
        Set<AlleleProto.AlleleKey> whiteList = new HashSet<>(clinVarWhiteList);
        whiteList.addAll(userWhiteList);
        logger.info("Loaded {} whitelist variants", whiteList.size());
        return InMemoryVariantWhiteList.of(Set.copyOf(whiteList));
    }

    private TabixDataSource getTabixDataSourceOrDefault(String dataSourceName, String tabixPath) {
        Path path = genomeDataResolver.resolvePathOrNullIfEmpty(tabixPath);
        if (path != null) {
            logger.info("Opening {} data from source: {}", dataSourceName, path);
            return TabixDataSourceLoader.load(path);
        } else {
            logger.debug("Data for {} is not configured. THIS WILL LEAD TO ERRORS IF REQUIRED DURING ANALYSIS. Check the application.properties is pointing to a valid file.", dataSourceName);
            String message = "Data for " + dataSourceName + " is not configured. Check the application.properties is pointing to a valid file.";
            return new ErrorThrowingTabixDataSource(message);
        }
    }

    public JannovarData getJannovarData() {
        return jannovarData;
    }

    public MVStore getAllelePropsMvStore() {
        return allelePropsMvStore;
    }

    public MVStore getClinVarMvStore() {
        return clinVarMvStore;
    }

    public VariantWhiteList getVariantWhiteList() {
        return variantWhiteList;
    }

    public TabixDataSource getLocalFrequencyTabixDataSource() {
        return localFrequencyTabixDataSource;
    }

    public TabixDataSource getCaddSnvTabixDataSource() {
        return caddSnvTabixDataSource;
    }

    public TabixDataSource getCaddIndelTabixDataSource() {
        return caddIndelTabixDataSource;
    }

    public TabixDataSource getRemmTabixDataSource() {
        return remmTabixDataSource;
    }

    public TabixDataSource getTestPathogenicityTabixDataSource() {
        return testPathogenicityTabixDataSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomeDataSourceLoader that = (GenomeDataSourceLoader) o;
        return Objects.equals(jannovarData, that.jannovarData) &&
                Objects.equals(allelePropsMvStore, that.allelePropsMvStore) &&
                Objects.equals(localFrequencyTabixDataSource, that.localFrequencyTabixDataSource) &&
                Objects.equals(caddSnvTabixDataSource, that.caddSnvTabixDataSource) &&
                Objects.equals(caddIndelTabixDataSource, that.caddIndelTabixDataSource) &&
                Objects.equals(remmTabixDataSource, that.remmTabixDataSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jannovarData, allelePropsMvStore, localFrequencyTabixDataSource, caddSnvTabixDataSource, caddIndelTabixDataSource, remmTabixDataSource);
    }

}
