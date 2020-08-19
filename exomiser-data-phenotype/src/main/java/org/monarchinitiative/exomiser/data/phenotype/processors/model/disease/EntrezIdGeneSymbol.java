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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.disease;

import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EntrezIdGeneSymbol implements OutputLine {

    private final int entrezId;
    private final String geneSymbol;
    // TODO this could become more useful and include the HGNC and Ensembl gene ids too.

    public EntrezIdGeneSymbol(int entrezId, String geneSymbol) {
        this.entrezId = entrezId;
        this.geneSymbol = Objects.requireNonNull(geneSymbol);
    }

    @Override
    public String toOutputLine() {
        return entrezId + "|" + geneSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntrezIdGeneSymbol)) return false;
        EntrezIdGeneSymbol that = (EntrezIdGeneSymbol) o;
        return entrezId == that.entrezId &&
                geneSymbol.equals(that.geneSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entrezId, geneSymbol);
    }

    @Override
    public String toString() {
        return "EntrezIdGeneSymbol{" +
                "entrezId=" + entrezId +
                ", geneSymbol='" + geneSymbol + '\'' +
                '}';
    }
}
