/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import java.util.List;

import static de.charite.compbio.exomiser.core.prioritisers.PrioritiserSettingsImpl.PrioritiserSettingsBuilder;

/**
 * Settings parameters required by the prioritisers.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PrioritiserSettings {

    static PrioritiserSettingsBuilder builder() {
        return PrioritiserSettingsImpl.builder();
    }

    PriorityType getPrioritiserType();
    
    String getDiseaseId();

    String getCandidateGene();

    List<String> getHpoIds();

    List<Integer> getSeedGeneList();

    String getExomiser2Params();
}
