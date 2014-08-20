/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.priority.*;
import de.charite.compbio.exomiser.priority.GenewandererPriorityScore;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.TSV;

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            writer.write(writeString(sampleData, settings, priorityList));
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        //this is either empty or has a gene name
        String candidateGene = settings.getCandidateGene();
        StringBuilder stringBuilder = new StringBuilder();
        for (Gene gene : sampleData.getGeneList()) {
            if (gene.passesFilters()) {
                stringBuilder.append(makeGeneLine(gene, candidateGene));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Writes out the gene data in a tab delimited string ending in a newline
     * character.
     *
     * @param gene
     * @param candidateGene
     * @return
     */
    protected String makeGeneLine(Gene gene, String candidateGene) {
        float humanPhenScore = 0f;
        float mousePhenScore = 0f;
        float fishPhenScore = 0f;
        float rawWalkerScore = 0f;
        float scaledMaxScore = 0f;
        float walkerScore = 0f;
        float exomiser2Score = 0f;
        float omimScore = 0f;
        // priority score calculation
        for (PriorityType i : gene.getPriorityScoreMap().keySet()) {
            PriorityScore priorityScore = gene.getPriorityScoreMap().get(i);
            if (i == PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY) {
                DynamicPhenoWandererRelevanceScore phenoScore = (DynamicPhenoWandererRelevanceScore) priorityScore;
                exomiser2Score = phenoScore.getScore();
                humanPhenScore = phenoScore.getHumanScore();
                mousePhenScore = phenoScore.getMouseScore();
                fishPhenScore = phenoScore.getFishScore();
                walkerScore = phenoScore.getWalkerScore();
            } else if (i == PriorityType.OMIM_PRIORITY) {
                omimScore = priorityScore.getScore();
            } else if (i == PriorityType.GENEWANDERER_PRIORITY) {
                GenewandererPriorityScore wandererScore = (GenewandererPriorityScore) priorityScore;
                walkerScore = priorityScore.getScore();
                rawWalkerScore = (float) wandererScore.getRawScore();
                scaledMaxScore = (float) wandererScore.getScaledScore();
            }
        }
        //pathogenicity and frequency variant scores
        float maxFreq = 0f;
        float pathogenicityScore = 0f;
        float polyphen = Float.NaN;
        float sift = Float.NaN;
        float mutTaster = Float.NaN;
        float caddRaw = Float.NaN;
        String variantType = "";
        for (VariantEvaluation ve : gene.getVariantList()) {
            Map<FilterType, FilterScore> filterScoreMap = ve.getFilterScoreMap();
            //FilterType.PATHOGENICITY_FILTER is always run so in theory should never be null....
            if (ve.passedFilter(FilterType.PATHOGENICITY_FILTER)) {
                FilterScore pathogenicityFilterScore = ve.getFilterScore(FilterType.PATHOGENICITY_FILTER);
                if (pathogenicityFilterScore.getScore() > pathogenicityScore) {
                    variantType = ve.getVariantType().toString();
                    //reset the pathogenicity scores as this variant now has the best result
                    pathogenicityScore = pathogenicityFilterScore.getScore();
                    PathogenicityData pathData = ve.getPathogenicityData();
                    PolyPhenScore polyPhenScore = pathData.getPolyPhenScore();
                    if (polyPhenScore != null){
                        polyphen = polyPhenScore.getScore();
                    }
                    SiftScore siftScore = pathData.getSiftScore();
                    if (siftScore != null){
                        sift = siftScore.getScore();
                    }
                    MutationTasterScore mutationTasterScore = pathData.getMutationTasterScore();
                    if (mutationTasterScore != null){
                        mutTaster = mutationTasterScore.getScore();
                    }
                    CaddScore caddScore = pathData.getCaddScore();
                    if (caddScore != null){
                        caddRaw = caddScore.getScore();
                    }
                 }
            }
            if (ve.passedFilter(FilterType.FREQUENCY_FILTER)) {
                maxFreq = ve.getFrequencyData().getMaxFreq();
            }
        }
        //flag to indicate if the gene matches the candidate gene specified by the user
        int matchesCandidateGene = 0;
        if (gene.getGeneSymbol().equals(candidateGene) || gene.getGeneSymbol().startsWith(candidateGene + ",")) {// bug fix for new Jannovar labelling where can have multiple genes per var but first one is most pathogenic
            matchesCandidateGene = 1;
        }

        return String.format("%s\t%d\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%s\t%d\n",
                gene.getGeneSymbol(),
                gene.getEntrezGeneID(),
                gene.getPriorityScore(),
                gene.getFilterScore(),
                gene.getCombinedScore(),
                humanPhenScore,
                mousePhenScore,
                fishPhenScore,
                rawWalkerScore,
                scaledMaxScore,
                walkerScore,
                exomiser2Score,
                omimScore,
                maxFreq,
                pathogenicityScore,
                polyphen,
                sift,
                mutTaster,
                caddRaw,
                variantType,
                matchesCandidateGene);
    }

}
