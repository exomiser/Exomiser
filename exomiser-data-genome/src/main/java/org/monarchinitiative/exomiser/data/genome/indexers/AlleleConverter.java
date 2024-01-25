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

package org.monarchinitiative.exomiser.data.genome.indexers;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleConverter {

    private static final Logger logger = LoggerFactory.getLogger(AlleleConverter.class);

    private AlleleConverter() {
        //static utility class
    }

    public static AlleleKey toAlleleKey(Allele allele) {
        return AlleleKey.newBuilder()
                .setChr(allele.getChr())
                .setPosition(allele.getPos())
                .setRef(allele.getRef())
                .setAlt(allele.getAlt())
                .build();
    }

    public static AlleleProperties mergeProperties(AlleleProperties originalProperties, AlleleProperties properties) {
        if (originalProperties.equals(properties)) {
            return originalProperties;
        }
        logger.debug("Merging {} with {}", originalProperties, properties);
        //original rsid would have been overwritten by the new one - we don't necessarily want that, so re-set it now.
        String updatedRsId = originalProperties.getRsId().isEmpty() ? properties.getRsId() : originalProperties.getRsId();

        // unfortunately since changing from a map to a list-based representation of the frequencies and pathogenicity scores
        // this is more manual than simply calling .mergeFrom(originalProperties) / .mergeFrom(properties) as these will
        // append the lists resulting in possible duplicates, hence we're merging them manually to avoid duplicates and
        // overwrite any existing values with the newer version for that source.
        Collection<AlleleProto.Frequency> mergedFrequencies = mergeFrequencies(originalProperties.getFrequenciesList(), properties.getFrequenciesList());
        Collection<AlleleProto.PathogenicityScore> mergedPathScores = mergePathScores(originalProperties.getPathogenicityScoresList(), properties.getPathogenicityScoresList());

        AlleleProperties.Builder mergedProperties = originalProperties.toBuilder();
        if (!mergedProperties.hasClinVar() && properties.hasClinVar()) {
            mergedProperties.setClinVar(properties.getClinVar());
        }
        return mergedProperties
                .clearFrequencies()
                .addAllFrequencies(mergedFrequencies)
                .clearPathogenicityScores()
                .addAllPathogenicityScores(mergedPathScores)
                .setRsId(updatedRsId)
                .build();
    }

    private static Collection<AlleleProto.PathogenicityScore> mergePathScores(List<AlleleProto.PathogenicityScore> originalPathScores, List<AlleleProto.PathogenicityScore> currentPathScores) {
        if (originalPathScores.isEmpty()) {
            return currentPathScores;
        }
        Map<AlleleProto.PathogenicitySource, AlleleProto.PathogenicityScore> mergedPaths = new EnumMap<>(AlleleProto.PathogenicitySource.class);
        mergePaths(originalPathScores, mergedPaths);
        mergePaths(currentPathScores, mergedPaths);
        return mergedPaths.values();
    }

    private static void mergePaths(List<AlleleProto.PathogenicityScore> originalPathScores, Map<AlleleProto.PathogenicitySource, AlleleProto.PathogenicityScore> mergedPaths) {
        for (int i = 0; i < originalPathScores.size(); i++) {
            var pathScore = originalPathScores.get(i);
            mergedPaths.put(pathScore.getPathogenicitySource(), pathScore);
        }
    }

    private static Collection<AlleleProto.Frequency> mergeFrequencies(List<AlleleProto.Frequency> originalFreqs, List<AlleleProto.Frequency> currentFreqs) {
        if (originalFreqs.isEmpty()) {
            return currentFreqs;
        }
        Map<AlleleProto.FrequencySource, AlleleProto.Frequency> mergedFreqs = new EnumMap<>(AlleleProto.FrequencySource.class);
        mergeFreqs(originalFreqs, mergedFreqs);
        mergeFreqs(currentFreqs, mergedFreqs);
        return mergedFreqs.values();
    }

    private static void mergeFreqs(List<AlleleProto.Frequency> originalFreqs, Map<AlleleProto.FrequencySource, AlleleProto.Frequency> mergedFreqs) {
        for (int i = 0; i < originalFreqs.size(); i++) {
            var freq = originalFreqs.get(i);
            mergedFreqs.put(freq.getFrequencySource(), freq);
        }
    }

    public static AlleleProperties toAlleleProperties(Allele allele) {
        AlleleProperties.Builder builder = AlleleProperties.newBuilder();
        builder.setRsId(allele.getRsId());
        builder.addAllFrequencies(allele.getFrequencies());
        builder.addAllPathogenicityScores(allele.getPathogenicityScores());
        addClinVarData(builder, allele);
        return builder.build();
    }

    @Deprecated(since = "14.0.0")
    private static void addAllelePropertyValues(AlleleProperties.Builder builder, Map<AlleleProperty, Float> values) {
        for (Map.Entry<AlleleProperty, Float> entry : values.entrySet()) {
            builder.putProperties(entry.getKey().toString(), entry.getValue());
        }
    }

    private static void addClinVarData(AlleleProperties.Builder builder, Allele allele) {
        if (allele.hasClinVarData()) {
            ClinVar clinVar = toProtoClinVar(allele.getClinVarData());
            builder.setClinVar(clinVar);
        }
    }

    public static ClinVar toProtoClinVar(ClinVarData clinVarData) {
        ClinVar.Builder builder = ClinVar.newBuilder();
        builder.setVariationId(clinVarData.getVariationId());
        builder.setPrimaryInterpretation(toProtoClinSig(clinVarData.getPrimaryInterpretation()));
        for (Map.Entry<ClinVarData.ClinSig, Integer> entry : clinVarData.getConflictingInterpretationCounts().entrySet()) {
            builder.putClinSigCounts(entry.getKey().toString(), entry.getValue());
        }
        for (ClinVarData.ClinSig clinSig : clinVarData.getSecondaryInterpretations()) {
            builder.addSecondaryInterpretations(toProtoClinSig(clinSig));
        }
        builder.setReviewStatus(toProtoReviewStatus(clinVarData.getReviewStatus()));
        for (Map.Entry<String, ClinVarData.ClinSig> entry : clinVarData.getIncludedAlleles().entrySet()) {
            builder.putIncludedAlleles(entry.getKey(), toProtoClinSig(entry.getValue()));
        }
        builder.setGeneSymbol(clinVarData.getGeneSymbol());
        builder.setVariantEffect(toProtoVariantEffect(clinVarData.getVariantEffect()));
        builder.setHgvsCdna(clinVarData.getHgvsCdna());
        builder.setHgvsProtein(clinVarData.getHgvsProtein());
        return builder.build();
    }

    private static ClinVar.ClinSig toProtoClinSig(ClinVarData.ClinSig clinSig) {
        return switch (clinSig) {
            case BENIGN -> ClinVar.ClinSig.BENIGN;
            case BENIGN_OR_LIKELY_BENIGN -> ClinVar.ClinSig.BENIGN_OR_LIKELY_BENIGN;
            case LIKELY_BENIGN -> ClinVar.ClinSig.LIKELY_BENIGN;
            case UNCERTAIN_SIGNIFICANCE -> ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE;
            case LIKELY_PATHOGENIC -> ClinVar.ClinSig.LIKELY_PATHOGENIC;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC -> ClinVar.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case PATHOGENIC -> ClinVar.ClinSig.PATHOGENIC;
            case CONFLICTING_PATHOGENICITY_INTERPRETATIONS -> ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case AFFECTS -> ClinVar.ClinSig.AFFECTS;
            case ASSOCIATION -> ClinVar.ClinSig.ASSOCIATION;
            case DRUG_RESPONSE -> ClinVar.ClinSig.DRUG_RESPONSE;
            case NOT_PROVIDED -> ClinVar.ClinSig.NOT_PROVIDED;
            case OTHER -> ClinVar.ClinSig.OTHER;
            case PROTECTIVE -> ClinVar.ClinSig.PROTECTIVE;
            case RISK_FACTOR -> ClinVar.ClinSig.RISK_FACTOR;
        };
    }

    private static ClinVar.ReviewStatus toProtoReviewStatus(ClinVarData.ReviewStatus reviewStatus) {
        return switch (reviewStatus) {
            case NO_ASSERTION_PROVIDED -> ClinVar.ReviewStatus.NO_ASSERTION_PROVIDED;
            case NO_ASSERTION_CRITERIA_PROVIDED -> ClinVar.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED;
            case NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT -> ClinVar.ReviewStatus.NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT;
            case CRITERIA_PROVIDED_SINGLE_SUBMITTER -> ClinVar.ReviewStatus.CRITERIA_PROVIDED_SINGLE_SUBMITTER;
            case CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS -> ClinVar.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS;
            case CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS -> ClinVar.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS;
            case REVIEWED_BY_EXPERT_PANEL -> ClinVar.ReviewStatus.REVIEWED_BY_EXPERT_PANEL;
            case PRACTICE_GUIDELINE -> ClinVar.ReviewStatus.PRACTICE_GUIDELINE;
        };
    }

    public static AlleleProto.VariantEffect toProtoVariantEffect(VariantEffect variantEffect) {
        return switch (variantEffect) {
            case CHROMOSOME_NUMBER_VARIATION -> AlleleProto.VariantEffect.CHROMOSOME_NUMBER_VARIATION;
            case TRANSCRIPT_ABLATION -> AlleleProto.VariantEffect.TRANSCRIPT_ABLATION;
            case EXON_LOSS_VARIANT -> AlleleProto.VariantEffect.EXON_LOSS_VARIANT;
            case INVERSION -> AlleleProto.VariantEffect.INVERSION;
            case INSERTION -> AlleleProto.VariantEffect.INSERTION;
            case TRANSLOCATION -> AlleleProto.VariantEffect.TRANSLOCATION;
            case FRAMESHIFT_ELONGATION -> AlleleProto.VariantEffect.FRAMESHIFT_ELONGATION;
            case FRAMESHIFT_TRUNCATION -> AlleleProto.VariantEffect.FRAMESHIFT_TRUNCATION;
            case FRAMESHIFT_VARIANT -> AlleleProto.VariantEffect.FRAMESHIFT_VARIANT;
            case INTERNAL_FEATURE_ELONGATION -> AlleleProto.VariantEffect.INTERNAL_FEATURE_ELONGATION;
            case FEATURE_TRUNCATION -> AlleleProto.VariantEffect.FEATURE_TRUNCATION;
            case TRANSCRIPT_AMPLIFICATION -> AlleleProto.VariantEffect.TRANSCRIPT_AMPLIFICATION;
            case COPY_NUMBER_CHANGE -> AlleleProto.VariantEffect.COPY_NUMBER_CHANGE;
            case MNV -> AlleleProto.VariantEffect.MNV;
            case COMPLEX_SUBSTITUTION -> AlleleProto.VariantEffect.COMPLEX_SUBSTITUTION;
            case STOP_GAINED -> AlleleProto.VariantEffect.STOP_GAINED;
            case STOP_LOST -> AlleleProto.VariantEffect.STOP_LOST;
            case START_LOST -> AlleleProto.VariantEffect.START_LOST;
            case SPLICE_ACCEPTOR_VARIANT -> AlleleProto.VariantEffect.SPLICE_ACCEPTOR_VARIANT;
            case SPLICE_DONOR_VARIANT -> AlleleProto.VariantEffect.SPLICE_DONOR_VARIANT;
            case RARE_AMINO_ACID_VARIANT -> AlleleProto.VariantEffect.RARE_AMINO_ACID_VARIANT;
            // unused marker
            case _SMALLEST_HIGH_IMPACT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case MISSENSE_VARIANT -> AlleleProto.VariantEffect.MISSENSE_VARIANT;
            case INFRAME_INSERTION -> AlleleProto.VariantEffect.INFRAME_INSERTION;
            case DISRUPTIVE_INFRAME_INSERTION -> AlleleProto.VariantEffect.DISRUPTIVE_INFRAME_INSERTION;
            case INFRAME_DELETION -> AlleleProto.VariantEffect.INFRAME_DELETION;
            case DISRUPTIVE_INFRAME_DELETION -> AlleleProto.VariantEffect.DISRUPTIVE_INFRAME_DELETION;
            case FIVE_PRIME_UTR_TRUNCATION -> AlleleProto.VariantEffect.FIVE_PRIME_UTR_TRUNCATION;
            case THREE_PRIME_UTR_TRUNCATION -> AlleleProto.VariantEffect.THREE_PRIME_UTR_TRUNCATION;
            // unused marker
            case _SMALLEST_MODERATE_IMPACT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case SPLICE_REGION_VARIANT -> AlleleProto.VariantEffect.SPLICE_REGION_VARIANT;
            case STOP_RETAINED_VARIANT -> AlleleProto.VariantEffect.STOP_RETAINED_VARIANT;
            case INITIATOR_CODON_VARIANT -> AlleleProto.VariantEffect.INITIATOR_CODON_VARIANT;
            case SYNONYMOUS_VARIANT -> AlleleProto.VariantEffect.SYNONYMOUS_VARIANT;
            case CODING_TRANSCRIPT_INTRON_VARIANT -> AlleleProto.VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT;
            case FIVE_PRIME_UTR_PREMATURE_START_CODON_GAIN_VARIANT -> AlleleProto.VariantEffect.FIVE_PRIME_UTR_PREMATURE_START_CODON_GAIN_VARIANT;
            case FIVE_PRIME_UTR_EXON_VARIANT -> AlleleProto.VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT;
            case THREE_PRIME_UTR_EXON_VARIANT -> AlleleProto.VariantEffect.THREE_PRIME_UTR_EXON_VARIANT;
            case FIVE_PRIME_UTR_INTRON_VARIANT -> AlleleProto.VariantEffect.FIVE_PRIME_UTR_INTRON_VARIANT;
            case THREE_PRIME_UTR_INTRON_VARIANT -> AlleleProto.VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT;
            case NON_CODING_TRANSCRIPT_EXON_VARIANT -> AlleleProto.VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT;
            case NON_CODING_TRANSCRIPT_INTRON_VARIANT -> AlleleProto.VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT;
            // unused marker
            case _SMALLEST_LOW_IMPACT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case DIRECT_TANDEM_DUPLICATION -> AlleleProto.VariantEffect.DIRECT_TANDEM_DUPLICATION;
            case MOBILE_ELEMENT_DELETION -> AlleleProto.VariantEffect.MOBILE_ELEMENT_DELETION;
            case MOBILE_ELEMENT_INSERTION -> AlleleProto.VariantEffect.MOBILE_ELEMENT_INSERTION;
            // unused
            case CUSTOM -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case UPSTREAM_GENE_VARIANT -> AlleleProto.VariantEffect.UPSTREAM_GENE_VARIANT;
            case DOWNSTREAM_GENE_VARIANT -> AlleleProto.VariantEffect.DOWNSTREAM_GENE_VARIANT;
            case INTERGENIC_VARIANT -> AlleleProto.VariantEffect.INTERGENIC_VARIANT;
            case TFBS_ABLATION -> AlleleProto.VariantEffect.TFBS_ABLATION;
            case TFBS_AMPLIFICATION -> AlleleProto.VariantEffect.TFBS_AMPLIFICATION;
            case TF_BINDING_SITE_VARIANT -> AlleleProto.VariantEffect.TF_BINDING_SITE_VARIANT;
            case REGULATORY_REGION_VARIANT -> AlleleProto.VariantEffect.REGULATORY_REGION_VARIANT;
            case REGULATORY_REGION_ABLATION -> AlleleProto.VariantEffect.REGULATORY_REGION_ABLATION;
            case REGULATORY_REGION_AMPLIFICATION -> AlleleProto.VariantEffect.REGULATORY_REGION_AMPLIFICATION;
            case CONSERVED_INTRON_VARIANT -> AlleleProto.VariantEffect.CONSERVED_INTRON_VARIANT;
            case INTRAGENIC_VARIANT -> AlleleProto.VariantEffect.INTRAGENIC_VARIANT;
            case CONSERVED_INTERGENIC_VARIANT -> AlleleProto.VariantEffect.CONSERVED_INTERGENIC_VARIANT;
            case STRUCTURAL_VARIANT -> AlleleProto.VariantEffect.STRUCTURAL_VARIANT;
            case CODING_SEQUENCE_VARIANT -> AlleleProto.VariantEffect.CODING_SEQUENCE_VARIANT;
            case INTRON_VARIANT -> AlleleProto.VariantEffect.INTRON_VARIANT;
            case EXON_VARIANT -> AlleleProto.VariantEffect.EXON_VARIANT;
            case SPLICING_VARIANT -> AlleleProto.VariantEffect.SPLICING_VARIANT;
            case MIRNA -> AlleleProto.VariantEffect.MIRNA;
            // unused
            case GENE_VARIANT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case CODING_TRANSCRIPT_VARIANT -> AlleleProto.VariantEffect.CODING_TRANSCRIPT_VARIANT;
            case NON_CODING_TRANSCRIPT_VARIANT -> AlleleProto.VariantEffect.NON_CODING_TRANSCRIPT_VARIANT;
            // unused
            case TRANSCRIPT_VARIANT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            // unused
            case INTERGENIC_REGION -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            // unused
            case CHROMOSOME -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
            case SEQUENCE_VARIANT -> AlleleProto.VariantEffect.SEQUENCE_VARIANT;
        };
    }

}
