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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;
import org.phenopackets.schema.v1.core.Pedigree;
import org.phenopackets.schema.v1.core.Sex;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PhenopacketPedigreeConverterTest {

    @Test
    void emptyPedigree() {
        assertThat(PhenopacketPedigreeConverter.toExomiserPedigree(Pedigree.getDefaultInstance()), equalTo(org.monarchinitiative.exomiser.core.model.Pedigree
                .empty()));
    }

    @Test
    void singleProband() {
        Pedigree.Person proband = Pedigree.Person.newBuilder()
                .setSex(Sex.MALE)
                .setAffectedStatus(Pedigree.Person.AffectedStatus.AFFECTED)
                .setIndividualId("Zaphod")
                .build();

        Pedigree.Person female = Pedigree.Person.newBuilder()
                .setSex(Sex.FEMALE)
                .setAffectedStatus(Pedigree.Person.AffectedStatus.UNAFFECTED)
                .setIndividualId("Female")
                .build();

        Pedigree.Person other = Pedigree.Person.newBuilder()
                .setSex(Sex.OTHER_SEX)
                .setAffectedStatus(Pedigree.Person.AffectedStatus.MISSING)
                .setIndividualId("Other")
                .build();

        Pedigree pedigree = Pedigree.newBuilder()
                .addPersons(proband)
                .addPersons(female)
                .addPersons(other)
                .build();

        Individual exProband = Individual.builder()
                .sex(Individual.Sex.MALE)
                .status(Status.AFFECTED)
                .id("Zaphod")
                .build();

        Individual exFemale = Individual.builder()
                .sex(Individual.Sex.FEMALE)
                .status(Status.UNAFFECTED)
                .id("Female")
                .build();

        Individual exOther = Individual.builder()
                .sex(Individual.Sex.UNKNOWN)
                .status(Status.UNKNOWN)
                .id("Other")
                .build();
        var exPedigree = org.monarchinitiative.exomiser.core.model.Pedigree.of(exProband, exFemale, exOther);

        assertThat(PhenopacketPedigreeConverter.toExomiserPedigree(pedigree), equalTo(exPedigree));
    }

    @Test
    void singletonPedigree() {
        org.phenopackets.schema.v1.core.Individual phenopacketProband = org.phenopackets.schema.v1.core.Individual.newBuilder()
                .setId("Zaphod")
                .setSex(Sex.MALE)
                .build();

        Individual exomiserProband = Individual.builder()
                .id("Zaphod")
                .sex(Individual.Sex.MALE)
                .status(Status.AFFECTED)
                .build();

        assertThat(PhenopacketPedigreeConverter.createSingleSamplePedigree(phenopacketProband),
                equalTo(org.monarchinitiative.exomiser.core.model.Pedigree.of(exomiserProband)));
    }
}