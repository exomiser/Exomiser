package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ClinVarDaoMvStoreTest {

    @Test
    void getClinVarData() {
        try (MVStore mvStore = new MVStore.Builder().open()) {
            var clinvarMap = MvStoreUtil.openClinVarMVMap(mvStore);
            AlleleProto.AlleleKey alleleKey = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(200).setRef("A").setAlt("T").build();
            AlleleProto.ClinVar clinVar = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("12345")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus("criteria_provided,_multiple_submitters,_no_conflicts")
                    .build();
            clinvarMap.put(alleleKey, clinVar);

            ClinVarDao instance = new ClinVarDaoMvStore(mvStore);
            Variant clinVarVariant = VariantEvaluation.builder()
                    .variant(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, Coordinates.oneBased(200, 200), "A", "T")
                    .build();

            assertThat(instance.getClinVarData(clinVarVariant), equalTo(AlleleProtoAdaptor.toClinVarData(clinVar)));

            Variant nonClinVarVariant = VariantEvaluation.builder()
                    .variant(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, Coordinates.oneBased(200, 200), "A", "A")
                    .build();

            assertThat(instance.getClinVarData(nonClinVarVariant), equalTo(ClinVarData.empty()));
        }
    }
}