/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    public boolean keepNonPathogenicVariants();

    public ModeOfInheritance getModeOfInheritance();

    public Set<Integer> getGenesToKeep();
}
