package org.monarchinitiative.exomiser.core.prioritisers.dao;

import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

/**
 * Utility class to translate the short disease codes used in the database to and from their domain objects.
 */
public final class DiseaseTypeCodes {

    private DiseaseTypeCodes() {
        // static utility class
    }

    public static Disease.DiseaseType parseDiseaseTypeCode(String diseaseTypeCode) {
        return switch (diseaseTypeCode) {
            case "D" -> Disease.DiseaseType.DISEASE;
            case "N" -> Disease.DiseaseType.NON_DISEASE;
            case "S" -> Disease.DiseaseType.SUSCEPTIBILITY;
            case "C" -> Disease.DiseaseType.CNV;
            case "?" -> Disease.DiseaseType.UNCONFIRMED;
            default -> Disease.DiseaseType.UNCONFIRMED;
        };
    }

    public static String toDiseaseTypeCode(Disease.DiseaseType diseaseType) {
        return switch (diseaseType) {
            case DISEASE -> "D";
            case NON_DISEASE -> "N";
            case SUSCEPTIBILITY -> "S";
            case CNV -> "C";
            case UNCONFIRMED -> "?";
        };
    }
}
