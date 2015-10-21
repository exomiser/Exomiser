/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import java.util.List;

/**
 * Settings parameters required by the prioritisers.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PrioritiserSettings {

    public PriorityType getPrioritiserType();
    
    public String getDiseaseId();

    public String getCandidateGene();

    public List<String> getHpoIds();

    public List<Integer> getSeedGeneList();

    public String getExomiser2Params();
}
