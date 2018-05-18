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

import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleConverter {

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
        String updatedRsId = (originalProperties.getRsId()
                .isEmpty()) ? properties.getRsId() : originalProperties.getRsId();
        return AlleleProperties.newBuilder()
                .mergeFrom(originalProperties)
                .mergeFrom(properties)
                //original rsid would have been overwritten by the new one - we don't necessarily want that, so re-set it now.
                .setRsId(updatedRsId)
                .build();
    }

    public static AlleleProperties toAlleleProperties(Allele allele) {
        AlleleProperties.Builder builder = AlleleProperties.newBuilder();
        builder.setRsId(allele.getRsId());
        addAllelePropertyValues(builder, allele.getValues());
        addClinVarData(builder, allele);
        return builder.build();
    }

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
        builder.setAlleleId(clinVarData.getAlleleId());
        builder.setPrimaryInterpretation(toProtoClinSig(clinVarData.getPrimaryInterpretation()));
        for (ClinVarData.ClinSig clinSig : clinVarData.getSecondaryInterpretations()) {
            builder.addSecondaryInterpretations(toProtoClinSig(clinSig));
        }
        builder.setReviewStatus(clinVarData.getReviewStatus());
        for (Map.Entry<String, ClinVarData.ClinSig> entry : clinVarData.getIncludedAlleles().entrySet()) {
            builder.putIncludedAlleles(entry.getKey(), toProtoClinSig(entry.getValue()));
        }
        return builder.build();
    }

    private static ClinVar.ClinSig toProtoClinSig(ClinVarData.ClinSig clinSig) {
        switch (clinSig){
            case BENIGN:
                return ClinVar.ClinSig.BENIGN;
            case BENIGN_OR_LIKELY_BENIGN:
                return ClinVar.ClinSig.BENIGN_OR_LIKELY_BENIGN;
            case LIKELY_BENIGN:
                return ClinVar.ClinSig.LIKELY_BENIGN;
            case UNCERTAIN_SIGNIFICANCE:
                return ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE;
            case LIKELY_PATHOGENIC:
                return ClinVar.ClinSig.LIKELY_PATHOGENIC;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC:
                return ClinVar.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case PATHOGENIC:
                return ClinVar.ClinSig.PATHOGENIC;
            case CONFLICTING_PATHOGENICITY_INTERPRETATIONS:
                return ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case AFFECTS:
                return ClinVar.ClinSig.AFFECTS;
            case ASSOCIATION:
                return ClinVar.ClinSig.ASSOCIATION;
            case DRUG_RESPONSE:
                return ClinVar.ClinSig.DRUG_RESPONSE;
            case NOT_PROVIDED:
                return ClinVar.ClinSig.NOT_PROVIDED;
            case OTHER:
                return ClinVar.ClinSig.OTHER;
            case PROTECTIVE:
                return ClinVar.ClinSig.PROTECTIVE;
            case RISK_FACTOR:
                return ClinVar.ClinSig.RISK_FACTOR;
        }
        throw new IllegalArgumentException(clinSig + " not a recognised value");
    }

}
