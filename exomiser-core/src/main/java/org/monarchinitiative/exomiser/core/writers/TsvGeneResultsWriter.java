/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvGeneResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_GENE;

    private final CSVFormat format = CSVFormat
            .newFormat('\t')
            .withQuote(null)
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("#GENE_SYMBOL",
                    "ENTREZ_GENE_ID",
                    "EXOMISER_GENE_PHENO_SCORE",
                    "EXOMISER_GENE_VARIANT_SCORE",
                    "EXOMISER_GENE_COMBINED_SCORE",
                    "HUMAN_PHENO_SCORE",
                    "MOUSE_PHENO_SCORE",
                    "FISH_PHENO_SCORE",
                    "WALKER_SCORE",
                    "PHIVE_ALL_SPECIES_SCORE",
                    "OMIM_SCORE",
                    "MATCHES_CANDIDATE_GENE",
                    "HUMAN_PHENO_EVIDENCE",
                    "MOUSE_PHENO_EVIDENCE",
                    "FISH_PHENO_EVIDENCE",
                    "HUMAN_PPI_EVIDENCE",
                    "MOUSE_PPI_EVIDENCE",
                    "FISH_PPI_EVIDENCE"
            );

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    public TsvGeneResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT, modeOfInheritance);
        Path outFile = Paths.get(outFileName);
        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outFile, StandardCharsets.UTF_8), format)) {
            writeData(modeOfInheritance, analysisResults, printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}", outFileName, ex);
        }
        logger.debug("{} {} results written to file {}", OUTPUT_FORMAT, modeOfInheritance.getAbbreviation(), outFileName);
    }

    @Override
    public String writeString(ModeOfInheritance modeOfInheritance, Analysis analysis, AnalysisResults analysisResults, OutputSettings settings) {
        StringBuilder stringBuilder = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(stringBuilder, format)) {
            writeData(modeOfInheritance, analysisResults, printer);
        } catch (IOException ex) {
            logger.error("Unable to write results to string {}", stringBuilder, ex);
        }
        return stringBuilder.toString();
    }

    private void writeData(ModeOfInheritance modeOfInheritance, AnalysisResults analysisResults, CSVPrinter printer) throws IOException {
        for (Gene gene : analysisResults.getGenes()) {
            if (gene.passedFilters() && gene.isCompatibleWith(modeOfInheritance)) {
                List<String> geneRecord = makeGeneRecord(modeOfInheritance, gene);
                printer.printRecord(geneRecord);
            }
        }
    }

    private List<String> makeGeneRecord(ModeOfInheritance modeOfInheritance, Gene gene) {
        double humanPhenScore = 0;
        double mousePhenScore = 0;
        double fishPhenScore = 0;
        double walkerScore = 0;
        double phiveAllSpeciesScore = 0;
        double omimScore = 0;
        String phenoEvidence = "";
        //flag to indicate if the gene matches the candidate gene specified by the user
        int matchesCandidateGene = 0;

        // priority score calculation
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
                omimScore = omimPriorityResult.getScoreForMode(modeOfInheritance);
            } else if (type == PriorityType.EXOMEWALKER_PRIORITY) {
                walkerScore = prioritiserResult.getScore();
            }
        }
        ArrayList<String> values = new ArrayList<>(13);

        values.add(gene.getGeneSymbol());
        values.add(Integer.toString(gene.getEntrezGeneID()));
        values.add(decimalFormat.format(gene.getPriorityScoreForMode(modeOfInheritance)));
        values.add(decimalFormat.format(gene.getVariantScoreForMode(modeOfInheritance)));
        values.add(decimalFormat.format(gene.getCombinedScoreForMode(modeOfInheritance)));
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
