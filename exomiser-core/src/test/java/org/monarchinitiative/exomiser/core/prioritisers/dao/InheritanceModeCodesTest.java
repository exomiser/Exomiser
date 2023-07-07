package org.monarchinitiative.exomiser.core.prioritisers.dao;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class InheritanceModeCodesTest {

    /**
     * Test of getInheritanceCode method, of class InheritanceMode.
     */
    @Test
    void testGetInheritanceCode() {
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.AUTOSOMAL_DOMINANT), equalTo("D"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.AUTOSOMAL_RECESSIVE), equalTo("R"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.SEMIDOMINANT), equalTo("B"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.X_LINKED), equalTo("X"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.X_DOMINANT), equalTo("XD"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.X_RECESSIVE), equalTo("XR"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.Y_LINKED), equalTo("Y"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.MITOCHONDRIAL), equalTo("M"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.SOMATIC), equalTo("S"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.POLYGENIC), equalTo("P"));
        assertThat(InheritanceModeCodes.toInheritanceModeCode(InheritanceMode.UNKNOWN), equalTo("U"));
    }

    /**
     * Test of valueOfInheritanceCode method, of class InheritanceMode.
     */
    @Test
    void testValueOfInheritanceCode() {
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("D"), equalTo(InheritanceMode.AUTOSOMAL_DOMINANT));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("R"), equalTo(InheritanceMode.AUTOSOMAL_RECESSIVE));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("B"), equalTo(InheritanceMode.SEMIDOMINANT));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("X"), equalTo(InheritanceMode.X_LINKED));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("XD"), equalTo(InheritanceMode.X_DOMINANT));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("XR"), equalTo(InheritanceMode.X_RECESSIVE));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("Y"), equalTo(InheritanceMode.Y_LINKED));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("M"), equalTo(InheritanceMode.MITOCHONDRIAL));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("S"), equalTo(InheritanceMode.SOMATIC));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("P"), equalTo(InheritanceMode.POLYGENIC));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("U"), equalTo(InheritanceMode.UNKNOWN));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode("unrecognised code"), equalTo(InheritanceMode.UNKNOWN));
        assertThat(InheritanceModeCodes.parseInheritanceModeCode(null), equalTo(InheritanceMode.UNKNOWN));
    }
}