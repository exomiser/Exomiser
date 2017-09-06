/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchScore {

    private final String metric;
    private final int score;
    private final int rank;

    public PhenoGridMatchScore(String metric, int score, int rank) {
        this.metric = metric;
        this.score = score;
        this.rank = rank;
    }

    public String getMetric() {
        return metric;
    }

    public int getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.metric);
        hash = 89 * hash + this.score;
        hash = 89 * hash + this.rank;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhenoGridMatchScore other = (PhenoGridMatchScore) obj;
        if (!Objects.equals(this.metric, other.metric)) {
            return false;
        }
        if (this.score != other.score) {
            return false;
        }
        return this.rank == other.rank;
    }


    @Override
    public String toString() {
        return "PhenoGridMatchScore{" + "metric=" + metric + ", score=" + score + ", rank=" + rank + '}';
    }

}
