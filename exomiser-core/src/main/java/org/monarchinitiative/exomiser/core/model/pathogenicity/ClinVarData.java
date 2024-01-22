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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.*;

/**
 * Immutable data class representing data from the <a href=https://www.ncbi.nlm.nih.gov/clinvar/>ClinVar resource</a>, with
 * explanation of the data from <a href=https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/>https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/</a>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class ClinVarData {

    private static final ClinVarData EMPTY = new Builder().build();


    public enum ClinSig {
        // ACMG/AMP-based
        PATHOGENIC,
        PATHOGENIC_OR_LIKELY_PATHOGENIC,
        LIKELY_PATHOGENIC,
        UNCERTAIN_SIGNIFICANCE,
        LIKELY_BENIGN,
        BENIGN_OR_LIKELY_BENIGN,
        BENIGN,
        CONFLICTING_PATHOGENICITY_INTERPRETATIONS,
        //Non-ACMG-based
        AFFECTS,
        ASSOCIATION,
        DRUG_RESPONSE,
        NOT_PROVIDED,
        OTHER,
        PROTECTIVE,
        RISK_FACTOR;
    }

    /**
     * Enum for the ClinVar <a href=https://www.ncbi.nlm.nih.gov/clinvar/docs/review_status/>review status</a> field.
     * @since 14.0.0
     */
    public enum ReviewStatus {
        NO_ASSERTION_PROVIDED, // default value
        NO_ASSERTION_CRITERIA_PROVIDED,
        NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT,
        CRITERIA_PROVIDED_SINGLE_SUBMITTER,
        CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS,
        CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS,
        REVIEWED_BY_EXPERT_PANEL,
        PRACTICE_GUIDELINE;

        public int starRating() {
            return switch (this) {
                case CRITERIA_PROVIDED_SINGLE_SUBMITTER, CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS -> 1;
                case CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS -> 2;
                case REVIEWED_BY_EXPERT_PANEL -> 3;
                case PRACTICE_GUIDELINE -> 4;
                default -> 0;
            };
        }

        public static ReviewStatus parseReviewStatus(String reviewStatus) {
            return switch (reviewStatus.replace("_", " ")) {
                case "no assertion criteria provided" -> NO_ASSERTION_CRITERIA_PROVIDED;
                case "no interpretation for the single variant", "no interpretation for the individual variant" ->
                        NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT;
                case "criteria provided, single submitter" -> CRITERIA_PROVIDED_SINGLE_SUBMITTER;
                case "criteria provided, conflicting interpretations" -> CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS;
                case "criteria provided, multiple submitters, no conflicts" ->
                        CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS;
                case "reviewed by expert panel" -> REVIEWED_BY_EXPERT_PANEL;
                case "practice guideline" -> PRACTICE_GUIDELINE;
                default -> NO_ASSERTION_PROVIDED;
            };
        }
    }
    //https://www.ncbi.nlm.nih.gov/clinvar/variation/99222
    private final String variationId;
    private final ClinSig primaryInterpretation;
    private final Map<ClinSig, Integer> conflictingInterpretationCounts;

    private final Set<ClinSig> secondaryInterpretations;
    private final ReviewStatus reviewStatus;
    private final Map<String, ClinSig> includedAlleles;
    private final String geneSymbol;

    private final VariantEffect variantEffect;
    private final String hgvsCdna;

    private final String hgvsProtein;
    // https://www.medschool.umaryland.edu/Genetic_Variant_Interpretation_Tool1.html/
    // BP1, Missense variant in a gene for which primarily truncating variants are known to cause disease
    // BP2, Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder or observed in cis with a pathogenic variant in any inheritance pattern
    // BP6, * Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation
    // BS1, Allele frequency is greater than expected for disorder
    // ---
    // PS1, Same amino acid change as a previously established pathogenic variant regardless of nucleotide change
    // PM5, Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before
    // PP2, Missense variant in a gene that has a low rate of benign missense variation and in which missense variants are a common mechanism of disease
    // PP5, * Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation
    //
    // - needs link to gene, disease and amino acid change on variant. Could run 2*+ through Jannovar and annotation pipeline.
    // Need to collect hets and homs for variant in gene
    // TODO: add GENEINFO (geneSymbol:ncbiGeneId) and
    //  CLNDISDB=MedGen:C0006142,OMIM:114480,Orphanet:ORPHA227535,SNOMED_CT:254843006|MedGen:C0027672,SNOMED_CT:699346009
    //  22	29130684	460820	G	C	.	.	ALLELEID=471879;CLNDISDB=MedGen:C0006142,OMIM:114480,Orphanet:ORPHA227535,SNOMED_CT:254843006|MedGen:C0027672,SNOMED_CT:699346009;CLNDN=Familial_cancer_of_breast|Hereditary_cancer-predisposing_syndrome;CLNHGVS=NC_000022.10:g.29130684G>C;CLNREVSTAT=criteria_provided,_multiple_submitters,_no_conflicts;CLNSIG=Uncertain_significance;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;GENEINFO=CHEK2:11200;MC=SO:0001583|missense_variant,SO:0001623|5_prime_UTR_variant;ORIGIN=1;RS=1456931393
    //  10	123357783	299024	G	T	.	.	AF_TGP=0.01717;ALLELEID=320947;CLNDISDB=Human_Phenotype_Ontology:HP:0001363,MedGen:C1849943|Human_Phenotype_Ontology:HP:0004439,MeSH:D003394,MedGen:C2931196,OMIM:123500,Orphanet:ORPHA207,SNOMED_CT:28861008|MedGen:C0001193,OMIM:101200,Orphanet:ORPHA87,SNOMED_CT:205258009|MedGen:C0175699,OMIM:101400,Orphanet:ORPHA794,SNOMED_CT:83015004|MedGen:C0220658,OMIM:101600,Orphanet:ORPHA710,Orphanet:ORPHA93258,Orphanet:ORPHA93259|MedGen:C0265269,OMIM:149730,Orphanet:ORPHA2363,SNOMED_CT:23817003|MedGen:C0795998,OMIM:123150,Orphanet:ORPHA1540|MedGen:C1852406,OMIM:123790,Orphanet:ORPHA1555|MedGen:CN043619;CLNDN=Craniosynostosis|Crouzon_syndrome|Acrocephalosyndactyly_type_I|Saethre-Chotzen_syndrome|Pfeiffer_syndrome|Levy-Hollister_syndrome|Jackson-Weiss_syndrome|Cutis_Gyrata_syndrome_of_Beare_and_Stevenson|Isolated_coronal_synostosis;CLNHGVS=NC_000010.10:g.123357783G>T;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Likely_benign;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=Illumina_Clinical_Services_Laboratory,Illumina:288098;GENEINFO=FGFR2:2263;MC=SO:0001619|non-coding_transcript_variant,SO:0001623|5_prime_UTR_variant;ORIGIN=1;RS=41301043
    //  ##fileformat=VCFv4.1
    //##fileDate=2020-03-02
    //##source=ClinVar
    //##reference=GRCh37
    //##ID=<Description="ClinVar Variation ID">
    //##INFO=<ID=AF_ESP,Number=1,Type=Float,Description="allele frequencies from GO-ESP">
    //##INFO=<ID=AF_EXAC,Number=1,Type=Float,Description="allele frequencies from ExAC">
    //##INFO=<ID=AF_TGP,Number=1,Type=Float,Description="allele frequencies from TGP">
    //##INFO=<ID=ALLELEID,Number=1,Type=Integer,Description="the ClinVar Allele ID">
    //##INFO=<ID=CLNDN,Number=.,Type=String,Description="ClinVar's preferred disease name for the concept specified by disease identifiers in CLNDISDB">
    //##INFO=<ID=CLNDNINCL,Number=.,Type=String,Description="For included Variant : ClinVar's preferred disease name for the concept specified by disease identifiers in CLNDISDB">
    //##INFO=<ID=CLNDISDB,Number=.,Type=String,Description="Tag-value pairs of disease database name and identifier, e.g. OMIM:NNNNNN">
    //##INFO=<ID=CLNDISDBINCL,Number=.,Type=String,Description="For included Variant: Tag-value pairs of disease database name and identifier, e.g. OMIM:NNNNNN">
    //##INFO=<ID=CLNHGVS,Number=.,Type=String,Description="Top-level (primary assembly, alt, or patch) HGVS expression.">
    //##INFO=<ID=CLNREVSTAT,Number=.,Type=String,Description="ClinVar review status for the Variation ID">
    //##INFO=<ID=CLNSIG,Number=.,Type=String,Description="Clinical significance for this single variant">
    //##INFO=<ID=CLNSIGCONF,Number=.,Type=String,Description="Conflicting clinical significance for this single variant">
    //##INFO=<ID=CLNSIGINCL,Number=.,Type=String,Description="Clinical significance for a haplotype or genotype that includes this variant. Reported as pairs of VariationID:clinical significance.">
    //##INFO=<ID=CLNVC,Number=1,Type=String,Description="Variant type">
    //##INFO=<ID=CLNVCSO,Number=1,Type=String,Description="Sequence Ontology id for variant type">
    //##INFO=<ID=CLNVI,Number=.,Type=String,Description="the variant's clinical sources reported as tag-value pairs of database and variant identifier">
    //##INFO=<ID=DBVARID,Number=.,Type=String,Description="nsv accessions from dbVar for the variant">
    //##INFO=<ID=GENEINFO,Number=1,Type=String,Description="Gene(s) for the variant reported as gene symbol:gene id. The gene symbol and id are delimited by a colon (:) and each pair is delimited by a vertical bar (|)">
    //##INFO=<ID=MC,Number=.,Type=String,Description="comma separated list of molecular consequence in the form of Sequence Ontology ID|molecular_consequence">
    //##INFO=<ID=ORIGIN,Number=.,Type=String,Description="Allele origin. One or more of the following values may be added: 0 - unknown; 1 - germline; 2 - somatic; 4 - inherited; 8 - paternal; 16 - maternal; 32 - de-novo; 64 - biparental; 128 - uniparental; 256 - not-tested; 512 - tested-inconclusive; 1073741824 - other">
    //##INFO=<ID=RS,Number=.,Type=String,Description="dbSNP ID (i.e. rs number)">

    //##INFO=<ID=SSR,Number=1,Type=Integer,Description="Variant Suspect Reason Codes. One or more of the following values may be added: 0 - unspecified, 1 - Paralog, 2 - byEST, 4 - oldAlign, 8 - Para_EST, 16 - 1kg_failed, 1024 - other">

    private ClinVarData(Builder builder) {
        this.variationId = builder.variationId;
        this.primaryInterpretation = builder.primaryInterpretation;
        Map<ClinSig, Integer> map = new EnumMap<>(ClinSig.class);
        map.putAll(builder.conflictingInterpretationCounts);
        this.conflictingInterpretationCounts = Collections.unmodifiableMap(map);
        this.secondaryInterpretations = Collections.unmodifiableSet(builder.secondaryInterpretations);
        this.reviewStatus = builder.reviewStatus;
        this.includedAlleles = Collections.unmodifiableMap(builder.includedAlleles);
        this.geneSymbol = builder.geneSymbol;
        this.variantEffect = builder.variantEffect;
        this.hgvsCdna = builder.hgvsCdna;
        this.hgvsProtein = builder.hgvsProtein;
    }

    public static ClinVarData empty() {
        return EMPTY;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public String getVariationId() {
        return variationId;
    }

    public ClinSig getPrimaryInterpretation() {
        return primaryInterpretation;
    }

    public Map<ClinSig, Integer> getConflictingInterpretationCounts() {
        return conflictingInterpretationCounts;
    }

    public Set<ClinSig> getSecondaryInterpretations() {
        return secondaryInterpretations;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public Map<String, ClinSig> getIncludedAlleles() {
        return includedAlleles;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public VariantEffect getVariantEffect() {
        return variantEffect;
    }

    public String getHgvsCdna() {
        return hgvsCdna;
    }

    public String getHgvsProtein() {
        return hgvsProtein;
    }

    /**
     * @return true if the secondary CLNSIG contains one of 'affects', 'other', 'association', 'risk factor' or
     * 'protective'. These are considered unimportant from the mendelian disease perspective. The category 'drug response'
     * is *not* included here as these are also associated with CFTR alleles known to be pathogenic/likely pathogenic
     * for CF.
     * @since 13.0.0
     */
    @JsonIgnore
    public boolean isSecondaryAssociationRiskFactorOrOther() {
        for (ClinVarData.ClinSig secondaryClinSig : secondaryInterpretations) {
            if (Objects.requireNonNull(secondaryClinSig) == ClinSig.AFFECTS || secondaryClinSig == ClinSig.OTHER || secondaryClinSig == ClinSig.ASSOCIATION || secondaryClinSig == ClinSig.RISK_FACTOR || secondaryClinSig == ClinSig.PROTECTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the <a href=https://www.ncbi.nlm.nih.gov/clinvar/docs/review_status/#revstat_def>ClinVar star rating</a>.
     * In the VCF CLNREVSTAT the star ratings are mapped as follows:
     * <p>
     * <pre>
     * 1* criteria_provided,_conflicting_interpretations
     * 1* criteria_provided,_single_submitter
     * 2* criteria_provided,_multiple_submitters,_no_conflicts
     * 3* reviewed_by_expert_panel
     * 4* practice_guideline
     * </pre>
     *
     * @return an integer value between 0 (worst) and 4 (best)
     * @since 13.0.0
     */
    public int starRating() {
        return reviewStatus.starRating();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClinVarData that = (ClinVarData) o;
        return Objects.equals(variationId, that.variationId) &&
               primaryInterpretation == that.primaryInterpretation &&
               Objects.equals(secondaryInterpretations, that.secondaryInterpretations) &&
               reviewStatus == that.reviewStatus &&
               Objects.equals(includedAlleles, that.includedAlleles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variationId, primaryInterpretation, secondaryInterpretations, reviewStatus, includedAlleles);
    }

    @Override
    public String toString() {
        return "ClinVarData{" +
               "variationId='" + variationId + '\'' +
               ", geneSymbol='" + geneSymbol + '\'' +
               ", hgvsCdna='" + hgvsCdna + '\'' +
               ", hgvsProtein='" + hgvsProtein + '\'' +
               ", variantEffect='" + variantEffect + '\'' +
               ", primaryInterpretation=" + primaryInterpretation +
               (primaryInterpretation == ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS ? ", conflictingInterpretationCounts=" + conflictingInterpretationCounts : "") +
               ", secondaryInterpretations=" + secondaryInterpretations +
               ", reviewStatus=" + reviewStatus +
               ", includedAlleles=" + includedAlleles +
               '}';
    }

    public Builder toBuilder() {
        return new Builder()
                .variationId(variationId)
                .primaryInterpretation(primaryInterpretation)
                .secondaryInterpretations(secondaryInterpretations)
                .reviewStatus(reviewStatus)
                .includedAlleles(includedAlleles)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .hgvsCdna(hgvsCdna)
                .hgvsProtein(hgvsProtein)
                .conflictingInterpretationCounts(conflictingInterpretationCounts)
                ;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String variationId = "";
        private ClinSig primaryInterpretation = ClinSig.NOT_PROVIDED;
        private Set<ClinSig> secondaryInterpretations = EnumSet.noneOf(ClinSig.class);

        private ReviewStatus reviewStatus = ReviewStatus.NO_ASSERTION_PROVIDED;
        private Map<String, ClinSig> includedAlleles = Collections.emptyMap();

        private String geneSymbol = "";
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;

        private String hgvsCdna = "";
        private String hgvsProtein = "";

        private Map<ClinSig, Integer> conflictingInterpretationCounts = new EnumMap<>(ClinSig.class);

        public Builder variationId(String variationId) {
            Objects.requireNonNull(variationId);
            this.variationId = variationId;
            return this;
        }

        public Builder primaryInterpretation(ClinSig primaryInterpretation) {
            this.primaryInterpretation = Objects.requireNonNull(primaryInterpretation);
            return this;
        }

        public Builder secondaryInterpretations(Set<ClinSig> secondaryInterpretations) {
            Objects.requireNonNull(secondaryInterpretations);
            this.secondaryInterpretations = secondaryInterpretations.isEmpty() ? Set.of() : EnumSet.copyOf(secondaryInterpretations);
            return this;
        }

        public Builder reviewStatus(ReviewStatus reviewStatus) {
            this.reviewStatus = Objects.requireNonNull(reviewStatus);
            return this;
        }

        public Builder includedAlleles(Map<String, ClinSig> includedAlleles) {
            this.includedAlleles = Objects.requireNonNull(includedAlleles);
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            this.geneSymbol = Objects.requireNonNull(geneSymbol);
            return this;
        }

        public Builder variantEffect(VariantEffect variantEffect) {
            this.variantEffect = Objects.requireNonNull(variantEffect);
            return this;
        }

        public Builder hgvsCdna(String hgvsCdna) {
            this.hgvsCdna = Objects.requireNonNull(hgvsCdna);
            return this;
        }

        public Builder hgvsProtein(String hgvsProtein) {
            this.hgvsProtein = Objects.requireNonNull(hgvsProtein);
            return this;
        }

        public Builder conflictingInterpretationCounts(Map<ClinSig, Integer> conflictingInterpretationCounts) {
            this.conflictingInterpretationCounts = Objects.requireNonNull(conflictingInterpretationCounts);
            return this;
        }

        public ClinVarData build() {
            return new ClinVarData(this);
        }

    }
}
