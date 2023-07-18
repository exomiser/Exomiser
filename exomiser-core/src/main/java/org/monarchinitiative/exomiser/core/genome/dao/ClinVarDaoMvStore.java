package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

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
        AlleleProto.AlleleKey alleleKey = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProto.ClinVar clinVar = clinVarMap.get(alleleKey);
        return clinVar == null ? ClinVarData.empty() : AlleleProtoAdaptor.toClinVarData(clinVar);
    }
}
