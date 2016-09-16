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
package de.charite.compbio.exomiser.core.writers;

import com.google.common.collect.Lists;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.factories.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.pathogenicity.*;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriority;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;
import de.charite.compbio.jannovar.pedigree.Genotype;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.TemplateEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private VariantEvaluation missenseVariantEvaluation;
    private VariantEvaluation indelVariantEvaluation;

    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene gene1;
    private Gene gene2;

    @Before
    public void setUp() {
        instance = new HtmlResultsWriter(templateEngine);
        
        TestVariantFactory varFactory = new TestVariantFactory();

        missenseVariantEvaluation = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        missenseVariantEvaluation.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(0.01f, FrequencySource.THOUSAND_GENOMES)));
        missenseVariantEvaluation.setPathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1f), MutationTasterScore.valueOf(1f), SiftScore.valueOf(0f), CaddScore.valueOf(1f)));
        missenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.FREQUENCY_FILTER));
        missenseVariantEvaluation.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));

        indelVariantEvaluation = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        gene1 = new Gene(missenseVariantEvaluation.getGeneSymbol(), missenseVariantEvaluation.getEntrezGeneId());
        gene1.addVariant(missenseVariantEvaluation);

        gene2 = new Gene(indelVariantEvaluation.getGeneSymbol(), indelVariantEvaluation.getEntrezGeneId());
        gene2.addVariant(indelVariantEvaluation);

        gene1.addPriorityResult(new PhivePriorityResult(gene1.getEntrezGeneID(), gene1.getGeneSymbol(), 0.99f, "MGI:12345", "Gene1"));
        gene2.addPriorityResult(new PhivePriorityResult(gene2.getEntrezGeneID(), gene2.getGeneSymbol(), 0.98f, "MGI:54321", "Gene2"));

        gene1.addPriorityResult(new OMIMPriorityResult(gene1.getEntrezGeneID(), gene1.getGeneSymbol(), 1f, new ArrayList()));
        gene2.addPriorityResult(new OMIMPriorityResult(gene2.getEntrezGeneID(), gene2.getGeneSymbol(), 1f, Collections.emptyList()));

        unAnnotatedVariantEvaluation1 = varFactory.constructVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        unAnnotatedVariantEvaluation2 = varFactory.constructVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
    }

    @After
    public void tearDown() {
        Paths.get(testOutFilePrefix).toFile().delete();
    }

    private SampleData makeSampleData(List<Gene> genes, List<VariantEvaluation> variantEvaluations) {
        SampleData sampleData = new SampleData();
        sampleData.setSampleNames(Lists.newArrayList("Slartibartfast"));
        sampleData.setNumberOfSamples(1);
        sampleData.setGenes(genes);
        sampleData.setVariantEvaluations(variantEvaluations);

        return sampleData;
    }

    @Test
    public void testWriteTemplateWithEmptyData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWrite.html").toString();

        Analysis analysis = Analysis.newBuilder().build();
        SampleData sampleData = makeSampleData(Collections.emptyList(), Collections.emptyList());
       
        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, sampleData, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());

    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantData.html").toString();
        Analysis analysis = Analysis.newBuilder().build();

        List<VariantEvaluation> variants = Lists.newArrayList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        SampleData sampleData = makeSampleData(Collections.emptyList(), variants);

        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, sampleData, settings);

        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantDataAndGenes.html").toString();
        Analysis analysis = Analysis.newBuilder().build();

        List<VariantEvaluation> variants = Lists.newArrayList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2);
        List<Gene> genes = Lists.newArrayList(gene1, gene2);
        SampleData sampleData = makeSampleData(genes, variants);

        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, sampleData, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        Files.readAllLines(testOutFile).forEach(System.out::println);

        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithEmptyDataAndFullAnalysis() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWrite").toString();

        SampleData sampleData = makeSampleData(Collections.emptyList(), Collections.emptyList());

        Analysis analysis = Analysis.newBuilder()
                .hpoIds(Lists.newArrayList("HP:000001", "HP:000002"))
                .addStep(new RegulatoryFeatureFilter())
                .addStep(new FrequencyFilter(0.1f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new PhivePriority(Collections.emptyList(), null))
                .build();

        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        String output = instance.writeString(analysis, sampleData, settings);

        Path testOutFile = Paths.get(testOutFilePrefix);
        assertFalse(output.isEmpty());
        assertTrue(testOutFile.toFile().exists());
    }

}
