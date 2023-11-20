package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ClinVarDaoMvStoreTest {

    private ClinVarDaoMvStore buildClinVarDaoMvStore(String... broadFormatVariants) {
        // open an in-memory store
        MVStore mvStore = new MVStore.Builder().open();
        MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap = MvStoreUtil.openClinVarMVMap(mvStore);
        for (String variant : broadFormatVariants) {
            clinVarMap.put(parseAlleleKey(variant), AlleleProto.ClinVar.newBuilder().build());
        }
        return new ClinVarDaoMvStore(mvStore);
    }

    private AlleleProto.AlleleKey parseAlleleKey(String broadFormatVariant) {
        String[] fields = broadFormatVariant.split("-");
        return AlleleProto.AlleleKey.newBuilder()
                .setChr(Integer.parseInt(fields[0]))
                .setPosition(Integer.parseInt(fields[1]))
                .setRef(fields[2])
                .setAlt(fields[3])
                .build();
    }

    private GenomicVariant parseVariant(String broadFormatVariant) {
        String[] fields = broadFormatVariant.split("-");
        return GenomicVariant.of(GenomeAssembly.HG19.getContigById(Integer.parseInt(fields[0])), Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, Integer.parseInt(fields[1]), fields[2]), fields[2], fields[3]);
    }

    private final AlleleProto.AlleleKey positionStartMinus1 = parseAlleleKey("1-1229-A-G");
    private final AlleleProto.AlleleKey positionEndPlus1 = parseAlleleKey("1-1231-C-G");

    // Variants directly at the boundaries:
    private final AlleleProto.AlleleKey positionStartMinus2 = parseAlleleKey("1-1228-A-G");
    private final AlleleProto.AlleleKey positionEndPlus2 = parseAlleleKey("1-1232-T-A");

    // Variants just outside the boundaries:
    private final AlleleProto.AlleleKey positionStartMinus3 = parseAlleleKey("1-1227-A-G");
    private final AlleleProto.AlleleKey positionEndPlus3 = parseAlleleKey("1-1233-C-T");

    // Variants are exactly matching on position:
    private final AlleleProto.AlleleKey positionExactlyMatches = parseAlleleKey("1-1230-T-G");

    // Variants are exactly matching on position + but ref and alt are different
    private final AlleleProto.AlleleKey positionExactlyMatchesButDifferentRefAlt = parseAlleleKey("1-1230-T-C");

    // Variants are far out of boundaries:
    private final AlleleProto.AlleleKey positionFarOutOfBoundariesMinus = parseAlleleKey("1-7700-A-G");
    private final AlleleProto.AlleleKey positionFarOutOfBoundariesPlus = parseAlleleKey("1-1-A-G");

    // two variants that are in range but Alt.length and Ref.length > 1
    private final AlleleProto.AlleleKey positionInButLengthIncorrect = parseAlleleKey("1-1229-AA-G");
    private final AlleleProto.AlleleKey positionInButLengthIncorrect1 = parseAlleleKey("1-1231-A-GG");

    @Test
    void getClinVarData() {
        try (MVStore mvStore = new MVStore.Builder().open()) {
            var clinvarMap = MvStoreUtil.openClinVarMVMap(mvStore);
            AlleleProto.AlleleKey alleleKey = parseAlleleKey("1-200-A-T");
            AlleleProto.ClinVar clinVar = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("12345")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus("criteria_provided,_multiple_submitters,_no_conflicts")
                    .build();
            clinvarMap.put(alleleKey, clinVar);

            ClinVarDao instance = new ClinVarDaoMvStore(mvStore);

            GenomicVariant clinVarVariant = parseVariant("1-200-A-T");
            assertThat(instance.getClinVarData(clinVarVariant), equalTo(AlleleProtoAdaptor.toClinVarData(clinVar)));

            GenomicVariant nonClinVarVariant = parseVariant("1-200-A-A");
            assertThat(instance.getClinVarData(nonClinVarVariant), equalTo(ClinVarData.empty()));
        }
    }

    @Test
    public void FiveInsideAndFourOutsideBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore(
                "1-1-A-G",
                "1-1227-A-G", // -3
                "1-1228-A-G", // -2
                "1-1229-A-G", // -1
                "1-1230-T-G", // 0 same ref
                "1-1230-T-C", // 0 same ref
                "1-1231-C-G", // +1
                "1-1232-T-A", // +2
                "1-1233-C-T", // +3
                "1-7700-A-G"
                );

        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(6));
    }

    @Test
    public void variantsNonOverlap() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1227-A-G", "1-1233-C-T");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));

    }

    @Test
    public void variantsOverlapDirectlyAtBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1228-A-G", "1-1232-T-A");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }

    @Test
    public void variantsOverlapInsideBoundaries() {
//        MVStore mvStore = newMvStore();
//        MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap = MvStoreUtil.openClinVarMVMap(mvStore);
//        clinVarMap.put(parseAlleleKey("1-1229-A-G"), AlleleProto.ClinVar.newBuilder().build());
//        clinVarMap.put(parseAlleleKey("1-1231-C-G"), AlleleProto.ClinVar.newBuilder().build());
//        ClinVarDaoMvStore clinVarDao = new ClinVarDaoMvStore(mvStore);
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1231-C-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }

    @Test
    public void variantsExactlyMatchOnPosition() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1230-T-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.keySet(), equalTo(Set.of(parseVariant("1-1230-T-G"))));
    }

    @Test
    public void variantsAreFarOutOfBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-7700-A-G", "1-1-A-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));

    }

    @Test
    public void noVariantsGettingAddedNoOverlap() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore();
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));

    }

    @Test
    public void setPositionInButLengthIncorrectTest() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1228-AA-G", "1-1231-A-GG");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }
}