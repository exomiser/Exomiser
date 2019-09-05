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

import org.monarchinitiative.exomiser.core.model.Pedigree;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static test pedigree data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestPedigrees {

    private static final Pedigree.Individual EVA = Pedigree.Individual.builder()
            .familyId("1")
            .id("Eva")
            .sex(Pedigree.Individual.Sex.FEMALE)
            .status(Pedigree.Individual.Status.UNAFFECTED)
            .build();
    private static final Pedigree.Individual ADAM = Pedigree.Individual.builder()
            .familyId("1")
            .id("Adam")
            .sex(Pedigree.Individual.Sex.MALE)
            .status(Pedigree.Individual.Status.UNAFFECTED)
            .build();

    private static final Pedigree.Individual ADAM_AFFECTED = Pedigree.Individual.builder()
            .familyId("1")
            .id("Adam")
            .sex(Pedigree.Individual.Sex.MALE)
            .status(Pedigree.Individual.Status.AFFECTED)
            .build();

    private static final Pedigree.Individual SETH = Pedigree.Individual.builder()
            .familyId("1")
            .id("Seth")
            .motherId("Eva")
            .fatherId("Adam")
            .sex(Pedigree.Individual.Sex.MALE)
            .status(Pedigree.Individual.Status.AFFECTED)
            .build();

    //1	Eva	0	0	2	1
    //1	Adam	0	0	1	1
    //1	Seth	Adam	Eva	1	2
    private static final Pedigree TRIO_CHILD_AFFECTED = Pedigree.of(EVA, ADAM, SETH);
    private static final Path TRIO_CHILD_AFFECTED_PATH  = Paths.get("src/test/resources/inheritance/childAffected.ped");

    private static final Pedigree TRIO_CHILD_AND_FATHER_AFFECTED = Pedigree.of(EVA, ADAM_AFFECTED, SETH);
    private static final Path TRIO_CHILD_AND_FATHER_AFFECTED_PATH = Paths.get("src/test/resources/inheritance/twoAffected.ped");

    private static final Path TRIO_VCF_PATH = Paths.get("src/test/resources/inheritance/inheritanceFilterTest.vcf");

    private TestPedigrees() {
    }

    public static Path trioVcfPath() {
        return TRIO_VCF_PATH;
    }

    public static Pedigree.Individual affectedChild() {
        return SETH;
    }

    public static Pedigree trioChildAffected() {
        return TRIO_CHILD_AFFECTED;
    }

    public static Path trioWithChildAffectedPedPath() {
        return TRIO_CHILD_AFFECTED_PATH;
    }

    public static Pedigree trioChildAndFatherAffected() {
        return TRIO_CHILD_AND_FATHER_AFFECTED;
    }

    public static Path trioChildAndFatherAffectedPedPath() {
        return TRIO_CHILD_AND_FATHER_AFFECTED_PATH;
    }

}
