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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import java.util.Collection;
import java.util.Objects;

/**
 * Temporary data class for parsing common data out of MGI and IMPC gene-phenotype files. Convert to record type in the
 * future.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class GenePhenotype {

    private final String id;
    private final String geneId;
    private final Collection<String> phenotypeIds;

    public GenePhenotype(String id, String geneId, Collection<String> phenotypeIds) {
        this.id = id;
        this.geneId = geneId;
        this.phenotypeIds = phenotypeIds;
    }

    public String getId() {
        return id;
    }

    public String getGeneId() {
        return geneId;
    }

    public Collection<String> getPhenotypeIds() {
        return phenotypeIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenePhenotype)) return false;
        GenePhenotype that = (GenePhenotype) o;
        return id.equals(that.id) &&
                geneId.equals(that.geneId) &&
                phenotypeIds.equals(that.phenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, geneId, phenotypeIds);
    }

    @Override
    public String toString() {
        return "MouseGeneModel{" +
                "id='" + id + '\'' +
                ", geneId='" + geneId + '\'' +
                ", phenotypeIds=" + phenotypeIds +
                '}';
    }
}
