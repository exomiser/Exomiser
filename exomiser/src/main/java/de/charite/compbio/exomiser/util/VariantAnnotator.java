/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.util;

import de.charite.compbio.exomiser.exome.VariantEvaluation;
import jannovar.annotation.AnnotationList;
import jannovar.exception.AnnotationException;
import jannovar.exome.Variant;
import jannovar.reference.Chromosome;
import java.util.List;
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

    /**
     * Iterates over all the variants parsed from the VCF file and provides each
     * one with an annovar-style annotation.
     */
    public List<VariantEvaluation> annotateVariants(List<VariantEvaluation> variantList) {
        logger.info("Annotating {} variants with known gene data", variantList.size());
        for (VariantEvaluation ve : variantList) {
            Variant v = ve.getVariant();
            // System.out.println(v);
            byte chr = v.getChromosomeAsByte();
            int pos = v.get_position();
            String ref = v.get_ref();
            String alt = v.get_alt();
            Chromosome c = chromosomeMap.get(chr);
            if (c == null) {
                logger.error("Could not identify chromosome {}", chr);
            } else {
                try {
                    AnnotationList anno = c.getAnnotationList(pos, ref, alt);
                    if (anno == null) {
                        logger.info("No annotations found for variant {}", v);
                        continue;
                    }
                    v.setAnnotation(anno);
                } catch (AnnotationException ae) {
                    logger.error("Unable to annotate variant {}", v.getChromosomalVariant(), ae);
                }
            }

        }
        logger.info("Done");
        return variantList;
    }
}
