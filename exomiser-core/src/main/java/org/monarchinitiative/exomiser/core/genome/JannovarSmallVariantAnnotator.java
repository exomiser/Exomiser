/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.*;
import de.charite.compbio.jannovar.annotation.AnnotationLocation.RankType;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.hgvs.AminoAcidCode;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.svart.GenomicVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class JannovarSmallVariantAnnotator implements VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarSmallVariantAnnotator.class);

    private final GenomeAssembly genomeAssembly;
    private final JannovarVariantConverter jannovarVariantConverter;
    private final JannovarAnnotationService jannovarAnnotationService;
    private final ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex;

    JannovarSmallVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.genomeAssembly = genomeAssembly;
        this.jannovarVariantConverter = new JannovarVariantConverter(jannovarData);
        this.jannovarAnnotationService = new JannovarAnnotationService(jannovarData);
        this.regulatoryRegionIndex = regulatoryRegionIndex;
    }

    @Override
    public GenomeAssembly genomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public List<VariantAnnotation> annotate(GenomicVariant variant) {
        if (variant == null) {
            return List.of();
        }
        GenomeVariant genomeVariant = jannovarVariantConverter.toGenomeVariant(variant);
        VariantAnnotations variantAnnotations = jannovarAnnotationService.annotateGenomeVariant(genomeVariant);
        return buildVariantAnnotations(variant, variantAnnotations);
    }

    private List<VariantAnnotation> buildVariantAnnotations(GenomicVariant variant, VariantAnnotations variantAnnotations) {
        // Group annotations by geneSymbol then create new Jannovar.VariantAnnotations from these then return List<VariantAnnotation>
        // see issue https://github.com/exomiser/Exomiser/issues/294. However it creates approximately 2x as many variants
        // which doubles the runtime, and most of the new variants are then filtered out. So here we're trying to limit the amount of new
        // VariantAnnotations returned by only splitting those with a MODERATE or greater putative impact.
        if (effectsMoreThanOneGeneWithMinimumImpact(variantAnnotations, PutativeImpact.MODERATE)) {
            return splitAnnotationsByGene(variantAnnotations)
                    .map(variantGeneAnnotations -> buildVariantAlleleAnnotation(variant, variantGeneAnnotations))
                    .collect(toUnmodifiableList());
        }
        return List.of(buildVariantAlleleAnnotation(variant, variantAnnotations));
    }

    private boolean effectsMoreThanOneGeneWithMinimumImpact(VariantAnnotations variantAnnotations, PutativeImpact minimumImpact) {
        List<Annotation> annotations = variantAnnotations.getAnnotations();
        // increasing cost of computation with each stage of the evaluation - don't change order.
        return annotations.size() > 1 &&
                variantEffectImpactIsAtLeast(variantAnnotations.getHighestImpactEffect(), minimumImpact) &&
                annotationsContainMoreThanOneGene(annotations) &&
                annotationsEffectMoreThanOneGeneWithMinimumImpact(annotations, minimumImpact);
    }

    private boolean variantEffectImpactIsAtLeast(VariantEffect variantEffect, PutativeImpact minimumImpact) {
        // HIGH and MODERATE are ordinal 0, 1 which we want to look at if there are any
        return variantEffect.getImpact().ordinal() <= minimumImpact.ordinal();
    }

    private boolean annotationsContainMoreThanOneGene(List<Annotation> annotations) {
        return annotations.stream().map(Annotation::getGeneSymbol).distinct().count() > 1L;
    }

    private boolean annotationsEffectMoreThanOneGeneWithMinimumImpact(List<Annotation> annotations, PutativeImpact minimumImpact) {
        Map<String, List<Annotation>> annotationsByGene = annotations.stream()
                .filter(annotation -> annotation.getMostPathogenicVarType() != null)
                .filter(annotation -> variantEffectImpactIsAtLeast(annotation.getMostPathogenicVarType(), minimumImpact))
                .collect(groupingBy(Annotation::getGeneSymbol));
        return annotationsByGene.size() > 1;
    }

    private Stream<VariantAnnotations> splitAnnotationsByGene(VariantAnnotations variantAnnotations) {
        List<Annotation> annotations = variantAnnotations.getAnnotations();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        logger.debug("Multiple annotations for {} {} {} {}", genomeVariant.getChrName(), genomeVariant.getPos(), genomeVariant
                .getRef(), genomeVariant.getAlt());

        return annotations.stream()
                .collect(groupingBy(Annotation::getGeneSymbol))
                .values().stream()
                //.peek(annotationList -> annotationList.forEach(annotation -> logger.info("{}", toAnnotationString(genomeVariant, annotation))))
                .map(annos -> new VariantAnnotations(genomeVariant, annos));
    }

    private String toAnnotationString(GenomeVariant genomeVariant, Annotation annotation) {
        return genomeVariant + ", " +
                annotation.getTranscript().getGeneSymbol() + ", " +
                annotation.getTranscript().getGeneID() + ", " +
                annotation.getMostPathogenicVarType() + ", " +
                annotation.getPutativeImpact() + ", " +
                annotation.getTranscript();
    }

    private VariantAnnotation buildVariantAlleleAnnotation(GenomicVariant variant, VariantAnnotations variantAnnotations) {
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();
        String geneSymbol = buildGeneSymbol(highestImpactAnnotation);
        String geneId = buildGeneId(highestImpactAnnotation);

        //Jannovar presently ignores all structural variants, so flag it here. Not that we do anything with them at present.
        VariantEffect highestImpactEffect = variant.isSymbolic() ? VariantEffect.STRUCTURAL_VARIANT : variantAnnotations
                .getHighestImpactEffect();
        List<TranscriptAnnotation> transcriptAnnotations = buildTranscriptAnnotations(variantAnnotations.getAnnotations());

        VariantEffect variantEffect = checkRegulatoryRegionVariantEffect(highestImpactEffect, variant);

        return VariantAnnotation.of(geneSymbol, geneId, variantEffect, transcriptAnnotations);
    }

    private String buildGeneId(Annotation annotation) {
        return annotation == null ? "" : TranscriptModelUtil.getTranscriptGeneId(annotation.getTranscript());
    }

    private String buildGeneSymbol(Annotation annotation) {
        return annotation == null ? "" : TranscriptModelUtil.getTranscriptGeneSymbol(annotation.getTranscript());
    }

    private List<TranscriptAnnotation> buildTranscriptAnnotations(List<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return List.of();
        }
        List<TranscriptAnnotation> transcriptAnnotations = new ArrayList<>(annotations.size());
        for (Annotation annotation : annotations) {
            transcriptAnnotations.add(toTranscriptAnnotation(annotation));
        }
        return List.copyOf(transcriptAnnotations);
    }

    private TranscriptAnnotation toTranscriptAnnotation(Annotation annotation) {
        AnnotationLocation annoLoc = annotation.getAnnoLoc();
        return TranscriptAnnotation.builder()
                .variantEffect(getVariantEffectOrDefault(annotation.getMostPathogenicVarType(), VariantEffect.SEQUENCE_VARIANT))
                .accession(TranscriptModelUtil.getTranscriptAccession(annotation.getTranscript()))
                .geneSymbol(buildGeneSymbol(annotation))
                .hgvsGenomic((annotation.getGenomicNTChange() == null) ? "" : annotation.getGenomicNTChangeStr())
                .hgvsCdna(annotation.getCDSNTChangeStr())
                .hgvsProtein(annotation.getProteinChangeStr(AminoAcidCode.THREE_LETTER))
                .rankType(annoLoc == null ? TranscriptAnnotation.RankType.UNDEFINED : getRankType(annoLoc.getRankType()))
                .rankTotal(annoLoc == null ? -1 : annoLoc.getTotalRank())
                .rank(annoLoc == null ? -1 : annoLoc.getRank() + 1)
                .distanceFromNearestGene(getDistFromNearestGene(annotation))
                .build();
    }

    private TranscriptAnnotation.RankType getRankType(RankType annoLocRankType) {
        switch (annoLocRankType) {
            case EXON:
                return TranscriptAnnotation.RankType.EXON;
            case INTRON:
                return TranscriptAnnotation.RankType.INTRON;
            case UNDEFINED:
                return TranscriptAnnotation.RankType.UNDEFINED;
        }
        return TranscriptAnnotation.RankType.UNDEFINED;
    }

    private VariantEffect getVariantEffectOrDefault(VariantEffect annotatedEffect, VariantEffect defaultEffect) {
        return annotatedEffect == null ? defaultEffect : annotatedEffect;
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
            if (change.getGenomeInterval().isLeftOf(tm.getTXRegion().getGenomeBeginPos())) {
                return tm.getTXRegion().getGenomeBeginPos().differenceTo(change.getGenomeInterval().getGenomeEndPos());
            } else {
                return change.getGenomeInterval().getGenomeBeginPos().differenceTo(tm.getTXRegion().getGenomeEndPos());
            }
        }
        // we're in a gene region so there is no distance
        return 0;
    }

    //Adds the missing REGULATORY_REGION_VARIANT effect to variants - this isn't in the Jannovar data set.
    private VariantEffect checkRegulatoryRegionVariantEffect(VariantEffect variantEffect, GenomicVariant variant) {
        //n.b this check here is important as ENSEMBLE can have regulatory regions overlapping with missense variants.
        if (isIntergenicOrUpstreamOfGene(variantEffect) && regulatoryRegionIndex.hasRegionOverlappingVariant(variant)) {
            //the effect is the same for all regulatory regions, so for the sake of speed, just assign it here rather than look it up from the list
            return VariantEffect.REGULATORY_REGION_VARIANT;
        }
        return variantEffect;
    }

    private boolean isIntergenicOrUpstreamOfGene(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.INTERGENIC_VARIANT || variantEffect == VariantEffect.UPSTREAM_GENE_VARIANT;
    }

}
