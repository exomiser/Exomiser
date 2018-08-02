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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.genome.dao.RegulatoryFeatureDao;
import org.monarchinitiative.exomiser.core.genome.dao.TabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TadDao;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Acts as a manual version of Spring component discovery and DI. This is required as there can be more than one
 * {@link org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService} present to provide data for different genome
 * assemblies. The {@link org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService} has multiple layers of
 * dependencies and as such is not a simple to construct, hence this class.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class GenomeAnalysisServiceConfigurer implements GenomeAnalysisServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GenomeAnalysisServiceConfigurer.class);

    private final GenomeProperties genomeProperties;

    protected final DataSource dataSource;
    protected final JannovarData jannovarData;
    protected final MVStore mvStore;

    //Optional user-provided TabixDataSources
    protected final TabixDataSource localFrequencyTabixDataSource;
    protected final TabixDataSource caddSnvTabixDataSource;
    protected final TabixDataSource caddIndelTabixDataSource;
    protected final TabixDataSource remmTabixDataSource;

    public GenomeAnalysisServiceConfigurer(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        this.genomeProperties = genomeProperties;
        logger.debug("Loading data sources for {} {} {}", genomeProperties.getDataVersion(), genomeProperties.getAssembly(), genomeProperties.getTranscriptSource());
        GenomeDataSources genomeDataSources = GenomeDataSources.from(genomeProperties, exomiserDataDirectory);
        GenomeDataSourceLoader genomeDataSourceLoader = GenomeDataSourceLoader.load(genomeDataSources);
        this.dataSource = genomeDataSourceLoader.getGenomeDataSource();
        this.jannovarData = genomeDataSourceLoader.getJannovarData();
        this.mvStore = genomeDataSourceLoader.getMvStore();

        this.localFrequencyTabixDataSource = genomeDataSourceLoader.getLocalFrequencyTabixDataSource();
        this.caddSnvTabixDataSource = genomeDataSourceLoader.getCaddSnvTabixDataSource();
        this.caddIndelTabixDataSource = genomeDataSourceLoader.getCaddIndelTabixDataSource();
        this.remmTabixDataSource = genomeDataSourceLoader.getRemmTabixDataSource();
    }

    /**
     * Only one instance of an MVStore can access the store on disk at a time in a single JVM. This prevents tests failing
     * when the store hasn't been properly closed.
     */
    @PreDestroy
    public void closeMvStore() {
        mvStore.close();
    }

    protected VariantAnnotator buildVariantAnnotator() {
        ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex = genomeDataService().getRegulatoryRegionIndex();
        return new JannovarVariantAnnotator(genomeProperties.getAssembly(), jannovarData, regulatoryRegionIndex);
    }

    protected VariantFactory buildVariantFactory() {
        return new VariantFactoryImpl(variantAnnotator());
    }

    //This method is calling the public interface of the concrete implementation so that the caching works on the DAOs
    protected VariantDataService buildVariantDataService() {
        return VariantDataServiceImpl.builder()
                .defaultFrequencyDao(defaultFrequencyDao())
                .localFrequencyDao(localFrequencyDao())
                .pathogenicityDao(pathogenicityDao())
                .remmDao(remmDao())
                .caddDao(caddDao())
                .build();
    }

    protected GenomeDataService buildGenomeDataService() {
        RegulatoryFeatureDao regulatoryFeatureDao = new RegulatoryFeatureDao(dataSource);
        TadDao tadDao = new TadDao(dataSource);
        GeneFactory geneFactory = new GeneFactory(jannovarData);
        return new GenomeDataServiceImpl(geneFactory, regulatoryFeatureDao, tadDao);
    }

    // The protected methods here are exposed so that the concrete sub-classes can call these as a bean method in order that
    // Spring can intercept any caching annotations, but otherwise keep the duplicated GenomeAnalysisServices separate from
    // any autowiring and autoconfiguration which will cause name clashes.
    protected GenomeAnalysisService buildGenomeAnalysisService() {
        return new GenomeAnalysisServiceImpl(genomeProperties.getAssembly(), genomeDataService(), variantDataService(), variantFactory());
    }
}
