package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter Variants on the basis of Uberpheno semantic similarity measure between
 * the HPO clinical phenotypes associated with the disease being sequenced and
 * MP annotated MGI mouse models and/or Zebrafish phenotypes.
 *
 * @author Sebastian Koehler
 * @version 0.02 (April 2, 2013).
 */
public class UberphenoPriorityResult implements PriorityResult {

    private double uberphenoScore;

    /**
     * @param uberphenoSemSimScore
     */
    public UberphenoPriorityResult(double uberphenoSemSimScore) {
        this.uberphenoScore = uberphenoSemSimScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.UBERPHENO_PRIORITY;
    }

    /* (non-Javadoc)
     * @see exomizer.priority.Priority#getScore
     */
    @Override
    public float getScore() {
        return (float) uberphenoScore;
    }

    /* (non-Javadoc)
     * @see exomizer.filter.Triage#getHTMLCode()
     */
    @Override
    public String getHTMLCode() {
        return "";
    }

}
