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
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Objects;

/**
 * A Gene runFilter for filtering against a particular inheritance mode.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceFilter implements GeneFilter {

    private static final FilterType filterType = FilterType.INHERITANCE_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);
    private static final FilterResult NOT_RUN = FilterResult.notRun(filterType);

    private final ModeOfInheritance modeOfInheritance;
    
    public InheritanceFilter(ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
    }

    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }
    
    @Override
    public FilterResult runFilter(Gene gene) {
        if (modeOfInheritance == ModeOfInheritance.ANY) {
            //if ModeOfInheritance.ANY pass the runFilter - ideally it shouldn't be applied in the first place.
            return NOT_RUN;
        }
        if (gene.isCompatibleWith(modeOfInheritance)) {
            return addFilterResultToVariants(PASS, gene);
        }
        return addFilterResultToVariants(FAIL, gene);
    }

    private FilterResult addFilterResultToVariants(FilterResult filterResult, Gene gene) {
        for (VariantEvaluation variant : gene.getVariantEvaluations()) {
            if (variant.isCompatibleWith(modeOfInheritance)) {
                variant.addFilterResult(PASS);
            } else {
                variant.addFilterResult(FAIL);
            }
        }
        return filterResult;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.modeOfInheritance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InheritanceFilter other = (InheritanceFilter) obj;
        return this.modeOfInheritance == other.modeOfInheritance;
    }

    @Override
    public String toString() {
        return filterType + " filter: ModeOfInheritance=" + modeOfInheritance;
    }
    
}
