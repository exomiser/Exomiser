/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VariantDataServiceImplTest {

    private VariantDataServiceImpl instance;
    @Mock
    private ClinVarDao clinVarDao;
    @Mock
    private FrequencyDao defaultFrequencyDao;
    @Mock
    private FrequencyDao localFrequencyDao;
    @Mock
    private PathogenicityDao defaultPathogenicityDao;
    @Mock
    private RemmDao mockRemmDao;
    @Mock
    private CaddDao mockCaddDao;

    @Mock
    private FrequencyDao svFrequencyDao;
    @Mock
    private PathogenicityDao svPathogenicityDao;

    private static final ClinVarData PATH_CLINVAR_DATA = ClinVarData.builder().variationId("12345")
            .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
            .build();
    private static final PathogenicityData PATH_DATA = PathogenicityData.of(
            PATH_CLINVAR_DATA,
            PolyPhenScore.of(1),
            MutationTasterScore.of(1),
            SiftScore.of(0)
    );

    private static final FrequencyData FREQ_DATA = FrequencyData.of("rs1234567", Frequency.of(ESP_AA, 100.0f));

    private static final PathogenicityData CADD_DATA = PathogenicityData.of(CaddScore.of(15f));

    private static final VariantEffect REGULATORY_REGION = VariantEffect.REGULATORY_REGION_VARIANT;

    private VariantEvaluation variant;

    @BeforeEach
    public void setUp() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        Mockito.when(clinVarDao.getClinVarData(variant)).thenReturn(PATH_CLINVAR_DATA);
        Mockito.when(defaultPathogenicityDao.getPathogenicityData(variant)).thenReturn(PATH_DATA);
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FREQ_DATA);
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
        Mockito.when(mockCaddDao.getPathogenicityData(variant)).thenReturn(CADD_DATA);

        Mockito.when(svFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
        Mockito.when(svPathogenicityDao.getPathogenicityData(variant)).thenReturn(PathogenicityData.empty());

        instance = VariantDataServiceImpl.builder()
                .clinVarDao(clinVarDao)
                .defaultFrequencyDao(defaultFrequencyDao)
                .localFrequencyDao(localFrequencyDao)
                .defaultPathogenicityDao(defaultPathogenicityDao)
                .caddDao(mockCaddDao)
                .remmDao(mockRemmDao)
//                .svFrequencyDao(svFrequencyDao)
//                .svPathogenicityDao(svPathogenicityDao)
                .build();
    }

    private VariantEvaluation buildVariantOfType(VariantEffect variantEffect) {
        return TestFactory.variantBuilder(1, 1, "A", "T").variantEffect(variantEffect).build();
    }

    @Test
    public void testInstanceIsNotNull() {
        assertThat(instance, notNullValue());
    }

    @Test
    public void serviceReturnsPathogenicityDataForVariant() {
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT));
        assertThat(result, equalTo(PATH_DATA));
    }

    @Test
    public void serviceReturnsClinVarDataEvenWhenNoSourcesAreDefined() {
        PathogenicityData result = instance.getVariantPathogenicityData(variant, Collections.emptySet());
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA)));
    }

    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.POLYPHEN));
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, PolyPhenScore.of(1f))));
    }

    @Test
    public void serviceReturnsCaddDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD));
        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, CADD_DATA.pathogenicityScore(PathogenicitySource.CADD))));
    }

    @Test
    public void serviceReturnsCaddAndStandardMissenseDescriptorDataForMissenseVariant() {
        variant = buildVariantOfType(VariantEffect.MISSENSE_VARIANT);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.CADD, PathogenicitySource.POLYPHEN));

        assertThat(result, equalTo(PathogenicityData.of(PATH_CLINVAR_DATA, PolyPhenScore.of(1f), CaddScore.of(15f))));
    }

    @Test
    public void serviceReturnsSpecifiedPathogenicityDataForKnownNonCodingVariant() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedNcdsData = PathogenicityData.of(PATH_CLINVAR_DATA, RemmScore.of(1f));
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedNcdsData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedNcdsData));
    }

    @Test
    public void pathogenicityDataForKnownNonCodingVariantShouldContainClinVarData() {
        variant = buildVariantOfType(VariantEffect.REGULATORY_REGION_VARIANT);
        PathogenicityData expectedPathData = PathogenicityData.of(PATH_CLINVAR_DATA);
        Mockito.when(mockRemmDao.getPathogenicityData(variant)).thenReturn(expectedPathData);
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.REMM));
        assertThat(result, equalTo(expectedPathData));
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
        PathogenicityData expectedNcdsData = PathogenicityData.of(PATH_CLINVAR_DATA, CaddScore.of(15f), RemmScore.of(1f));
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
        Mockito.when(defaultPathogenicityDao.getPathogenicityData(variant)).thenReturn(PathogenicityData.of(MutationTasterScore.of(1f)));
        Mockito.when(clinVarDao.getClinVarData(variant)).thenReturn(ClinVarData.empty());
        PathogenicityData result = instance.getVariantPathogenicityData(variant, EnumSet.of(PathogenicitySource.MUTATION_TASTER));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWithRsIdForVariantWhenNoSourcesAreDefined() {
        FrequencyData result = instance.getVariantFrequencyData(variant, Collections.emptySet());
        assertThat(result, equalTo(FrequencyData.of("rs1234567")));
    }

    @Test
    public void serviceReturnsFrequencyDataForVariant() {
        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.allOf(FrequencySource.class));
        assertThat(result, equalTo(FREQ_DATA));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariant() {
        FrequencyData frequencyData = FrequencyData.of("rs234567", Frequency.of(ESP_AA, 1f), Frequency
                .of(ESP_EA, 1f));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(ESP_AA));
        assertThat(result, equalTo(FrequencyData.of("rs234567", Frequency.of(ESP_AA, 1f))));
    }

    @Test
    public void serviceReturnsLocalFrequencyDataForVariant() {
        FrequencyData localFrequencyData = FrequencyData.of(Frequency.of(LOCAL, 2f));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(LOCAL));
        assertThat(result, equalTo(localFrequencyData));
    }

    @Test
    public void serviceReturnsLocalFrequencyDataForVariantWithRsIdIfKnown() {
        FrequencyData localFrequencyData = FrequencyData.of(Frequency.of(LOCAL, 2f));
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(LOCAL));
        assertThat(result, equalTo(FrequencyData.of("rs1234567", Frequency.of(LOCAL, 2f))));
    }

    @Test
    public void serviceReturnsSpecifiedFrequencyDataForVariantIncludingLocalData() {
        FrequencyData frequencyData = FrequencyData.of("rs234567", Frequency.of(ESP_AA, 1f), Frequency
                .of(ESP_EA, 1f));
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(frequencyData);

        FrequencyData localFrequencyData = FrequencyData.of(Frequency.of(LOCAL, 2f));
        Mockito.when(localFrequencyDao.getFrequencyData(variant)).thenReturn(localFrequencyData);

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(ESP_AA, LOCAL));
        assertThat(result, equalTo(FrequencyData.of("rs234567", Frequency.of(ESP_AA, 1f), Frequency
                .of(LOCAL, 2f))));
    }

    @Test
    public void serviceReturnsEmptyFrequencyDataWhenSpecifiedFrequencyDataIsUnavailable() {
        Mockito.when(defaultFrequencyDao.getFrequencyData(variant)).thenReturn(FrequencyData.empty());

        FrequencyData result = instance.getVariantFrequencyData(variant, EnumSet.of(LOCAL));
        assertThat(result, equalTo(FrequencyData.empty()));
    }

    @Test
    void serviceReturnsDataAboutWhiteList() {
        assertThat(instance.variantIsWhiteListed(variant), is(false));
    }

    @Test
    void whiteListedVariant() {
        Variant whiteListVariant = TestFactory.variantBuilder(3, 12345, "A", "C").build();
        Variant nonWhiteListVariant = TestFactory.variantBuilder(3, 12345, "G", "T").build();

        VariantWhiteList whiteList = InMemoryVariantWhiteList.of(Set.of(AlleleProtoAdaptor.toAlleleKey(whiteListVariant)));
        VariantDataServiceImpl instance = VariantDataServiceImpl.builder()
                .defaultFrequencyDao(defaultFrequencyDao)
                .defaultPathogenicityDao(defaultPathogenicityDao)
                .clinVarDao(clinVarDao)
                .variantWhiteList(whiteList)
                .build();

        assertThat(instance.variantIsWhiteListed(whiteListVariant), is(true));
        assertThat(instance.variantIsWhiteListed(nonWhiteListVariant), is(false));
    }
}
