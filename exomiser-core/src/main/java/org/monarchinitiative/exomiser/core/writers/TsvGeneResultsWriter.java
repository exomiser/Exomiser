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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * @since 13.1.0
 */
public class TsvGeneResultsWriter implements ResultsWriter {
    private static final Logger logger = LoggerFactory.getLogger(TsvGeneResultsWriter.class);
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_GENE;
    private final CSVFormat csvFormat = CSVFormat.newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE", "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE", "HUMAN_PHENO_SCORE", "MOUSE_PHENO_SCORE", "FISH_PHENO_SCORE", "WALKER_SCORE", "PHIVE_ALL_SPECIES_SCORE", "OMIM_SCORE", "MATCHES_CANDIDATE_GENE", "HUMAN_PHENO_EVIDENCE", "MOUSE_PHENO_EVIDENCE", "FISH_PHENO_EVIDENCE", "HUMAN_PPI_EVIDENCE", "MOUSE_PPI_EVIDENCE", "FISH_PPI_EVIDENCE");

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    public TsvGeneResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    public void writeFile(AnalysisResults analysisResults, OutputSettings outputSettings) {
        Sample sample = analysisResults.getSample();
        String outFileName = ResultsWriterUtils.makeOutputFilename(sample.getVcfPath(), outputSettings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Path.of(outFileName);

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), this.csvFormat)){
            writeData(analysisResults, outputSettings, printer);
        } catch (Exception e) {
            logger.error("Unable to write results to file {}", outFileName, e);
        }

        logger.debug("{} results written to file {}", OUTPUT_FORMAT, outFileName);
    }

    public String writeString(AnalysisResults analysisResults, OutputSettings outputSettings) {
        StringBuilder stringBuilder = new StringBuilder();

        try (CSVPrinter printer = new CSVPrinter(stringBuilder, this.csvFormat)) {
            this.writeData(analysisResults, outputSettings, printer);
        } catch (IOException e) {
            logger.error("Unable to write results to string {}", stringBuilder, e);
        }

        return stringBuilder.toString();
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings outputSettings, CSVPrinter printer) {
        GeneScoreRanker geneScoreRanker = new GeneScoreRanker(analysisResults, outputSettings);
        geneScoreRanker.rankedGenes()
                .map(rankedGene -> makeGeneScoreRecord(rankedGene.rank(), rankedGene.gene(), rankedGene.geneScore()))
                .forEach(printRecord(printer));
    }

    Consumer<Iterable<String>> printRecord(CSVPrinter printer) {
        return strings -> {
            try {
                printer.printRecord(strings);
            } catch (IOException e) {
                // cross fingers and swallow?
                throw new IllegalStateException(e);
            }
        };
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
            if (prioritiserResult instanceof HiPhivePriorityResult hiPhiveResult) {
                phiveAllSpeciesScore = hiPhiveResult.getScore();
                humanPhenScore = hiPhiveResult.getHumanScore();
                mousePhenScore = hiPhiveResult.getMouseScore();
                fishPhenScore = hiPhiveResult.getFishScore();
                walkerScore = hiPhiveResult.getPpiScore();
                phenoEvidence = hiPhiveResult.getPhenotypeEvidenceText();
                if (hiPhiveResult.isCandidateGeneMatch()) {
                    matchesCandidateGene = 1;
                }
            } else if (prioritiserResult instanceof OmimPriorityResult omimResult) {
                omimScore = omimResult.getScoreForMode(geneScore.getModeOfInheritance());
            } else if (prioritiserResult instanceof ExomeWalkerPriorityResult walkerResult) {
                walkerScore = walkerResult.getScore();
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
        values.add(decimalFormat.format(geneScore.pValue()));
        values.add(decimalFormat.format(geneScore.getCombinedScore()));
        values.add(decimalFormat.format(geneScore.getPhenotypeScore()));
        values.add(decimalFormat.format(geneScore.getVariantScore()));
        values.add(decimalFormat.format(humanPhenScore));
        values.add(decimalFormat.format(mousePhenScore));
        values.add(decimalFormat.format(fishPhenScore));
        values.add(decimalFormat.format(walkerScore));
        values.add(decimalFormat.format(phiveAllSpeciesScore));
        values.add(decimalFormat.format(omimScore));
        values.add(Integer.toString(matchesCandidateGene));
        values.add(phenoEvidence);
        return values;
    }
}
