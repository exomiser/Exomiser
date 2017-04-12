
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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedigreeFactoryTest {
    
    private PedigreeFactory instance;

    private static final Path validPedFilePath = Paths.get("src/test/resources/pedValid.ped");
    private static final Path inValidPedFilePath = Paths.get("src/test/resources/pedNotValid.ped");

    private static final Person ADAM = new Person("Adam", null, null, Sex.MALE, Disease.UNAFFECTED);
    private static final Person EVA = new Person("Eva", null, null, Sex.FEMALE, Disease.UNAFFECTED);
    private static final Person SETH = new Person("Seth", ADAM, EVA, Sex.MALE, Disease.AFFECTED);

    @Before
    public void setUp() {
        instance = new PedigreeFactory();
    }

    private void assertContainsPerson(Person expected, Pedigree pedigree) {
        String name = expected.getName();
        assertThat(pedigree.hasPerson(name), is(true));
        Person person = pedigree.getNameToMember().get(name).getPerson();
        assertThat(person, equalTo(expected));
    }

    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {
        Pedigree result = instance.createPedigreeForSampleData(null, Collections.emptyList());
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo(PedigreeFactory.DEFAULT_SAMPLE_NAME));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void pedigreePresentAndSampleHasNoName() {
        instance.createPedigreeForSampleData(validPedFilePath, Collections.emptyList());
    }

    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndNoPedFile() {
        Pedigree result = instance.createPedigreeForSampleData(null, Arrays.asList("Adam"));
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo("Adam"));
    }

    @Test
    public void createsSingleSamplePedigreeWithSampleName() {
        Pedigree result = instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Seth"));
        Person person = result.getMembers().get(0);
        assertThat(person.getName(), equalTo("Seth"));    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void throwsErrorWhenMultiSampleDataHasNoPedFile() {
        instance.createPedigreeForSampleData(null, Arrays.asList("Adam", "Eve", "Cain", "Abel"));
    }

    @Test
    public void throwsErrorForNamedMultiSampleWithInvalidPedFile() {
        Pedigree pedigree = instance.createPedigreeForSampleData(inValidPedFilePath, Arrays.asList("Adam", "Eva", "Seth"));
        pedigree.getMembers().forEach(System.out::println);
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        //TODO: should we throw an exception still, log a warning, or leave it to go through?
        Person eva = pedigree.getNameToMember().get(EVA.getName()).getPerson();
        assertThat(eva.getSex(), equalTo(Sex.UNKNOWN));

        Person seth = pedigree.getNameToMember().get(SETH.getName()).getPerson();
        assertThat(seth, equalTo(new Person("Seth", ADAM, eva, Sex.MALE, Disease.AFFECTED)));
    }
    
    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Adam", "Eva", "Seth"));
        System.out.println(pedigree);
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(EVA, pedigree);
        assertContainsPerson(SETH, pedigree);
    }

    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFileWithDisorderedNames() {
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Adam", "Seth", "Eva"));
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(EVA, pedigree);
        assertContainsPerson(SETH, pedigree);
    }

    @Test
    public void createsPedigreeForPedFileWithHeader() {
        Pedigree pedigree = instance.createPedigreeForSampleData(Paths.get("src/test/resources/pedWithHeader.ped"), Arrays.asList("Adam", "Seth", "Eva"));
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(SETH, pedigree);
        assertContainsPerson(EVA, pedigree);
    }

    @Test
    public void testCreatePedigreeWithMorePedigreeMembersThanSampleNames() {
        Pedigree pedigree = instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Eva", "Seth"));
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(SETH, pedigree);
        assertContainsPerson(EVA, pedigree);
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembers() {
        instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Adam", "Eva", "Cain", "Abel", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembersAndMismatchedSample() {
        instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Adam", "Marge", "Abel", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMismatchedSampleNameAndPedigreeMember() {
        instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Adam", "Marge", "Seth"));
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithCompletelyMismatchedSampleNames() {
        instance.createPedigreeForSampleData(validPedFilePath, Arrays.asList("Homer", "Marge", "Bart", "Lisa", "Maggie"));
    }

    @Test
    public void testCreatePedigreeWithSpacesInsteadOfTabs() {
        Pedigree pedigree = instance.createPedigreeForSampleData(Paths.get("src/test/resources/pedWithSpaces.ped"), Arrays
                .asList("Adam", "Eva", "Seth"));
        pedigree.getMembers().forEach(System.out::println);
        assertThat(pedigree.getMembers().size(), equalTo(3));
        assertContainsPerson(ADAM, pedigree);
        assertContainsPerson(EVA, pedigree);
        assertContainsPerson(SETH, pedigree);
    }

    @Test(expected = PedigreeFactory.PedigreeCreationException.class)
    public void testCreatePedigreeWithMoreThanOneFamilyInFile() {
        instance.createPedigreeForSampleData(Paths.get("src/test/resources/pedMultiFamily.ped"), Arrays.asList("Adam", "Eva", "Seth"));
    }

}
