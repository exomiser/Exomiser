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

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.Evidence.*;
import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.Impact.BENIGN;
import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.Impact.PATHOGENIC;

/**
 * @since 13.1.0
 */
public enum AcmgCriterion {

    // PATHOGENIC - Table 3 of https://www.acmg.net/docs/Standards_Guidelines_for_the_Interpretation_of_Sequence_Variants.pdf
    PVS1(PATHOGENIC, VERY_STRONG, "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"),

    PS1(PATHOGENIC, STRONG, "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"),
    PS2(PATHOGENIC, STRONG, "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"),
    PS3(PATHOGENIC, STRONG, "Well-established in vitro or in vivo functional studies supportive of a damaging effect on the gene or gene product"),
    PS4(PATHOGENIC, STRONG, "The prevalence of the variant in affected individuals is significantly increased compared with the prevalence in controls"),

    PM1(PATHOGENIC, MODERATE, "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"),
    PM2(PATHOGENIC, MODERATE, "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"),
    PM3(PATHOGENIC, MODERATE, "For recessive disorders, detected in trans with a pathogenic variant"),
    PM4(PATHOGENIC, MODERATE, "Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants"),
    PM5(PATHOGENIC, MODERATE, "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"),
    PM6(PATHOGENIC, MODERATE, "Assumed de novo, but without confirmation of paternity and maternity"),

    PP1(PATHOGENIC, SUPPORTING, "Cosegregation with disease in multiple affected family members in a gene definitively known to cause the disease"),
    PP2(PATHOGENIC, SUPPORTING, "Missense variant in a gene that has a low rate of benign missense variation and in which missense variants are a common mechanism of disease"),
    PP3(PATHOGENIC, SUPPORTING, "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"),
    PP4(PATHOGENIC, SUPPORTING, "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"),
    PP5(PATHOGENIC, SUPPORTING, "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"),

    // BENIGN - Table 4 of https://www.acmg.net/docs/Standards_Guidelines_for_the_Interpretation_of_Sequence_Variants.pdf
    BA1(BENIGN, STAND_ALONE, "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"),

    BS1(BENIGN, STRONG, "Allele frequency is greater than expected for disorder"),
    BS2(BENIGN, STRONG, "Observed in a healthy adult individual for a recessive (homozygous), dominant (heterozygous), or X-linked (hemizygous) disorder, with full penetrance expected at an early age"),
    BS3(BENIGN, STRONG, "Well-established in vitro or in vivo functional studies show no damaging effect on protein function or splicing"),
    BS4(BENIGN, STRONG, "Lack of segregation in affected members of a family"),

    BP1(BENIGN, SUPPORTING, "Missense variant in a gene for which primarily truncating variants are known to cause disease"),
    BP2(BENIGN, SUPPORTING, "Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder or observed in cis with a pathogenic variant in any inheritance pattern"),
    BP3(BENIGN, SUPPORTING, "In-frame deletions/insertions in a repetitive region without a known function"),
    BP4(BENIGN, SUPPORTING, "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"),
    BP5(BENIGN, SUPPORTING, "Variant found in a case with an alternate molecular basis for disease"),
    BP6(BENIGN, SUPPORTING, "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"),
    BP7(BENIGN, SUPPORTING, "A synonymous (silent) variant for which splicing prediction algorithms predict no impact to the splice consensus sequence nor the creation of a new splice site AND the nucleotide is not highly conserved");

    public enum Impact {
        PATHOGENIC, BENIGN;
    }

    public enum Evidence {
        STAND_ALONE, VERY_STRONG, STRONG, MODERATE, SUPPORTING;

        public static Evidence parseValue(String displayString) {
            try {
                return Evidence.valueOf(displayString);
            } catch (IllegalArgumentException ex) {
                // swallow and try the display string
            }
            return switch (displayString) {
                case "StandAlone" -> STAND_ALONE;
                case "VeryStrong" -> VERY_STRONG;
                case "Strong" -> STRONG;
                case "Moderate" -> MODERATE;
                case "Supporting" -> SUPPORTING;
                default -> throw new IllegalArgumentException("Unrecognised evidence value '" + displayString + "'");
            };
        }

        public String displayString() {
            return switch (this) {
                case STAND_ALONE -> "StandAlone";
                case VERY_STRONG -> "VeryStrong";
                case STRONG -> "Strong";
                case MODERATE -> "Moderate";
                case SUPPORTING -> "Supporting";
            };
        }
    }

    private final Impact impact;
    private final Evidence evidence;
    private final String description;

    AcmgCriterion(Impact impact, Evidence evidence, String description) {
        this.impact = impact;
        this.evidence = evidence;
        this.description = description;
    }

    public Impact impact() {
        return impact;
    }

    public boolean isPathogenic() {
        return impact == PATHOGENIC;
    }

    public boolean isBenign() {
        return impact == BENIGN;
    }

    public Evidence evidence() {
        return evidence;
    }

    public String description() {
        return description;
    }

}
