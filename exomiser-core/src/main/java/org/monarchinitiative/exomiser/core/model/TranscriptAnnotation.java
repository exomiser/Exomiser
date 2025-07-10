/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.VariantEffect;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record TranscriptAnnotation(VariantEffect variantEffect,
                                   // Set<VariantEffect> variantEffects,
                                   String geneSymbol,
                                   String accession,

                                   String hgvsGenomic,
                                   String hgvsCdna,
                                   String hgvsProtein,
                                   // exon / intron 'rank' e.g. lies in Exon (rank) 3 of (totalRank) 4
                                   RankType rankType,
                                   // 1-based intron/exon rank
                                   int rank,
                                   // total number of introns/exons for this transcript
                                   int rankTotal,
                                   int distanceFromNearestGene) {

    public enum RankType {
        EXON, INTRON, UNDEFINED
    }

    private static final TranscriptAnnotation EMPTY = TranscriptAnnotation.builder().build();

    public static TranscriptAnnotation empty() {
        return EMPTY;
    }

    @Override
    public String toString() {
        return "TranscriptAnnotation{" + "variantEffect=" + variantEffect + ", geneSymbol='" + geneSymbol + '\'' + ", accession='" + accession + '\'' + ", hgvsGenomic='" + hgvsGenomic + '\'' + ", hgvsCdna='" + hgvsCdna + '\'' + ", hgvsProtein='" + hgvsProtein + '\'' + ", rankType='" + rankType + '\'' + ", rank='" + rank + '\'' + ", rankTotal='" + rankTotal + '\'' + ", distanceFromNearestGene=" + distanceFromNearestGene + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;

//        private Set<VariantEffect> variantEffects = EnumSet.noneOf(VariantEffect.class);

        private String geneSymbol = "";
        private String accession = "";

        private String hgvsGenomic = "";
        private String hgvsCdna = "";
        private String hgvsProtein = "";

        private RankType rankType = RankType.UNDEFINED;
        private int rank = -1;
        private int rankTotal = -1;

        private int distanceFromNearestGene = Integer.MIN_VALUE;

        public Builder variantEffect(@Nonnull VariantEffect variantEffect) {
            this.variantEffect = variantEffect;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = Objects.requireNonNullElse(geneSymbol, "");
            return this;
        }

        public Builder accession(String accession) {
            this.accession = Objects.requireNonNullElse(accession, "");
            return this;
        }

        public Builder hgvsGenomic(String hgvsGenomic) {
            this.hgvsGenomic = Objects.requireNonNullElse(hgvsGenomic, "");
            return this;
        }

        public Builder hgvsCdna(String hgvsCdna) {
            this.hgvsCdna = Objects.requireNonNullElse(hgvsCdna, "");
            return this;
        }

        public Builder hgvsProtein(String hgvsProtein) {
            this.hgvsProtein = Objects.requireNonNullElse(hgvsProtein, "");
            return this;
        }

        public Builder rankType(RankType rankType) {
            this.rankType = Objects.requireNonNullElse(rankType, RankType.UNDEFINED);
            return this;
        }

        public Builder rankTotal(int rankTotal) {
            this.rankTotal = rankTotal;
            return this;
        }

        public Builder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public Builder distanceFromNearestGene(int distanceFromNearestGene) {
            this.distanceFromNearestGene = distanceFromNearestGene;
            return this;
        }

        public TranscriptAnnotation build() {
            return new TranscriptAnnotation(
                    variantEffect,
                    geneSymbol,
                    accession,
                    hgvsGenomic,
                    hgvsCdna,
                    hgvsProtein,
                    rankType,
                    rank,
                    rankTotal,
                    distanceFromNearestGene
            );
        }
    }

}
