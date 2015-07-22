/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.pedigree.Pedigree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SampleDataFactoryTestConfig.class)
public class SampleDataFactoryTest {
    
    @Autowired
    private SampleDataFactory instance;

    @Test(expected = NullPointerException.class)
    public void testNullVcfThrowsANullPointer() {
        Path pedPath = Paths.get("ped");
        instance.createSampleData(null, pedPath);
    }
    
    @Test
    public void createsSampleDataWithSingleSampleVcfAndNoPedFile() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        SampleData sampleData = instance.createSampleData(vcfPath, null);
        
        String sampleName = "manuel"; 
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);

        assertThat(sampleData, notNullValue());
        assertThat(sampleData.getVcfPath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().getMembers().get(0), equalTo(pedigree.getMembers().get(0)));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(false));
        assertThat(sampleData.getVariantEvaluations().size(), equalTo(3));
        assertThat(sampleData.getGenes().isEmpty(), is(false));
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
        assertThat(sampleData.getVcfPath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().getMembers().get(0), equalTo(pedigree.getMembers().get(0)));
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
