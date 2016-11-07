/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

/**
 * Filter Variants on the basis of Uberpheno semantic similarity measure between
 * the HPO clinical phenotypes associated with the disease being sequenced and
 * MP annotated MGI mouse models and/or Zebrafish phenotypes.
 *
 * @author Sebastian Koehler
 * @version 0.02 (April 2, 2013).
 */
public class UberphenoPriorityResult extends AbstractPriorityResult {

    private static final PriorityType priorityType = PriorityType.UBERPHENO_PRIORITY;

    /**
     * @param uberphenoSemSimScore
     */
    public UberphenoPriorityResult(int geneId, String geneSymbol, double uberphenoSemSimScore) {
        super(priorityType, geneId, geneSymbol, uberphenoSemSimScore);
    }

    /* (non-Javadoc)
     * @see exomizer.filter.Triage#getHTMLCode()
     */
    @Override
    public String getHTMLCode() {
        return "";
    }

}
