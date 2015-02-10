/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.dao.TestJannovarDataFactory;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.variantcontext.VariantContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleDataFactoryTest {
    
    @InjectMocks
    private SampleDataFactory instance;
    
    @Mock
    private VariantAnnotator variantAnnotator;

    @Before
    public void setUp() {
        // variantAnnotator to return dummy Variant objects
        Mockito.when(variantAnnotator.annotateVariantContext(isNotNull(VariantContext.class))).thenAnswer(
                new Answer<List<Variant>>() {
            public List<Variant> answer(InvocationOnMock invocation) {
                Variant v = new Variant((VariantContext) invocation.getArguments()[0], 0,
                        new AnnotationList(new ArrayList<Annotation>()));
                        return Arrays.asList(v);
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testNullVcfThrowsANullPointer() {
        Path pedPath = Paths.get("ped");
        instance.createSampleData(null, pedPath);
    }
    
    @Test
    public void createsSampleDataWithSingleSampleVcfAndNoPedFile() {
        Path vcfPath = Paths.get("src/test/resources/Pfeiffer.vcf");
        SampleData sampleData = instance.createSampleData(vcfPath, null);
        
        String sampleName = "manuel"; 
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);
                
        assertThat(sampleData, notNullValue());
        assertThat(sampleData.getVcfFilePath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().members.get(0), equalTo(pedigree.members.get(0)));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(false));
        assertThat(sampleData.getVariantEvaluations().size(), equalTo(37709));
        assertThat(sampleData.getGenes().isEmpty(), is(true));
    }
    
    @Test
    public void returnsEmptySampleDataFromEmptyVcfFileAndNullPedFile() {
        Path vcfPath = Paths.get("src/test/resources/headerOnly.vcf");
        SampleData sampleData = instance.createSampleData(vcfPath, null);
        
        String sampleName = "manuel"; 
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);
        
        assertThat(sampleData, notNullValue());
        assertThat(sampleData.getVcfFilePath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().members.get(0), equalTo(pedigree.members.get(0)));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(true));
        assertThat(sampleData.getVariantEvaluations().size(), equalTo(0));
        assertThat(sampleData.getGenes().isEmpty(), is(true));
    }
    
    @Test(expected = RuntimeException.class)
    public void throwsErrorWithNonVcfPathAndNullPedFile() {
        Path vcfPath = Paths.get("");
        instance.createSampleData(vcfPath, null);
    }

    @Test(expected = RuntimeException.class)
    public void throwsErrorWithNonVcfFileAndNullPedFile() {
        Path vcfPath = Paths.get("src/test/resources/invalidPedTestFile.ped");
        instance.createSampleData(vcfPath, null);
    }

}
