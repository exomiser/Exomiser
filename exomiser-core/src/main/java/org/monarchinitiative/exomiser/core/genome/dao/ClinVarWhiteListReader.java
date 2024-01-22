package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utility class for reading and filtering variants from an {@link MVStore} containing the 'clinvar' map.
 *
 * @since 14.0.0
 */
public class ClinVarWhiteListReader {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarWhiteListReader.class);

    private ClinVarWhiteListReader() {
        // uninstantiable static utility class
    }

    /**
     * Will read the contents of the clinVarMVStore and filter those variants which are reported as pathogenic or
     * likely pathogenic variants with a 1* rating or higher (not including 1* ratings with conflicting assertions).
     *
     * @param clinVarMVStore An {@link MVStore} containing a 'clinvar' map.
     * @return a Set of {@link org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey} for undisputed
     * pathogenic/likely pathogenic germline alleles
     * @since 14.0.0
     */
    public static Set<AlleleProto.AlleleKey> readVariantWhiteList(MVStore clinVarMVStore) {
        // note that it would be nicer to just require a Path to an input file, but there is a constraint that the
        // MVStore can only be opened once by a single JVM so this has to be coordinated elsewhere.
        Objects.requireNonNull(clinVarMVStore);
        logger.info("Reading ClinVar whitelist...");
        Instant start = Instant.now();
        MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> alleleKeyClinVarMVMap = MvStoreUtil.openClinVarMVMap(clinVarMVStore);
        Set<AlleleProto.AlleleKey> whiteListKeys = alleleKeyClinVarMVMap.entrySet().stream()
                .filter(entry -> isWhiteListed(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
        logger.info("Read {} ClinVar whitelist variants in {} ms", whiteListKeys.size(), Duration.between(start, Instant.now()).toMillis());
        return Set.copyOf(whiteListKeys);
    }

    private static boolean isWhiteListed(AlleleProto.ClinVar clinVar) {
        return isPathOrLikelyPath(clinVar) && starRating(clinVar.getReviewStatus()) >= 1;
    }

    /*
     * 2023-07-17 Having looked at more examples of these there are at most a few hundred cases where there are other
     * interpretations provided as well as the primary Pathogenic/Likely_pathogenic/Benign etc. In the case of many
     * pathogenic CF alleles a drug_response interpretation is also provided, but these need to be retained as they are
     * otherwise pathogenic variants with an expert review 3* rating. Others are harder to judge but would either fail
     * due to there being no criteria provided or not the right class, so it is safer to omit this set of criteria. For
     * example VariationID:17967 is a strong 2* Pathogenic variant for OMIM:613490 which also has an associated risk
     * factor.
     */
    private static boolean isSecondaryAssociationRiskFactorOrOther(AlleleProto.ClinVar clinVar) {
        //  zgrep -Eow 'CLNSIG=[A-Z][a-z_|]+' clinvar.vcf.gz | sort | uniq -c
        //  e.g.
        //  152147 CLNSIG=Pathogenic
        //       7 CLNSIG=Pathogenic|association
        //       1 CLNSIG=Pathogenic|association|protective
        //       1 CLNSIG=Pathogenic|confers_sensitivity
        //      55 CLNSIG=Pathogenic|drug_response
        //       1 CLNSIG=Pathogenic|drug_response|other
        //      75 CLNSIG=Pathogenic|other
        //       3 CLNSIG=Pathogenic|protective
        //      25 CLNSIG=Pathogenic|risk_factor
        for (AlleleProto.ClinVar.ClinSig secondaryClinSig : clinVar.getSecondaryInterpretationsList()) {
            if (Objects.requireNonNull(secondaryClinSig) == AlleleProto.ClinVar.ClinSig.AFFECTS ||
                    secondaryClinSig == AlleleProto.ClinVar.ClinSig.OTHER ||
                    secondaryClinSig == AlleleProto.ClinVar.ClinSig.ASSOCIATION ||
                    secondaryClinSig == AlleleProto.ClinVar.ClinSig.RISK_FACTOR ||
                    // DRUG_RESPONSE is found associated with Pathogenic CF alleles, e.g. clinvar variationID:7108,
                    // 48688, 38733 and others. DO NOT include it here.
                    secondaryClinSig == AlleleProto.ClinVar.ClinSig.PROTECTIVE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPathOrLikelyPath(AlleleProto.ClinVar clinVar) {
        return switch (clinVar.getPrimaryInterpretation()) {
            case PATHOGENIC, PATHOGENIC_OR_LIKELY_PATHOGENIC, LIKELY_PATHOGENIC -> true;
            default -> false;
        };
    }

    private static int starRating(AlleleProto.ClinVar.ReviewStatus reviewStatus) {
        return switch (reviewStatus) {
            // ordinarily "CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS" is also a 1 star variant,
            // (see https://www.ncbi.nlm.nih.gov/clinvar/docs/review_status/)
            // but we're going to be more stringent for the whitelist
            case CRITERIA_PROVIDED_SINGLE_SUBMITTER -> 1;
            case CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS -> 2;
            case REVIEWED_BY_EXPERT_PANEL -> 3;
            case PRACTICE_GUIDELINE -> 4;
            default -> 0;
        };
    }
}
