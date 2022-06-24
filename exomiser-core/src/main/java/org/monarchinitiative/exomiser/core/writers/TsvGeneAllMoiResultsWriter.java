/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsvGeneAllMoiResultsWriter implements ResultsWriter {
    private static final Logger logger = LoggerFactory.getLogger(TsvGeneAllMoiResultsWriter.class);
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_GENE;
    private final CSVFormat format = CSVFormat.newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE", "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "HUMAN_PHENO_SCORE", "MOUSE_PHENO_SCORE", "FISH_PHENO_SCORE", "WALKER_SCORE", "PHIVE_ALL_SPECIES_SCORE", "OMIM_SCORE", "MATCHES_CANDIDATE_GENE", "HUMAN_PHENO_EVIDENCE", "MOUSE_PHENO_EVIDENCE", "FISH_PHENO_EVIDENCE", "HUMAN_PPI_EVIDENCE", "MOUSE_PPI_EVIDENCE", "FISH_PPI_EVIDENCE");
    private final NumberFormat decimalFormat;

    public TsvGeneAllMoiResultsWriter() {
        this.decimalFormat = NumberFormat.getInstance(Locale.UK);
        this.decimalFormat.setMinimumFractionDigits(4);
        this.decimalFormat.setMaximumFractionDigits(4);
    }

    public void writeFile(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.getSample();
        String outFileName = ResultsWriterUtils.makeOutputFilename(sample.getVcfPath(), outputSettings.getOutputPrefix(), OUTPUT_FORMAT, ModeOfInheritance.ANY);
        Path outFile = Paths.get(outFileName);

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), this.format)){
            writeData(analysisResults, outputSettings, printer);
        } catch (IOException e) {
            logger.error("Unable to write results to file {}", outFileName, e);
        }

        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, modeOfInheritance.getAbbreviation(), outFileName);
    }

    public String writeString(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, OutputSettings outputSettings) {
        StringBuilder stringBuilder = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(stringBuilder, this.format)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (IOException e) {
            logger.error("Unable to write results to string {}", stringBuilder, e);
        }

        return stringBuilder.toString();
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings outputSettings, CSVPrinter printer) throws IOException {
        List<Gene> filteredGenesForOutput = outputSettings.filterGenesForOutput(analysisResults.getGenes());
        Map<GeneIdentifier, Gene> genesById = filteredGenesForOutput.stream()
                .collect(Collectors.toMap(Gene::getGeneIdentifier, Function.identity()));
        List<GeneScore> rankedGeneScores = filteredGenesForOutput.stream()
                .flatMap(gene -> gene.getCompatibleGeneScores().stream())
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        int rank = 1;

        for (GeneScore geneScore : rankedGeneScores) {
            List<String> geneRecord = this.makeGeneScoreRecord(rank++, genesById.get(geneScore.getGeneIdentifier()), geneScore);
            printer.printRecord(geneRecord);
        }

    }

    private List<String> makeGeneScoreRecord(int rank, Gene gene, GeneScore geneScore) {
        double humanPhenScore = 0.0;
        double mousePhenScore = 0.0;
        double fishPhenScore = 0.0;
        double walkerScore = 0.0;
        double phiveAllSpeciesScore = 0.0;
        double omimScore = 0.0;
        String phenoEvidence = "";
        int matchesCandidateGene = 0;

        for (PriorityResult prioritiserResult : gene.getPriorityResults().values()) {
            PriorityType type = prioritiserResult.getPriorityType();
            if (type == PriorityType.HIPHIVE_PRIORITY) {
                HiPhivePriorityResult phenoScore = (HiPhivePriorityResult) prioritiserResult;
                phiveAllSpeciesScore = phenoScore.getScore();
                humanPhenScore = phenoScore.getHumanScore();
                mousePhenScore = phenoScore.getMouseScore();
                fishPhenScore = phenoScore.getFishScore();
                walkerScore = phenoScore.getPpiScore();
                phenoEvidence = phenoScore.getPhenotypeEvidenceText();
                if (phenoScore.isCandidateGeneMatch()) {
                    matchesCandidateGene = 1;
                }
            } else if (type == PriorityType.OMIM_PRIORITY) {
                OmimPriorityResult omimPriorityResult = (OmimPriorityResult) prioritiserResult;
                omimScore = omimPriorityResult.getScoreForMode(geneScore.getModeOfInheritance());
            } else if (type == PriorityType.EXOMEWALKER_PRIORITY) {
                walkerScore = prioritiserResult.getScore();
            }
        }
        List<String> values = new ArrayList<>(16);
        ModeOfInheritance modeOfInheritance = geneScore.getModeOfInheritance();
        String moiAbbreviation = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        values.add(Integer.toString(rank));
        String geneSymbol = gene.getGeneSymbol();
        values.add(geneSymbol + "_" + moiAbbreviation);
        values.add(geneSymbol);
        values.add(Integer.toString(gene.getEntrezGeneID()));
        values.add(moiAbbreviation);
        values.add(this.decimalFormat.format(geneScore.pValue()));
        values.add(this.decimalFormat.format(geneScore.getCombinedScore()));
        values.add(this.decimalFormat.format(geneScore.getPhenotypeScore()));
        values.add(this.decimalFormat.format(geneScore.getVariantScore()));
        values.add(this.decimalFormat.format(humanPhenScore));
        values.add(this.decimalFormat.format(mousePhenScore));
        values.add(this.decimalFormat.format(fishPhenScore));
        values.add(this.decimalFormat.format(walkerScore));
        values.add(this.decimalFormat.format(phiveAllSpeciesScore));
        values.add(this.decimalFormat.format(omimScore));
        values.add(Integer.toString(matchesCandidateGene));
        values.add(phenoEvidence);
        return values;
    }
}
