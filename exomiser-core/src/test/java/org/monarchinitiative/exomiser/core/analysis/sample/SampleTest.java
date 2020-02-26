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
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.FEMALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SampleTest {

    @Test
    void defaultInstance() {
        Sample instance = Sample.builder().build();
        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.defaultBuild()));
        assertThat(instance.hasVcf(), is(false));
        assertThat(instance.getVcfPath(), equalTo(null));
        assertThat(instance.getPedigree(), equalTo(Pedigree.empty()));
        assertThat(instance.getProbandSampleName(), equalTo("sample"));
        assertThat(instance.getSex(), equalTo(Pedigree.Individual.Sex.UNKNOWN));
        assertThat(instance.getAge(), equalTo(Age.unknown()));
        assertThat(instance.getHpoIds(), equalTo(Collections.emptyList()));
    }

    @Test
    void getGenomeAssembly() {
        Sample instance = Sample.builder()
                .genomeAssembly(GenomeAssembly.HG38)
                .build();
        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    void getVcfPath() {
        Sample instance = Sample.builder()
                .vcfPath(Paths.get(""))
                .build();
        assertThat(instance.getVcfPath(), equalTo(Paths.get("")));
    }

    @Test
    void hasVcf() {
        Sample instance = Sample.builder()
                .vcfPath(Paths.get(""))
                .build();
        assertThat(instance.hasVcf(), is(true));
    }

    @Test
    void getProbandSampleName() {
        Sample instance = Sample.builder()
                .probandSampleName("Bart")
                .build();
        assertThat(instance.getProbandSampleName(), equalTo("Bart"));
    }

    @Test
    void getAge() {
        Sample instance = Sample.builder()
                .age(Age.parse("P2Y3M"))
                .build();
        assertThat(instance.getAge(), equalTo(Age.parse("P2Y3M")));
    }

    @Test
    void getSex() {
        Sample instance = Sample.builder()
                .sex(MALE)
                .build();
        assertThat(instance.getSex(), equalTo(MALE));
    }

    @Test
    void getPedigree() {
        Sample instance = Sample.builder()
                .pedigree(Pedigree.empty())
                .build();
        assertThat(instance.getPedigree(), equalTo(Pedigree.empty()));
    }

    @Test
    void emptyPedigreeDefaultSampleIdentifierSexPresent() {
        Sample instance = Sample.builder()
                .sex(MALE)
                .pedigree(Pedigree.empty())
                .build();
        assertThat(instance.getSex(), equalTo(MALE));
    }

    @Test
    void emptyPedigreeSampleIdentifierSexPresent() {
        Sample instance = Sample.builder()
                .probandSampleName("Bart")
                .sex(MALE)
                .pedigree(Pedigree.empty())
                .build();
        assertThat(instance.getSex(), equalTo(MALE));
    }

    @Test
    void pedigreeMismatchedSampleIdentifier() {
        Sample.Builder instance = Sample.builder()
                .probandSampleName("Bart")
                .pedigree(Pedigree.justProband("Lisa"));
        assertThrows(IllegalArgumentException.class, instance::build, "Proband 'Bart' not present in pedigree");
    }

    @Test
    void pedigreeMismatchedSex() {
        Sample.Builder instance = Sample.builder()
                .probandSampleName("Bart")
                .sex(MALE)
                .pedigree(Pedigree.justProband("Bart"));
        assertThrows(IllegalArgumentException.class, instance::build, "Proband sex stated as MALE does not match pedigree stated sex of UNKNOWN");
    }

    @Test
    void pedigreeDefinedSexReturnedWhenUndefinedInSample() {
        Sample instance = Sample.builder()
                .sex(Pedigree.Individual.Sex.UNKNOWN)
                .probandSampleName("Bart")
                .pedigree(Pedigree.of(Pedigree.Individual.builder().id("Bart").sex(MALE).build()))
                .build();
        assertThat(instance.getSex(), equalTo(MALE));
    }

    @Test
    void pedigreeDefinedSexDisagreesWithSample() {
        Sample.Builder instance = Sample.builder()
                .sex(FEMALE)
                .probandSampleName("Lisa")
                .pedigree(Pedigree.of(Pedigree.Individual.builder().id("Lisa").sex(MALE).build()));
        assertThrows(IllegalArgumentException.class, instance::build, "Proband sex stated as MALE does not match pedigree stated sex of UNKNOWN");
    }

    @Test
    void pedigreeDefinedSexAgreesWithSample() {
        Sample instance = Sample.builder()
                .sex(FEMALE)
                .probandSampleName("Lisa")
                .pedigree(Pedigree.of(Pedigree.Individual.builder().id("Lisa").sex(FEMALE).build()))
                .build();
        assertThat(instance.getSex(), equalTo(FEMALE));
    }

    @Test
    void getHpoIds() {
        Sample instance = Sample.builder()
                .hpoIds(List.of("HP:0000001", "HP:0000002"))
                .build();
        assertThat(instance.getHpoIds(), equalTo(List.of("HP:0000001", "HP:0000002")));
    }
}