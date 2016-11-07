package org.monarchinitiative.exomiser.core.model;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HumanGeneIdentifier {

    public static final String EMPTY_FIELD = "";

    private final String hgncId;
    private final String geneSymbol;
    private final String geneName;
    private final String locusGroup;
    private final String locusType;
    private final String location;

    private final String entrezId;
    private final String ensemblId;
    private final String ucscId;

    private final boolean withdrawn;

    private HumanGeneIdentifier(Builder builder) {
        this.hgncId = builder.hgncId;
        this.geneSymbol = builder.geneSymbol;
        this.geneName = builder.geneName;
        this.entrezId = builder.entrezId;
        this.ensemblId = builder.ensemblId;
        this.ucscId = builder.ucscId;
        this.location = builder.location;
        this.locusGroup = builder.locusGroup;
        this.locusType = builder.locusType;

        this.withdrawn = builder.withdrawn;
    }

    public String getHgncId() {
        return hgncId;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getLocation() {
        return location;
    }

    public String getLocusGroup() {
        return locusGroup;
    }

    public String getLocusType() {
        return locusType;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public Integer getEntrezIdAsInteger() {
        return (entrezId.equals(EMPTY_FIELD)) ? -1 : Integer.valueOf(entrezId);
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public String getUcscId() {
        return ucscId;
    }

    public boolean isWithdrawn() {
        return withdrawn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HumanGeneIdentifier that = (HumanGeneIdentifier) o;
        return withdrawn == that.withdrawn &&
                Objects.equals(hgncId, that.hgncId) &&
                Objects.equals(geneSymbol, that.geneSymbol) &&
                Objects.equals(geneName, that.geneName) &&
                Objects.equals(location, that.location) &&
                Objects.equals(entrezId, that.entrezId) &&
                Objects.equals(ensemblId, that.ensemblId) &&
                Objects.equals(ucscId, that.ucscId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hgncId, geneSymbol, geneName, location, entrezId, ensemblId, ucscId, withdrawn);
    }

    @Override
    public String toString() {
        if (withdrawn) {
            return "HumanGeneIdentifier{" +
                    "hgncId='" + hgncId + '\'' +
                    ", geneSymbol='" + geneSymbol + '\'' +
                    ", WITHDRAWN" +
                    '}';
        }
        return "HumanGeneIdentifier{" +
                "hgncId='" + hgncId + '\'' +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", geneName='" + geneName + '\'' +
                ", location='" + location + '\'' +
                ", locusGroup='" + locusGroup + '\'' +
                ", locusType='" + locusType + '\'' +
                ", entrezId='" + entrezId + '\'' +
                ", ensemblId='" + ensemblId + '\'' +
                ", ucscId='" + ucscId + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hgncId = EMPTY_FIELD;
        private String geneSymbol = EMPTY_FIELD;
        private String geneName = EMPTY_FIELD;
        private String location = EMPTY_FIELD;

        private String locusGroup = EMPTY_FIELD;
        private String locusType = EMPTY_FIELD;

        private String entrezId = EMPTY_FIELD;
        private String ensemblId = EMPTY_FIELD;
        private String ucscId = EMPTY_FIELD;

        private boolean withdrawn = false;

        private Builder() {}

        public Builder hgncId(String hgncId) {
            this.hgncId = hgncId;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = geneSymbol;
            return this;
        }

        public Builder geneName(String geneName) {
            this.geneName = geneName;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder locusGroup(String locusGroup) {
            this.locusGroup = locusGroup;
            return this;
        }

        public Builder locusType(String locusType) {
            this.locusType = locusType;
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
            this.ucscId = ucscId;
            return this;
        }

        public Builder withdrawn(boolean withdrawn) {
            this.withdrawn = withdrawn;
            return this;
        }

        public HumanGeneIdentifier build() {
            return new HumanGeneIdentifier(this);
        }
    }
}
