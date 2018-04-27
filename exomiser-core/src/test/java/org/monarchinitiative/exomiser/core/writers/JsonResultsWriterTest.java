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

package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Genotype;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JsonResultsWriterTest {

    private final TestVariantFactory varFactory = new TestVariantFactory();

    private final OutputSettingsImp.OutputSettingsBuilder settingsBuilder = OutputSettings.builder()
            .outputFormats(EnumSet.of(OutputFormat.JSON));
    private final Analysis analysis = Analysis.builder().build();
    private AnalysisResults.Builder analysisResults;

    @Before
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.addVariant(makePassVariant());

        Gene shh = TestFactory.newGeneSHH();
        shh.addVariant(makeFailVariant());

        analysisResults = AnalysisResults.builder()
                .genes(Arrays.asList(fgfr2, shh));
    }

    private VariantEvaluation makePassVariant() {
        VariantEvaluation variant = varFactory.buildVariant(10, 123256215, "T", "G", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.valueOf(1f)));
        return variant;
    }

    private VariantEvaluation makeFailVariant() {
        VariantEvaluation variant = varFactory.buildVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        variant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        return variant;
    }

    @Test
    public void writeToString() {
        JsonResultsWriter instance = new JsonResultsWriter();
        List<Gene> genes = ImmutableList.of();
        OutputSettings outputSettings = this.settingsBuilder.build();
        AnalysisResults analysisResults = this.analysisResults.build();
        String result = instance.writeString(ModeOfInheritance.AUTOSOMAL_DOMINANT, analysis, analysisResults, outputSettings);
        System.out.println(result);
    }
}