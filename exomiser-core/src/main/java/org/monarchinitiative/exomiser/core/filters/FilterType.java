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

package org.monarchinitiative.exomiser.core.filters;

/**
 * This is a simple class of enumerated constants that describe the type of
 * filtering that was applied to a Gene/Variant.
 * *
 * @author Peter Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum FilterType {

    FAILED_VARIANT_FILTER("filter", "VCF Quality Filter"),
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
    PRIORITY_SCORE_FILTER("gene-priority", "Gene priority score"),
    GENE_BLACKLIST_FILTER("gene-blacklist", "Gene blacklist");

    private final String vcfValue;
    private final String stringValue;

    FilterType(String vcfValue, String stringValue) {
        this.vcfValue = vcfValue;
        this.stringValue = stringValue;
    }

    public String vcfValue() {
        return vcfValue;
    }

    public String shortName() {
        return stringValue;
    }

}
