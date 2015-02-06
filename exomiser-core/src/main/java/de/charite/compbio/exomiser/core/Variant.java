package de.charite.compbio.exomiser.core;

import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeChange;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Collects information about one allele in a VCF variant, together with its Jannovar Annotation.
 * 
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class Variant {

    /** HTSJDK {@link VariantContext} instance of this allele */
    public final VariantContext vc;

    /** numeric index of the alternative allele in {@link #vc}. */
    public final int altAlleleID;

    /**
     * list of {@link Annotation}s for this variant context, one for each affected transcript, and sorted by predicted
     * impact, highest first.
     */
    public final AnnotationList annotations;

    /** shortcut to the {@link GenomeChange} in the first element of {@link #annotations}, or null. */
    public final GenomeChange change;

    /**
     * Initialize the object with the given values.
     */
    public Variant(VariantContext vc, int altAlleleID, AnnotationList annotations) {
        this.vc = vc;
        this.altAlleleID = altAlleleID;
        this.annotations = annotations;
        if (annotations.entries.isEmpty())
            this.change = null;
        else
            this.change = annotations.entries.get(0).change;
    }
    
    public VariantEffect getVariantTypeConstant() {
        if (this.annotations.entries.isEmpty() || this.annotations.entries.get(0).effects.isEmpty())
            return null;
        else
            return this.annotations.entries.get(0).effects.first();
    }

}
