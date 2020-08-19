/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.gene;

import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class GeneOrtholog implements OutputLine {

    private final String orthologGeneId;
    private final String orthologGeneSymbol;
    private final String humanGeneSymbol;
    private final int entrezGeneId;

    public GeneOrtholog(String orthologGeneId, String orthologGeneSymbol, String humanGeneSymbol, int entrezGeneId) {
        this.orthologGeneId = orthologGeneId;
        this.orthologGeneSymbol = orthologGeneSymbol;
        this.humanGeneSymbol = humanGeneSymbol;
        this.entrezGeneId = entrezGeneId;
    }

    public String getOrthologGeneId() {
        return orthologGeneId;
    }

    public String getOrthologGeneSymbol() {
        return orthologGeneSymbol;
    }

    public String getHumanGeneSymbol() {
        return humanGeneSymbol;
    }

    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    @Override
    public String toOutputLine() {
        return orthologGeneId +"|" + orthologGeneSymbol + "|" + humanGeneSymbol + "|" + entrezGeneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneOrtholog)) return false;
        GeneOrtholog that = (GeneOrtholog) o;
        return entrezGeneId == that.entrezGeneId &&
                orthologGeneId.equals(that.orthologGeneId) &&
                orthologGeneSymbol.equals(that.orthologGeneSymbol) &&
                humanGeneSymbol.equals(that.humanGeneSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orthologGeneId, orthologGeneSymbol, humanGeneSymbol, entrezGeneId);
    }

    @Override
    public String toString() {
        return "GeneOrtholog{" +
                "orthologGeneId='" + orthologGeneId + '\'' +
                ", orthologGeneSymbol='" + orthologGeneSymbol + '\'' +
                ", humanGeneSymbol='" + humanGeneSymbol + '\'' +
                ", entrezGeneId=" + entrezGeneId +
                '}';
    }
}
