/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DaoTestConfig.class)
@Sql(scripts = {"file:src/test/resources/sql/create_pathogenicity.sql","file:src/test/resources/sql/pathogenicityDaoTestData.sql"})
public class DefaultPathogenicityDaoTest {
    
    @Autowired
    private DefaultPathogenicityDao instance;
    
    @Mock
    Variant nonMissenseVariant;
    @Mock
    Variant missenseVariantNotInDatabase;
    @Mock
    Variant missenseVariantInDatabase;
    
    private static final PathogenicityData NO_PATH_DATA = new PathogenicityData(null, null, null, null);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        Mockito.when(nonMissenseVariant.getVariantEffect()).thenReturn(VariantEffect.DOWNSTREAM_GENE_VARIANT);
        
        GenomePosition invalidPos = new GenomePosition(HG19RefDictBuilder.build(), Strand.FWD, 0, 0, PositionType.ONE_BASED);
        GenomeChange invalidGenomeChange = new GenomeChange(invalidPos, "T", "G");
        Mockito.when(missenseVariantNotInDatabase.getVariantEffect()).thenReturn(VariantEffect.MISSENSE_VARIANT);
//        Mockito.when(missenseVariantNotInDatabase.getGenomePosition()).thenReturn(invalidPos);
//        Mockito.when(missenseVariantNotInDatabase.getGenomeChange()).thenReturn(invalidGenomeChange);
        Mockito.when(missenseVariantNotInDatabase.getChromosome()).thenReturn(0);
        Mockito.when(missenseVariantNotInDatabase.getPosition()).thenReturn(0);
        Mockito.when(missenseVariantNotInDatabase.getRef()).thenReturn("T");
        Mockito.when(missenseVariantNotInDatabase.getAlt()).thenReturn("G");
        
        GenomePosition validPos = new GenomePosition(HG19RefDictBuilder.build(), Strand.FWD, 10, 123256215, PositionType.ONE_BASED);
        GenomeChange validGenomeChange = new GenomeChange(validPos, "T", "G");
        Mockito.when(missenseVariantInDatabase.getVariantEffect()).thenReturn(VariantEffect.MISSENSE_VARIANT);
//        Mockito.when(missenseVariantInDatabase.getGenomePosition()).thenReturn(validPos);
//        Mockito.when(missenseVariantInDatabase.getGenomeChange()).thenReturn(validGenomeChange);
        Mockito.when(missenseVariantInDatabase.getChromosome()).thenReturn(10);
        Mockito.when(missenseVariantInDatabase.getPosition()).thenReturn(123256215);
        Mockito.when(missenseVariantInDatabase.getRef()).thenReturn("T");
        Mockito.when(missenseVariantInDatabase.getAlt()).thenReturn("G");
        
    }

    @Test
    public void testNonMissenseVariantReturnsAnEmptyPathogenicityData() {
        PathogenicityData result = instance.getPathogenicityData(nonMissenseVariant);
               
        assertThat(result, equalTo(NO_PATH_DATA));
        assertThat(result.hasPredictedScore(), is(false));     
    }
    
    @Test
    public void testMissenseVariantReturnsAnEmptyPathogenicityDataWhenNotInDatabase() {
        PathogenicityData result = instance.getPathogenicityData(missenseVariantNotInDatabase);
                
        assertThat(result, equalTo(NO_PATH_DATA));
        assertThat(result.hasPredictedScore(), is(false));  
    }
    
    @Test
    public void testMissenseVariantReturnsPathogenicityDataWhenInDatabase() {
        PathogenicityData result = instance.getPathogenicityData(missenseVariantInDatabase);
                
        assertThat(result.hasPredictedScore(), is(true));
        assertThat(result.getSiftScore(), equalTo(new SiftScore(0f)));
        assertThat(result.getCaddScore(), equalTo(new CaddScore(23.7f)));
        assertThat(result.getPolyPhenScore(), equalTo(new PolyPhenScore(0.998f)));
        assertThat(result.getMutationTasterScore(), equalTo(new MutationTasterScore(1.0f)));
    }
    
}
