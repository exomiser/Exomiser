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

    private static final String GENE_SYMBOL = "GENE1";
    @Mock
    private VariantEvaluation variantEvaluation;


    @Before
    public void setUp() {
        Mockito.when(variantEvaluation.getGeneSymbol()).thenReturn(GENE_SYMBOL);
        Mockito.when(variantEvaluation.getEntrezGeneID()).thenReturn(123456);

    }

    @Test
    public void testConstructorSetsInstanceVariables() {
        instance = new Gene(variantEvaluation);

        List<VariantEvaluation> expectedVariantEvaluations = new ArrayList<>();
        expectedVariantEvaluations.add(variantEvaluation);

        assertThat(instance.getGeneSymbol(), equalTo(variantEvaluation.getGeneSymbol()));
        assertThat(instance.getEntrezGeneID(), equalTo(variantEvaluation.getEntrezGeneID()));
        assertThat(instance.getVariantEvaluations(), equalTo(expectedVariantEvaluations));

        assertThat(instance.passedFilters(), is(false));
        assertThat(instance.getPriorityScoreMap().isEmpty(), is(true));

        assertThat(instance.getFilterScore(), equalTo(0f));
        assertThat(instance.getPriorityScore(), equalTo(0f));
        assertThat(instance.getCombinedScore(), equalTo(0f));
    }

}
