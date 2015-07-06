/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.factories.TestVariantFactory;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.PassFilterResult;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriterTest {

    private HtmlResultsWriter instance;

    /**
     * The temporary folder to write files to, automatically removed after tests
     * finish.
     */
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static TemplateEngine templateEngine;

    private String testOutFilePrefix;

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

        missenseVariantEvaluation = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        missenseVariantEvaluation.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(0.01f, FrequencySource.THOUSAND_GENOMES)));
        missenseVariantEvaluation.setPathogenicityData(new PathogenicityData(new PolyPhenScore(1f), new MutationTasterScore(1f), new SiftScore(0f), new CaddScore(1f)));
        missenseVariantEvaluation.addFilterResult(new PassFilterResult(FilterType.FREQUENCY_FILTER, 1.0f));
        missenseVariantEvaluation.addFilterResult(new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER, 1.0f));

        indelVariantEvaluation = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        gene1 = new Gene(missenseVariantEvaluation.getGeneSymbol(), missenseVariantEvaluation.getEntrezGeneId());
        gene1.addVariant(missenseVariantEvaluation);

        gene2 = new Gene(indelVariantEvaluation.getGeneSymbol(), indelVariantEvaluation.getEntrezGeneId());
        gene2.addVariant(indelVariantEvaluation);

        gene1.addPriorityResult(new PhivePriorityResult("MGI:12345", "Gene1", 0.99f));
        gene2.addPriorityResult(new PhivePriorityResult("MGI:54321", "Gene2", 0.98f));

        OMIMPriorityResult gene1PriorityScore = new OMIMPriorityResult();
        gene1PriorityScore.addRow("OMIM:12345", "OMIM:67890", "Disease syndrome", 'D', 'D', 1f);
        gene1.addPriorityResult(gene1PriorityScore);
        gene2.addPriorityResult(new OMIMPriorityResult());

        unAnnotatedVariantEvaluation1 = varFactory.constructVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        unAnnotatedVariantEvaluation2 = varFactory.constructVariant(5, 10, "C", "T", Genotype.HETEROZYGOUS, 30, 0, 1.0);
    }

    @After
    public void tearDown() {
        Paths.get(testOutFilePrefix).toFile().delete();
    }

    private Analysis makeAnalysis(List<Gene> genes, List<VariantEvaluation> variantEvaluations) {
        SampleData sampleData = new SampleData();
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add("Slartibartfast");
        sampleData.setSampleNames(sampleNames);
        sampleData.setNumberOfSamples(1);
        sampleData.setGenes(genes);
        sampleData.setVariantEvaluations(variantEvaluations);

        Analysis analysis = new Analysis();
        analysis.setSampleData(sampleData);
        return analysis;
    }

    @Test
    public void testWriteTemplateWithEmptyData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWrite.html").toString();

        Analysis analysis = makeAnalysis(new ArrayList<Gene>(), new ArrayList<VariantEvaluation>());
       
        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());

    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantData() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantData.html").toString();
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);
        Analysis analysis = makeAnalysis(new ArrayList<Gene>(), variantData);
        
        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, settings);

        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());
    }

    @Test
    public void testWriteTemplateWithUnAnnotatedVariantDataAndGenes() throws Exception {
        testOutFilePrefix = tmpFolder.newFile("testWriteTemplateWithUnAnnotatedVariantDataAndGenes.html").toString();
        List<VariantEvaluation> variantData = new ArrayList<>();
        variantData.add(unAnnotatedVariantEvaluation1);
        variantData.add(unAnnotatedVariantEvaluation2);

        List<Gene> genes = new ArrayList<>();
        genes.add(gene1);
        genes.add(gene2);

        Analysis analysis = makeAnalysis(genes, variantData);
        
        OutputSettings settings = new OutputSettingsBuilder().outputPrefix(testOutFilePrefix).build();

        instance.writeFile(analysis, settings);
        Path testOutFile = Paths.get(testOutFilePrefix);
        assertTrue(testOutFile.toFile().exists());
    }

}
