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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterSettings.FilterSettingsBuilder;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class FilterSettingsTest {

    private FilterSettings instance;
    private FilterSettingsBuilder builder;

    private static final float MAX_FREQ_DEFAULT = 100.00f;
    private static final float MIN_QUAL_DEFAULT = 0;
    private static final GeneticInterval GENETIC_INTERVAL_DEFAULT = null;
    private static final boolean KEEP_NON_PATHOGENIC_VARIANTS_DEFAULT = false;
    private static final boolean REMOVE_FAILED_VARIANTS_DEFAULT = false;
    private static final boolean REMOVE_KNOWN_VARIANTS_DEFAULT = false;
    private static final boolean KEEP_OFF_TARGET_VARIANTS_DEFAULT = false;
    private static final Set<Integer> GENE_IDS_TO_KEEP_DEFAULT = new LinkedHashSet<>();
    private static final ModeOfInheritance MODE_OF_INHERITANCE_DEFAULT = ModeOfInheritance.ANY;

    @BeforeEach
    void setUp() {
        builder = FilterSettings.builder();
    }

    @Test
    void testDefaultValues() {
        instance = builder.build();
        assertThat(instance.maximumFrequency(), equalTo(MAX_FREQ_DEFAULT));
        assertThat(instance.minimumQuality(), equalTo(MIN_QUAL_DEFAULT));
        assertThat(instance.geneticInterval(), equalTo(GENETIC_INTERVAL_DEFAULT));
        assertThat(instance.keepNonPathogenicVariants(), equalTo(KEEP_NON_PATHOGENIC_VARIANTS_DEFAULT));
        assertThat(instance.removeFailedVariants(), equalTo(REMOVE_FAILED_VARIANTS_DEFAULT));
        assertThat(instance.removeKnownVariants(), equalTo(REMOVE_KNOWN_VARIANTS_DEFAULT));
        assertThat(instance.keepOffTargetVariants(), equalTo(KEEP_OFF_TARGET_VARIANTS_DEFAULT));
        assertThat(instance.genesToKeep(), equalTo(GENE_IDS_TO_KEEP_DEFAULT));
        assertThat(instance.modeOfInheritance(), equalTo(MODE_OF_INHERITANCE_DEFAULT));
    }
    
    @Test
    void testMaximumFrequency() {
        float maxFreq = 0.5f;
        instance = builder.maximumFrequency(maxFreq).build();
        assertThat(instance.maximumFrequency(), equalTo(maxFreq));
    }

    @Test
    void testMinimumQuality() {
        float minQual = 10f;
        instance = builder.minimumQuality(minQual).build();
        assertThat(instance.minimumQuality(), equalTo(minQual));
    }

    @Test
    void testGeneticInterval() {
        GeneticInterval interval = null;
        instance = builder.geneticInterval(interval).build();
        assertThat(instance.geneticInterval(), nullValue());
    }
    
    @Test
    void testKeepNonPathogenic() {
        boolean keepNonPathogenic = false;
        instance = builder.keepNonPathogenic(keepNonPathogenic).build();
        assertThat(instance.keepNonPathogenicVariants(), equalTo(keepNonPathogenic));
    }

    @Test
    void testRemoveFailedVariants() {
        boolean expected = true;
        instance = builder.removeFailedVariants(expected).build();
        assertThat(instance.removeFailedVariants(), equalTo(expected));
    }

    @Test
    void testRemoveKnownVariants() {
        boolean removeKnownVariants = true;
        instance = builder.removeKnownVariants(removeKnownVariants).build();
        assertThat(instance.removeKnownVariants(), equalTo(removeKnownVariants));
    }
    
    @Test
    void testKeepOffTargetVariants() {
        boolean keepOffTargetVariants = true;
        instance = builder.keepOffTargetVariants(keepOffTargetVariants).build();
        assertThat(instance.keepOffTargetVariants(), equalTo(keepOffTargetVariants));
    }
    
    @Test
    void testGenesToKeep() {
        Set<String> genesToKeep = new LinkedHashSet<>();
        genesToKeep.add("1");
        genesToKeep.add("2");
        genesToKeep.add("3");
        instance = builder.genesToKeep(genesToKeep).build();
        assertThat(instance.genesToKeep(), equalTo(genesToKeep));
    }
    
    @Test
    void testModeOfInheritance() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        instance = builder.modeOfInheritance(modeOfInheritance).build();
        assertThat(instance.modeOfInheritance(), equalTo(modeOfInheritance));
    }
    
    @Test
    void testHashCode() {
        FilterSettings other = FilterSettings.builder().build();
        instance = builder.build();
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }
    
    @Test
    void testEquals() {
        FilterSettings other = FilterSettings.builder().build();
        instance = builder.build();
        assertThat(instance, equalTo(other));
    }
    
    @Test
    void testNotEquals() {
        FilterSettings other = FilterSettings.builder().minimumQuality(Float.MAX_VALUE).build();
        instance = builder.build();
        assertThat(instance.equals(other), is(false));
    }
}
