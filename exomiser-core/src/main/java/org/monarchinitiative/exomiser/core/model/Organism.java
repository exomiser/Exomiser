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
package org.monarchinitiative.exomiser.core.model;

/**
 * Enum representing the model organism present in the Exomiser database for which
 * there are phenotype mappings. These are few and should not be added to
 * frequently.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum Organism {

    //TODO: Also consider having a DISEASE and PATIENT type. Doesn't quite make sense, but HUMAN is not really being consistently used here either as it is used to refer to DISEASE models or HUMAN phenotypes.
    HUMAN,
    MOUSE,
    FISH

}
