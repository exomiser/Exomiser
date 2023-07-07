package org.monarchinitiative.exomiser.core.prioritisers.dao;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import static org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode.*;

/**
 * Utility class to translate the short inheritance codes used in the database to and from their domain objects.
 */
public final class InheritanceModeCodes {
    private InheritanceModeCodes() {
        // static utility class
    }

    public static InheritanceMode parseInheritanceModeCode(String inheritanceCode) {
        return inheritanceCode == null ? UNKNOWN : switch (inheritanceCode) {
            case "U" -> UNKNOWN;
            case "D" -> AUTOSOMAL_DOMINANT;
            case "R" -> AUTOSOMAL_RECESSIVE;
            case "B" -> SEMIDOMINANT;
            case "X" -> X_LINKED;
            case "XD" -> X_DOMINANT;
            case "XR" -> X_RECESSIVE;
            case "Y" -> Y_LINKED;
            case "M" -> MITOCHONDRIAL;
            case "S" -> SOMATIC;
            case "P" -> POLYGENIC;
            default -> UNKNOWN;
        };
    }

    public static String toInheritanceModeCode(InheritanceMode inheritanceMode) {
        return switch (inheritanceMode) {
            case UNKNOWN -> "U";
            case AUTOSOMAL_DOMINANT -> "D";
            case AUTOSOMAL_RECESSIVE -> "R";
            case SEMIDOMINANT -> "B";
            case X_LINKED -> "X";
            case X_DOMINANT -> "XD";
            case X_RECESSIVE -> "XR";
            case Y_LINKED -> "Y";
            case MITOCHONDRIAL -> "M";
            case SOMATIC -> "S";
            case POLYGENIC -> "P";
        };
    }
}
