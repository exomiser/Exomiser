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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Immutable data class representing data from the <a href=https://www.ncbi.nlm.nih.gov/clinvar/>ClinVar resource</a>, with
 * explanation of the data from <a href=https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/>https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/</a>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class ClinVarData {

    private static final ClinVarData EMPTY = new Builder().build();

    public enum ClinSig {
        // ACMG/AMP-based
        BENIGN,
        BENIGN_OR_LIKELY_BENIGN,
        LIKELY_BENIGN,
        UNCERTAIN_SIGNIFICANCE,
        LIKELY_PATHOGENIC,
        PATHOGENIC_OR_LIKELY_PATHOGENIC,
        PATHOGENIC,
        CONFLICTING_PATHOGENICITY_INTERPRETATIONS,
        //Non-ACMG-based
        AFFECTS,
        ASSOCIATION,
        DRUG_RESPONSE,
        NOT_PROVIDED,
        OTHER,
        PROTECTIVE,
        RISK_FACTOR;
    }

    //https://www.ncbi.nlm.nih.gov/clinvar/?term=99222[alleleid]
    private final String alleleId;
    private final ClinSig primaryInterpretation;
    private final Set<ClinSig> secondaryInterpretations;

    private final String reviewStatus;
    private final Map<String, ClinSig> includedAlleles;


    private ClinVarData(Builder builder) {
        this.alleleId = builder.alleleId;
        this.primaryInterpretation = builder.primaryInterpretation;
        this.secondaryInterpretations = Sets.immutableEnumSet(builder.secondaryInterpretations);
        this.reviewStatus = builder.reviewStatus;
        this.includedAlleles = ImmutableMap.copyOf(builder.includedAlleles);
    }

    public static ClinVarData empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public String getAlleleId() {
        return alleleId;
    }

    public ClinSig getPrimaryInterpretation() {
        return primaryInterpretation;
    }

    public Set<ClinSig> getSecondaryInterpretations() {
        return secondaryInterpretations;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public Map<String, ClinSig> getIncludedAlleles() {
        return includedAlleles;
    }

    /**
     * @return true if the secondary CLNSIG contains one of 'affects', 'other', 'association', 'risk factor' or
     * 'protective'. These are considered unimportant from the mendelian disease perspective. The category 'drug response'
     * is *not* included here as these are also associated with CFTR alleles known to be pathogenic/likely pathogenic
     * for CF.
     * @since 13.0.0
     */
    @JsonIgnore
    public boolean isSecondaryAssociationRiskFactorOrOther() {
        for (ClinVarData.ClinSig secondaryClinSig : secondaryInterpretations) {
            switch (secondaryClinSig) {
                case AFFECTS:
                case OTHER:
                case ASSOCIATION:
                case RISK_FACTOR:
                case PROTECTIVE:
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns the ClinVar star rating according to the criteria provided at
     * https://www.ncbi.nlm.nih.gov/clinvar/docs/review_status/#revstat_def
     * <p>
     * In the VCF CLNREVSTAT the start ratings are mapped as follows:
     * <p>
     * 1* criteria_provided,_conflicting_interpretations
     * 1* criteria_provided,_single_submitter
     * 2* criteria_provided,_multiple_submitters,_no_conflicts
     * 3* reviewed_by_expert_panel
     * 4* practice_guideline
     *
     * @return an integer value between 0 (worst) and 4 (best)
     * @since 13.0.0
     */
    @JsonIgnore
    public int getStarRating() {
        switch (reviewStatus) {
            case "criteria_provided,_single_submitter":
            case "criteria_provided,_conflicting_interpretations":
                return 1;
            case "criteria_provided,_multiple_submitters,_no_conflicts":
                return 2;
            case "reviewed_by_expert_panel":
                return 3;
            case "practice_guideline":
                return 4;
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClinVarData that = (ClinVarData) o;
        return Objects.equals(alleleId, that.alleleId) &&
                primaryInterpretation == that.primaryInterpretation &&
                Objects.equals(secondaryInterpretations, that.secondaryInterpretations) &&
                Objects.equals(reviewStatus, that.reviewStatus) &&
                Objects.equals(includedAlleles, that.includedAlleles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alleleId, primaryInterpretation, secondaryInterpretations, reviewStatus, includedAlleles);
    }

    @Override
    public String toString() {
        return "ClinVarData{" +
                "alleleId='" + alleleId + '\'' +
                ", primaryInterpretation=" + primaryInterpretation +
                ", secondaryInterpretations=" + secondaryInterpretations +
                ", reviewStatus='" + reviewStatus + '\'' +
                ", includedAlleles=" + includedAlleles +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String alleleId = "";
        private ClinSig primaryInterpretation = ClinSig.NOT_PROVIDED;
        private Set<ClinSig> secondaryInterpretations = EnumSet.noneOf(ClinSig.class);

        private String reviewStatus = "";
        private Map<String, ClinSig> includedAlleles = Collections.emptyMap();

        public Builder alleleId(String alleleId) {
            Objects.requireNonNull(alleleId);
            this.alleleId = alleleId;
            return this;
        }

        public Builder primaryInterpretation(ClinSig primaryInterpretation) {
            Objects.requireNonNull(primaryInterpretation);
            this.primaryInterpretation = primaryInterpretation;
            return this;
        }

        public Builder secondaryInterpretations(Set<ClinSig> secondaryInterpretations) {
            Objects.requireNonNull(secondaryInterpretations);
            this.secondaryInterpretations = secondaryInterpretations;
            return this;
        }

        public Builder reviewStatus(String reviewStatus) {
            Objects.requireNonNull(reviewStatus);
            this.reviewStatus = reviewStatus;
            return this;
        }

        public Builder includedAlleles(Map<String, ClinSig> includedAlleles) {
            Objects.requireNonNull(includedAlleles);
            this.includedAlleles = includedAlleles;
            return this;
        }

        public ClinVarData build() {
            return new ClinVarData(this);
        }

    }
}
