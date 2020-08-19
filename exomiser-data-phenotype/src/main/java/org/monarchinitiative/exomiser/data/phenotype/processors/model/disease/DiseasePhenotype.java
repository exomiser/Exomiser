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
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseasePhenotype implements OutputLine {

    private final String diseaseId;
    private final Set<String> hpIds;

    public DiseasePhenotype(String diseaseId, Set<String> hpIds) {
        this.diseaseId = Objects.requireNonNull(diseaseId);
        this.hpIds = Objects.requireNonNull(hpIds);
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public Set<String> getHpIds() {
        return hpIds;
    }

    public String toOutputLine() {
        return diseaseId + "|" + String.join(",", hpIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiseasePhenotype)) return false;
        DiseasePhenotype that = (DiseasePhenotype) o;
        return diseaseId.equals(that.diseaseId) &&
                hpIds.equals(that.hpIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diseaseId, hpIds);
    }

    @Override
    public String toString() {
        return "DiseasePhenotype{" +
                "diseaseId='" + diseaseId + '\'' +
                ", hpIds=" + hpIds +
                '}';
    }
}
