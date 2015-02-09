/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.writers.VcfResultsWriter;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.TargetFilterResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.pedigree.Genotype;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VcfResultsWriterTest {

    private VcfResultsWriter instance;
    
    private Gene geneOne;
    private SampleData sampleData;
    
    private ExomiserSettings settings;

    @Mock
    private AnnotationList geneOneAnnotations;   
    @Mock
    private AnnotationList geneTwoAnnotations;
    @Mock
    private AnnotationList nullAnnotationList;

    private String unannotatedVariantVcfString;
    private String passVariantVcfString;
    private String failOneFilterVariantVcfString;
    private String failTwoFilterVariantVcfString;
    private String unFilteredVariantVcfString;
    
    // FIXME(holtgrew): Uncomment lines again.

    // @Before
    // public void before() throws AnnotationException {
    // instance = new VcfResultsWriter();
    //
    // settings = new ExomiserSettings.SettingsBuilder()
    // .vcfFilePath(Paths.get("dummyVcfPath"))
    // .usePrioritiser(PriorityType.OMIM_PRIORITY)
    // .outputFormats(EnumSet.of(OutputFormat.VCF))
    // .build();
    //
    // setUpMocks();
    // setUpSampleData();
    // }
    //
    // private void setUpMocks() throws AnnotationException {
    // Mockito.when(geneOneAnnotations.getGeneSymbol()).thenReturn("ABC1");
    // Mockito.when(geneOneAnnotations.getEntrezGeneID()).thenReturn(123456);
    // Mockito.when(geneOneAnnotations.getVariantAnnotation()).thenReturn("Lovely annotations");
    //
    // Mockito.when(geneTwoAnnotations.getGeneSymbol()).thenReturn("CDE2");
    // Mockito.when(geneTwoAnnotations.getEntrezGeneID()).thenReturn(7891011);
    // Mockito.when(geneTwoAnnotations.getVariantAnnotation()).thenReturn("More lovely annotations");
    //
    // Mockito.when(nullAnnotationList.getGeneSymbol()).thenReturn(".");
    // Mockito.when(nullAnnotationList.getEntrezGeneID()).thenReturn(0);
    // Mockito.when(nullAnnotationList.getVariantAnnotation()).thenReturn(".");
    // }
    //
    // private void setUpSampleData() {
    //
    // GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
    //
    // FilterResult passTargetResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
    // FilterResult failTargetResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);
    // FilterResult failFrequencyResult = new FrequencyFilterResult(0f, FilterResultStatus.FAIL);
    //
    // byte chr1 = 1;
    // Variant passVariant = new Variant(chr1, 1, "A", "T", genotypeCall, 2.2f, "");
    // passVariant.setAnnotation(geneOneAnnotations);
    // VariantEvaluation passVariantEval = new VariantEvaluation(passVariant);
    // passVariantEval.addFilterResult(passTargetResult);
    // passVariantVcfString =
    // "chr1	1	.	A	T	2.2	PASS	;EXOMISER_GENE=ABC1;EXOMISER_VARIANT_SCORE=1.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n";
    //
    // Variant failOneFilterVariant = new Variant(chr1, 2, "T", "-", genotypeCall, 2.2f, "");
    // failOneFilterVariant.setAnnotation(geneOneAnnotations);
    // VariantEvaluation failOneFilterVariantEval = new VariantEvaluation(failOneFilterVariant);
    // failOneFilterVariantEval.addFilterResult(failTargetResult);
    // failOneFilterVariantVcfString =
    // "chr1	2	.	T	-	2.2	Target	;EXOMISER_GENE=ABC1;EXOMISER_VARIANT_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n";
    //
    // byte chr2 = 2;
    // Variant failTwoFiltersVariant = new Variant(chr2, 3, "C", "T", genotypeCall, 2.2f, "");
    // failTwoFiltersVariant.setAnnotation(geneTwoAnnotations);
    // VariantEvaluation failTwoFiltersVariantEval = new VariantEvaluation(failTwoFiltersVariant);
    // failTwoFiltersVariantEval.addFilterResult(failTargetResult);
    // failTwoFiltersVariantEval.addFilterResult(failFrequencyResult);
    // failTwoFilterVariantVcfString =
    // "chr2	3	.	C	T	2.2	Frequency;Target	;EXOMISER_GENE=CDE2;EXOMISER_VARIANT_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n";
    //
    // byte chr3 = 3;
    // Variant unAnnotatedVariant = new Variant(chr3, 4, "G", "C", genotypeCall, 2.2f, "");
    // unAnnotatedVariant.setAnnotation(nullAnnotationList);
    // VariantEvaluation unAnnotatedVariantEval = new VariantEvaluation(unAnnotatedVariant);
    // //we're not going to add any filters to this in order to test that this is reported properly according to the VCF
    // spec.
    // // unAnnotatedVariantEval.addFilterResult(FAIL_FILTER_RESULT);
    // unannotatedVariantVcfString = "chr3	4	.	G	C	2.2		;VARIANT NOT ANALYSED - NO GENE ANNOTATIONS	GT	0/1\n";
    //
    // Variant unFilteredVariant = new Variant(chr3, 5, "G", "C", genotypeCall, 2.2f, "");
    // unFilteredVariant.setAnnotation(geneTwoAnnotations);
    // VariantEvaluation unFilteredVariantEval = new VariantEvaluation(unFilteredVariant);
    // //we're not going to add any filters to this in order to test that this is reported properly according to the VCF
    // spec.
    // // unFilteredVariantEval.addFilterResult(new InheritanceFilterResult(0f, FilterResultStatus.NOT_RUN));
    // unFilteredVariantVcfString =
    // "chr3	5	.	G	C	2.2		;EXOMISER_GENE=CDE2;EXOMISER_VARIANT_SCORE=1.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_GENE_COMBINED_SCORE=0.0	GT	0/1\n";
    //
    //
    // geneOne = new Gene(passVariantEval);
    // geneOne.addVariant(failOneFilterVariantEval);
    // Gene geneTwo = new Gene(failTwoFiltersVariantEval);
    // geneTwo.addVariant(unFilteredVariantEval);
    //
    // List<Gene> geneList = new ArrayList();
    // geneList.add(geneOne);
    // geneList.add(geneTwo);
    //
    // List<VariantEvaluation> variantEvaluations = new ArrayList<>();
    // variantEvaluations.add(unAnnotatedVariantEval);
    // variantEvaluations.add(passVariantEval);
    // variantEvaluations.add(failOneFilterVariantEval);
    // variantEvaluations.add(failTwoFiltersVariantEval);
    // variantEvaluations.add(unFilteredVariantEval);
    //
    // sampleData = new SampleData();
    // sampleData.setGenes(geneList);
    // sampleData.setVariantEvaluations(variantEvaluations);
    // }
    //
    // @Test
    // public void testWriteFile() {
    // settings = new ExomiserSettings.SettingsBuilder()
    // .vcfFilePath(Paths.get("dummyVcfPath"))
    // .usePrioritiser(PriorityType.OMIM_PRIORITY)
    // .outputFormats(EnumSet.of(OutputFormat.VCF))
    // .outFileName("testWrite")
    // .build();
    // instance.writeFile(sampleData, settings);
    // assertTrue(Paths.get("testWrite.vcf").toFile().exists());
    // assertTrue(Paths.get("testWrite.vcf").toFile().delete());
    // }
    //
    // @Test
    // public void testBuildGeneVariantsString() {
    // String vcf = instance.buildGeneVariantsString(geneOne);
    // assertThat(vcf, containsString(passVariantVcfString));
    // }
    //
    // @Test
    // public void testAddColumnField() {
    // StringBuilder sb = new StringBuilder();
    // String value = "WIBBLE";
    // instance.addColumnField(sb, value);
    // String result = sb.toString();
    // assertThat(result, equalTo(value + "\t"));
    // }
    //
    // @Test
    // public void testWritePassVariant() {
    // String vcf = instance.writeString(sampleData, settings);
    // System.out.println(vcf);
    // assertThat(vcf, containsString(passVariantVcfString));
    // }
    //
    // @Test
    // public void testWriteFailVariant() {
    // String vcf = instance.writeString(sampleData, settings);
    // assertThat(vcf, containsString(failOneFilterVariantVcfString));
    // }
    //
    // @Test
    // public void testWriteFailTwoFiltersVariant() {
    // String vcf = instance.writeString(sampleData, settings);
    // assertThat(vcf, containsString(failTwoFilterVariantVcfString));
    // }
    //
    // @Test
    // public void writesUnAnnotatedVariants() {
    // String vcf = instance.writeString(sampleData, settings);
    // assertThat(vcf, containsString(unannotatedVariantVcfString));
    // }
    //
    // @Test
    // public void writesUnFilteredVariants() {
    // String vcf = instance.writeString(sampleData, settings);
    // assertThat(vcf, containsString(unFilteredVariantVcfString));
    // }
    //
    // @Test
    // public void testFormatFailedFilters() {
    // Set failedFilters = EnumSet.of(FilterType.FREQUENCY_FILTER, FilterType.PATHOGENICITY_FILTER,
    // FilterType.TARGET_FILTER);
    // assertThat(instance.makeFailedFilters(failedFilters), equalTo("Pathogenicity;Frequency;Target"));
    // }
}
