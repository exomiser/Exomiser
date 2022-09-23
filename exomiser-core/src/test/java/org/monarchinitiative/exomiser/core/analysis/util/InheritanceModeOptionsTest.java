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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeOptionsTest {

    private static final float DEFAULT_DOMINANT_FREQ = 0.1f;

    private static final float DEFAULT_HOM_ALT_RECESSIVE_FREQ = 0.1f;
    private static final float DEFAULT_COMP_HET_RECESSIVE_FREQ = 2.0f;

    private static final float DEFAULT_RECESSIVE_FREQ = DEFAULT_COMP_HET_RECESSIVE_FREQ;

    private static final float DEFAULT_MITO_FREQ = 0.2f;

    @Test
    public void empty() {
        InheritanceModeOptions instance = InheritanceModeOptions.empty();

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));

        //
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_DOMINANT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_COMP_HET), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_HOM_ALT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.MITOCHONDRIAL), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void defaultModeOfInheritanceValues() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaults();

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(DEFAULT_MITO_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void defaultSubModeOfInheritanceValues() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaults();

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(DEFAULT_COMP_HET_RECESSIVE_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_COMP_HET), equalTo(DEFAULT_COMP_HET_RECESSIVE_FREQ));

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(DEFAULT_HOM_ALT_RECESSIVE_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_HOM_ALT), equalTo(DEFAULT_HOM_ALT_RECESSIVE_FREQ));

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.MITOCHONDRIAL), equalTo(DEFAULT_MITO_FREQ));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void defaultModeOfInheritanceFromSubList() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaultForModes(
                ModeOfInheritance.AUTOSOMAL_DOMINANT,
                ModeOfInheritance.X_RECESSIVE
        );
        // values defined in the input
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));
        // undefined values
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void defaultModeOfInheritanceFromCompleteList() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaultForModes(
                ModeOfInheritance.AUTOSOMAL_DOMINANT,
                ModeOfInheritance.AUTOSOMAL_RECESSIVE,

                ModeOfInheritance.X_DOMINANT,
                ModeOfInheritance.X_RECESSIVE,

                ModeOfInheritance.MITOCHONDRIAL

        );
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(DEFAULT_MITO_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void defaultModeOfInheritanceThrowsExceptionWithAny() {
        InheritanceModeOptions anyDefault = InheritanceModeOptions.defaultForModes(ModeOfInheritance.ANY);
        assertThat(anyDefault.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(2.0f));
    }

    @Test
    public void noArgsEqualToEmpty() {
        assertThat(InheritanceModeOptions.defaultForModes(), equalTo(InheritanceModeOptions.empty()));
    }

    @Test
    public void canGetDefinedModes() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaultForModes(
                ModeOfInheritance.AUTOSOMAL_DOMINANT,
                ModeOfInheritance.X_RECESSIVE
        );

        InheritanceModeOptions empty = InheritanceModeOptions.empty();

        assertThat(instance.getDefinedModes(), equalTo(ImmutableSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_RECESSIVE)));
        assertThat(empty.getDefinedModes(), equalTo(ImmutableSet.of()));
    }

    @Test
    public void canGetDefinedSubModes() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaultForModes(
                ModeOfInheritance.AUTOSOMAL_DOMINANT,
                ModeOfInheritance.X_RECESSIVE
        );

        InheritanceModeOptions empty = InheritanceModeOptions.empty();

        assertThat(instance.getDefinedSubModes(), equalTo(ImmutableSet.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, SubModeOfInheritance.X_RECESSIVE_COMP_HET, SubModeOfInheritance.X_RECESSIVE_HOM_ALT)));
        assertThat(empty.getDefinedSubModes(), equalTo(ImmutableSet.of()));
    }

    @Test
    public void isEmpty() {
        assertThat(InheritanceModeOptions.empty().isEmpty(), equalTo(true));
        assertThat(InheritanceModeOptions.defaults().isEmpty(), equalTo(false));
    }

    @Test
    public void throwsExceptionWithNullInput() {
        assertThrows(NullPointerException.class, () -> InheritanceModeOptions.of(null));
    }

    @Test
    public void acceptsAnyValue() {
        Map<SubModeOfInheritance, Float> withAny = Map.of(
                SubModeOfInheritance.ANY, 100f
        );
        InheritanceModeOptions instance = InheritanceModeOptions.of(withAny);
        assertThat(instance.getDefinedModes(), equalTo(EnumSet.of(ModeOfInheritance.ANY)));
    }

    @Test
    public void throwsExceptionWithNonPercentageValue() {
        //check upper bounds
        Map<SubModeOfInheritance, Float> tooHigh = Map.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 101f);
        assertThrows(IllegalArgumentException.class, () -> InheritanceModeOptions.of(tooHigh));

        //check lower bounds
        Map<SubModeOfInheritance, Float> tooLow = Map.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, -1f);
        assertThrows(IllegalArgumentException.class, () -> InheritanceModeOptions.of(tooLow));
    }

    @Test
    public void testMaxAndMinValues() {
        //check max
        Map<SubModeOfInheritance, Float> max = Map.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 100f);
        InheritanceModeOptions.of(max);

        //check min
        Map<SubModeOfInheritance, Float> min = Map.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0f);
        InheritanceModeOptions.of(min);
    }

    @Test
    public void userDefinedFrequencyCutoffs() {
        Map<SubModeOfInheritance, Float> userDefinedThresholds = Map.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 1f);
        //should we allow this to happen? If people don't define the COMP_HET and HOM_ALT then the recessive modes won't
        InheritanceModeOptions instance = InheritanceModeOptions.of(userDefinedThresholds);

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(1f));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(1f));
        //non-user-defined
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(InheritanceModeOptions.MAX_FREQ));
    }

    @Test
    public void getMaxFreq() {
        assertThat(InheritanceModeOptions.empty().getMaxFreq(), equalTo(InheritanceModeOptions.MAX_FREQ));
        assertThat(InheritanceModeOptions.defaults().getMaxFreq(), equalTo(2.0f));

        Map<SubModeOfInheritance, Float> userDefinedThresholds = Map.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 1f);
        InheritanceModeOptions userDefined = InheritanceModeOptions.of(userDefinedThresholds);
        assertThat(userDefined.getMaxFreq(), equalTo(1f));
    }

    @Test
    void getMaxFreqsEmpty() {
        assertThat(InheritanceModeOptions.empty().getMaxFreqs(), equalTo(Map.of()));
    }

    @Test
    void getMaxFreqsDefaults() {
        Map<SubModeOfInheritance, Float> expected = new EnumMap<>(SubModeOfInheritance.class);
        expected.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f);
        expected.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);
        expected.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, 0.1f);

        expected.put(SubModeOfInheritance.X_DOMINANT, 0.1f);
        expected.put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, 2.0f);
        expected.put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, 0.1f);

        expected.put(SubModeOfInheritance.MITOCHONDRIAL, 0.2f);

        InheritanceModeOptions instance = InheritanceModeOptions.defaults();
        assertThat(instance.getMaxFreqs(), equalTo(expected));
    }

    @Test
    void getMaxFreqUserDefined() {
        Map<SubModeOfInheritance, Float> expected = new EnumMap<>(SubModeOfInheritance.class);
        expected.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f);
        expected.put(SubModeOfInheritance.X_DOMINANT, 0.1f);

        InheritanceModeOptions instance = InheritanceModeOptions.of(expected);
        assertThat(instance.getMaxFreqs(), equalTo(expected));
    }

    @Test
    public void testToString() {
        assertThat(InheritanceModeOptions.defaults().toString(), equalTo("InheritanceModeMaxFrequencies{AUTOSOMAL_DOMINANT=0.1, AUTOSOMAL_RECESSIVE_COMP_HET=2.0, AUTOSOMAL_RECESSIVE_HOM_ALT=0.1, X_RECESSIVE_COMP_HET=2.0, X_RECESSIVE_HOM_ALT=0.1, X_DOMINANT=0.1, MITOCHONDRIAL=0.2}"));
    }
}