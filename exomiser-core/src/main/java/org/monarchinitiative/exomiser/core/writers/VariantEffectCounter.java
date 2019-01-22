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
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.AlleleCall;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

public class VariantEffectCounter {

    private final int numSamples;
    private final List<Map<VariantEffect, Integer>> sampleVariantEffectCounts;

    public VariantEffectCounter(List<String> sampleNames, List<VariantEvaluation> variantEvaluations) {
        numSamples = sampleNames.size();
        sampleVariantEffectCounts = new ArrayList<>();
        for (int i = 0; i < numSamples; ++i) {
            sampleVariantEffectCounts.add(new EnumMap<>(VariantEffect.class));
        }
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            put(variantEvaluation);
        }

    }

    /**
     * Increment the counter for the given variant's effect.
     *
     * @param variant
     */
    private void put(VariantEvaluation variant) {
        VariantEffect effect = variant.getVariantEffect();
        if (effect == null) {
            return;
        }

        Map<String, SampleGenotype> sampleGenotypes = variant.getSampleGenotypes();
        // this is always an ordered map in the order of the sample names declared in the VCF header
        List<SampleGenotype> genotypes = new ArrayList<>(sampleGenotypes.values());
        for (int i = 0; i < genotypes.size(); i++) {
            SampleGenotype sampleGenotype = genotypes.get(i);
            List<AlleleCall> calls = sampleGenotype.getCalls();
            if (calls.size() == 2 && calls.contains(AlleleCall.ALT)) {
                if (!sampleVariantEffectCounts.get(i).containsKey(effect)) {
                    sampleVariantEffectCounts.get(i).put(effect, 1);
                } else {
                    sampleVariantEffectCounts.get(i).put(effect, sampleVariantEffectCounts.get(i).get(effect) + 1);
                }
            }
        }
    }

    public List<VariantEffectCount> getVariantEffectCounts(Set<VariantEffect> variantEffects) {
        Set<VariantEffect> effects = EnumSet.copyOf(variantEffects);

        List<Map<VariantEffect, Integer>> freqMaps = getFrequencyMap(variantEffects);
        for (int sampleIdx = 0; sampleIdx < numSamples; ++sampleIdx) {
            effects.addAll(freqMaps.get(sampleIdx).keySet());
        }

        List<VariantEffectCount> result = new ArrayList<>();
        for (VariantEffect effect : effects) {
            List<Integer> typeSpecificCounts = new ArrayList<>();
            for (int sampleIdx = 0; sampleIdx < numSamples; ++sampleIdx) {
                typeSpecificCounts.add(freqMaps.get(sampleIdx).get(effect));
            }
            result.add(new VariantEffectCount(effect, typeSpecificCounts));
        }

        return result;
    }

    /**
     * @return map with the frequency for the <code>effects</code> for each
     * individual
     */
    private List<Map<VariantEffect, Integer>> getFrequencyMap(Collection<VariantEffect> effects) {

        ImmutableList.Builder<Map<VariantEffect, Integer>> listBuilder = new ImmutableList.Builder<>();
        for (Map<VariantEffect, Integer> map : sampleVariantEffectCounts) {
            Map<VariantEffect, Integer> counters2 = new EnumMap<>(VariantEffect.class);
            for (VariantEffect effect : effects) {
                counters2.put(effect, 0);
            }
            for (Map.Entry<VariantEffect, Integer> entry : counters2.entrySet()) {
                if (counters2.containsKey(entry.getKey())) {
                    counters2.put(entry.getKey(), entry.getValue());
                }
            }

            ImmutableMap.Builder<VariantEffect, Integer> mapBuilder = new ImmutableMap.Builder<>();
            for (Map.Entry<VariantEffect, Integer> entry : counters2.entrySet()) {
                if (map.get(entry.getKey()) == null) {
                    mapBuilder.put(entry.getKey(), 0);
                } else {
                    mapBuilder.put(entry.getKey(), map.get(entry.getKey()));
                }
            }
            listBuilder.add(mapBuilder.build());
        }
        return listBuilder.build();
    }
}
