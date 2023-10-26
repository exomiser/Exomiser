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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.Evidence;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @since 13.1.0
 */
public class AcmgEvidence {

    // These constants are derived in "Modeling the ACMG/AMP Variant Classification Guidelines as a Bayesian
    //  Classification Framework" Tavtigian et al. 2018, DOI:10.1038/gim.2017.210
    // https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6336098/bin/NIHMS915467-supplement-Supplemental_Table_S1.xlsx

    // Very_Strong == (2 * Strong) == (2 * Moderate) == (2 * Supporting)
    // therefore points Supporting = 1, Moderate = 2, Strong = 4, Very_strong = 8 can be assigned and these fit to a
    // Bayesian classification framework where (using the combining rules from Riggs et al. 2016) the posterior
    // probabilities are Path >= 0.99, LikelyPath 0.90 - 0.98, LikelyBenign 0.1 - 0.01, Benign < 0.01

    private static final double PRIOR_PROB = 0.1;
    private static final double ODDS_PATH_VERY_STRONG = 350.0;
    private static final double EXPONENTIAL_PROGRESSION = 2.0;
    private static final double SUPPORTING_EVIDENCE_EXPONENT = Math.pow(EXPONENTIAL_PROGRESSION, -3); // 0.125
    private static final double ODDS_PATH_SUPPORTING = Math.pow(ODDS_PATH_VERY_STRONG, SUPPORTING_EVIDENCE_EXPONENT); // 2.08

    private static final AcmgEvidence EMPTY = new AcmgEvidence(Map.of());

    @JsonProperty
    private final Map<AcmgCriterion, Evidence> evidence;

    private int pvs = 0;
    private int ps = 0;
    private int pm = 0;
    private int pp = 0;

    private int ba = 0;
    private int bvs = 0;
    private int bs = 0;
    private int bm = 0;
    private int bp = 0;

    private int points = 0;

    private AcmgEvidence(Map<AcmgCriterion, Evidence> evidence) {
        this.evidence = evidence == null || evidence.isEmpty() ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(evidence));
        countCriteriaEvidence(this.evidence);
        points = pathPoints() - benignPoints();
    }

    @JsonCreator
    public static AcmgEvidence of(Map<AcmgCriterion, Evidence> evidence) {
        return new AcmgEvidence(evidence);
    }

    public static AcmgEvidence empty() {
        return EMPTY;
    }

    private void countCriteriaEvidence(Map<AcmgCriterion, Evidence> evidence) {
        for (Map.Entry<AcmgCriterion, Evidence> entry : evidence.entrySet()) {
            AcmgCriterion acmgCriterion = entry.getKey();
            Evidence evidenceStrength = entry.getValue();
            if (acmgCriterion.isPathogenic()) {
                switch (evidenceStrength) {
                    case VERY_STRONG:
                        pvs++;
                        break;
                    case STRONG:
                        ps++;
                        break;
                    case MODERATE:
                        pm++;
                        break;
                    case SUPPORTING:
                        pp++;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }

            if (acmgCriterion.isBenign()) {
                switch (evidenceStrength) {
                    case STAND_ALONE:
                        ba++;
                        break;
                    case VERY_STRONG:
                        bvs++;
                        break;
                    case STRONG:
                        bs++;
                        break;
                    case MODERATE:
                        bm++;
                        break;
                    case SUPPORTING:
                        bp++;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
        }
    }

    public int pathPoints() {
        return (int) (pp + pm * 2.0 + ps * 4.0 + pvs * 8.0);
    }

    public int benignPoints() {
        // n.b. here BA1 is given the equivalent weight as PVS1. This was *not* specified in the two papers. Specifically,
        // in the 2018 paper they state "We excluded BA1, “benign stand alone” because it is used as absolute evidence
        // that a variant is benign, irrespective of other evidence, which is contrary to Bayesian reasoning. The BA1
        // filter is useful for excluding a variant from entering a Bayesian framework, and will be addressed separately
        // by the ClinGen Sequence Variant Interpretation (SVI) Working Group."
        // Similarly, BM and BVS have been added here because it is possible to assign a Moderate, Strong or VeryStrong
        // modifier to BP4 according to https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9748256/ which will result in a VUS
        // rather than LB if not included
        return (int) (bp + bm * 2.0 + bs * 4.0 + bvs * 8.0 + ba * 8.0);
    }

    public boolean hasCriterion(AcmgCriterion acmgCriterion) {
        return evidence.containsKey(acmgCriterion);
    }

    @Nullable
    public Evidence criterionEvidence(AcmgCriterion acmgCriterion) {
        return evidence.get(acmgCriterion);
    }

    public int size() {
        return evidence.size();
    }

    public boolean isEmpty() {
        return evidence.isEmpty();
    }

    public Map<AcmgCriterion, Evidence> evidence() {
        return evidence;
    }

    public int pvs() {
        return pvs;
    }

    public int ps() {
        return ps;
    }

    public int pm() {
        return pm;
    }

    public int pp() {
        return pp;
    }

    public int ba() {
        return ba;
    }

    public int bvs() {
        return bvs;
    }

    public int bs() {
        return bs;
    }

    public int bm() {
        return bm;
    }

    public int bp() {
        return bp;
    }

    public int points() {
        return points;
    }

    public double postProbPath() {
        // Equation 2 from Tavtigian et al., 2020 (DOI: 10.1002/humu.24088) which is a re-written from of equation 5 from
        // Tavtigian et al., 2018 (DOI: 10.1038/gim.2017.210)
        double oddsPath = Math.pow(ODDS_PATH_SUPPORTING, points);
        // posteriorProbability = (OddsPathogenicity*Prior P)/((OddsPathogenicity−1)*Prior_P+1)
        return (oddsPath * PRIOR_PROB) / ((oddsPath - 1) * PRIOR_PROB + 1);
    }

    public static AcmgEvidence.Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcmgEvidence that = (AcmgEvidence) o;
        return evidence.equals(that.evidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidence);
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (Map.Entry<AcmgCriterion, Evidence> entry : evidence.entrySet()) {
            AcmgCriterion acmgCriterion = entry.getKey();
            Evidence assignedEvidence = entry.getValue();
            if (acmgCriterion.evidence() != assignedEvidence) {
                stringJoiner.add(acmgCriterion.toString() + '_' + assignedEvidence.displayString());
            } else {
                stringJoiner.add(acmgCriterion.toString());
            }
        }
        return '[' + stringJoiner.toString() + ']';
    }

    public static class Builder {

        private final EnumMap<AcmgCriterion, Evidence> evidence = new EnumMap<>(AcmgCriterion.class);

        public Builder add(AcmgCriterion acmgCriterion) {
            evidence.put(acmgCriterion, acmgCriterion.evidence());
            return this;
        }

        public Builder add(AcmgCriterion acmgCriterion, Evidence evidence) {
            this.evidence.put(acmgCriterion, evidence);
            return this;
        }

        public boolean contains(AcmgCriterion acmgCriterion) {
            return evidence.containsKey(acmgCriterion);
        }

        public AcmgEvidence build() {
            return evidence.isEmpty() ? EMPTY : new AcmgEvidence(evidence);
        }
    }
}
