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

import org.monarchinitiative.exomiser.core.model.Gene;

/**
 * Prioritization of Genes results in a relevance score for each tested
 * {@link Gene Gene} object. The methods may also annotate the
 genes with data (e.g., a link to OMIM or a link to Phenodigm or uberpheno
 data. Each prioritization is expected to result on an object of a class that
 implements PriorityResult
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PriorityResult extends Comparable<PriorityResult> {

    /**
     * @return the gene id for which this result is associated.
     */
    int getGeneId();

    /**
     * @return the gene symbol for which this result is associated.
     */
    String getGeneSymbol();

    /**
     * @return return a float representation of the prioritiser result with values between 0..1 where zero means no match
     * and one is the best match.
     */
    double getScore();
    
    //TODO: is a PriorityType strictly necessary? Investigate...
    PriorityType getPriorityType();

    /**
     * @return HTML code representing this prioritization/relevance score
     * @deprecated this should be handled by the writers
     */
    @Deprecated
    default String getHTMLCode() {
        return "";
    }

    /**
     * PriorityResults are sorted according to descending numerical value of the score (in other words higher is better)
     * and if equal, by natural ordering of the gene symbol.
     * @param o
     * @return
     */
    @Override
    default int compareTo(PriorityResult o) {
        int scoreComparison = Double.compare(this.getScore(), o.getScore());
        return scoreComparison == 0 ? this.getGeneSymbol().compareTo(o.getGeneSymbol()) : - scoreComparison;
    }

}
