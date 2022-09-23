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

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;
import org.phenopackets.schema.v1.core.Sex;

import java.util.Set;
import java.util.function.Function;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class PhenopacketPedigreeConverter {

    private PhenopacketPedigreeConverter() {
    }

    public static Pedigree createSingleSamplePedigree(org.phenopackets.schema.v1.core.Individual individual) {
        Pedigree.Individual proband = Pedigree.Individual.builder()
                .id(individual.getId())
                .sex(toExomiserSex(individual.getSex()))
                .status(Individual.Status.AFFECTED)
                .build();
        return Pedigree.of(proband);
    }

    public static Pedigree toExomiserPedigree(org.phenopackets.schema.v1.core.Pedigree pedigree) {
        Set<Individual> individuals = pedigree.getPersonsList()
                .stream()
                .map(toIndividual())
                .collect(ImmutableSet.toImmutableSet());
        return Pedigree.of(individuals);
    }

    private static Function<Person, Individual> toIndividual() {
        return person -> Individual.builder()
                .familyId(person.getFamilyId())
                .id(person.getIndividualId())
                .motherId(person.getMaternalId())
                .fatherId(person.getPaternalId())
                .sex(toExomiserSex(person.getSex()))
                .status(toExomiserStatus(person.getAffectedStatus()))
                .build();
    }

    private static Individual.Status toExomiserStatus(AffectedStatus affectedStatus) {
        return switch (affectedStatus) {
            case AFFECTED -> Individual.Status.AFFECTED;
            case UNAFFECTED -> Individual.Status.UNAFFECTED;
            case UNRECOGNIZED, MISSING -> Individual.Status.UNKNOWN;
        };
    }

    public static org.phenopackets.schema.v1.core.Pedigree toPhenopacketPedigree(Pedigree pedigree) {
        Set<Person> people = pedigree.getIndividuals()
                .stream()
                .map(toPerson())
                .collect(ImmutableSet.toImmutableSet());
        return org.phenopackets.schema.v1.core.Pedigree.newBuilder()
                .addAllPersons(people)
                .build();
    }

    private static Function<Individual, Person> toPerson() {
        return individual -> Person.newBuilder()
                .setFamilyId(individual.getFamilyId())
                .setIndividualId(individual.getId())
                .setMaternalId(individual.getMotherId())
                .setPaternalId(individual.getFatherId())
                .setSex(toPhenopacketSex(individual.getSex()))
                .setAffectedStatus(toPhenopacketStatus(individual.getStatus()))
                .build();
    }

    private static AffectedStatus toPhenopacketStatus(Individual.Status status) {
        return switch (status) {
            case UNAFFECTED -> AffectedStatus.UNAFFECTED;
            case AFFECTED -> AffectedStatus.AFFECTED;
            case UNKNOWN -> AffectedStatus.MISSING;
        };
    }

    public static Sex toPhenopacketSex(Individual.Sex sex) {
        return switch (sex) {
            case FEMALE -> Sex.FEMALE;
            case MALE -> Sex.MALE;
            case UNKNOWN -> Sex.UNKNOWN_SEX;
        };
    }

    public static Individual.Sex toExomiserSex(Sex sex) {
        return switch (sex) {
            case FEMALE -> Individual.Sex.FEMALE;
            case MALE -> Individual.Sex.MALE;
            case OTHER_SEX, UNKNOWN_SEX, UNRECOGNIZED -> Individual.Sex.UNKNOWN;
        };
    }

}
