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

    public void annotateVariant(Variant variant) {
        byte chr = variant.getChromosomeAsByte();
        int pos = variant.get_position();
        String ref = variant.get_ref();
        String alt = variant.get_alt();
        Chromosome c = chromosomeMap.get(chr);
        if (c == null) {
            logger.error("Could not identify chromosome {}", chr);
            return;
        }
        try {
            AnnotationList anno = c.getAnnotationList(pos, ref, alt);
            if (anno == null) {
                logger.info("No annotations found for variant {}", variant);
                return;
            }
            variant.setAnnotation(anno);
        } catch (AnnotationException ae) {
            logger.error("Unable to annotate variant {}", variant.getChromosomalVariant(), ae);
        }
    }
}
