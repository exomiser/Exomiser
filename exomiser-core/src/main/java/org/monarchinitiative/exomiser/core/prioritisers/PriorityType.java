/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PriorityType {

    //Prioritises against PPI-RandomWalk-proximity and dynamic human, mouse and fish phenotypes
    HIPHIVE_PRIORITY,
    //Prioritises against PPI-RandomWalk-proximity A.K.A "GeneWanderer"
    EXOMEWALKER_PRIORITY,
    //Prioritises against human phenotypes A.K.A. "Legacy HPO Phenomizer prioritizer"
    LEGACY_PHENIX_PRIORITY,
    //Prioritises against human-mouse phenotype similarities
    PHIVE_PRIORITY,
    //Prioritises against OMIM data
    OMIM_PRIORITY,
    //None - for when you don't want to run any prioritisation
    NONE

}
