/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PriorityFactory {

    /**
     * Returns a Prioritiser of the given type, ready to run according to the
     * settings provided. Will return a non-functional prioritiser in cases
     * where the type is not recognised.
     *
     * @param settings
     * @return
     */
    Prioritiser makePrioritiser(PrioritiserSettings settings);

    OMIMPriority makeOmimPrioritiser();

    PhenixPriority makePhenixPrioritiser();

    PhivePriority makePhivePrioritiser();

    ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes);

    HiPhivePriority makeHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions);

    List<String> getHpoIdsForDiseaseId(String diseaseId);
}
