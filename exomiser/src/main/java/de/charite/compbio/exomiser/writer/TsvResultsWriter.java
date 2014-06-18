/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.common.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.filter.FrequencyVariantScore;
import de.charite.compbio.exomiser.filter.PathogenicityVariantScore;
import de.charite.compbio.exomiser.priority.DynamicPhenoWandererRelevanceScore;
import de.charite.compbio.exomiser.priority.GeneScore;
import de.charite.compbio.exomiser.priority.GenewandererRelevanceScore;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.util.ExomiserSettings;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvResultsWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(TsvResultsWriter.class);

    @Override
    public void write(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList) {
        String outFileName = settings.getOutFileName();
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            //this is either empty or has a gene name
            String candidateGene = settings.getCandidateGene();

            for (Gene gene : sampleData.getGeneList()) {
                String s = makeGeneLine(gene, candidateGene);
                writer.write(s);    
            }
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("Results written to file {}.", outFileName);

    }

    /**
     * Writes out the gene data in a tab delimited string ending in a newline character. 
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
        for (PriorityType i : gene.getRelevanceMap().keySet()) {
            GeneScore geneScore = gene.getRelevanceMap().get(i);
            if (i == PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY) {
                DynamicPhenoWandererRelevanceScore phenoScore = (DynamicPhenoWandererRelevanceScore) geneScore;
                exomiser2Score = phenoScore.getScore();
                humanPhenScore = phenoScore.getHumanScore();
                mousePhenScore = phenoScore.getMouseScore();
                fishPhenScore = phenoScore.getFishScore();
                walkerScore = phenoScore.getWalkerScore();
            } else if (i == PriorityType.OMIM_PRIORITY) {
                omimScore = geneScore.getScore();
            } else if (i == PriorityType.GENEWANDERER_PRIORITY) {
                GenewandererRelevanceScore wandererScore = (GenewandererRelevanceScore) geneScore;
                walkerScore = geneScore.getScore();
                rawWalkerScore = (float) wandererScore.getRawScore();
                scaledMaxScore = (float) wandererScore.getScaledScore();
            }
        }
        //pathogenicity and frequency variant scores
        float maxFreq = 0f;
        float pathogenicityScore = 0f;
        float polyphen = 0f;
        float sift = 0f;
        float mutTaster = 0f;
        float caddRaw = 0f;
        String variantType = "";
        for (VariantEvaluation ve : gene.getVariantList()) {
            //FilterType.PATHOGENICITY_FILTER is always run so in theory should never be null....
            PathogenicityVariantScore pathogenicityTriage = (PathogenicityVariantScore) ve.getVariantScoreMap().get(FilterType.PATHOGENICITY_FILTER);
            if (pathogenicityTriage.filterResult() > pathogenicityScore) {
                variantType = ve.getVariantType();
                //reset the pathogenicity scores as this variant now has the best result
                pathogenicityScore = pathogenicityTriage.filterResult();
                polyphen = pathogenicityTriage.getPolyphen();
                sift = pathogenicityTriage.getSift();
                mutTaster = pathogenicityTriage.getMutTaster();
                caddRaw = pathogenicityTriage.getCADDRaw();
                FrequencyVariantScore ft = (FrequencyVariantScore) ve.getVariantScoreMap().get(FilterType.FREQUENCY_FILTER);
                maxFreq = ft.getMaxFreq();
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
