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
package de.charite.compbio.exomiser.core.factories;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.PositionType;
import java.util.Collections;

/**
 * Given a {@link VariantAnnotator}, build a {@link Variant} for each
 * alternative allele.
 *
 * Uses the {@link VariantContextAnnotator} class of the Jannovar-HTSJDK bridge.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Deprecated
public class VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(VariantAnnotator.class);

    /**
     * tool for obtaining annotations for the {@link VariantContext} objects
     */
    private final VariantContextAnnotator annotator;

    public VariantAnnotator(VariantContextAnnotator variantContextAnnotator) {
        this.annotator = variantContextAnnotator;
    }

    public VariantAnnotator(JannovarData jannovarData) {
        this.annotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
    }

    /**
     * Returns a list of variants of known reference. If a VariantContext has no
     * know reference on the genome an empty list will be returned.
     *
     * @param variantContext {@link VariantContext} to get {@link Variant} objects for
     * @return one {@link Variant} object for each alternative allele in vc.
     */
    public List<VariantAnnotations> buildVariantAnnotations(VariantContext variantContext) {
        try {
            //builds one annotation list for each alternative allele
            return annotator.buildAnnotations(variantContext);
        } catch (InvalidCoordinatesException ex) {
            //Not all genes can be assigned to a chromosome, so these will fail here. 
            //Should we report these? They will not be used in the analysis or appear in the output anywhere.
            logger.trace("Cannot build annotations for VariantContext {} {} {}: {}", variantContext.getContig(), variantContext.getStart(), variantContext.getAlleles(), ex);
        }
        return Collections.emptyList();
    }

}
