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

package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.AlleleCall;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class VariantEffectCounter {

    private final List<VariantEffectCount> variantEffectCounts;

    public VariantEffectCounter(List<String> sampleNames, List<VariantEvaluation> variantEvaluations) {
        variantEffectCounts = setup(sampleNames.size(), variantEvaluations);
    }

    private List<VariantEffectCount> setup(int numSamples, List<VariantEvaluation> variantEvaluations) {
        Map<VariantEffect, int[]> tempCounts = countVariantEffects(numSamples, variantEvaluations);

        return tempCounts.entrySet()
                .stream()
                .map(entry -> {
                    VariantEffect variantEffect = entry.getKey();
                    List<Integer> counts = IntStream.of(entry.getValue())
                            .boxed()
                            .collect(toList());
                    return new VariantEffectCount(variantEffect, counts);
                })
                .collect(toList());
    }

    private Map<VariantEffect, int[]> countVariantEffects(int numSamples, List<VariantEvaluation> variantEvaluations) {
        Map<VariantEffect, int[]> tempCounts = new EnumMap<>(VariantEffect.class);
        // ensure all cases are created as the input set may not contain them all
        for (VariantEffect variantEffect : VariantEffect.values()) {
            tempCounts.put(variantEffect, zeroes(numSamples));
        }

        for (VariantEvaluation variant : variantEvaluations) {
            Map<String, SampleGenotype> sampleGenotypes = variant.getSampleGenotypes();
            // this is always an ordered map in the order of the sample names declared in the VCF header
            List<SampleGenotype> genotypes = ImmutableList.copyOf(sampleGenotypes.values());
            VariantEffect effect = variant.getVariantEffect();
            int[] effectCounts = tempCounts.get(effect);
            for (int i = 0; i < genotypes.size(); i++) {
                SampleGenotype sampleGenotype = genotypes.get(i);
                List<AlleleCall> calls = sampleGenotype.getCalls();
                if (calls.size() == 2 && calls.contains(AlleleCall.ALT)) {
                    effectCounts[i]++;
                }
            }
        }
        return tempCounts;
    }

    private int[] zeroes(int numSamples) {
        int[] zeroes = new int[numSamples];
        for (int i = 0; i < numSamples; i++) {
            zeroes[i] = 0;
        }
        return zeroes;
    }

    public List<VariantEffectCount> getVariantEffectCounts(Set<VariantEffect> variantEffects) {
        return variantEffectCounts.stream()
                .filter(variantEffectCount -> variantEffects.contains(variantEffectCount.getVariantType()))
                .collect(toList());
    }
}
