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

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDataSourcesTest {

    @Test
    public void fromGenomeProperties() {
        GenomeProperties hg19GenomeProperties = new Hg19GenomeProperties();
        hg19GenomeProperties.setDataVersion("1710");
        hg19GenomeProperties.setTranscriptSource("refseq");
        hg19GenomeProperties.setLocalFrequencyPath("local_freq.tsv.gz");
        Path exomiserDataPath = Paths.get("src/test/resources/data");

        GenomeDataSources instance = GenomeDataSources.from(hg19GenomeProperties, exomiserDataPath);

        Path expectedParent = exomiserDataPath.resolve("1710_hg19").toAbsolutePath();
        assertThat(instance.getTranscriptFilePath(), equalTo(expectedParent.resolve("1710_hg19_transcripts_refseq.ser")));
        assertThat(instance.getMvStorePath(), equalTo(expectedParent.resolve("1710_hg19_variants.mv.db")));
        assertThat(instance.getGenomeDataSource(), instanceOf(HikariDataSource.class));

        assertThat(instance.getCaddIndelPath(), equalTo(Optional.empty()));
        assertThat(instance.getCaddSnvPath(), equalTo(Optional.empty()));
        assertThat(instance.getRemmPath(), equalTo(Optional.empty()));
        assertThat(instance.getLocalFrequencyPath(), equalTo(Optional.of(expectedParent.resolve("local_freq.tsv.gz"))));
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNoArgs() {
        GenomeDataSources.builder().build();
        GenomeDataSources.builder().mvStorePath(Paths.get("mvStore.mv")).build();
        GenomeDataSources.builder().transcriptFilePath(Paths.get("transcripts.ser")).build();
        GenomeDataSources.builder().genomeDataSource(new HikariDataSource()).build();
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNullMvStorePath() {
        GenomeDataSources.builder()
                .mvStorePath(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNullTranscriptFilePath() {
        GenomeDataSources.builder()
                .transcriptFilePath(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNullGenomeDataSourcePath() {
        GenomeDataSources.builder()
                .genomeDataSource(null)
                .build();
    }

    @Test
    public void minimalExpectedDataSources() {
        Path mvStorePath = Paths.get("mvStore.mv");
        Path transcriptFilePath = Paths.get("transcripts.ser");
        HikariDataSource genomeDataSource = new HikariDataSource();

        GenomeDataSources instance = GenomeDataSources.builder()
                .mvStorePath(mvStorePath)
                .transcriptFilePath(transcriptFilePath)
                .genomeDataSource(genomeDataSource)
                .build();

        assertThat(instance.getMvStorePath(), equalTo(mvStorePath));
        assertThat(instance.getTranscriptFilePath(), equalTo(transcriptFilePath));
        assertThat(instance.getGenomeDataSource(), equalTo(genomeDataSource));
        //optional resources
        assertThat(instance.getCaddSnvPath(), equalTo(Optional.empty()));
        assertThat(instance.getCaddIndelPath(), equalTo(Optional.empty()));
        assertThat(instance.getRemmPath(), equalTo(Optional.empty()));
        assertThat(instance.getLocalFrequencyPath(), equalTo(Optional.empty()));
    }
}