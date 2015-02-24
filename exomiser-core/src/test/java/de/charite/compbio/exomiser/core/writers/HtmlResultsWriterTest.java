/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.dao.TestVariantFactory;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.Genotype;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class HtmlResultsWriterTest {

    private HtmlResultsWriter instance;

    /** The temporary folder to write files to, automatically removed after tests finish. */
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static TemplateEngine templateEngine;

    private String testOutFileName;

    private VariantEvaluation missenseVariantEvaluation;
    private VariantEvaluation indelVariantEvaluation;

    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene gene1;
    private Gene gene2;

    @BeforeClass
    public static void makeTemplateEngine() {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("html/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Before
    public void setUp() {
        instance = new HtmlResultsWriter(templateEngine);

        TestVariantFactory varFactory = new TestVariantFactory();
        Variant missenseVariant = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0,
                2.2);
        Variant indelVariant = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        missenseVariantEvaluation = new VariantEvaluation(missenseVariant);
        indelVariantEvaluation = new VariantEvaluation(indelVariant);

        gene1 = new Gene(missenseVariantEvaluation.getGeneSymbol(), missenseVariantEvaluation.getEntrezGeneID());
        gene1.addVariant(missenseVariantEvaluation);
        
        gene2 = new Gene(indelVariantEvaluation.getGeneSymbol(), indelVariantEvaluation.getEntrezGeneID());
        gene2.addVariant(indelVariantEvaluation);
        
        gene1.addPriorityResult(new PhivePriorityResult("MGI:12345", "Gene1", 0.99f));
        gene2.addPriorityResult(new PhivePriorityResult("MGI:54321", "Gene2", 0.98f));

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
        Paths.get(testOutFileName).toFile().delete();
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
    public void testWriteTemplateWithEmptyData() throws Exception{
        testOutFileName = "testWrite.html";
        
        SampleData sampleData = makeSampleData(new ArrayList<Gene>(), new ArrayList<VariantEvaluation>());
        ExomiserSettings settings = getSettingsBuilder()
                .outFileName(testOutFileName).build();

        instance.writeFile(sampleData, settings);
        Path testOutFile = Paths.get(testOutFileName);
        assertTrue(testOutFile.toFile().exists());

    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() throws Exception {
        testOutFileName = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantData.html").toString();
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);
        SampleData sampleData = makeSampleData(new ArrayList<Gene>(), variantData);
        ExomiserSettings settings = getSettingsBuilder().outFileName(testOutFileName).build();

        instance.writeFile(sampleData, settings);

        Path testOutFile = Paths.get(testOutFileName);
        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        testOutFileName = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantDataAndGenes.html").toString();
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);

        List<Gene> genes = new ArrayList<>();
        genes.add(gene1);
        genes.add(gene2);

        SampleData sampleData = makeSampleData(genes, variantData);
        ExomiserSettings settings = getSettingsBuilder().outFileName(testOutFileName).build();

        instance.writeFile(sampleData, settings);
        Path testOutFile = Paths.get(testOutFileName);
        assertTrue(testOutFile.toFile().exists());
    }

    private static ExomiserSettings.SettingsBuilder getSettingsBuilder() {
        return new ExomiserSettings.SettingsBuilder()
                .vcfFilePath(Paths.get(System.getProperty("java.io.tmpdir"), "temp.vcf"))
                .usePrioritiser(PriorityType.NONE);
    }

}