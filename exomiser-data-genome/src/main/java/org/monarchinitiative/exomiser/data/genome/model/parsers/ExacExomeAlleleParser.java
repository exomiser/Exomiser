package org.monarchinitiative.exomiser.data.genome.model.parsers;

import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExacExomeAlleleParser extends ExacAlleleParser {
    public ExacExomeAlleleParser() {
        super(ExacPopulationKey.EXAC_EXOMES, Set.of(".", "PASS"));
    }
}
