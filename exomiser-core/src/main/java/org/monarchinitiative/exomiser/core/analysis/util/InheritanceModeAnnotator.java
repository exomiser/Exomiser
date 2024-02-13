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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.mendel.*;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Native wrapper for the Jannovar {@link MendelianInheritanceChecker} rather than relying on the Jannovar HTSJDK bridge.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class InheritanceModeAnnotator {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeAnnotator.class);

    private final Pedigree pedigree;
    private final InheritanceModeOptions inheritanceModeOptions;

    private final MendelianInheritanceChecker mendelChecker;

    public InheritanceModeAnnotator(Pedigree pedigree, InheritanceModeOptions inheritanceModeOptions) {
        Objects.requireNonNull(pedigree);
        if (pedigree.isEmpty()){
            throw new IllegalArgumentException("pedigree cannot be empty - at least one named, affected individual must be present");
        }
        this.pedigree = pedigree;
        Objects.requireNonNull(inheritanceModeOptions);
        this.inheritanceModeOptions = inheritanceModeOptions;
        this.mendelChecker = new MendelianInheritanceChecker(PedigreeConverter.convertToJannovarPedigree(pedigree));
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public InheritanceModeOptions getInheritanceModeOptions() {
        return inheritanceModeOptions;
    }

    public Set<ModeOfInheritance> getDefinedModes() {
        return inheritanceModeOptions.getDefinedModes();
    }

    /**
     * This method will check the supplied list of {@link VariantEvaluation} against the pedigree supplied in the class
     * constructor to determine the modes of inheritance with which the variants are compatible. It does not alter the
     * input objects. It is expected that the input variants have been pre-filtered, failure to do so will result in
     * overly-long computation time and potentially incorrect analyses.
     *
     * @param variantEvaluations a pre-filtered list of variants
     * @return a map of inheritance modes and the variants which are compatible with them
     */
    public Map<ModeOfInheritance, List<VariantEvaluation>> computeCompatibleInheritanceModes(List<VariantEvaluation> variantEvaluations) {
        List<GenotypeCalls> genotypeCalls = buildGenotypeCalls(variantEvaluations);
        try {
            Map<ModeOfInheritance, ImmutableList<GenotypeCalls>> compatibilityCalls = mendelChecker.checkMendelianInheritance(genotypeCalls);
            logger.debug("{}", compatibilityCalls);
            return variantsGroupedByCompatibleMode(compatibilityCalls);
        } catch (IncompatiblePedigreeException e) {
            logger.error("Problem with annotating VariantContext for Mendelian inheritance.", e);
        }
        return Collections.emptyMap();
    }

    /**
     * This method will check the supplied list of {@link VariantEvaluation} against the pedigree supplied in the class
     * constructor to determine the sub-modes of inheritance with which the variants are compatible. It does not alter the
     * input objects. It is expected that the input variants have been pre-filtered, failure to do so will result in
     * overly-long computation time and potentially incorrect analyses.
     *
     * @param variantEvaluations a pre-filtered list of variants
     * @return a map of sub-inheritance modes and the variants which are compatible with them
     */
    public Map<SubModeOfInheritance, List<VariantEvaluation>> computeCompatibleInheritanceSubModes(List<VariantEvaluation> variantEvaluations) {
        List<GenotypeCalls> genotypeCalls = buildGenotypeCalls(variantEvaluations);
        try {
            Map<SubModeOfInheritance, ImmutableList<GenotypeCalls>> compatibilityCalls = mendelChecker.checkMendelianInheritanceSub(genotypeCalls);
            logger.debug("{}", compatibilityCalls);
            return variantsGroupedByCompatibleSubMode(compatibilityCalls);
        } catch (IncompatiblePedigreeException e) {
            logger.error("Problem with annotating VariantContext for Mendelian inheritance.", e);
        }
        return Collections.emptyMap();
    }


    private Map<ModeOfInheritance, List<VariantEvaluation>> variantsGroupedByCompatibleMode(Map<ModeOfInheritance, ImmutableList<GenotypeCalls>> compatibilityCalls) {
        Map<ModeOfInheritance, List<VariantEvaluation>> results = new EnumMap<>(ModeOfInheritance.class);
        for (Map.Entry<ModeOfInheritance, ImmutableList<GenotypeCalls>> entry : compatibilityCalls.entrySet()) {
            ModeOfInheritance compatibleMode = entry.getKey();
            if (inheritanceModeOptions.getDefinedModes().contains(compatibleMode)) {
                List<GenotypeCalls> genotypeCalls = entry.getValue();
                float maxFreqForMode = inheritanceModeOptions.getMaxFreqForMode(compatibleMode);
                List<VariantEvaluation> compatibleVariants = getCompatibleVariantsUnderFrequencyThreshold(genotypeCalls, maxFreqForMode);
                if (!compatibleVariants.isEmpty()) {
                    results.put(compatibleMode, compatibleVariants);
                }
            }
        }
        return results;
    }

    private Map<SubModeOfInheritance, List<VariantEvaluation>> variantsGroupedByCompatibleSubMode(Map<SubModeOfInheritance, ImmutableList<GenotypeCalls>> compatibilityCalls) {
        Map<SubModeOfInheritance, List<VariantEvaluation>> results = new EnumMap<>(SubModeOfInheritance.class);
        for (Map.Entry<SubModeOfInheritance, ImmutableList<GenotypeCalls>> entry : compatibilityCalls.entrySet()) {
            SubModeOfInheritance compatibleSubMode = entry.getKey();
            if (inheritanceModeOptions.getDefinedSubModes().contains(compatibleSubMode)) {
                List<GenotypeCalls> genotypeCalls = entry.getValue();
                float maxFreqForMode = inheritanceModeOptions.getMaxFreqForSubMode(compatibleSubMode);
                List<VariantEvaluation> compatibleVariants = getCompatibleVariantsUnderFrequencyThreshold(genotypeCalls, maxFreqForMode);
                if (!compatibleVariants.isEmpty()) {
                    results.put(compatibleSubMode, compatibleVariants);
                }
            }
        }
        return results;
    }

    private List<VariantEvaluation> getCompatibleVariantsUnderFrequencyThreshold(List<GenotypeCalls> genotypeCalls, float maxFreqForMode) {
        List<VariantEvaluation> compatibleVariants = new ArrayList<>();
        for (GenotypeCalls callResults : genotypeCalls) {
            VariantEvaluation variantEvaluation = (VariantEvaluation) callResults.getPayload();
            // Issue #361 Add logic here to ignore a given list of frequency sources when checking against the maxFreqForMode e.g. LOCAL
            // float maxFreq = frequencyData.maxFreqIgnoring(Set.of(LOCAL, ESP))
            // float maxFreq = frequencyData.maxFreqFrom(Set.of(LOCAL, ESP))
            FrequencyData frequencyData = variantEvaluation.getFrequencyData();
            if (frequencyData.maxFreq() <= maxFreqForMode || variantEvaluation.isWhiteListed()) {
                compatibleVariants.add(variantEvaluation);
            } else {
                logger.debug("FAIL variant freq {} >= {} (maxFreq for MOI) {}", frequencyData.maxFreq(), maxFreqForMode, variantEvaluation);
            }
        }
        return compatibleVariants;
    }

    private List<GenotypeCalls> buildGenotypeCalls(List<VariantEvaluation> variantEvaluations) {
        ArrayList<GenotypeCalls> result = new ArrayList<>();

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            GenotypeCallsBuilder genotypeCallsBuilder = new GenotypeCallsBuilder();

            genotypeCallsBuilder.setPayload(variantEvaluation);

            ChromosomeType chromosomeType = toChromosomeType(variantEvaluation.contigId());
            genotypeCallsBuilder.setChromType(chromosomeType);

            //This could be moved into the VariantFactory and a getSampleGenotypes() method added to the VariantEvaluation
            //then we can mostly discard the VariantContext, apart from writing out again...
            SampleGenotypes sampleGenotypes = variantEvaluation.getSampleGenotypes();
            logger.debug("Converting {} {} {}", variantEvaluation.ref(), variantEvaluation.alt(), sampleGenotypes);
            for (SampleData sampleData : sampleGenotypes) {
                String sampleName = sampleData.getId();
                SampleGenotype sampleGenotype = sampleData.getSampleGenotype();
                Genotype genotype = toGenotype(sampleGenotype);
                logger.debug("Converted {} {} to {}", sampleName, sampleGenotype, genotype);
                genotypeCallsBuilder.getSampleToGenotype().put(sampleName, genotype);
            }
            result.add(genotypeCallsBuilder.build());
        }

        return result;
    }

    private Genotype toGenotype(SampleGenotype sampleGenotype) {
        List<Integer> calls = new ArrayList<>(sampleGenotype.numCalls());
        for (AlleleCall alleleCall : sampleGenotype.getCalls()) {
            calls.add(toCall(alleleCall));
        }
        return new Genotype(calls);
    }

    private int toCall(AlleleCall alleleCall) {
        switch (alleleCall) {
            case REF:
                return Genotype.REF_CALL;
            case ALT:
                return 1;
            case OTHER_ALT:
                return 2;
            case NO_CALL:
            default:
                return Genotype.NO_CALL;
        }
    }

    private ChromosomeType toChromosomeType(int chromosome) {
        switch (chromosome) {
            case 23:
                return ChromosomeType.X_CHROMOSOMAL;
            case 24:
                return ChromosomeType.Y_CHROMOSOMAL;
            case 25:
                return ChromosomeType.MITOCHONDRIAL;
            default:
                return ChromosomeType.AUTOSOMAL;
        }
    }
}
