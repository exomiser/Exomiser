package de.charite.compbio.exomiser.core.writers;

import htsjdk.variant.variantcontext.Genotype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.annotation.VariantEffect;

public class VariantEffectCounter {

    /** counter for each variant effect */
    private final ArrayList<HashMap<VariantEffect, Integer>> counters;

    VariantEffectCounter(int numSamples) {
        this.counters = new ArrayList<HashMap<VariantEffect, Integer>>();
        for (int i = 0; i < numSamples; ++i)
            counters.add(new HashMap<VariantEffect, Integer>());
    }

    /**
     * Increment the counter for the given variant's effect.
     * 
     * @param variant
     */
    public void put(Variant variant) {
        VariantEffect effect = variant.getVariantEffect();
        if (effect == null)
            return;

        for (int sampleIdx = 0; sampleIdx < variant.vc.getSampleNames().size(); ++sampleIdx) {
            final Genotype gt = variant.vc.getGenotype(sampleIdx);
            if (gt.getAlleles().size() != 2)
                continue; // counted as no-call
            boolean isAltAllele = false;
            for (int i = 0; i < 2; ++i)
                if (gt.getAllele(i).equals(variant.vc.getAlternateAllele(variant.altAlleleID)))
                    isAltAllele = true;
            if (!isAltAllele)
                continue; // does not have correct alternative allele
            
            if (!counters.get(sampleIdx).containsKey(effect))
                counters.get(sampleIdx).put(effect, 1);
            else
                counters.get(sampleIdx).put(effect, counters.get(sampleIdx).get(effect).intValue() + 1);
        }
    }

    /**
     * @return map with the frequency for all variant effects in the map for each individual
     */
    public ImmutableList<ImmutableMap<VariantEffect, Integer>> getFrequencyMap() {
        ImmutableList.Builder<ImmutableMap<VariantEffect, Integer>> builder = new ImmutableList.Builder<ImmutableMap<VariantEffect, Integer>>();

        int sampleIdx = 0;
        for (HashMap<VariantEffect, Integer> map : counters) {
            ImmutableMap.Builder<VariantEffect, Integer> builder2 = new ImmutableMap.Builder<VariantEffect, Integer>();
            for (Map.Entry<VariantEffect, Integer> entry : counters.get(sampleIdx).entrySet())
                builder2.put(entry.getKey(), map.get(entry.getKey()));
            builder.add(builder2.build());
            ++sampleIdx;
        }
        return builder.build();
    }

    /**
     * @return map with the frequency for the <code>effects</code> for each individual
     */
    public ImmutableList<ImmutableMap<VariantEffect, Integer>> getFrequencyMap(Collection<VariantEffect> effects) {
        ImmutableList.Builder<ImmutableMap<VariantEffect, Integer>> builder = new ImmutableList.Builder<ImmutableMap<VariantEffect, Integer>>();

        for (HashMap<VariantEffect, Integer> map : counters) {
            HashMap<VariantEffect, Integer> counters2 = new HashMap<VariantEffect, Integer>();
            for (VariantEffect effect : effects)
                counters2.put(effect, 0);
            for (Map.Entry<VariantEffect, Integer> entry : counters2.entrySet())
                if (counters2.containsKey(entry))
                    counters2.put(entry.getKey(), entry.getValue());

            ImmutableMap.Builder<VariantEffect, Integer> builder2 = new ImmutableMap.Builder<VariantEffect, Integer>();
            for (Map.Entry<VariantEffect, Integer> entry : counters2.entrySet())
                builder2.put(entry.getKey(), map.get(entry.getKey()));
            builder.add(builder2.build());
        }
        return builder.build();
    }

}
