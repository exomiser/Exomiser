package de.charite.compbio.exomiser.io.tsv;

import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.filter.FrequencyVariantScore;
import de.charite.compbio.exomiser.filter.PathogenicityVariantScore;
import de.charite.compbio.exomiser.filter.VariantScore;
import de.charite.compbio.exomiser.priority.DynamicPhenoWandererRelevanceScore;
import de.charite.compbio.exomiser.priority.GeneScore;
import de.charite.compbio.exomiser.priority.GenewandererRelevanceScore;
import de.charite.compbio.exomiser.priority.PriorityType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author ds5
 */
public class TSVWriter {

    /**
     * File handle to write output.
     */
    private Writer out;

    /**
     * This constructor opens a new file handle for writing.
     *
     * @param fname The name of the output file that will be created
     */
    public TSVWriter(String fname) {
        try {
            FileWriter fstream = new FileWriter(fname);
            this.out = new BufferedWriter(fstream);
        } catch (IOException e) {
            String s = String.format("Error initializing TSVWriter: %s", e.getMessage());
        }
    }

    /**
     * Write the main output
     *
     * @param geneList List of genes, assumed to be sorted in prioritized order.
     * @param candidateGene Gene that is known to be causative
     */
    public void writeTSV(List<Gene> geneList, String candidateGene)
            throws IOException {
        for (Gene g : geneList) {
            float humanPhenScore = 0f;
            float mousePhenScore = 0f;
            float fishPhenScore = 0f;
            float rawWalkerScore = 0f;
            float scaledMaxScore = 0f;
            float walkerScore = 0f;
            float exomiser2Score = 0f;
            float omimScore = 0f;
            float maxFreq = 0f;
            float pathogenicityScore = 0f;
            float polyphen = 0f;
            float sift = 0f;
            float mutTaster = 0f;
            float caddRaw = 0f;
            String variantType = "";
            // priority score calculation
            for (PriorityType i : g.getRelevanceMap().keySet()) {
                GeneScore r = g.getRelevanceMap().get(i);
                float x = r.getScore();
                if (i == PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY) {
                    exomiser2Score = x;
                    humanPhenScore = ((DynamicPhenoWandererRelevanceScore) r).getHumanScore();
                    mousePhenScore = ((DynamicPhenoWandererRelevanceScore) r).getMouseScore();
                    fishPhenScore = ((DynamicPhenoWandererRelevanceScore) r).getFishScore();
                    walkerScore = ((DynamicPhenoWandererRelevanceScore) r).getWalkerScore();
                } else if (i == PriorityType.OMIM_PRIORITY) {
                    omimScore = x;
                } else if (i == PriorityType.GENEWANDERER_PRIORITY) {
                    walkerScore = x;
                    rawWalkerScore = (float) ((GenewandererRelevanceScore) r).getRawScore();
                    scaledMaxScore = (float) ((GenewandererRelevanceScore) r).getScaledScore();
                }
            }
            for (VariantEvaluation ve : g.getVariantList()) {
                float x = ve.getFilterScore();
                for (FilterType i : ve.getVariantScoreMap().keySet()) {
                    VariantScore itria = ve.getVariantScoreMap().get(i);
                    if (itria instanceof PathogenicityVariantScore) {
                        if (((PathogenicityVariantScore) itria).filterResult() > pathogenicityScore) {
                            variantType = ve.getVariantType();
                            pathogenicityScore = ((PathogenicityVariantScore) itria).filterResult();
                            polyphen = ((PathogenicityVariantScore) itria).getPolyphen();
                            sift = ((PathogenicityVariantScore) itria).getSift();
                            mutTaster = ((PathogenicityVariantScore) itria).getMutTaster();
                            caddRaw = ((PathogenicityVariantScore) itria).getCADDRaw();
                            FrequencyVariantScore ft = (FrequencyVariantScore) ve.getVariantScoreMap().get(FilterType.FREQUENCY_FILTER);
                            maxFreq = ft.getMaxFreq();
                        }
                    }
                }
            }

            String s = String.format("%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%s",
                    g.getGeneSymbol(),
                    g.getEntrezGeneID(),
                    g.getPriorityScore(),
                    g.getFilterScore(),
                    g.getCombinedScore(),
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
                    variantType);

            out.write(s);

            if (candidateGene == null) {
                out.write("\n");
            } else {
                if (candidateGene.equals(g.getGeneSymbol())) {
                    out.write("\t1\n");
                } else if (g.getGeneSymbol().startsWith(candidateGene + ",")) {// bug fix for new Jannovar labelling where can have multiple genes per var but first one is most pathogenic
                    out.write("\t1\n");
                } else {
                    out.write("\t0\n");
                }
            }
        }
    }
}