/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.factories;

import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneFactoryTest {

    private GeneFactory instance;
    private List<VariantEvaluation> variantEvaluations;

    private static final String GENE1_GENE_SYMBOL = "GENE1";
    private static final int GENE1_GENE_ID = 123456;
    
    @Mock
    private VariantEvaluation offTargetVariantEvaluation;
    @Mock
    private VariantEvaluation firstGene1VariantEvaluation;
    @Mock
    private VariantEvaluation secondGene1VariantEvaluation;

    private static final String GENE2_GENE_SYMBOL = "GENE2";
    private static final int GENE2_GENE_ID = 654321;
    @Mock
    private VariantEvaluation gene2VariantEvaluation;
    
    private static final String GENE3_GENE_SYMBOL = "GENE3";
    private static final int GENE3_GENE_ID = 9999999;
    @Mock
    private VariantEvaluation gene3VariantEvaluation;    
    @Mock
    private VariantEvaluation variantEvaluationInTwoGeneRegions;


    @Before
    public void setUp() {
        variantEvaluations = new ArrayList<>();

        Mockito.when(offTargetVariantEvaluation.getGeneSymbol()).thenReturn(null);

        Mockito.when(firstGene1VariantEvaluation.getGeneSymbol()).thenReturn(GENE1_GENE_SYMBOL);
        Mockito.when(firstGene1VariantEvaluation.getEntrezGeneId()).thenReturn(GENE1_GENE_ID);

        Mockito.when(secondGene1VariantEvaluation.getGeneSymbol()).thenReturn(GENE1_GENE_SYMBOL);
        Mockito.when(secondGene1VariantEvaluation.getEntrezGeneId()).thenReturn(GENE1_GENE_ID);

        Mockito.when(gene2VariantEvaluation.getGeneSymbol()).thenReturn(GENE2_GENE_SYMBOL);
        Mockito.when(gene2VariantEvaluation.getEntrezGeneId()).thenReturn(GENE2_GENE_ID);
        
        //the variantEvaluation should only return the first gene symbol listed by Jannovar - see VariantEvaluationTest 
        Mockito.when(variantEvaluationInTwoGeneRegions.getGeneSymbol()).thenReturn(GENE3_GENE_SYMBOL);
        Mockito.when(variantEvaluationInTwoGeneRegions.getEntrezGeneId()).thenReturn(GENE3_GENE_ID);
        
        Mockito.when(gene3VariantEvaluation.getGeneSymbol()).thenReturn(GENE3_GENE_SYMBOL);
        Mockito.when(gene3VariantEvaluation.getEntrezGeneId()).thenReturn(GENE3_GENE_ID);
    }

    private Gene createNewGene(String geneSymbol, int geneId, VariantEvaluation variantEvaluation) {
        Gene gene = new Gene(geneSymbol, geneId);
        gene.addVariant(variantEvaluation);
        return gene;
    }
        
    @Test
    public void testGeneFactoryWillReturnAnEmptyResultFromAnEmptyInput() {
        List<VariantEvaluation> emptyVariantEvaluations = new ArrayList<>();
        List<Gene> emptyGenes = new ArrayList<>();

        List<Gene> result = GeneFactory.createGenes(emptyVariantEvaluations);
        assertThat(result, equalTo(emptyGenes));
    }

    @Test
    public void testOffTargetVariantReturnsNoGenes() {
        variantEvaluations.add(offTargetVariantEvaluation);

        List<Gene> emptyGenes = new ArrayList<>();

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        assertThat(result, equalTo(emptyGenes));
    }

    @Test
    public void testOneOnTargetVariantReturnsSingleGene() {

        variantEvaluations.add(firstGene1VariantEvaluation);

        List<Gene> genes = new ArrayList<>();
        Gene gene = createNewGene(GENE1_GENE_SYMBOL, GENE1_GENE_ID, firstGene1VariantEvaluation);
        genes.add(gene);

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        assertThat(result, equalTo(genes));
    }

    @Test
    public void testTwoVariantsInSameGeneReturnsSingleGene() {

        variantEvaluations.add(firstGene1VariantEvaluation);
        variantEvaluations.add(secondGene1VariantEvaluation);

        List<Gene> genes = new ArrayList<>();
        genes.add(createNewGene(GENE1_GENE_SYMBOL, GENE1_GENE_ID, firstGene1VariantEvaluation));

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        assertThat(result, equalTo(genes));

        Gene resultGene = result.get(0);
        assertThat(resultGene.getGeneSymbol(), equalTo(GENE1_GENE_SYMBOL));
        assertThat(resultGene.getNumberOfVariants(), equalTo(variantEvaluations.size()));
    }

    @Test
    public void testTwoVariantsInDifferentGeneReturnsTwoGenes() {

        Gene gene1 = createNewGene(GENE1_GENE_SYMBOL, GENE1_GENE_ID, firstGene1VariantEvaluation);
        variantEvaluations.add(firstGene1VariantEvaluation);

        Gene gene2 = createNewGene(GENE2_GENE_SYMBOL, GENE2_GENE_ID, gene2VariantEvaluation);
        variantEvaluations.add(gene2VariantEvaluation);

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);

        assertThat(result.contains(gene1), is(true));
        assertThat(result.contains(gene2), is(true));
    }

    @Test
    public void testVariantInTwoGeneRegionsReturnsSingleGene() {

        variantEvaluations.add(variantEvaluationInTwoGeneRegions);

        List<Gene> genes = new ArrayList<>();
        genes.add(createNewGene(GENE3_GENE_SYMBOL, GENE3_GENE_ID, variantEvaluationInTwoGeneRegions));

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        assertThat(result, equalTo(genes));

    }
    
    @Test
    public void testVariantInTwoGeneRegionsReturnsSingleGeneWithFirstGeneSymbol() {

        variantEvaluations.add(variantEvaluationInTwoGeneRegions);

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        
        Gene resultGene = result.get(0);
        System.out.println(resultGene);
        assertThat(resultGene.getGeneSymbol(), equalTo(GENE3_GENE_SYMBOL));
        assertThat(resultGene.getNumberOfVariants(), equalTo(variantEvaluations.size()));

    }
    
    @Test
    public void testThreeVariantsInTwoGeneRegionsReturnsTwoGenes() {
        
        variantEvaluations.add(variantEvaluationInTwoGeneRegions);

        Gene gene3 = createNewGene(GENE3_GENE_SYMBOL, GENE3_GENE_ID, gene3VariantEvaluation);
        variantEvaluations.add(gene3VariantEvaluation);

        Gene gene2 = createNewGene(GENE2_GENE_SYMBOL, GENE2_GENE_ID, gene2VariantEvaluation);
        variantEvaluations.add(gene2VariantEvaluation);        
        

        List<Gene> result = GeneFactory.createGenes(variantEvaluations);
        for (Gene gene : result) {
            System.out.println(gene);

        }
        assertThat(result.size(), equalTo(2));
        assertThat(result.contains(gene2), is(true));
        assertThat(result.contains(gene3), is(true));
    }

    @Test
    public void testCreateKnownGenes() {
        JannovarData jannovarData = TestFactory.buildDefaultJannovarData();
        Set<String> knownGeneSymbols = jannovarData.getTmByGeneSymbol().keySet();

        List<Gene> knownGenes = GeneFactory.createKnownGenes(jannovarData);
        assertThat(knownGenes.size(), equalTo(4));
        knownGenes.forEach(gene -> {
            assertThat(gene.getEntrezGeneID(), not(equalTo(0)));
            assertThat(gene.getGeneSymbol(), not(equalTo("")));
            assertThat(knownGeneSymbols.contains(gene.getGeneSymbol()), is(true));
        });
    }

    @Test
    public void testCreateKnownGeneIdentifiers() throws Exception {

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("2263", "FGFR2");
        expected.put("114814", "GNRHR2");
        expected.put("9939", "RBM8A");
        expected.put("6469", "SHH");

        JannovarData jannovarData = TestFactory.buildDefaultJannovarData();
        Map<String, String> knownGenes = GeneFactory.createKnownGeneIdentifiers(jannovarData);

        assertThat(knownGenes, equalTo(expected));
    }
}
