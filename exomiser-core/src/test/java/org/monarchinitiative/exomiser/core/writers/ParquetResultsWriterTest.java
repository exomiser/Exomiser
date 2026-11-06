package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.model.*;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ParquetResultsWriterTest {

    @TempDir
    Path tempDir;

    private final Gene gene = TestFactory.newGeneFGFR2();
    private final PhenotypeTerm query = PhenotypeTerm.of("HP:0000316", "Hypertelorism");
    private final PhenotypeTerm secondQuery = PhenotypeTerm.of("HP:0000244", "Brachyturricephaly");
    private final List<PhenotypeMatch> termMatches = List.of(
            PhenotypeMatch.builder().query(query).match(PhenotypeTerm.of("HP:0000316", "Hypertelorism")).score(2.5).build(),
            PhenotypeMatch.builder().query(secondQuery).match(PhenotypeTerm.of("HP:0001363", "Craniosynostosis")).score(1.25).build());
    // Synthetic disease associations: model IDs intentionally differ from disease IDs.
    private final Disease recessive = disease("OMIM:12345", "Synthetic recessive disease", InheritanceMode.AUTOSOMAL_RECESSIVE);
    private final Disease dominant = disease("OMIM:12346", "Synthetic dominant disease", InheritanceMode.AUTOSOMAL_DOMINANT);
    private final Disease unknown = disease("OMIM:12347", "Synthetic unknown inheritance", InheritanceMode.UNKNOWN);
    private final Disease neighbour = Disease.builder().diseaseId("OMIM:12348").diseaseName("Synthetic PPI disease")
            .associatedGeneId(9939).associatedGeneSymbol("RBM8A").build();
    private final GeneModelPhenotypeMatch mouse = ortholog("MGI:12345", Organism.MOUSE, 0.6);
    private final GeneModelPhenotypeMatch fish = ortholog("ZFIN:12345", Organism.FISH, 0.5);
    private final GeneModelPhenotypeMatch ppi = diseaseMatch(neighbour, 0.4);

    @ParameterizedTest
    @EnumSource(value = ModeOfInheritance.class, names = {"AUTOSOMAL_DOMINANT", "AUTOSOMAL_RECESSIVE", "ANY"})
    void writesAllHumanMatchesOnEveryVariantRow(ModeOfInheritance mode) throws Exception {
        HiPhivePriorityResult hiPhive = hiPhive(List.of(diseaseMatch(dominant, 0.7), mouse,
                diseaseMatch(unknown, 0.3), fish, diseaseMatch(recessive, 0.9)));
        gene.addPriorityResult(hiPhive);
        addAssociatedDiseases();
        addGeneScore(mode, hiPhive.compatibleDiseaseMatches(mode));
        // Insert in reverse genomic order to exercise the writer's existing variant ordering.
        VariantEvaluation second = variant(12346, mode);
        VariantEvaluation first = variant(12345, mode);
        gene.addVariant(second);
        gene.addVariant(first);

        WrittenResults written = writeAndRead();
        assertThat(written.parquet().size(), equalTo(2));
        assertThat(written.tsv().size(), equalTo(1));
        List<ModelPhenotypeMatch<Disease>> expected = List.of(
                ModelPhenotypeMatch.of(0.9, recessive, termMatches),
                ModelPhenotypeMatch.of(0.7, dominant, termMatches),
                ModelPhenotypeMatch.of(0.3, unknown, termMatches));
        for (int i = 0; i < written.parquet().size(); i++) {
            JsonNode row = written.parquet().get(i);
            VariantEvaluation variant = List.of(first, second).get(i);
            assertThat(row.path("variant").asText(), equalTo(variant.toGnomad() + "_" + moi(mode)));
            assertRowScores(row, written.tsv().getFirst(), mode);
            assertHumanMatches(row.path("diseaseMatches"), expected);
            assertHumanTsvEvidence(row, written.tsv().getFirst());
            assertAssociatedDiseases(row);
            assertOtherEvidence(row);
        }
        assertThat(gene.geneScoreForMode(mode).compatibleDiseaseMatches(), equalTo(hiPhive.compatibleDiseaseMatches(mode)));
    }

    @ParameterizedTest
    @EnumSource(value = ModeOfInheritance.class, names = {"AUTOSOMAL_DOMINANT", "AUTOSOMAL_RECESSIVE", "ANY"})
    void writesHumanMatchRegardlessOfRowInheritance(ModeOfInheritance mode) throws Exception {
        HiPhivePriorityResult hiPhive = hiPhive(List.of(diseaseMatch(recessive, 0.9)));
        gene.addPriorityResult(hiPhive);
        addAssociatedDiseases();
        addGeneScore(mode, hiPhive.compatibleDiseaseMatches(mode));
        gene.addVariant(variant(12345, mode));
        assertThat(gene.geneScoreForMode(mode).compatibleDiseaseMatches().isEmpty(),
                equalTo(mode == ModeOfInheritance.AUTOSOMAL_DOMINANT));

        WrittenResults written = writeAndRead();
        assertThat(written.parquet().size(), equalTo(1));
        assertHumanMatches(written.parquet().getFirst().path("diseaseMatches"),
                List.of(ModelPhenotypeMatch.of(0.9, recessive, termMatches)));
        assertHumanTsvEvidence(written.parquet().getFirst(), written.tsv().getFirst());
    }

    @Test
    void writesHumanEvidenceForPhenotypeOnlyRowWithoutCompatibleAnnotations() throws Exception {
        gene.addPriorityResult(hiPhive(List.of(diseaseMatch(recessive, 0.9), mouse, fish)));
        addAssociatedDiseases();
        addGeneScore(ModeOfInheritance.ANY, List.of());

        WrittenResults written = writeAndRead();
        assertThat(written.parquet().size(), equalTo(1));
        JsonNode row = written.parquet().getFirst();
        assertThat(row.path("variant").asText(), equalTo(""));
        assertHumanMatches(row.path("diseaseMatches"), List.of(ModelPhenotypeMatch.of(0.9, recessive, termMatches)));
        assertHumanTsvEvidence(row, written.tsv().getFirst());
        assertAssociatedDiseases(row);
        assertOtherEvidence(row);
    }

    @Test
    void doesNotUseMouseFishOrPpiEvidenceAsDirectHumanEvidence() throws Exception {
        gene.addPriorityResult(hiPhive(List.of(mouse, fish)));
        addGeneScore(ModeOfInheritance.ANY, List.of());

        WrittenResults written = writeAndRead();
        assertThat(written.parquet().size(), equalTo(1));
        JsonNode row = written.parquet().getFirst();
        assertHumanMatches(row.path("diseaseMatches"), List.of());
        assertThat(row.path("diseasePhenotypeScore").asDouble(), equalTo(0.0));
        assertThat(written.tsv().getFirst().get("HUMAN_PHENO_EVIDENCE"), equalTo(""));
        assertOtherEvidence(row);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservesCompatibleMatchesWhenHiPhiveIsAbsent(boolean hasCompatibleMatches) throws Exception {
        List<ModelPhenotypeMatch<Disease>> matches = hasCompatibleMatches
                ? List.of(ModelPhenotypeMatch.of(0.7, dominant, termMatches)) : List.of();
        addAssociatedDiseases();
        addGeneScore(ModeOfInheritance.AUTOSOMAL_DOMINANT, matches);

        WrittenResults written = writeAndRead();
        assertThat(written.parquet().size(), equalTo(1));
        JsonNode row = written.parquet().getFirst();
        assertHumanMatches(row.path("diseaseMatches"), matches);
        assertAssociatedDiseases(row);
        assertThat(row.path("diseasePhenotypeScore").asDouble(), equalTo(0.0));
        assertThat(row.path("mouseMatches").size(), equalTo(0));
        assertThat(row.path("fishMatches").size(), equalTo(0));
        assertThat(row.path("ppiMatches").size(), equalTo(0));
        assertThat(written.tsv().getFirst().get("HUMAN_PHENO_EVIDENCE"), equalTo(""));
    }

    private WrittenResults writeAndRead() throws Exception {
        AnalysisResults results = AnalysisResults.builder().genes(List.of(gene)).build();
        OutputSettings settings = OutputSettings.builder().outputDirectory(tempDir).outputFileName("evidence").build();
        new ParquetResultsWriter().writeFile(results, settings);
        new TsvGeneResultsWriter().writeFile(results, settings);
        List<JsonNode> rows = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try (var connection = DriverManager.getConnection("jdbc:duckdb:");
             var statement = connection.prepareStatement("SELECT to_json(r) FROM read_parquet(?) r")) {
            statement.setString(1, tempDir.resolve("evidence.parquet").toString());
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapper.readTree(resultSet.getString(1)));
                }
            }
        }
        try (var reader = Files.newBufferedReader(tempDir.resolve("evidence.genes.tsv"));
             var parser = CSVFormat.TDF.withFirstRecordAsHeader().parse(reader)) {
            return new WrittenResults(rows, parser.getRecords());
        }
    }

    private void assertHumanMatches(JsonNode actual, List<ModelPhenotypeMatch<Disease>> expected) {
        assertThat(actual.size(), equalTo(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            ModelPhenotypeMatch<Disease> match = expected.get(i);
            assertThat(actual.get(i).path("diseaseId").asText(), equalTo(match.model().diseaseId()));
            assertThat(actual.get(i).path("score").asDouble(), equalTo(match.score()));
            assertTermMatches(actual.get(i).path("phenotypeMatches"), match.bestPhenotypeMatches());
        }
    }

    private void assertTermMatches(JsonNode actual, List<PhenotypeMatch> expected) {
        assertThat(actual.size(), equalTo(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            PhenotypeMatch match = expected.get(i);
            assertThat(actual.get(i).path("score").asDouble(), equalTo(match.score()));
            assertThat(actual.get(i).path("query").path("id").asText(), equalTo(match.queryPhenotype().id()));
            assertThat(actual.get(i).path("query").path("label").asText(), equalTo(match.queryPhenotype().label()));
            assertThat(actual.get(i).path("match").path("id").asText(), equalTo(match.matchPhenotype().id()));
            assertThat(actual.get(i).path("match").path("label").asText(), equalTo(match.matchPhenotype().label()));
        }
    }

    private void assertHumanTsvEvidence(JsonNode row, CSVRecord tsv) {
        JsonNode bestMatch = row.path("diseaseMatches").get(0);
        String diseaseId = bestMatch.path("diseaseId").asText();
        String diseaseName = "";
        for (JsonNode disease : row.path("associatedDiseases")) {
            if (disease.path("diseaseId").asText().equals(diseaseId)) {
                diseaseName = disease.path("diseaseName").asText();
            }
        }
        StringBuilder evidence = new StringBuilder(diseaseName + " (" + diseaseId + "): ");
        for (JsonNode term : bestMatch.path("phenotypeMatches")) {
            evidence.append(String.format("%s (%s)-%s (%s), ",
                    term.path("query").path("label").asText(), term.path("query").path("id").asText(),
                    term.path("match").path("label").asText(), term.path("match").path("id").asText()));
        }
        assertThat(evidence.toString().trim(), equalTo(tsv.get("HUMAN_PHENO_EVIDENCE")));
        assertThat(row.path("diseasePhenotypeScore").asDouble(), equalTo(Double.parseDouble(tsv.get("HUMAN_PHENO_SCORE"))));
    }

    private void assertAssociatedDiseases(JsonNode row) {
        JsonNode actual = row.path("associatedDiseases");
        assertThat(actual.size(), equalTo(3));
        for (int i = 0; i < 3; i++) {
            Disease disease = List.of(dominant, recessive, unknown).get(i);
            assertThat(actual.get(i).path("diseaseId").asText(), equalTo(disease.diseaseId()));
            assertThat(actual.get(i).path("diseaseName").asText(), equalTo(disease.diseaseName()));
            assertThat(actual.get(i).path("inheritanceMode").asText(), equalTo(disease.inheritanceMode().name()));
        }
    }

    private void assertOtherEvidence(JsonNode row) {
        assertThat(row.path("mousePhenotypeScore").asDouble(), equalTo(0.6));
        assertThat(row.path("fishPhenotypeScore").asDouble(), equalTo(0.5));
        assertThat(row.path("ppiScore").asDouble(), equalTo(0.4));
        for (String field : List.of("mouseMatches", "fishMatches", "ppiMatches")) {
            assertThat(row.path(field).size(), equalTo(1));
            assertTermMatches(row.path(field).get(0).path("phenotypeMatches"), termMatches);
        }
        assertThat(row.path("mouseMatches").get(0).path("mgiId").asText(), equalTo(mouse.model().id()));
        assertThat(row.path("fishMatches").get(0).path("zfinId").asText(), equalTo(fish.model().id()));
        assertThat(row.path("ppiMatches").get(0).path("id").asText(), equalTo(ppi.model().id()));
    }

    private void assertRowScores(JsonNode row, CSVRecord tsv, ModeOfInheritance mode) {
        assertThat(row.path("moi").asText(), equalTo(moi(mode)));
        assertThat(row.path("rank").asInt(), equalTo(Integer.parseInt(tsv.get("#RANK"))));
        assertThat(row.path("geneSymbol").asText(), equalTo(tsv.get("GENE_SYMBOL")));
        assertThat(row.path("geneCombinedScore").asDouble(), equalTo(0.8));
        assertThat(row.path("genePhenotypeScore").asDouble(), equalTo(0.9));
        assertThat(row.path("geneVariantScore").asDouble(), equalTo(0.7));
        assertThat(row.path("failedFilters").size(), equalTo(0));
        assertThat(row.path("acmgClassification").asText(), equalTo("NOT_AVAILABLE"));
    }

    private Disease disease(String id, String name, InheritanceMode mode) {
        return Disease.builder().diseaseId(id).diseaseName(name).associatedGeneId(gene.entrezGeneId())
                .associatedGeneSymbol(gene.geneSymbol()).inheritanceMode(mode)
                .phenotypeIds(List.of(query.id(), secondQuery.id())).build();
    }

    private GeneModelPhenotypeMatch diseaseMatch(Disease disease, double score) {
        return new GeneModelPhenotypeMatch(score, new GeneDiseaseModel("model_" + disease.diseaseId(), Organism.HUMAN, disease), termMatches);
    }

    private GeneModelPhenotypeMatch ortholog(String id, Organism organism, double score) {
        return new GeneModelPhenotypeMatch(score, new GeneOrthologModel(id, organism, gene.entrezGeneId(),
                gene.geneSymbol(), id, "synthetic model", List.of()), termMatches);
    }

    private HiPhivePriorityResult hiPhive(List<GeneModelPhenotypeMatch> evidence) {
        return new HiPhivePriorityResult(gene.entrezGeneId(), gene.geneSymbol(), 0.9,
                List.of(query, secondQuery), evidence, List.of(ppi), 0.4, false);
    }

    private void addAssociatedDiseases() {
        gene.addPriorityResult(new OmimPriorityResult(gene.entrezGeneId(), gene.geneSymbol(), 1.0,
                List.of(dominant, recessive, unknown), Map.of()));
    }

    private void addGeneScore(ModeOfInheritance mode, List<ModelPhenotypeMatch<Disease>> matches) {
        gene.setCompatibleInheritanceModes(Set.of(mode));
        gene.addGeneScore(GeneScore.builder().geneIdentifier(gene.geneIdentifier()).modeOfInheritance(mode)
                .combinedScore(0.8).phenotypeScore(0.9).variantScore(0.7).compatibleDiseaseMatches(matches).build());
    }

    private VariantEvaluation variant(int position, ModeOfInheritance mode) {
        return VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.oneBased(), position, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(mode)).contributingModes(Set.of(mode)).build();
    }

    private String moi(ModeOfInheritance mode) {
        return mode == ModeOfInheritance.ANY ? "ANY" : mode.getAbbreviation();
    }

    private record WrittenResults(List<JsonNode> parquet, List<CSVRecord> tsv) {}
}
