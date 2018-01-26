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

package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable class for collecting information about a gene's score under a particular inheritance mode and the alleles
 * contributing to the score.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class GeneScore {

    private static final GeneScore EMPTY = new Builder().build();

    private final GeneIdentifier geneIdentifier;
    private final ModeOfInheritance modeOfInheritance;
    private final float combinedScore;
    private final float phenotypeScore;
    private final float variantScore;
    private final List<VariantEvaluation> contributingVariants;

    private GeneScore(Builder builder) {
        this.geneIdentifier = builder.geneIdentifier;
        this.modeOfInheritance = builder.modeOfInheritance;
        this.combinedScore = builder.combinedScore;
        this.phenotypeScore = builder.phenotypeScore;
        this.variantScore = builder.variantScore;
        this.contributingVariants = ImmutableList.copyOf(builder.contributingVariants);
    }

    public GeneIdentifier getGeneIdentifier() {
        return geneIdentifier;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    public float getCombinedScore() {
        return combinedScore;
    }

    public float getPhenotypeScore() {
        return phenotypeScore;
    }

    public float getVariantScore() {
        return variantScore;
    }

    public List<VariantEvaluation> getContributingVariants() {
        return contributingVariants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneScore geneScore = (GeneScore) o;
        return Float.compare(geneScore.combinedScore, combinedScore) == 0 &&
                Float.compare(geneScore.phenotypeScore, phenotypeScore) == 0 &&
                Float.compare(geneScore.variantScore, variantScore) == 0 &&
                Objects.equals(geneIdentifier, geneScore.geneIdentifier) &&
                modeOfInheritance == geneScore.modeOfInheritance &&
                Objects.equals(contributingVariants, geneScore.contributingVariants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geneIdentifier, modeOfInheritance, combinedScore, phenotypeScore, variantScore, contributingVariants);
    }

    @Override
    public String toString() {
        return "GeneScore{" +
                "geneIdentifier=" + geneIdentifier +
                ", modeOfInheritance=" + modeOfInheritance +
                ", combinedScore=" + combinedScore +
                ", phenotypeScore=" + phenotypeScore +
                ", variantScore=" + variantScore +
                ", contributingVariants=" + contributingVariants +
                '}';
    }

    public static GeneScore empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private GeneIdentifier geneIdentifier = GeneIdentifier.builder().build();
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;
        private float combinedScore;
        private float phenotypeScore;
        private float variantScore;
        private List<VariantEvaluation> contributingVariants = new ArrayList<>();

        public Builder geneIdentifier(GeneIdentifier geneIdentifier) {
            this.geneIdentifier = geneIdentifier;
            return this;
        }

        public Builder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            this.modeOfInheritance = modeOfInheritance;
            return this;
        }

        public Builder combinedScore(float combinedScore) {
            this.combinedScore = combinedScore;
            return this;
        }

        public Builder phenotypeScore(float phenotypeScore) {
            this.phenotypeScore = phenotypeScore;
            return this;
        }

        public Builder variantScore(float variantScore) {
            this.variantScore = variantScore;
            return this;
        }

        public Builder contributingVariants(List<VariantEvaluation> contributingVariants) {
            this.contributingVariants = contributingVariants;
            return this;
        }

        public GeneScore build() {
            return new GeneScore(this);
        }
    }
}
