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

package org.monarchinitiative.exomiser.core.factories;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Allows the easy creation of {@link JannovarData} {@link VariantFactory} and {@link SampleDataFactory} objects for testing.
 *
 * The default data contains one transcript each for the genes FGFR2, GNRHR2A, RBM8A (overlaps with GNRHR2A), and SHH based on the HG19/GRCh37 build.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestFactory {

    private static final ReferenceDictionary DEFAULT_REF_DICT = HG19RefDictBuilder.build();

    private static final TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
    private static final TranscriptModel tmGNRHR2A = TestTranscriptModelFactory.buildTMForGNRHR2A();
    private static final TranscriptModel tmRBM8A = TestTranscriptModelFactory.buildTMForRBM8A();
    private static final TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();

    private static final JannovarData DEFAULT_JANNOVAR_DATA = new JannovarData(DEFAULT_REF_DICT, ImmutableList.of(tmFGFR2, tmGNRHR2A, tmRBM8A, tmSHH));
    private static final VariantFactory DEFAULT_VARIANT_FACTORY = new VariantFactory(DEFAULT_JANNOVAR_DATA);
    private static final SampleDataFactory DEFAULT_SAMPLE_DATA_FACTORY = new SampleDataFactory(buildDefaultVariantFactory(), DEFAULT_JANNOVAR_DATA);

    private TestFactory() {
        //this class should be used in a static context.
    }

    public static ReferenceDictionary getDefaultRefDict() {
        return DEFAULT_REF_DICT;
    }

    public static JannovarData buildDefaultJannovarData() {
        return DEFAULT_JANNOVAR_DATA;
    }
    public static VariantFactory buildDefaultVariantFactory() {
        return DEFAULT_VARIANT_FACTORY;
    }

    public static SampleDataFactory buildDefaultSampleDataFactory() {
        return DEFAULT_SAMPLE_DATA_FACTORY;
    }

    public static JannovarData buildJannovarData(TranscriptModel... transcriptModels) {
        return new JannovarData(DEFAULT_REF_DICT, ImmutableList.copyOf(transcriptModels));
    }

    public static VariantFactory buildVariantFactory(TranscriptModel... transcriptModels) {
        final JannovarData jannovarData = buildJannovarData(transcriptModels);
        return new VariantFactory(jannovarData);
    }

    public static VariantFactory buildVariantFactory(JannovarData jannovarData) {
        return new VariantFactory(jannovarData);
    }

    public static SampleDataFactory buildSampleDataFactory(TranscriptModel... transcriptModels) {
        final JannovarData jannovarData = buildJannovarData(transcriptModels);
        return new SampleDataFactory(buildVariantFactory(jannovarData), jannovarData);
    }

    public static SampleDataFactory buildSampleDataFactory(JannovarData jannovarData) {
        return new SampleDataFactory(buildVariantFactory(jannovarData), jannovarData);
    }

}
