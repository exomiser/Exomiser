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

import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class AlleleProtoAdaptor {

    private static final Map<String, FrequencySource> FREQUENCY_SOURCE_MAP = FrequencySource.FREQUENCY_SOURCE_MAP;

    private AlleleProtoAdaptor() {
        //un-instantiable utility class
    }

    public static FrequencyData toFrequencyData(AlleleProperties alleleProperties) {
        if (alleleProperties.equals(AlleleProperties.getDefaultInstance())) {
            return FrequencyData.empty();
        }
        RsId rsId = RsId.valueOf(alleleProperties.getRsId());
        List<Frequency> frequencies = parseFrequencyData(alleleProperties.getPropertiesMap());
        return FrequencyData.of(rsId, frequencies);
    }

    private static List<Frequency> parseFrequencyData(Map<String, Float> values) {
        List<Frequency> frequencies = new ArrayList<>(values.size());
        for (Map.Entry<String, Float> field : values.entrySet()) {
            String key = field.getKey();
            if (FREQUENCY_SOURCE_MAP.containsKey(key)) {
                float value = field.getValue();
                FrequencySource source = FREQUENCY_SOURCE_MAP.get(key);
                frequencies.add(Frequency.valueOf(value, source));
            }
        }
        return frequencies;
    }

    public static PathogenicityData toPathogenicityData(AlleleProperties alleleProperties) {
        if (alleleProperties.equals(AlleleProperties.getDefaultInstance())) {
            return PathogenicityData.empty();
        }
        List<PathogenicityScore> pathogenicityScores = parsePathogenicityData(alleleProperties.getPropertiesMap());
        ClinVarData clinVarData = parseClinVarData(alleleProperties.getClinVar());
        return PathogenicityData.of(clinVarData, pathogenicityScores);
    }

    private static List<PathogenicityScore> parsePathogenicityData(Map<String, Float> values) {
        List<PathogenicityScore> pathogenicityScores = new ArrayList<>(values.size());
        for (Map.Entry<String, Float> field : values.entrySet()) {
            String key = field.getKey();
            if (key.startsWith("SIFT")) {
                float value = field.getValue();
                pathogenicityScores.add(SiftScore.valueOf(value));
            }
            if (key.startsWith("POLYPHEN")) {
                float value = field.getValue();
                pathogenicityScores.add(PolyPhenScore.valueOf(value));
            }
            if (key.startsWith("MUT_TASTER")) {
                float value = field.getValue();
                pathogenicityScores.add(MutationTasterScore.valueOf(value));
            }
            if (key.startsWith("REMM")) {
                float value = field.getValue();
                pathogenicityScores.add(RemmScore.valueOf(value));
            }
            if (key.startsWith("CADD")) {
                float value = field.getValue();
                pathogenicityScores.add(CaddScore.valueOf(value));
            }
        }
        return pathogenicityScores;
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
        switch (protoClinSig) {
            case BENIGN:
                return ClinVarData.ClinSig.BENIGN;
            case BENIGN_OR_LIKELY_BENIGN:
                return ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN;
            case LIKELY_BENIGN:
                return ClinVarData.ClinSig.LIKELY_BENIGN;
            case UNCERTAIN_SIGNIFICANCE:
                return ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE;
            case LIKELY_PATHOGENIC:
                return ClinVarData.ClinSig.LIKELY_PATHOGENIC;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC:
                return ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case PATHOGENIC:
                return ClinVarData.ClinSig.PATHOGENIC;
            case CONFLICTING_PATHOGENICITY_INTERPRETATIONS:
                return ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case AFFECTS:
                return ClinVarData.ClinSig.AFFECTS;
            case ASSOCIATION:
                return ClinVarData.ClinSig.ASSOCIATION;
            case DRUG_RESPONSE:
                return ClinVarData.ClinSig.DRUG_RESPONSE;
            case OTHER:
                return ClinVarData.ClinSig.OTHER;
            case PROTECTIVE:
                return ClinVarData.ClinSig.PROTECTIVE;
            case RISK_FACTOR:
                return ClinVarData.ClinSig.RISK_FACTOR;
            case NOT_PROVIDED:
            case UNRECOGNIZED:
            default:
                return ClinVarData.ClinSig.NOT_PROVIDED;
        }
    }
}
