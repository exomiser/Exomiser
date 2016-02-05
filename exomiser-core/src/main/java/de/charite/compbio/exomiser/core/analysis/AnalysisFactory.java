/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private final SampleDataFactory sampleDataFactory;
    private final PriorityFactory priorityFactory;
    private final VariantDataService variantDataService;

    @Autowired
    public AnalysisFactory(SampleDataFactory sampleDataFactory, PriorityFactory priorityFactory, VariantDataService variantDataService) {
        this.sampleDataFactory = sampleDataFactory;
        this.variantDataService = variantDataService;
        this.priorityFactory = priorityFactory;
    }

    public AnalysisRunner getAnalysisRunnerForMode(AnalysisMode analysisMode) {
        switch (analysisMode) {
            case FULL:
                return new SimpleAnalysisRunner(sampleDataFactory, variantDataService);
            case SPARSE:
                return new SparseAnalysisRunner(sampleDataFactory, variantDataService);
            case PASS_ONLY:
                return passOnlyAnalysisRunner();
            default:
                //this guy takes up the least RAM
                return passOnlyAnalysisRunner();
        }
    }
    
    private AnalysisRunner passOnlyAnalysisRunner() {
        return new PassOnlyAnalysisRunner(sampleDataFactory, variantDataService);
    }

    public AnalysisBuilder getAnalysisBuilder(Path vcfPath) {
        return new AnalysisBuilder(priorityFactory, vcfPath);
    }

    public static class AnalysisBuilder {

        private final PriorityFactory priorityFactory;
        private final Analysis analysis;

        private final List<AnalysisStep> analysisSteps = new ArrayList<>();

        private AnalysisBuilder(PriorityFactory priorityFactory, Path vcfPath) {
            this.priorityFactory = priorityFactory;
            analysis = new Analysis(vcfPath);
        }

        public Analysis build() {
            analysis.addAllSteps(analysisSteps);
            return analysis;
        }

        public AnalysisBuilder pedPath(Path pedPath) {
            analysis.setPedPath(pedPath);
            return this;
        }

        public AnalysisBuilder hpoIds(List<String> hpoIds) {
            analysis.setHpoIds(hpoIds);
            return this;
        }

        public AnalysisBuilder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            analysis.setModeOfInheritance(modeOfInheritance);
            return this;
        }

        public AnalysisBuilder scoringMode(ScoringMode scoreMode) {
            analysis.setScoringMode(scoreMode);
            return this;
        }

        public AnalysisBuilder analysisMode(AnalysisMode analysisMode) {
            analysis.setAnalysisMode(analysisMode);
            return this;
        }

        public AnalysisBuilder frequencySources(Set<FrequencySource> frequencySources) {
            analysis.setFrequencySources(frequencySources);
            return this;
        }

        public AnalysisBuilder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources) {
            analysis.setPathogenicitySources(pathogenicitySources);
            return this;
        }

        public AnalysisBuilder addOmimPrioritiser() {
            analysis.addStep(priorityFactory.makeOmimPrioritiser());
            return this;
        }

        public AnalysisBuilder addPhivePrioritiser(List<String> hpoIds) {
            analysis.addStep(priorityFactory.makePhivePrioritiser(hpoIds));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser(List<String> hpoIds) {
            analysis.addStep(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions()));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions) {
            analysis.addStep(priorityFactory.makeHiPhivePrioritiser(hpoIds, hiPhiveOptions));
            return this;
        }

        public AnalysisBuilder addPhenixPrioritiser(List<String> hpoIds) {
            analysis.addStep(priorityFactory.makePhenixPrioritiser(hpoIds));
            return this;
        }

        public AnalysisBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes) {
            analysis.addStep(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));
            return this;
        }

        public AnalysisBuilder addAnalysisStep(AnalysisStep analysisStep) {
            analysis.addStep(analysisStep);
            return this;
        }
    }

}
