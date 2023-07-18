package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;


/**
 * Interface for providing {@link ClinVarData} about a {@link Variant}.
 *
 * @since 14.0.0
 */
public interface ClinVarDao {

    ClinVarData getClinVarData(Variant variant);

//    List<ClinVarData> findClinVarRecordsOverlappingRegion(GenomicInterval genomicInterval);
    ////            Map<AlleleKey, PathogenicityData> pathogenicityData = mvStoreDao.getPathogenicityDataForRange(chr, min, max);

}
