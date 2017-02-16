package org.monarchinitiative.exomiser.core.filters;

/**
 * This is a simple class of enumerated constants that describe the type of
 * filtering that was applied to a Gene/Variant.
 * *
 * @author Peter Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum FilterType {

    FAILED_VARIANT_FILTER("filter", "Failed previous VCF filters"),
    QUALITY_FILTER("quality", "Quality"),
    INTERVAL_FILTER("interval", "Interval"),
    ENTREZ_GENE_ID_FILTER("gene-id", "Gene id"),
    PATHOGENICITY_FILTER("path", "Pathogenicity"),
    REGULATORY_FEATURE_FILTER("reg-feat", "Regulatory feature"),
    FREQUENCY_FILTER("freq", "Frequency"),
    KNOWN_VARIANT_FILTER("known-var", "Known variant"),
    VARIANT_EFFECT_FILTER("var-effect", "Variant effect"),
    INHERITANCE_FILTER("inheritance", "Inheritance"),
    BED_FILTER("bed", "Gene panel target region (Bed)"),
    PRIORITY_SCORE_FILTER("gene-priority", "Gene priority score");

    private final String vcfValue;
    private final String stringValue;

    FilterType(String vcfValue, String stringValue) {
        this.vcfValue = vcfValue;
        this.stringValue = stringValue;
    }

    public String toVcfValue() {
        return vcfValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
