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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a phenotype term from a phenotype ontology - e.g. the HPO, MPO, ZPO... 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeTerm {

    private enum Status {
        PRESENT, NOT_PRESENT
    }

    private final String id;
    private final String label;
    private final double ic;
    @JsonIgnore
    private final Status status;

    @Deprecated
    public PhenotypeTerm(String id, String label, double ic) {
        this.id = id;
        this.label = label;
        this.ic = ic;
        this.status = Status.PRESENT;
    }

    private PhenotypeTerm(String id, String label, Status status) {
        this.id = id;
        this.label = label;
        this.ic = 0;
        this.status = status;
    }
    
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @JsonProperty("IC")
    @Deprecated
    public double getIc() {
        return ic;
    }

    @JsonIgnore
    public boolean isPresent() {
        return status == Status.PRESENT;
    }

    @JsonIgnore
    public boolean notPresent() {
        return status == Status.NOT_PRESENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhenotypeTerm)) return false;
        PhenotypeTerm that = (PhenotypeTerm) o;
        return Double.compare(that.ic, ic) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, ic, status);
    }

    @Override
    public String toString() {
        return "PhenotypeTerm{" + "id=" + id + ", label=" + label + ", ic=" + ic + ", present=" + isPresent() +'}';
    }

    public static PhenotypeTerm of(String id, String label) {
        return new PhenotypeTerm(id, label, Status.PRESENT);
    }

    public static PhenotypeTerm notOf(String id, String label) {
        return new PhenotypeTerm(id, label, Status.NOT_PRESENT);
    }

}
