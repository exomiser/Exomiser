/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisTest {
    
    private Analysis instance;
    
    private SampleData sampleData;
    
    @Before
    public void setUp() {
        sampleData = new SampleData();
        instance = new Analysis(sampleData);
    }

    @Test
    public void testCanGetSampleData() {
        assertThat(instance.getSampleData(), equalTo(sampleData));
    }
    
    @Test
    public void modeOfInheritanceDefaultsToUnspecified() {
        assertThat(instance.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED));
    }
    
    @Test
    public void canSetModeOfInheritanceViaOptionalConstructor() {
        instance = new Analysis(sampleData, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(instance.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }
    
    @Test
    public void canGeneScoringModeDefaultsToRawScore() {
        assertThat(instance.getScoringMode(), equalTo(ScoringMode.RAW_SCORE));
    }
    
//    @Test
//    public void testCanGetSettings() {
//        assertThat(instance.getSettings(), equalTo(settings));
//    }
    
    @Test
    public void testCanAddVariantFilterAsAnAnalysisStep() {
        VariantFilter targetFilter = new TargetFilter();
        instance.addStep(targetFilter);
    }
    
    @Test
    public void testCanAddGeneFilterAsAnAnalysisStep() {
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        instance.addStep(inheritanceFilter);
    }
    
    @Test
    public void testCanAddPrioritiserAsAnAnalysisStep() {
        Prioritiser phive = new NoneTypePrioritiser();
        instance.addStep(phive);
    }
      
    @Test
    public void testGetAnalysisSteps_ReturnsEmptyListWhenNoStepsHaveBeedAdded() {
        List<AnalysisStep> steps = Collections.emptyList();
        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testGetAnalysisSteps_ReturnsListOfStepsAdded() {
        List<AnalysisStep> steps = new ArrayList<>();
        VariantFilter geneIdFilter = new EntrezGeneIdFilter(new HashSet<Integer>());
        Prioritiser noneType = new NoneTypePrioritiser();
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        VariantFilter targetFilter = new TargetFilter();
        
        steps.add(geneIdFilter);
        steps.add(noneType);
        steps.add(inheritanceFilter);
        steps.add(targetFilter);
        
        instance.addStep(geneIdFilter);
        instance.addStep(noneType);
        instance.addStep(inheritanceFilter);
        instance.addStep(targetFilter);

        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }
    
}
