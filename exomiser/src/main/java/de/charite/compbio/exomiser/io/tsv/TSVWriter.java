package de.charite.compbio.exomiser.io.tsv;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.FrequencyTriage;
import de.charite.compbio.exomiser.filter.PathogenicityTriage;
import de.charite.compbio.exomiser.filter.Triage;
import de.charite.compbio.exomiser.io.html.HTMLTable;
import de.charite.compbio.exomiser.priority.DynamicPhenoWandererRelevanceScore;
import de.charite.compbio.exomiser.priority.GenewandererRelevanceScore;
import de.charite.compbio.exomiser.priority.RelevanceScore;
import jannovar.pedigree.Pedigree;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ds5
 */
public class TSVWriter {

    /**
     * File handle to write output.
     */
    protected Writer out = null;

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
            for (FilterType i : g.getRelevanceMap().keySet()) {
                RelevanceScore r = g.getRelevanceMap().get(i);
                float x = r.getRelevanceScore();
                if (i == FilterType.DYNAMIC_PHENOWANDERER_FILTER) {
                    exomiser2Score = x;
                    humanPhenScore = ((DynamicPhenoWandererRelevanceScore) r).getHumanScore();
                    mousePhenScore = ((DynamicPhenoWandererRelevanceScore) r).getMouseScore();
                    fishPhenScore = ((DynamicPhenoWandererRelevanceScore) r).getFishScore();
                    walkerScore = ((DynamicPhenoWandererRelevanceScore) r).getWalkerScore();
                } else if (i == FilterType.OMIM_FILTER) {
                    omimScore = x;
                } else if (i == FilterType.GENEWANDERER_FILTER) {
                    walkerScore = x;
                    rawWalkerScore = (float) ((GenewandererRelevanceScore) r).getRawScore();
                    scaledMaxScore = (float) ((GenewandererRelevanceScore) r).getScaledScore();
                }
            }
            for (VariantEvaluation ve : g.getVariantList()) {
                float x = ve.getFilterScore();
                for (FilterType i : ve.getTriageMap().keySet()) {
                    Triage itria = ve.getTriageMap().get(i);
                    if (itria instanceof PathogenicityTriage) {
                        if (((PathogenicityTriage) itria).filterResult() > pathogenicityScore) {
                            variantType = ve.getVariantType();
                            pathogenicityScore = ((PathogenicityTriage) itria).filterResult();
                            polyphen = ((PathogenicityTriage) itria).getPolyphen();
                            sift = ((PathogenicityTriage) itria).getSift();
                            mutTaster = ((PathogenicityTriage) itria).getMutTaster();
                            caddRaw = ((PathogenicityTriage) itria).getCADDRaw();
                            FrequencyTriage ft = (FrequencyTriage) ve.getTriageMap().get(FilterType.FREQUENCY_FILTER);
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