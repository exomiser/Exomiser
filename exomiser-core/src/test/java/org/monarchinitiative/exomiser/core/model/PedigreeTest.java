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

package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableSet;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeTest {

    private static final Individual MOTHER = Individual.newBuilder()
            .id("mother")
            .sex(Sex.FEMALE)
            .status(Status.UNAFFECTED)
            .build();

    private static final Individual FATHER = Individual.newBuilder()
            .id("father")
            .sex(Sex.MALE)
            .status(Status.UNKNOWN)
            .build();

    private static final Individual PROBAND = Individual.newBuilder()
            .id("proband")
            .fatherId(FATHER.getId())
            .motherId(MOTHER.getId())
            .status(Status.AFFECTED)
            .build();

    @Test
    public void testEmpty() {
        Pedigree instance = Pedigree.empty();
        assertThat(instance.getIndividuals().isEmpty(), CoreMatchers.is(true));
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

    @Test(expected = IllegalArgumentException.class)
    public void individualWithEmptyIdentifier() {
        Individual.newBuilder().id("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void individualWithNullIdentifier() {
        Individual.newBuilder().id(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateIndividualId() {
        Individual duplicatedId = Individual.newBuilder().id(PROBAND.getId()).build();
        Pedigree.of(PROBAND, duplicatedId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalFatherId() {
        Individual noFather = Individual.newBuilder().id("proband").fatherId("father").build();
        Pedigree.of(noFather);
    }

    @Test(expected = IllegalArgumentException.class)
    public void incorrectSexFather() {
        Individual incorrectSexFather = Individual.newBuilder().id("father").sex(Sex.FEMALE).build();
        Pedigree.of(PROBAND, incorrectSexFather);
    }

    @Test(expected = IllegalArgumentException.class)
    public void incorrectSexMother() {
        Individual incorrectSexMother = Individual.newBuilder().id("mother").sex(Sex.MALE).build();
        Pedigree.of(PROBAND, incorrectSexMother);
    }

    //TODO other getter methods
}