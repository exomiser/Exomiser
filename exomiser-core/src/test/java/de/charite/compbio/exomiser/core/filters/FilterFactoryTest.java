/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.filters.Filter;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.FilterFactory;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.BedFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import jannovar.common.ModeOfInheritance;
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
        ExomiserSettings settings = settingsBuilder.removePathFilterCutOff(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval).build();

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
    public void testMakeVariantFilters() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        Set<Integer> geneIdsToKeep = new HashSet<>();
        geneIdsToKeep.add(1);
        
        ExomiserSettings settings = settingsBuilder.genesToKeepList(geneIdsToKeep).removePathFilterCutOff(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval).build();

        List<VariantFilter> expResult = new ArrayList<>();

        VariantFilter geneIdFilter = new EntrezGeneIdFilter(geneIdsToKeep);
        expResult.add(geneIdFilter);

        VariantFilter targetFilter = new TargetFilter();
        expResult.add(targetFilter);
        
        VariantFilter frequencyFilter = new FrequencyFilter(0.25f, false);
        expResult.add(frequencyFilter);
        
        VariantFilter qualityFilter = new QualityFilter(2f);
        expResult.add(qualityFilter);
        
        VariantFilter pathogenicityFilter = new PathogenicityFilter(true);
        expResult.add(pathogenicityFilter);
        
        VariantFilter intervalFilter = new IntervalFilter(interval);
        expResult.add(intervalFilter);
        
        List<VariantFilter> result = instance.makeVariantFilters(settings);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testMakeVariantFiltersDoesNotIncludeGeneFiltersInReturnedList() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval VariantFilters 
        ExomiserSettings settings = settingsBuilder.removePathFilterCutOff(true).maximumFrequency(0.25f).minimumQuality(2f).geneticInterval(interval)
                //and a Inheritance GeneFilter
                .modeOfInheritance(ModeOfInheritance.X_DOMINANT).build();

        List<VariantFilter> expResult = new ArrayList<>();

        VariantFilter targetFilter = new TargetFilter();
        expResult.add(targetFilter);
        
        VariantFilter frequencyFilter = new FrequencyFilter(0.25f, false);
        expResult.add(frequencyFilter);
        
        VariantFilter qualityFilter = new QualityFilter(2f);
        expResult.add(qualityFilter);
        
        VariantFilter pathogenicityFilter = new PathogenicityFilter(true);
        expResult.add(pathogenicityFilter);
        
        VariantFilter intervalFilter = new IntervalFilter(interval);
        expResult.add(intervalFilter);
        
        List<VariantFilter> result = instance.makeVariantFilters(settings);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testMakeGeneFilters() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_RECESSIVE;
        //make a new Settings object specifying an Inheritance GeneFilter
        ExomiserSettings settings = settingsBuilder.modeOfInheritance(modeOfInheritance).build();

        List<GeneFilter> expResult = new ArrayList<>();

        GeneFilter inheritanceFilter = new InheritanceFilter(modeOfInheritance);
        expResult.add(inheritanceFilter);
        
                
        List<GeneFilter> result = instance.makeGeneFilters(settings);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetEntrezGeneIdFilter() {
        Set<Integer> geneIdentifiers = new HashSet<>();
        Filter expResult = new EntrezGeneIdFilter(geneIdentifiers);
        Filter result = instance.getEntrezGeneIdFilter(geneIdentifiers);
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
        Filter expResult = new FrequencyFilter(maxFrequency, filterOutAllDbsnp);
        
        Filter result = instance.getFrequencyFilter(maxFrequency, filterOutAllDbsnp);
        
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetQualityFilter() {
        float quality_threshold = 8f;
        Filter expResult = new QualityFilter(quality_threshold);
        Filter result = instance.getQualityFilter(quality_threshold);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetPathogenicityFilter() {
        boolean filterOutNonpathogenic = false;
        Filter expResult = new PathogenicityFilter(filterOutNonpathogenic);
        Filter result = instance.getPathogenicityFilter(filterOutNonpathogenic);
        assertThat(result, equalTo(expResult));

    }

    @Test
    public void testGetIntervalFilter() {
        Filter expResult = new IntervalFilter(interval);
        Filter result = instance.getIntervalFilter(interval);
        assertThat(result, equalTo(expResult));
    }
   
    @Test
    public void testGetBedFilter() {
        Set<String> genes = new TreeSet<>();
        Filter expResult = new BedFilter(genes);
        Filter result = instance.getBedFilter(genes);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testGetInheritanceFilter() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        Filter expFilter = new InheritanceFilter(modeOfInheritance);
        Filter resultFilter = instance.getInheritanceFilter(modeOfInheritance);
        assertThat(resultFilter, equalTo(expFilter));
    }
    
}
