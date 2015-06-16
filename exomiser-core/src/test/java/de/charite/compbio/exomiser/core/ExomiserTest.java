/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.FilterSettings;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
* Tests for Exomiser class.
*
* @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
*/
public class ExomiserTest {

    private Exomiser instance;
            
    private SettingsBuilder settingsBuilder;
    private ExomiserSettings settings;
    private SampleData sampleData;

    @Before
    public void setUp() {       
        VariantDataService stubVariantDataService = new VariantDataServiceStub();
        PriorityFactory stubPriorityFactory = new NoneTypePriorityFactoryStub();
        instance = new Exomiser(stubVariantDataService, stubPriorityFactory);
        
        settingsBuilder = new ExomiserSettings.SettingsBuilder().vcfFilePath(Paths.get("testFile"));
        sampleData = new SampleData();
    }

    @Test
    public void testDefaultAnalysisIsTargetFrequencyAndPathogenicityFilters() {
        VariantFilter targetFilter = new TargetFilter();
        VariantFilter frequencyFilter = new FrequencyFilter(100f, false);
        VariantFilter pathogenicityFilter = new PathogenicityFilter(false);
        
        settings = settingsBuilder.build();
        
        Analysis expected = new Analysis(sampleData);
        expected.addStep(targetFilter);
        expected.addStep(frequencyFilter);
        expected.addStep(pathogenicityFilter);
        
        Analysis result = instance.analyse(sampleData, settings);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSpecifyingInheritanceModeAddsAnInheritanceFilter() {
        final ModeOfInheritance dominantInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;

        VariantFilter targetFilter = new TargetFilter();
        VariantFilter frequencyFilter = new FrequencyFilter(100f, false);
        VariantFilter pathogenicityFilter = new PathogenicityFilter(false);
        GeneFilter inheritanceFilter = new InheritanceFilter(dominantInheritance);
        
        settings = settingsBuilder.runFullAnalysis(false)
                .modeOfInheritance(dominantInheritance).build();
        
        Analysis expected = new Analysis(sampleData);
        expected.addStep(targetFilter);
        expected.addStep(frequencyFilter);
        expected.addStep(pathogenicityFilter);
        expected.addStep(inheritanceFilter);
        
        Analysis result = instance.analyse(sampleData, settings);
        assertThat(result, equalTo(expected));
        
    }
    
    @Test
    public void testSpecifyingOmimPrioritiserOnlyAddsOmimPrioritiser() {
        final ModeOfInheritance dominantInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;

        VariantFilter targetFilter = new TargetFilter();
        VariantFilter frequencyFilter = new FrequencyFilter(100f, false);
        VariantFilter pathogenicityFilter = new PathogenicityFilter(false);
        GeneFilter inheritanceFilter = new InheritanceFilter(dominantInheritance);
        Prioritiser omimPrioritiser = new NoneTypePrioritiser();
        
        settings = settingsBuilder.runFullAnalysis(false)
                .modeOfInheritance(dominantInheritance)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .build();
        
        Analysis expected = new Analysis(sampleData);
        expected.addStep(targetFilter);
        expected.addStep(frequencyFilter);
        expected.addStep(pathogenicityFilter);
        expected.addStep(inheritanceFilter);
        expected.addStep(omimPrioritiser);
        
        Analysis result = instance.analyse(sampleData, settings);
        assertThat(result, equalTo(expected));
        
    }
    
    @Test
    public void testSpecifyingPrioritiserAddsAnOmimAndTheSpecifiedPrioritiser() {
        final ModeOfInheritance dominantInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;

        VariantFilter targetFilter = new TargetFilter();
        VariantFilter frequencyFilter = new FrequencyFilter(100f, false);
        VariantFilter pathogenicityFilter = new PathogenicityFilter(false);
        GeneFilter inheritanceFilter = new InheritanceFilter(dominantInheritance);
        Prioritiser omimPrioritiser = new NoneTypePrioritiser();
        List<String> hpoIds = new ArrayList<>();
        hpoIds.add("HP:000001");
        hpoIds.add("HP:000002");
        hpoIds.add("HP:000003");
        Prioritiser phivePrioritiser = new NoneTypePrioritiser();
        
        settings = settingsBuilder.runFullAnalysis(false)
                .modeOfInheritance(dominantInheritance)
                .usePrioritiser(PriorityType.PHIVE_PRIORITY)
                .hpoIdList(hpoIds)
                .build();

        Analysis expected = new Analysis(sampleData);
        expected.addStep(targetFilter);
        expected.addStep(frequencyFilter);
        expected.addStep(pathogenicityFilter);
        expected.addStep(inheritanceFilter);
        expected.addStep(omimPrioritiser);
        expected.addStep(phivePrioritiser);
        
        Analysis result = instance.analyse(sampleData, settings);
        assertThat(result, equalTo(expected));        
    }
    
}
