/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalysisResultsTest {


    @Mock
    List<Annotation> mockNotEmptyListOfAnnotations;
   
    @Before
    public void setUp() {
        Mockito.when(mockNotEmptyListOfAnnotations.isEmpty()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void noArgsConstructorInitialisesGenesVariantEvalations() {
        AnalysisResults instance = AnalysisResults.builder().build();
        assertThat(instance.getGenes(), notNullValue());
        assertThat(instance.getVariantEvaluations(), notNullValue());
    }

    @Test
    public void testCanSetAndGetProbandSampleName() {
        String probandSampleName = "Slartibartfast";

        AnalysisResults instance = AnalysisResults.builder()
                .probandSampleName(probandSampleName)
                .build();

        assertThat(instance.getProbandSampleName(), equalTo(probandSampleName));
    }

    @Test
    public void testCanSetAndGetSampleNames() {
        List<String> sampleNames = Arrays.asList("David");

        AnalysisResults instance = AnalysisResults.builder()
                .sampleNames(sampleNames)
                .build();

        assertThat(instance.getSampleNames(), equalTo(sampleNames));
    }

    @Test
    public void testCanSetAndGetVcfFilePath() {
        Path vcfPath = Paths.get("vcf");
        AnalysisResults instance = AnalysisResults.builder()
                .vcfPath(vcfPath)
                .build();
        assertThat(instance.getVcfPath(), equalTo(vcfPath));
    }
    
    @Test
    public void testCanSetAndGetPedFilePath() {
        Path pedPath = Paths.get("ped");
        AnalysisResults instance = AnalysisResults.builder()
                .pedPath(pedPath)
                .build();
        assertThat(instance.getPedPath(), equalTo(pedPath));
    }

    @Test
    public void testCanSetAndGetVcfHeader() {
        VCFHeader vcfHeader = new VCFHeader();
        AnalysisResults instance = AnalysisResults.builder()
                .vcfHeader(vcfHeader)
                .build();
        assertThat(instance.getVcfHeader(), equalTo(vcfHeader));
    }

    @Test
    public void testCanSetAndGetVariantEvaluations() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        AnalysisResults instance = AnalysisResults.builder()
                .variantEvaluations(variantEvaluations)
                .build();
        assertThat(instance.getVariantEvaluations(), equalTo(variantEvaluations));
    }

    @Test
    public void testCanSetAndGetPedigree() {
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Individual");
        AnalysisResults instance = AnalysisResults.builder()
                .pedigree(pedigree)
                .build();
        assertThat(instance.getPedigree(), equalTo(pedigree));
    }

    @Test
    public void testCanSetAndGetGenes() {
        List<Gene> genes = new ArrayList<>();
        AnalysisResults instance = AnalysisResults.builder()
                .genes(genes)
                .build();
        assertThat(instance.getGenes(), equalTo(genes));
    }

    @Test
    public void testCanReturnUnannotatedVariantEvaluations() {
        VariantEvaluation annotatedVariantEvaluation = new VariantEvaluation.Builder(10, 123353297, "G", "C")
                .annotations(mockNotEmptyListOfAnnotations).build();
        
        VariantEvaluation unAnnotatedVariantEvaluation = new VariantEvaluation.Builder(7, 155604800, "C", "CTT").build();

        List<VariantEvaluation> allVariantEvaluations = Arrays.asList(annotatedVariantEvaluation, unAnnotatedVariantEvaluation);

        AnalysisResults instance = AnalysisResults.builder()
                .variantEvaluations(allVariantEvaluations)
                .build();

        List<VariantEvaluation> unAnnotatedVariantEvaluations = Arrays.asList(unAnnotatedVariantEvaluation);

        assertThat(instance.getUnAnnotatedVariantEvaluations(), equalTo(unAnnotatedVariantEvaluations));
    }
    
    @Test 
    public void testHashCode() {
        AnalysisResults instance = AnalysisResults.builder().build();
        AnalysisResults other = AnalysisResults.builder().build();
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test 
    public void testEquals() {
        Path vcf = Paths.get("test.vcf");
        AnalysisResults instance = AnalysisResults.builder().vcfPath(vcf).build();
        AnalysisResults other = AnalysisResults.builder().vcfPath(vcf).build();
        assertThat(instance, equalTo(other));
    }
    
    @Test 
    public void testNotEquals() {
        AnalysisResults instance = AnalysisResults.builder().vcfPath(Paths.get("test.vcf")).build();
        AnalysisResults other = AnalysisResults.builder().pedPath(Paths.get("other.ped")).build();
        assertThat(instance, not(equalTo(other)));
    }

    @Test
    public void testString() {
        AnalysisResults instance = AnalysisResults.builder().vcfPath(Paths.get("test.vcf")).pedPath(Paths.get("test.ped")).build();

        System.out.println(instance);
    }
}
