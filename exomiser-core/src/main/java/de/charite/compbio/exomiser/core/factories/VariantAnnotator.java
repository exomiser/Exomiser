/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.reference.GenomeChange;

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
     * @param vc {@link VariantContext} to get {@link Variant} objects for
     * @return one {@link Variant} object for each alternative allele in vc.
     */
    public List<Variant> annotateVariantContext(VariantContext vc) {
        //builds one annotation list for each alternative allele
        ImmutableList<AnnotationList> lst = annotator.buildAnnotationList(vc);
        List<Variant> result = new ArrayList<>();
        //an Exomiser Variant is a single-allele variant the VariantContext can have multiple alleles
        for (int i = 0; i < vc.getAlternateAlleles().size(); ++i) {
            AnnotationList annotationList = lst.get(i);
            if (!annotationList.isEmpty()) {
                GenomeChange change = annotationList.get(0).change;
            //Currently a change can be null for variants with unknown reference. 
                //This will cause BAD things like NPEs to happen, so filter these things out here before they get into the system.
                //this should be fixed with issue #55
                if (change != null) {
                    result.add(new Variant(vc, i, annotationList));
                }
            }
        }
        return result;
    }

}
