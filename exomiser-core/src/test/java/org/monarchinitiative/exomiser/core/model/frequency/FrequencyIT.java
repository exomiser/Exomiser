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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.frequency;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyIT {

    private static final int VARIANTS_IN_GENOME = 4000000;
    private static final int VARIANTS_IN_EXOME = 40000;

    public static void main(String[] args) {

        sleep(10);

        List exomeMafList = createObjects(VARIANTS_IN_EXOME);
        compareThresholdForObjects(exomeMafList, 23.0f);

        createPrimitives(VARIANTS_IN_EXOME);
        List<Float> exomeFloats = createAutoBoxedPrimitives(VARIANTS_IN_EXOME);
        compareThresholdForAutoBoxedPrimitives(exomeFloats, 23.0f);
        
        sleep(2);

        List genomeMafList = createObjects(VARIANTS_IN_GENOME);
        compareThresholdForObjects(genomeMafList, 23.0f);

        createPrimitives(VARIANTS_IN_GENOME);
        List<Float> genomeFloats = createAutoBoxedPrimitives(VARIANTS_IN_GENOME);
        compareThresholdForAutoBoxedPrimitives(genomeFloats, 23.0f);
    }

    private static void sleep(int secs) {
        System.out.printf("Sleeping for %d secs%n", secs);
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(FrequencyIT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<Frequency> createObjects(int numObjects) {
        long start = System.currentTimeMillis();
        int i = 0;
        List<Frequency> mafList = new ArrayList<>();
        while (i < numObjects) {
            i++;
            //we're creating four variants here
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.ESP_ALL));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.ESP_AFRICAN_AMERICAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.ESP_EUROPEAN_AMERICAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.THOUSAND_GENOMES));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_AMERICAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_FINNISH));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_EAST_ASIAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_NON_FINNISH_EUROPEAN));
            mafList.add(Frequency.valueOf(getRandomPercentage(), FrequencySource.EXAC_OTHER));
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, mafList.size(), Frequency.class.getName()));
        return mafList;
    }
    
    private static void compareThresholdForObjects(List<Frequency> mafList, float threshold) {
        
        long start = System.currentTimeMillis();

        long mafOverThrehold = 0;
        for (Frequency minorAlleleFrequency : mafList) {
            if (minorAlleleFrequency.getFrequency() > threshold) {
                mafOverThrehold++;
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to compare %s %s objects  - %s were over threshold of %s", end - start, mafList.size(), Frequency.class.getName(), mafOverThrehold, threshold));
    }

    private static void createPrimitives(int numObjects) {
        long start = System.currentTimeMillis();
        int i = 0;
        while (i < numObjects) {
            i++;
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
            getRandomPercentage();
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s primitives", end - start, (long) i * 4));
    }
    
    private static List<Float> createAutoBoxedPrimitives(int numObjects) {
        long start = System.currentTimeMillis();
        int i = 0;
        List<Float> floatList = new ArrayList<>();
        while (i < numObjects) {
            i++;
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
            floatList.add(getRandomPercentage());
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, floatList.size(), Float.class.getName()));
        return floatList;
    }

    
    private static void compareThresholdForAutoBoxedPrimitives(List<Float> floatList, float threshold) {
        
        long start = System.currentTimeMillis();

        long overThrehold = 0;
        for (Float value : floatList) {
            if (value > threshold) {
                overThrehold++;
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to compare %s %s objects  - %s were over threshold of %s", end - start, floatList.size(), Float.class.getName(), overThrehold, threshold));
    }
        
    private static float getRandomPercentage() {
        return (float) Math.random() * 100;
    }

    /**
     * Attach the performance monitor to this method...
     */
    @Test
    public void testConstructorPerformanceGenome() {
        int i = 0;
        while (i < VARIANTS_IN_GENOME * 10) {
            i++;
            Frequency.valueOf(20f, FrequencySource.UNKNOWN);
        }
        System.out.println(i);
    }

    /**
     * Attach the performance monitor to this method...
     */
    @Test
    public void testOverThresholdPerformanceGenome() {
        float threshold = 24.56f;

        for (int i = 0; i < VARIANTS_IN_GENOME * 10; i++) {
            Frequency.valueOf(20f, FrequencySource.UNKNOWN);
        }
    }
}
