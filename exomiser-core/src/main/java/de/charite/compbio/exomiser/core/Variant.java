package de.charite.compbio.exomiser.core;

import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;

import com.google.common.base.Joiner;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.AnnotationLocation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.VariantDescription;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Collects information about one allele in a VCF variant, together with its
 * Jannovar Annotation.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class Variant {

    // HTSJDK {@link VariantContext} instance of this allele
    private final VariantContext variantContext;

    // numeric index of the alternative allele in {@link #vc}.
    private final int altAlleleID;

    /**
     * list of {@link Annotation}s for this variant context, one for each
     * affected transcript, and sorted by predicted impact, highest first.
     */
    private final AnnotationList annotationList;

    /**
     * shortcut to the {@link GenomeChange} in the first element of
     * {@link #annotationList}, or null.
     */
    private final GenomeChange genomeChange;

    public Variant(VariantContext variantContext, int altAlleleID, GenomeChange genomeChange, AnnotationList annotationList) {
        this.variantContext = variantContext;
        this.altAlleleID = altAlleleID;
        this.annotationList = annotationList;
        this.genomeChange = genomeChange;
    }

    public VariantContext getVariantContext() {
        return variantContext;
    }

    public int getAltAlleleID() {
        return altAlleleID;
    }

    public GenomeChange getGenomeChange() {
        return genomeChange;
    }
    
    public AnnotationList getAnnotationList() {
        return annotationList;
    }

    /**
     * @return forward strand {@link GenomePosition}
     */
    public GenomePosition getGenomePosition() {
        return genomeChange.pos.withStrand(Strand.FWD);
    }

    /**
     * Shortcut to <code>change.pos.chr</code>
     *
     * @return <code>int</code> representation of chromosome
     */
    public int getChromosome() {
        return genomeChange.getChr();
    }

    /**
     * @return String representation of {@link #genomeChange}/
     */
    public String getChromosomalVariant() {
        // Change can be null for unknown references. In this case, we hack together something from the Variant Context.
        //TODO: change should never be null - it should be constructed from the VariantContext 
        if (genomeChange != null) {
            return genomeChange.toString();
        } else {
            return StringUtils.concat(variantContext.getChr(), ":g.", variantContext.getStart(), variantContext.getReference(), ">",
                    variantContext.getAlternateAllele(altAlleleID));
        }
    }

    /**
     * Shortcut to <code>change.pos.pos + 1</code>.
     *
     * Returns a 1-based coordinate (as used in the Exomiser) instead of the
     * 0-based coordinates from Jannovar.
     *
     * @return one-based position
     */
    public int getPosition() {
        if (genomeChange.getRef().equals("")) {
//            return change.pos.withStrand(Strand.FWD).pos;
            return genomeChange.getPos();
        } else {
            return genomeChange.getPos() + 1;
        }
    }

    /**
     * Shortcut to {@link #change.ref}, returning "-" in case of insertions.
     */
    public String getRef() {
//        if (change.ref.equals("")) {
        if (genomeChange.getRef().equals("")) {
            return "-";
        } else {
//            return change.withStrand('+').ref;
            return genomeChange.getRef();
        }
    }

    /**
     * Shortcut to {@link #change.alt}, returning "-" in case of deletions.
     */
    public String getAlt() {
        if (genomeChange.getAlt().equals("")) {
            return "-";
        } else {
            return genomeChange.getAlt();
        }
    }

    /**
     * @return most pathogenic {@link VariantEffect}
     */
    public VariantEffect getVariantEffect() {
        return annotationList.getHighestImpactEffect();

    }

    /**
     * @return <code>true</code> if the variant is neither exonic nor splicing
     */
    public boolean isOffExome() {
        return annotationList.getHighestImpactEffect().isOffExome();
    }

    public int getReadDepth() {
        // FIXME: alleleID != sample ID!
        return variantContext.getGenotype(altAlleleID).getDP();
    }

    /**
     * @return annotation of the most pathogenic annotation
     */
    public String getRepresentativeAnnotation() {
        Annotation anno = annotationList.getHighestImpactAnnotation();
        if (anno == null) {
            return "?";
        }

        String exonIntron = null;
        if (anno.annoLoc != null && anno.annoLoc.rankType == AnnotationLocation.RankType.EXON) {
            exonIntron = StringUtils.concat("exon", anno.annoLoc.rank + 1);
        } else if (anno.annoLoc != null && anno.annoLoc.rankType == AnnotationLocation.RankType.INTRON) {
            exonIntron = StringUtils.concat("intron", anno.annoLoc.rank + 1);
        }

        final Joiner joiner = Joiner.on(":").skipNulls();
        return joiner.join(anno.getGeneSymbol(), anno.transcript.accession, exonIntron, anno.ntHGVSDescription,
                anno.aaHGVSDescription);
    }

    /**
     * @return list of all annotation strings
     */
    public List<String> getAnnotations() {
        ArrayList<String> result = new ArrayList<>();
        for (Annotation anno : annotationList) {
            String annoS = anno.getSymbolAndAnnotation();
            if (annoS != null) {
                result.add(annoS);
            }
        }
        return result;
    }

    /**
     * @return list of all annotation strings with type prepended
     */
    public List<String> getAnnotationsWithAnnotationClass() {
        List<String> result = new ArrayList<>();
        for (Annotation anno : annotationList) {
            result.add(anno.getMostPathogenicVarType() + "|" + anno.getSymbolAndAnnotation());
        }
        return result;
    }

    public boolean isXChromosomal() {
        return getChromosome() == genomeChange.pos.refDict.contigID.get("X").intValue();
    }

    public boolean isYChromosomal() {
        return getChromosome() == genomeChange.pos.refDict.contigID.get("Y").intValue();
    }

    public double getPhredScore() {
        return variantContext.getPhredScaledQual();
    }

    public String getGeneSymbol() {
        final Annotation anno = annotationList.getHighestImpactAnnotation();
        return anno.getGeneSymbol();
    }

    public String getGenotypeAsString() {
        // collect genotype string list
        ArrayList<String> gtStrings = new ArrayList<String>();
        for (Genotype gt : variantContext.getGenotypes()) {
            boolean firstAllele = true;
            StringBuilder builder = new StringBuilder();
            for (Allele allele : gt.getAlleles()) {
                if (firstAllele) {
                    firstAllele = false;
                } else {
                    builder.append('/');
                }

                if (allele.isNoCall()) {
                    builder.append('.');
                } else if (allele.equals(variantContext.getAlternateAllele(altAlleleID))) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
            gtStrings.add(builder.toString());
        }

        // normalize 1/0 to 0/1 and join genotype strings with colon
        for (int i = 0; i < gtStrings.size(); ++i) {
            if (gtStrings.get(i).equals("1/0")) {
                gtStrings.set(i, "0/1");
            }
        }
        return Joiner.on(":").join(gtStrings);
    }

    public int getEntrezGeneID() {
        final Annotation anno = annotationList.getHighestImpactAnnotation();
        if (anno == null || anno.transcript == null || anno.transcript.geneID == null) {
            return -1;
        }
        // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
        // for UCSC. At this point, there is a hard dependency on using the UCSC database.
        return Integer.parseInt(anno.transcript.geneID.substring("ENTREZ".length()));
    }

    public Genotype getGenotype() {
        return variantContext.getGenotype(0);
    }

    @Override
    public String toString() {
        return "Variant [vc=" + variantContext + ", altAlleleID=" + altAlleleID + ", annotations=" + annotationList + ", change="
                + genomeChange + "]";
    }
}
