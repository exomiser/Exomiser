/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

import de.charite.compbio.exomiser.core.analysis.util.ChromosomalRegionIndex;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RegulatoryFeatureDataProvider extends AbstractFilterDataProvider {

    private final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureDataProvider.class);

    private final ChromosomalRegionIndex<RegulatoryFeature> regulatoryFeatureIndex;

    public RegulatoryFeatureDataProvider(VariantDataService variantDataService, VariantFilter variantFilter) {
        super(variantDataService, variantFilter);
        //make an regulatoryFeature interval tree
        List<RegulatoryFeature> regulatoryFeatures = variantDataService.getRegulatoryFeatures();
        regulatoryFeatureIndex = new ChromosomalRegionIndex(regulatoryFeatures);
    }

    @Override
    public void provideVariantData(VariantEvaluation variantEvaluation) {
        //get the variant effect form the interval tree.
        List<RegulatoryFeature> overlappingFeatures = regulatoryFeatureIndex.getRegionsContainingVariant(variantEvaluation);
        if (overlappingFeatures.isEmpty()) {
            return;
        }
        logger.debug("chr {} {} found in regions {}", variantEvaluation.getChromosome(), variantEvaluation.getPosition(), overlappingFeatures);
        VariantEffect variantEffect = overlappingFeatures.get(0).getVariantEffect();
        variantEvaluation.setVariantEffect(variantEffect);
    }

}
