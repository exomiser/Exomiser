/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.proto.JannovarProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

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
        Set<TranscriptModel> uniqueTranscriptModels = new HashSet<>(transcriptModelsByAccession.values());
        logger.debug("Converting transcript models...");
        Set<JannovarProto.TranscriptModel> protoTranscriptModels = uniqueTranscriptModels
                .parallelStream()
                .map(toProtoTranscriptModel())
                .collect(toSet());
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
                .setAccession(transcriptModel.getAccession())
                .setGeneSymbol(transcriptModel.getGeneSymbol())
                .setGeneID(transcriptModel.getGeneID())
                .putAllAltGeneIds(transcriptModel.getAltGeneIDs())
                .setTranscriptSupportLevel(transcriptModel.getTranscriptSupportLevel())
                .setSequence(transcriptModel.getSequence())
                .setCdsRegion(toProtoGenomeInterval(transcriptModel.getCDSRegion()))
                .setTxRegion(toProtoGenomeInterval(transcriptModel.getTXRegion()))
                .addAllExonRegions(toProtoExonRegions(transcriptModel.getExonRegions()))
                .build();
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

    private static Function<JannovarProto.TranscriptModel, TranscriptModel> toTranscriptModel(ReferenceDictionary referenceDictionary) {
        return protoTranscriptModel -> new TranscriptModel(
                protoTranscriptModel.getAccession(),
                protoTranscriptModel.getGeneSymbol(),
                toGenomeInterval(referenceDictionary, protoTranscriptModel.getTxRegion()),
                toGenomeInterval(referenceDictionary, protoTranscriptModel.getCdsRegion()),
                toExonRegions(referenceDictionary, protoTranscriptModel.getExonRegionsList()),
                protoTranscriptModel.getSequence(),
                protoTranscriptModel.getGeneID(),
                protoTranscriptModel.getTranscriptSupportLevel(),
                protoTranscriptModel.getAltGeneIdsMap()
        );
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
}
