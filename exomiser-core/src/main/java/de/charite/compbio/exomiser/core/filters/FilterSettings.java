/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.Set;

/**
 * Settings parameters required by the filters.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface FilterSettings {

    public float getMaximumFrequency();

    public float getMinimumQuality();

    public GeneticInterval getGeneticInterval();

    public boolean keepOffTargetVariants();

    public boolean removeKnownVariants();

    public boolean removePathFilterCutOff();

    public ModeOfInheritance getModeOfInheritance();

    public Set<Integer> getGenesToKeep();
}
