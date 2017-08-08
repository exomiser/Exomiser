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
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataProvider extends AbstractFilterDataProvider {

    private final Set<PathogenicitySource> pathogenicitySources;

    public PathogenicityDataProvider(VariantDataService variantDataService, Set<PathogenicitySource> pathogenicitySources, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);

        if (pathogenicitySources.isEmpty()) {
            this.pathogenicitySources = EnumSet.noneOf(PathogenicitySource.class);
        } else {
            this.pathogenicitySources = EnumSet.copyOf(pathogenicitySources);
        }
    }

    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getPathogenicityData().isEmpty()) {
            PathogenicityData pathData = variantDataService.getVariantPathogenicityData(variantEvaluation, pathogenicitySources);
            variantEvaluation.setPathogenicityData(pathData);
        }
    }

}
