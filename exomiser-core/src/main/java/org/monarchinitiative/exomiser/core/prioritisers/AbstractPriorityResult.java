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

import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
abstract class AbstractPriorityResult implements PriorityResult {

    private final PriorityType priorityType;

    final int geneId;
    final String geneSymbol;
    final double score;

    AbstractPriorityResult(PriorityType priorityType, int geneId, String geneSymbol, double score) {
        this.priorityType = priorityType;
        this.geneId = geneId;
        this.geneSymbol = geneSymbol;
        this.score = score;
    }

    @Override
    public PriorityType getPriorityType() {
        return priorityType;
    }

    @Override
    public int getGeneId() {
        return geneId;
    }

    @Override
    public String getGeneSymbol() {
        return geneSymbol;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPriorityResult)) return false;
        AbstractPriorityResult that = (AbstractPriorityResult) o;
        return geneId == that.geneId &&
                Double.compare(that.score, score) == 0 &&
                priorityType == that.priorityType &&
                Objects.equals(geneSymbol, that.geneSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priorityType, geneId, geneSymbol, score);
    }

    @Override
    public String toString() {
        return "AbstractPriorityResult{" +
                "priorityType=" + priorityType +
                ", geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                '}';
    }
}
