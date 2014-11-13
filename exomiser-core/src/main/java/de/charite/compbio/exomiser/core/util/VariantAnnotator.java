/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import jannovar.annotation.AnnotationList;
import jannovar.exception.AnnotationException;
import jannovar.exome.Variant;
import jannovar.reference.Chromosome;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(VariantAnnotator.class);

    private final Map<Byte, Chromosome> chromosomeMap;

    public VariantAnnotator(Map<Byte, Chromosome> chromosomeMap) {
        this.chromosomeMap = chromosomeMap;
    }

    public Variant annotateVariant(Variant variant) {
        AnnotationList annotations = getVariantAnnotations(variant);
        if (annotations != null) {
            variant.setAnnotation(annotations);
        }
        return variant;
    }

    private AnnotationList getVariantAnnotations(Variant variant) {
        Chromosome chromosome = getChromosomeForVariant(variant);
        if (chromosome == null) {
            logger.error("Could not identify chromosome {}", variant.getChromosomeAsByte());
            return null;
        }
    
        return getVariantAnnotationsFromChromosome(variant, chromosome);
    }

    private Chromosome getChromosomeForVariant(Variant variant) {
        return chromosomeMap.get(variant.getChromosomeAsByte());
    }
    
    private AnnotationList getVariantAnnotationsFromChromosome(Variant variant, Chromosome chromosome) {
        try {
            int pos = variant.get_position();
            String ref = variant.get_ref();
            String alt = variant.get_alt();
            
            return chromosome.getAnnotationList(pos, ref, alt);           
        } catch (AnnotationException ae) {
            logger.error("Unable to annotate variant {}", variant.getChromosomalVariant(), ae);
        }
        return null;
    }  
}
