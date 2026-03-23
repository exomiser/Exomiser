package org.monarchinitiative.exomiser.core.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DiseaseIdentifiersTest {

    @ParameterizedTest
    @CsvSource({
            "OMIM:101600, https://omim.org/entry/101600",
            "ORPHA:93259, https://www.orpha.net/en/disease/detail/93259",
            "G2P01586, https://www.ebi.ac.uk/gene2phenotype/lgd/G2P01586",
            "MONDO:0007043, https://monarchinitiative.org/MONDO:0007043",
            "MEDGEN:67390, MEDGEN:67390",
    })
    void url(String diseaseId, String expectedUrl) {
        assertThat(DiseaseIdentifiers.toURLString(diseaseId), equalTo(expectedUrl));
    }
}