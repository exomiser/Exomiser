/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ReviewStatus;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ClinVarAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarAlleleParser.class);

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        for (Allele allele : alleles) {
            ClinVarData clinVarData = parseClinVarData(allele, info);
            if (!clinVarData.isEmpty()) {
                allele.setClinVarData(clinVarData);
            }
            logger.debug("{}", allele);
        }
        return alleles;
    }

    /**
     * @param info
     * @return
     */
    private ClinVarData parseClinVarData(Allele allele, String info) {
//        ##INFO=<ID=ALLELEID,Number=1,Type=Integer,Description="the ClinVar Allele ID"> - get to the web record using: https://www.ncbi.nlm.nih.gov/clinvar/?term=99222[alleleid]
//        ##INFO=<ID=CLNSIG,Number=.,Type=String,Description="Clinical significance for this single variant">
//        ##INFO=<ID=CLNSIGINCL,Number=.,Type=String,Description="Clinical significance for a haplotype or genotype that includes this variant. Reported as pairs of VariationID:clinical significance.">

        ClinVarData.Builder clinVarBuilder = ClinVarData.builder();
        clinVarBuilder.variationId(allele.getRsId());
        String[] fields = info.split(";");
        for (String field : fields) {
            String[] keyValue = field.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            switch (key) {
                case "CLNSIG":
                    String[] clinsigs = value.split(",_");
                    ClinSig primary = parseClinSig(clinsigs[0]);
                    Set<ClinSig> secondary = parseSecondaryClinSig(clinsigs);
                    clinVarBuilder.primaryInterpretation(primary);
                    clinVarBuilder.secondaryInterpretations(secondary);
                    break;
                case "CLNREVSTAT":
                    //CLNREVSTAT criteria_provided,_conflicting_interpretations, criteria_provided,_multiple_submitters,_no_conflicts, criteria_provided,_single_submitter, no_assertion_criteria_provided, no_assertion_provided, no_interpretation_for_the_single_variant, practice_guideline, reviewed_by_expert_panel
                    //CLNREVSTAT counts: criteria_provided,_conflicting_interpretations=12678, criteria_provided,_multiple_submitters,_no_conflicts=34967, criteria_provided,_single_submitter=197277, no_assertion_criteria_provided=34308, no_assertion_provided=10980, no_interpretation_for_the_single_variant=500, practice_guideline=23, reviewed_by_expert_panel=8786
                    clinVarBuilder.reviewStatus(ReviewStatus.parseReviewStatus(value));
                    break;
                case "CLNSIGINCL":
                    Map<String, ClinVarData.ClinSig> includedAlleles = parseIncludedAlleles(value);
                    clinVarBuilder.includedAlleles(includedAlleles);
                    break;
                case "CLNSIGCONF":
                    Map<ClinSig, Integer> clnSigConf = parseClnSigConf(value);
                    clinVarBuilder.conflictingInterpretationCounts(clnSigConf);
                    break;
                case "RS":
                    // Clinvar use their variation ID in the ID field and indicate the rsID in the RS INFO field.
                    allele.setRsId(value);
                    break;
                default:
                    break;
            }
        }
        return clinVarBuilder.build();
    }

    private Map<String, ClinSig> parseIncludedAlleles(String value) {
        //15127:other|15128:other|15334:Pathogenic|
        Map<String, ClinSig> includedAlleles = new HashMap<>();
        String[] incls = value.split("\\|");
        for (String inc : incls) {
            String[] fields = inc.split(":");
            if (fields.length == 2) {
                includedAlleles.put(fields[0], parseClinSig(fields[1]));
            }
        }
        return includedAlleles;
    }

    private Set<ClinSig> parseSecondaryClinSig(String[] clinsigs) {
        if (clinsigs.length > 1) {
            Set<ClinSig> secondaryClinSigs = EnumSet.noneOf(ClinSig.class);
            for (int i = 1; i < clinsigs.length; i++) {
                secondaryClinSigs.add(parseClinSig(clinsigs[i]));
            }
            return secondaryClinSigs;
        }
        return Collections.emptySet();
    }

    private Map<ClinSig, Integer> parseClnSigConf(String value) {
        // CLNSIGCONF=Pathogenic(1)|Uncertain_significance(2)
        // CLNSIGCONF=Uncertain_significance(1)|Likely_benign(1)
        Map<ClinSig, Integer> confMap = new LinkedHashMap<>();
        String[] categories = value.split("\\|");
        for (String category : categories) {
            int openParenIndex = category.indexOf("(");
            ClinSig clinSig = parseClinSig(category.substring(0, openParenIndex));
            int count = Integer.parseInt(category.substring(openParenIndex + 1, category.length() - 1));
            confMap.put(clinSig, count);
        }
        return confMap;
    }


    private ClinSig parseClinSig(String clinsig) {
        // Unique CLNSIG counts
        // Affects=100, Benign=23963, Benign/Likely_benign=10827, Conflicting_interpretations_of_pathogenicity=12784,
        // Likely_benign=52064, Likely_pathogenic=15127, Pathogenic=46803, Pathogenic/Likely_pathogenic=3278,
        // Uncertain_significance=120418, association=148, drug_response=290, not_provided=10980, other=1796, protective=30,
        // risk_factor=411
        return switch (clinsig) {
            case "Uncertain_significance" -> UNCERTAIN_SIGNIFICANCE;
            case "Benign" -> BENIGN;
            case "Benign/Likely_benign" -> BENIGN_OR_LIKELY_BENIGN;
            case "Likely_benign" -> LIKELY_BENIGN;
            case "Conflicting_interpretations_of_pathogenicity" -> CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case "Likely_pathogenic" -> LIKELY_PATHOGENIC;
            case "Pathogenic/Likely_pathogenic" -> PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case "Pathogenic" -> PATHOGENIC;
            case "Affects" -> AFFECTS;
            case "association" -> ASSOCIATION;
            case "drug_response" -> DRUG_RESPONSE;
            case "other" -> OTHER;
            case "protective" -> PROTECTIVE;
            case "risk_factor" -> RISK_FACTOR;
            default -> NOT_PROVIDED;
        };
    }

}
