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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.datasource.DataSource;
import de.charite.compbio.jannovar.datasource.DataSourceFactory;
import de.charite.compbio.jannovar.datasource.DatasourceOptions;
import de.charite.compbio.jannovar.datasource.InvalidDataSourceException;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for downloading, creating and persisting {@link JannovarData}. This class aims to automate/guide most
 * of the steps for producing Jannovar data programmatically. It supports creating hg19 and hg38 builds of the ENSEMBL,
 * UCSC and RefSeq data sets.
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(JannovarDataFactory.class);

    private final DataSourceFactory dataSourceFactory;
    private final boolean printProgressBars;
    private final Path downloadDir;

    private JannovarDataFactory(Builder builder) {
        this.dataSourceFactory = builder.dataSourceFactory;
        this.printProgressBars = builder.printProgressBars;
        this.downloadDir = builder.downloadDir.toAbsolutePath();
    }

    /**
     * Downloads, builds and writes a serialised {@link JannovarData} file to the specified output path. The serialised
     * format written by this method is the Exomiser protobuf format. This format offers 2-3x faster loading speeds than
     * the native format.
     *
     * @param assembly the desired {@link GenomeAssembly}
     * @param source   the desired {@link TranscriptSource}
     * @param outPath  the output file to which the {@link JannovarData} should be written
     */
    public void buildAndWrite(GenomeAssembly assembly, TranscriptSource source, Path outPath) {
        Objects.requireNonNull(outPath);
        JannovarData data = buildData(assembly, source);
        JannovarDataProtoSerialiser.save(outPath, data);
    }

    /**
     * Downloads, builds and writes a serialised {@link JannovarData} file to the specified output path. The serialised
     * format written by this method is the Jannovar native format.
     *
     * @param assembly the desired {@link GenomeAssembly}
     * @param source   the desired {@link TranscriptSource}
     * @param outPath  the output file to which the {@link JannovarData} should be written
     */
    public void buildAndWriteNative(GenomeAssembly assembly, TranscriptSource source, Path outPath) {
        Objects.requireNonNull(outPath);
        JannovarData data = buildData(assembly, source);
        JannovarDataSerializer serializer = new JannovarDataSerializer(outPath.toAbsolutePath().toString());
        try {
            serializer.save(data);
        } catch (SerializationException e) {
            logger.error("Jannovar error", e);
        }
    }

    /**
     * Downloads the source files and builds a {@link JannovarData} object for the {@link GenomeAssembly} and {@link TranscriptSource}
     * specified.
     *
     * @param assembly the desired {@link GenomeAssembly}
     * @param source   the desired {@link TranscriptSource}
     * @return
     * @throws JannovarException on unsupported {@link GenomeAssembly} or {@link TranscriptSource}
     */
    public JannovarData buildData(GenomeAssembly assembly, TranscriptSource source)  {
        String name = createJannovarName(assembly, source);
        try{
            logger.info("Downloading/parsing for data source {}", name);
            DataSource dataSource = dataSourceFactory.getDataSource(name);
            return dataSource.getDataFactory().build(downloadDir.toString(), printProgressBars, Collections.emptyList());
        } catch (Exception e) {
            throw new JannovarException(e);
        }
    }

    private String createJannovarName(GenomeAssembly assembly, TranscriptSource source) {
        Objects.requireNonNull(assembly);
        Objects.requireNonNull(source);
        return assembly.toString() + "/" + source;
    }

    public static Builder builder(Path iniFile) {
        return new Builder(iniFile);
    }

    public static class Builder {

        private URL httpProxy = null;
        private URL httpsProxy = null;
        private URL ftpProxy = null;

        private final Path iniFile;

        private boolean printProgressBars = false;
        private Path downloadDir = null;

        private DataSourceFactory dataSourceFactory;

        public Builder(Path iniFile) {
            Objects.requireNonNull(iniFile, "Jannovar iniFile cannot be null");
            this.iniFile = iniFile;
        }

        public Builder httpProxy(URL httpProxy) {
            this.httpProxy = httpProxy;
            return this;
        }

        public Builder httpsProxy(URL httpsProxy) {
            this.httpsProxy = httpsProxy;
            return this;
        }

        public Builder ftpProxy(URL ftpProxy) {
            this.ftpProxy = ftpProxy;
            return this;
        }

        public Builder printProgressBars(boolean printProgressBars) {
            this.printProgressBars = printProgressBars;
            return this;
        }

        /**
         * Optional download directory - if none is specified a temporary directory will be used.
         * @param downloadDir
         * @return
         */
        public Builder downloadDir(Path downloadDir) {
            this.downloadDir = downloadDir;
            return this;
        }

        public JannovarDataFactory build() {

            if (downloadDir == null) {
                try {
                    downloadDir = Files.createTempDirectory("jannovar-data");
                } catch (IOException e) {
                    throw new JannovarException("Unable to create temp directory for data download", e);
                }
            }

            DatasourceOptions dsOptions = new DatasourceOptions(httpProxy, httpsProxy, ftpProxy, printProgressBars);
            try {
                dataSourceFactory = new DataSourceFactory(dsOptions, List.of(iniFile.toAbsolutePath().toString()));
            } catch (InvalidDataSourceException e) {
                throw new JannovarException(e);
            }

            return new JannovarDataFactory(this);
        }
    }
}
