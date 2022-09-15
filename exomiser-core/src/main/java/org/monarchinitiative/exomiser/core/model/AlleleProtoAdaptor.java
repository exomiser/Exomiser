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

package org.monarchinitiative.exomiser.core.model;

import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;
import org.monarchinitiative.svart.Variant;

import java.util.*;

import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.CLINVAR;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class AlleleProtoAdaptor {

    private AlleleProtoAdaptor() {
        //un-instantiable utility class
    }

    // This would make sense to have this here rather than having similar functionality in the MvStoreUtil
    // and the VariantKeyGenerator
    public static AlleleKey toAlleleKey(Variant variant) {
        // ARGH! I didn't put the frikking genome assembly in the alleleKey!
        // adding it will probably make the data backwards-incompatible as the MVStore is essentially a TreeMap
        return AlleleKey.newBuilder()
                .setChr(variant.contigId())
                .setPosition(variant.start())
                .setRef(variant.ref())
                .setAlt(variant.alt())
                .build();
    }

    public static FrequencyData toFrequencyData(AlleleProperties alleleProperties) {
        if (alleleProperties.equals(AlleleProperties.getDefaultInstance())) {
            return FrequencyData.empty();
        }
        List<Frequency> frequencies = parseFrequencyData(alleleProperties.getFrequenciesList());
        return FrequencyData.of(alleleProperties.getRsId(), frequencies);
    }

    private static List<Frequency> parseFrequencyData(List<AlleleProto.Frequency> frequenciesList) {
        var frequencies = new ArrayList<Frequency>(frequenciesList.size());
        for (int i = 0; i < frequenciesList.size(); i++) {
            AlleleProto.Frequency frequency = frequenciesList.get(i);
            var freqSource = toFreqSource(frequency.getFrequencySource());
            var freq = percentageFrequency(frequency.getAc(), frequency.getAn());
            // TODO: MAKE FrequencyDataBuilder to prevent useless Frequency object creation
            frequencies.add(Frequency.of(freqSource, freq));
            // TODO! ADD HOM
            var hom = frequency.getHom();
        }
        return frequencies;
    }

    private static float percentageFrequency(int ac, int an) {
        return 100f * (ac / (float) an);
    }

    private static FrequencySource toFreqSource(AlleleProto.FrequencySource frequencySource) {
        return switch (frequencySource) {
            case KG -> THOUSAND_GENOMES;
            case TOPMED -> TOPMED;
            case UK10K -> UK10K;
            case ESP_EA -> ESP_EUROPEAN_AMERICAN;
            case ESP_AA -> ESP_AFRICAN_AMERICAN;
            case ESP_ALL -> ESP_ALL;
            case EXAC_AFR -> EXAC_AFRICAN_INC_AFRICAN_AMERICAN;
            case EXAC_AMR -> EXAC_AMERICAN;
            case EXAC_EAS -> EXAC_EAST_ASIAN;
            case EXAC_FIN -> EXAC_FINNISH;
            case EXAC_NFE -> EXAC_NON_FINNISH_EUROPEAN;
            case EXAC_OTH -> EXAC_OTHER;
            case EXAC_SAS -> EXAC_SOUTH_ASIAN;

            case GNOMAD_E_AFR -> GNOMAD_E_AFR;
            case GNOMAD_E_AMR -> GNOMAD_E_AMR;
            case GNOMAD_E_ASJ -> GNOMAD_E_ASJ;
            case GNOMAD_E_EAS -> GNOMAD_E_EAS;
            case GNOMAD_E_FIN -> GNOMAD_E_FIN;
            case GNOMAD_E_NFE -> GNOMAD_E_NFE;
            case GNOMAD_E_OTH -> GNOMAD_E_OTH;
            case GNOMAD_E_SAS -> GNOMAD_E_SAS;
            // TODO add new gnomad v2/3 populations (MID, AMI)

            case GNOMAD_G_AFR -> GNOMAD_G_AFR;
            case GNOMAD_G_AMR -> GNOMAD_G_AMR;
            case GNOMAD_G_ASJ -> GNOMAD_G_ASJ;
            case GNOMAD_G_EAS -> GNOMAD_G_EAS;
            case GNOMAD_G_FIN -> GNOMAD_G_FIN;
            case GNOMAD_G_NFE -> GNOMAD_G_NFE;
            case GNOMAD_G_OTH -> GNOMAD_G_OTH;
            case UNSPECIFIED_FREQUENCY_SOURCE, UNRECOGNIZED -> UNKNOWN;
        };
    }

    public static PathogenicityData toPathogenicityData(AlleleProperties alleleProperties) {
        if (alleleProperties.equals(AlleleProperties.getDefaultInstance())) {
            return PathogenicityData.empty();
        }
        List<PathogenicityScore> pathogenicityScores = parsePathogenicityData(alleleProperties.getPathogenicityScoresList());
        ClinVarData clinVarData = parseClinVarData(alleleProperties.getClinVar());
        return PathogenicityData.of(clinVarData, pathogenicityScores);
    }

    private static List<PathogenicityScore> parsePathogenicityData(List<AlleleProto.PathogenicityScore> pathogenicityScoresList) {
        List<PathogenicityScore> pathogenicityScores = new ArrayList<>(pathogenicityScoresList.size());
        for (int i = 0; i < pathogenicityScoresList.size(); i++) {
            AlleleProto.PathogenicityScore pathogenicityScore = pathogenicityScoresList.get(i);
            pathogenicityScores.add(PathogenicityScore.of(toPathSource(pathogenicityScore.getPathogenicitySource()), pathogenicityScore.getScore()));
        }
        return pathogenicityScores;
    }

    private static PathogenicitySource toPathSource(AlleleProto.PathogenicitySource pathogenicitySource) {
        return switch (pathogenicitySource) {
            case VARIANT_EFFECT -> VARIANT_TYPE;
            case POLYPHEN -> POLYPHEN;
            case MUTATION_TASTER -> MUTATION_TASTER;
            case SIFT -> SIFT;
            case CADD -> CADD;
            case REMM -> REMM;
            case REVEL -> REVEL;
            case M_CAP -> M_CAP;
            case MPC -> MPC;
            case MVP -> MVP;
            case PRIMATE_AI -> PRIMATE_AI;
            case SPLICE_AI -> SPLICE_AI;
            case TEST -> TEST;
            case DBVAR -> PathogenicitySource.DBVAR;
            case CLINVAR -> CLINVAR;
            case UNRECOGNIZED, UNKNOWN_PATH_SOURCE ->
                    throw new IllegalStateException("Unexpected value: " + pathogenicitySource);
        };
    }

    private static ClinVarData parseClinVarData(ClinVar clinVar) {
        if (clinVar.equals(clinVar.getDefaultInstanceForType())) {
            return ClinVarData.empty();
        }
        ClinVarData.Builder builder = ClinVarData.builder();
        builder.alleleId(clinVar.getAlleleId());
        builder.primaryInterpretation(toClinSig(clinVar.getPrimaryInterpretation()));
        builder.secondaryInterpretations(toClinSigSet(clinVar.getSecondaryInterpretationsList()));
        builder.includedAlleles(getToIncludedAlleles(clinVar.getIncludedAllelesMap()));
        builder.reviewStatus(clinVar.getReviewStatus());
        return builder.build();
    }

    private static Map<String, ClinVarData.ClinSig> getToIncludedAlleles(Map<String, ClinVar.ClinSig> includedAllelesMap) {
        if (includedAllelesMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ClinVarData.ClinSig> converted = new HashMap<>(includedAllelesMap.size());

        for (Map.Entry<String, ClinVar.ClinSig> included : includedAllelesMap.entrySet()) {
            converted.put(included.getKey(), toClinSig(included.getValue()));
        }

        return converted;
    }

    private static Set<ClinVarData.ClinSig> toClinSigSet(List<ClinVar.ClinSig> protoClinSigs) {
        if (protoClinSigs.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ClinVarData.ClinSig> converted = new HashSet<>(protoClinSigs.size());
        for (ClinVar.ClinSig protoClinSig : protoClinSigs) {
            converted.add(toClinSig(protoClinSig));
        }
        return converted;
    }

    private static ClinVarData.ClinSig toClinSig(ClinVar.ClinSig protoClinSig) {
        return switch (protoClinSig) {
            case BENIGN -> ClinVarData.ClinSig.BENIGN;
            case BENIGN_OR_LIKELY_BENIGN -> ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN;
            case LIKELY_BENIGN -> ClinVarData.ClinSig.LIKELY_BENIGN;
            case UNCERTAIN_SIGNIFICANCE -> ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE;
            case LIKELY_PATHOGENIC -> ClinVarData.ClinSig.LIKELY_PATHOGENIC;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC -> ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case PATHOGENIC -> ClinVarData.ClinSig.PATHOGENIC;
            case CONFLICTING_PATHOGENICITY_INTERPRETATIONS -> ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case AFFECTS -> ClinVarData.ClinSig.AFFECTS;
            case ASSOCIATION -> ClinVarData.ClinSig.ASSOCIATION;
            case DRUG_RESPONSE -> ClinVarData.ClinSig.DRUG_RESPONSE;
            case OTHER -> ClinVarData.ClinSig.OTHER;
            case PROTECTIVE -> ClinVarData.ClinSig.PROTECTIVE;
            case RISK_FACTOR -> ClinVarData.ClinSig.RISK_FACTOR;
            case NOT_PROVIDED, UNRECOGNIZED -> ClinVarData.ClinSig.NOT_PROVIDED;
        };
    }
}
