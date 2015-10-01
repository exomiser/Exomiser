/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.CaddDao;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.NcdsDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.dao.RegulatoryFeatureDao;
import de.charite.compbio.exomiser.core.dao.TadDao;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.VariantEvaluation.VariantBuilder;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.NcdsScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import static de.charite.compbio.jannovar.annotation.VariantEffect.REGULATORY_REGION_VARIANT;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class VariantDataServiceImplTest {

    @InjectMocks
    private VariantDataServiceImpl instance;
    @Mock
    private FrequencyDao mockFrequencyDao;
    @Mock
    private PathogenicityDao mockPathogenicityDao;
    @Mock
    private NcdsDao mockNcdsDao;
    @Mock
    private CaddDao mockCaddDao;
    @Mock
    private RegulatoryFeatureDao mockRegulatoryFeatureDao;
    @Mock
    private TadDao mockTadDao;

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImplTest.class);

    private static final PathogenicityData PATH_DATA = new PathogenicityData(new PolyPhenScore(1), new MutationTasterScore(1), new SiftScore(0));
    private static final FrequencyData FREQ_DATA = new FrequencyData(new RsId(1234567), new Frequency(100.0f, FrequencySource.ESP_AFRICAN_AMERICAN));
    private static final PathogenicityData CADD_DATA = new PathogenicityData(new CaddScore(1));
    
    private VariantEvaluation variant;
    private static final VariantBuilder variantBuilder = new VariantBuilder(1, 1, "A", "T");

    @Before
    public void setUp() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        Map<String, Gene> allGenes = Collections.EMPTY_MAP;
        Mockito.when(mockPathogenicityDao.getPathogenicityData(variant)).thenReturn(PATH_DATA);
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(FREQ_DATA);
        Mockito.when(mockCaddDao.getPathogenicityData(variant)).thenReturn(CADD_DATA);
        Mockito.when(mockRegulatoryFeatureDao.getRegulatoryFeatureData(variant)).thenReturn(REGULATORY_REGION_VARIANT);
    }

    private static VariantEvaluation buildVariantOfType(VariantEffect variantEffect) {
        return variantBuilder.variantEffect(variantEffect).build();
    }

    @Test
    public void testInstanceIsNotNull() {
        assertThat(instance, notNullValue());
    }

    /*
     * Ignore until sort out how VariantDataService should behave for CADD vs
     * PathogenictyFilter swithc
     */
    @Test
    public void serviceReturnsPathogenicityDataForVariant() {
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT));
        assertThat(result, equalTo(PATH_DATA));
    }
    
    @Test
    public void serviceReturnsEmptyPathogenicityDataForVariantWhenNoSourcesAreDefined() {
        PathogenicityData result = instance.getVariantPathogenicityData(variant, Collections.emptySet());
        assertThat(result, equalTo(new PathogenicityData()));
    }

    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.POLYPHEN));
        assertThat(result, equalTo(new PathogenicityData(new PolyPhenScore(1f))));
    }

    @Test
    public void serviceReturnsCaddDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD));
        assertThat(result, equalTo(CADD_DATA));
    }

    @Test
    public void serviceReturnsCaddAndStandardMissenseDescriptorDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.POLYPHEN));
        
        assertThat(result, equalTo(new PathogenicityData(new PolyPhenScore(1f), new CaddScore(1f))));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = new PathogenicityData(new NcdsScore(1f));
        Mockito.when(mockNcdsDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.NCDS));
        assertThat(result, equalTo(expectedNcdsData));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForNonCodingNonRegulatoryVariant() {
        variant = buildVariantOfType(VariantEffect.SPLICE_REGION_VARIANT);
        //Test that the NCDS DAO is only called whe the variant type is of the type NCDS is trained against. 
        Mockito.when(mockNcdsDao.getPathogenicityData(variant)).thenReturn(new PathogenicityData(new NcdsScore(1f)));
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.NCDS));
        assertThat(result, equalTo(new PathogenicityData()));
    }
    
    @Test
    public void serviceReturnsCaddAndNonCodingScoreForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = new PathogenicityData(new CaddScore(1f), new NcdsScore(1f));
        Mockito.when(mockNcdsDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.NCDS));
        assertThat(result, equalTo(expectedNcdsData));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataForVariantWhenNoSourcesAreDefined() {
        FrequencyData result = instance.getVariantFrequencyData(variant, Collections.emptySet());
        assertThat(result, equalTo(new FrequencyData(new RsId(1234567))));
    }

    @Test
    public void serviceReturnsFrequencyDataForVariant() {
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.allOf(FrequencySource.class));
        assertThat(result, equalTo(FREQ_DATA));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariant() {
        FrequencyData frequencyData = new FrequencyData(new RsId(234567), new Frequency(1f, FrequencySource.ESP_AFRICAN_AMERICAN), new Frequency(1f, FrequencySource.ESP_EUROPEAN_AMERICAN));
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);
        
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN));
        assertThat(result, equalTo(new FrequencyData(new RsId(234567), new Frequency(1f, FrequencySource.ESP_AFRICAN_AMERICAN))));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWhenSpecifiedFrequencyDataIsUnavailable() {
        FrequencyData frequencyData = new FrequencyData();
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);
                
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.LOCAL));
        assertThat(result, equalTo(new FrequencyData()));
    }

    @Test
    public void serviceReturnsOriginalVariantEffectForCodingVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        VariantEffect result = instance.getVariantRegulatoryFeatureData(variant);
        assertThat(result, equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    public void serviceReturnsRegulatoryFeatureVariantEffectForIntergenicVariant() {
        variant = buildVariantOfType(VariantEffect.INTERGENIC_VARIANT);
        VariantEffect result = instance.getVariantRegulatoryFeatureData(variant);
        assertThat(result, equalTo(REGULATORY_REGION_VARIANT));
    }

    @Test
    public void serviceReturnsRegulatoryFeaturelVariantEffectForUpstreamGeneVariant() {
        variant = buildVariantOfType(VariantEffect.UPSTREAM_GENE_VARIANT);
        VariantEffect result = instance.getVariantRegulatoryFeatureData(variant);
        assertThat(result, equalTo(REGULATORY_REGION_VARIANT));
    }
    
    @Test
    public void serviceReturnsEmptyListForVariantNotInTad(){
        Mockito.when(mockTadDao.getGenesInTad(variant)).thenReturn(Collections.emptyList());

        List<String> genesFromTad = instance.getGenesInTad(variant);
        assertThat(genesFromTad.isEmpty(), is(true));
    }
    
    @Test
    public void serviceReturnsEmptyListForVariantInTad(){
        List<String> geneSymbols = Arrays.asList("GENE1","GENE2");
        Mockito.when(mockTadDao.getGenesInTad(variant)).thenReturn(geneSymbols);

        List<String> genesFromTad = instance.getGenesInTad(variant);
        assertThat(genesFromTad, equalTo(geneSymbols));
    }

}
