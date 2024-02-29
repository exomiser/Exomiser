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

import de.charite.compbio.jannovar.annotation.SVAnnotation;
import de.charite.compbio.jannovar.annotation.SVAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.SVGenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.svart.GenomicVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class JannovarStructuralVariantAnnotator implements VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarStructuralVariantAnnotator.class);
    private static final VariantAnnotation EMPTY_STRUCTURAL_ANNOTATION = VariantAnnotation.of("", "", STRUCTURAL_VARIANT, List.of());

    private static final VariantEffect DEFAULT_EFFECT = SEQUENCE_VARIANT;

    private final GenomeAssembly genomeAssembly;
    private final JannovarVariantConverter jannovarVariantConverter;
    private final JannovarAnnotationService jannovarAnnotationService;
    private final ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex;

    JannovarStructuralVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.genomeAssembly = genomeAssembly;
        this.jannovarAnnotationService = new JannovarAnnotationService(jannovarData);
        this.jannovarVariantConverter = new JannovarVariantConverter(jannovarData);
        this.regulatoryRegionIndex = regulatoryRegionIndex;
    }

    @Override
    public GenomeAssembly genomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public List<VariantAnnotation> annotate(GenomicVariant variant) {
        if (variant == null || variant.isBreakend()) {
            // TODO: re-enable breakends!
            return List.of();
        }
        SVGenomeVariant svGenomeVariant = jannovarVariantConverter.toSvGenomeVariant(variant);
        SVAnnotations svAnnotations = jannovarAnnotationService.annotateSvGenomeVariant(svGenomeVariant);
        return buildVariantAnnotations(svAnnotations, variant);
    }

    private List<VariantAnnotation> buildVariantAnnotations(SVAnnotations svAnnotations, GenomicVariant variant) {
        if (!svAnnotations.hasAnnotation()) {
            return List.of(EMPTY_STRUCTURAL_ANNOTATION);
        }

        // This is a map of gene symbol to SVAnnotation
        // each SVAnnotation contains a TranscriptModel mapped to a geneSymbol. Transcripts overlapping multiple genes will be seen multiple times.
        Map<String, List<SVAnnotation>> annotationsByGeneSymbol = svAnnotations.getAnnotations()
                .stream()
                .collect(groupingBy(this::buildGeneSymbol));

        return annotationsByGeneSymbol.values()
                .stream()
                .map(geneSvAnnotations -> toStructuralVariantAnnotation(variant, geneSvAnnotations))
                .toList();
    }

    private VariantAnnotation toStructuralVariantAnnotation(GenomicVariant variant, List<SVAnnotation> svAnnotations) {
        svAnnotations.sort(SVAnnotation::compareTo);
        @Nullable SVAnnotation highestImpactAnnotation = svAnnotations.isEmpty() ? null : svAnnotations.get(0);
        // CAUTION! highestImpactAnnotation can be null

        // Jannovar reports INSERTION & INVERSION as a HIGH impact, as the SO does not have terms to describe structural
        // variants within a coding sequence, so this loses the context of where the insertion was seen.
        // This is a potentially deleterious insertion:
        // [INSERTION, STRUCTURAL_VARIANT, CODING_SEQUENCE_VARIANT, CODING_TRANSCRIPT_VARIANT] -> CODING_SEQUENCE_VARIANT
        // This is a downstream, non-coding insertion so is probably uninteresting
        // [INSERTION, DOWNSTREAM_GENE_VARIANT, STRUCTURAL_VARIANT, CODING_TRANSCRIPT_VARIANT] -> DOWNSTREAM_GENE_VARIANT
        // This is an inversion overlapping a coding transcript. Note that CODING_TRANSCRIPT_VARIANT is a MODIFIER impact
        // [INVERSION, STRUCTURAL_VARIANT, CODING_TRANSCRIPT_VARIANT] -> CODING_TRANSCRIPT_VARIANT
        // TODO: This would be an ideal place to calculate a variantEffectScore (instead of in the VariantEffectPathogenicityScore)
        //  based on the SvAnna scoring system https://genomemedicine.biomedcentral.com/articles/10.1186/s13073-022-01046-6/tables/1
        VariantEffect highestImpactEffect = getHighestImpactEffect(highestImpactAnnotation);
        String geneSymbol = buildGeneSymbol(highestImpactAnnotation);
        String geneId = buildGeneId(highestImpactAnnotation);
        List<TranscriptAnnotation> transcriptAnnotations = buildSvTranscriptAnnotations(svAnnotations);

        VariantEffect variantEffect = checkRegulatoryRegionVariantEffect(highestImpactEffect, variant);

        return VariantAnnotation.of(geneSymbol, geneId, variantEffect, transcriptAnnotations);
    }

    private VariantEffect getHighestImpactEffect(@Nullable SVAnnotation highestImpactAnnotation) {
        return (highestImpactAnnotation == null || highestImpactAnnotation.getMostPathogenicVariantEffect() == null) ? DEFAULT_EFFECT :
                filterEffects(highestImpactAnnotation.getEffects());
    }

    private static VariantEffect filterEffects(Set<VariantEffect> variantEffects) {
        for (VariantEffect varEff : variantEffects) {
            if (!(varEff == INSERTION || varEff == INVERSION || varEff == STRUCTURAL_VARIANT)) {
                return varEff;
            }
        }
        return DEFAULT_EFFECT;
    }

    private List<TranscriptAnnotation> buildSvTranscriptAnnotations(List<SVAnnotation> svAnnotations) {
        List<TranscriptAnnotation> transcriptAnnotations = new ArrayList<>(svAnnotations.size());
        for (SVAnnotation annotation : svAnnotations) {
            transcriptAnnotations.add(toTranscriptAnnotation(annotation));
        }
        return transcriptAnnotations;
    }

    private TranscriptAnnotation toTranscriptAnnotation(SVAnnotation svAnnotation) {
        return TranscriptAnnotation.builder()
                .variantEffect(getVariantEffectOrDefault(svAnnotation.getMostPathogenicVariantEffect(), DEFAULT_EFFECT))
                .accession(TranscriptModelUtil.getTranscriptAccession(svAnnotation.getTranscript()))
                .geneSymbol(buildGeneSymbol(svAnnotation))
                .distanceFromNearestGene(getDistFromNearestGene(svAnnotation))
                .build();
    }

    private VariantEffect getVariantEffectOrDefault(VariantEffect annotatedEffect, VariantEffect defaultEffect) {
        return annotatedEffect == null ? defaultEffect : annotatedEffect;
    }

    private String buildGeneId(@Nullable SVAnnotation svAnnotation) {
        return svAnnotation == null ? "" : TranscriptModelUtil.getTranscriptGeneId(svAnnotation.getTranscript());
    }

    private String buildGeneSymbol(@Nullable SVAnnotation svAnnotation) {
        return svAnnotation == null ? "" : TranscriptModelUtil.getTranscriptGeneSymbol(svAnnotation.getTranscript());
    }

    //Adds the missing REGULATORY_REGION_VARIANT effect to variants - this isn't in the Jannovar data set.
    private VariantEffect checkRegulatoryRegionVariantEffect(VariantEffect variantEffect, GenomicVariant variant) {
        //n.b this check here is important as ENSEMBLE can have regulatory regions overlapping with missense variants.
        // TODO do we need a regulatoryRegionIndex.hasRegionOverlapping(chr, start, end)
        if (isIntergenicOrUpstreamOfGene(variantEffect) && regulatoryRegionIndex.hasRegionOverlappingVariant(variant)) {
            //the effect is the same for all regulatory regions, so for the sake of speed, just assign it here rather than look it up from the list
            return VariantEffect.REGULATORY_REGION_VARIANT;
        }
        return variantEffect;
    }

    private boolean isIntergenicOrUpstreamOfGene(VariantEffect variantEffect) {
        return variantEffect == INTERGENIC_VARIANT || variantEffect == UPSTREAM_GENE_VARIANT;
    }

    private int getDistFromNearestGene(SVAnnotation annotation) {
        TranscriptModel tm = annotation.getTranscript();
        if (tm == null) {
            return Integer.MIN_VALUE;
        }
        Set<VariantEffect> effects = annotation.getEffects();
        SVGenomeVariant change = annotation.getVariant();
        if (change.getChr() != change.getChr2()) {
            // breakend
            if (change.getChr() == tm.getChr()) {
                return distToNearestGene(tm, new GenomeInterval(change.getGenomePos(), 1), effects);
            }
            if (change.getChr2() == tm.getChr()) {
                return distToNearestGene(tm, new GenomeInterval(change.getGenomePos2(), 1), effects);
            }
        }
        return distToNearestGene(tm, change.getGenomeInterval(), effects);
    }

    private int distToNearestGene(TranscriptModel tm, GenomeInterval genomeInterval, Set<VariantEffect> effects) {
        if (effects.contains(INTERGENIC_VARIANT) || effects.contains(UPSTREAM_GENE_VARIANT) || effects.contains(DOWNSTREAM_GENE_VARIANT)) {
            if (genomeInterval.isLeftOf(tm.getTXRegion().getGenomeBeginPos())) {
                return tm.getTXRegion().getGenomeBeginPos().differenceTo(genomeInterval.getGenomeEndPos());
            } else {
                return genomeInterval.getGenomeBeginPos().differenceTo(tm.getTXRegion().getGenomeEndPos());
            }
        }
        // we're in a gene region so there is no distance
        return 0;
    }
}
