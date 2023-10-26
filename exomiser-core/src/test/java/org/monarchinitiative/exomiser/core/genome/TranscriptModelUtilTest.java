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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class TranscriptModelUtilTest {

    private static final GenomeInterval TX_REGION = new GenomeInterval(HG19RefDictBuilder.build(), Strand.FWD, 1, 1000, 1000);

    @Test
    void getTranscriptAccessionNullTranscriptModel() {
        assertThat(TranscriptModelUtil.getTranscriptAccession(null), equalTo(""));
    }

    @Test
    void getTranscriptAccession() {
        TranscriptModel transcriptModel = new TranscriptModel("ENST12345678", null, TX_REGION, TX_REGION,
                ImmutableList.of(TX_REGION), null, null, 0, false, false, Map.of(), Alignment.createUngappedAlignment(TX_REGION.length()));
        assertThat(TranscriptModelUtil.getTranscriptAccession(transcriptModel), equalTo("ENST12345678"));
    }

    @Test
    void getTranscriptGeneIdNullTranscriptModel() {
        assertThat(TranscriptModelUtil.getTranscriptGeneId(null), equalTo(""));
    }

    @Test
    void getTranscriptNullGeneIdNullTranscriptModelGeneId() {
        TranscriptModel transcriptModel = new TranscriptModel(null, null, TX_REGION, TX_REGION,
                ImmutableList.of(TX_REGION), null, null, 0, false, false, Map.of(), Alignment.createUngappedAlignment(TX_REGION.length()));
        assertThat(TranscriptModelUtil.getTranscriptGeneId(transcriptModel), equalTo(""));
    }

    @Test
    void getTranscriptGeneId() {
        TranscriptModel transcriptModel = new TranscriptModel(null, null, TX_REGION, TX_REGION,
                ImmutableList.of(TX_REGION), null, "GENE:12345", 0, false, false, Map.of(), Alignment.createUngappedAlignment(TX_REGION.length()));
        assertThat(TranscriptModelUtil.getTranscriptGeneId(transcriptModel), equalTo("GENE:12345"));
    }

    @Test
    void getTranscriptGeneSymbolNullTranscriptModel() {
        assertThat(TranscriptModelUtil.getTranscriptGeneSymbol(null), equalTo("."));
    }

    @Test
    void getTranscriptNullGeneSymbolNullTranscriptModelGeneSymbol() {
        TranscriptModel transcriptModel = new TranscriptModel(null, null, TX_REGION, TX_REGION,
                ImmutableList.of(TX_REGION), null, null, 0, false, false, Map.of(), Alignment.createUngappedAlignment(TX_REGION.length()));
        assertThat(TranscriptModelUtil.getTranscriptGeneSymbol(transcriptModel), equalTo("."));
    }

    @Test
    void getTranscriptGeneSymbol() {
        TranscriptModel transcriptModel = new TranscriptModel(null, "SYMBOL", TX_REGION, TX_REGION,
                ImmutableList.of(TX_REGION), null, null, 0, false, false, Map.of(), Alignment.createUngappedAlignment(TX_REGION.length()));
        assertThat(TranscriptModelUtil.getTranscriptGeneSymbol(transcriptModel), equalTo("SYMBOL"));
    }
}