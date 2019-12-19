/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Pedigree;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeConverterTest {

    private static final org.monarchinitiative.exomiser.core.model.Pedigree.Individual ADAM = org.monarchinitiative.exomiser.core.model.Pedigree.Individual
            .builder()
            .familyId("1")
            .id("Adam")
            .sex(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE)
            .status(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.UNAFFECTED)
            .build();

    private static final org.monarchinitiative.exomiser.core.model.Pedigree.Individual EVA = org.monarchinitiative.exomiser.core.model.Pedigree.Individual
            .builder()
            .familyId("1")
            .id("Eva")
            .sex(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.FEMALE)
            .status(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.UNAFFECTED)
            .build();

    private static final org.monarchinitiative.exomiser.core.model.Pedigree.Individual SETH = org.monarchinitiative.exomiser.core.model.Pedigree.Individual
            .builder()
            .familyId("1")
            .id("Seth")
            .sex(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE)
            .fatherId("Adam")
            .motherId("Eva")
            .status(org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status.AFFECTED)
            .build();

    private static final org.monarchinitiative.exomiser.core.model.Pedigree.Individual NEMO = org.monarchinitiative.exomiser.core.model.Pedigree.Individual
            .builder()
            .familyId("1")
            .id("Nemo")
            .fatherId("Adam")
            .motherId("Eva")
            .build();

    private void assertContainsPerson(Person expected, de.charite.compbio.jannovar.pedigree.Pedigree pedigree) {
        String name = expected.getName();
        assertThat(pedigree.hasPerson(name), is(true));
        Person person = pedigree.getNameToMember().get(name).getPerson();
        assertThat(person, equalTo(expected));
    }

    @Test
    public void toJannovar() {
        Pedigree original = Pedigree.of(ADAM, EVA, SETH, NEMO);

        de.charite.compbio.jannovar.pedigree.Pedigree converted = PedigreeConverter.convertToJannovarPedigree(original);
        Person jannovarAdam = new Person("Adam", null, null, Sex.MALE, Disease.UNAFFECTED);
        Person jannovarEva = new Person("Eva", null, null, Sex.FEMALE, Disease.UNAFFECTED);
        Person jannovarSeth = new Person("Seth", jannovarAdam, jannovarEva, Sex.MALE, Disease.AFFECTED);
        Person jannovarNemo = new Person("Nemo", jannovarAdam, jannovarEva, Sex.UNKNOWN, Disease.UNKNOWN);

        assertThat(converted.getNMembers(), equalTo(original.size()));
        assertContainsPerson(jannovarAdam, converted);
        assertContainsPerson(jannovarEva, converted);
        assertContainsPerson(jannovarSeth, converted);
        assertContainsPerson(jannovarNemo, converted);
    }

    @Test
    public void toJannovarThrowsException() {

        Pedigree.Individual illegal = Pedigree.Individual.builder().id("illegal").fatherId("not_present").build();

        assertThrows(IllegalArgumentException.class, () -> Pedigree.of(ADAM, EVA, SETH, illegal));
    }
}