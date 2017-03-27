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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation.Builder;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
    private RemmDao mockRemmDao;
    @Mock
    private CaddDao mockCaddDao;
    @Mock
    private RegulatoryFeatureDao mockRegulatoryFeatureDao;
    @Mock
    private TadDao mockTadDao;

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImplTest.class);

    private static final PathogenicityData PATH_DATA = new PathogenicityData(PolyPhenScore.valueOf(1), MutationTasterScore.valueOf(1), SiftScore.valueOf(0));
    private static final FrequencyData FREQ_DATA = new FrequencyData(RsId.valueOf(1234567), Frequency.valueOf(100.0f, FrequencySource.ESP_AFRICAN_AMERICAN));
    private static final PathogenicityData CADD_DATA = new PathogenicityData(CaddScore.valueOf(1));
    private static final VariantEffect REGULATORY_REGION = VariantEffect.REGULATORY_REGION_VARIANT;

    private VariantEvaluation variant;
    private static final Builder variantBuilder = new Builder(1, 1, "A", "T");

    @Before
    public void setUp() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        Map<String, Gene> allGenes = Collections.EMPTY_MAP;
        Mockito.when(mockPathogenicityDao.getPathogenicityData(variant)).thenReturn(PATH_DATA);
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(FREQ_DATA);
        Mockito.when(mockCaddDao.getPathogenicityData(variant)).thenReturn(CADD_DATA);
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
        assertThat(result, equalTo(new PathogenicityData(PolyPhenScore.valueOf(1f))));
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
        
        assertThat(result, equalTo(new PathogenicityData(PolyPhenScore.valueOf(1f), CaddScore.valueOf(1f))));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = new PathogenicityData(RemmScore.valueOf(1f));
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedNcdsData));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForNonCodingNonRegulatoryVariant() {
        variant = buildVariantOfType(VariantEffect.SPLICE_REGION_VARIANT);
        //Test that the REMM DAO is only called whe the variant type is of the type REMM is trained against.
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(new PathogenicityData(RemmScore.valueOf(1f)));
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(new PathogenicityData()));
    }
    
    @Test
    public void serviceReturnsCaddAndNonCodingScoreForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = new PathogenicityData(CaddScore.valueOf(1f), RemmScore.valueOf(1f));
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedNcdsData));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWithRsIdForVariantWhenNoSourcesAreDefined() {
        FrequencyData result = instance.getVariantFrequencyData(variant, Collections.emptySet());
        assertThat(result, equalTo(new FrequencyData(RsId.valueOf(1234567))));
    }

    @Test
    public void serviceReturnsFrequencyDataForVariant() {
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.allOf(FrequencySource.class));
        assertThat(result, equalTo(FREQ_DATA));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariant() {
        FrequencyData frequencyData = new FrequencyData(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN), Frequency.valueOf(1f, FrequencySource.ESP_EUROPEAN_AMERICAN));
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);
        
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN));
        assertThat(result, equalTo(new FrequencyData(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN))));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWhenSpecifiedFrequencyDataIsUnavailable() {
        Mockito.when(mockFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.EMPTY_DATA);
                
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.LOCAL));
        assertThat(result, equalTo(FrequencyData.EMPTY_DATA));
    }

    @Test
    public void serviceReturnsRegulatoryFeatures() {
        List<RegulatoryFeature> regulatoryFeatures = Arrays.asList(new RegulatoryFeature(1, 10, 100, RegulatoryFeature.FeatureType.ENHANCER));
        Mockito.when(mockRegulatoryFeatureDao.getRegulatoryFeatures()).thenReturn(regulatoryFeatures);

        List<RegulatoryFeature> result = instance.getRegulatoryFeatures();
        assertThat(result, equalTo(regulatoryFeatures));
    }

    @Test
    public void serviceReturnsTopologicalDomains(){
        List<TopologicalDomain> tads = Arrays.asList(new TopologicalDomain(1, 1, 2, Collections.emptyMap()));
        Mockito.when(mockTadDao.getAllTads()).thenReturn(tads);

        List<TopologicalDomain> topologicalDomains = instance.getTopologicallyAssociatedDomains();
        assertThat(topologicalDomains, equalTo(tads));
    }

}
