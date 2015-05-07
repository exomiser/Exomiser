/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import java.util.Collections;

/**
 * Given a {@link VariantAnnotationsFactory}, build a {@link Variant} for each
 * alternative allele.
 *
 * Uses the {@link VariantContextAnnotator} class of the Jannovar-HTSJDK bridge.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantAnnotationsFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantAnnotationsFactory.class);

    /**
     * tool for obtaining annotations for the {@link VariantContext} objects
     */
    private final VariantContextAnnotator annotator;

    public VariantAnnotationsFactory(JannovarData jannovarData) {
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
        return buildAlleleAnnotations(variantContext);
    }

    private List<VariantAnnotations> buildAlleleAnnotations(VariantContext variantContext) {
        try {
            //builds one annotation list for each alternative allele
            return annotator.buildAnnotations(variantContext);
        } catch (InvalidCoordinatesException ex) {
            //not all genes can be assigned to a chromosome, so these will fail here. 
            logger.warn("Cannot build annotations for VariantContext {} - coordinates are invalid: {}", variantContext, ex);
            return Collections.emptyList();
        }
    }

}
