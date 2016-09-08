/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvGeneResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV_GENE;
    private static final String HEADER_LINE = "#GENE_SYMBOL	ENTREZ_GENE_ID	"
            + "EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE	"
            + "HUMAN_PHENO_SCORE	MOUSE_PHENO_SCORE	FISH_PHENO_SCORE	WALKER_SCORE	"
            + "PHIVE_ALL_SPECIES_SCORE	OMIM_SCORE	MATCHES_CANDIDATE_GENE	HUMAN_PHENO_EVIDENCE	MOUSE_PHENO_EVIDENCE	FISH_PHENO_EVIDENCE	HUMAN_PPI_EVIDENCE	MOUSE_PPI_EVIDENCE	FISH_PPI_EVIDENCE\n";

    public TsvGeneResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(Analysis analysis, SampleData sampleData, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            writer.write(writeString(analysis, sampleData, settings));
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, SampleData sampleData, OutputSettings settings) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HEADER_LINE);

        for (Gene gene : sampleData.getGenes()) {
            if (gene.passedFilters()) {
                stringBuilder.append(makeGeneLine(gene));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Writes out the gene data in a tab delimited string ending in a newline
     * character.
     *
     * @param gene
     * @return
     */
    protected String makeGeneLine(Gene gene) {
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
                walkerScore = phenoScore.getWalkerScore();
                phenoEvidence = phenoScore.getPhenotypeEvidenceText();
                if (phenoScore.isCandidateGeneMatch()) {
                    matchesCandidateGene = 1;
                }
            } else if (type == PriorityType.OMIM_PRIORITY) {
                omimScore = prioritiserResult.getScore();
            } else if (type == PriorityType.EXOMEWALKER_PRIORITY) {
                walkerScore = prioritiserResult.getScore();
            }
        }

        return String.format("%s\t%d\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%s\n",
                gene.getGeneSymbol(),
                gene.getEntrezGeneID(),
                gene.getPriorityScore(),
                gene.getFilterScore(),
                gene.getCombinedScore(),
                humanPhenScore,
                mousePhenScore,
                fishPhenScore,
                walkerScore,
                phiveAllSpeciesScore,
                omimScore,
                matchesCandidateGene,
                phenoEvidence);
    }

}
