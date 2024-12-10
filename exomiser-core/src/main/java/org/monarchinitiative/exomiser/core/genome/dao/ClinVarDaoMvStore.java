package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.model.GeneStatistics;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS;

/**
 * @since 14.0.0
 */
public class ClinVarDaoMvStore implements ClinVarDao {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarDaoMvStore.class);

    private final MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap;
    private final Map<String, GeneStatistics> geneStats;

    public ClinVarDaoMvStore(MVStore mvStore) {
        clinVarMap = MvStoreUtil.openClinVarMVMap(mvStore);
        geneStats = calculateGeneStatistics(clinVarMap);
    }

    private Map<String, GeneStatistics> calculateGeneStatistics(MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap) {
        Instant start = Instant.now();
        Map<String, GeneStatistics.Builder> geneStatisticsBuilders = new LinkedHashMap<>(1900);
        for (Map.Entry<AlleleProto.AlleleKey, AlleleProto.ClinVar> entry : clinVarMap.entrySet()) {
            AlleleProto.ClinVar clinVar = entry.getValue();
            ClinVarData.ReviewStatus reviewStatus = AlleleProtoAdaptor.toReviewStatus(clinVar.getReviewStatus());
            if (reviewStatus != CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS && reviewStatus.starRating() >= 1) {
                VariantEffect variantEffect = AlleleProtoAdaptor.toVariantEffect(clinVar.getVariantEffect());
                ClinVarData.ClinSig clinSig = AlleleProtoAdaptor.toClinSig(clinVar.getPrimaryInterpretation());
                geneStatisticsBuilders.compute(clinVar.getGeneSymbol(), (k, v) -> (v == null) ? GeneStatistics.builder(k, variantEffect, clinSig) : v.put(variantEffect, clinSig));
            }
        }
        final Map<String, GeneStatistics> stats = new HashMap<>(geneStatisticsBuilders.size());
        geneStatisticsBuilders.forEach((k, v) -> stats.put(k, v.build()));
        Instant end = Instant.now();
        logger.info("Created {} ClinVar gene stats in {} ms", stats.size(), Duration.between(start, end).toMillis());
        return stats;
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull Variant variant) {
        return getClinVarData(variant.alleleKey());
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull GenomicVariant genomicVariant) {
        return getClinVarData(AlleleProtoAdaptor.toAlleleKey(genomicVariant));
    }

    private ClinVarData getClinVarData(@Nonnull AlleleProto.AlleleKey alleleKey) {
        AlleleProto.ClinVar clinVar = clinVarMap.get(alleleKey);
        return clinVar == null ? ClinVarData.empty() : AlleleProtoAdaptor.toClinVarData(clinVar);
    }

    @Override
    public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(@Nonnull GenomicInterval genomicInterval) {
        Contig contig = genomicInterval.contig();

        int chr = genomicInterval.contigId();
        // the ClinVar data is stored using VCF coordinates
        int start = genomicInterval.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ONE_BASED);
        int end = genomicInterval.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ONE_BASED);

        // build Allele keys for map and define bounds
        AlleleProto.AlleleKey lowerBound = AlleleProto.AlleleKey.newBuilder()
                .setChr(chr)
                .setPosition(start)
                .build();

        Map<GenomicVariant, ClinVarData> results = new LinkedHashMap<>();
        AlleleProto.AlleleKey floorKey = clinVarMap.floorKey(lowerBound);
        logger.debug("Searching for: {} from key {}", genomicInterval, (floorKey == null ? null : floorKey.getPosition()));
        // all keys are downstream of the variant
        Iterator<AlleleProto.AlleleKey> keyIterator = clinVarMap.keyIterator(floorKey);
        while (keyIterator.hasNext()) {
            AlleleProto.AlleleKey alleleKey = keyIterator.next();
            if (alleleKey.getPosition() >= start && alleleKey.getPosition() <= end) {
                GenomicVariant variant = alleleKeyToGenomicVariant(alleleKey, contig);
                AlleleProto.ClinVar clinVar = clinVarMap.get(alleleKey);
                if (clinVar != null) {
                    results.put(variant, AlleleProtoAdaptor.toClinVarData(clinVar));
                }
            }
            if (alleleKey.getPosition() > end) {
                break;
            }
        }
        return results;
    }

    @Override
    public GeneStatistics getGeneStatistics(@Nonnull String geneSymbol) {
        return geneStats.getOrDefault(geneSymbol, GeneStatistics.builder(geneSymbol).build());
    }

    private String broadFormat(AlleleProto.AlleleKey alleleKey) {
        return alleleKey.getChr() + "-" + alleleKey.getPosition()  + "-" + alleleKey.getRef() + "-" + alleleKey.getAlt();
    }

    private GenomicVariant alleleKeyToGenomicVariant(AlleleProto.AlleleKey alleleKey, Contig contig) {
        return GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, alleleKey.getPosition(), alleleKey.getRef(), alleleKey.getAlt());
    }

}
