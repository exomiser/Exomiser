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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PhivePriority;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriterTest {

    private final HtmlResultsWriter instance = new HtmlResultsWriter();

    private static Path testOutDir;

    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene fgfr2Gene;
    private Gene shhGene;

    @BeforeAll
    public static void makeTempDir() throws IOException {
        testOutDir = Files.createTempDirectory("exomiser_html_writer_test");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Files.delete(testOutDir);
    }

    @BeforeEach
    public void setUp(){
        TestVariantFactory varFactory = new TestVariantFactory();

        VariantEvaluation fgfr2MissenseVariantEvaluation = TestVariantFactory.buildVariant(10, 123256215, "T", "G", SampleGenotype.het(), 30, 2.2);
        fgfr2MissenseVariantEvaluation.setFrequencyData(FrequencyData.of("rs123456", Frequency.of(FrequencySource.THOUSAND_GENOMES, 0.01f)));
        fgfr2MissenseVariantEvaluation.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(1f), MutationTasterScore
                .of(1f), SiftScore.of(0f), CaddScore.of(1f)));
        fgfr2MissenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        fgfr2MissenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));

        //TODO: make a few canned variants from the smallTest.vcf and Pfeiffer.vcf these can then be used to run the full system without the vcf if required.
        fgfr2Gene = TestFactory.newGeneFGFR2();
        fgfr2Gene.addVariant(fgfr2MissenseVariantEvaluation);

        VariantEvaluation shhIndelVariantEvaluation = TestVariantFactory.buildVariant(7, 155604800, "C", "CTT", SampleGenotype.het(), 30, 1.0);
        shhGene = TestFactory.newGeneSHH();
        shhGene.addVariant(shhIndelVariantEvaluation);

        fgfr2Gene.addPriorityResult(new OmimPriorityResult(fgfr2Gene.getEntrezGeneID(), fgfr2Gene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));
        shhGene.addPriorityResult(new OmimPriorityResult(shhGene.getEntrezGeneID(), shhGene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));

        unAnnotatedVariantEvaluation1 = TestVariantFactory.buildVariant(5, 10, "C", "T", SampleGenotype.het(), 30, 1.0);
        unAnnotatedVariantEvaluation2 = TestVariantFactory.buildVariant(5, 10, "C", "T", SampleGenotype.het(), 30, 1.0);
    }

    private AnalysisResults buildAnalysisResults(Sample sample, Analysis analysis, List<Gene> genes, List<VariantEvaluation> variantEvaluations) {
        return AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .sampleNames(List.of("Slartibartfast"))
                .genes(genes)
                .variantEvaluations(variantEvaluations)
                .build();
    }

    @Test
    public void testWriteTemplateWithEmptyData() {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();
        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, Collections.emptyList(), Collections.emptyList());

        String outputPrefix = testOutDir.resolve("testWriteTemplateWithEmptyData").toString();
        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .build();

        instance.writeFile(analysisResults, settings);
        Path testOutFile = Paths.get(outputPrefix + ".html");
        assertTrue(testOutFile.toFile().exists());
        assertTrue(testOutFile.toFile().delete());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();

        List<VariantEvaluation> variants = List.of(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, Collections.emptyList(), variants);

        String testOutFilePrefix = testOutDir.resolve("testWriteTemplateWithUnAnnotatedVariantData").toString();
        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(testOutFilePrefix)
                .build();

        instance.writeFile(analysisResults, settings);

        Path testOutFile = Paths.get(testOutFilePrefix + ".html");
        assertTrue(testOutFile.toFile().exists());
        assertTrue(testOutFile.toFile().delete());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        Sample sample = Sample.builder().build();
        Analysis analysis = Analysis.builder().build();

        List<VariantEvaluation> variants = List.of(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        List<Gene> genes = List.of(fgfr2Gene, shhGene);
        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, genes, variants);

        String testOutFilePrefix = testOutDir.resolve("testWriteTemplateWithUnAnnotatedVariantDataAndGenes").toString();
        OutputSettings settings = OutputSettings.builder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysisResults, settings);
        Path testOutFile = Paths.get(testOutFilePrefix + ".html");

        List<String> lines = Files.readAllLines(testOutFile);
        assertFalse(lines.isEmpty());

        assertTrue(testOutFile.toFile().exists());
        assertTrue(testOutFile.toFile().delete());
    }

    @Test
    public void testWriteTemplateWithEmptyDataAndFullAnalysis() throws Exception {

        Sample sample = Sample.builder()
                .hpoIds(List.of("HP:000001", "HP:000002"))
                .build();

        Analysis analysis = Analysis.builder()
                .addStep(new RegulatoryFeatureFilter())
                .addStep(new FrequencyFilter(0.1f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new PhivePriority(TestPriorityServiceFactory.stubPriorityService()))
                .build();

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, Collections.emptyList(), Collections.emptyList());
        OutputSettings settings = OutputSettings.builder().build();

        String output = instance.writeString(analysisResults, settings);
        assertFalse(output.isEmpty());
    }

    @Test
    public void testWriteTemplateWithFullDataAndFullAnalysis() throws Exception {

        unAnnotatedVariantEvaluation1.setContributesToGeneScoreUnderMode(ModeOfInheritance.ANY);

        List<VariantEvaluation> variants = List.of(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        List<Gene> genes = List.of(fgfr2Gene, shhGene);

        Sample sample = Sample.builder()
                .hpoIds(List.of("HP:000001", "HP:000002"))
                .build();

        Analysis analysis = Analysis.builder()
                .addStep(new RegulatoryFeatureFilter())
                .addStep(new FrequencyFilter(0.1f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new PhivePriority(TestPriorityServiceFactory.stubPriorityService()))
                .build();
        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, genes, variants);

        OutputSettings settings = OutputSettings.builder().build();

        String output = instance.writeString(analysisResults, settings);
        assertTrue(output.contains("Exomiser Analysis Results for"));
        assertTrue(output.contains("FGFR2"));
        assertTrue(output.contains("SHH"));
    }

}
