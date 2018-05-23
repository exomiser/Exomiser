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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for holding the file locations of resources to be used by the Exomiser. They can either be manually
 * provided by the user using the Builder or resolved automatically using a {@link GenomeProperties} instance and a {@link Path}
 * to the Exomiser data directory where the data release directories are stored.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class GenomeDataSources {

    private static final Logger logger = LoggerFactory.getLogger(GenomeDataSources.class);

    private Path transcriptFilePath;
    private Path mvStorePath;
    private DataSource genomeDataSource;

    // Tabix files
    private Path localFrequencyPath;
    private Path caddSnvPath;
    private Path caddIndelPath;
    private Path remmPath;

    /**
     * Static constructor which will automatically resolve the resources for the supplied {@code GenomeProperties} where
     * the data directory for the data version and genome assembly are to be found on the {@code exomiserDataDirectory}
     * {@code Path}
     *
     * @param genomeProperties
     * @param exomiserDataDirectory
     * @return
     */
    public static GenomeDataSources from(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        logger.debug("Locating resources for {} assembly (data-version={}, transcript-source={})", genomeProperties.getAssembly(), genomeProperties
                .getDataVersion(), genomeProperties.getTranscriptSource());

        GenomeDataResolver genomeDataResolver = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);

        Path transcriptFilePath = buildTranscriptPath(genomeProperties, genomeDataResolver);
        Path mvStoreFilePath = buildMvStorePath(genomeDataResolver);
        DataSource genomeDataSource = buildGenomeDataSource(genomeProperties, genomeDataResolver);

        Path localFreqPath = resolvePathOrNullIfEmpty(genomeProperties.getLocalFrequencyPath(), genomeDataResolver);
        Path caddSnvPath = resolvePathOrNullIfEmpty(genomeProperties.getCaddSnvPath(), genomeDataResolver);
        Path caddIndelPath = resolvePathOrNullIfEmpty(genomeProperties.getCaddInDelPath(), genomeDataResolver);
        Path remmPath = resolvePathOrNullIfEmpty(genomeProperties.getRemmPath(), genomeDataResolver);

        return GenomeDataSources.builder()
                .transcriptFilePath(transcriptFilePath)
                .mvStorePath(mvStoreFilePath)
                .genomeDataSource(genomeDataSource)
                .localFrequencyPath(localFreqPath)
                .caddSnvPath(caddSnvPath)
                .caddIndelPath(caddIndelPath)
                .remmPath(remmPath)
                .build();
    }

    private static Path buildTranscriptPath(GenomeProperties genomeProperties, GenomeDataResolver genomeDataResolver) {
        TranscriptSource transcriptSource = genomeProperties.getTranscriptSource();
        //e.g 1710_hg19_transcripts_ucsc.ser
        String transcriptFileNameValue = String.format("%s_transcripts_%s.ser", genomeDataResolver.getVersionAssemblyPrefix(), transcriptSource
                .toString());
        return genomeDataResolver.getGenomeAssemblyDataPath().resolve(transcriptFileNameValue);
    }

    private static Path buildMvStorePath(GenomeDataResolver genomeDataResolver) {
        String mvStoreFileName = String.format("%s_variants.mv.db", genomeDataResolver.getVersionAssemblyPrefix());
        return genomeDataResolver.resolveAbsoluteResourcePath(mvStoreFileName);
    }

    private static DataSource buildGenomeDataSource(GenomeProperties genomeProperties, GenomeDataResolver genomeDataResolver) {
        logger.debug("{}", genomeProperties.getDatasource());
        //omit the .h2.db extensions
        String dbFileName = String.format("%s_genome", genomeDataResolver.getVersionAssemblyPrefix());
        Path dbPath = genomeDataResolver.resolveAbsoluteResourcePath(dbFileName);
        String startUpArgs = ";MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;AUTO_RECONNECT=TRUE;ACCESS_MODE_DATA=r;";
        String jdbcUrl = String.format("jdbc:h2:file:%s%s", dbPath, startUpArgs);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(3);
        config.setPoolName(String.format("exomiser-genome-%s-%s", genomeProperties.getAssembly(), genomeProperties.getDataVersion()));

        return new HikariDataSource(config);
    }

    private static Path resolvePathOrNullIfEmpty(String pathToTabixGzFile, GenomeDataResolver genomeDataResolver) {
        if (pathToTabixGzFile == null || pathToTabixGzFile.isEmpty()) {
            return null;
        }
        return genomeDataResolver.resolveAbsoluteResourcePath(pathToTabixGzFile);
    }

    private GenomeDataSources(Builder builder) {
        this.transcriptFilePath = builder.transcriptFilePath;
        this.genomeDataSource = builder.genomeDataSource;
        this.mvStorePath = builder.mvStorePath;

        this.localFrequencyPath = builder.localFrequencyPath;
        this.caddSnvPath = builder.caddSnvPath;
        this.caddIndelPath = builder.caddIndelPath;
        this.remmPath = builder.remmPath;
    }

    public Path getTranscriptFilePath() {
        return transcriptFilePath;
    }

    public Path getMvStorePath() {
        return mvStorePath;
    }

    public DataSource getGenomeDataSource() {
        return genomeDataSource;
    }

    public Optional<Path> getLocalFrequencyPath() {
        return Optional.ofNullable(localFrequencyPath);
    }

    public Optional<Path> getCaddSnvPath() {
        return Optional.ofNullable(caddSnvPath);
    }

    /**
     * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     */
    public Optional<Path> getCaddIndelPath() {
        return Optional.ofNullable(caddIndelPath);
    }

    public Optional<Path> getRemmPath() {
        return Optional.ofNullable(remmPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomeDataSources that = (GenomeDataSources) o;
        return Objects.equals(transcriptFilePath, that.transcriptFilePath) &&
                Objects.equals(mvStorePath, that.mvStorePath) &&
                Objects.equals(genomeDataSource, that.genomeDataSource) &&
                Objects.equals(localFrequencyPath, that.localFrequencyPath) &&
                Objects.equals(caddSnvPath, that.caddSnvPath) &&
                Objects.equals(caddIndelPath, that.caddIndelPath) &&
                Objects.equals(remmPath, that.remmPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transcriptFilePath, mvStorePath, genomeDataSource, localFrequencyPath, caddSnvPath, caddIndelPath, remmPath);
    }

    @Override
    public String toString() {
        return "GenomeDataSources{" +
                "transcriptFilePath=" + transcriptFilePath +
                ", mvStorePath=" + mvStorePath +
                ", genomeDataSource=" + genomeDataSource +
                ", localFrequencyPath=" + localFrequencyPath +
                ", caddSnvPath=" + caddSnvPath +
                ", caddIndelPath=" + caddIndelPath +
                ", remmPath=" + remmPath +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Path transcriptFilePath;
        private Path mvStorePath;
        private DataSource genomeDataSource;

        //These are all expected to be null as they are optional data sources
        private Path localFrequencyPath = null;
        private Path caddSnvPath = null;
        private Path caddIndelPath = null;
        private Path remmPath = null;

        public Builder transcriptFilePath(Path transcriptFilePath) {
            Objects.requireNonNull(transcriptFilePath);
            this.transcriptFilePath = transcriptFilePath;
            return this;
        }

        public Builder mvStorePath(Path mvStorePath) {
            Objects.requireNonNull(mvStorePath);
            this.mvStorePath = mvStorePath;
            return this;
        }

        public Builder genomeDataSource(DataSource genomeDataSource) {
            Objects.requireNonNull(genomeDataSource);
            this.genomeDataSource = genomeDataSource;
            return this;
        }

        /**
         * Optional full system path to local frequency .tsv.gz and .tsv.gz.tbi file pair.
         */
        public Builder localFrequencyPath(Path localFrequencyPath) {
            this.localFrequencyPath = localFrequencyPath;
            return this;
        }

        /**
         * Optional full system path to CADD whole_genome_SNVs.tsv.gz and whole_genome_SNVs.tsv.gz.tbi file pair.
         * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
         */
        public Builder caddSnvPath(Path caddSnvPath) {
            this.caddSnvPath = caddSnvPath;
            return this;
        }

        /**
         * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
         * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
         */
        public Builder caddIndelPath(Path caddIndelPath) {
            this.caddIndelPath = caddIndelPath;
            return this;
        }

        /**
         * Optional full system path to REMM remmData.tsv.gz and remmData.tsv.gz.tbi file pair.
         * <p>
         * Default is empty and will return no data.
         */
        public Builder remmPath(Path remmPath) {
            this.remmPath = remmPath;
            return this;
        }

        public GenomeDataSources build() {
            Objects.requireNonNull(transcriptFilePath);
            Objects.requireNonNull(mvStorePath);
            Objects.requireNonNull(genomeDataSource);
            return new GenomeDataSources(this);
        }
    }
}
