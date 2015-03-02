/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import htsjdk.variant.vcf.VCFHeader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

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
public class SampleDataTest {

    private SampleData instance;

    @Mock
    Variant mockAnnotatedVariant;
    
    @Mock
    Variant mockUnAnnotatedVariant;
    
    @Before
    public void setUp() {
        instance = new SampleData();

        // This is hard-coding Jannovar's return values be aware this could change
        Mockito.when(mockAnnotatedVariant.getAnnotations()).thenReturn(Arrays.asList("Lots of lovely annotations"));
        Mockito.when(mockUnAnnotatedVariant.getAnnotations()).thenReturn(Arrays.<String> asList());
    }

    @Test
    public void noArgsConstructorInitialisesGenesVariantEvalations() {
        assertThat(instance.getGenes(), notNullValue());
        assertThat(instance.getVariantEvaluations(), notNullValue());
    }

    @Test
    public void testCanSetAndGetSampleNames() {
        List<String> sampleNames = new ArrayList<>();
        instance.setSampleNames(sampleNames);
        assertThat(instance.getSampleNames(), equalTo(sampleNames));
    }

    @Test
    public void testCanSetAndGetNumberOfSamples() {
        int numSamples = 1;
        instance.setNumberOfSamples(numSamples);
        assertThat(instance.getNumberOfSamples(), equalTo(numSamples));
    }

    @Test
    public void testCanSetAndGetVcfFilePath() {
        Path vcfPath = Paths.get("vcf");
        instance.setVcfFilePath(vcfPath);
        assertThat(instance.getVcfFilePath(), equalTo(vcfPath));
    }

    @Test
    public void testCanSetAndGetVcfHeader() {
        VCFHeader vcfHeader = new VCFHeader();
        instance.setVcfHeader(vcfHeader);
        assertThat(instance.getVcfHeader(), equalTo(vcfHeader));
    }

    @Test
    public void testCanSetAndGetVariantEvaluations() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        instance.setVariantEvaluations(variantEvaluations);
        assertThat(instance.getVariantEvaluations(), equalTo(variantEvaluations));
    }

    @Test
    public void testCanSetAndGetPedigree() {
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Individual");
        instance.setPedigree(pedigree);
        assertThat(instance.getPedigree(), equalTo(pedigree));
    }

    @Test
    public void testCanSetAndGetGenes() {
        List<Gene> genes = new ArrayList<>();
        instance.setGenes(genes);
        assertThat(instance.getGenes(), equalTo(genes));
    }

    @Test
    public void testCanReturnUnannotatedVariantEvaluations() {
        VariantEvaluation annotatedVariantEvaluation = new VariantEvaluation(mockAnnotatedVariant);
        VariantEvaluation unAnnotatedVariantEvaluation = new VariantEvaluation(mockUnAnnotatedVariant);

        List<VariantEvaluation> allVariantEvaluations = new ArrayList<>();
        allVariantEvaluations.add(annotatedVariantEvaluation);
        allVariantEvaluations.add(unAnnotatedVariantEvaluation);
        instance.setVariantEvaluations(allVariantEvaluations);

        List<VariantEvaluation> unAnnotatedVariantEvaluations = new ArrayList<>();
        unAnnotatedVariantEvaluations.add(unAnnotatedVariantEvaluation);

        assertThat(instance.getUnAnnotatedVariantEvaluations(), equalTo(unAnnotatedVariantEvaluations));
    }
}
