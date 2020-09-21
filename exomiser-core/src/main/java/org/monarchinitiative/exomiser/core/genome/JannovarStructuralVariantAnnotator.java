/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class JannovarStructuralVariantAnnotator implements StructuralVariantAnnotator {

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
    public List<VariantAnnotation> annotate(String startContig, int startPos, String ref, String alt, VariantType variantType, int length, ConfidenceInterval ciStart, String endContig, int endPos, ConfidenceInterval ciEnd) {
//        logger.info("Annotating {} {} {} {} {} {} {} {} {}", structuralType, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd, length);
        SVAnnotations svAnnotations = jannovarAnnotationService
                .annotateStructuralVariant(variantType, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd);
        return buildVariantAnnotations(svAnnotations, variantType, ref, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd, length);
    }

    private List<VariantAnnotation> buildVariantAnnotations(SVAnnotations svAnnotations, VariantType variantType, String ref, String alt, String startContig, int startPos, ConfidenceInterval ciStart, String endContig, int endPos, ConfidenceInterval ciEnd, int length) {
        // This is a map of gene symbol to SVAnnotation
        // each SVAnnotation contains a TranscriptModel mapped to a geneSymbol. Transcripts overlapping multiple genes will be seen multiple times.
        Map<String, List<SVAnnotation>> annotationsByGeneSymbol = svAnnotations.getAnnotations()
                .stream()
                .collect(groupingBy(this::buildGeneSymbol));

        return annotationsByGeneSymbol.values()
                .stream()
                .map(geneSvAnnotations -> toStructuralVariantAnnotation(genomeAssembly, svAnnotations.getGenomeVariant(), geneSvAnnotations, structuralType, ref, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd, length))
                .collect(toList());
    }

    private String buildGeneSymbol(SVAnnotation svAnnotation) {
        return svAnnotation == null ? "." : TranscriptModelUtil.getTranscriptGeneSymbol(svAnnotation.getTranscript());
    }

    private VariantAnnotation toStructuralVariantAnnotation(GenomeAssembly genomeAssembly, SVGenomeVariant genomeVariant, List<SVAnnotation> svAnnotations, VariantType variantType, String ref, String alt, String startContig, int startPos, ConfidenceInterval ciStart, String endContig, int endPos, ConfidenceInterval ciEnd, int length) {
        svAnnotations.sort(SVAnnotation::compareTo);
        SVAnnotation highestImpactAnnotation = svAnnotations.get(0);
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
        VariantEffect variantEffect = checkRegulatoryRegionVariantEffect(highestImpactEffect, chr, startPos);

        return VariantAnnotation.builder()
                .geneId(geneId)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .annotations(annotations)
                .genomeAssembly(genomeAssembly)
                .chromosome(chr)
                .chromosomeName(getChromosomeNameOrDefault(genomeVariant.getChrName(), startContig))
                .start(startPos)
                .startMin(startPos + genomeVariant.getPosCILowerBound())
                .startMax(startPos + genomeVariant.getPosCIUpperBound())
                .endChromosome(endChr)
                .endChromosomeName(endContig)
                .end(endPos)
                .endMin(endPos + genomeVariant.getPos2CILowerBound())
                .endMax(endPos + genomeVariant.getPos2CIUpperBound())
                .length(length)
                .variantType(variantType)
                .ref(ref)
                .alt(alt)
                .build();
    }

    private VariantEffect getHighestImpactEffect(SVAnnotation highestImpactAnnotation) {
        return (highestImpactAnnotation == null || highestImpactAnnotation.getMostPathogenicVariantEffect() == null) ? VariantEffect.STRUCTURAL_VARIANT : highestImpactAnnotation
                .getMostPathogenicVariantEffect();
    }

    private String getChromosomeNameOrDefault(String chrName, String startContig) {
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

    private String buildGeneId(SVAnnotation svAnnotation) {
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
