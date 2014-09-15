/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import java.util.ArrayList;
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
    
    private final GeneticInterval interval = new GeneticInterval((byte) 2, 12345, 67890);
    
    public FilterFactoryTest() {
    }

    @Before
    public void setUp() {
        settingsBuilder = new ExomiserSettings.SettingsBuilder();
        instance = new FilterFactory();
    }
    
    @Test
    public void testDetermineFilterTypesToRunOnDefaultSettings() {
        //make a new default Settings object
        ExomiserSettings settings = settingsBuilder.build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        
        List<FilterType> result = FilterFactory.determineFilterTypesToRun(settings);
        
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDetermineFilterTypesToRun() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        ExomiserSettings settings = settingsBuilder.keepNonPathogenicMissense(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval).build();

        List<FilterType> expResult = new ArrayList<>();

        expResult.add(FilterType.TARGET_FILTER);
        expResult.add(FilterType.FREQUENCY_FILTER);
        expResult.add(FilterType.QUALITY_FILTER);
        expResult.add(FilterType.PATHOGENICITY_FILTER);
        expResult.add(FilterType.INTERVAL_FILTER);
        
        List<FilterType> result = FilterFactory.determineFilterTypesToRun(settings);
        
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testMakeFilters() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        ExomiserSettings settings = settingsBuilder.keepNonPathogenicMissense(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval).build();

        List<Filter> expResult = new ArrayList<>();

        TargetFilter targetFilter = new TargetFilter();
        expResult.add(targetFilter);
        
        FrequencyFilter frequencyFilter = new FrequencyFilter(0.25f, false);
        expResult.add(frequencyFilter);
        
        QualityFilter qualityFilter = new QualityFilter(2f);
        expResult.add(qualityFilter);
        
        PathogenicityFilter pathogenicityFilter = new PathogenicityFilter(true);
        expResult.add(pathogenicityFilter);
        
        IntervalFilter intervalFilter = new IntervalFilter(interval);
        expResult.add(intervalFilter);
        
        List<Filter> result = instance.makeFilters(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetTargetFilter() {
        Filter expResult = new TargetFilter();
        Filter result = instance.getTargetFilter();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetFrequencyFilter() {
        float maxFrequency = 0.0F;
        boolean filterOutAllDbsnp = false;
        FrequencyFilter expResult = new FrequencyFilter(maxFrequency, filterOutAllDbsnp);
        
        FrequencyFilter result = (FrequencyFilter) instance.getFrequencyFilter(maxFrequency, filterOutAllDbsnp);
        
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetQualityFilter() {
        float quality_threshold = 8f;
        QualityFilter expResult = new QualityFilter(quality_threshold);
        QualityFilter result = (QualityFilter) instance.getQualityFilter(quality_threshold);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetPathogenicityFilter() {
        boolean filterOutNonpathogenic = false;
        boolean removeSynonomousVariants = false;
        PathogenicityFilter expResult = new PathogenicityFilter(removeSynonomousVariants);
        PathogenicityFilter result = (PathogenicityFilter) instance.getPathogenicityFilter(filterOutNonpathogenic);
        assertThat(result, equalTo(expResult));

    }

    @Test
    public void testGetIntervalFilter() {
        IntervalFilter expResult = new IntervalFilter(interval);
        IntervalFilter result = (IntervalFilter) instance.getIntervalFilter(interval);
        assertThat(result, equalTo(expResult));
    }
   
    @Test
    public void testGetBedFilter() {
        Set<String> genes = new TreeSet<>();
        BedFilter expResult = new BedFilter(genes);
        BedFilter result = (BedFilter) instance.getBedFilter(genes);
        assertThat(result, equalTo(expResult));
    }
    
}
