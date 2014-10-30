/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import jannovar.exception.PedParseException;
import jannovar.pedigree.Pedigree;
import jannovar.pedigree.Person;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleDataTest {
    
    private SampleData instance; 
        
    
    @Before
    public void setUp() {
        instance = new SampleData();
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
        List<String> vcfHeader = new ArrayList<>();        
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
    public void testCanSetAndGetPedigree() throws PedParseException {
        Pedigree pedigree = new Pedigree(new ArrayList<Person>(), "Family Robinson");        
        instance.setPedigree(pedigree);
        assertThat(instance.getPedigree(), equalTo(pedigree));
    }

    @Test
    public void testCanSetAndGetGenes() {
        List<Gene> genes = new ArrayList<>();       
        instance.setGenes(genes);
        assertThat(instance.getGenes(), equalTo(genes));    
    }
    
}
