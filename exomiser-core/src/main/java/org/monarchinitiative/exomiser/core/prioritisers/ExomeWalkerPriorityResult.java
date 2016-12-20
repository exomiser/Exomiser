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
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @version 0.06 (6 January, 2014).
 */
public class ExomeWalkerPriorityResult extends AbstractPriorityResult {

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public ExomeWalkerPriorityResult(int geneId, String geneSymbol, double score) {
        super(PriorityType.EXOMEWALKER_PRIORITY, geneId, geneSymbol, score);
    }

    /**
     * @return An HTML list with an entry representing the GeneWanderer (Random
     * walk) similarity score.
     * 
     */
    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>Random walk similarity score: %.3f</dt></dl>", this.score);
    }

}
