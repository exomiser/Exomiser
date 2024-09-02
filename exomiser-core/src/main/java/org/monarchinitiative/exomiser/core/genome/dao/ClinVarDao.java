package org.monarchinitiative.exomiser.core.genome.dao;

import jakarta.annotation.Nonnull;
import org.monarchinitiative.exomiser.core.model.GeneStatistics;
import org.monarchinitiative.exomiser.core.model.Variant;
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

    ClinVarData getClinVarData(@Nonnull Variant variant);

    ClinVarData getClinVarData(@Nonnull GenomicVariant genomicVariant);

    Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(@Nonnull GenomicInterval genomicInterval);

    GeneStatistics getGeneStatistics(@Nonnull String geneSymbol);
}
