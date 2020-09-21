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

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.exomiser.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles creation of {@link VariantAnnotation} using Jannovar.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarVariantAnnotator implements VariantAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(JannovarVariantAnnotator.class);

    private final SmallVariantAnnotator smallVariantAnnotator;
    private final StructuralVariantAnnotator structuralVariantAnnotator;

    public JannovarVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.smallVariantAnnotator = new JannovarSmallVariantAnnotator(genomeAssembly, jannovarData, regulatoryRegionIndex);
        this.structuralVariantAnnotator = new JannovarStructuralVariantAnnotator(genomeAssembly, jannovarData, regulatoryRegionIndex);
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
     * @param contig chromosome identifier
     * @param start 1-based start position of the first base of the ref string
     * @param ref reference base(s)
     * @param alt alternate bases
     * @return {@link VariantAnnotation} objects trimmed according to {@link AllelePosition#trim(int, String, String)} and annotated using Jannovar.
     * @since 13.0.0
     */
    @Override
    public List<VariantAnnotation> annotate(String contig, int start, String ref, String alt) {
        return smallVariantAnnotator.annotate(contig, start, ref, alt);
    }

    /**
     * Returns {@link VariantAnnotation}s which overlap the genomic region covered by input parameters. These are assumed
     * to have been derived from a VCF file.
     *
     * @param startContig start chromosome identifier
     * @param startPos    1-based position of the first base of the ref string
     * @param ref         reference base
     * @param alt         alternate base (should be symbolic)
     * @param variantType the structural type of the variant
     * @param length      length of the variant
     * @param ciStart     confidence intervals for the start position
     * @param endContig   end chromosome identifier
     * @param endPos      1-based position for the end of the alt string
     * @param ciEnd       confidence intervals for the end position
     * @return {@link VariantAnnotation} objects for the variant annotated with Jannovar
     * @since 13.0.0
     */
    @Override
    public List<VariantAnnotation> annotate(String startContig, int startPos, String ref, String alt, VariantType variantType, int length, ConfidenceInterval ciStart, String endContig, int endPos, ConfidenceInterval ciEnd) {
        return structuralVariantAnnotator.annotate(startContig, startPos, ref, alt, variantType, length, ciStart, endContig, endPos, ciEnd);
    }
}
