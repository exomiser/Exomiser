/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneTranscriptModelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GeneTranscriptModelBuilder.class);

    private static final ReferenceDictionary REF_DICT = HG19RefDictBuilder.build();

    private final TranscriptModelBuilder builder;
    private final int chr;

    public GeneTranscriptModelBuilder(String geneSymbol, String geneId, String accession, int chr, Strand strand, String mRnaTranscriptSequence) {
        builder = new TranscriptModelBuilder();
        builder.setGeneSymbol(geneSymbol);
        builder.setGeneID(geneId);
        builder.setStrand(strand);
        builder.setAccession(accession);
        builder.setSequence(mRnaTranscriptSequence.toUpperCase());

        this.chr = chr;
    }

    public GeneTranscriptModelBuilder buildTxRegion(int start, int end) {
        GenomeInterval tx = getZeroBasedFwdStrandInterval(chr, start, end);
        builder.setTXRegion(tx);
        return this;
    }

    public GeneTranscriptModelBuilder buildCdsRegion(int start, int end) {
        GenomeInterval cds = getZeroBasedFwdStrandInterval(chr, start, end);
        builder.setCDSRegion(cds);
        return this;
    }

    public GeneTranscriptModelBuilder addExon(int start, int end) {
        GenomeInterval exonRegion = getZeroBasedFwdStrandInterval(chr, start, end);
        builder.addExonRegion(exonRegion);
        return this;
    }

    public GeneTranscriptModelBuilder addAltGeneIds(Map<String, String> altGeneIds) {
        builder.getAltGeneIDs().putAll(altGeneIds);
        return this;
    }

    private GenomeInterval getZeroBasedFwdStrandInterval(int chr, int start, int end) {
        return new GenomeInterval(REF_DICT, Strand.FWD, chr, start, end, PositionType.ZERO_BASED);
    }

    public TranscriptModel build() {
        if (builder.getTXRegion() == null) {
            buildTxRegion(1, 1);
        }

        if (builder.getCDSRegion() == null) {
            buildCdsRegion(1, 1);
        }

        if (builder.getExonRegions().isEmpty()) {
            addExon(1, 1);
        }
        final int txStart = builder.getTXRegion().getBeginPos();
        final int txEnd = builder.getTXRegion().getEndPos();
        final int cdsStart = builder.getCDSRegion().getBeginPos();
        final int cdsEnd = builder.getCDSRegion().getEndPos();
        logger.info("Built TranscriptModel Gene={} accession={} Chr{} Strand={} seqLen={} txRegion={}-{}({} bases) CDS={}-{}({} bases) {} exons={}", builder.getGeneSymbol(), builder.getAccession(), builder.getCDSRegion().getChr(),
                builder.getStrand(), builder.getSequence().length(), txStart, txEnd, txEnd - txStart,
                cdsStart, cdsEnd, cdsEnd - cdsStart,
                builder.getExonRegions().size(), builder.getExonRegions().stream().map(exon -> String.format("[%d-%d] ", exon.getBeginPos(), exon.getEndPos())).reduce("", (a, b) -> a + b));

        return builder.build();
    }
}
