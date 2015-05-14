/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleGeneFilterRunnerTest {
    
    private SimpleGeneFilterRunner instance;
    
    private List<GeneFilter> filters;
    
    private List<Gene> genes;
    private Gene passGene;
    private Gene failGene;
    
    
    @Before
    public void setUp() {
        instance = new SimpleGeneFilterRunner();
        
        passGene = new Gene("GENE1", 12345);
        failGene = new Gene("GENE2", 56789);
        
        filters = new ArrayList<>();
        
        genes = new ArrayList<>();
        genes.add(passGene);
        genes.add(failGene);
    }

    @Test
    public void testRun() {
        setUpForInheritanceModeFiltering();
        
        instance.run(filters, genes);
        
        assertThat(passGene.passedFilters(), is (true));
        assertThat(failGene.passedFilters(), is (false));
    }
    
    @Test
    public void testRun_appliesFilterResultToVariantsInGene() {
        setUpForInheritanceModeFiltering();
       
        VariantEvaluation passGeneVariant1 = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        VariantEvaluation passGeneVariant2 = new VariantEvaluation.VariantBuilder(1, 2, "G", "T").build();
        
        assertThat(passGeneVariant1.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
        assertThat(passGeneVariant2.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
        
        passGene.addVariant(passGeneVariant1);
        passGene.addVariant(passGeneVariant2);
        
        VariantEvaluation failGeneVariant1 = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").build();
        VariantEvaluation failGeneVariant2 = new VariantEvaluation.VariantBuilder(1, 2, "G", "T").build();
        
        assertThat(failGeneVariant1.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
        assertThat(failGeneVariant2.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
        
        failGene.addVariant(failGeneVariant1);
        failGene.addVariant(failGeneVariant2);
        
        instance.run(filters, genes);
        
        assertThat(passGene.passedFilters(), is (true));
        assertThat(passGeneVariant1.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
        assertThat(passGeneVariant2.passedFilter(FilterType.INHERITANCE_FILTER), is(true));
        
        assertThat(failGene.passedFilters(), is (false));
        assertThat(failGeneVariant1.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
        assertThat(failGeneVariant2.passedFilter(FilterType.INHERITANCE_FILTER), is(false));
    }

    private void setUpForInheritanceModeFiltering() {
        ModeOfInheritance passMode = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        ModeOfInheritance failMode = ModeOfInheritance.AUTOSOMAL_RECESSIVE;
        
        filters.add(new InheritanceFilter(passMode));
        
        passGene.setInheritanceModes(EnumSet.of(passMode));
        failGene.setInheritanceModes(EnumSet.of(failMode));
    }
    
}
