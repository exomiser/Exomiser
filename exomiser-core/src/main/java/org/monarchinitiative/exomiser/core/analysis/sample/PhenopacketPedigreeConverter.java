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
import org.monarchinitiative.exomiser.core.pedigree.Pedigree;
import org.monarchinitiative.exomiser.core.pedigree.Pedigree.Individual;
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
                // PED and the phenopacket schema require a "0" value for missing relations
                .motherId(person.getMaternalId().equals("0") ? "" : person.getMaternalId())
                .fatherId(person.getPaternalId().equals("0") ? "" : person.getPaternalId())
                .sex(toExomiserSex(person.getSex()))
                .status(toExomiserStatus(person.getAffectedStatus()))
                .build();
    }

    private static Individual.Status toExomiserStatus(AffectedStatus affectedStatus) {
        return switch (affectedStatus) {
            case AFFECTED -> Individual.Status.AFFECTED;
            case UNAFFECTED -> Individual.Status.UNAFFECTED;
            default -> Individual.Status.UNKNOWN;
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
                .setFamilyId(individual.familyId())
                .setIndividualId(individual.id())
                // PED and the phenopacket schema require a "0" value for missing relations
                .setMaternalId(individual.motherId().isEmpty() ?  "0" : individual.motherId())
                .setPaternalId(individual.fatherId().isEmpty() ?  "0" : individual.fatherId())
                .setSex(toPhenopacketSex(individual.sex()))
                .setAffectedStatus(toPhenopacketStatus(individual.status()))
                .build();
    }

    private static AffectedStatus toPhenopacketStatus(Individual.Status status) {
        return switch (status) {
            case UNAFFECTED -> AffectedStatus.UNAFFECTED;
            case AFFECTED -> AffectedStatus.AFFECTED;
            default -> AffectedStatus.MISSING;
        };
    }

    public static Sex toPhenopacketSex(Individual.Sex sex) {
        return switch (sex) {
            case FEMALE -> Sex.FEMALE;
            case MALE -> Sex.MALE;
            default -> Sex.UNKNOWN_SEX;
        };
    }

    public static Individual.Sex toExomiserSex(Sex sex) {
        return switch (sex) {
            case FEMALE -> Individual.Sex.FEMALE;
            case MALE -> Individual.Sex.MALE;
            default -> Individual.Sex.UNKNOWN;
        };
    }

}
