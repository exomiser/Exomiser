/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.List;
import java.util.Set;

/**
 * Settings parameters required by the filters.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface FilterSettings {

    public List<FilterType> getFilterTypesToRun();

    public Set<Integer> getGenesToKeep();

    public float getMaximumFrequency();

    public boolean removeKnownVariants();

    public float getMinimumQuality();

    public boolean removePathFilterCutOff();

    public GeneticInterval getGeneticInterval();

    public ModeOfInheritance getModeOfInheritance();
}
