/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.TargetFilterScore;
import jannovar.annotation.AnnotationList;
import jannovar.common.Genotype;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriterTest {

    private final Gene gene;
    private final Gene failGene;
    private final VcfResultsWriter instance;
    private static final String PASS_VARIANT_STRING = "chr1	1	.	A	T	2.2	PASS	;EXOMISER_GENE=.;EXOMISER_VARIANT_SCORE=1.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n";
    private static final FilterType FAIL_FILTER_TYPE = FilterType.PATHOGENICITY_FILTER;
    private static final String FAIL_VARIANT_STRING = String.format("chr1	2	.	T	-	2.2	%s	;EXOMISER_GENE=.;EXOMISER_VARIANT_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n", FAIL_FILTER_TYPE.toString());
    private SampleData sampleData;
    
    @Mock
    AnnotationList annotationList;
    
    public VcfResultsWriterTest() {
        instance = new VcfResultsWriter();
        
        MockitoAnnotations.initMocks(this);
        Mockito.when(annotationList.getGeneSymbol()).thenReturn(".");
        Mockito.when(annotationList.getEntrezGeneID()).thenReturn(0);
        
        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
        byte chr = 1;

        
        Variant passVariant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f, "");
        passVariant.setAnnotation(annotationList);
        VariantEvaluation passVariantEval = new VariantEvaluation(passVariant);
        gene = new Gene(passVariantEval);
        
        Variant failVariant = new Variant(chr, 2, "T", "-", genotypeCall, 2.2f, "");
        failVariant.setAnnotation(annotationList);
        VariantEvaluation failVariantEval = new VariantEvaluation(failVariant);
        failVariantEval.addFailedFilter(FAIL_FILTER_TYPE, new TargetFilterScore(0f));
        failGene = new Gene(failVariantEval);
    }
    
    @Before
    public void before() {
        sampleData = new SampleData();
        sampleData.setGeneList(new ArrayList<Gene>());
    }
    

    @Test
    public void testWriteFile() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormats(EnumSet.of(OutputFormat.VCF)).build();
        instance.writeFile(sampleData, settings, null);
        assertTrue(Paths.get("testWrite.vcf").toFile().exists());
        assertTrue(Paths.get("testWrite.vcf").toFile().delete());
    }

    @Test
    public void testBuildGeneVariantsString() {
        String result = instance.buildGeneVariantsString(gene);
        assertThat(result, equalTo(PASS_VARIANT_STRING));
    }

    @Test
    public void testAddColumnField() {
        StringBuilder sb = new StringBuilder();
        String value = "WIBBLE";
        VcfResultsWriter instance = new VcfResultsWriter();
        instance.addColumnField(sb, value);
        String result = sb.toString();
        assertThat(result, equalTo(value + "\t"));
    }

    @Test
    public void testWritePassGene() {
        List<Gene> geneList = new ArrayList();
        geneList.add(gene);
        sampleData.setGeneList(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.VCF)).build();
        String result = instance.writeString(sampleData, settings, null);
        //there will be a header portion which we're not interested in for this test.
        System.out.println(result);
        assertThat(result, endsWith(PASS_VARIANT_STRING));
    }

    @Test
    public void testWriteFailGene() {
        List<Gene> geneList = new ArrayList();
        geneList.add(failGene);
        sampleData.setGeneList(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.VCF)).build();
        String result = instance.writeString(sampleData, settings, null);
        //there will be a header portion which we're not interested in for this test.
        System.out.println(result);
        assertThat(result, endsWith(FAIL_VARIANT_STRING));
    }
    
    @Test
    public void testFormatFailedFilters() {
        Set failedFilters = EnumSet.of(FilterType.FREQUENCY_FILTER, FilterType.PATHOGENICITY_FILTER, FilterType.TARGET_FILTER);
        assertThat(instance.formatFailedFilters(failedFilters), equalTo("Pathogenicity;Frequency;Target"));
    }
}
