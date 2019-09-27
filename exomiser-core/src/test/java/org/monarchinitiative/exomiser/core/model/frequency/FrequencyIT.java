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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.frequency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class FrequencyIT {

    private static final int VARIANTS_IN_GENOME = 2000_000;
    private static final int VARIANTS_IN_EXOME = 40_000;

    @Disabled
    @Test
    void testConstructorPerformance() {
        FrequencySource[] sources = FrequencySource.values();
        int[] sizes = {VARIANTS_IN_EXOME, VARIANTS_IN_GENOME};
        for (int size : sizes) {
            float[] freqs = createPrimitives(size, sources);
            List<Frequency> frequencies = createFrequencies(freqs, sources);
            compareThresholdForObjects(frequencies, 23.0f);
            List<Float> floats = createAutoBoxedPrimitives(freqs, sources);
            compareThresholdForAutoBoxedPrimitives(floats, 23.0f);
        }
    }

    private float[] createPrimitives(int numVariants, FrequencySource[] sources) {
        int numFreqs = numVariants * sources.length;
        float[] freqs = new float[numFreqs];
        long start = System.currentTimeMillis();

        for (int i = 0; i < numFreqs; ) {
            freqs[i++] = (float) Math.random() * 100;
        }

        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %d random floats", end - start, numFreqs));
        return freqs;
    }

    private static List<Frequency> createFrequencies(float[] freqs, FrequencySource[] sources) {
        long start = System.currentTimeMillis();
        List<Frequency> mafList = new ArrayList<>(freqs.length);
        for (int i = 0; i < freqs.length; ) {
            for (int j = 0, sourcesLength = sources.length; j < sourcesLength; j++) {
                mafList.add(Frequency.of(sources[j], freqs[i++]));
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, freqs.length, Frequency.class
                .getName()));
        return mafList;
    }
    
    private static void compareThresholdForObjects(List<Frequency> mafList, float threshold) {
        long start = System.currentTimeMillis();

        long overThreshold = 0;
        for (Frequency frequency : mafList) {
            if (frequency.isOverThreshold(threshold)) {
                overThreshold++;
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to compare %s %s objects - %s were over threshold of %s", end - start, mafList
                .size(), Frequency.class.getName(), overThreshold, threshold));
    }

    private static List<Float> createAutoBoxedPrimitives(float[] freqs, FrequencySource[] sources) {
        long start = System.currentTimeMillis();
        List<Float> floatList = new ArrayList<>(freqs.length);
        for (int i = 0; i < freqs.length; ) {
            for (int j = 0, sourcesLength = sources.length; j < sourcesLength; j++) {
                floatList.add(freqs[i++]);
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, freqs.length, Float.class.getName()));
        return floatList;
    }

    
    private static void compareThresholdForAutoBoxedPrimitives(List<Float> floatList, float threshold) {
        
        long start = System.currentTimeMillis();

        long overThreshold = 0;
        for (Float value : floatList) {
            if (value > threshold) {
                overThreshold++;
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to compare %s %s objects - %s were over threshold of %s", end - start, floatList
                .size(), Float.class.getName(), overThreshold, threshold));
    }
}
