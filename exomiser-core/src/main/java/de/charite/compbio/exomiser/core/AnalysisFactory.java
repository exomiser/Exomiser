/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.AnalysisRunner.AnalysisMode;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level factory for creating an Analysis and AnalysisRunner. This is
 * pretty much all that's needed to run an analysis.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisFactory {

    private final SampleDataFactory sampleDataFactory;
    private final VariantDataService variantDataService;
    private final PriorityFactory priorityFactory;

    public AnalysisFactory(SampleDataFactory sampleDataFactory, VariantDataService variantDataService, PriorityFactory priorityFactory) {
        this.sampleDataFactory = sampleDataFactory;
        this.variantDataService = variantDataService;
        this.priorityFactory = priorityFactory;
    }

    public AnalysisRunner getFullAnalysisRunner() {
        return new AnalysisRunner(variantDataService, AnalysisRunner.AnalysisMode.FULL);
    }

    public AnalysisRunner getPassOnlyAnalysisRunner() {
        return new AnalysisRunner(variantDataService, AnalysisRunner.AnalysisMode.PASS_ONLY);
    }

    public AnalysisBuilder getAnalysisBuilder() {
        return new AnalysisBuilder(priorityFactory);
    }

    public static class AnalysisBuilder {

        private final PriorityFactory priorityFactory;

        private List<String> hpoIds = new ArrayList<>();
        private SampleData sampleData;
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        private ScoringMode scoringMode = ScoringMode.RAW_SCORE;
        private AnalysisMode analysisMode = AnalysisMode.PASS_ONLY;
        private List<AnalysisStep> analysisSteps = new ArrayList<>();

        private AnalysisBuilder(PriorityFactory priorityFactory) {
            this.priorityFactory = priorityFactory;
        }

        public Analysis build() {
            Analysis analysis = new Analysis();
//            analysis.setHpoIds(hpoIds);
            analysis.addAllSteps(analysisSteps);
            return analysis;
        }

        public AnalysisBuilder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            this.modeOfInheritance = modeOfInheritance;
            return this;
        }

        public AnalysisBuilder scoringMode(ScoringMode scoreMode) {
            this.scoringMode = scoreMode;
            return this;
        }

        public AnalysisBuilder analysisMode(AnalysisMode analysisMode) {
            this.analysisMode = analysisMode;
            return this;
        }

        public AnalysisBuilder hpoIds(List<String> hpoIds) {
            this.hpoIds = hpoIds;
            return this;
        }
        
        public AnalysisBuilder addStep(AnalysisStep step) {
            this.analysisSteps.add(step);
            return this;
        }
        
        public AnalysisBuilder addOmimPrioritiser() {
            analysisSteps.add(priorityFactory.makeOmimPrioritiser());
            return this;
        }

        public AnalysisBuilder addPhivePrioritiser() {
            analysisSteps.add(priorityFactory.makePhivePrioritiser(hpoIds));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser() {
            analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions()));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
            analysisSteps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, hiPhiveOptions));
            return this;
        }

        public AnalysisBuilder addPhenixPrioritiser() {
            analysisSteps.add(priorityFactory.makePhenixPrioritiser(hpoIds));
            return this;
        }

        public AnalysisBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes) {
            analysisSteps.add(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));
            return this;
        }

        public AnalysisBuilder addAnalysisStep(AnalysisStep analysisStep) {
            analysisSteps.add(analysisStep);
            return this;
        }
    }

}
