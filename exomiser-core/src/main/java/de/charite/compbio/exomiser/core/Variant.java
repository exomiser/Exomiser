package de.charite.compbio.exomiser.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
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

    /**
     * @return forward strand {@link GenomePosition}
     */
    public GenomePosition getGenomePosition() {
        return change.pos.withStrand('+');
    }

    /**
     * Shortcut to <code>change.pos.chr</code>
     * 
     * @return <code>int</code> representation of chromosome
     */
    public int getChromosome() {
        return change.pos.chr;
    }

    /**
     * @return name of chromosome
     */
    public String getChromosomeStr() {
        return change.pos.refDict.contigName.get(change.pos.chr);
    }

    /**
     * Shortcut to <code>change.pos.pos + 1</code>.
     * 
     * Returns a 1-based coordinate (as used in the Exomiser) instead of the 0-based coordinates from Jannovar.
     * 
     * @return one-based position
     */
    public int getPosition() {
        return change.pos.pos + 1;
    }

    /**
     * Shortcut to {@link #change.ref}, returning "-" in case of insertions.
     */
    public String getRef() {
        if (change.ref.equals(""))
            return "-";
        else
            return change.ref;
    }

    /**
     * Shortcut to {@link #change.alt}, returning "-" in case of deletions.
     */
    public String getAlt() {
        if (change.ref.equals(""))
            return "-";
        else
            return change.alt;
    }

    /**
     * @return Highest-impact {@link VariantEffect} or <code>null</code> if there is none.
     */
    public VariantEffect getHighestImpactEffect() {
        return annotations.getHighestImpactEffect();
    }

    /**
     * @return Highest-impact {@link Annotation} or <code>null</code> if there is none.
     */
    public Annotation getHighestImpactAnnotation() {
        return annotations.getHighestImpactAnnotation();
    }

    /**
     * @return <code>true</code> if the variant is neither exonic nor splicing
     */
    public boolean isOffExomeTarget() {
        Annotation anno = annotations.getHighestImpactAnnotation();
        if (anno == null || anno.effects.isEmpty())
            return true;
        for (VariantEffect eff : anno.effects) {
            if (eff.isSplicing())
                return false;
            else if (eff.isIntronic())
                return true;
        }
        return false;
    }

    public int getVariantReadDepth() {
        // FIXME: alleleID != sample ID!
        return vc.getGenotype(altAlleleID).getDP();
    }

    /**
     * @return annotation of the most pathogenic annotation
     */
    public String getRepresentativeAnnotation() {
        Annotation anno = annotations.getHighestImpactAnnotation();
        if (anno == null)
            return "?";
        else
            return anno.getSymbolAndAnnotation();
    }
    
    /**
     * @return list of all annotation strings
     */
    public List<String> getAnnotationList() {
        ArrayList<String> result = new ArrayList<String>();
        for (Annotation anno : annotations.entries)
            result.add(anno.getSymbolAndAnnotation());
        return result;
    }

    /**
     * @return list of all annotation strings with type prepended
     */
    public List<String> getAnnotationListWithAnnotationClass() {
        ArrayList<String> result = new ArrayList<String>();
        for (Annotation anno : annotations.entries)
            result.add(anno.getMostPathogenicVarType() + "|" + anno.getSymbolAndAnnotation());
        return result;
    }

    public boolean isXChromosomal() {
        return getChromosome() == change.pos.refDict.contigID.get("X").intValue();
    }

    public boolean isYChromosomal() {
        return getChromosome() == change.pos.refDict.contigID.get("Y").intValue();
    }

    /**
     * @return most pathogenic {@link VariantEffect}
     */
    public VariantEffect getVariantEffect() {
        final Annotation anno = annotations.getHighestImpactAnnotation();
        if (anno == null)
            return null;
        return anno.getMostPathogenicVarType();
    }

    public double getVariantPhredScore() {
        return vc.getPhredScaledQual();
    }

    public String getGeneSymbol() {
        final Annotation anno = annotations.getHighestImpactAnnotation();
        if (anno == null)
            return ".";
        else
            return anno.getGeneSymbol();
    }

    public String getGenotypeAsString() {
        return vc.getGenotype(0).toBriefString();
    }

    public int getEntrezGeneID() {
        final Annotation anno = getHighestImpactAnnotation();
        if (anno == null || anno.transcript == null || anno.transcript.geneID == null)
            return -1;
        // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
        // for UCSC. At this point, there is a hard dependency on using the UCSC database.
        return Integer.parseInt(anno.transcript.geneID.substring("ENTREZ".length()));
    }

}
