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

import com.google.common.collect.ImmutableList;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.reference.GenomeChange;
import java.util.Collections;

/**
 * Given a {@link VariantAnnotator}, build a {@link Variant} for each
 * alternative allele.
 *
 * Uses the {@link VariantContextAnnotator} class of the Jannovar-HTSJDK bridge.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(VariantAnnotator.class);

    /**
     * tool for obtaining annotations for the {@link VariantContext} objects
     */
    private final VariantContextAnnotator annotator;

    public VariantAnnotator(JannovarData jannovarData) {
        this.annotator = new VariantContextAnnotator(jannovarData.refDict, jannovarData.chromosomes);
    }

    /**
     * Returns a list of variants of known reference. If a VariantContext has no
     * know reference on the genome an empty list will be returned.
     *
     * @param variantContext {@link VariantContext} to get {@link Variant} objects for
     * @return one {@link Variant} object for each alternative allele in vc.
     */
    public List<Variant> annotateVariantContext(VariantContext variantContext) {
        ImmutableList<AnnotationList> alleleAnnotationLists = buildAlleleAnnotations(variantContext);
        if (alleleAnnotationLists.isEmpty()) {
            return Collections.emptyList();
        }
        List<Variant> variants = new ArrayList<>();
        //an Exomiser Variant is a single-allele variant the VariantContext can have multiple alleles
        for (int alleleID = 0; alleleID < variantContext.getAlternateAlleles().size(); ++alleleID) {
            AnnotationList annotationList = alleleAnnotationLists.get(alleleID);
            if (!annotationList.isEmpty()) {
                GenomeChange change = annotationList.getChange();
                //this shouldn't happen, as it should have been dealt with by buildAlleleAnnotations(), but just in case...
                if (change != null) {
                    variants.add(new Variant(variantContext, alleleID, change, annotationList));
                }
            }
        }
        return variants;
    }

    private ImmutableList<AnnotationList> buildAlleleAnnotations(VariantContext variantContext) {
        try {
            //builds one annotation list for each alternative allele
            return annotator.buildAnnotationList(variantContext);
        } catch (InvalidCoordinatesException ex) {
            //not all genes can be assigned to a chromosome, so these will fail here. 
            logger.debug("Cannot build annotations for VariantContext {} - coordinates are invalid: {}", variantContext, ex);
            return new ImmutableList.Builder<AnnotationList>().build();
        }
    }

}
