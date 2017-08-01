package org.monarchinitiative.exomiser.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ConfigurationProperties("exomiser.genome")
public class GenomeProperties {

    public enum GenomeBuild {
        hg19, hg38
    }

    public enum TranscriptSource {
        ucsc, ensembl, refseq;
    }

    /**
     * version of the data release
     */
    public String dataVersion = "1707";
    /**
     * genome build version - hg19/hg38
     */
    public GenomeBuild build = GenomeBuild.hg19;
    /**
     * source of the transcripts - ucsc/ensembl/refseq
     */
    public TranscriptSource transcriptSource = TranscriptSource.ucsc;
//        public H2 exomiserGenomeDatabase = new H2();

    public GenomeProperties(String dataVersion, GenomeBuild build, TranscriptSource transcriptSource) {
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

    public GenomeBuild getBuild() {
        return build;
    }

    public void setBuild(GenomeBuild build) {
        this.build = build;
    }

    public void setBuild(String build) {
        this.build = GenomeBuild.valueOf(build);
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
