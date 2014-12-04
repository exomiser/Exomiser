/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filter.FilterType;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneTest {

    private Gene instance;

    private static final String GENE_SYMBOL_1 = "GENE1";
    private static final String GENE_SYMBOL_2 = "GENE2";

    @Mock
    private VariantEvaluation variantEvaluationGene1;
    @Mock
    private VariantEvaluation variantEvaluationGene2;

    private static final int BEFORE = -1;
    private static final int AFTER = 1;
    private static final int EQUAL = 0;

    @Before
    public void setUp() {
        Mockito.when(variantEvaluationGene1.getGeneSymbol()).thenReturn(GENE_SYMBOL_1);
        Mockito.when(variantEvaluationGene1.getEntrezGeneID()).thenReturn(123456);
        
        Mockito.when(variantEvaluationGene2.getGeneSymbol()).thenReturn(GENE_SYMBOL_2);
        Mockito.when(variantEvaluationGene2.getEntrezGeneID()).thenReturn(654321);

    }

    @Test
    public void testConstructorSetsInstanceVariables() {
        instance = new Gene(variantEvaluationGene1);

        List<VariantEvaluation> expectedVariantEvaluations = new ArrayList<>();
        expectedVariantEvaluations.add(variantEvaluationGene1);

        assertThat(instance.getGeneSymbol(), equalTo(variantEvaluationGene1.getGeneSymbol()));
        assertThat(instance.getEntrezGeneID(), equalTo(variantEvaluationGene1.getEntrezGeneID()));
        assertThat(instance.getVariantEvaluations(), equalTo(expectedVariantEvaluations));

        assertThat(instance.passedFilters(), is(false));
        assertThat(instance.getPriorityScoreMap().isEmpty(), is(true));

        assertThat(instance.getFilterScore(), equalTo(0f));
        assertThat(instance.getPriorityScore(), equalTo(0f));
        assertThat(instance.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual(){
        Gene gene1 = new Gene(variantEvaluationGene1);
        Gene gene2 = new Gene(variantEvaluationGene2);
                
        assertThat(gene1.compareTo(gene2), equalTo(BEFORE));
        assertThat(gene2.compareTo(gene1), equalTo(AFTER));
    }
        
    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByCombinedScore(){
        Gene gene1 = new Gene(variantEvaluationGene1);
        Gene gene2 = new Gene(variantEvaluationGene2);

        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);
        
        assertThat(gene1.compareTo(gene2), equalTo(AFTER));
        assertThat(gene2.compareTo(gene1), equalTo(BEFORE));        
    }
    
    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual(){
        Gene gene1 = new Gene(variantEvaluationGene1);
        Gene gene2 = new Gene(variantEvaluationGene1);
        
        assertThat(gene1.compareTo(gene2), equalTo(EQUAL));
        assertThat(gene2.compareTo(gene1), equalTo(EQUAL));    
    }
    
    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByCombinedScoresAreEqual(){
        Gene gene1 = new Gene(variantEvaluationGene1);
        Gene gene2 = new Gene(variantEvaluationGene1);
        
        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);
        
        System.out.println(gene1);
        System.out.println(gene2);
        
        assertThat(gene1.compareTo(gene2), equalTo(AFTER));
        assertThat(gene2.compareTo(gene1), equalTo(BEFORE));    
    }
}
