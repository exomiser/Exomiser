package org.monarchinitiative.exomiser.core.model;

/**
 * Utility class for handling disease identifiers.
 *
 * @author  <j.jacobsen@qmul.ac.uk>
 */
public class DiseaseIdentifiers {
    private DiseaseIdentifiers() {
    }

    /**
     * Constructs a URL based on the provided disease identifier.
     * The method determines the appropriate URL prefix based on
     * the first three characters of the identifier and generates
     * the full URL accordingly.
     *
     * @param id the disease identifier, expected to start with a
     *           predefined prefix such as "OMI", "ORP", "MON", or "G2P",
     *           followed by a colon and a unique identifier.
     * @return the constructed URL if the prefix matches one of the
     *         predefined types, or the original identifier if no match is found.
     */
    public static String toURLString(String id) {
        return switch (id.substring(0, 3)) {
            case "OMI" -> "https://omim.org/entry/" + id.split(":")[1];
            case "ORP" -> "https://www.orpha.net/en/disease/detail/" + id.split(":")[1];
            case "MON" -> "https://monarchinitiative.org/" + id;
            case "G2P" -> "https://www.ebi.ac.uk/gene2phenotype/lgd/" + id;
            default -> id;
        };
    }
}
