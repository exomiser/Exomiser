/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.frequency;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class FrequencyIT {

    private static final int VARIANTS_IN_GENOME = 4000000;
    private static final int VARIANTS_IN_EXOME = 40000;

    public static void main(String[] args) {

//        sleep(20);

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
            Frequency maf1 = new Frequency(getRandomPercentage());
            mafList.add(maf1);
            Frequency maf2 = new Frequency(getRandomPercentage());
            mafList.add(maf2);
            Frequency maf3 = new Frequency(getRandomPercentage());
            mafList.add(maf3);
            Frequency maf4 = new Frequency(getRandomPercentage());
            mafList.add(maf4);
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, (long) i * 4, Frequency.class.getName()));
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
            float maf1 = getRandomPercentage();
            float maf2 = getRandomPercentage();
            float maf3 = getRandomPercentage();
            float maf4 = getRandomPercentage();
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
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format("Took %dms to create %s %s objects", end - start, (long) i * 4, Float.class.getName()));
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

    public FrequencyIT() {
    }

    @Before
    public void setUp() {
    }

    /**
     * Attach the performance monitor to this method...
     */
    @Test
    public void testConstructorPerformanceGenome() {
        int i = 0;
        while (i < VARIANTS_IN_GENOME) {
            i++;
            Frequency maf = new Frequency(20f);
        }
        System.out.println(i);

    }

    /**
     * Attach the performance monitor to this method...
     */
    @Test
    public void testOverThresholdPerformanceGenome() {
        float threshold = 24.56f;

        for (int i = 0; i < VARIANTS_IN_GENOME; i++) {
            Frequency maf = new Frequency(20f);
        }
    }
}
