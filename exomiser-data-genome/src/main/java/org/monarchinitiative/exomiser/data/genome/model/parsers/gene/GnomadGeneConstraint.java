/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers.gene;

import org.monarchinitiative.exomiser.data.genome.model.OutputLine;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GnomadGeneConstraint implements OutputLine, Comparable<GnomadGeneConstraint> {

    private final String geneSymbol;
    private final String geneId;
    private final String geneTranscript;
    private final float pLI;
    private final float oeLofLower;
    private final float oeLofUpper;

    public GnomadGeneConstraint(String geneSymbol, String geneId, String geneTranscript, float pLI, float oeLofLower, float oeLofUpper) {
        this.geneSymbol = geneSymbol;
        this.geneId = geneId;
        this.geneTranscript = geneTranscript;
        this.pLI = pLI;
        this.oeLofLower = oeLofLower;
        this.oeLofUpper = oeLofUpper;
    }

    @Override
    public String toOutputLine() {
        return geneSymbol + SEP + geneId + SEP + geneTranscript + SEP + pLI + SEP + oeLofLower + SEP + oeLofUpper;
    }

    @Override
    public int compareTo(GnomadGeneConstraint o) {
        return this.geneSymbol.compareTo(o.geneSymbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GnomadGeneConstraint)) return false;
        GnomadGeneConstraint that = (GnomadGeneConstraint) o;
        return Float.compare(that.pLI, pLI) == 0 &&
                Float.compare(that.oeLofLower, oeLofLower) == 0 &&
                Float.compare(that.oeLofUpper, oeLofUpper) == 0 &&
                geneSymbol.equals(that.geneSymbol) &&
                geneId.equals(that.geneId) &&
                geneTranscript.equals(that.geneTranscript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geneSymbol, geneId, geneTranscript, pLI, oeLofLower, oeLofUpper);
    }

    @Override
    public String toString() {
        return "GnomadGeneConstraint{" +
                "geneSymbol='" + geneSymbol + '\'' +
                ", geneId='" + geneId + '\'' +
                ", geneTranscript='" + geneTranscript + '\'' +
                ", pLI=" + pLI +
                ", eoLofLower=" + oeLofLower +
                ", eoLofUpper=" + oeLofUpper +
                '}';
    }
}
