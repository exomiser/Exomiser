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

package org.monarchinitiative.exomiser.core.analysis;

import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.VariantFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * High-level factory for creating an {@link Analysis} and {@link AnalysisRunner}. This is
 * pretty much all that's needed to run an analysis with.
 * 
 * @see Exomiser
 * @see AnalysisMode
 * 
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class AnalysisFactory {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisFactory.class);

    private final VariantFactory variantFactory;
    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;

    private final PriorityFactory priorityFactory;

    @Autowired
    public AnalysisFactory(GenomeAnalysisServiceProvider genomeAnalysisServiceProvider, VariantFactory variantFactory, PriorityFactory priorityFactory) {
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.variantFactory = variantFactory;
        this.priorityFactory = priorityFactory;
    }

    public AnalysisRunner getAnalysisRunner(GenomeAssembly genomeAssembly, AnalysisMode analysisMode) {
        //This class primarily exists as an external interface for the Exomiser class to be able to create and run analyses
        //without having to expose too much of the Analysis package implementation. e.g. the AnalysisRunner implementations
        // below are package-private.
        GenomeAnalysisService genomeAnalysisService = genomeAnalysisServiceProvider.get(genomeAssembly);

        switch (analysisMode) {
            case FULL:
                return new SimpleAnalysisRunner(variantFactory, genomeAnalysisService);
            case SPARSE:
                return new SparseAnalysisRunner(variantFactory, genomeAnalysisService);
            case PASS_ONLY:
            default:
                //this guy takes up the least RAM
                return new PassOnlyAnalysisRunner(variantFactory, genomeAnalysisService);
        }
    }

    public AnalysisBuilder getAnalysisBuilder() {
        return new AnalysisBuilder(priorityFactory, genomeAnalysisServiceProvider);
    }

}
