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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumSet;

/**
 * Mock variant effect filter which will always return a PASS FilterResult with a score of 1.0.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassAllVariantEffectsFilter extends VariantEffectFilter {

    public PassAllVariantEffectsFilter() {
        super(EnumSet.noneOf(VariantEffect.class));
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.VARIANT_EFFECT_FILTER;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER);
    }

}
