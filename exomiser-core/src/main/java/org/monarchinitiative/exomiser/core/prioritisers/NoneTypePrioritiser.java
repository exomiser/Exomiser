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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A non-functional prioritiser to be used as a default stand-in for a real one.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePrioritiser implements Prioritiser<PriorityResult> {

    @Override
    public void prioritizeGenes(List<String> hpoIds, List<Gene> geneList) {
        //Deliberately empty - this prioritiser does nothing.
    }

    @Override
    public Stream<PriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        return Stream.empty();
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.NONE;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(NoneTypePrioritiser.class.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "NoneTypePrioritiser{}";
    }

}
