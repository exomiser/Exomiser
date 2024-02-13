package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
        return GenomicVariant.of(GenomeAssembly.HG19.getContigByName(fields[0]), Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, Integer.parseInt(fields[1]), fields[2]), fields[2], fields[3]);
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
                    .setReviewStatus(AlleleProto.ClinVar.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
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
    public void emptyStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore();
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    public void variantDownstreamOfVariantsInStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1220-A-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    public void variantUpstreamOfVariantsInStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1240-A-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    public void fiveInsideAndFourOutsideBoundaries() {
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

        Contig chr1 = GenomeAssembly.HG19.getContigById(1);
        GenomicInterval genomicInterval = GenomicInterval.of(chr1, Strand.POSITIVE, Coordinates.oneBased(1228, 1232));

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicInterval);
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
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1230-T-G", "1-1231-C-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.keySet(), equalTo(Set.of(parseVariant("1-1230-T-G"))));
    }

    @Test
    public void variantOverlapsDeletionPosition() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1230-T-G", "1-1231-C-G");
        GenomicVariant genomicVariant = parseVariant("1-1230-TT-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.keySet(), equalTo(Set.of(parseVariant("1-1230-T-G"), parseVariant("1-1231-C-G"))));
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

    @Disabled("manual testing utility")
    @Test
    void manualDataExplorer() {
        MVStore mvStore = new MVStore.Builder().fileName("/home/hhx640/Documents/exomiser-data/2401_hg19/2401_hg19_clinvar.mv.db").readOnly().open();
        ClinVarDaoMvStore clinVarDao = new ClinVarDaoMvStore(mvStore);

        MVStore alleleStore = new MVStore.Builder().fileName("/home/hhx640/Documents/exomiser-data/2401_hg19/2401_hg19_variants.mv.db").readOnly().open();
        AllelePropertiesDao allelePropertiesDao = new AllelePropertiesDaoMvStore(alleleStore);
        GenomeAssembly assembly = GenomeAssembly.HG19;

        JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(Path.of("/home/hhx640/Documents/exomiser-data/2401_hg19/2401_hg19_transcripts_ensembl.ser"));
        VariantAnnotator variantAnnotator = new JannovarVariantAnnotator(assembly, jannovarData, ChromosomalRegionIndex.empty());
        // 18-57550760-A-T NA
        // 18-57550753-A-C LP // 55217985 (hg19)
        // 1-226923505-G-T // hg19
        // PS1: 10-123247514-C-A and 10-123247514-C-G - FGFR2 c.1704G>T p.(Lys568Asn) / c.1704G>C  p.(Lys568Asn) PATH
        // 10-123276886-G-C and 10-123276887-C-G - FGFR2 c.1031C>G p.(Ala344Gly) / c.1030G>C p.(Ala344Pro)  PATH
        // PS1: 10-123279562-C-A and 10-123279562-C-G - FGFR2 c.870G>T p.(Trp290Cys) / c.870G>C p.(Trp290Cys)  PATH
        // PS1: 10-123279564-A-T and 10-123279564-A-G - NM_000141.5(FGFR2):c.868T>A / c.868T>C (p.Trp290Arg)
        // TODO: Jannovar does not choose the same transcript as VEP as it does not apply any CDSlength rules as per:
        // https://mart.ensembl.org/info/genome/genebuild/canonical.html (see also vitt). Ideally the Jannovar Annotations
        // should be sorted before being converted to TranscriptAnnotations. This isn't an issue if MANE only
        // transcripts are being used as these are the only ones available to report on.
        GenomicVariant genomicVariant = parseVariant(assembly, "10-123256215-T-A"); // 10-123256215-T-G hg38:10-121496701-T-G

        System.out.println("Searching for: " + toBroad(genomicVariant));
        AlleleProto.AlleleKey alleleKey = AlleleProtoAdaptor.toAlleleKey(genomicVariant);
        // encode as VariantKey (https://doi.org/10.1101/473744) == 8 bytes fixed size (long);
//        System.out.println("AlleleKey size (bytes): " + alleleKey.getSerializedSize()); // SNP = 13 bytes, 11 bases = 23
        System.out.println();

        System.out.println(clinVarDao.getClinVarData(genomicVariant));
        Map<GenomicVariant, ClinVarData> clinVarRecordsOverlappingInterval = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        clinVarRecordsOverlappingInterval.forEach((variant, clinVarData) -> System.out.println(toBroad(variant) + " : " + clinVarData));

        System.out.println();
        List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(genomicVariant);
        AlleleProto.AlleleProperties alleleProperties = allelePropertiesDao.getAlleleProperties(alleleKey, assembly);
        FrequencyData frequencyData = AlleleProtoAdaptor.toFrequencyData(alleleProperties);
        ClinVarData clinVarData = clinVarDao.getClinVarData(genomicVariant);
        VariantAnnotation variantAnnotation = variantAnnotations.isEmpty() ? null : variantAnnotations.get(0);
        TranscriptAnnotation transcriptAnnotation = variantAnnotation != null && variantAnnotation.hasTranscriptAnnotations() ? variantAnnotation.getTranscriptAnnotations().get(0) : null;
        System.out.println(toBroad(genomicVariant) +  (variantAnnotation == null ? "" : " " + variantAnnotation.getGeneSymbol() + (transcriptAnnotation == null ? "" :  ":" + transcriptAnnotation.getHgvsCdna() + ":" + transcriptAnnotation.getHgvsProtein() ) + " " + variantAnnotation.getVariantEffect()));
        System.out.print(frequencyData.getRsId());
        System.out.println(clinVarData.isEmpty() ? "" : " " + clinVarData.getPrimaryInterpretation() + " ("  + clinVarData.starRating() + "*) " + clinVarData.getVariationId() + " " + clinVarData.getGeneSymbol() + ":" + clinVarData.getHgvsCdna() + ":" + clinVarData.getHgvsProtein() + " " + clinVarData.getVariantEffect());
        System.out.println("Frequency data:");
        if (frequencyData.isEmpty()) {
            System.out.println("\t-");
        } else {
            frequencyData.frequencies().forEach(freq -> System.out.println("\t" + freq.source() + "=" + freq.frequency()));
        }

        System.out.println("Pathogenicity scores:");
        PathogenicityData pathogenicityData = AlleleProtoAdaptor.toPathogenicityData(alleleProperties);
        if (pathogenicityData.isEmpty()) {
            System.out.println("\t-");
        } else {
            pathogenicityData.getPredictedPathogenicityScores().forEach(path -> System.out.println("\t" + path));
        }
    }

    private String toBroad(GenomicVariant genomicVariant) {
        return genomicVariant.contig().name() + '-' + genomicVariant.start() + '-' + genomicVariant.ref() + '-' + genomicVariant.alt();
    }

    private GenomicVariant parseVariant(GenomeAssembly genomeAssembly, String broadFormatVariant) {
        String[] fields = broadFormatVariant.split("-");
        return GenomicVariant.of(genomeAssembly.getContigByName(fields[0]), Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, Integer.parseInt(fields[1]), fields[2]), fields[2], fields[3]);
    }
}