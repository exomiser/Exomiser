package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

public class VariantEffectCounter {

    /**
     * counter for each variant effect
     */
    private final List<Map<VariantEffect, Integer>> counters;

    VariantEffectCounter(int numSamples) {
        this.counters = new ArrayList<>();
        for (int i = 0; i < numSamples; ++i) {
            counters.add(new EnumMap<VariantEffect, Integer>(VariantEffect.class));
        }
    }

    /**
     * Increment the counter for the given variant's effect.
     *
     * @param variant
     */
    public void put(VariantEvaluation variant) {
        VariantEffect effect = variant.getVariantEffect();
        if (effect == null) {
            return;
        }
        VariantContext variantContext = variant.getVariantContext();
        int numSamples = variant.getNumberOfIndividuals();
        for (int sampleIdx = 0; sampleIdx < numSamples; ++sampleIdx) {
            final Genotype gt = variantContext.getGenotype(sampleIdx);
            if (gt.getAlleles().size() != 2) {
                // counted as no-call
                continue;
            }
            boolean isAltAllele = false;
            for (int i = 0; i < 2; ++i) {
                if (gt.getAllele(i).equals(variantContext.getAlternateAllele(variant.getAltAlleleId()))) {
                    isAltAllele = true;
                }
            }
            if (!isAltAllele) {
                // does not have correct alternative allele
                continue;
            }
            if (!counters.get(sampleIdx).containsKey(effect)) {
                counters.get(sampleIdx).put(effect, 1);
            } else {
                counters.get(sampleIdx).put(effect, counters.get(sampleIdx).get(effect).intValue() + 1);
            }
        }
    }

    /**
     * @return map with the frequency for all variant effects in the map for
     * each individual
     */
    public List<Map<VariantEffect, Integer>> getFrequencyMap() {
        ImmutableList.Builder<Map<VariantEffect, Integer>> builder = new ImmutableList.Builder<>();

        int sampleIdx = 0;
        for (Map<VariantEffect, Integer> map : counters) {
            ImmutableMap.Builder<VariantEffect, Integer> builder2 = new ImmutableMap.Builder<>();
            for (Map.Entry<VariantEffect, Integer> entry : counters.get(sampleIdx).entrySet()) {
                builder2.put(entry.getKey(), map.get(entry.getKey()));
            }
            builder.add(builder2.build());
            ++sampleIdx;
        }
        return builder.build();
    }

    /**
     * @return map with the frequency for the <code>effects</code> for each
     * individual
     */
    public List<Map<VariantEffect, Integer>> getFrequencyMap(Collection<VariantEffect> effects) {
        ImmutableList.Builder<Map<VariantEffect, Integer>> listBuilder = new ImmutableList.Builder<>();

        for (Map<VariantEffect, Integer> map : counters) {
            Map<VariantEffect, Integer> counters2 = new HashMap<>();
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
