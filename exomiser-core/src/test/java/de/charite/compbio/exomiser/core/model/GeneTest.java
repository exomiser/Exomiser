/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.TargetFilterResult;
import de.charite.compbio.exomiser.core.prioritisers.ExomeWalkerPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import jannovar.common.Genotype;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    private static final String GENE1_SYMBOL = "GENE1";
    private static final int GENE1_ENTREZ_GENE_ID = 1234567;

    private static final String GENE2_SYMBOL = "GENE2";
    private static final int GENE2_ENTREZ_GENE_ID = 654321;

    private static final Integer QUALITY = 2;
    private static final Integer READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;
    
    @Mock
    private Variant variant1;
    private VariantEvaluation variantEvaluation1Gene1;
    
    @Mock
    private Variant variant3;
    private VariantEvaluation variantEvaluation2Gene1;

    @Mock
    private Variant variant2;
    private VariantEvaluation variantEvaluationGene2;

    private static final int BEFORE = -1;
    private static final int AFTER = 1;
    private static final int EQUAL = 0;
    
    private static final FilterResult PASS_TARGET_FILTER_RESULT = new TargetFilterResult(1f, FilterResultStatus.PASS);
    private static final FilterResult FAIL_FREQUENCY_FILTER_RESULT = new FrequencyFilterResult(0.0f, FilterResultStatus.FAIL);

    @Before
    public void setUp() {
        
        GenotypeCall genotypeCall = new GenotypeCall(HETEROZYGOUS, QUALITY, READ_DEPTH);

        Mockito.when(variant1.getGeneSymbol()).thenReturn(GENE1_SYMBOL);
        Mockito.when(variant1.getEntrezGeneID()).thenReturn(GENE1_ENTREZ_GENE_ID);
        Mockito.when(variant1.getChromosomeAsByte()).thenReturn((byte) 1);
        Mockito.when(variant1.get_position()).thenReturn(1);
        Mockito.when(variant1.get_ref()).thenReturn("A");
        Mockito.when(variant1.get_alt()).thenReturn("T");
        Mockito.when(variant1.getGenotype()).thenReturn(genotypeCall);
        Mockito.when(variant1.getVariantPhredScore()).thenReturn(2.2f);
        Mockito.when(variant1.getVariantReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(variant1.getAnnotation()).thenReturn("variant annotations...");
        variantEvaluation1Gene1 = new VariantEvaluation(variant1);
        
        Mockito.when(variant3.getGeneSymbol()).thenReturn(GENE1_SYMBOL);
        Mockito.when(variant3.getEntrezGeneID()).thenReturn(GENE1_ENTREZ_GENE_ID);
        Mockito.when(variant3.getChromosomeAsByte()).thenReturn((byte) 1);
        Mockito.when(variant3.get_position()).thenReturn(2);
        Mockito.when(variant3.get_ref()).thenReturn("A");
        Mockito.when(variant3.get_alt()).thenReturn("TT");
        Mockito.when(variant3.getGenotype()).thenReturn(genotypeCall);
        Mockito.when(variant3.getVariantPhredScore()).thenReturn(2.2f);
        Mockito.when(variant3.getVariantReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(variant3.getAnnotation()).thenReturn("variant annotations...");
        variantEvaluation2Gene1 = new VariantEvaluation(variant3);
        
        Mockito.when(variant2.getGeneSymbol()).thenReturn(GENE2_SYMBOL);
        Mockito.when(variant2.getEntrezGeneID()).thenReturn(GENE2_ENTREZ_GENE_ID);
        Mockito.when(variant2.getChromosomeAsByte()).thenReturn((byte) 1);
        Mockito.when(variant2.get_position()).thenReturn(1);
        Mockito.when(variant2.get_ref()).thenReturn("A");
        Mockito.when(variant2.get_alt()).thenReturn("T");
        Mockito.when(variant2.getGenotype()).thenReturn(genotypeCall);
        Mockito.when(variant2.getVariantPhredScore()).thenReturn(2.2f);
        Mockito.when(variant2.getVariantReadDepth()).thenReturn(READ_DEPTH);
        Mockito.when(variant2.getAnnotation()).thenReturn("variant annotations...");
        variantEvaluationGene2 = new VariantEvaluation(variant2);
    }

    @Test
    public void testConstructorSetsInstanceVariables() {
        instance = new Gene(variantEvaluation1Gene1);

        List<VariantEvaluation> expectedVariantEvaluations = new ArrayList<>();
        expectedVariantEvaluations.add(variantEvaluation1Gene1);

        assertThat(instance.getGeneSymbol(), equalTo(variantEvaluation1Gene1.getGeneSymbol()));
        assertThat(instance.getEntrezGeneID(), equalTo(variantEvaluation1Gene1.getEntrezGeneID()));
        assertThat(instance.getVariantEvaluations(), equalTo(expectedVariantEvaluations));

        assertThat(instance.passedFilters(), is(true));
        assertThat(instance.getPriorityResults().isEmpty(), is(true));

        assertThat(instance.getFilterScore(), equalTo(0f));
        assertThat(instance.getPriorityScore(), equalTo(0f));
        assertThat(instance.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual(){
        Gene gene1 = new Gene(variantEvaluation1Gene1);
        Gene gene2 = new Gene(variantEvaluationGene2);
                
        assertThat(gene1.compareTo(gene2), equalTo(BEFORE));
        assertThat(gene2.compareTo(gene1), equalTo(AFTER));
    }
        
    @Test
    public void testGenesWithDifferentGeneSymbolsAreComparedByCombinedScore(){
        Gene gene1 = new Gene(variantEvaluation1Gene1);
        Gene gene2 = new Gene(variantEvaluationGene2);

        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);
        
        assertThat(gene1.compareTo(gene2), equalTo(AFTER));
        assertThat(gene2.compareTo(gene1), equalTo(BEFORE));        
    }
    
    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByGeneSymbolWhenScoresAreEqual(){
        Gene gene1 = new Gene(variantEvaluation1Gene1);
        Gene gene2 = new Gene(variantEvaluation1Gene1);
        
        assertThat(gene1.compareTo(gene2), equalTo(EQUAL));
        assertThat(gene2.compareTo(gene1), equalTo(EQUAL));    
    }
    
    @Test
    public void testGenesWithSameGeneSymbolsAreComparedByCombinedScore(){
        Gene gene1 = new Gene(variantEvaluation1Gene1);
        Gene gene2 = new Gene(variantEvaluation1Gene1);
        
        gene1.setCombinedScore(0.0f);
        gene2.setCombinedScore(1.0f);
        
        assertThat(gene1.compareTo(gene2), equalTo(AFTER));
        assertThat(gene2.compareTo(gene1), equalTo(BEFORE));    
    }
    
    @Test
    public void testPassesFiltersWhenNoFiltersHaveBeenApplied() {
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFiltersWhenVariantPassesFilter() {
        variantEvaluation1Gene1.addFilterResult(PASS_TARGET_FILTER_RESULT);
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testPassesFiltersWhenAtLeastOneVariantPassesFilter() {
        variantEvaluation1Gene1.addFilterResult(PASS_TARGET_FILTER_RESULT);
        instance = new Gene(variantEvaluation1Gene1);
        
        variantEvaluation2Gene1.addFilterResult(FAIL_FREQUENCY_FILTER_RESULT);
        instance.addVariant(variantEvaluationGene2);
        
        assertThat(instance.passedFilters(), is(true));
    }
    
    @Test
    public void testPassesFiltersWhenVariantFailsFilter() {
        variantEvaluation1Gene1.addFilterResult(FAIL_FREQUENCY_FILTER_RESULT);
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testGetPassedVariantEvaluationsIsEmptyWhenVariantFailsFilter() {
        variantEvaluation1Gene1.addFilterResult(FAIL_FREQUENCY_FILTER_RESULT);
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.getPassedVariantEvaluations().isEmpty(), is(true));
    }
    
    @Test
    public void testGetPassedVariantEvaluations() {
        variantEvaluation1Gene1.addFilterResult(PASS_TARGET_FILTER_RESULT);
        instance = new Gene(variantEvaluation1Gene1);
        
        variantEvaluation2Gene1.addFilterResult(FAIL_FREQUENCY_FILTER_RESULT);
        instance.addVariant(variantEvaluation2Gene1);
        
        List<VariantEvaluation> passedVariantEvaluations = new ArrayList<>();
        passedVariantEvaluations.add(variantEvaluation1Gene1);
        
        assertThat(instance.getPassedVariantEvaluations(), equalTo(passedVariantEvaluations));
    }
    
    @Test
    public void testGetNthVariant() {
        instance = new Gene(variantEvaluation1Gene1);
        instance.addVariant(variantEvaluation2Gene1);
        assertThat(instance.getNthVariant(1), equalTo(variantEvaluation2Gene1));
    }
    
    @Test
    public void testGetNthVariantWhenAskingForElementPastEndOfArray() {
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.getNthVariant(1000), equalTo(null));
    }
    
    @Test
    public void testCanAddAndRetrievePriorityScoreByPriorityType() {
        PriorityResult omimPriorityResult = new OMIMPriorityResult();
        PriorityType priorityType = PriorityType.OMIM_PRIORITY;
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.addPriorityResult(omimPriorityResult);
        instance.addPriorityResult(new ExomeWalkerPriorityResult(0.0d));
        //TODO: this is odd shouldn't it actually return the Object, not the value?
        assertThat(instance.getPriorityResult(priorityType), equalTo(omimPriorityResult));
    }
    
    @Test
    public void canInheritanceModes() {
        instance = new Gene(variantEvaluation1Gene1);
        assertThat(instance.getInheritanceModes(), notNullValue());
        assertThat(instance.getInheritanceModes().isEmpty(), is(true));
    }
    
    @Test
    public void canSetAndGetInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_DOMINANT);
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.setInheritanceModes(inheritanceModes);
        
        assertThat(instance.getInheritanceModes(), equalTo(inheritanceModes));
    }
    
    @Test
    public void isConsistentWithInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.X_RECESSIVE);
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isConsistentWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isConsistentWithDominant(), is(true));
        assertThat(instance.isConsistentWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(true));
    }
    
    @Test
    public void isConsistentWithDominantInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isConsistentWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isConsistentWithDominant(), is(true));
        assertThat(instance.isConsistentWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(false));
    }
    
    @Test
    public void isConsistentWithRecessiveInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isConsistentWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isConsistentWithDominant(), is(false));
        assertThat(instance.isConsistentWithRecessive(), is(true));
        assertThat(instance.isConsistentWithX(), is(false));
    }
    
    @Test
    public void isConsistentWithXRecessiveInheritanceModes() {
        Set inheritanceModes = EnumSet.of(ModeOfInheritance.X_RECESSIVE);
        
        instance = new Gene(variantEvaluation1Gene1);
        instance.setInheritanceModes(inheritanceModes);

        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(false));
        assertThat(instance.isConsistentWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));
        assertThat(instance.isConsistentWith(ModeOfInheritance.X_RECESSIVE), is(true));
        assertThat(instance.isConsistentWithDominant(), is(false));
        assertThat(instance.isConsistentWithRecessive(), is(false));
        assertThat(instance.isConsistentWithX(), is(true));
    }
}
