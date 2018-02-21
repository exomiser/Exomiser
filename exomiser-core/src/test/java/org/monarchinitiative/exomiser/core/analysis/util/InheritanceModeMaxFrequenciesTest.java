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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeMaxFrequenciesTest {

    @Test
    public void defaultModeOfInheritanceValues() {
        InheritanceModeMaxFrequencies instance = InheritanceModeMaxFrequencies.defaultValues();

        float defaultDominantFreq = 0.1f;
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(defaultDominantFreq));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(defaultDominantFreq));

        float defaultRecessiveFreq = 2.0f;
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(defaultRecessiveFreq));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(defaultRecessiveFreq));

        float defaultMitoFreq = 0.2f;
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(defaultMitoFreq));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
    }

    @Test
    public void defaultSubModeOfInheritanceValues() {
        InheritanceModeMaxFrequencies instance = InheritanceModeMaxFrequencies.defaultValues();

        float defaultDominantFreq = 0.1f;
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(defaultDominantFreq));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_DOMINANT), equalTo(defaultDominantFreq));

        float defaultCompHetRecessiveFreq = 2.0f;
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(defaultCompHetRecessiveFreq));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_COMP_HET), equalTo(defaultCompHetRecessiveFreq));

        //This should be changed
        float defaultHomAltRecessiveFreq = 2.0f;
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(defaultHomAltRecessiveFreq));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_HOM_ALT), equalTo(defaultHomAltRecessiveFreq));


        float defaultMitoFreq = 0.2f;
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.MITOCHONDRIAL), equalTo(defaultMitoFreq));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNullInput() {
        InheritanceModeMaxFrequencies.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWithNonPercentageValue() {
        //check upper bounds
        Map<SubModeOfInheritance, Float> tooHigh = new HashMap<>();
        tooHigh.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 101f);

        InheritanceModeMaxFrequencies.of(tooHigh);

        //check lower bounds
        Map<SubModeOfInheritance, Float> toolow = new HashMap<>();
        tooHigh.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, -1f);

        InheritanceModeMaxFrequencies.of(toolow);
    }

    @Test
    public void testMaxAndMinValues() {
        //check max
        Map<SubModeOfInheritance, Float> max = new HashMap<>();
        max.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 100f);

        InheritanceModeMaxFrequencies.of(max);

        //check min
        Map<SubModeOfInheritance, Float> min = new HashMap<>();
        max.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0f);

        InheritanceModeMaxFrequencies.of(min);
    }

    @Test
    public void userDefinedFrequencyCutoffs() {
        Map<SubModeOfInheritance, Float> userDefinedThresholds = new EnumMap<>(SubModeOfInheritance.class);
        userDefinedThresholds.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 1f);
        //should we allow this to happen? If people don't define the COMP_HET and HOM_ALT then the recessive modes won't
        InheritanceModeMaxFrequencies instance = InheritanceModeMaxFrequencies.of(userDefinedThresholds);

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(1f));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(1f));
        //non-user-defined
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(Float.MAX_VALUE));
    }

    @Test
    public void testToString() {
        System.out.println(InheritanceModeMaxFrequencies.defaultValues());
    }
}