/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.genome.dao.CaddDao;
import org.monarchinitiative.exomiser.core.genome.dao.FrequencyDao;
import org.monarchinitiative.exomiser.core.genome.dao.PathogenicityDao;
import org.monarchinitiative.exomiser.core.genome.dao.RemmDao;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class VariantDataServiceImplTest {

    private VariantDataServiceImpl instance;
    @Mock
    private FrequencyDao defaultFrequencyDao;
    @Mock
    private FrequencyDao localFrequencyDao;
    @Mock
    private PathogenicityDao mockPathogenicityDao;
    @Mock
    private RemmDao mockRemmDao;
    @Mock
    private CaddDao mockCaddDao;

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImplTest.class);

    private static final ClinVarData PATH_CLINVAR_DATA = ClinVarData.builder().alleleId("12345")
            .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
            .build();
    private static final PathogenicityData PATH_DATA = PathogenicityData.of(
            PATH_CLINVAR_DATA,
            PolyPhenScore.valueOf(1),
            MutationTasterScore.valueOf(1),
            SiftScore.valueOf(0)
    );

    private static final FrequencyData FREQ_DATA = FrequencyData.of(
            RsId.valueOf(1234567),
            Frequency.valueOf(100.0f, FrequencySource.ESP_AFRICAN_AMERICAN)
    );

    private static final PathogenicityData CADD_DATA = PathogenicityData.of(CaddScore.valueOf(1));

    private static final VariantEffect REGULATORY_REGION = VariantEffect.REGULATORY_REGION_VARIANT;

    private VariantEvaluation variant;

    @Before
    public void setUp() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        Mockito.when(mockPathogenicityDao.getPathogenicityData(variant)).thenReturn(PATH_DATA);
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FREQ_DATA);
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
        Mockito.when(mockCaddDao.getPathogenicityData(variant)).thenReturn(CADD_DATA);

        instance = VariantDataServiceImpl.builder()
                .defaultFrequencyDao(defaultFrequencyDao)
                .localFrequencyDao(localFrequencyDao)
                .pathogenicityDao(mockPathogenicityDao)
                .caddDao(mockCaddDao)
                .remmDao(mockRemmDao)
                .build();
    }

    private VariantEvaluation buildVariantOfType(VariantEffect variantEffect) {
        return VariantEvaluation.builder(1, 1, "A", "T").variantEffect(variantEffect).build();
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
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.POLYPHEN));
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, PolyPhenScore.valueOf(1f))));
    }

    @Test
    public void serviceReturnsCaddDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD));
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, CADD_DATA.getCaddScore())));
    }

    @Test
    public void serviceReturnsCaddAndStandardMissenseDescriptorDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.POLYPHEN));

        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, PolyPhenScore.valueOf(1f), CaddScore.valueOf(1f))));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = PathogenicityData.of(RemmScore.valueOf(1f));
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedNcdsData));
    }
    
    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForNonCodingNonRegulatoryVariant() {
        variant = buildVariantOfType(VariantEffect.SPLICE_REGION_VARIANT);
        //Test that the REMM DAO is only called when the variant type is of the type REMM is trained against.
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA)));
    }
    
    @Test
    public void serviceReturnsCaddAndNonCodingScoreForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = PathogenicityData.of(CaddScore.valueOf(1f), RemmScore.valueOf(1f));
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedNcdsData));
    }

    @Test
    public void serviceQueryForSynonymousVariantReturnsEmptyPathogenicityData() {
        variant = buildVariantOfType(VariantEffect.SYNONYMOUS_VARIANT);
        // Even if there is pathogenicity data it's likely wrong for a synonymous variant, so check we ignore it
        // This will cause a UnnecessaryStubbingException to be thrown as the result of this stubbing is ignored, but
        // we're trying to test for exactly that functionality we're running with the MockitoJUnitRunner.Silent.class
        Mockito.when(mockPathogenicityDao.getPathogenicityData(variant)).thenReturn(PathogenicityData.of(MutationTasterScore.valueOf(1f)));
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.MUTATION_TASTER));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWithRsIdForVariantWhenNoSourcesAreDefined() {
        FrequencyData result = instance.getVariantFrequencyData(variant, Collections.emptySet());
        assertThat(result, equalTo(FrequencyData.of(RsId.valueOf(1234567))));
    }

    @Test
    public void serviceReturnsFrequencyDataForVariant() {
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.allOf(FrequencySource.class));
        assertThat(result, equalTo(FREQ_DATA));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariant() {
        FrequencyData frequencyData = FrequencyData.of(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN), Frequency
                .valueOf(1f, FrequencySource.ESP_EUROPEAN_AMERICAN));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN));
        assertThat(result, equalTo(FrequencyData.of(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN))));
    }

    @Test
    public void serviceReturnsLocalFrequencyDataForVariant() {
        FrequencyData localFrequencyData = FrequencyData.of(RsId.empty(), Frequency.valueOf(2f, FrequencySource.LOCAL));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.LOCAL));
        assertThat(result, equalTo(localFrequencyData));
    }

    @Test
    public void serviceReturnsLocalFrequencyDataForVariantWithRsIdIfKnown() {
        FrequencyData localFrequencyData = FrequencyData.of(RsId.empty(), Frequency.valueOf(2f, FrequencySource.LOCAL));
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.LOCAL));
        assertThat(result, equalTo(FrequencyData.of(RsId.valueOf(1234567), Frequency.valueOf(2f, FrequencySource.LOCAL))));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariantIncludingLocalData() {
        FrequencyData frequencyData = FrequencyData.of(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN), Frequency
                .valueOf(1f, FrequencySource.ESP_EUROPEAN_AMERICAN));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);

        FrequencyData localFrequencyData = FrequencyData.of(RsId.empty(), Frequency.valueOf(2f, FrequencySource.LOCAL));
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.ESP_AFRICAN_AMERICAN, FrequencySource.LOCAL));
        assertThat(result, equalTo(FrequencyData.of(RsId.valueOf(234567), Frequency.valueOf(1f, FrequencySource.ESP_AFRICAN_AMERICAN), Frequency
                .valueOf(2f, FrequencySource.LOCAL))));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWhenSpecifiedFrequencyDataIsUnavailable() {
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
                
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(FrequencySource.LOCAL));
        assertThat(result, equalTo(FrequencyData.empty()));
    }

}
