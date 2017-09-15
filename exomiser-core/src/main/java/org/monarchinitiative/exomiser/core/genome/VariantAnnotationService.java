/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.VariantCoordinates;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface VariantAnnotationService {

    //TODO: change name to ChromosomalVariant
    public VariantEvaluation annotate(VariantCoordinates variantCoordinates);

    public Stream<VariantEvaluation> annotate(VariantContext variantContext);

    public Stream<VariantEvaluation> annotate(Stream<VariantContext> variantContextStream);

}
