/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.proto.JannovarProto;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarProtoConverterTest {

    @Test
    public void thereAndBackAgain() {
        JannovarData original = TestFactory.buildDefaultJannovarData();

        JannovarProto.JannovarData serialised = JannovarProtoConverter.toJannovarProto(original);
        JannovarData roundTripped = JannovarProtoConverter.toJannovarData(serialised);

        assertThat(roundTripped.getRefDict().getContigNameToID(), equalTo(original.getRefDict().getContigNameToID()));
        assertThat(roundTripped.getRefDict().getContigIDToLength(), equalTo(original.getRefDict()
                .getContigIDToLength()));
        assertThat(roundTripped.getRefDict().getContigIDToName(), equalTo(original.getRefDict().getContigIDToName()));

        assertThat(roundTripped.getTmByAccession(), equalTo(original.getTmByAccession()));
        assertThat(roundTripped.getTmByGeneSymbol(), equalTo(original.getTmByGeneSymbol()));
        // can't easily test getChromosomes() method as the underlying classes don't override equals() or hashCode()
        // given they are derived from the TranscriptModels and these have been asserted as equal we'll assume that we've
        // tested enough
    }

    @Test
    void testTrimDuplicatedVersionFromEnst() {
        assertThat(JannovarProtoConverter.trimDuplicatedEnsemblVersion("ENST00000523072.11.11"), equalTo("ENST00000523072.11"));
    }

    @Test
    void canLoadPreJannovar041Data() {
        // jannovar exomiser format < 0.33
        JannovarData v30Data = JannovarDataSourceLoader.loadJannovarData(Path.of("src/test/resources/jannovar/1710_hg19_transcripts_ensembl_exomiser_format.ser"));
        // jannovar exomiser format >= 0.33
        JannovarData v41Data = JannovarDataSourceLoader.loadJannovarData(Path.of("src/test/resources/jannovar/2309_hg19_transcripts_ensembl_exomiser_format.ser"));
        // jannovarData does not implement equals, so we need to test the internals manually
        assertThat(v30Data.getRefDict().getContigNameToID(), equalTo(v41Data.getRefDict().getContigNameToID()));
        assertThat(v30Data.getRefDict().getContigIDToLength(), equalTo(v41Data.getRefDict()
                .getContigIDToLength()));
        assertThat(v30Data.getRefDict().getContigIDToName(), equalTo(v41Data.getRefDict().getContigIDToName()));
        assertThat(v30Data.getTmByGeneSymbol(), equalTo(v41Data.getTmByGeneSymbol()));
        assertThat(v30Data.getTmByAccession(), equalTo(v41Data.getTmByAccession()));
    }
}