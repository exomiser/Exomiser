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

package org.monarchinitiative.exomiser.data.genome.parsers;

import java.util.Objects;

/**
 * DbNSFP is a massive TSV table which is great, however they like to add new columns with new data but in doing so they
 * often change the position of the column we want. This class holds the column header names for use by the column indexer
 * in the {@link DbNsfpAlleleParser}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpColumnIndex {

    public static final DbNsfpColumnIndex HG19 = builder().chrHeader("hg19_chr").posHeader("hg19_pos(1-based)").build();
    public static final DbNsfpColumnIndex HG38 = builder().chrHeader("chr").posHeader("pos(1-based)").build();

    private final String chrHeader;
    private final String posHeader;
    private final String rsPrefix;
    private final String refHeader;
    private final String altHeader;

    private final String siftHeader;
    private final String polyPhen2HvarHeader;
    private final String mTasterScoreHeader;
    private final String mTasterPredHeader;
    private final String revelScoreHeader;

    private DbNsfpColumnIndex(Builder builder) {
        this.chrHeader = builder.chrHeader;
        this.posHeader = builder.posHeader;
        this.rsPrefix = builder.rsPrefix;
        this.refHeader = builder.refHeader;
        this.altHeader = builder.altHeader;

        this.siftHeader = builder.siftHeader;
        this.polyPhen2HvarHeader = builder.polyPhen2HvarHeader;
        this.mTasterScoreHeader = builder.mTasterScoreHeader;
        this.mTasterPredHeader = builder.mTasterPredHeader;
        this.revelScoreHeader = builder.revelScoreHeader;
    }

    public String getChrHeader() {
        return chrHeader;
    }

    public String getPosHeader() {
        return posHeader;
    }

    public String getRsPrefix() {
        return rsPrefix;
    }

    public String getRefHeader() {
        return refHeader;
    }

    public String getAltHeader() {
        return altHeader;
    }

    public String getSiftHeader() {
        return siftHeader;
    }

    public String getPolyPhen2HvarHeader() {
        return polyPhen2HvarHeader;
    }

    public String getMTasterScoreHeader() {
        return mTasterScoreHeader;
    }

    public String getMTasterPredHeader() {
        return mTasterPredHeader;
    }

    public String getRevelScoreHeader() {
        return revelScoreHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbNsfpColumnIndex that = (DbNsfpColumnIndex) o;
        return Objects.equals(chrHeader, that.chrHeader) &&
                Objects.equals(posHeader, that.posHeader) &&
                Objects.equals(rsPrefix, that.rsPrefix) &&
                Objects.equals(refHeader, that.refHeader) &&
                Objects.equals(altHeader, that.altHeader) &&
                Objects.equals(siftHeader, that.siftHeader) &&
                Objects.equals(polyPhen2HvarHeader, that.polyPhen2HvarHeader) &&
                Objects.equals(mTasterScoreHeader, that.mTasterScoreHeader) &&
                Objects.equals(mTasterPredHeader, that.mTasterPredHeader) &&
                Objects.equals(revelScoreHeader, that.revelScoreHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chrHeader, posHeader, rsPrefix, refHeader, altHeader, siftHeader, polyPhen2HvarHeader, mTasterScoreHeader, mTasterPredHeader, revelScoreHeader);
    }

    @Override
    public String toString() {
        return "DbNsfpColumnIndex{" +
                "chrHeader='" + chrHeader + '\'' +
                ", posHeader='" + posHeader + '\'' +
                ", rsPrefix='" + rsPrefix + '\'' +
                ", refHeader='" + refHeader + '\'' +
                ", altHeader='" + altHeader + '\'' +
                ", siftHeader='" + siftHeader + '\'' +
                ", polyPhen2HvarHeader='" + polyPhen2HvarHeader + '\'' +
                ", mTasterScoreHeader='" + mTasterScoreHeader + '\'' +
                ", mTasterPredHeader='" + mTasterPredHeader + '\'' +
                ", revelScoreHeader='" + revelScoreHeader + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String chrHeader = "chr";
        private String posHeader = "pos(1-based)";
        //the rs column could change as it starts with 'rs_dbSNP' and is suffixed with the DbSNP build number: rs_dbSNP147
        private String rsPrefix = "rs_dbSNP";
        private String refHeader = "ref";
        private String altHeader = "alt";

        private String siftHeader = "SIFT_score";
        private String polyPhen2HvarHeader = "Polyphen2_HVAR_score";
        private String mTasterScoreHeader = "MutationTaster_score";
        private String mTasterPredHeader = "MutationTaster_pred";
        private String revelScoreHeader = "REVEL_score";

        public Builder chrHeader(String chrHeader) {
            this.chrHeader = chrHeader;
            return this;
        }

        public Builder posHeader(String posHeader) {
            this.posHeader = posHeader;
            return this;
        }

        public Builder rsPrefix(String rsPrefix) {
            this.rsPrefix = rsPrefix;
            return this;
        }

        public Builder refHeader(String refHeader) {
            this.refHeader = refHeader;
            return this;
        }

        public Builder altHeader(String altHeader) {
            this.altHeader = altHeader;
            return this;
        }

        public Builder siftHeader(String siftHeader) {
            this.siftHeader = siftHeader;
            return this;
        }

        public Builder polyPhen2HvarHeader(String polyPhen2HvarHeader) {
            this.polyPhen2HvarHeader = polyPhen2HvarHeader;
            return this;
        }

        public Builder mTasterScoreHeader(String mTasterScoreHeader) {
            this.mTasterScoreHeader = mTasterScoreHeader;
            return this;
        }

        public Builder mTasterPredHeader(String mTasterPredHeader) {
            this.mTasterPredHeader = mTasterPredHeader;
            return this;
        }

        public Builder revelScoreHeader(String revelScoreHeader) {
            this.revelScoreHeader = revelScoreHeader;
            return this;
        }

        public DbNsfpColumnIndex build() {
            return new DbNsfpColumnIndex(this);
        }
    }
}
