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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;

/**
 * Adaptor around an {@link Analysis} to extract and validate the {@link Sample} data. This is package private and ought
 * to disappear in the future.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class SampleAnalysisUtil {

    public static Sample extractSample(Analysis analysis) {
        Pedigree pedigree = analysis.getPedigree();
        String probandSampleName = analysis.getProbandSampleName();
        Sex sex = extractProbandSex(probandSampleName, pedigree);

        return Sample.builder()
                .genomeAssembly(analysis.getGenomeAssembly())
                .vcfPath(analysis.getVcfPath())
                .hpoIds(analysis.getHpoIds())
                .probandSampleName(analysis.getProbandSampleName())
                .age(Age.unknown())
                .sex(sex)
                .pedigree(pedigree)
                .build();
    }

    private static Sex extractProbandSex(String probandSampleName, Pedigree pedigree) {
        if (pedigree.isEmpty()) {
            return Pedigree.Individual.Sex.UNKNOWN;
        }
        Pedigree.Individual proband = pedigree.getIndividualById(probandSampleName);
        if (proband == null) {
            throw new IllegalArgumentException("Proband '" + probandSampleName + "' not present in pedigree");
        }
        return proband.getSex();
    }
}
