package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class JannovarVariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarVariantAnnotator.class);

    private final ReferenceDictionary referenceDictionary;
    private final VariantAnnotator variantAnnotator;

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private static final int UNKNOWN_CHROMOSOME = 0;

    @Autowired
    public JannovarVariantAnnotator(JannovarData jannovarData) {
        this.referenceDictionary = jannovarData.getRefDict();
        this.variantAnnotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
    }

    public VariantAnnotations getVariantAnnotations(String contig, AllelePosition allelePosition) {
        return getVariantAnnotations(contig, allelePosition.getPos(), allelePosition.getRef(), allelePosition.getAlt());
    }

    public VariantAnnotations getVariantAnnotations(String contig, int pos, String ref, String alt) {
        GenomeVariant genomeVariant = buildOneBasedFwdStrandGenomicVariant(contig, pos, ref, alt);
        if (genomeVariant.getChr() == UNKNOWN_CHROMOSOME) {
            logger.trace("Unknown contig '{}' - mapping to {}", contig, UNKNOWN_CHROMOSOME);
            //Need to check this here and return otherwise the variantAnnotator will throw a NPE.
            return VariantAnnotations.buildEmptyList(genomeVariant);
        }
        return buildAnnotations(genomeVariant);
    }

    private VariantAnnotations buildAnnotations(GenomeVariant genomeVariant) {
        try {
            return variantAnnotator.buildAnnotations(genomeVariant);
        } catch (Exception e) {
            logger.debug("Unable to annotate variant {}-{}-{}-{}", genomeVariant.getChrName(), genomeVariant.getPos(), genomeVariant
                    .getRef(), genomeVariant.getAlt(), e);
        }
        return VariantAnnotations.buildEmptyList(genomeVariant);
    }


    private GenomeVariant buildOneBasedFwdStrandGenomicVariant(String contig, int pos, String ref, String alt) {
        int chr = getIntValueOfChromosomeOrZero(contig);
        GenomePosition genomePosition = new GenomePosition(referenceDictionary, Strand.FWD, chr, pos, PositionType.ONE_BASED);
        return new GenomeVariant(genomePosition, ref, alt);
    }

    private Integer getIntValueOfChromosomeOrZero(String contig) {
        return referenceDictionary.getContigNameToID().getOrDefault(contig, UNKNOWN_CHROMOSOME);
    }

}
