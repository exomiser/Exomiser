/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeTest {

    private static final Individual MOTHER = Individual.builder()
            .id("mother")
            .sex(Sex.FEMALE)
            .status(Status.UNAFFECTED)
            .build();

    private static final Individual FATHER = Individual.builder()
            .id("father")
            .sex(Sex.MALE)
            .status(Status.UNKNOWN)
            .build();

    private static final Individual PROBAND = Individual.builder()
            .id("proband")
            .fatherId(FATHER.getId())
            .motherId(MOTHER.getId())
            .status(Status.AFFECTED)
            .build();

    @Test
    public void testEmpty() {
        Pedigree instance = Pedigree.empty();
        assertThat(instance.getIndividuals().isEmpty(), is(true));
    }

    @Test
    public void testStaticConstructor() {
        Set<Individual> individuals = ImmutableSet.of(PROBAND, MOTHER, FATHER);
        Pedigree instance = Pedigree.of(individuals);
        assertThat(instance.getIndividuals(), equalTo(individuals));
    }

    @Test
    public void testStaticVarArgsConstructor() {
        Pedigree instance = Pedigree.of(PROBAND, MOTHER, FATHER);
        instance.getIndividuals().forEach(System.out::println);
        assertThat(instance.getIndividuals(), equalTo(ImmutableSet.of(PROBAND, MOTHER, FATHER)));
    }

    @Test
    public void testJustProband() {
        Pedigree instance = Pedigree.justProband("proband");
        Individual expectedProband = Individual.builder()
                .id("proband")
                .sex(Sex.UNKNOWN)
                .status(Status.AFFECTED)
                .build();
        assertThat(instance.getIndividuals(), equalTo(ImmutableSet.of(expectedProband)));
    }

    @Test
    public void testJustProbandWithSex() {
        Pedigree instance = Pedigree.justProband("proband", Sex.MALE);
        Individual expectedProband = Individual.builder().id("proband").sex(Sex.MALE).status(Status.AFFECTED).build();
        assertThat(instance.getIndividuals(), equalTo(ImmutableSet.of(expectedProband)));
    }

    @Test
    public void individualWithEmptyIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> Individual.builder().id("").build());
    }

    @Test
    public void individualWithNullIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> Individual.builder().id(null).build());
    }

    @Test
    public void duplicateIndividualId() {
        Individual duplicatedId = Individual.builder().id(PROBAND.getId()).build();
        assertThrows(IllegalArgumentException.class, () -> Pedigree.of(PROBAND, duplicatedId));
    }

    @Test
    public void illegalFatherId() {
        Individual noFather = Individual.builder().id("proband").fatherId("father").build();
        assertThrows(IllegalArgumentException.class, () -> Pedigree.of(noFather));
    }

    @Test
    public void incorrectSexFather() {
        Individual incorrectSexFather = Individual.builder().id("father").sex(Sex.FEMALE).build();
        assertThrows(IllegalArgumentException.class, () -> Pedigree.of(PROBAND, incorrectSexFather));
    }

    @Test
    public void incorrectSexMother() {
        Individual incorrectSexMother = Individual.builder().id("mother").sex(Sex.MALE).build();
        assertThrows(IllegalArgumentException.class, () -> Pedigree.of(PROBAND, incorrectSexMother));
    }

    @Test
    public void testSize() {
        assertThat(Pedigree.empty().size(), equalTo(0));
        assertThat(Pedigree.justProband("Nemo").size(), equalTo(1));
        assertThat(Pedigree.of(PROBAND, MOTHER, FATHER).size(), equalTo(3));
    }

    @Test
    public void testIsEmpty() {
        assertThat(Pedigree.empty().isEmpty(), is(true));
        assertThat(Pedigree.justProband("Nemo").isEmpty(), is(false));
    }

    @Test
    public void testGetIdentifiers() {
        Pedigree instance = Pedigree.of(PROBAND, MOTHER, FATHER);
        assertThat(instance.getIdentifiers(), equalTo(ImmutableSet.of(PROBAND.getId(), MOTHER.getId(), FATHER.getId())));
    }

    @Test
    public void testContainsId() {
        Pedigree instance = Pedigree.justProband("Nemo");
        assertThat(instance.containsId("Nemo"), is(true));
        assertThat(instance.containsId("Someone"), is(false));
    }

    @Test
    public void testGetIndividuals() {
        Pedigree instance = Pedigree.of(PROBAND, MOTHER, FATHER);
        assertThat(instance.getIndividuals(), equalTo(ImmutableSet.of(PROBAND, MOTHER, FATHER)));
    }

    @Test
    public void testGetIndividualById() {
        Pedigree instance = Pedigree.of(PROBAND, MOTHER, FATHER);
        assertThat(instance.getIndividualById(MOTHER.getId()), equalTo(MOTHER));
    }
}