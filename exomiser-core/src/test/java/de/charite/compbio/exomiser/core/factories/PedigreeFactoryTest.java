
/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.factories.PedigreeFactory.PedigreeCreationException;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
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

    private SampleData createUnNamedSingleSampleData() {
        SampleData singleSampleData = new SampleData();
        singleSampleData.setNumberOfSamples(1);
        return singleSampleData;
    }

    @Test(expected = PedigreeCreationException.class)
    public void throwsErrorWhenSampleDataIsEmpty() {
        SampleData emptySampleData = new SampleData();
        instance.createPedigreeForSampleData(nullPath, emptySampleData);        
    }
    
    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {      
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo(PedigreeFactory.DEFAULT_SAMPLE_NAME));
    }
    
    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndNoPedFile() {

        String joeBloggs = "Joe Bloggs";
        singleSampleData.setSampleNames(Arrays.asList(joeBloggs));
                        
        Pedigree result = instance.createPedigreeForSampleData(nullPath, singleSampleData);        
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo(joeBloggs));
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
        System.out.println(pedigree);
        assertThat(pedigree.getMembers().size(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {

        List<String> trioNames = Arrays.asList("Adam", "Eva", "Seth");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree);
        assertThat(pedigree.getMembers().size(), equalTo(trioNames.size()));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFileWithDisorderedNames() {

        List<String> trioNames = Arrays.asList("Adam", "Seth", "Eva");
        multiSampleData.setSampleNames(trioNames);
                        
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);        
        System.out.println(pedigree);
        assertThat(pedigree.getMembers().size(), equalTo(trioNames.size()));

        assertThat(pedigree.hasPerson("Adam"), is(true));
        Person adam = pedigree.getNameToMember().get("Adam").getPerson();
        assertThat(adam.isUnaffected(), is(true));
        assertThat(adam.isMale(), is(true));
        
        assertThat(pedigree.hasPerson("Eva"), is(true));
        Person eva = pedigree.getNameToMember().get("Eva").getPerson();
        assertThat(eva.isUnaffected(), is(true));
        assertThat(eva.isFemale(), is(true));

        assertThat(pedigree.hasPerson("Seth"), is(true));
        Person seth = pedigree.getNameToMember().get("Seth").getPerson();
        assertThat(seth.isUnaffected(), is(false));
        assertThat(seth.isMale(), is(true));
        
    }

    @Test(expected = PedigreeCreationException.class)
    public void testCreatePedigreeWithMismatchedSampleNames() {
        multiSampleData.setSampleNames(Arrays.asList("Homer", "Seth", "Marge", "Bart"));
        //TODO: should this fail or not? Requires full system testing :(
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, multiSampleData);
        System.out.println(pedigree);
    }

    @Test(expected = PedigreeCreationException.class)
    public void testCreatePedigreeWithSpacesInsteadOfTabs() {
        multiSampleData.setSampleNames(Arrays.asList("Adam", "Seth", "Eva"));

        instance.createPedigreeForSampleData(Paths.get("src/test/resources/malformedPedTestFileWithSpaces.ped"), multiSampleData);
    }

}
