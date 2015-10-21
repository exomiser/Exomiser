/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

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
     * @param priorityType
     * @param settings
     * @return
     */
    public Prioritiser makePrioritiser(PriorityType priorityType, PrioritiserSettings settings);

    public OMIMPriority makeOmimPrioritiser();

    public PhenixPriority makePhenixPrioritiser(List<String> hpoIds);

    public PhivePriority makePhivePrioritiser(List<String> hpoIds);

    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes);

    public HiPhivePriority makeHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions);
}
