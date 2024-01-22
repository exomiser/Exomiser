/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisMode;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Disabled("Broken due to new SV fields - fix and re-enable for final release")
public class JsonResultsWriterTest {

    private final TestVariantFactory varFactory = new TestVariantFactory();

    private final OutputSettings.Builder settingsBuilder = OutputSettings.builder()
            .outputFormats(EnumSet.of(OutputFormat.JSON));
    private final Analysis.Builder analysisBuilder = Analysis.builder();
    private AnalysisResults.Builder analysisResultsBuilder;

    @BeforeEach
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        VariantEvaluation contributingDominantAndRecessiveVariant = makeContributingDominantAndRecessiveVariant();
        contributingDominantAndRecessiveVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        contributingDominantAndRecessiveVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        contributingDominantAndRecessiveVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        fgfr2.addVariant(contributingDominantAndRecessiveVariant);
        fgfr2.addGeneScore(GeneScore.builder()
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .variantScore(1.0f)
                .phenotypeScore(1.0f)
                .combinedScore(1.0f)
                .pValue(0.001)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .contributingVariants(List.of(contributingDominantAndRecessiveVariant)).build()
        );

        VariantEvaluation contributingRecessiveCompHetVariant = makeContributingCompHetRecessiveVariant();
        contributingRecessiveCompHetVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        contributingRecessiveCompHetVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        fgfr2.addVariant(contributingRecessiveCompHetVariant);
        fgfr2.addGeneScore(GeneScore.builder()
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .variantScore(0.945f)
                .phenotypeScore(1.0f)
                .combinedScore(0.945f)
                .pValue(0.0005)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .contributingVariants(List.of(contributingDominantAndRecessiveVariant, contributingRecessiveCompHetVariant)).build()
        );
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passVariant = makePassVariant();
        fgfr2.addVariant(passVariant);

        Gene shh = TestFactory.newGeneSHH();
        VariantEvaluation failVariant = makeFailVariant();
        shh.addVariant(failVariant);
        shh.addGeneScore(GeneScore.builder()
                .geneIdentifier(shh.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build()
        );

        analysisResultsBuilder = AnalysisResults.builder()
                .genes(Arrays.asList(fgfr2, shh));
    }

    private VariantEvaluation makeContributingCompHetRecessiveVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(10, 123256214, "A", "G", SampleGenotype.het(), 30, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(0.89f)));
        return variant;
    }

    private VariantEvaluation makeContributingDominantAndRecessiveVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(10, 123256215, "T", "G", SampleGenotype.het(), 30, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setWhiteListed(true);
        ClinVarData clinVarData = ClinVarData.builder()
                .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
                .build();
        variant.setPathogenicityData(PathogenicityData.of(clinVarData, PolyPhenScore.of(1f)));
        return variant;
    }

    private VariantEvaluation makePassVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(10, 123256204, "A", "G", SampleGenotype.het(), 30, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(0.6f)));
        return variant;
    }

    private VariantEvaluation makeFailVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(7, 155604800, "C", "CTT", SampleGenotype.het(), 30, 1.0);
        variant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        return variant;
    }

    private String readFromFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Files.lines(Paths.get(filePath)).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @Test
    public void writeToStringPassOnlyAutosomalDominant() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.PASS_ONLY).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();
        OutputSettings outputSettings = this.settingsBuilder.outputContributingVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.AUTOSOMAL_DOMINANT, analysisResults, outputSettings);
        System.out.println(result);
        String expected = readFromFile("src/test/resources/writers/contributing_only_autosomal_dominant_moi_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringPassOnlyAnyModeOfInheritance() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.PASS_ONLY)
                .build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();
        OutputSettings outputSettings = this.settingsBuilder.outputContributingVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/contributing_only_any_moi_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringFullAnalysisAnyModeOfInheritanceAllVariants() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();
        OutputSettings outputSettings = this.settingsBuilder.outputContributingVariantsOnly(false).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/full_any_moi_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringFullAnalysisNoModeOfInheritanceMatchAllVariants() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();
        OutputSettings outputSettings = this.settingsBuilder.outputContributingVariantsOnly(false).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        //we have no MITOCHONDRIAL matches in the results set
        String result = instance.writeString(ModeOfInheritance.MITOCHONDRIAL, analysisResults, outputSettings);
        String expected = "[]";
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringFullAnalysisAnyModeOfInheritancePassOnlyVariants() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();
        OutputSettings outputSettings = this.settingsBuilder.outputContributingVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/contributing_only_any_moi_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToFileOutputFullAnyModeOfInheritancePassOnlyVariants() throws IOException {
        Sample sample = Sample.builder().build();
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder
                .sample(sample)
                .analysis(analysis)
                .build();

        Path outPath = Files.createTempFile("exomiser_test", "");
        OutputSettings outputSettings = settingsBuilder.outputPrefix(outPath + "testWrite").build();

        JsonResultsWriter instance = new JsonResultsWriter();
        instance.writeFile(ModeOfInheritance.ANY, analysisResults, outputSettings);
        Path anyOutputPath = Paths.get(outPath + "testWrite.json");
        assertThat(anyOutputPath.toFile().exists(), is(true));
        assertThat(anyOutputPath.toFile().delete(), is(true));

        instance.writeFile(ModeOfInheritance.AUTOSOMAL_RECESSIVE, analysisResults, outputSettings);
        Path arOutputPath = Paths.get(outPath + "testWrite_AR.json");
        assertThat(arOutputPath.toFile().exists(), is(true));
        assertThat(arOutputPath.toFile().delete(), is(true));

        instance.writeFile(ModeOfInheritance.AUTOSOMAL_DOMINANT, analysisResults, outputSettings);
        Path adOutputPath = Paths.get(outPath + "testWrite_AD.json");
        assertThat(adOutputPath.toFile().exists(), is(true));
        assertThat(adOutputPath.toFile().delete(), is(true));
        Files.delete(outPath);
    }
}