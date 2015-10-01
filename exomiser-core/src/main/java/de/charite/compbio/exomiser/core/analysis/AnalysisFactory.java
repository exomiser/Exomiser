/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
public class AnalysisFactory {

    private final SampleDataFactory sampleDataFactory;
    private final PriorityFactory priorityFactory;
    private final VariantDataService variantDataService;

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

    public AnalysisBuilder getAnalysisBuilder(Path vcfPath, List<String> hpoIds) {
        return new AnalysisBuilder(priorityFactory, vcfPath, hpoIds);
    }

    public static class AnalysisBuilder {

        private final PriorityFactory priorityFactory;
        private final Analysis analysis;

        private List<String> hpoIds = new ArrayList<>();
        private final List<AnalysisStep> analysisSteps = new ArrayList<>();

        private AnalysisBuilder(PriorityFactory priorityFactory, Path vcfPath, List<String> hpoIds) {
            this.priorityFactory = priorityFactory;
            this.hpoIds = hpoIds;
            analysis = new Analysis(vcfPath);
            analysis.setHpoIds(hpoIds);
        }

        public Analysis build() {
            analysis.setFrequencySources(null);
            analysis.setPathogenicitySources(null);
            analysis.addAllSteps(analysisSteps);
            return analysis;
        }

        public AnalysisBuilder pedPath(Path pedPath) {
            analysis.setPedPath(pedPath);
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

        public AnalysisBuilder hpoIds(List<String> hpoIds) {
            analysis.setHpoIds(hpoIds);
            return this;
        }
        
        public AnalysisBuilder addOmimPrioritiser() {
            analysis.addStep(priorityFactory.makeOmimPrioritiser());
            return this;
        }

        public AnalysisBuilder addPhivePrioritiser() {
            analysis.addStep(priorityFactory.makePhivePrioritiser(hpoIds));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser() {
            analysis.addStep(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions()));
            return this;
        }

        public AnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
            analysis.addStep(priorityFactory.makeHiPhivePrioritiser(hpoIds, hiPhiveOptions));
            return this;
        }

        public AnalysisBuilder addPhenixPrioritiser() {
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
