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

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseGeneMoiComparison implements OutputLine {

    private final DiseaseGene omimDiseaseGene;
    private final InheritanceMode inheritanceMode;

    public DiseaseGeneMoiComparison(DiseaseGene omimDiseaseGene, InheritanceMode inheritanceMode) {
        this.omimDiseaseGene = omimDiseaseGene;
        this.inheritanceMode = inheritanceMode;
    }

    public static DiseaseGeneMoiComparison of(DiseaseGene omimDiseaseGene, DiseaseGene hpoDiseaseGene) {
        return new DiseaseGeneMoiComparison(omimDiseaseGene, hpoDiseaseGene.getInheritanceMode());
    }

    public static DiseaseGeneMoiComparison of(DiseaseGene omimDiseaseGene, InheritanceMode moi) {
        return new DiseaseGeneMoiComparison(omimDiseaseGene, moi);
    }

    public boolean isMissingHpoMoi() {
        return inheritanceMode == InheritanceMode.UNKNOWN && omimDiseaseGene.getInheritanceMode() != InheritanceMode.UNKNOWN;
    }

    public boolean isMissingOmimMoi() {
        return omimDiseaseGene.getInheritanceMode() == InheritanceMode.UNKNOWN && inheritanceMode != InheritanceMode.UNKNOWN;
    }

    /**
     * @return true if neither {@link InheritanceMode} is UNKNOWN or equal.
     */
    public boolean hasMismatchedMoi() {
        return inheritanceMode != InheritanceMode.UNKNOWN && omimDiseaseGene.getInheritanceMode() != InheritanceMode.UNKNOWN && omimDiseaseGene.getInheritanceMode() != inheritanceMode;
    }

    public boolean hasMatchingMoi() {
        return inheritanceMode == omimDiseaseGene.getInheritanceMode();
    }

    @Override
    public String toOutputLine() {
        return "- [ ] " + omimDiseaseGene.getDiseaseName() +
                " (" + omimDiseaseGene.getDiseaseId() + ")" +
                "; " + omimDiseaseGene.getGeneSymbol() +
                " (" + omimDiseaseGene.getOmimGeneId() + ")" +
                "; HPO_MOI: " + inheritanceMode.toString() +
                ", OMIM_MOI: " + omimDiseaseGene.getInheritanceMode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiseaseGeneMoiComparison)) return false;
        DiseaseGeneMoiComparison that = (DiseaseGeneMoiComparison) o;
        return inheritanceMode.equals(that.inheritanceMode) &&
                omimDiseaseGene.equals(that.omimDiseaseGene);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inheritanceMode, omimDiseaseGene);
    }

    @Override
    public String toString() {
        return "DiseaseGeneMoiComparison{" +
                "diseaseId=" + omimDiseaseGene.getDiseaseId() +
                ", geneId=" + omimDiseaseGene.getOmimGeneId() +
                ", " + omimDiseaseGene.getDiseaseName() +
                ", HPO MOI=" + inheritanceMode +
                ", OMIM MOI=" + omimDiseaseGene.getInheritanceMode() +
                '}';
    }
}
