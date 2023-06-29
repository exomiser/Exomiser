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

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.svart.GenomicVariant;
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

    private final GenomeAssembly genomeAssembly;
    private final JannovarSmallVariantAnnotator smallVariantAnnotator;
    private final JannovarStructuralVariantAnnotator structuralVariantAnnotator;

    public JannovarVariantAnnotator(GenomeAssembly genomeAssembly, JannovarData jannovarData, ChromosomalRegionIndex<RegulatoryFeature> regulatoryRegionIndex) {
        this.genomeAssembly = genomeAssembly;
        this.smallVariantAnnotator = new JannovarSmallVariantAnnotator(genomeAssembly, jannovarData, regulatoryRegionIndex);
        this.structuralVariantAnnotator = new JannovarStructuralVariantAnnotator(genomeAssembly, jannovarData, regulatoryRegionIndex);
    }

    @Override
    public GenomeAssembly genomeAssembly() {
        return genomeAssembly;
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
     * @param variant
     * @return {@link VariantAnnotation} objects annotated using Jannovar.
     * @since 13.0.0
     */
    @Override
    public List<VariantAnnotation> annotate(GenomicVariant variant) {
        if (variant == null) {
            return List.of();
        }
        // Jannovar has a 1Kb cut-off for precise variant where it will simply assign them as 'STRUCTURAL_VARIANT'.
        // Here we're going to hack Jannovar and send any of these types to the structural variant annotator.
        return (variant.isSymbolic() || Math.abs(variant.changeLength()) >= 1000) ? structuralVariantAnnotator.annotate(variant) : smallVariantAnnotator.annotate(variant);
    }
}
