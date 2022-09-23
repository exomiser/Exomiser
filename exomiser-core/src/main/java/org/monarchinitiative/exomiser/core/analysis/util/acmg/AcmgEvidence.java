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

    private static final AcmgEvidence EMPTY = new AcmgEvidence(Map.of());

    @JsonProperty
    private final Map<AcmgCriterion, Evidence> evidence;

    private int pvs = 0;
    private int ps = 0;
    private int pm = 0;
    private int pp = 0;

    private int ba = 0;
    private int bs = 0;
    private int bp = 0;

    private AcmgEvidence(Map<AcmgCriterion, Evidence> evidence) {
        this.evidence = evidence == null || evidence.isEmpty() ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(evidence));
        countCriteriaEvidence(this.evidence);
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
                    case VERY_STRONG -> pvs++;
                    case STRONG -> ps++;
                    case MODERATE -> pm++;
                    case SUPPORTING -> pp++;
                    default -> {
                        // do nothing
                    }
                }
            }

            if (acmgCriterion.isBenign()) {
                switch (evidenceStrength) {
                    case STAND_ALONE -> ba++;
                    case STRONG -> bs++;
                    case SUPPORTING -> bp++;
                    default -> {
                        // do nothing
                    }
                }
            }
        }
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

    public int bs() {
        return bs;
    }

    public int bp() {
        return bp;
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
