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

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestFactory {

    private static final TestJannovarDataFactory TEST_JANNOVAR_DATA_FACTORY = new TestJannovarDataFactory();
    private static final JannovarData DEFAULT_JANNOVAR_DATA = TEST_JANNOVAR_DATA_FACTORY.getJannovarData();

    private TestFactory() {
        //this class should be used in a static context.
    }

    public static JannovarData buildDefaultJannovarData() {
        return DEFAULT_JANNOVAR_DATA;
    }
    public static VariantFactory buildDefaultVariantFactory() {
        final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(DEFAULT_JANNOVAR_DATA.getRefDict(), DEFAULT_JANNOVAR_DATA.getChromosomes());
        return new VariantFactory(new VariantAnnotator(variantContextAnnotator));
    }

    public static SampleDataFactory buildDefaultSampleDataFactory() {
        return new SampleDataFactory(buildDefaultVariantFactory(), DEFAULT_JANNOVAR_DATA);
    }

    public static JannovarData buildJannovarData(TranscriptModel... transcriptModels) {
        return TEST_JANNOVAR_DATA_FACTORY.buildJannovarData(transcriptModels);
    }

    public static VariantFactory buildVariantFactory(TranscriptModel... transcriptModels) {
        final JannovarData jannovarData = buildJannovarData(transcriptModels);
        final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
        return new VariantFactory(new VariantAnnotator(variantContextAnnotator));
    }

    public static VariantFactory buildVariantFactory(JannovarData jannovarData) {
        final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
        return new VariantFactory(new VariantAnnotator(variantContextAnnotator));
    }

    public static SampleDataFactory buildSampleDataFactory(TranscriptModel... transcriptModels) {
        final JannovarData jannovarData = buildJannovarData(transcriptModels);
        return new SampleDataFactory(buildVariantFactory(jannovarData), jannovarData);
    }

    public static SampleDataFactory buildSampleDataFactory(JannovarData jannovarData) {
        return new SampleDataFactory(buildVariantFactory(jannovarData), jannovarData);
    }

}
