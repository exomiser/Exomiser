/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotator implements org.monarchinitiative.exomiser.core.genome.VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarVariantAnnotator.class);

    private final GenomeAssembly genomeAssembly;
    private final ReferenceDictionary referenceDictionary;
    private final de.charite.compbio.jannovar.annotation.VariantAnnotator variantAnnotator;

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private static final int UNKNOWN_CHROMOSOME = 0;

    public JannovarVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData) {
        this.genomeAssembly = genomeAssembly;
        this.referenceDictionary = jannovarData.getRefDict();
        this.variantAnnotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
    }

    public VariantAnnotation annotate(String contig, int pos, String ref, String alt) {
        AllelePosition trimmedAllele = AllelePosition.trim(pos, ref, alt);
        VariantAnnotations variantAnnotations = getVariantAnnotations(contig, trimmedAllele);
        return buildVariantAlleleAnnotation(contig, trimmedAllele, variantAnnotations);
    }

    protected VariantAnnotations getVariantAnnotations(String contig, AllelePosition allelePosition) {
        return getVariantAnnotations(contig, allelePosition.getPos(), allelePosition.getRef(), allelePosition.getAlt());
    }

    protected VariantAnnotations getVariantAnnotations(String contig, int pos, String ref, String alt) {
        GenomeVariant genomeVariant = buildOneBasedFwdStrandGenomicVariant(contig, pos, ref, alt);
        return buildAnnotations(genomeVariant);
    }

    private GenomeVariant buildOneBasedFwdStrandGenomicVariant(String contig, int pos, String ref, String alt) {
        int chr = getIntValueOfChromosomeOrZero(contig);
        GenomePosition genomePosition = new GenomePosition(referenceDictionary, Strand.FWD, chr, pos, PositionType.ONE_BASED);
        return new GenomeVariant(genomePosition, ref, alt);
    }

    private Integer getIntValueOfChromosomeOrZero(String contig) {
        return referenceDictionary.getContigNameToID().getOrDefault(contig, UNKNOWN_CHROMOSOME);
    }

    private VariantAnnotations buildAnnotations(GenomeVariant genomeVariant) {
        //TODO: check this can be removed
        if (genomeVariant.getChr() == UNKNOWN_CHROMOSOME) {
            //Need to check this here and return otherwise the variantAnnotator will throw a NPE.
            return VariantAnnotations.buildEmptyList(genomeVariant);
        }
        try {
            return variantAnnotator.buildAnnotations(genomeVariant);
        } catch (Exception e) {
            logger.debug("Unable to annotate variant {}-{}-{}-{}",
                    genomeVariant.getChrName(),
                    genomeVariant.getPos(),
                    genomeVariant.getRef(),
                    genomeVariant.getAlt(),
                    e);
        }
        return VariantAnnotations.buildEmptyList(genomeVariant);
    }

    private VariantAnnotation buildVariantAlleleAnnotation(String contig, AllelePosition allelePosition, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        String chromosomeName = genomeVariant.getChrName() == null ? contig : genomeVariant.getChrName();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();
        String geneSymbol = buildGeneSymbol(highestImpactAnnotation);
        String geneId = buildEntrezGeneId(highestImpactAnnotation);

        VariantEffect variantEffect = variantAnnotations.getHighestImpactEffect();
        List<TranscriptAnnotation> annotations = buildTranscriptAnnotations(variantAnnotations.getAnnotations());

        int pos = allelePosition.getPos();
        String ref = allelePosition.getRef();
        String alt = allelePosition.getAlt();

        return VariantAnnotation.builder()
                .genomeAssembly(genomeAssembly)
                .chromosome(chr)
                .chromosomeName(chromosomeName)
                .position(pos)
                .ref(ref)
                .alt(alt)
                .geneId(geneId)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .annotations(annotations).build();
    }

    private List<TranscriptAnnotation> buildTranscriptAnnotations(List<Annotation> annotations) {
        List<TranscriptAnnotation> transcriptAnnotations = new ArrayList<>(annotations.size());
        for (Annotation annotation : annotations) {
            transcriptAnnotations.add(toTranscriptAnnotation(annotation));
        }
        return transcriptAnnotations;
    }

    private TranscriptAnnotation toTranscriptAnnotation(Annotation annotation) {
        return TranscriptAnnotation.builder()
                .variantEffect(annotation.getMostPathogenicVarType())
                .accession(getTranscriptAccession(annotation))
                .geneSymbol(buildGeneSymbol(annotation))
//                .hgvsGenomic(annotation.getGenomicNTChangeStr())
                .hgvsCdna(annotation.getCDSNTChangeStr())
                .hgvsProtein(annotation.getProteinChangeStr())
                .distanceFromNearestGene(getDistFromNearestGene(annotation))
                .build();
    }

    private String getTranscriptAccession(Annotation annotation) {
        TranscriptModel transcriptModel = annotation.getTranscript();
        if (transcriptModel == null) {
            return "";
        }
        return transcriptModel.getAccession();
    }

    private int getDistFromNearestGene(Annotation annotation) {

        TranscriptModel tm = annotation.getTranscript();
        if (tm == null) {
            return Integer.MIN_VALUE;
        }
        GenomeVariant change = annotation.getGenomeVariant();
        Set<VariantEffect> effects = annotation.getEffects();
        if (effects.contains(VariantEffect.INTERGENIC_VARIANT) || effects.contains(VariantEffect.UPSTREAM_GENE_VARIANT) || effects
                .contains(VariantEffect.DOWNSTREAM_GENE_VARIANT)) {
            if (change.getGenomeInterval().isLeftOf(tm.getTXRegion().getGenomeBeginPos()))
                return tm.getTXRegion().getGenomeBeginPos().differenceTo(change.getGenomeInterval().getGenomeEndPos());
            else
                return change.getGenomeInterval().getGenomeBeginPos().differenceTo(tm.getTXRegion().getGenomeEndPos());
        }

        return Integer.MIN_VALUE;
    }

    private String buildEntrezGeneId(Annotation annotation) {
        if (annotation == null) {
            return "";
        }

        final TranscriptModel transcriptModel = annotation.getTranscript();
        if (transcriptModel == null) {
            return "";
        }
        //this will now return the id from the user-specified data source. Previously would only return the Entrez id.
        String geneId = transcriptModel.getGeneID();
        return geneId == null ? "" : geneId;
    }

    private String buildGeneSymbol(Annotation annotation) {
        if (annotation == null || annotation.getGeneSymbol() == null) {
            return ".";
        } else {
            return annotation.getGeneSymbol();
        }
    }

}
