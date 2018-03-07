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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeOptionsTest {

    private static final float DEFAULT_DOMINANT_FREQ = 0.1f;

    //TODO: this value is too high and needs to be revised
    private static final float DEFAULT_HOM_ALT_RECESSIVE_FREQ = 1.0f;
    private static final float DEFAULT_COMP_HET_RECESSIVE_FREQ = 2.0f;

    private static final float DEFAULT_RECESSIVE_FREQ = DEFAULT_COMP_HET_RECESSIVE_FREQ;

    private static final float DEFAULT_MITO_FREQ = 0.2f;

    @Test
    public void empty() {
        InheritanceModeOptions instance = InheritanceModeOptions.empty();

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));

        //
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_DOMINANT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_COMP_HET), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.X_RECESSIVE_HOM_ALT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.MITOCHONDRIAL), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
    }

    @Test
    public void defaultModeOfInheritanceValues() {
        InheritanceModeOptions instance = InheritanceModeOptions.defaults();

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(DEFAULT_DOMINANT_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_RECESSIVE), equalTo(DEFAULT_RECESSIVE_FREQ));

        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(DEFAULT_MITO_FREQ));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
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
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
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
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.X_DOMINANT), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(Float.MAX_VALUE));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
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
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.ANY), equalTo(Float.MAX_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultModeOfInheritanceThrowsExceptionWithAny() {
        InheritanceModeOptions.defaultForModes(ModeOfInheritance.ANY);
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
    public void isEmpty() {
        assertThat(InheritanceModeOptions.empty().isEmpty(), equalTo(true));
        assertThat(InheritanceModeOptions.defaults().isEmpty(), equalTo(false));
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionWithNullInput() {
        InheritanceModeOptions.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWithAnyValue() {
        Map<SubModeOfInheritance, Float> withAny = ImmutableMap.of(
                SubModeOfInheritance.ANY, 100f
        );
        InheritanceModeOptions.of(withAny);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWithNonPercentageValue() {
        //check upper bounds
        Map<SubModeOfInheritance, Float> tooHigh = ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 101f);
        InheritanceModeOptions.of(tooHigh);

        //check lower bounds
        Map<SubModeOfInheritance, Float> tooLow = ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, -1f);
        InheritanceModeOptions.of(tooLow);
    }

    @Test
    public void testMaxAndMinValues() {
        //check max
        Map<SubModeOfInheritance, Float> max = ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 100f);
        InheritanceModeOptions.of(max);

        //check min
        Map<SubModeOfInheritance, Float> min = ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0f);
        InheritanceModeOptions.of(min);
    }

    @Test
    public void userDefinedFrequencyCutoffs() {
        Map<SubModeOfInheritance, Float> userDefinedThresholds = ImmutableMap.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 1f);
        //should we allow this to happen? If people don't define the COMP_HET and HOM_ALT then the recessive modes won't
        InheritanceModeOptions instance = InheritanceModeOptions.of(userDefinedThresholds);

        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET), equalTo(1f));
        assertThat(instance.getMaxFreqForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(1f));
        //non-user-defined
        assertThat(instance.getMaxFreqForSubMode(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT), equalTo(Float.MAX_VALUE));
    }

    @Test
    public void testToString() {
        System.out.println(InheritanceModeOptions.defaults());
    }
}