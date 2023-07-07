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

package org.monarchinitiative.exomiser.core.prioritisers.model;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public final class Disease {

    public enum DiseaseType {

        DISEASE("disease", "D"),
        NON_DISEASE("non-disease", "N"),
        SUSCEPTIBILITY("susceptibility", "S"),
        UNCONFIRMED("unconfirmed", "?"),
        CNV("CNV", "C");

        private final String value;
        private final String columnValue;

        DiseaseType(String value, String columnValue) {
            this.value = value;
            this.columnValue = columnValue;
        }

        public String getValue() {
            return value;
        }

        public String getCode() {
            return columnValue;
        }
    }

    private final String diseaseId;
    private final String diseaseName;

    private final int associatedGeneId;
    private final String associateGeneSymbol;

    private final DiseaseType diseaseType;
    private final InheritanceMode inheritanceMode;

    private final List<String> phenotypeIds;

    private Disease(Builder builder) {
        this.diseaseId = builder.diseaseId;
        this.diseaseName = builder.diseaseName;
        this.associatedGeneId = builder.entrezGeneId;
        this.associateGeneSymbol = builder.humanGeneSymbol;
        this.diseaseType = builder.diseaseType;
        this.inheritanceMode = builder.inheritanceMode;
        this.phenotypeIds = List.copyOf(builder.phenotypeIds);
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public int getAssociatedGeneId() {
        return associatedGeneId;
    }

    public String getAssociatedGeneSymbol() {
        return associateGeneSymbol;
    }

    public DiseaseType getDiseaseType() {
        return diseaseType;
    }

    public InheritanceMode getInheritanceMode() {
        return inheritanceMode;
    }

    public List<String> getPhenotypeIds() {
        return phenotypeIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disease)) return false;
        Disease that = (Disease) o;
        return associatedGeneId == that.associatedGeneId &&
                Objects.equals(diseaseId, that.diseaseId) &&
                Objects.equals(diseaseName, that.diseaseName) &&
                Objects.equals(associateGeneSymbol, that.associateGeneSymbol) &&
                diseaseType == that.diseaseType &&
                inheritanceMode == that.inheritanceMode &&
                Objects.equals(phenotypeIds, that.phenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diseaseId, diseaseName, associatedGeneId, associateGeneSymbol, diseaseType, inheritanceMode, phenotypeIds);
    }


    @Override
    public String toString() {
        return "Disease{" +
                "diseaseId='" + diseaseId + '\'' +
                ", diseaseName='" + diseaseName + '\'' +
                ", associatedGeneId=" + associatedGeneId +
                ", associateGeneSymbol='" + associateGeneSymbol + '\'' +
                ", diseaseType=" + diseaseType +
                ", inheritanceMode=" + inheritanceMode +
                ", phenotypeIds=" + phenotypeIds +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String diseaseId = "";
        private String diseaseName = "";

        private int entrezGeneId = 0;
        private String humanGeneSymbol = "";

        private DiseaseType diseaseType = DiseaseType.UNCONFIRMED;
        private InheritanceMode inheritanceMode = InheritanceMode.UNKNOWN;

        private List<String> phenotypeIds = List.of();

        private Builder() {
        }

        public Builder diseaseId(String diseaseId) {
            this.diseaseId = Objects.requireNonNullElse(diseaseId, "");
            return this;
        }

        public Builder diseaseName(String diseaseName) {
            this.diseaseName = Objects.requireNonNullElse(diseaseName, "");
            return this;
        }

        public Builder associatedGeneId(int entrezGeneId) {
            this.entrezGeneId = entrezGeneId;
            return this;
        }

        public Builder associatedGeneSymbol(String humanGeneSymbol) {
            this.humanGeneSymbol = Objects.requireNonNullElse(humanGeneSymbol, "");
            return this;
        }

        public Builder diseaseType(DiseaseType diseaseType) {
            this.diseaseType = Objects.requireNonNullElse(diseaseType, DiseaseType.UNCONFIRMED);
            return this;
        }

        public Builder inheritanceMode(InheritanceMode inheritanceMode) {
            this.inheritanceMode = Objects.requireNonNullElse(inheritanceMode, InheritanceMode.UNKNOWN);
            return this;
        }

        public Builder phenotypeIds(List<String> phenotypeIds) {
            this.phenotypeIds = Objects.requireNonNullElse(phenotypeIds, List.of());
            return this;
        }

        public Disease build() {
            return new Disease(this);
        }

    }
}
