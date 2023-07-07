package org.monarchinitiative.exomiser.core.prioritisers.dao;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class DiseaseTypeCodesTest {

    @Test
    void testDiseaseCode() {
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("D"), equalTo(Disease.DiseaseType.DISEASE));
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("N"), equalTo(Disease.DiseaseType.NON_DISEASE));
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("S"), equalTo(Disease.DiseaseType.SUSCEPTIBILITY));
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("C"), equalTo(Disease.DiseaseType.CNV));
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("?"), equalTo(Disease.DiseaseType.UNCONFIRMED));
        assertThat(DiseaseTypeCodes.parseDiseaseTypeCode("U"), equalTo(Disease.DiseaseType.UNCONFIRMED));
    }

}