/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.pedigree.Genotype;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.OMIMPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PhivePriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.TemplateEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ThymeleafConfig.class)
public class HtmlResultsWriterTest {

    private HtmlResultsWriter instance;

    /**
     * The temporary folder to write files to, automatically removed after tests
     * finish.
     */
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Autowired
    private TemplateEngine templateEngine;

    private String testOutFilePrefix;

    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene fgfr2Gene;
    private Gene shhGene;

    @Before
    public void setUp() {
        instance = new HtmlResultsWriter(templateEngine);
        
        TestVariantFactory varFactory = new TestVariantFactory();

        VariantEvaluation fgfr2MissenseVariantEvaluation = varFactory.buildVariant(10, 123256215, "T", "G", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        fgfr2MissenseVariantEvaluation.setFrequencyData(new FrequencyData(RsId.valueOf(123456), Frequency.valueOf(0.01f, FrequencySource.THOUSAND_GENOMES)));
        fgfr2MissenseVariantEvaluation.setPathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1f), MutationTasterScore.valueOf(1f), SiftScore.valueOf(0f), CaddScore.valueOf(1f)));
        fgfr2MissenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        fgfr2MissenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));

        //TODO: make a few canned variants from the smallTest.vcf and Pfeiffer.vcf these can then be used to run the full system without the vcf if required.
        fgfr2Gene = TestFactory.newGeneFGFR2();
        fgfr2Gene.addVariant(fgfr2MissenseVariantEvaluation);

        VariantEvaluation shhIndelVariantEvaluation = varFactory.buildVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        shhGene = TestFactory.newGeneSHH();
        shhGene.addVariant(shhIndelVariantEvaluation);

        fgfr2Gene.addPriorityResult(new OMIMPriorityResult(fgfr2Gene.getEntrezGeneID(), fgfr2Gene.getGeneSymbol(), 1f, Collections.emptyList()));
        shhGene.addPriorityResult(new OMIMPriorityResult(shhGene.getEntrezGeneID(), shhGene.getGeneSymbol(), 1f, Collections.emptyList()));

        unAnnotatedVariantEvaluation1 = varFactory.buildVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        unAnnotatedVariantEvaluation2 = varFactory.buildVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
    }

    @After
    public void tearDown() {
        Paths.get(testOutFilePrefix).toFile().delete();
    }

    private AnalysisResults buildAnalysisResults(List<Gene> genes, List<VariantEvaluation> variantEvaluations) {
        return AnalysisResults.builder()
                .sampleNames(Lists.newArrayList("Slartibartfast"))
                .genes(genes)
                .variantEvaluations(variantEvaluations)
                .build();
    }

    @Test
    public void testWriteTemplateWithEmptyData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWrite.html").toString();

        Analysis analysis = Analysis.builder().build();
        AnalysisResults analysisResults = buildAnalysisResults(Collections.emptyList(), Collections.emptyList());
       
        OutputSettings settings = OutputSettings.builder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, analysisResults, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());

    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantData.html").toString();
        Analysis analysis = Analysis.builder().build();

        List<VariantEvaluation> variants = Lists.newArrayList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        AnalysisResults analysisResults = buildAnalysisResults(Collections.emptyList(), variants);

        OutputSettings settings = OutputSettings.builder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, analysisResults, settings);

        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantDataAndGenes.html").toString();
        Analysis analysis = Analysis.builder().build();

        List<VariantEvaluation> variants = Lists.newArrayList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        List<Gene> genes = Lists.newArrayList(fgfr2Gene, shhGene);
        AnalysisResults analysisResults = buildAnalysisResults(genes, variants);

        OutputSettings settings = OutputSettings.builder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, analysisResults, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        Files.readAllLines(testOutFile).forEach(System.out::println);

        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithEmptyDataAndFullAnalysis() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWrite").toString();

        AnalysisResults analysisResults = buildAnalysisResults(Collections.emptyList(), Collections.emptyList());

        Analysis analysis = Analysis.builder()
                .hpoIds(Lists.newArrayList("HP:000001", "HP:000002"))
                .addStep(new RegulatoryFeatureFilter())
                .addStep(new FrequencyFilter(0.1f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new PhivePriority(Collections.emptyList(), null))
                .build();

        OutputSettings settings = OutputSettings.builder().outputPrefix(testOutFilePrefix).build();

        String output = instance.writeString(analysis, analysisResults, settings);

        Path testOutFile = Paths.get(testOutFilePrefix);
        assertFalse(output.isEmpty());
        assertTrue(testOutFile.toFile().exists());
    }

}
