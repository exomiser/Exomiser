/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.Settings.SettingsBuilder;
import org.apache.commons.cli.Option;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CandidateGeneOptionMarshaller extends AbstractOptionMarshaller {

    public static final String CANDIDATE_GENE_OPTION = "candidate-gene";

    public CandidateGeneOptionMarshaller() {
        option = new Option(null, CANDIDATE_GENE_OPTION, true, "Gene symbol of known or suspected gene association e.g. FGFR2");
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.candidateGene(values[0]);
    }
    
}
