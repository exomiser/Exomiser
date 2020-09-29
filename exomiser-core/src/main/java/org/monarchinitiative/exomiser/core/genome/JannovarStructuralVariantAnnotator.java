/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
import de.charite.compbio.jannovar.reference.SVGenomeVariant;
import org.monarchinitiative.exomiser.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class JannovarStructuralVariantAnnotator implements VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarStructuralVariantAnnotator.class);

    private final GenomeAssembly genomeAssembly;
    private final JannovarAnnotationService jannovarAnnotationService;
    private final ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex;

    JannovarStructuralVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.genomeAssembly = genomeAssembly;
        this.jannovarAnnotationService = new JannovarAnnotationService(jannovarData);
        this.regulatoryRegionIndex = regulatoryRegionIndex;
    }

    @Override
    public List<VariantAnnotation> annotate(VariantCoordinates variantCoordinates) {
        SVAnnotations svAnnotations = jannovarAnnotationService
                .annotateStructuralVariant(variantCoordinates.getVariantType(), variantCoordinates.getAlt(), variantCoordinates
                        .getStartContigName(), variantCoordinates.getStart(), variantCoordinates.getStartCi(), variantCoordinates
                        .getEndContigName(), variantCoordinates.getEnd(), variantCoordinates.getEndCi());
        return buildVariantAnnotations(svAnnotations, variantCoordinates);
    }

    private List<VariantAnnotation> buildVariantAnnotations(SVAnnotations svAnnotations, VariantCoordinates varCoords) {
        if (!svAnnotations.hasAnnotation()) {
            return List.of(toStructuralVariantAnnotation(genomeAssembly, svAnnotations.getGenomeVariant(), new ArrayList<>(), varCoords));
        }

        // This is a map of gene symbol to SVAnnotation
        // each SVAnnotation contains a TranscriptModel mapped to a geneSymbol. Transcripts overlapping multiple genes will be seen multiple times.
        Map<String, List<SVAnnotation>> annotationsByGeneSymbol = svAnnotations.getAnnotations()
                .stream()
                .collect(groupingBy(this::buildGeneSymbol));

        return annotationsByGeneSymbol.values()
                .stream()
                .map(geneSvAnnotations -> toStructuralVariantAnnotation(genomeAssembly, svAnnotations.getGenomeVariant(), geneSvAnnotations, varCoords))
                .collect(toList());
    }

    private String buildGeneSymbol(@Nullable SVAnnotation svAnnotation) {
        return svAnnotation == null ? "." : TranscriptModelUtil.getTranscriptGeneSymbol(svAnnotation.getTranscript());
    }

    private VariantAnnotation toStructuralVariantAnnotation(GenomeAssembly genomeAssembly, SVGenomeVariant genomeVariant, List<SVAnnotation> svAnnotations, VariantCoordinates varCoords) {
        svAnnotations.sort(SVAnnotation::compareTo);
//        svAnnotations.forEach(svAnnotation -> logger.info("{}", svAnnotation));
        SVAnnotation highestImpactAnnotation = svAnnotations.isEmpty() ? null : svAnnotations.get(0);
        //Attention! highestImpactAnnotation can be null
        VariantEffect highestImpactEffect = getHighestImpactEffect(highestImpactAnnotation);
        String geneSymbol = buildGeneSymbol(highestImpactAnnotation);
        String geneId = buildGeneId(highestImpactAnnotation);
        List<TranscriptAnnotation> annotations = buildSvTranscriptAnnotations(svAnnotations);

        int chr = genomeVariant.getChr();
        int endChr = genomeVariant.getChr2();
        // The genomeVariant.getStart() seems to be 0-based despite being constructed using 1-based coordinates
        //  so ensure we use the original startPos from the VCF to avoid confusion.
        //TODO: enable this to do regulatory gubbins with SVs
        VariantEffect variantEffect = checkRegulatoryRegionVariantEffect(highestImpactEffect, chr, varCoords.getStart());

        return VariantAnnotation.builder()
                .geneId(geneId)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .annotations(annotations)
                .genomeAssembly(genomeAssembly)
                .chromosome(chr)
                .contig(getChromosomeNameOrDefault(genomeVariant.getChrName(), varCoords.getStartContigName()))
                .start(varCoords.getStart())
                .startCi(varCoords.getStartCi())
                .endChromosome(endChr)
                .endContig(varCoords.getEndContigName())
                .end(varCoords.getEnd())
                .endCi(varCoords.getEndCi())
                .length(varCoords.getLength())
                .variantType(varCoords.getVariantType())
                .ref(varCoords.getRef())
                .alt(varCoords.getAlt())
                .build();
    }

    private VariantEffect getHighestImpactEffect(@Nullable SVAnnotation highestImpactAnnotation) {
        return (highestImpactAnnotation == null || highestImpactAnnotation.getMostPathogenicVariantEffect() == null) ? VariantEffect.STRUCTURAL_VARIANT : highestImpactAnnotation
                .getMostPathogenicVariantEffect();
    }

    private String getChromosomeNameOrDefault(@Nullable String chrName, String startContig) {
        return chrName == null ? startContig : chrName;
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
                .variantEffect(getVariantEffectOrDefault(svAnnotation.getMostPathogenicVariantEffect(), VariantEffect.STRUCTURAL_VARIANT))
                .accession(TranscriptModelUtil.getTranscriptAccession(svAnnotation.getTranscript()))
                .geneSymbol(buildGeneSymbol(svAnnotation))
                .build();
    }

    private VariantEffect getVariantEffectOrDefault(VariantEffect annotatedEffect, VariantEffect defaultEffect) {
        return annotatedEffect == null ? defaultEffect : annotatedEffect;
    }

    private String buildGeneId(@Nullable SVAnnotation svAnnotation) {
        return svAnnotation == null ? "" : TranscriptModelUtil.getTranscriptGeneId(svAnnotation.getTranscript());
    }

    //Adds the missing REGULATORY_REGION_VARIANT effect to variants - this isn't in the Jannovar data set.
    private VariantEffect checkRegulatoryRegionVariantEffect(VariantEffect variantEffect, int chr, int pos) {
        //n.b this check here is important as ENSEMBLE can have regulatory regions overlapping with missense variants.
        // TODO do we need a regulatoryRegionIndex.hasRegionOverlapping(startChr, startPos, endChr, endPos)
        if (isIntergenicOrUpstreamOfGene(variantEffect) && regulatoryRegionIndex.hasRegionContainingPosition(chr, pos)) {
            //the effect is the same for all regulatory regions, so for the sake of speed, just assign it here rather than look it up from the list
            return VariantEffect.REGULATORY_REGION_VARIANT;
        }
        return variantEffect;
    }

    private boolean isIntergenicOrUpstreamOfGene(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.INTERGENIC_VARIANT || variantEffect == VariantEffect.UPSTREAM_GENE_VARIANT;
    }
}
