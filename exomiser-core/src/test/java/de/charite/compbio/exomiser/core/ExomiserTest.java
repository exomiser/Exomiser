/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package de.charite.compbio.exomiser.core;

import static de.charite.compbio.exomiser.core.Exomiser.NON_EXONIC_VARIANT_EFFECTS;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
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
import org.junit.Test;

/**
* Tests for Exomiser class.
*
* @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
*/
public class ExomiserTest {

    private Exomiser instance;
            
    private SettingsBuilder settingsBuilder;
    private Analysis analysis;
    
    private final ModeOfInheritance autosomal_dominant = ModeOfInheritance.AUTOSOMAL_DOMINANT;
    private final GeneticInterval interval = new GeneticInterval(2, 12345, 67890);
    
    @Before
    public void setUp() {       
        PriorityFactory stubPriorityFactory = new NoneTypePriorityFactoryStub();
        instance = new Exomiser(stubPriorityFactory);
        
        settingsBuilder = new SettingsBuilder().vcfFilePath(Paths.get("vcf"));
        analysis = new Analysis(); 
        analysis.setVcfPath(Paths.get("vcf"));
    }

    private void addDefaultVariantFilters(Analysis analysis) {
        analysis.addStep(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS));
        analysis.addStep(new FrequencyFilter(100f));
        analysis.addStep(new PathogenicityFilter(false));
    }

    @Test
    public void testDefaultAnalysisIsTargetFrequencyAndPathogenicityFilters() {
        ExomiserSettings settings = settingsBuilder.build();
        
        addDefaultVariantFilters(analysis);
        
        Analysis result = instance.setUpExomiserAnalysis(settings);
        assertThat(result, equalTo(analysis));
    }

    @Test
    public void testSpecifyingInheritanceModeAddsAnInheritanceFilter() {
        
        ExomiserSettings settings = settingsBuilder
                .modeOfInheritance(autosomal_dominant).build();
        
        addDefaultVariantFilters(analysis);
        analysis.setModeOfInheritance(autosomal_dominant);
        analysis.addStep(new InheritanceFilter(autosomal_dominant));
        
        Analysis result = instance.setUpExomiserAnalysis(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testCanMakeAllTypesOfFilter() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        Set<Integer> geneIdsToKeep = new HashSet<>();
        geneIdsToKeep.add(1);
        
        ExomiserSettings settings = settingsBuilder
                .modeOfInheritance(autosomal_dominant)
                .genesToKeepList(geneIdsToKeep)
                .removePathFilterCutOff(true)
                .removeKnownVariants(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                .build();

        analysis.addStep(new EntrezGeneIdFilter(geneIdsToKeep));
        analysis.addStep(new IntervalFilter(interval));
        analysis.addStep(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS));
        analysis.addStep(new QualityFilter(2f));
        analysis.addStep(new KnownVariantFilter());
        analysis.addStep(new FrequencyFilter(0.25f));
        analysis.addStep(new PathogenicityFilter(true));
        analysis.addStep(new InheritanceFilter(autosomal_dominant));
        analysis.setModeOfInheritance(autosomal_dominant);
        
        Analysis result = instance.setUpExomiserAnalysis(settings);
        assertThat(result, equalTo(analysis));
    }
    
    @Test
    public void testSpecifyingOmimPrioritiserOnlyAddsOmimPrioritiser() {

        ExomiserSettings settings = settingsBuilder
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .build();
        
        addDefaultVariantFilters(analysis);
        analysis.addStep(new OMIMPriority());
        
        Analysis result = instance.setUpExomiserAnalysis(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testSpecifyingPrioritiserAddsAnOmimAndTheSpecifiedPrioritiser() {

        List<String> hpoIds = new ArrayList<>();
        hpoIds.add("HP:000001");
        hpoIds.add("HP:000002");
        hpoIds.add("HP:000003");
        
        ExomiserSettings settings = settingsBuilder
                .usePrioritiser(PriorityType.PHIVE_PRIORITY)
                .hpoIdList(hpoIds)
                .build();
        
        analysis.setHpoIds(hpoIds);
        addDefaultVariantFilters(analysis);
        analysis.addStep(new OMIMPriority());
        analysis.addStep(new NoneTypePrioritiser());
        System.out.println(analysis);
        
        Analysis result = instance.setUpExomiserAnalysis(settings);
        assertThat(result, equalTo(analysis));        
    }
    
}
