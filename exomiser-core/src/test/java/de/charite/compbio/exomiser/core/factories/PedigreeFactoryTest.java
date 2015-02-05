/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.factories.PedigreeFactory.PedigreeCreationException;
import de.charite.compbio.exomiser.core.model.SampleData;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedigreeFactoryTest {
    
    private PedigreeFactory instance;
    
    private static final Path nullPath = null;
    private static final Path validPedFilePath = Paths.get("src/test/resources/validPedTestFile.ped");
    private static final Path inValidPedFilePath = Paths.get("src/test/resources/invalidPedTestFile.ped");

    private SampleData singleSampleData;
    private SampleData multiSampleData;
    
    @Before
    public void setUp() {
        instance = new PedigreeFactory();
        singleSampleData = createUnNamedSingleSampleData();
        multiSampleData = new SampleData();
        multiSampleData.setNumberOfSamples(2);
    }

    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorWhenSampleDataIsEmpty() {
        SampleData emptySampleData = new SampleData();
        instance.createPedigreeForSampleData(nullPath, emptySampleData);        
    }
    
    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {      
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);
        assertThat(result.getSingleSampleName(), equalTo(PedigreeFactory.DEFAULT_SAMPLE_NAME));
    }
    
    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndNoPedFile() {

        String joeBloggs = "Joe Bloggs";
        singleSampleData.setSampleNames(Arrays.asList(joeBloggs));
                        
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);        
        assertThat(result.getSingleSampleName(), equalTo(joeBloggs));
    }
    
    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorWhenMultiSampleDataHasNoPedFile() {

        instance.createPedigreeForSampleData(nullPath, multiSampleData);        
    }
    
    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorForNamedMultiSampleWithInvalidPedFile() {

        List<String> trioNames = Arrays.asList("Adam", "Eva", "Seth");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(inValidPedFilePath, multiSampleData);        
        System.out.println(pedigree.getPedigreeSummary());
        assertThat(pedigree.getPedigreeSize(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {

        List<String> trioNames = Arrays.asList("Adam", "Eva", "Seth");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree.getPedigreeSummary());
        assertThat(pedigree.getPedigreeSize(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFileWithDisorderedNames() {

        List<String> trioNames = Arrays.asList("Adam", "Seth", "Eva");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree.getPedigreeSummary());
        assertThat(pedigree.getPedigreeSize(), equalTo(trioNames.size()));

        assertThat(pedigree.sampleIsRepresentedInPedigree("Adam"), is(true));
        assertThat(pedigree.getPerson("Adam").isUnaffected(), is(true));
        assertThat(pedigree.getPerson("Adam").isMale(), is(true));
        
        assertThat(pedigree.sampleIsRepresentedInPedigree("Eva"), is(true));
        assertThat(pedigree.getPerson("Eva").isUnaffected(), is(true));
        assertThat(pedigree.getPerson("Eva").isFemale(), is(true));

        assertThat(pedigree.sampleIsRepresentedInPedigree("Seth"), is(true));
        assertThat(pedigree.getPerson("Seth").isUnaffected(), is(false));
        assertThat(pedigree.getPerson("Seth").isMale(), is(true));
        
    }

    private SampleData createUnNamedSingleSampleData() {
        SampleData singleSampleData = new SampleData();
        singleSampleData.setNumberOfSamples(1);
        return singleSampleData;
    }
    
}
