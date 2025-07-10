/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.Function;

/**
 * Super-simple completely bare-bones pedigree implementation based on the PED file format.
 * <p>
 * *IMPORTANT* this class does absolutely no validation on the input data. That functionality is left to the
 * implementation of the pedigree analysis tool.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Pedigree {

    private static final Pedigree EMPTY = new Pedigree(Collections.emptyList());

    private final Set<Individual> individuals;

    @JsonIgnore
    private final Map<String, Individual> individualsById;

    private Pedigree(Collection<Individual> individuals) {
        Objects.requireNonNull(individuals);
        this.individuals = ImmutableSet.copyOf(individuals);
        this.individualsById = this.individuals.stream().collect(ImmutableMap.toImmutableMap(Individual::id, Function.identity()));
        validate();
    }

    private void validate() {
        if (individualsById.size() != individuals.size()) {
            //this should already be caught by the ImmutableMap constructor
            throw new IllegalArgumentException("Duplicate individual");
        }
        for (Individual individual : individuals) {
            if (!individual.motherId.isEmpty()) {
                checkParent(individual, individual.motherId, Individual.Sex.FEMALE);
            }
            if (!individual.fatherId.isEmpty()) {
                checkParent(individual, individual.fatherId, Individual.Sex.MALE);
            }
        }
    }

    private void checkParent(Individual individual, String parentId, Individual.Sex sex) {
        if (!parentId.isEmpty()) {
            if (!individualsById.containsKey(parentId)) {
                throw new IllegalArgumentException(String.format("Parent '%s' not found for individual '%s'", parentId, individual.id));
            }
            Individual parent = individualsById.get(parentId);
            if (parent.sex() != sex) {
                throw new IllegalArgumentException(String.format("Individual '%s' listed as %s parent of '%s', but should be %s", parentId, parent.sex(), individual.id, sex));
            }
        }
    }

    public static Pedigree of(Collection<Individual> individuals) {
        return new Pedigree(individuals);
    }

    public static Pedigree of(Individual... individuals) {
        return new Pedigree(Arrays.asList(individuals));
    }

    public static Pedigree justProband(String id) {
        Individual individual = Individual.builder().id(id).status(Individual.Status.AFFECTED).build();
        return Pedigree.of(individual);
    }

    public static Pedigree justProband(String id, Individual.Sex sex) {
        Individual individual = Individual.builder()
                .id(id)
                .status(Individual.Status.AFFECTED)
                .sex(sex)
                .build();
        return Pedigree.of(individual);
    }

    public static Pedigree empty() {
        return EMPTY;
    }

    @JsonIgnore
    public boolean containsId(String id) {
        return individualsById.containsKey(id);
    }

    @JsonIgnore
    public Individual getIndividualById(String individualId) {
        return individualsById.get(individualId);
    }

    public Set<Individual> getIndividuals() {
        return individuals;
    }

    @JsonIgnore
    public Set<String> getIdentifiers() {
        return individualsById.keySet();
    }

    @JsonIgnore
    public int size() {
        return individuals.size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return individuals.isEmpty();
    }

    @JsonIgnore
    public int numFamilies() {
        return (int) individuals.stream().map(Individual::familyId).distinct().count();
    }

    public List<Pedigree.Individual> ancestorsOf(Pedigree.Individual individual) {
        if (individual == null) {
            return List.of();
        }
        String motherId = individual.motherId();
        String fatherId = individual.fatherId();
        if (motherId.isEmpty() && fatherId.isEmpty()) {
            return List.of();
        }
        ArrayList<Pedigree.Individual> ancestors = new ArrayList<>();
        Individual mother = getIndividualById(motherId);
        if (mother != null) {
            ancestors.add(mother);
            ancestors.addAll(ancestorsOf(mother));
        }
        Individual father = getIndividualById(fatherId);
        if (father != null) {
            ancestors.add(father);
            ancestors.addAll(ancestorsOf(father));
        }
        return List.copyOf(ancestors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedigree pedigree = (Pedigree) o;
        return Objects.equals(individuals, pedigree.individuals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(individuals);
    }

    @Override
    public String toString() {
        return "Pedigree{" +
               "individuals=" + individuals +
               '}';
    }

    public record Individual(
            String familyId,
            String id,
            String motherId,
            String fatherId,
            Sex sex,
            Status status
    ) {

        public enum Sex {
            UNKNOWN,
            MALE,
            FEMALE
        }

        public enum Status {
            UNKNOWN,
            UNAFFECTED,
            AFFECTED
        }

        public Individual {
            Objects.requireNonNull(familyId);
            Objects.requireNonNull(id);
            Objects.requireNonNull(motherId);
            Objects.requireNonNull(fatherId);
            Objects.requireNonNull(sex);
            Objects.requireNonNull(status);
        }

        @JsonIgnore
        public boolean isAffected() {
            return status == Status.AFFECTED;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String familyId = "";
            private String id = "";
            private String motherId = "";
            private String fatherId = "";
            private Sex sex = Sex.UNKNOWN;
            private Status status = Status.UNKNOWN;

            public Builder familyId(String familyId) {
                Objects.requireNonNull(familyId);
                this.familyId = familyId;
                return this;
            }

            public Builder id(String id) {
                if (id == null || id.isEmpty()) {
                    throw new IllegalArgumentException("Individual cannot have a null or empty identifier");
                }
                this.id = id;
                return this;
            }

            public Builder motherId(String motherId) {
                this.motherId = Objects.requireNonNull(motherId);
                return this;
            }

            public Builder fatherId(String fatherId) {
                this.fatherId = Objects.requireNonNull(fatherId);
                return this;
            }

            public Builder sex(Sex sex) {
                this.sex = Objects.requireNonNull(sex);
                return this;
            }

            public Builder status(Status status) {
                this.status = Objects.requireNonNull(status);
                return this;
            }

            public Individual build() {
                return new Individual(
                        familyId,
                        id,
                        motherId,
                        fatherId,
                        sex,
                        status
                );
            }
        }

        @Override
        public String toString() {
            return "Individual{" +
                   "familyId='" + familyId + '\'' +
                   ", id='" + id + '\'' +
                   ", motherId='" + motherId + '\'' +
                   ", fatherId='" + fatherId + '\'' +
                   ", sex=" + sex +
                   ", affectedStatus=" + status +
                   '}';
        }
    }
}
