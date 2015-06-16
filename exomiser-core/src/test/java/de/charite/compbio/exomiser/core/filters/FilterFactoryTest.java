/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for FilterFactory
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterFactoryTest {
   
    private SettingsBuilder settingsBuilder;
    private FilterFactory instance;
        
    private final GeneticInterval interval = new GeneticInterval(2, 12345, 67890);
    
    public FilterFactoryTest() {
    }

    @Before
    public void setUp() {
        settingsBuilder = new SettingsBuilder();
        instance = new FilterFactory();
    }
    
    @Test
    public void testMakeVariantFiltersCanMakeAllTypesOfVariantFilter() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        Set<Integer> geneIdsToKeep = new HashSet<>();
        geneIdsToKeep.add(1);
        
        FilterSettings settings = settingsBuilder
                .genesToKeepList(geneIdsToKeep)
                .removePathFilterCutOff(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                .build();

        List<VariantFilter> expResult = new ArrayList<>();

        expResult.add(new EntrezGeneIdFilter(geneIdsToKeep));
        expResult.add(new TargetFilter());
        expResult.add(new FrequencyFilter(0.25f, false));
        expResult.add(new QualityFilter(2f));
        expResult.add(new PathogenicityFilter(true));
        expResult.add(new IntervalFilter(interval));
        
        List<VariantFilter> result = instance.makeVariantFilters(settings);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testMakeVariantFiltersDoesNotIncludeGeneFiltersInReturnedList() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval VariantFilters 
        FilterSettings settings = settingsBuilder
                .removePathFilterCutOff(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                //and a Inheritance GeneFilter
                .modeOfInheritance(ModeOfInheritance.X_DOMINANT)
                .build();

        List<VariantFilter> expResult = new ArrayList<>();

        expResult.add(new TargetFilter());
        expResult.add(new FrequencyFilter(0.25f, false));
        expResult.add(new QualityFilter(2f));
        expResult.add(new PathogenicityFilter(true));
        expResult.add(new IntervalFilter(interval));
        
        List<VariantFilter> result = instance.makeVariantFilters(settings);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testMakeGeneFilters() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_RECESSIVE;
        //make a new Settings object specifying an Inheritance GeneFilter
        FilterSettings settings = settingsBuilder.modeOfInheritance(modeOfInheritance).build();

        List<GeneFilter> expResult = new ArrayList<>();
        expResult.add(new InheritanceFilter(modeOfInheritance));
                
        List<GeneFilter> result = instance.makeGeneFilters(settings);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDetermineFilterTypesToRunOnDefaultSettings() {
        //make a new default Settings object
        FilterSettings settings = settingsBuilder.build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        
        List<FilterType> result = instance.determineFilterTypesToRun(settings);
        
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDetermineFilterTypesToRun() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        FilterSettings settings = settingsBuilder
                .removePathFilterCutOff(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                .build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.QUALITY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        expResult.add(FilterType.INTERVAL_FILTER);
        
        List<FilterType> result = instance.determineFilterTypesToRun(settings);
        
        assertThat(result, equalTo(expResult));
    }
    
}
