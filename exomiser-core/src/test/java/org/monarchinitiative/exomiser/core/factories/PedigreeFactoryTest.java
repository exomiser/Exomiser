
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

package org.monarchinitiative.exomiser.core.factories;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.SampleData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedigreeFactoryTest {
    
    private PedigreeFactory instance;

    private static final Path validPedFilePath = Paths.get("src/test/resources/validPedTestFile.ped");
    private static final Path inValidPedFilePath = Paths.get("src/test/resources/invalidPedTestFile.ped");

    private static final Person ADAM = new Person("Adam", null, null, Sex.MALE, Disease.UNAFFECTED);
    private static final Person EVA = new Person("Eva", null, null, Sex.FEMALE, Disease.UNAFFECTED);
    private static final Person SETH = new Person("Seth", ADAM, EVA, Sex.MALE, Disease.AFFECTED);

    @Before
    public void setUp() {
        instance = new PedigreeFactory();
    }

    private SampleData createSampleData(String... name) {
        SampleData sampleData = new SampleData();
        sampleData.setSampleNames(Arrays.asList(name));
        sampleData.setNumberOfSamples(name.length);
        return sampleData;
    }


    private void assertContainsPerson(Person expected, Pedigree pedigree) {
        String name = expected.getName();
        assertThat(pedigree.hasPerson(name), is(true));
        Person person = pedigree.getNameToMember().get(name).getPerson();
        assertThat(person, equalTo(expected));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void throwsErrorWhenSampleDataIsEmpty() {
        SampleData emptySampleData = new SampleData();
        instance.createPedigreeForSampleData(null, emptySampleData);
    }
    
    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {
        SampleData sampleData = new SampleData();
        sampleData.setNumberOfSamples(1);

        Pedigree result = instance.createPedigreeForSampleData(null, sampleData);
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo(PedigreeFactory.DEFAULT_SAMPLE_NAME));
    }
    
    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndNoPedFile() {
        Pedigree result = instance.createPedigreeForSampleData(null, createSampleData("Adam"));
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo("Adam"));
    }
    
    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void throwsErrorWhenMultiSampleDataHasNoPedFile() {
        instance.createPedigreeForSampleData(null, createSampleData("Adam", "Eve", "Cain", "Abel"));
    }
    
    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void throwsErrorForNamedMultiSampleWithInvalidPedFile() {
        instance.createPedigreeForSampleData(inValidPedFilePath, createSampleData("Adam", "Eva", "Seth"));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Adam", "Eva", "Seth"));
        System.out.println(pedigree);
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(EVA, pedigree);
        assertContainsPerson(SETH, pedigree);
    }

    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFileWithDisorderedNames() {
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Adam", "Seth", "Eva"));
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(EVA, pedigree);
        assertContainsPerson(SETH, pedigree);
    }

    @Test
    public void createsPedigreeForPedFileWithHeader() {
        Pedigree pedigree = instance.createPedigreeForSampleData(Paths.get("src/test/resources/pedWithHeader.ped"), createSampleData("Adam", "Seth", "Eva"));
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(SETH, pedigree);
        assertContainsPerson(EVA, pedigree);
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMorePedigreeMembersThanSampleNames() {
        instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Eva", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembers() {
        instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Adam", "Eva", "Cain", "Abel", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembersAndMismatchedSample() {
        instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Adam", "Marge", "Abel", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMismatchedSampleNameAndPedigreeMember() {
        instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Adam", "Marge", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithCompletelyMismatchedSampleNames() {
        instance.createPedigreeForSampleData(validPedFilePath, createSampleData("Homer", "Marge", "Bart", "Lisa", "Maggie"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithSpacesInsteadOfTabs() {
        instance.createPedigreeForSampleData(Paths.get("src/test/resources/malformedPedTestFileWithSpaces.ped"),  createSampleData("Adam", "Eva", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreThanOneFamilyInFile() {
        instance.createPedigreeForSampleData(Paths.get("src/test/resources/multiFamilyTest.ped"),  createSampleData("Adam", "Eva", "Seth"));
    }

}
