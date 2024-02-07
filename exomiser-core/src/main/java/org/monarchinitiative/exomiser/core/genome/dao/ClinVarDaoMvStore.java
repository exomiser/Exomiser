package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
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
    public ClinVarData getClinVarData(@Nonnull Variant variant) {
        return getClinVarData(variant.alleleKey());
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull GenomicVariant genomicVariant) {
        return getClinVarData(AlleleProtoAdaptor.toAlleleKey(genomicVariant));
    }

    public ClinVarData getClinVarData(@Nonnull AlleleProto.AlleleKey alleleKey) {
        AlleleProto.ClinVar clinVar = clinVarMap.get(alleleKey);
        return clinVar == null ? ClinVarData.empty() : AlleleProtoAdaptor.toClinVarData(clinVar);
    }

    @Override
    public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(GenomicInterval genomicInterval) {
        // TODO: WARNING! This is not fully tested/optimised - DO NOT USE!
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

        AlleleProto.AlleleKey floorKey = clinVarMap.floorKey(lowerBound);
        if (floorKey != null && floorKey.getPosition() < lowerBound.getPosition()) {
            lowerBound = floorKey;
        }

        Map<GenomicVariant, ClinVarData> results = new LinkedHashMap<>();
        System.out.println(genomicInterval);
        System.out.println("From=" + (floorKey != null ?  floorKey.getPosition() : floorKey));
        // all keys are upstream of the variant

        Iterator<AlleleProto.AlleleKey> keyIterator = clinVarMap.keyIterator(floorKey);
        while (keyIterator.hasNext()) {
            AlleleProto.AlleleKey alleleKey = keyIterator.next();
            System.out.println(broadFormat(alleleKey));
            // don't process keys out of the initial boundaries
            // alleleKey end: (alleleKey.getPosition() + alleleKey.getRef().length() - 1) use this or alleleKey.getPosition()?
            if (alleleKey.getPosition() >= start && alleleKey.getPosition() <= end) {
                GenomicVariant variant = alleleKeyToGenomicVariant(alleleKey, contig, genomicInterval.coordinateSystem());
                ClinVarData clinVarData = getClinVarData(alleleKey);
                results.put(variant, clinVarData);
            }
            if (alleleKey.getPosition() > end) {
                break;
            }
        }
        return results;

//        Map<GenomicVariant, ClinVarData> results = new LinkedHashMap<>();
//        AlleleProto.AlleleKey from = clinVarMap.ceilingKey(AlleleProto.AlleleKey.newBuilder().setChr(chr).setPosition(start).setRef("A").build());
//        System.out.println(genomicInterval);
//        System.out.println("From=" + (from != null ?  from.getPosition() : from));
//        if (from == null || from.getPosition() < end) {
//            return results;
//        }
//        // 17-78451256-C-A rs920455245, freq={GNOMAD_G_EAS=1|1558|0}, path={}
//        Iterator<AlleleProto.AlleleKey> keyIterator = clinVarMap.keyIterator(from);
//        while (keyIterator.hasNext()) {
//            AlleleProto.AlleleKey a = keyIterator.next();
//            if (a.getPosition() <= genomicInterval.end()) {
//                GenomicVariant clinVarVariant = alleleKeyToGenomicVariant(a, contig, CoordinateSystem.ONE_BASED);
//                ClinVarData clinVarData = getClinVarData(a);
//                results.put(clinVarVariant, clinVarData);
//            }
//            if (a.getPosition() > end) {
//                break;
//            }
//        }
//        return results;
    }

    private String broadFormat(AlleleProto.AlleleKey alleleKey) {
        return alleleKey.getChr() + "-" + alleleKey.getPosition()  + "-" + alleleKey.getRef() + "-" + alleleKey.getAlt();
    }

    private GenomicVariant alleleKeyToGenomicVariant(AlleleProto.AlleleKey alleleKey, Contig contig, CoordinateSystem coordinateSystem) {
        return GenomicVariant.of(contig, Strand.POSITIVE, coordinateSystem, alleleKey.getPosition(), alleleKey.getRef(), alleleKey.getAlt());
    }

}
