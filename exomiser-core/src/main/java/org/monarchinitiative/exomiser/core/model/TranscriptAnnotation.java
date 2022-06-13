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

import javax.annotation.Nonnull;
import java.util.Objects;

import static de.charite.compbio.jannovar.annotation.AnnotationLocation.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TranscriptAnnotation {

    private static final TranscriptAnnotation EMPTY = TranscriptAnnotation.builder().build();

    private final VariantEffect variantEffect;

    private final String geneSymbol;
    private final String accession;

    private final String hgvsGenomic;
    private final String hgvsCdna;
    private final String hgvsProtein;

    // exon / intron 'rank' e.g. lies in Exon (rank) 3 of (totalRank) 4
    private final RankType rankType;
    // 1-based intron/exon rank
    private final int rank;
    // total number of introns/exons for this transcript
    private final int rankTotal;

    private final int distanceFromNearestGene;

    private TranscriptAnnotation(Builder builder) {
        this.variantEffect = builder.variantEffect;
        this.geneSymbol = builder.geneSymbol;
        this.accession = builder.accession;
        this.hgvsGenomic = builder.hgvsGenomic;
        this.hgvsCdna = builder.hgvsCdna;
        this.hgvsProtein = builder.hgvsProtein;
        this.rankType = builder.rankType;
        this.rank = builder.rank;
        this.rankTotal = builder.rankTotal;
        this.distanceFromNearestGene = builder.distanceFromNearestGene;
    }

    public static TranscriptAnnotation empty() {
        return EMPTY;
    }

    public VariantEffect getVariantEffect() {
        return variantEffect;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getAccession() {
        return accession;
    }

    public String getHgvsGenomic() {
        return hgvsGenomic;
    }

    public String getHgvsCdna() {
        return hgvsCdna;
    }

    public String getHgvsProtein() {
        return hgvsProtein;
    }

    public RankType getRankType() {
        return rankType;
    }

    public int getRank() {
        return rank;
    }

    public int getRankTotal() {
        return rankTotal;
    }

    public int getDistanceFromNearestGene() {
        return distanceFromNearestGene;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptAnnotation that = (TranscriptAnnotation) o;
        return rank == that.rank && rankTotal == that.rankTotal && distanceFromNearestGene == that.distanceFromNearestGene && variantEffect == that.variantEffect && geneSymbol.equals(that.geneSymbol) && accession.equals(that.accession) && hgvsGenomic.equals(that.hgvsGenomic) && hgvsCdna.equals(that.hgvsCdna) && hgvsProtein.equals(that.hgvsProtein) && rankType == that.rankType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantEffect, geneSymbol, accession, hgvsGenomic, hgvsCdna, hgvsProtein, rankType, rank, rankTotal, distanceFromNearestGene);
    }

    @Override
    public String toString() {
        return "TranscriptAnnotation{" +
                "variantEffect=" + variantEffect +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", accession='" + accession + '\'' +
                ", hgvsGenomic='" + hgvsGenomic + '\'' +
                ", hgvsCdna='" + hgvsCdna + '\'' +
                ", hgvsProtein='" + hgvsProtein + '\'' +
                ", rankType='" + rankType + '\'' +
                ", rank='" + rank + '\'' +
                ", rankTotal='" + rankTotal + '\'' +
                ", distanceFromNearestGene=" + distanceFromNearestGene +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;

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
            this.geneSymbol = geneSymbol;
            return this;
        }

        public Builder accession(String accession) {
            this.accession = accession;
            return this;
        }

        public Builder hgvsGenomic(String hgvsGenomic) {
            this.hgvsGenomic = hgvsGenomic;
            return this;
        }

        public Builder hgvsCdna(String hgvsCdna) {
            this.hgvsCdna = hgvsCdna;
            return this;
        }

        public Builder hgvsProtein(String hgvsProtein) {
            this.hgvsProtein = hgvsProtein;
            return this;
        }

        public Builder rankType(RankType rankType) {
            this.rankType = rankType;
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
            return new TranscriptAnnotation(this);
        }
    }

}
