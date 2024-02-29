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

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractGenomeProperties implements GenomeProperties {

    private final GenomeAssembly assembly;
    private TranscriptSource transcriptSource = TranscriptSource.ENSEMBL;
    private String dataVersion = "";

    private Path dataDirectory;

    private String clinVarDataVersion = "";
    private boolean useClinVarWhiteList = true;

    // Optional tabix data file containing whitelisted variants
    // This overrides the variant effect, frequency and pathogenicity filters
    private String variantWhiteListPath = "";

    // Optional tabix variant data
    private String caddSnvPath = "";
    private String caddInDelPath = "";
    private String remmPath = "";
    private String localFrequencyPath = "";

    // 'special' tabix datasource for quickly testing new pathogenicity data sources before plumbing them into the main
    // datastore
    private String testPathogenicityScorePath = "";

    protected AbstractGenomeProperties(GenomeAssembly assembly) {
        this.assembly = assembly;
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = Paths.get(dataDirectory);
    }

    public GenomeAssembly getAssembly() {
        return assembly;
    }

    @Override
    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    @Override
    public String getClinVarDataVersion() {
        return clinVarDataVersion;
    }

    public void setClinVarDataVersion(String clinVarDataVersion) {
        this.clinVarDataVersion = clinVarDataVersion;
    }

    @Override
    public boolean useClinVarWhiteList() {
        return useClinVarWhiteList;
    }

    public void setUseClinVarWhiteList(boolean useClinVarWhiteList) {
        this.useClinVarWhiteList = useClinVarWhiteList;
    }

    public TranscriptSource getTranscriptSource() {
        return transcriptSource;
    }

    public void setTranscriptSource(TranscriptSource transcriptSource) {
        this.transcriptSource = transcriptSource;
    }

    public void setTranscriptSource(String name) {
        this.transcriptSource = TranscriptSource.parseValue(name);
    }

    public String getVariantWhiteListPath() {
        return variantWhiteListPath;
    }

    public void setVariantWhiteListPath(String variantWhiteListPath) {
        this.variantWhiteListPath = variantWhiteListPath;
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
