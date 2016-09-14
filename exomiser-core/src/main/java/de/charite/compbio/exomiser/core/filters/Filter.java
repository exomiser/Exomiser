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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.analysis.AnalysisStep;
import de.charite.compbio.exomiser.core.model.Filterable;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @param <T>
 */
public interface Filter<T extends Filterable> extends AnalysisStep {

    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    @JsonIgnore
    FilterType getFilterType();

    /**
     * True or false depending on whether the {@code VariantEvaluation} passes the runFilter or not.
     *
     * @param filterable
     * @return true if the {@code VariantEvaluation} passes the runFilter.
     */
    FilterResult runFilter(T filterable);
}
