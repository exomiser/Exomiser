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

import org.monarchinitiative.exomiser.autoconfigure.DataSourceProperties;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractGenomeProperties implements GenomeProperties {

    private GenomeAssembly assembly;
    private TranscriptSource transcriptSource = TranscriptSource.ensembl;
    private String dataVersion = "";

    private Path dataDirectory;

    //Optional tabix variant data
    private String caddSnvPath = "";
    private String caddInDelPath = "";
    private String remmPath = "";
    private String localFrequencyPath = "";

    // 'special' tabix datasource for quickly testing new pathogenicity data sources before plumbing them into the main
    // datastore
    private String testPathogenicityScorePath = "";

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = Paths.get(dataDirectory);
    }

    @NestedConfigurationProperty
    private DataSourceProperties datasource = new DataSourceProperties();

    public AbstractGenomeProperties(GenomeAssembly assembly) {
        this.assembly = assembly;
    }

    public GenomeAssembly getAssembly() {
        return assembly;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public TranscriptSource getTranscriptSource() {
        return transcriptSource;
    }

    public void setTranscriptSource(TranscriptSource transcriptSource) {
        this.transcriptSource = transcriptSource;
    }

    public void setTranscriptSource(String name) {
        this.transcriptSource = TranscriptSource.valueOf(name);
    }

    public DataSourceProperties getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSourceProperties dataSourceProperties) {
        this.datasource = dataSourceProperties;
    }

    public String getCaddSnvPath() {
        return caddSnvPath;
    }

    public void setCaddSnvPath(String caddSnvPath) {
        this.caddSnvPath = caddSnvPath;
    }

    public String getCaddInDelPath() {
        return caddInDelPath;
    }

    public void setCaddInDelPath(String caddInDelPath) {
        this.caddInDelPath = caddInDelPath;
    }

    public String getRemmPath() {
        return remmPath;
    }

    public void setRemmPath(String remmPath) {
        this.remmPath = remmPath;
    }

    public String getLocalFrequencyPath() {
        return localFrequencyPath;
    }

    public void setLocalFrequencyPath(String localFrequencyPath) {
        this.localFrequencyPath = localFrequencyPath;
    }

    public String getTestPathogenicityScorePath() {
        return testPathogenicityScorePath;
    }

    public void setTestPathogenicityScorePath(String testPathogenicityScorePath) {
        this.testPathogenicityScorePath = testPathogenicityScorePath;
    }
}
