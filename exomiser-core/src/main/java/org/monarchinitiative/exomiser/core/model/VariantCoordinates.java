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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantCoordinates extends ChromosomalRegion {

    GenomeAssembly getGenomeAssembly();

    /**
     * @return String representation of the chromosome. Chromosomes 1-22 will return
     * a string value of their number. Sex chromosomes 23=X 24=Y and mitochondrial 25=MT.
     */
    // TODO: Could use the Chromosome/Contig class...
    String getChromosomeName();

    String getEndChromosomeName();

    /**
     * @return String with the reference allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getRef();

    /**
     * @return String with the alternative allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getAlt();

    default boolean isStructuralVariant() {
        return getVariantType().isStructural();
    }

    VariantType getVariantType();
}
