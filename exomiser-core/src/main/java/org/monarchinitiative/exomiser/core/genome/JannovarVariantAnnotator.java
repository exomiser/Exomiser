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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.*;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.hgvs.AminoAcidCode;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.SVGenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Handles creation of {@link VariantAnnotation} using Jannovar.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotator implements VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarVariantAnnotator.class);

    private final GenomeAssembly genomeAssembly;
    private final JannovarAnnotationService jannovarAnnotationService;
    private final ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex;

    public JannovarVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.genomeAssembly = genomeAssembly;
        this.jannovarAnnotationService = new JannovarAnnotationService(jannovarData);
        this.regulatoryRegionIndex = regulatoryRegionIndex;
    }

    /**
     * Given a single allele from a multi-positional site, incoming variants might not be fully trimmed.
     * In cases where there is repetition, depending on the program used, the final variant allele will be different.
     * VCF:      X-118887583-TCAAAA-TCAAAACAAAA
     * Exomiser: X-118887583-T     -TCAAAA
     * CellBase: X-118887584--     - CAAAA
     * Jannovar: X-118887588-      -      CAAAA
     * Nirvana:  X-118887589-      -      CAAAA
     * <p>
     * Trimming first with Exomiser, then annotating with Jannovar, constrains the Jannovar annotation to the same
     * position as Exomiser.
     * VCF:      X-118887583-TCAAAA-TCAAAACAAAA
     * Exomiser: X-118887583-T     -TCAAAA
     * CellBase: X-118887584--     - CAAAA
     * Jannovar: X-118887583-      - CAAAA      (Jannovar is zero-based)
     * Nirvana:  X-118887584-      - CAAAA
     * <p>
     * Cellbase:
     * https://github.com/opencb/biodata/blob/develop/biodata-tools/src/main/java/org/opencb/biodata/tools/variant/VariantNormalizer.java
     * http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/genomic/variant/X:118887583:TCAAAA:TCAAAACAAAA/annotation?assembly=grch37&limit=-1&skip=-1&count=false&Output format=json&normalize=true
     * <p>
     * Nirvana style trimming:
     * https://github.com/Illumina/Nirvana/blob/master/VariantAnnotation/Algorithms/BiDirectionalTrimmer.cs
     * <p>
     * Jannovar:
     * https://github.com/charite/jannovar/blob/master/jannovar-core/src/main/java/de/charite/compbio/jannovar/reference/VariantDataCorrector.java
     *
     * @param chr chromosome identifier
     * @param pos 1-based start position of the first base of the ref string
     * @param ref reference base(s)
     * @param alt alternate bases
     * @return {@link VariantAnnotation} objects trimmed according to {@link AllelePosition#trim(int, String, String)} and annotated using Jannovar.
     * @since 13.0.0
     */
    @Override
    public List<VariantAnnotation> annotate(String chr, int pos, String ref, String alt) {
        //so given the above, trim the allele first, then annotate it otherwise untrimmed alleles from multi-allelic sites will give different results
        AllelePosition trimmedAllele = AllelePosition.trim(pos, ref, alt);
        VariantAnnotations variantAnnotations = jannovarAnnotationService
                .annotateVariant(chr, trimmedAllele.getStart(), trimmedAllele.getRef(), trimmedAllele.getAlt());

        // Group annotations by geneSymbol then create new Jannovar.VariantAnnotations from these then return List<VariantAnnotation>
        // see issue https://github.com/exomiser/Exomiser/issues/294. However it creates approximately 2x as many variants
        // which doubles the runtime, and most of the new variants are then filtered out. So here we're trying to limit the amount of new
        // VariantAnnotations returned by only splitting those with a MODERATE or greater putative impact.
        if (effectsMoreThanOneGeneWithMinimumImpact(variantAnnotations, PutativeImpact.MODERATE)) {
            return splitAnnotationsByGene(variantAnnotations)
                    .map(variantGeneAnnotations -> buildVariantAlleleAnnotation(genomeAssembly, chr, trimmedAllele, variantGeneAnnotations))
                    .collect(toList());
        }
        return ImmutableList.of(buildVariantAlleleAnnotation(genomeAssembly, chr, trimmedAllele, variantAnnotations));
    }

    @Override
    public List<VariantAnnotation> annotateStructuralVariant(StructuralType structuralType, String ref, String alt, String startContig, int startPos, List<Integer> ciStart, String endContig, int endPos, List<Integer> ciEnd, int length) {
//        logger.info("Annotating {} {} {} {} {} {} {} {} {}", structuralType, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd, length);
        SVAnnotations svAnnotations = jannovarAnnotationService
                .annotateStructuralVariant(structuralType, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd);

//        svAnnotations.getAnnotations().forEach(svAnnotation -> logger.info(toAnnotationString(structuralType, svAnnotation)));

        // This is a map of gene symbol to SVAnnotation
        // each SVAnnotation contains a TranscriptModel mapped to a geneSymbol. Transcripts overlapping multiple genes will be seen multiple times.
        Map<String, List<SVAnnotation>> annotationsByGeneSymbol = svAnnotations.getAnnotations().stream().collect(groupingBy(this::buildGeneSymbol));

        return annotationsByGeneSymbol.values()
                .stream()
                .map(geneSvAnnotations -> toStructuralVariantAnnotation(genomeAssembly, svAnnotations.getGenomeVariant(), geneSvAnnotations, structuralType, ref, alt, startContig, startPos, ciStart, endContig, endPos, ciEnd, length))
                .collect(toList());
    }

    private boolean effectsMoreThanOneGeneWithMinimumImpact(VariantAnnotations variantAnnotations, PutativeImpact minimumImpact) {
        ImmutableList<Annotation> annotations = variantAnnotations.getAnnotations();
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

    private boolean annotationsContainMoreThanOneGene(ImmutableList<Annotation> annotations) {
        return annotations.stream().map(Annotation::getGeneSymbol).distinct().count() > 1L;
    }

    private boolean annotationsEffectMoreThanOneGeneWithMinimumImpact(ImmutableList<Annotation> annotations, PutativeImpact minimumImpact) {
        Map<String, List<Annotation>> annotationsByGene = annotations.stream()
                .filter(annotation -> annotation.getMostPathogenicVarType() != null)
                .filter(annotation -> variantEffectImpactIsAtLeast(annotation.getMostPathogenicVarType(), minimumImpact))
                .collect(groupingBy(Annotation::getGeneSymbol));
        return annotationsByGene.size() > 1;
    }

    private Stream<VariantAnnotations> splitAnnotationsByGene(VariantAnnotations variantAnnotations) {
        ImmutableList<Annotation> annotations = variantAnnotations.getAnnotations();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        logger.debug("Multiple annotations for {} {} {} {}", genomeVariant.getChrName(), genomeVariant.getPos(), genomeVariant.getRef(), genomeVariant.getAlt());

        return annotations.stream()
                .collect(groupingBy(Annotation::getGeneSymbol))
                .values().stream()
                //.peek(annotationList -> annotationList.forEach(annotation -> logger.info("{}", toAnnotationString(annotation))))
                .map(annos -> new VariantAnnotations(genomeVariant, annos));
    }

    private String toAnnotationString(StructuralType structuralType, SVAnnotation annotation) {
        return structuralType + ", " +
                annotation.getVariant()  + ", " +
                annotation.getTranscript().getGeneSymbol() + ", " +
                annotation.getTranscript().getGeneID() + ", " +
                annotation.getMostPathogenicVariantEffect() + ", " +
                annotation.getPutativeImpact() + ", " +
                annotation.getTranscript();
    }


    private VariantAnnotation toStructuralVariantAnnotation(GenomeAssembly genomeAssembly, SVGenomeVariant genomeVariant, List<SVAnnotation> svAnnotations, StructuralType structuralType, String ref, String alt, String startContig, int startPos, List<Integer> ciStart, String endContig, int endPos, List<Integer> ciEnd, int length) {
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
                .endContig(endContig)
                .end(endPos)
                .endMin(endPos + genomeVariant.getPos2CILowerBound())
                .endMax(endPos + genomeVariant.getPos2CIUpperBound())
                .length(length)
                .structuralType(structuralType)
                .ref(ref)
                .alt(alt)
                .build();
    }

    private String getChromosomeNameOrDefault(String chrName, String startContig) {
        return chrName == null ? startContig : chrName;
    }

    private VariantEffect getHighestImpactEffect(SVAnnotation highestImpactAnnotation) {
        return (highestImpactAnnotation == null || highestImpactAnnotation.getMostPathogenicVariantEffect() == null) ? VariantEffect.STRUCTURAL_VARIANT : highestImpactAnnotation.getMostPathogenicVariantEffect();
    }

    // TODO: Do these need splitting into static utility classes for structural and small variants?
    private VariantAnnotation buildVariantAlleleAnnotation(GenomeAssembly genomeAssembly, String contig, AllelePosition allelePosition, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();
        String geneSymbol = buildGeneSymbol(highestImpactAnnotation);
        String geneId = buildGeneId(highestImpactAnnotation);

        //Jannovar presently ignores all structural variants, so flag it here. Not that we do anything with them at present.
        VariantEffect highestImpactEffect = allelePosition.isSymbolic() ? VariantEffect.STRUCTURAL_VARIANT : variantAnnotations.getHighestImpactEffect();
        List<TranscriptAnnotation> annotations = buildTranscriptAnnotations(variantAnnotations.getAnnotations());

        int start = allelePosition.getStart();
        String ref = allelePosition.getRef();
        String alt = allelePosition.getAlt();
        int end = start + Math.max(ref.length() - 1, 0);

        VariantEffect variantEffect = checkRegulatoryRegionVariantEffect(highestImpactEffect, chr, start);
        return VariantAnnotation.builder()
                .genomeAssembly(genomeAssembly)
                .chromosome(chr)
                .chromosomeName(getChromosomeNameOrDefault(genomeVariant.getChrName(), contig))
                .start(start)
                .end(end)
                .ref(ref)
                .alt(alt)
                .geneId(geneId)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .annotations(annotations)
                .build();
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
                .variantEffect(getVariantEffectOrDefault(annotation.getMostPathogenicVarType(), VariantEffect.SEQUENCE_VARIANT))
                .accession(getTranscriptAccession(annotation.getTranscript()))
                .geneSymbol(buildGeneSymbol(annotation))
                .hgvsGenomic((annotation.getGenomicNTChange() == null) ? "" : annotation.getGenomicNTChangeStr())
                .hgvsCdna(annotation.getCDSNTChangeStr())
                .hgvsProtein(annotation.getProteinChangeStr(AminoAcidCode.THREE_LETTER))
                .distanceFromNearestGene(getDistFromNearestGene(annotation))
                .build();
    }

    private VariantEffect getVariantEffectOrDefault(VariantEffect annotatedEffect, VariantEffect defaultEffect) {
        return annotatedEffect == null ? defaultEffect : annotatedEffect;
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
                .accession(getTranscriptAccession(svAnnotation.getTranscript()))
                .geneSymbol(buildGeneSymbol(svAnnotation))
                .build();
    }

    private String getTranscriptAccession(TranscriptModel transcriptModel) {
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

    private String buildGeneId(Annotation annotation) {
        return annotation == null ? "" : buildTranscriptGeneId(annotation.getTranscript());
    }

    private String buildGeneId(SVAnnotation svAnnotation) {
        return svAnnotation == null ? "" : buildTranscriptGeneId(svAnnotation.getTranscript());
    }

    private String buildTranscriptGeneId(TranscriptModel transcriptModel) {
        if (transcriptModel == null || transcriptModel.getGeneID() == null) {
            return "";
        }
        //this will now return the id from the user-specified data source. Previously would only return the Entrez id.
        return transcriptModel.getGeneID();
    }

    private String buildGeneSymbol(Annotation annotation) {
        return annotation == null ? "." : buildTranscriptGeneSymbol(annotation.getTranscript());
    }

    private String buildGeneSymbol(SVAnnotation svAnnotation) {
        return svAnnotation == null ? "." : buildTranscriptGeneSymbol(svAnnotation.getTranscript());
    }

    private String buildTranscriptGeneSymbol(TranscriptModel transcriptModel) {
        if (transcriptModel == null || transcriptModel.getGeneSymbol() == null) {
            return ".";
        } else {
            return transcriptModel.getGeneSymbol();
        }
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
