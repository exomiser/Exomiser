/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ConfigurationProperties("exomiser.genome")
public class GenomeProperties {

    /**
     * source of the transcripts - ucsc/ensembl/refseq
     */
    public enum TranscriptSource {
        ucsc, ensembl, refseq;
    }

    /**
     * version of the data release
     */
    private String dataVersion;

    private GenomeAssembly build;
    private TranscriptSource transcriptSource;
//        public H2 exomiserGenomeDatabase = new H2();

    /**
     * Defaults
     */
    public GenomeProperties() {
        this.dataVersion = "1707";
        this.build = GenomeAssembly.HG19;
        this.transcriptSource = TranscriptSource.ucsc;
    }

    public GenomeProperties(String dataVersion, GenomeAssembly build, TranscriptSource transcriptSource) {
        this.dataVersion = dataVersion;
        this.build = build;
        this.transcriptSource = transcriptSource;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public GenomeAssembly getBuild() {
        return build;
    }

    public void setBuild(GenomeAssembly build) {
        this.build = build;
    }

    public void setBuild(String build) {
        this.build = GenomeAssembly.fromValue(build);
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

}
