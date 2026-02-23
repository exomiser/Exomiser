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

import java.util.Objects;

/**
 * This is a bit of a hybrid concept - can represent any geneId and corresponding symbol, but also contains fields for linking explicitly to human
 * gene databases such as ENSEMBL, ENTREZ gene and UCSC and the HGNC identifiers. This could for instance represent a mouse gene - human ortholog pair
 * or a human gene using one of the mapped coordinate systems as an implicit default via the getGeneId and getGeneSymbol methods.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
public record GeneIdentifier(String geneId,
                             String geneSymbol,

                             String hgncId,
                             String hgncSymbol,

                             String entrezId,
                             String ensemblId,
                             String ucscId) implements Comparable<GeneIdentifier> {


    public boolean hasEntrezId() {
        return !entrezId.isEmpty();
    }

    /**
     * Compares the two specified {@code GeneIdentifier} values based on the natural lexicographical order of their
     * gene symbols.
     *
     * @param otherGeneIdentifier The other {@code GeneIdentifier} to be compared against this one.
     * @return
     * @since 10.0.0
     */
    public int compareTo(GeneIdentifier otherGeneIdentifier) {
        Objects.requireNonNull(otherGeneIdentifier);
        return GeneIdentifier.compare(this, otherGeneIdentifier);
    }

    /**
     * Compares the two specified {@code GeneIdentifier} values based on the natural lexicographical order of their
     * gene symbols.
     *
     * @param g1 the first {@code GeneIdentifier} to be compared.
     * @param g2 the second {@code GeneIdentifier} to be compared.
     * @return the value {@code 0} if the argument {@code GeneIdentifier} gene symbol is equal to
     *          this gene symbol; a value less than {@code 0} if this gene symbol
     *          is lexicographically less than the {@code GeneIdentifier} argument gene symbol; and a
     *          value greater than {@code 0} if this gene symbol is
     *          lexicographically greater than the {@code GeneIdentifier} argument gene symbol.
     * @since 10.0.0
     */
    public static int compare(GeneIdentifier g1, GeneIdentifier g2) {
        String g1GeneSymbol = g1.geneSymbol();
        String g2GeneSymbol = g2.geneSymbol();
        return g1GeneSymbol.compareTo(g2GeneSymbol);
    }

    @Override
    public String toString() {
        return "GeneIdentifier{" +
               "geneId='" + geneId + '\'' +
               ", geneSymbol='" + geneSymbol + '\'' +
               ", hgncId='" + hgncId + '\'' +
               ", hgncSymbol='" + hgncSymbol + '\'' +
               ", entrezId='" + entrezId + '\'' +
               ", ensemblId='" + ensemblId + '\'' +
               ", ucscId='" + ucscId + '\'' +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String geneId = "";
        private String geneSymbol = "";

        private String hgncId = "";
        private String hgncSymbol = "";

        private String entrezId = "";
        private String ensemblId = "";
        private String ucscId = "";

        private Builder() {
        }

        public Builder geneId(String geneId) {
            this.geneId = geneId;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
            return this;
        }

        public Builder hgncId(String hgncId) {
            this.hgncId = Objects.requireNonNullElse(hgncId, "");
            return this;
        }

        public Builder hgncSymbol(String hgncSymbol) {
            this.hgncSymbol = Objects.requireNonNullElse(hgncSymbol, "");
            return this;
        }

        public Builder entrezId(String entrezId) {
            this.entrezId = entrezId;
            return this;
        }

        public Builder ensemblId(String ensemblId) {
            this.ensemblId = ensemblId;
            return this;
        }

        public Builder ucscId(String ucscId) {
            this.ucscId = Objects.requireNonNullElse(ucscId, "");
            return this;
        }

        public GeneIdentifier build() {
            return new GeneIdentifier(
                    Objects.requireNonNull(geneId, "GeneIdentifier geneId cannot be null"),
                    Objects.requireNonNull(geneSymbol, "GeneIdentifier geneSymbol cannot be null"),

                    hgncId,
                    hgncSymbol,

                    //the entrezId is used by the Prioritisers as a primary key for the gene so this is important!
                    Objects.requireNonNull(entrezId, "GeneIdentifier entrezId cannot be null"),
                    ensemblId,
                    ucscId);
        }
    }
}
