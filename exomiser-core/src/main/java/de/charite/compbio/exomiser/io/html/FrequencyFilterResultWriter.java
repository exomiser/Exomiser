/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.frequency.Frequency;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.frequency.RsId;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to write-out objects for the HTML classes.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyFilterResultWriter implements FilterResultWriter {

    /**
     * @return A string with a summary of the filtering results (intended for
     * HTML).
     */
    public String getFilterResultSummary(VariantEvaluation variantEvaluation) {
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        
        if (frequencyData == null) {
            return("No frequency data found<br/>\n");
        }
        
        RsId rsId = frequencyData.getRsId();
        Frequency dbSnpMaf = frequencyData.getDbSnpMaf();
        StringBuilder sb = new StringBuilder();
        if (rsId != null) {
            int dbSnpId = rsId.getId();
            String url = String.format("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=%d", dbSnpId);
            sb.append(String.format("<a href=\"%s\" target=\"_new%s\">rs%s</a>", url, dbSnpId, dbSnpId));
            if (dbSnpMaf != null) {
                sb.append(String.format(" MAF: %.4f", dbSnpMaf.getFrequency()));
            } 
            sb.append("<br/>\n");
        }
        
        Frequency espAll = frequencyData.getEspAllMaf();
        Frequency espEA = frequencyData.getEspEaMaf();
        Frequency espAA = frequencyData.getEspAaMaf();
        
        String allString = "-";
        if (espAll != null) {
            allString = String.format("%.3f%%", espAll);
        }
        String eaString = "-";
        if (espEA != null) {
            eaString = String.format("%.3f%%", espEA);
        }
        String aaString = "-";
        if (espAA != null) {
            aaString = String.format("%.3f%%", espAA);
        }
        sb.append(String.format("ESP: all %s, EA %s, AA %s<br/>\n", allString, eaString, aaString));
        
        if (sb.length() == 0) {
            sb.append("No frequency data found<br/>\n");
        }
        return sb.toString();
    }

    /**
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired.
     */
    public List<String> getFilterResultList(VariantEvaluation variantEvaluation) {
        List<String> resultsList = new ArrayList<>();

        
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        
        if (frequencyData == null) {
            resultsList.add(".");
            return resultsList;
        }
        
        RsId rsId = frequencyData.getRsId();
        Frequency dbSnpMaf = frequencyData.getDbSnpMaf();
        if (rsId != null) {
            if (dbSnpMaf != null) {
                resultsList.add(String.format("rs%s: %.4f", rsId.getId(), dbSnpMaf.getFrequency()));
            } else {
                resultsList.add(String.format("rs%s", rsId.getId()));
            }
        } else if (dbSnpMaf != null) {
            resultsList.add(String.format("dbSNP: %.4f", dbSnpMaf.getFrequency()));
        }

        Frequency espAll = frequencyData.getEspAllMaf();
        Frequency espEA = frequencyData.getEspEaMaf();
        Frequency espAA = frequencyData.getEspAaMaf();

        String allString = "-";
        if (espAll != null) {
            allString = String.format("%.4f", espAll);
        }
        String eaString = "-";
        if (espEA != null) {
            eaString = String.format("%.4f", espEA);
        }
        String aaString = "-";
        if (espAA != null) {
            aaString = String.format("%.4f", espAA);
        }
        resultsList.add(String.format("ESP: all %s, EA %s, AA %s", allString, eaString, aaString));
            
        if (resultsList.isEmpty()) {
            resultsList.add(".");
        }
        return resultsList;
    }

    /**
     * @return HTML code for the contents of the cell representing the frequency
     * evaluation
     */
    public String getHTMLCode(VariantEvaluation variantEvaluation) {
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>\n");
        RsId rsId = frequencyData.getRsId();
        Frequency dbSnpMaf = frequencyData.getDbSnpMaf();
        if (rsId != null) { // NOTE SOMEWHERE CODE IS ENTERING -1 instead of UNINITIALIZED_INT) {
            String url = String.format("<a href=\"http://www.ncbi.nlm.nih.gov/snp/?term=rs%d\">rs%d</a>",
                    rsId.getId(), rsId.getId());
            if (dbSnpMaf != null) {
                sb.append(String.format("<li>%s: %.2f%%</li>", url, dbSnpMaf.getFrequency()));
            } else {
                sb.append(String.format("<li>%s (no frequency data)</li>\n", url));
            }
        } else {
            sb.append("<li>No dbSNP entry found</li>\n");
        }
        Frequency espAll = frequencyData.getEspAllMaf();
        Frequency espEA = frequencyData.getEspEaMaf();
        Frequency espAA = frequencyData.getEspAaMaf();
        if (espAll != null) {
            sb.append("<li>ESP:<ul>");
            sb.append(String.format("<li>all %.4f%%</li>", espAll.getFrequency()));
            if (espEA != null) {
                sb.append(String.format("<li>EA %.4f%%</li>", espEA.getFrequency()));
            }
            if (espAA != null) {
                sb.append(String.format("<li>AA %.4f%%</li>", espAA.getFrequency()));
            }
            sb.append("</ul></li>\n");
        }
        sb.append("</ul>\n");
        return sb.toString();
    }
}
