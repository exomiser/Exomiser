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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantAlleleTest {

    @Test
    void throwsExceptionWhenUsingSymbolicInNonSymbolicConstructor() {
        assertThrows(IllegalArgumentException.class, () -> VariantAllele.of("1", 1, "A", "<INS>"));
    }

    @Test
    void trimsAndAssignsVariantTypeWhenNonSymbolicSuppliedInFullConstructor() {
        VariantAllele result = VariantAllele.of("1", 1, 1, "AA", "AT", 0, VariantType.MNV, "1", ConfidenceInterval.precise(), ConfidenceInterval
                .precise());
        assertThat(result.getStart(), equalTo(2));
        assertThat(result.getRef(), equalTo("A"));
        assertThat(result.getAlt(), equalTo("T"));
        assertThat(result.getStart(), equalTo(2));
        assertThat(result.getEnd(), equalTo(2));
        assertThat(result.getLength(), equalTo(1));
        assertThat(result.getVariantType(), equalTo(VariantType.SNV));
    }

    @Test
    void buildNonSymbolicUntrimmedReturnsTrimmed() {
        VariantAllele result = VariantAllele.of("1", 1, "AA", "AT");
        System.out.println(result);
        assertThat(result.getStart(), equalTo(2));
        assertThat(result.getRef(), equalTo("A"));
        assertThat(result.getAlt(), equalTo("T"));
        assertThat(result.getStart(), equalTo(2));
        assertThat(result.getEnd(), equalTo(2));
        assertThat(result.getLength(), equalTo(1));
        assertThat(result.getVariantType(), equalTo(VariantType.SNV));
    }

    @Test
    void canFullySpecifySnv() {
        VariantAllele instance = VariantAllele.of("1", 1, 1, "A", "T", 0, VariantType.SNV, "1", ConfidenceInterval.precise(), ConfidenceInterval
                .precise());
        assertThat(instance.getStartContigName(), equalTo("1"));
        assertThat(instance.getStartContigId(), equalTo(1));
        assertThat(instance.getStart(), equalTo(1));
        assertThat(instance.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getEndContigId(), equalTo(1));
        assertThat(instance.getEndContigName(), equalTo("1"));
        assertThat(instance.getEnd(), equalTo(1));
        assertThat(instance.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getLength(), equalTo(1));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
        assertThat(instance.getVariantType(), equalTo(VariantType.SNV));
        assertThat(instance.isSymbolic(), equalTo(false));
    }

    @Test
    void canFullySpecifyNonSymbolicDel() {
        VariantAllele instance = VariantAllele.of("1", 1, 1, "CGTGGATGCGGGGAC", "C", -14, VariantType.DEL, "1", ConfidenceInterval
                .precise(), ConfidenceInterval
                .precise());
        assertThat(instance.getStartContigName(), equalTo("1"));
        assertThat(instance.getStartContigId(), equalTo(1));
        assertThat(instance.getStart(), equalTo(1));
        assertThat(instance.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getEndContigId(), equalTo(1));
        assertThat(instance.getEndContigName(), equalTo("1"));
        assertThat(instance.getEnd(), equalTo(15));
        assertThat(instance.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getLength(), equalTo(-14));
        assertThat(instance.getRef(), equalTo("CGTGGATGCGGGGAC"));
        assertThat(instance.getAlt(), equalTo("C"));
        assertThat(instance.getVariantType(), equalTo(VariantType.DEL));
        assertThat(instance.isSymbolic(), equalTo(false));
    }


    @Disabled
    @Test
    void canFullySpecifyNonSymbolicInv() {
        VariantAllele instance = VariantAllele.of("1", 1, 1, "ATGC", "GCAT", 4, VariantType.INV, "1", ConfidenceInterval
                .precise(), ConfidenceInterval
                .precise());
        assertThat(instance.getStartContigName(), equalTo("1"));
        assertThat(instance.getStartContigId(), equalTo(1));
        assertThat(instance.getStart(), equalTo(1));
        assertThat(instance.getStartCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getEndContigId(), equalTo(1));
        assertThat(instance.getEndContigName(), equalTo("1"));
        assertThat(instance.getEnd(), equalTo(4));
        assertThat(instance.getEndCi(), equalTo(ConfidenceInterval.precise()));
        assertThat(instance.getLength(), equalTo(4));
        assertThat(instance.getRef(), equalTo("ATGC"));
        assertThat(instance.getAlt(), equalTo("GCAT"));
        assertThat(instance.getVariantType(), equalTo(VariantType.INV));
        assertThat(instance.isSymbolic(), equalTo(false));
    }
}