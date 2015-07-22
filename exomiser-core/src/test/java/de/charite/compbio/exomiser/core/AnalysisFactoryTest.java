/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.AnalysisFactory.AnalysisBuilder;
import de.charite.compbio.exomiser.core.factories.SampleDataFactoryStub;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.filters.PassAllVariantEffectsFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.BaseMatcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisFactoryTest {
    
    private AnalysisFactory instance;
    private PriorityFactory priorityFactory;
    
    private AnalysisBuilder analysisBuilder;
    private List<AnalysisStep> steps;
    
    private List<String> hpoIds;

    
    @Before
    public void setUp() {
        VariantDataService stubVariantDataService = new VariantDataServiceStub();
        priorityFactory = new NoneTypePriorityFactoryStub();
        
        instance = new AnalysisFactory(null, stubVariantDataService, priorityFactory);
        
        hpoIds = Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055");
        steps = new ArrayList<>();
        
        analysisBuilder = instance.getAnalysisBuilder();
        analysisBuilder.hpoIds(hpoIds);
    }
        
    private List<AnalysisStep> analysisSteps() {
        return analysisBuilder.build().getAnalysisSteps();
    }
    
    @Test
    public void testCanMakeFullAnalysisRunner() {
        SimpleAnalysisRunner analysisRunner = instance.getFullAnalysisRunner();
        assertThat(analysisRunner, notNullValue());
    }

    @Test
    public void testCanMakeSparseAnalysisRunner() {
        SparseAnalysisRunner analysisRunner = instance.getSparseAnalysisRunner();
        assertThat(analysisRunner, notNullValue());
    }

    @Test
    public void testCanMakePassOnlyAnalysisRunner() {
        PassOnlyAnalysisRunner analysisRunner = instance.getPassOnlyAnalysisRunner();
        assertThat(analysisRunner, notNullValue());
    }
    
    @Test
    public void testCanSpecifyOmimPrioritiser() {
        steps.add(priorityFactory.makeOmimPrioritiser());
        
        analysisBuilder.addOmimPrioritiser();
        
        assertThat(analysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanSpecifyPhivePrioritiser() {
        steps.add(priorityFactory.makePhivePrioritiser(hpoIds));
        
        analysisBuilder.addPhivePrioritiser();
        
        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanSpecifyHiPhivePrioritiser_noOptions() {
        steps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, new HiPhiveOptions()));
        
        analysisBuilder.addHiPhivePrioritiser();

        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanSpecifyHiPhivePrioritiser_withOptions() {
        HiPhiveOptions options = new HiPhiveOptions("DISEASE:123", "GENE1", "human,mouse,fish,ppi");
        
        steps.add(priorityFactory.makeHiPhivePrioritiser(hpoIds, options));
        
        analysisBuilder.addHiPhivePrioritiser(options);

        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanSpecifyPhenixPrioritiser() {
        steps.add(priorityFactory.makePhenixPrioritiser(hpoIds));
        
        analysisBuilder.addPhenixPrioritiser();
        
        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanSpecifyExomeWalkerPrioritiser() {
        List<Integer> seedGenes = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        steps.add(priorityFactory.makeExomeWalkerPrioritiser(seedGenes));
        
        analysisBuilder.addExomeWalkerPrioritiser(seedGenes);
        
        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanSpecifyTwoPrioritisers() {
        steps.add(priorityFactory.makeOmimPrioritiser());
        steps.add(priorityFactory.makePhivePrioritiser(hpoIds));

        analysisBuilder.addOmimPrioritiser();
        analysisBuilder.addPhivePrioritiser();
       
        assertThat(analysisSteps(), equalTo(steps));
    }
    
    @Test
    public void testCanAddFilterStep() {
        AnalysisStep filter = new PassAllVariantEffectsFilter();
        steps.add(filter);
        
        analysisBuilder.addAnalysisStep(filter);
        
        assertThat(analysisSteps(), equalTo(steps));
    }
}
