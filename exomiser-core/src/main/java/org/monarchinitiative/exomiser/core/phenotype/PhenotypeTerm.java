/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

/**
 * Represents a phenotype term from a phenotype ontology - e.g. the HPO, MPO, ZPO... 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record PhenotypeTerm(String id, String label) implements Comparable<PhenotypeTerm> {

    public PhenotypeTerm(String id, String label) {
        this.id = Objects.requireNonNull(id, "Term id cannot be null");
        this.label = label == null ?  "" : label;
    }

    @JsonCreator
    public static PhenotypeTerm of(String id, String label) {
        return new PhenotypeTerm(id, label);
    }

    @Override
    public int compareTo(PhenotypeTerm o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "PhenotypeTerm{" + "id=" + id + ", label=" + label + '}';
    }

}
