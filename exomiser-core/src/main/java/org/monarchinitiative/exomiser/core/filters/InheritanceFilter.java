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

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A Gene runFilter for filtering against a particular inheritance mode.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceFilter implements GeneFilter {

    public static final Set<ModeOfInheritance> JUST_ANY = Sets.immutableEnumSet(ModeOfInheritance.ANY);

    private static final FilterType filterType = FilterType.INHERITANCE_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);
    private static final FilterResult NOT_RUN = FilterResult.notRun(filterType);


    private final Set<ModeOfInheritance> compatibleModes;

    public InheritanceFilter(ModeOfInheritance compatibleMode) {
        this.compatibleModes = Sets.immutableEnumSet(compatibleMode);
    }

    public InheritanceFilter(ModeOfInheritance... compatibleModes) {
        this.compatibleModes = Sets.immutableEnumSet(Arrays.asList(compatibleModes));
    }

    public InheritanceFilter(Set<ModeOfInheritance> compatibleModes) {
        this.compatibleModes = Sets.immutableEnumSet(compatibleModes);
    }

    public Set<ModeOfInheritance> getCompatibleModes() {
        return compatibleModes;
    }
    
    @Override
    public FilterResult runFilter(Gene gene) {
        if (compatibleModes.isEmpty() || compatibleModes.equals(JUST_ANY)) {
            //if ModeOfInheritance.ANY pass the runFilter - ideally it shouldn't be applied in the first place.
            return NOT_RUN;
        }

        addFilterResultToVariants(gene.getVariantEvaluations());

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
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InheritanceFilter that = (InheritanceFilter) o;
        return Objects.equals(compatibleModes, that.compatibleModes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatibleModes);
    }

    @Override
    public String toString() {
        return "InheritanceFilter{" +
                "compatibleModes=" + compatibleModes +
                '}';
    }
}
