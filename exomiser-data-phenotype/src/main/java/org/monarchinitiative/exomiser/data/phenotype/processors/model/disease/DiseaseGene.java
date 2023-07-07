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

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseTypeCodes;
import org.monarchinitiative.exomiser.core.prioritisers.dao.InheritanceModeCodes;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;

import java.util.List;
import java.util.Objects;

/**
 * Data class for a single disease-gene-mode of inheritance association. For use when parsing OMIM and Orphanet data so
 * they can be compared easily before being added to the database disease table.
 *
 * OMIM maps to MIM gene id, Entrez and Ensembl
 * ORPHA maps to MIM gene id, HGNC and Ensembl
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseGene implements OutputLine {

    private final String diseaseId;
    private final String diseaseName;
    private final Disease.DiseaseType diseaseType;
    private final String geneSymbol;
    private final String geneName;
    private final List<String> geneSynonyms;
    private final String hgncId; //nullable
    private final String ensemblGeneId;
    private final int entrezGeneId;
    private final String omimGeneId; // nullable
    private final InheritanceMode inheritanceMode;

    private DiseaseGene(Builder builder) {
        this.diseaseId = builder.diseaseId;
        this.diseaseName = builder.diseaseName;
        this.diseaseType = builder.diseaseType;
        this.geneSymbol = builder.geneSymbol;
        this.geneName = builder.geneName;
        this.geneSynonyms = builder.geneSynonyms.build();
        this.hgncId = builder.hgncId;
        this.ensemblGeneId = builder.ensemblGeneId;
        this.entrezGeneId = builder.entrezGeneId;
        this.omimGeneId = builder.omimGeneId;
        this.inheritanceMode = builder.inheritanceMode;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public Disease.DiseaseType getDiseaseType() {
        return diseaseType;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneName() {
        return geneName;
    }

    public List<String> getGeneSynonyms() {
        return geneSynonyms;
    }

    public String getHgncId() {
        return hgncId;
    }

    public String getEnsemblGeneId() {
        return ensemblGeneId;
    }

    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    public String getOmimGeneId() {
        return omimGeneId;
    }

    public InheritanceMode getInheritanceMode() {
        return inheritanceMode;
    }

    @Override
    public String toOutputLine() {
        return String.format("%s|%s|%s|%d|%s|%s", diseaseId, omimGeneId, diseaseName, entrezGeneId,
                DiseaseTypeCodes.toDiseaseTypeCode(diseaseType),
                InheritanceModeCodes.toInheritanceModeCode(inheritanceMode));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiseaseGene)) return false;
        DiseaseGene that = (DiseaseGene) o;
        return Objects.equals(diseaseId, that.diseaseId) &&
                diseaseType == that.diseaseType &&
                Objects.equals(geneSymbol, that.geneSymbol) &&
                Objects.equals(geneName, that.geneName) &&
                Objects.equals(geneSynonyms, that.geneSynonyms) &&
                Objects.equals(hgncId, that.hgncId) &&
                Objects.equals(omimGeneId, that.omimGeneId) &&
                inheritanceMode == that.inheritanceMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(diseaseId, diseaseType, geneSymbol, geneName, geneSynonyms, hgncId, omimGeneId, inheritanceMode);
    }

    @Override
    public String toString() {
        return "DiseaseGene{" +
                "diseaseId='" + diseaseId + '\'' +
                ", diseaseName='" + diseaseName + '\'' +
                ", diseaseType=" + diseaseType +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", geneName='" + geneName + '\'' +
                ", geneSynonyms=" + geneSynonyms +
                ", hgncId='" + hgncId + '\'' +
                ", ensemblGeneId='" + ensemblGeneId + '\'' +
                ", entrezGeneId=" + entrezGeneId +
                ", omimGeneId='" + omimGeneId + '\'' +
                ", inheritanceMode=" + inheritanceMode +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String diseaseId = "";
        private String diseaseName = "";
        private Disease.DiseaseType diseaseType = Disease.DiseaseType.UNCONFIRMED;
        private String geneSymbol = "";
        private String geneName = "";
        private final ImmutableList.Builder<String> geneSynonyms = new ImmutableList.Builder<>();
        private String hgncId = "";
        private String ensemblGeneId = "";
        private int entrezGeneId = 0;
        private String omimGeneId = "";
        private InheritanceMode inheritanceMode = InheritanceMode.UNKNOWN;

        public Builder diseaseId(String diseaseId) {
            this.diseaseId = Objects.requireNonNull(diseaseId);
            return this;
        }

        public Builder diseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
            return this;
        }

        public Builder diseaseType(Disease.DiseaseType diseaseType) {
            this.diseaseType = Objects.requireNonNull(diseaseType);
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = Objects.requireNonNull(geneSymbol);
            return this;
        }

        public Builder geneName(String geneName) {
            this.geneName = Objects.requireNonNull(geneName);
            return this;
        }

        public Builder synonyms(List<String> synonyms) {
            this.geneSynonyms.addAll(synonyms);
            return this;
        }

        public Builder hgncId(String hgncId) {
            this.hgncId = hgncId;
            return this;
        }

        public Builder ensemblGeneId(String ensemblGeneId) {
            this.ensemblGeneId = ensemblGeneId;
            return this;
        }

        public Builder entrezGeneId(int entrezGeneId) {
            this.entrezGeneId = entrezGeneId;
            return this;
        }

        public Builder omimGeneId(String omimGeneId) {
            this.omimGeneId = omimGeneId;
            return this;
        }

        public Builder inheritanceMode(InheritanceMode inheritanceMode) {
            this.inheritanceMode = inheritanceMode;
            return this;
        }

        public DiseaseGene build() {
            return new DiseaseGene(this);
        }
    }
}
