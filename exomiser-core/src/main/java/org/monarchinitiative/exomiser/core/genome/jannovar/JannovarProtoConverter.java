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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.exomiser.core.proto.JannovarProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Ser/de-serialiser for JannovarData to/from protobuf.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarProtoConverter {

    private static final Logger logger = LoggerFactory.getLogger(JannovarProtoConverter.class);

    private JannovarProtoConverter() {
        //uninstantiable utility class
    }

    public static JannovarProto.JannovarData toJannovarProto(JannovarData jannovarData) {
        logger.debug("Converting jannovar data...");
        ReferenceDictionary referenceDictionary = jannovarData.getRefDict();
        JannovarProto.ReferenceDictionary protoReferenceDictionary = toProtoReferenceDictionary(referenceDictionary);
        logger.debug("Converted referenceDictionary with {} contigs", protoReferenceDictionary.getContigNameToIdCount());

        Map<String, TranscriptModel> transcriptModelsByAccession = jannovarData.getTmByAccession();
        Set<TranscriptModel> uniqueTranscriptModels = new TreeSet<>(transcriptModelsByAccession.values());
        logger.debug("Converting {} transcript models...", uniqueTranscriptModels.size());
        // sorting and preserving the order leads to much smaller files (~30% of original size) with identical sizes each run.
        Set<JannovarProto.TranscriptModel> protoTranscriptModels = uniqueTranscriptModels
                .parallelStream()
                .sorted()
                .map(toProtoTranscriptModel())
                .collect(ImmutableSet.toImmutableSet());
        logger.debug("Added {} transcript models", protoTranscriptModels.size());

        return JannovarProto.JannovarData.newBuilder()
                .setReferenceDictionary(protoReferenceDictionary)
                .addAllTranscriptModels(protoTranscriptModels)
                .build();
    }

    private static JannovarProto.ReferenceDictionary toProtoReferenceDictionary(ReferenceDictionary referenceDictionary) {
        return JannovarProto.ReferenceDictionary.newBuilder()
                .putAllContigNameToId(referenceDictionary.getContigNameToID())
                .putAllContigIdToLength(referenceDictionary.getContigIDToLength())
                .putAllContigIdToName(referenceDictionary.getContigIDToName())
                .build();
    }

    private static Function<TranscriptModel, JannovarProto.TranscriptModel> toProtoTranscriptModel() {
        return transcriptModel -> JannovarProto.TranscriptModel.newBuilder()
                .setAccession(trimDuplicatedEnsemblVersion(transcriptModel.getAccession()))
                .setGeneSymbol(transcriptModel.getGeneSymbol())
                .setGeneID((transcriptModel.getGeneID() == null || transcriptModel.getGeneID()
                        .equals(".")) ? "" : transcriptModel.getGeneID())
                .setTxRegion(toProtoGenomeInterval(transcriptModel.getTXRegion()))
                .setCdsRegion(toProtoGenomeInterval(transcriptModel.getCDSRegion()))
                .addAllExonRegions(toProtoExonRegions(transcriptModel.getExonRegions()))
                .setSequence(transcriptModel.getSequence())
                .setTranscriptSupportLevel(transcriptModel.getTranscriptSupportLevel())
                .setHasSubstitutions(transcriptModel.isHasSubstitutions())
                .setHasIndels(transcriptModel.isHasIndels())
                .putAllAltGeneIds(replaceDotGeneIdWithEmpty(transcriptModel.getAltGeneIDs()))
                .setAlignment(toProtoSeqAlignment(transcriptModel.getSeqAlignment()))
                .build();
    }

    private static final Pattern ENST_REPEAT_VERSION = Pattern.compile("ENST\\d{11}\\.\\d+\\.\\d+");

    //TODO: REMOVE THIS for jannovar version 0.33!
    // fix bug in Jannovar 0.29 where the transcript version is duplicated
    static String trimDuplicatedEnsemblVersion(String transcriptAccession) {
        if (ENST_REPEAT_VERSION.matcher(transcriptAccession).matches()) {
            int lastDot = transcriptAccession.lastIndexOf('.');
            String trimmed = transcriptAccession.substring(0, lastDot);
            logger.debug("{} -> {}", transcriptAccession, trimmed);
            return trimmed;
        }
        return transcriptAccession;
    }

    private static Map<String, String> replaceDotGeneIdWithEmpty(Map<String, String> altGeneIds) {
        String entrezId = altGeneIds.getOrDefault("ENTREZ_ID", "");
        if (entrezId.equals(".")) {
            LinkedHashMap<String, String> altGeneIdsCopy = new LinkedHashMap<>(altGeneIds);
            altGeneIdsCopy.replace("ENTREZ_ID", ".", "");
            return altGeneIdsCopy;
        }
        return altGeneIds;
    }

    private static JannovarProto.GenomeInterval toProtoGenomeInterval(GenomeInterval genomeInterval) {
        return JannovarProto.GenomeInterval.newBuilder()
                .setChr(genomeInterval.getChr())
                .setStrand((genomeInterval.getStrand() == Strand.FWD) ? JannovarProto.Strand.FWD : JannovarProto.Strand.REV)
                .setBeginPos(genomeInterval.getBeginPos())
                .setEndPos(genomeInterval.getEndPos())
                .build();
    }

    private static List<JannovarProto.GenomeInterval> toProtoExonRegions(List<GenomeInterval> genomeIntervals) {
        List<JannovarProto.GenomeInterval> intervals = new ArrayList<>();
        genomeIntervals.forEach(interval -> intervals.add(toProtoGenomeInterval(interval)));
        return intervals;
    }

    private static JannovarProto.Alignment toProtoSeqAlignment(Alignment alignment) {
        return JannovarProto.Alignment.newBuilder()
                .addAllRefAnchors(toProtoAnchors(alignment.getRefAnchors()))
                .addAllQryAnchors(toProtoAnchors(alignment.getQryAnchors()))
                .build();
    }

    private static List<JannovarProto.Anchor> toProtoAnchors(List<Anchor> anchors) {
        List<JannovarProto.Anchor> protoAnchors = new ArrayList<>();
        anchors.forEach(anchor -> protoAnchors.add(JannovarProto.Anchor.newBuilder()
                .setGapPos(anchor.getGapPos())
                .setSeqPos(anchor.getSeqPos())
                .build()));
        return protoAnchors;
    }

    public static JannovarData toJannovarData(JannovarProto.JannovarData protoJannovarData) {
        logger.debug("Converting to jannovar data...");
        ReferenceDictionary referenceDictionary = toReferenceDictionary(protoJannovarData.getReferenceDictionary());
        ImmutableList<TranscriptModel> transcriptModels = protoJannovarData.getTranscriptModelsList()
                .parallelStream()
                .map(toTranscriptModel(referenceDictionary))
                .collect(ImmutableList.toImmutableList());
        logger.debug("Done");
        return new JannovarData(referenceDictionary, transcriptModels);
    }

    private static ReferenceDictionary toReferenceDictionary(JannovarProto.ReferenceDictionary protoRefDict) {
        ReferenceDictionaryBuilder referenceDictionaryBuilder = new ReferenceDictionaryBuilder();
        protoRefDict.getContigNameToIdMap().forEach(referenceDictionaryBuilder::putContigID);
        protoRefDict.getContigIdToNameMap().forEach(referenceDictionaryBuilder::putContigName);
        protoRefDict.getContigIdToLengthMap().forEach(referenceDictionaryBuilder::putContigLength);
        return referenceDictionaryBuilder.build();
    }

    public static Function<JannovarProto.TranscriptModel, TranscriptModel> toTranscriptModel(ReferenceDictionary referenceDictionary) {
        return protoTranscriptModel ->
                new TranscriptModel(
                        protoTranscriptModel.getAccession(),
                        protoTranscriptModel.getGeneSymbol(),
                        toGenomeInterval(referenceDictionary, protoTranscriptModel.getTxRegion()),
                        toGenomeInterval(referenceDictionary, protoTranscriptModel.getCdsRegion()),
                        toExonRegions(referenceDictionary, protoTranscriptModel.getExonRegionsList()),
                        protoTranscriptModel.getSequence(),
                        protoTranscriptModel.getGeneID(),
                        protoTranscriptModel.getTranscriptSupportLevel(),
                        protoTranscriptModel.getHasSubstitutions(),
                        protoTranscriptModel.getHasIndels(),
                        protoTranscriptModel.getAltGeneIdsMap(),
                        protoTranscriptModel.hasAlignment() ? toAlignment(protoTranscriptModel.getAlignment()) : buildUngappedAlignment(protoTranscriptModel.getExonRegionsList())
                );
    }

    // Builds an alignment if there was one missing. This will be the case for data created before the 2302 data release.
    // For Ensembl/MANE data there will be no difference. RefSeq will be affected, but this should be no different to the
    // <= 2302 data.
    private static Alignment buildUngappedAlignment(List<JannovarProto.GenomeInterval> exonRegions) {
        // Jannovar TranscriptModelBuilder uses the sum of the exon lengths in all the GFF parser code. Only the TranscriptModel
        // alt constructor uses the sequence length when calling Alignment.createUngappedAlignment
        int exonsLength = exonRegions.stream().mapToInt(exon -> exon.getEndPos() - exon.getBeginPos()).sum();
        // Jannovar uses ImmutableList internally
        List<Anchor> anchors = ImmutableList.of(new Anchor(0, 0), new Anchor(exonsLength, exonsLength));
        return new Alignment(anchors, anchors);
    }

    private static GenomeInterval toGenomeInterval(ReferenceDictionary refDict, JannovarProto.GenomeInterval protoGenomeInterval) {
        return new GenomeInterval(
                refDict,
                (protoGenomeInterval.getStrand() == JannovarProto.Strand.FWD) ? Strand.FWD : Strand.REV,
                protoGenomeInterval.getChr(),
                protoGenomeInterval.getBeginPos(),
                protoGenomeInterval.getEndPos()
        );
    }

    private static ImmutableList<GenomeInterval> toExonRegions(ReferenceDictionary refDict, List<JannovarProto.GenomeInterval> genomeIntervals) {
        ImmutableList.Builder<GenomeInterval> intervals = ImmutableList.builder();
        genomeIntervals.forEach(interval -> intervals.add(toGenomeInterval(refDict, interval)));
        return intervals.build();
    }

    private static Alignment toAlignment(JannovarProto.Alignment protoAlignment) {
        ImmutableList<Anchor> refAnchors = toAnchors(protoAlignment.getRefAnchorsList());
        ImmutableList<Anchor> qryAnchors = toAnchors(protoAlignment.getQryAnchorsList());
        return new Alignment(refAnchors, qryAnchors);
    }

    private static ImmutableList<Anchor> toAnchors(List<JannovarProto.Anchor> protoAnchors) {
        ImmutableList.Builder<Anchor> anchors = ImmutableList.builder();
        protoAnchors.forEach(anchor -> anchors.add(new Anchor(anchor.getGapPos(), anchor.getSeqPos())));
        return anchors.build();
    }
}
