package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.svart.GenomicInterval;
import org.monarchinitiative.svart.GenomicVariant;

import java.util.Map;


/**
 * Interface for providing {@link ClinVarData} about a {@link GenomicVariant}.
 *
 * @since 14.0.0
 */
public interface ClinVarDao {

    ClinVarData getClinVarData(GenomicVariant variant);

    Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(GenomicInterval genomicInterval);
}
