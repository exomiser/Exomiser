/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

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
@RunWith(MockitoJUnitRunner.class)
public class HtmlResultsWriterTest {

    HtmlResultsWriter instance;

    private final String testOutFileName = "testWrite.html";
    
    private static final Integer QUALITY = 2;
    private static final Integer READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;
    private static final String GENE1_GENE_SYMBOL = "GENE1";
    private static final int GENE1_ENTREZ_GENE_ID = 1234567;

    private static final String GENE2_GENE_SYMBOL = "GENE2";
    private static final int GENE2_ENTREZ_GENE_ID = 7654321;
    
    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;
    
    @Mock
    private Variant missenseVariant;
    private VariantEvaluation missenseVariantEvaluation;
    @Mock
    private Variant indelVariant;
    private VariantEvaluation indelVariantEvaluation;

    private Gene gene1;
    private Gene gene2;
    
    @Before
    public void setUp() {
        instance = new HtmlResultsWriter();

        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, 30, 6);

        Variant unannotatedVariant1 = new Variant((byte) 1, 1, "A", "T", genotypeCall, 2.2f, "Unannotated variant");
        unAnnotatedVariantEvaluation1 = new VariantEvaluation(unannotatedVariant1);

        Variant unannotatedVariant2 = new Variant((byte) 2, 2, "T", "AAA", genotypeCall, 2.2f, "Unannotated variant");
        unAnnotatedVariantEvaluation2 = new VariantEvaluation(unannotatedVariant2);

        Mockito.when(missenseVariant.getGeneSymbol()).thenReturn(GENE1_GENE_SYMBOL);
        Mockito.when(missenseVariant.getEntrezGeneID()).thenReturn(GENE1_ENTREZ_GENE_ID);
        Mockito.when(missenseVariant.getChromosomeAsByte()).thenReturn((byte) 1);
        Mockito.when(missenseVariant.get_position()).thenReturn(1);
        Mockito.when(missenseVariant.get_ref()).thenReturn("A");
        Mockito.when(missenseVariant.get_alt()).thenReturn("T");
        Mockito.when(missenseVariant.getGenotype()).thenReturn(genotypeCall);
        Mockito.when(missenseVariant.getVariantPhredScore()).thenReturn(2.2f);
        Mockito.when(missenseVariant.getVariantReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(missenseVariant.getVariantTypeConstant()).thenReturn(VariantType.MISSENSE);
        missenseVariantEvaluation = new VariantEvaluation(missenseVariant);
        missenseVariantEvaluation.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(0.01f),
                new Frequency(0.01f), new Frequency(0.01f), new Frequency(0.01f)));
        missenseVariantEvaluation.setPathogenicityData(new PathogenicityData(new PolyPhenScore(1f),
                new MutationTasterScore(1f), new SiftScore(0f), new CaddScore(1f)));
        missenseVariantEvaluation.addFilterResult(new FrequencyFilterResult(1.0f, FilterResultStatus.PASS));
        missenseVariantEvaluation.addFilterResult(new TargetFilterResult(1.0f, FilterResultStatus.PASS));

        Mockito.when(indelVariant.getGeneSymbol()).thenReturn(GENE2_GENE_SYMBOL);
        Mockito.when(indelVariant.getEntrezGeneID()).thenReturn(GENE2_ENTREZ_GENE_ID);
        Mockito.when(indelVariant.getChromosomeAsByte()).thenReturn((byte) 2);
        Mockito.when(indelVariant.get_position()).thenReturn(2);
        Mockito.when(indelVariant.get_ref()).thenReturn("C");
        Mockito.when(indelVariant.get_alt()).thenReturn("GCT");
        Mockito.when(indelVariant.getGenotype()).thenReturn(genotypeCall);
        Mockito.when(indelVariant.getVariantPhredScore()).thenReturn(2.2f);
        Mockito.when(indelVariant.getVariantReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(indelVariant.getVariantTypeConstant()).thenReturn(VariantType.FS_INSERTION);
        indelVariantEvaluation = new VariantEvaluation(indelVariant);
        indelVariantEvaluation.addFilterResult(new FrequencyFilterResult(1.0f, FilterResultStatus.PASS));
        indelVariantEvaluation.addFilterResult(new TargetFilterResult(1.0f, FilterResultStatus.PASS));

        gene1 = new Gene(missenseVariantEvaluation);
        gene2 = new Gene(indelVariantEvaluation);

        gene1.addPriorityResult(new ExomiserMousePriorityResult("MGI:12345", "Gene1", 0.99f));
        gene2.addPriorityResult(new ExomiserMousePriorityResult("MGI:54321", "Gene2", 0.98f));

        OMIMPriorityResult gene1PriorityScore = new OMIMPriorityResult();
        gene1PriorityScore.addRow("OMIM:12345", "OMIM:67890", "Disease syndrome", 'D', 'D', 1f);
        gene1.addPriorityResult(gene1PriorityScore);
        gene2.addPriorityResult(new OMIMPriorityResult());

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
