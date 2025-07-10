/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

/**
 * A Gene runFilter for filtering against a particular inheritance mode.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record InheritanceFilter(Set<ModeOfInheritance> compatibleModes) implements GeneFilter {

    private static final InheritanceFilter EMPTY = new InheritanceFilter(EnumSet.noneOf(ModeOfInheritance.class));

    public static final Set<ModeOfInheritance> JUST_ANY = Collections.unmodifiableSet(EnumSet.of(ModeOfInheritance.ANY));

    private static final FilterType filterType = FilterType.INHERITANCE_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);
    private static final FilterResult NOT_RUN = FilterResult.notRun(filterType);


    public InheritanceFilter {
        Objects.requireNonNull(compatibleModes);
        compatibleModes = Collections.unmodifiableSet(EnumSet.copyOf(compatibleModes));
    }

    public static InheritanceFilter of(ModeOfInheritance compatibleMode) {
        Objects.requireNonNull(compatibleMode);
        return new InheritanceFilter(Collections.unmodifiableSet(EnumSet.of(compatibleMode)));
    }

    public static InheritanceFilter of(ModeOfInheritance... compatibleModes) {
        return compatibleModes.length == 0 ? EMPTY : new InheritanceFilter(EnumSet.copyOf(List.of(compatibleModes)));
    }

    public static InheritanceFilter of(Set<ModeOfInheritance> compatibleModes) {
        Objects.requireNonNull(compatibleModes);
        return new InheritanceFilter(compatibleModes);
    }

    @Override
    public FilterResult runFilter(Gene gene) {
        if (compatibleModes.isEmpty() || compatibleModes.equals(JUST_ANY)) {
            //if ModeOfInheritance.ANY pass the runFilter - ideally it shouldn't be applied in the first place.
            return NOT_RUN;
        }

        addFilterResultToVariants(gene.variantEvaluations());

        //If we're going to score against multiple inheritance models we're going to want to keep them and filter when scoring.
        //On the other hand - if there is only one or two modes it makes sense to filter them out if incompatible.
        for (ModeOfInheritance modeOfInheritance : compatibleModes) {
            if (gene.isCompatibleWith(modeOfInheritance)) {
                return PASS;
            }
        }
        return FAIL;
    }

    private void addFilterResultToVariants(List<VariantEvaluation> variantEvaluations) {
        for (VariantEvaluation variant : variantEvaluations) {
            if (compatibleWithAtLeastOneModeOfInheritance(variant)) {
                variant.addFilterResult(PASS);
            } else {
                variant.addFilterResult(FAIL);
            }
        }
    }

    private boolean compatibleWithAtLeastOneModeOfInheritance(VariantEvaluation variant) {
        for (ModeOfInheritance modeOfInheritance : compatibleModes) {
            if (variant.isCompatibleWith(modeOfInheritance)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FilterType filterType() {
        return filterType;
    }

    @Override
    public String toString() {
        return "InheritanceFilter{" +
               "compatibleModes=" + compatibleModes +
               '}';
    }
}
