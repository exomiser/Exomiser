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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutputSettingsTest {

    @Test
    public void testThatDefaultOutputPassVariantsOptionIsFalse() {
        OutputSettings instance = OutputSettings.builder().build();
        assertThat(instance.outputContributingVariantsOnly(), equalTo(false));
    }

    @Test
    public void testThatBuilderProducesOutputPassVariantsOptionWhenSet() {
        OutputSettings instance = OutputSettings.builder()
                .outputContributingVariantsOnly(true)
                .build();
        assertThat(instance.outputContributingVariantsOnly(), equalTo(true));
    }

    /**
     * Test of getNumberOfGenesToShow method, of class ExomiserSettings.
     */
    @Test
    public void testThatDefaultNumberOfGenesToShowIsZero() {
        OutputSettings instance = OutputSettings.builder().build();
        assertThat(instance.getNumberOfGenesToShow(), equalTo(0));
    }

    @Test
    public void testThatBuilderCanSetNumberOfGenesToShow() {
        int numGenes = 200;
        OutputSettings instance = OutputSettings.builder()
                .numberOfGenesToShow(numGenes)
                .build();
        assertThat(instance.getNumberOfGenesToShow(), equalTo(numGenes));
    }

    /**
     * Test of getOutputPrefix method, of class ExomiserSettings.
     */
    @Test
    public void testThatBuilderProducesDefaultOutFileName() {
        OutputSettings instance = OutputSettings.builder().build();
        assertThat(instance.getOutputPrefix(), equalTo(""));
    }

    @Test
    public void testThatBuilderProducesSetOutFileName() {
        String outputPrefix = "wibble";
        OutputSettings instance = OutputSettings.builder()
                .outputPrefix(outputPrefix)
                .build();
        assertThat(instance.getOutputPrefix(), equalTo(outputPrefix));
    }

    /**
     * Test of getOutputFormats method, of class ExomiserSettings.
     */
    @Test
    public void testThatDefaultOutputFormatIsHtml() {
        OutputSettings instance = OutputSettings.builder().build();
        assertThat(instance.getOutputFormats(), equalTo(EnumSet.of(OutputFormat.HTML)));
    }

    @Test
    public void testThatBuilderProducesSetOutputFormat() {
        Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.TSV_GENE);
        OutputSettings instance = OutputSettings.builder()
                .outputFormats(outputFormats)
                .build();
        assertThat(instance.getOutputFormats(), equalTo(outputFormats));
    }

    @Test
    public void testHashCode() {
        OutputSettings instance = OutputSettings.builder().build();
        OutputSettings other = OutputSettings.builder().build();
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        OutputSettings instance = OutputSettings.builder().build();
        OutputSettings other = OutputSettings.builder().build();
        assertThat(instance, equalTo(other));
    }

    @Test
    public void testToString() {
        OutputSettings instance = OutputSettings.builder().build();
        System.out.println(instance);
        assertThat(instance.toString().isEmpty(), is(false));
    }

    @Test
    public void testCanBuildFromYaml() throws Exception {
        OutputSettings instance = OutputSettings.builder().build();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        OutputSettings createdFromYaml = mapper.readValue(
                "outputContributingVariantsOnly: false\n"
                        + "numGenes: 0\n"
                        + "outputPrefix: \"\"\n"
                        + "outputFormats: [HTML]",
                OutputSettings.class);

        assertThat(instance, equalTo(createdFromYaml));
    }

    @Test
    public void testCanOutputAsYaml() throws Exception {
        OutputSettings instance = OutputSettings.builder().build();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String output = mapper.writeValueAsString(instance);
        System.out.println(output);
    }
}
