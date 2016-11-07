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

package org.monarchinitiative.exomiser.core.filters;

import com.fasterxml.jackson.annotation.JsonRootName;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonRootName("regulatoryFeatureFilter")
public class RegulatoryFeatureFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureFilter.class);

    private static final FilterType filterType = FilterType.REGULATORY_FEATURE_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passedFilterResult = new PassFilterResult(filterType);
    private final FilterResult failedFilterResult = new FailFilterResult(filterType);

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        VariantEffect effect = variantEvaluation.getVariantEffect();
        // Note the INTERGENIC/UPSTREAM variants have already been assessed by the RegFeatureDAO and VariantEffect set to REGULATORY_REGION_VARIANT if in a known region
        // TODO make below nicer using a Jannovar method hopefully 
        if (effect.equals(VariantEffect.INTERGENIC_VARIANT) || effect.equals(VariantEffect.UPSTREAM_GENE_VARIANT)){
            // GeneReassigner can assign a new empty list
            if (variantEvaluation.getAnnotations().isEmpty()){
                return failedFilterResult;
            }
            int dist = getDistFromNearestGene(variantEvaluation);
            if (dist >= 0 && dist < 20000){
                return passedFilterResult;
            }
            return failedFilterResult;
        }
        return passedFilterResult;
    }

    private int getDistFromNearestGene(VariantEvaluation variantEvaluation) {
        Annotation annotation = variantEvaluation.getAnnotations().get(0);//.getHighestImpactAnnotation();
        String intergenicAnnotation = annotation.toVCFAnnoString(variantEvaluation.getAlt());
        return Math.abs(Integer.parseInt(intergenicAnnotation.split("\\|")[14]));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(RegulatoryFeatureFilter.filterType);
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
        final RegulatoryFeatureFilter other = (RegulatoryFeatureFilter) obj;
        return Objects.equals(this.getFilterType(), other.getFilterType());
    }

    @Override
    public String toString() {
        return "RegulatoryFeatureFilter{" + '}';
    }
    
}
