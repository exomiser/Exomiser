/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.AFFECTED;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeSampleValidatorTest {

    // Contents of src/test/resources/pedValid.ped
    //    1	Eva	0	0	2	1
    //    1	Adam	0	0	1	1
    //    1	Seth	Adam	Eva	1	2

    private static final Individual ADAM = Individual.builder()
            .familyId("1")
            .id("Adam")
            .sex(Individual.Sex.MALE)
            .status(Individual.Status.UNAFFECTED)
            .build();

    private static final Individual EVA = Individual.builder()
            .familyId("1")
            .id("Eva")
            .sex(Individual.Sex.FEMALE)
            .status(Individual.Status.UNAFFECTED)
            .build();

    private static final Individual SETH = Individual.builder()
            .familyId("1")
            .id("Seth")
            .sex(Individual.Sex.MALE)
            .fatherId("Adam")
            .motherId("Eva")
            .status(AFFECTED)
            .build();

    private static final Pedigree VALID_PEDIGREE = Pedigree
            .of(ADAM, EVA, SETH);

    @Test(expected = NullPointerException.class)
    public void pedigreeNotNullInConstructor() {
        PedigreeSampleValidator.validate(null, SampleIdentifier.of("sample", 0), Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void probandNotNullInConstructor() {
        PedigreeSampleValidator.validate(Pedigree.empty(), null, Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void sampleNamesNotNullInConstructor() {
        PedigreeSampleValidator.validate(Pedigree.empty(), SampleIdentifier.of("sample", 0), null);
    }

    @Test
    public void createsSingleSamplePedigreeWithDefaultNameWhenSampleHasNoNameOrPedFile() {
        Pedigree result = PedigreeSampleValidator.validate(Pedigree.empty(), SampleIdentifier.defaultSample(), Collections
                .emptyList());
        Pedigree expected = Pedigree.justProband(SampleIdentifier.defaultSample().getId());
        assertThat(result, equalTo(expected));
    }

    @Test(expected = RuntimeException.class)
    public void pedigreePresentAndSampleHasNoName() {
        Individual individual = Individual.builder().id("Nemo").build();
        Pedigree input = Pedigree.of(individual);
        PedigreeSampleValidator.validate(input, SampleIdentifier.defaultSample(), Collections.emptyList());
    }

    @Test(expected = RuntimeException.class)
    public void noPedigreeSampleIdentifierAndSampleNamesDoNotMatch() {
        PedigreeSampleValidator.validate(Pedigree.empty(), SampleIdentifier.of("Nemo", 0), ImmutableList.of("Adam"));
    }

    @Test()
    public void singleSamplePedigreePresentAndAllIsWell() {
        Pedigree input = Pedigree.justProband("Adam");
        Pedigree validated = PedigreeSampleValidator.validate(input, SampleIdentifier.of("Adam", 0), ImmutableList.of("Adam"));
        assertThat(input, equalTo(validated));
    }

    @Test
    public void createsSingleSamplePedigreeWhenSampleHasOnlyOneNamedMemberAndEmptyPedigree() {
        Pedigree result = PedigreeSampleValidator.validate(Pedigree.empty(), SampleIdentifier.of("Adam", 0), ImmutableList
                .of("Adam"));
        Pedigree expected = Pedigree.justProband("Adam");
        assertThat(result, equalTo(expected));
    }

    @Test
    public void createsSingleSamplePedigreeWithSampleName() {
        Pedigree result = PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 0), ImmutableList
                .of("Seth"));
        assertThat(result, equalTo(VALID_PEDIGREE));
    }

    @Test(expected = RuntimeException.class)
    public void throwsErrorWhenMultiSampleDataHasNoPedFile() {
        PedigreeSampleValidator.validate(Pedigree.empty(), SampleIdentifier.of("Cain", 2), ImmutableList.of("Adam", "Eve", "Cain", "Abel"));
    }

    @Test
    public void createsPedigreeForNamedMultiSampleWithPedFile() {
        Pedigree result = PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 2), ImmutableList
                .of("Adam", "Eva", "Seth"));
        assertThat(result, equalTo(VALID_PEDIGREE));
    }

    @Test
    public void testCreatePedigreeWithMorePedigreeMembersThanSampleNames() {
        Pedigree result = PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 2), ImmutableList
                .of("Eva", "Seth"));
        assertThat(result, equalTo(VALID_PEDIGREE));
    }

    @Test(expected = RuntimeException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembers() {
        PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 4), ImmutableList.of("Adam", "Eva", "Cain", "Abel", "Seth"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreatePedigreeWithMoreSampleNamesThanPedigreeMembersAndMismatchedSample() {
        PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 3), ImmutableList.of("Adam", "Marge", "Abel", "Seth"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreatePedigreeWithMismatchedSampleNameAndPedigreeMember() {
        PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 2), ImmutableList.of("Adam", "Marge", "Seth"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreatePedigreeWithCompletelyMismatchedSampleNames() {
        PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Seth", 2), ImmutableList.of("Homer", "Marge", "Bart", "Lisa", "Maggie"));
    }

    @Test(expected = RuntimeException.class)
    public void testValidatePedigreeWithMisMatchedSampleIdentifier() {
        PedigreeSampleValidator.validate(VALID_PEDIGREE, SampleIdentifier.of("Homer", 2), ImmutableList.of("Adam", "Eva", "Seth"));
    }

    @Test(expected = RuntimeException.class)
    public void validatePedigreeWithUnaffectedProband() {
        Pedigree pedigree = Pedigree.of(
                Individual.builder()
                        .id("Adam")
                        .status(Individual.Status.UNAFFECTED)
                        .build()
        );
        PedigreeSampleValidator.validate(pedigree, SampleIdentifier.of("Adam", 0), ImmutableList.of("Adam"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreatePedigreeWithMoreThanOneFamilyInFile() {
        Pedigree multiFamilyPed = Pedigree.of(
                Individual.builder()
                        .familyId("Adams")
                        .id("Adam")
                        .sex(Individual.Sex.MALE)
                        .status(Individual.Status.UNAFFECTED)
                        .build(),
                Individual.builder()
                        .familyId("Simpsons")
                        .id("Homer")
                        .sex(Individual.Sex.MALE)
                        .status(Individual.Status.UNAFFECTED)
                        .build()
        );
        PedigreeSampleValidator.validate(multiFamilyPed, SampleIdentifier.of("Homer", 1), ImmutableList.of("Adam", "Homer"));
    }
}