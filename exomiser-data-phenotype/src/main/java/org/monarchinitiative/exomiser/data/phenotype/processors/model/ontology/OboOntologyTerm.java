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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Very simple ontology class to represent the ontology term data found in an obo ontology which we require directly in
 * the Exomiser. This is *not* an even remotely complete representation of that data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OboOntologyTerm implements OutputLine {

    private final String id;
    private final String label;
    private final boolean obsolete;
    private final List<String> altIds;
    private final String replacedBy;

    private OboOntologyTerm(Builder builder) {
        Objects.requireNonNull(builder.id);
        this.id = builder.id;
        this.label = builder.label == null ? "" : builder.label;
        this.obsolete = builder.obsolete;
        this.altIds = builder.altIds == null ? ImmutableList.of() : ImmutableList.copyOf(builder.altIds);
        this.replacedBy = builder.replacedBy == null ? "" : builder.replacedBy;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public List<String> getAltIds() {
        return altIds;
    }

    public String getReplacedBy() {
        return replacedBy;
    }

    @Override
    public String toOutputLine() {
        return id + "|" + label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OboOntologyTerm that = (OboOntologyTerm) o;
        return obsolete == that.obsolete &&
                Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(altIds, that.altIds) &&
                Objects.equals(replacedBy, that.replacedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, obsolete, altIds, replacedBy);
    }

    @Override
    public String toString() {
        return "OboOntologyTerm{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", obsolete=" + obsolete +
                ", altIds=" + altIds +
                ", replacedBy='" + replacedBy + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id = null;
        private String label = "";
        private boolean obsolete = false;
        private List<String> altIds = new ArrayList<>();
        private String replacedBy = "";

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder obsolete(boolean obsolete) {
            this.obsolete = obsolete;
            return this;
        }

        public Builder altIds(List<String> altIds) {
            this.altIds = altIds;
            return this;
        }

        public Builder addAltId(String altId) {
            if (altId != null && !altId.isEmpty()) {
                this.altIds.add(altId);
            }
            return this;
        }

        public Builder replacedBy(String replacedBy) {
            this.replacedBy = replacedBy;
            return this;
        }

        public OboOntologyTerm build() {
            return new OboOntologyTerm(this);
        }
    }
}
