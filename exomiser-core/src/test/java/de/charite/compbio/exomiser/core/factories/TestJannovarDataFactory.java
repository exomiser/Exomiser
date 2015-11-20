/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.factories;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Allows the easy creation of {@link JannovarData} objects for testing.
 * 
 * The generated data contains one transcript each for the genes FGFR2, GNRHR2A, RBM8A (overlaps with GNRHR2A), and SHH.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 * @author Jules Jacobsen <julesjacobsen@sanger.ac.uk>
 */
class TestJannovarDataFactory {

    private static final ReferenceDictionary REF_DICT = HG19RefDictBuilder.build();
    private static final TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
    private static final TranscriptModel tmGNRHR2A = TestTranscriptModelFactory.buildTMForGNRHR2A();
    private static final TranscriptModel tmRBM8A = TestTranscriptModelFactory.buildTMForRBM8A();
    private static final TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();
    
    public ReferenceDictionary getRefDict() {
        return REF_DICT;
    }

    public JannovarData getJannovarData() {
        return buildJannovarData();
    }

    private JannovarData buildJannovarData() {
        return new JannovarData(REF_DICT, ImmutableList.of(tmFGFR2, tmGNRHR2A, tmRBM8A, tmSHH));
    }

    public JannovarData buildJannovarData(TranscriptModel... transcriptModels) {
        return new JannovarData(REF_DICT, ImmutableList.copyOf(transcriptModels));
    }

    public JannovarData buildJannovarData(ReferenceDictionary refDict, TranscriptModel... transcriptModels) {
        return new JannovarData(refDict, ImmutableList.copyOf(transcriptModels));
    }
}
