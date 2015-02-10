/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.dao.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.TargetFilterResult;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.prioritisers.ExomiserMousePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.jannovar.pedigree.Genotype;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriterTest {

    HtmlResultsWriter instance;

    private final String testOutFileName = "testWrite.html";

    private VariantEvaluation missenseVariantEvaluation;
    private VariantEvaluation indelVariantEvaluation;

    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene gene1;
    private Gene gene2;

    @Before
    public void setUp() {
        instance = new HtmlResultsWriter();

        TestVariantFactory varFactory = new TestVariantFactory();
        Variant missenseVariant = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0,
                2.2);
        Variant indelVariant = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        missenseVariantEvaluation = new VariantEvaluation(missenseVariant);
        indelVariantEvaluation = new VariantEvaluation(indelVariant);

        gene1 = new Gene(missenseVariantEvaluation);
        gene2 = new Gene(indelVariantEvaluation);

        gene1.addPriorityResult(new ExomiserMousePriorityResult("MGI:12345", "Gene1", 0.99f));
        gene2.addPriorityResult(new ExomiserMousePriorityResult("MGI:54321", "Gene2", 0.98f));

        OMIMPriorityResult gene1PriorityScore = new OMIMPriorityResult();
        gene1PriorityScore.addRow("OMIM:12345", "OMIM:67890", "Disease syndrome", 'D', 'D', 1f);
        gene1.addPriorityResult(gene1PriorityScore);
        gene2.addPriorityResult(new OMIMPriorityResult());

        Variant unannotatedVariant = varFactory.constructVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        unAnnotatedVariantEvaluation1 = new VariantEvaluation(unannotatedVariant);
        unAnnotatedVariantEvaluation2 = new VariantEvaluation(unannotatedVariant);
    }

    @After
    public void tearDown() {
        // Paths.get(testOutFileName).toFile().delete();
    }

    private SampleData makeSampleData(List<Gene> genes, List<VariantEvaluation> variantEvaluations) {
        SampleData sampleData = new SampleData();
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add("Slartibartfast");
        sampleData.setSampleNames(sampleNames);
        sampleData.setNumberOfSamples(1);
        sampleData.setGenes(genes);
        sampleData.setVariantEvaluations(variantEvaluations);
        return sampleData;
    }

    @Test
    public void testWriteTemplateWithEmptyData() {
        SampleData sampleData = makeSampleData(new ArrayList<Gene>(), new ArrayList<VariantEvaluation>());
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName(testOutFileName).build();

        instance.writeFile(sampleData, settings);

        assertTrue(Paths.get(testOutFileName).toFile().exists());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() throws Exception {
        String testOutFilename = "testWriteTemplateWithUnAnnotatedVariantData.html";
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);
        SampleData sampleData = makeSampleData(new ArrayList<Gene>(), variantData);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName(testOutFilename).build();

        instance.writeFile(sampleData, settings);

        File input = new File(testOutFilename);
        Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
        System.out.println(doc.toString());
        // assertTrue(doc.contains("Unanalysed Variants"));
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        String testOutFilename = "testWriteTemplateWithUnAnnotatedVariantDataAndGenes.html";
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);

        List<Gene> genes = new ArrayList<>();
        genes.add(gene1);
        genes.add(gene2);

        SampleData sampleData = makeSampleData(genes, variantData);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName(testOutFilename).build();

        instance.writeFile(sampleData, settings);
        assertTrue(Paths.get(testOutFilename).toFile().exists());

    }

}
