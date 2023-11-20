package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 14.0.0
 */
public class ClinVarDaoMvStore implements ClinVarDao {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarDaoMvStore.class);

    private final MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap;

    public ClinVarDaoMvStore(MVStore mvStore) {
        clinVarMap = MvStoreUtil.openClinVarMVMap(mvStore);
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull GenomicVariant variant) {
        AlleleProto.AlleleKey alleleKey = AlleleProtoAdaptor.toAlleleKey(variant);
        return getClinVarData(alleleKey);
    }

    private ClinVarData getClinVarData(AlleleProto.AlleleKey alleleKey) {
        AlleleProto.ClinVar clinVar = clinVarMap.get(alleleKey);
        return clinVar == null ? ClinVarData.empty() : AlleleProtoAdaptor.toClinVarData(clinVar);
    }

    @Override
    public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(GenomicInterval genomicInterval) {
        Contig contig = genomicInterval.contig();

        int chr = genomicInterval.contigId();
        int start = genomicInterval.start();
        int end = genomicInterval.end();

        // build Allele keys for map and define bounds
        AlleleProto.AlleleKey lowerBound = AlleleProto.AlleleKey.newBuilder()
                .setChr(chr)
                .setPosition(start)
                .build();
        AlleleProto.AlleleKey upperBound = AlleleProto.AlleleKey.newBuilder()
                .setChr(chr)
                .setPosition(end)
                .build();

        AlleleProto.AlleleKey floorKey = clinVarMap.floorKey(lowerBound);
        if (floorKey != null && floorKey.getPosition() < lowerBound.getPosition()) {
            lowerBound = floorKey;
        }

        Map<GenomicVariant, ClinVarData> results = new LinkedHashMap<>();

        Iterator<AlleleProto.AlleleKey> keyIterator = clinVarMap.keyIterator(lowerBound);
        while (keyIterator.hasNext()) {
            AlleleProto.AlleleKey ak = keyIterator.next();
            // don't process keys out of the initial boundaries
            if (ak.getPosition() >= start && ak.getPosition() <= end) {
                GenomicVariant gvFromAk = alleleKeyToGenomicVariant(ak, contig, genomicInterval.coordinateSystem());
                ClinVarData cvData = getClinVarData(ak);
                results.put(gvFromAk, cvData);
            }
            if (ak.getPosition() > upperBound.getPosition()) {
                break;
            }
        }
        return results;
    }

    private GenomicVariant alleleKeyToGenomicVariant(AlleleProto.AlleleKey alleleKey, Contig contig, CoordinateSystem coordinateSystem) {
        return GenomicVariant.builder()
                .variant(contig, Strand.POSITIVE, Coordinates.ofAllele(coordinateSystem, alleleKey.getPosition(), alleleKey.getRef()), alleleKey.getRef(), alleleKey.getAlt()).build();
    }

}
