package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DefaultDiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;

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

    private GenomicVariant variant(String variant) {
        return parseVariant(GenomeAssembly.HG19, variant);
    }

    @Test
    void getClinVarData() {
        try (MVStore mvStore = new MVStore.Builder().open()) {
            var clinvarMap = MvStoreUtil.openClinVarMVMap(mvStore);
            AlleleProto.AlleleKey alleleKey = parseAlleleKey("1-200-A-T");
            AlleleProto.ClinVar clinVar = AlleleProto.ClinVar.newBuilder()
                    .setVariationId("12345")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus(AlleleProto.ClinVar.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
                    .build();
            clinvarMap.put(alleleKey, clinVar);

            ClinVarDao instance = new ClinVarDaoMvStore(mvStore);

            GenomicVariant clinVarVariant = variant("1-200-A-T");
            assertThat(instance.getClinVarData(clinVarVariant), equalTo(AlleleProtoAdaptor.toClinVarData(clinVar)));

            GenomicVariant nonClinVarVariant = variant("1-200-A-A");
            assertThat(instance.getClinVarData(nonClinVarVariant), equalTo(ClinVarData.empty()));
        }
    }

    @Test
    void getClinVarDataNotReturningConflictingSig() {
        try (MVStore mvStore = new MVStore.Builder().open()) {
            var clinvarMap = MvStoreUtil.openClinVarMVMap(mvStore);
            AlleleProto.AlleleKey alleleKey = parseAlleleKey("1-200-A-T");
            AlleleProto.ClinVar clinVar = AlleleProto.ClinVar.newBuilder()
                    .setVariationId("12345")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                    .setReviewStatus(AlleleProto.ClinVar.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                    .putClinSigCounts("PATHOGENIC", 5)
                    .putClinSigCounts("UNCERTAIN_SIGNIFICANCE", 5)
                    .build();
            clinvarMap.put(alleleKey, clinVar);

            ClinVarDao instance = new ClinVarDaoMvStore(mvStore);
            System.out.println("AlleleProtoAdaptor.toClinVarData: " + AlleleProtoAdaptor.toClinVarData(clinVar));
            GenomicVariant clinVarVariant = variant("1-200-A-T");
            System.out.println("ClinVarDao.getClinVarData: " + instance.getClinVarData(clinVarVariant));
            assertThat(instance.getClinVarData(clinVarVariant), equalTo(AlleleProtoAdaptor.toClinVarData(clinVar)));

            GenomicVariant nonClinVarVariant = variant("1-200-A-A");
            assertThat(instance.getClinVarData(nonClinVarVariant), equalTo(ClinVarData.empty()));
        }
    }

    @Test
    void emptyStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore();
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    void variantDownstreamOfVariantsInStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1220-A-G");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    void variantUpstreamOfVariantsInStore() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1240-A-G");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.size(), equalTo(0));
    }

    @Test
    void insideAndOutsideBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore(
                "1-1-A-G",
                "1-1226-TGTGGGAA-A", // -4 this does overlap
                "1-1227-A-G", // -3
                "1-1228-A-G", // -2
                "1-1229-A-G", // -1
                "1-1230-T-G", // 0 same ref
                "1-1230-T-C", // 0 same ref
                "1-1231-C-G", // +1
                "1-1232-T-A", // +2
                "1-1232-TGTGGGAA-A", // +2
                "1-1232-A-ATTC", // +2
                "1-1233-C-T", // +3
                "1-7700-A-G"
                );

        Contig chr1 = GenomeAssembly.HG19.getContigById(1);
        GenomicInterval genomicInterval = GenomicInterval.of(chr1, Strand.POSITIVE, Coordinates.oneBased(1228, 1232));

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicInterval);
        assertThat(result.keySet(), equalTo(Set.of(
                variant("1-1228-A-G"),
                variant("1-1229-A-G"),
                variant("1-1230-T-G"),
                variant("1-1230-T-C"),
                variant("1-1231-C-G"),
                variant("1-1232-T-A"),
                variant("1-1232-TGTGGGAA-A"),
                variant("1-1232-A-ATTC")
        )));
    }

    @Test
    void variantsNonOverlap() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1227-A-G", "1-1233-C-T");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));
    }

    @Test
    void variantsOverlapDirectlyAtBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1228-A-G", "1-1232-T-A");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }

    @Test
    void variantsOverlapInsideBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1231-C-G");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }

    @Test
    void variantsExactlyMatchOnPosition() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1230-T-G", "1-1231-C-G");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.keySet(), equalTo(Set.of(variant("1-1230-T-G"))));
    }

    @Test
    void variantOverlapsDeletionPosition() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1229-A-G", "1-1230-T-G", "1-1231-C-G");
        GenomicVariant genomicVariant = variant("1-1230-TT-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant);
        assertThat(result.keySet(), equalTo(Set.of(variant("1-1230-T-G"), variant("1-1231-C-G"))));
    }

    @Test
    void variantsAreFarOutOfBoundaries() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-7700-A-G", "1-1-A-G");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));

    }

    @Test
    void noVariantsGettingAddedNoOverlap() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore();
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(0));

    }

    @Test
    void setPositionInButLengthIncorrectTest() {
        ClinVarDaoMvStore clinVarDao = buildClinVarDaoMvStore("1-1228-AA-G", "1-1231-A-GG");
        GenomicVariant genomicVariant = variant("1-1230-T-A");

        var result = clinVarDao.findClinVarRecordsOverlappingInterval(genomicVariant.withPadding(2, 2));
        assertThat(result.size(), equalTo(2));
    }

    @Disabled("manual testing utility")
    @Test
    void manualDataExplorer() {
        // variant under assessment in 'Broad format' (i.e. hyphen seperated chr-pos-ref-alt)
        String vua = "19-54123563-A-G"; // hg19-22-51019849-C-T possible cryptic splice variant? SPLICE_AI: 0.230 (PP3), ALPHA_MISSENSE: 0.125
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;
        // 37:10-124797364 // 38:10-123037848-G-A + 10-123051188-T-TAA + 10-123037804-C-A
        // 10-89624227-A-G PTEN PATHOGENIC 3* START_LOST
        // 10-89624245-G-T PTEN PATHOGENIC 2* STOP_GAINED
        // 10-89624242-A-G PTEN PATHOGENIC 3* MISSENSE_VARIANT

        // 4-15589553-G-C - Hypothetical, not in ClinVar, LIKELY_PATHOGENIC [PVS1_Strong, PS1, PM2_Supporting] points=9?
        // 1-63868019-G-A | 1-63877002-G-A

        // 2-232079571-G-A
        // FP:
        // MT-12706-T-C
        // 11-6638385-C-T
        // 12-103234252-T-C

        // 17-48275792-A-G SPLICE_DONOR_VARIANT (LP, ClinVar)
        // 17-48275815-T-G SYNONYMOUS_VARIANT (VUS, ClinVar)
        //
        // 3'UTR hg38-20-58909654-A-G high spliceAI
        GenomeAssembly assembly = GenomeAssembly.HG38;

        String dataDir = "/home/hhx640/Documents/exomiser-data/";
        String dataVersion = "2406_" + assembly;

        MVStore mvStore = new MVStore.Builder().fileName(dataDir + dataVersion + "/" + dataVersion + "_clinvar.mv.db").readOnly().open();
        ClinVarDaoMvStore clinVarDao = new ClinVarDaoMvStore(mvStore);

        MVStore alleleStore = new MVStore.Builder().fileName(dataDir + dataVersion + "/" + dataVersion + "_variants.mv.db").readOnly().open();
        AllelePropertiesDao allelePropertiesDao = new AllelePropertiesDaoMvStore(alleleStore);

        JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(Path.of(dataDir + dataVersion + "/" + dataVersion + "_transcripts_ensembl.ser"));
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
        GenomicVariant genomicVariant = parseVariant(assembly, vua); // 10-123256215-T-G hg38:10-121496701-T-G

        // hg38:4-25145092-C-T ENST00000382103.7(SEPSECS):c.846G>A (SEPSECS)p.(Leu282=) SpliceAI=0.42
        // 10-89624227-A-G (PTEN)
        // 10-123247514-C-G : PS1, PM1, PM2_Supporting, PM5_Supporting, PP3, PP5_Strong
        // 10-123247517-T-G : PS1_Supporting, PM1, PM2_Supporting, PM5_Supporting, PP2, PP3
        System.out.println("Searching for: " + toBroad(genomicVariant));
        AlleleProto.AlleleKey alleleKey = AlleleProtoAdaptor.toAlleleKey(genomicVariant);
        // encode as VariantKey (https://doi.org/10.1101/473744) == 8 bytes fixed size (long);
//        System.out.println("AlleleKey size (bytes): " + alleleKey.getSerializedSize()); // SNP = 13 bytes, 11 bases = 23

        System.out.println();
        Set<PathogenicitySource> pathSources = EnumSet.of(REVEL, MVP, ALPHA_MISSENSE, SPLICE_AI, REMM);
        List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(genomicVariant);
        AlleleProto.AlleleProperties alleleProperties = allelePropertiesDao.getAlleleProperties(alleleKey, assembly);
        FrequencyData frequencyData = AlleleProtoAdaptor.toFrequencyData(alleleProperties).toBuilder().filterSources(FrequencySource.NON_FOUNDER_POPS).build();
        PathogenicityData pathogenicityData = AlleleProtoAdaptor.toPathogenicityData(alleleProperties);
        pathogenicityData = PathogenicityData.of(pathogenicityData.pathogenicityScores().stream().filter(score -> pathSources.contains(score.getSource())).toList());
        ClinVarData clinVarData = clinVarDao.getClinVarData(genomicVariant);
        VariantAnnotation variantAnnotation = variantAnnotations.isEmpty() ? null : variantAnnotations.get(0);
        VariantEvaluation variantEvaluation = VariantEvaluation.builder()
                .variant(genomicVariant)
                .geneId(variantAnnotation.getGeneId())
                .geneSymbol(variantAnnotation.getGeneSymbol())
                .variantEffect(variantAnnotation.getVariantEffect())
                .annotations(variantAnnotation.getTranscriptAnnotations())
                .frequencyData(frequencyData)
                .pathogenicityData(PathogenicityData.of(clinVarData, pathogenicityData.pathogenicityScores()))
//                .whiteListed()
                .compatibleInheritanceModes(Set.of(modeOfInheritance))
                .build();

        TranscriptAnnotation transcriptAnnotation = variantAnnotation != null && variantAnnotation.hasTranscriptAnnotations() ? variantAnnotation.getTranscriptAnnotations().get(0) : null;
        Set<VariantEffect> varEffects = variantAnnotation != null && variantAnnotation.hasTranscriptAnnotations() ? variantAnnotation.getTranscriptAnnotations().stream().map(TranscriptAnnotation::getVariantEffect).collect(Collectors.toUnmodifiableSet()) : Set.of();
        System.out.println(toBroad(genomicVariant) + " " + (frequencyData.getRsId().isEmpty() ? "-" : frequencyData.getRsId()) + " " + (variantAnnotation == null ? "" : variantAnnotation.getGeneSymbol() + (transcriptAnnotation == null ? "" :  ":" + transcriptAnnotation.getAccession() + ":" + transcriptAnnotation.getHgvsCdna() + ":" + transcriptAnnotation.getHgvsProtein() + " " + transcriptAnnotation.getRankType() + " " + transcriptAnnotation.getRank() + "/" + transcriptAnnotation.getRankTotal()) + " " + varEffects));
        String geneSymbol = variantAnnotation.getGeneSymbol();
        GeneConstraint geneConstraint = GeneConstraints.geneConstraint(geneSymbol);
        if (geneConstraint != null) {
            System.out.println(geneSymbol + " " + geneConstraint + (geneConstraint.isLossOfFunctionIntolerant() ? (" (LOF INTOLERANT)") : ""));
        }
        System.out.println("ClinVar: " + (clinVarData.isEmpty() ? "-" : formatClinVarData(clinVarData)));
        System.out.println("Variant score: " + variantEvaluation.getVariantScore() + " Frequency score: " + variantEvaluation.getFrequencyScore() + " Pathogenicity score: " + variantEvaluation.getPathogenicityScore() + (variantEvaluation.isWhiteListed() ? " (WHITELIST VARIANT - all scores set to 1.0)" : ""));
        System.out.println("Frequency score: " + frequencyData.frequencyScore());
        if (frequencyData.isEmpty()) {
            System.out.println("\t(no frequency data)");
        } else {
            frequencyData.frequencies().forEach(freq -> System.out.println("\t" + freq.source() + "=" + freq.frequency() + (freq.an() == 0  ? "" : "(" + freq.ac() + "|" + freq.an() + "|" + freq.homs() + ")")));
        }

        System.out.println("Pathogenicity score: " + pathogenicityData.pathogenicityScore());
        if (pathogenicityData.isEmpty()) {
            System.out.println("\t(no pathogenicity data)");
        } else {
            pathogenicityData.pathogenicityScores().forEach(path -> System.out.println("\t" + path));
        }

        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("sample", Pedigree.justProband("sample"), clinVarDao);
        Disease disease = Disease.builder().diseaseId("DISEASE:1").diseaseName("disease").inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).build();
        AcmgEvidence acmgEvidence = acmgEvidenceAssigner.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, List.of(variantEvaluation), List.of(disease), List.of());
        AcmgEvidenceClassifier classifier = new Acmg2020PointsBasedClassifier();
        // should use an AcmgAssignmentCalculator to figure out the correct disease-gene association to use
        System.out.println(classifier.classify(acmgEvidence) + " " + acmgEvidence + " points=" + acmgEvidence.points() + " score=" + acmgEvidence.postProbPath());

//        var manualEvidence = AcmgEvidence.builder()
//                .add(AcmgCriterion.PM2, AcmgCriterion.Evidence.SUPPORTING)
//                .add(AcmgCriterion.PP3, AcmgCriterion.Evidence.SUPPORTING)
//                .add(AcmgCriterion.PS1)
//                        .build();
//        System.out.println(classifier.classify(manualEvidence) + " " + manualEvidence + " points=" + manualEvidence.points());

        System.out.println();

        GenomicRegion clinVarSearchInterval = genomicVariant.withPadding(25, 25);
        String interval = clinVarSearchInterval.contig().name() + ":" + clinVarSearchInterval.startOneBased() + "-" + clinVarSearchInterval.end();
        System.out.println("ClinVar neighbourhood " + interval + " (" + genomicVariant.start() + " +/-25 bases):");
        Map<GenomicVariant, ClinVarData> clinVarRecordsOverlappingInterval = clinVarDao.findClinVarRecordsOverlappingInterval(clinVarSearchInterval);
        clinVarRecordsOverlappingInterval.forEach((variant, data) -> System.out.println(toBroad(variant) + " : " + formatClinVarData(data)));
        //TODO add known diseases
    }

    private String formatClinVarData(ClinVarData clinVarData) {
        return clinVarData.getPrimaryInterpretation() + (clinVarData.getPrimaryInterpretation() == ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS ? " " + clinVarData.getConflictingInterpretationCounts() : "") + " (" + clinVarData.starRating() + "*) " + clinVarData.getVariationId() + " " + clinVarData.getGeneSymbol() + ":" + clinVarData.getHgvsCdna() + ":" + clinVarData.getHgvsProtein() + " " + clinVarData.getVariantEffect() + " (" + clinVarData.getReviewStatus() + ")";
    }

    private String toBroad(GenomicVariant genomicVariant) {
        return genomicVariant.contig().name() + '-' + genomicVariant.start() + '-' + genomicVariant.ref() + '-' + genomicVariant.alt();
    }

    /**
     * Parses small sequence variants of the form `chr-pos-ref-alt`. For example:
     * <pre>
     *     SNP  1-12345-A-T
     *     SNP  1:12345:A:T
     *     DEL  1-12345-AT-A
     *     INS  1-12345-A-AT
     *     MNV  1-12345-AT-GC
     *     INV  1-12345-AT-TA
     * </pre>
     * Separators can be either hyphens '-' or colons ':'. The genomic assembly is also required.
     *
     * @param genomeAssembly Genomic assembly for the variant coordinates
     * @param s The string representation of the variant
     * @return A {@link GenomicVariant}
     */
    private GenomicVariant parseVariant(GenomeAssembly genomeAssembly, String s) {
        String[] fields = s.split("[-:]");
        return GenomicVariant.of(genomeAssembly.getContigByName(fields[0]), Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, Integer.parseInt(fields[1]), fields[2]), fields[2], fields[3]);
    }
}