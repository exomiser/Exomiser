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

import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedFilesTest {

    @Test
    public void canParsePedigreeWithSpaces() {

        Pedigree.Individual manuel = Pedigree.Individual.builder()
                .familyId("1")
                .id("Manuel")
                .sex(Sex.MALE)
                .status(Status.AFFECTED)
                .build();
        //test combos of spaces and tabs
        Pedigree instance = PedFiles.parsePedigree(Stream.of("1\tManuel 0\t0 \t1\t2 words\t  more words"));
        assertThat(instance, equalTo(Pedigree.of(manuel)));
    }

    @Test(expected = RuntimeException.class)
    public void parsePedigreeNotEnoughFields() {
        PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t1"));
    }

    @Test(expected = RuntimeException.class)
    public void parsePedigreeEmptyId() {
        PedFiles.parsePedigree(Stream.of("1\t\t\t0\t1\t2"));
    }

    @Test(expected = RuntimeException.class)
    public void parsePedigreeEmptyParentId() {
        PedFiles.parsePedigree(Stream.of("1\tManuel\t\t0\t1\t2"));
    }

    @Test(expected = RuntimeException.class)
    public void parsePedigreeIllegalStatus() {
        PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t1\t7"));
    }

        @Test
    public void parsePedigreeUnknownStatus() {
        Pedigree.Individual zero = PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t1\t0"))
                .getIndividuals().stream().findFirst().orElse(Pedigree.Individual.builder().build());
        assertThat(zero.getStatus(), equalTo(Status.UNKNOWN));

        Pedigree.Individual minusNine = PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t1\t-9"))
                .getIndividuals().stream().findFirst().orElse(Pedigree.Individual.builder().build());
        assertThat(minusNine.getStatus(), equalTo(Status.UNKNOWN));
    }
    @Test
    public void parsePedigreeUnknownSex() {
        Pedigree.Individual zero = PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t0\t0"))
                .getIndividuals().stream().findFirst().orElse(Pedigree.Individual.builder().build());
        assertThat(zero.getSex(), equalTo(Sex.UNKNOWN));

        Pedigree.Individual minusNine = PedFiles.parsePedigree(Stream.of("1\tManuel\t0\t0\t42\t0"))
                .getIndividuals().stream().findFirst().orElse(Pedigree.Individual.builder().build());
        assertThat(minusNine.getSex(), equalTo(Sex.UNKNOWN));
    }

    @Test(expected = RuntimeException.class)
    public void unknownFile() {
        PedFiles.readPedigree(Paths.get("wibble.ped"));
    }

    @Test
    public void readValidPedigree() {
        Pedigree readPedigree = PedFiles.readPedigree(TestPedigrees.trioWithChildAffectedPedPath());
        assertThat(readPedigree, equalTo(TestPedigrees.trioChildAffected()));
    }
}