package de.charite.compbio.exomiser.core.model;

import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;

import com.google.common.base.Joiner;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationLocation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Collects information about one allele in a VCF variant, together with its
 * Jannovar Annotation.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@Deprecated
public class JannovarVariant  {

    // HTSJDK {@link VariantContext} instance of this allele
    private final VariantContext variantContext;

    // numeric index of the alternative allele in {@link #vc}.
    private final int altAlleleID;

    /**
     * list of {@link Annotation}s for this variant context, one for each
     * affected transcript, and sorted by predicted impact, highest first.
     */
    private final VariantAnnotations annotationList;

    /**
     * shortcut to the {@link GenomeChange} in the first element of
     * {@link #annotationList}, or null.
     */
    private final GenomeVariant genomeVariant;

    public JannovarVariant(VariantContext variantContext, int altAlleleID, GenomeVariant genomeVariant, VariantAnnotations variantAnnotations) {
        this.variantContext = variantContext;
        this.altAlleleID = altAlleleID;
        this.annotationList = variantAnnotations;
        this.genomeVariant = genomeVariant;
    }

//    public VariantContext getVariantContext() {
//        return variantContext;
//    }

//    public int getAltAlleleID() {
//        return altAlleleID;
//    }

    //only used in VariantEvaluation
//    public GenomeVariant getGenomeVariant() {
//        return genomeVariant;
//    }
 
    //unused anywhere
//    public VariantAnnotations getVariantAnnotations() {
//        return annotationList;
//    }

    /**
     * Shortcut to <code>change.pos.chr</code>
     *
     * @return <code>int</code> representation of chromosome
     */
//    @Override
//    public int getChromosome() {
//        return genomeVariant.getChr();
//    }

    
//    @Override
//    public String getChromosomeName() {
//        return genomeVariant.getChrName();
//    }

    /**
     * @return String representation of {@link #genomeVariant}/
     */
//    public String getChromosomalVariant() {
//        // Change can be null for unknown references. In this case, we hack together something from the Variant Context.
//        //TODO: genomeVariant should never be null - it should be constructed from the VariantContext 
//        if (genomeVariant != null) {
//            return genomeVariant.toString();
//        } else {
//            return StringUtils.concat(variantContext.getChr(), ":g.", variantContext.getStart(), variantContext.getReference(), ">",
//                    variantContext.getAlternateAllele(altAlleleID));
//        }
//    }

    /**
     * Shortcut to <code>change.pos.pos + 1</code>.
     *
     * Returns a 1-based coordinate (as used in the Exomiser) instead of the
     * 0-based coordinates from Jannovar.
     *
     * @return one-based position
     */
//    @Override
//    public int getPosition() {
//        if (genomeVariant.getRef().equals("")) {
//            return genomeVariant.getPos();
//        } else {
//            return genomeVariant.getPos() + 1;
//        }
//    }

    /**
     * Shortcut to {@link #change.ref}, returning "-" in case of insertions.
     */
//    @Override
//    public String getRef() {
//        if (genomeVariant.getRef().equals("")) {
//            return "-";
//        } else {
//            return genomeVariant.getRef();
//        }
//    }

    /**
     * Shortcut to {@link #change.alt}, returning "-" in case of deletions.
     */
//    @Override
//    public String getAlt() {
//        if (genomeVariant.getAlt().equals("")) {
//            return "-";
//        } else {
//            return genomeVariant.getAlt();
//        }
//    }

    /**
     * @return most pathogenic {@link VariantEffect}
     */
//    public VariantEffect getVariantEffect() {
//        return annotationList.getHighestImpactEffect();
//
//    }

    /**
     * @return <code>true</code> if the variant is neither exonic nor splicing
     */
//    public boolean isOffExome() {
//        return annotationList.getHighestImpactEffect().isOffExome();
//    }

//    @Override
//    public int getReadDepth() {
//        // FIXME: alleleID != sample ID!
//        return variantContext.getGenotype(altAlleleID).getDP();
//    }

    /**
     * @return annotation of the most pathogenic annotation
     */
//    public String getRepresentativeAnnotation() {
//        Annotation anno = annotationList.getHighestImpactAnnotation();
//        if (anno == null) {
//            return "?";
//        }
//
//        String exonIntron = null;
//        AnnotationLocation annotationLocation = anno.getAnnoLoc();
//        AnnotationLocation.RankType rankType = annotationLocation.getRankType();
//        if (rankType == AnnotationLocation.RankType.EXON) {
//            exonIntron = StringUtils.concat("exon", annotationLocation.getRank() + 1);
//        } else if (rankType == AnnotationLocation.RankType.INTRON) {
//            exonIntron = StringUtils.concat("intron", annotationLocation.getRank() + 1);
//        }
//
//        final Joiner joiner = Joiner.on(":").skipNulls();
//        return joiner.join(anno.getGeneSymbol(), anno.getTranscript().getAccession(), exonIntron, anno.getNucleotideHGVSDescription(),
//                anno.getAminoAcidHGVSDescription());
//    }

    /**
     * @return list of all annotation strings
     */
//    @Override
//    public List<Annotation> getAnnotations() {
//        return annotationList.getAnnotations();
////        List<String> result = new ArrayList<>();
////        for (Annotation anno : annotationList) {
////            String annoS = anno.getSymbolAndAnnotation();
////            if (annoS != null) {
////                result.add(annoS);
////            }
////        }
////        return result
//    }

//    @Override
//    public boolean isXChromosomal() {
//        return getChromosome() == genomeVariant.getGenomePos().getRefDict().getContigNameToID().get("X");
//    }
//
//    @Override
//    public boolean isYChromosomal() {
//        return getChromosome() == genomeVariant.getGenomePos().getRefDict().getContigNameToID().get("Y");
//    }

//    @Override
//    public double getPhredScore() {
//        return variantContext.getPhredScaledQual();
//    }

//    public String getGeneSymbol() {
//        final Annotation anno = annotationList.getHighestImpactAnnotation();
//        return anno.getGeneSymbol();
//    }

//    public String getGenotypeAsString() {
//        // collect genotype string list
//        List<String> gtStrings = new ArrayList<>();
//        for (Genotype gt : variantContext.getGenotypes()) {
//            boolean firstAllele = true;
//            StringBuilder builder = new StringBuilder();
//            for (Allele allele : gt.getAlleles()) {
//                if (firstAllele) {
//                    firstAllele = false;
//                } else {
//                    builder.append('/');
//                }
//
//                if (allele.isNoCall()) {
//                    builder.append('.');
//                } else if (allele.equals(variantContext.getAlternateAllele(altAlleleID))) {
//                    builder.append('1');
//                } else {
//                    builder.append('0');
//                }
//            }
//            gtStrings.add(builder.toString());
//        }
//
//        // normalize 1/0 to 0/1 and join genotype strings with colon
//        for (int i = 0; i < gtStrings.size(); ++i) {
//            if (gtStrings.get(i).equals("1/0")) {
//                gtStrings.set(i, "0/1");
//            }
//        }
//        return Joiner.on(":").join(gtStrings);
//    }

//    public int getEntrezGeneId() {
//        final Annotation anno = annotationList.getHighestImpactAnnotation();
//        final TranscriptModel transcriptModel = anno.getTranscript();
//        if (transcriptModel == null || transcriptModel.getGeneID().equals("null")) {
//            return -1;
//        }
//        // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
//        // for UCSC. At this point, there is a hard dependency on using the UCSC database.
//        return Integer.parseInt(transcriptModel.getGeneID().substring("ENTREZ".length()));
//    }

//    public Genotype getGenotype() {
//        return variantContext.getGenotype(0);
//    }

//    @Override
//    public String toString() {
//        return "Variant [vc=" + variantContext + ", altAlleleID=" + altAlleleID + ", annotations=" + annotationList + ", change="
//                + genomeVariant + "]";
//    }
}
