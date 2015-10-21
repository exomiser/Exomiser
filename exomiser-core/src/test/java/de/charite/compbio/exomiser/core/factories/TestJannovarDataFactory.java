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
 */
public class TestJannovarDataFactory {

    private final ReferenceDictionary refDict;
    private final JannovarData jannovarData;
    
    public TestJannovarDataFactory() {
        this.refDict = HG19RefDictBuilder.build();
        this.jannovarData = buildJannovarData();
    }

    private JannovarData buildJannovarData() {
        TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
        TranscriptModel tmGNRHR2A = TestTranscriptModelFactory.buildTMForGNRHR2A();
        TranscriptModel tmRBM8A = TestTranscriptModelFactory.buildTMForRBM8A();
        TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();
        return new JannovarData(refDict, ImmutableList.of(tmFGFR2, tmGNRHR2A, tmRBM8A, tmSHH));
    }

    public ReferenceDictionary getRefDict() {
        return refDict;
    }

    public JannovarData getJannovarData() {
        return jannovarData;
    }

}
