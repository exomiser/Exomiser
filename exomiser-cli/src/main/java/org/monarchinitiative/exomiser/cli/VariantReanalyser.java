package org.monarchinitiative.exomiser.cli;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.writers.TsvVariantResultsWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Reanalyses two set of results for a patient where no diagnosis was able to be made in the initial case to automatically
 * flag any variants with a significant change which warrant manual re-interpretation.
 * <p>
 * Compares two exomiser variant.tsv analysis results files for a particular sample i.e. a historic and a most recent 
 * analysis using different database builds, and flags up any variants which have a variant score >= 0.8 and an increase
 * in human phenotype score >= 0.2 or a change in ACMG classification from VUS -> P/LP.
 * 
 * @since 14.0.0
 */
public class VariantReanalyser implements Callable<Integer> {

    // variant score ≥ 0.8  and an increase in human phenotype score ≥ 0.2 || ACMG VUS -> P/LP

    private final Path originalPath;
    private final Path reanalysedPath;
//    private final Path outPath;

    public VariantReanalyser(Path originalPath, Path reanalysedPath, Path outPath) {
        this.originalPath = originalPath;
        this.reanalysedPath = reanalysedPath;
//        this.outPath = outPath;
    }

    public Integer call() {
        // read original
        try {
            Map<String, ResultRecord> originalResults = readResults(originalPath);
            Map<String, ResultRecord> reanalysedResults = readResults(reanalysedPath);

            List<ReclassificationCandidate> reclassificationCandidates = findReclassificationCandidates(originalResults, reanalysedResults);
            reclassificationCandidates.forEach(rc -> System.out.println(rc.printUpdate()));
//            writeOutFile(reclassificationCandidates);
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

    protected List<ReclassificationCandidate> findReclassificationCandidates(Map<String, ResultRecord> originalResults, Map<String, ResultRecord> reanalysedResults) {
        List<ReclassificationCandidate> results = new ArrayList<>();
        for (Map.Entry<String, ResultRecord> original : originalResults.entrySet()) {
            String originalVariantKey = original.getKey();
            if (reanalysedResults.containsKey(originalVariantKey)) {
                var originalResult = original.getValue();
                var reanalysedResult = reanalysedResults.get(originalVariantKey);
                if (reanalysedResult.humanPhenoScore >= originalResult.humanPhenoScore + 0.2f || acmgChanged(originalResult.acmgClassification, reanalysedResult.acmgClassification)) {
                    ReclassificationCandidate reclassificationCandidate = new ReclassificationCandidate(originalResult, reanalysedResult);
                    results.add(reclassificationCandidate);
                }
            }
        }
        return results;
    }

    private boolean acmgChanged(AcmgClassification originalClassification, AcmgClassification reanalysedClassification) {
        return originalClassification == AcmgClassification.UNCERTAIN_SIGNIFICANCE && (reanalysedClassification == AcmgClassification.PATHOGENIC || reanalysedClassification == AcmgClassification.LIKELY_PATHOGENIC);
    }

    private Map<String, ResultRecord> readResults(Path tsvVariantsFile) {
        LinkedHashMap<String, ResultRecord> results = new LinkedHashMap<>();
        CSVFormat variantsTsvFormat = TsvVariantResultsWriter.EXOMISER_VARIANTS_TSV_FORMAT;
        String delimiter = String.valueOf(variantsTsvFormat.getDelimiter());
        try (CSVParser csvParser = CSVParser.parse(tsvVariantsFile.toFile(), StandardCharsets.UTF_8, variantsTsvFormat)){
            for (CSVRecord csvRecord : csvParser) {
                int rank = Integer.parseInt(csvRecord.get("#RANK"));
                String id = csvRecord.get("ID");
                float combinedScore = Float.parseFloat(csvRecord.get("EXOMISER_GENE_COMBINED_SCORE"));
                float humanPhenoScore = Float.parseFloat(csvRecord.get("EXOMISER_GENE_PHENO_SCORE"));
                AcmgClassification acmgClassification = AcmgClassification.valueOf(csvRecord.get("EXOMISER_ACMG_CLASSIFICATION"));
                String line = line(delimiter, csvRecord);
                ResultRecord resultRecord = new ResultRecord(rank, id, combinedScore, humanPhenoScore, acmgClassification, line);
                results.put(id, resultRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private String line(String delimiter, CSVRecord csvRecord) {
        String[] objects = new String[csvRecord.size()];
        for (int i = 0; i < csvRecord.size(); i++) {
            objects[i] = csvRecord.get(i);
        }
        return String.join(delimiter, objects);
    }

    public record ResultRecord(
            int rank,
            String variant,
            float combinedScore,
            float humanPhenoScore,
            AcmgClassification acmgClassification,
            String line
    ) {}

    public record ReclassificationCandidate(ResultRecord original, ResultRecord reanalysed) {

        public String printUpdate() {
            return "- " + original.line + System.lineSeparator() +
                    "+ " + reanalysed.line + System.lineSeparator();
        }
    }
}
