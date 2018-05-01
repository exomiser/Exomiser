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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisMode;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JsonResultsWriterTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private final TestVariantFactory varFactory = new TestVariantFactory();

    private final OutputSettingsImp.OutputSettingsBuilder settingsBuilder = OutputSettings.builder()
            .outputFormats(EnumSet.of(OutputFormat.JSON));
    private final Analysis.Builder analysisBuilder = Analysis.builder();
    private AnalysisResults.Builder analysisResultsBuilder;

    @Before
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        VariantEvaluation passVariant = makePassVariant();
        passVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addVariant(passVariant);
        fgfr2.addGeneScore(GeneScore.builder()
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .variantScore(1.0f)
                .phenotypeScore(1.0f)
                .combinedScore(1.0f)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .contributingVariants(ImmutableList.of(passVariant)).build()
        );
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

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

    private String readFromFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Files.lines(Paths.get(filePath)).forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    @Test
    public void writeToStringPassOnlyAutosomalDominant() throws Exception {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.PASS_ONLY).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();
        OutputSettings outputSettings = this.settingsBuilder.outputPassVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.AUTOSOMAL_DOMINANT, analysis, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/pass_only_autosomal_dominant_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringPassOnlyAnyModeOfInheritance() throws Exception {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.PASS_ONLY).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();
        OutputSettings outputSettings = this.settingsBuilder.outputPassVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysis, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/pass_only_autosomal_dominant_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringOutputFullAnyModeOfInheritanceAllVariants() throws Exception {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();
        OutputSettings outputSettings = this.settingsBuilder.outputPassVariantsOnly(false).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysis, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/full_any_moi_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringOutputFullNoModeOfInheritanceMatchAllVariants() throws Exception {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();
        OutputSettings outputSettings = this.settingsBuilder.outputPassVariantsOnly(false).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        //we have no AR matches in the results set
        String result = instance.writeString(ModeOfInheritance.AUTOSOMAL_RECESSIVE, analysis, analysisResults, outputSettings);
        String expected = "[]";
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToStringOutputFullAnyModeOfInheritancePassOnlyVariants() throws Exception {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();
        OutputSettings outputSettings = this.settingsBuilder.outputPassVariantsOnly(true).build();

        JsonResultsWriter instance = new JsonResultsWriter();
        String result = instance.writeString(ModeOfInheritance.ANY, analysis, analysisResults, outputSettings);
        String expected = readFromFile("src/test/resources/writers/pass_only_autosomal_dominant_test.json");
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void writeToFileOutputFullAnyModeOfInheritancePassOnlyVariants() throws IOException {
        Analysis analysis = this.analysisBuilder.analysisMode(AnalysisMode.FULL).build();
        AnalysisResults analysisResults = this.analysisResultsBuilder.build();

        Path outPath = tmpFolder.newFile().toPath();
        OutputSettings outputSettings = settingsBuilder.outputPrefix(outPath + "testWrite").build();

        JsonResultsWriter instance = new JsonResultsWriter();
        instance.writeFile(ModeOfInheritance.ANY, analysis, analysisResults, outputSettings);
        Path anyOutputPath = Paths.get(outPath + "testWrite.json");
        assertThat(anyOutputPath.toFile().exists(), is(true));
        assertThat(anyOutputPath.toFile().delete(), is(true));

        instance.writeFile(ModeOfInheritance.AUTOSOMAL_RECESSIVE, analysis, analysisResults, outputSettings);
        Path arOutputPath = Paths.get(outPath + "testWrite_AR.json");
        assertThat(arOutputPath.toFile().exists(), is(true));
        assertThat(arOutputPath.toFile().delete(), is(true));

        instance.writeFile(ModeOfInheritance.AUTOSOMAL_DOMINANT, analysis, analysisResults, outputSettings);
        Path adOutputPath = Paths.get(outPath + "testWrite_AD.json");
        assertThat(adOutputPath.toFile().exists(), is(true));
        assertThat(adOutputPath.toFile().delete(), is(true));    }
}