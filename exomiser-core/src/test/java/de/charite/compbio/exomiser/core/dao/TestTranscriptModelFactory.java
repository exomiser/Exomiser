package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import static de.charite.compbio.jannovar.reference.Strand.FWD;
import static de.charite.compbio.jannovar.reference.Strand.REV;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;

/**
 * Allows the easy creation of transcript models from knownGenes.txt.gz lines.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class TestTranscriptModelFactory {

    /**
     * Helper function to parse a knownGenes.txt.gz line into a TranscriptModel.
     *
     * @param refDict reference dictionary
     * @param s The knownGeneList line to parse.
     */
    public static TranscriptModelBuilder parseKnownGenesLine(ReferenceDictionary refDict, String s) {
        String[] fields = s.split("\t");
        TranscriptModelBuilder result = new TranscriptModelBuilder();
        result.setAccession(fields[0]);

        int chr = refDict.contigID.get(fields[1].substring(3));

        result.setStrand(valueOf(fields[2].charAt(0)));
        GenomeInterval txRegion = new GenomeInterval(refDict, Strand.FWD, chr, Integer.parseInt(fields[3]) + 1,
                Integer.parseInt(fields[4]), PositionType.ONE_BASED);
        result.setTxRegion(txRegion);
        GenomeInterval cdsRegion = new GenomeInterval(refDict, Strand.FWD, chr, Integer.parseInt(fields[5]) + 1,
                Integer.parseInt(fields[6]), PositionType.ONE_BASED);
        result.setCdsRegion(cdsRegion);

        int exonCount = Integer.parseInt(fields[7]);
        String[] startFields = fields[8].split(",");
        String[] endFields = fields[9].split(",");
        for (int i = 0; i < exonCount; ++i) {
            GenomeInterval exonRegion = new GenomeInterval(refDict, Strand.FWD, chr, Integer.parseInt(startFields[i]) + 1,
                    Integer.parseInt(endFields[i]), PositionType.ONE_BASED);
            result.addExonRegion(exonRegion);
        }

        return result;
    }

    private static Strand valueOf(char strand) {
        return (strand == '-') ? REV : FWD;
    }
}
