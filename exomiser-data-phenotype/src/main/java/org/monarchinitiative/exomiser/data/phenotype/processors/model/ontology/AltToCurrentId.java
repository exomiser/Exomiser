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

import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AltToCurrentId implements OutputLine {

    private final String altId;
    private final String currentId;

    public AltToCurrentId(String altId, String currentId) {
        this.altId = altId;
        this.currentId = currentId;
    }

    public String getAltId() {
        return altId;
    }

    public String getCurrentId() {
        return currentId;
    }

    @Override
    public String toOutputLine() {
        return altId + "|" + currentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AltToCurrentId)) return false;
        AltToCurrentId that = (AltToCurrentId) o;
        return altId.equals(that.altId) &&
                currentId.equals(that.currentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(altId, currentId);
    }

    @Override
    public String toString() {
        return "AltToCurrentId{" +
                "altId='" + altId + '\'' +
                ", currentId='" + currentId + '\'' +
                '}';
    }
}
