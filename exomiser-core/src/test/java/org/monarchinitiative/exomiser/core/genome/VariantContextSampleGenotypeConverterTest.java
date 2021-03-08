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

import com.google.common.collect.ImmutableMap;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.AlleleCall;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantContextSampleGenotypeConverterTest {

    @Test
    void testNoGenotypeReturnsEmptyMap() {
        List<Allele> alleles = buildAlleles("A", "T");
        VariantContext variantContext = buildVariantContext(1, 12345, alleles);

        Map<String, SampleGenotype> result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testSingleSampleHetUnPhased() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));
        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);

        Map<String, SampleGenotype> result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

        Map<String, SampleGenotype> expected = ImmutableMap.of("Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSingleSampleHetPhased() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));
        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);

        Map<String, SampleGenotype> result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

        Map<String, SampleGenotype> expected = ImmutableMap.of("Adam", SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT));

        assertThat(result, equalTo(expected));
    }


    @Test
    public void testSingleSampleHomRef() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(0));
        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);

        Map<String, SampleGenotype> result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

        Map<String, SampleGenotype> expected = ImmutableMap.of("Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.REF));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSingleSampleHomAlt() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype genotype = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(1));
        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);

        Map<String, SampleGenotype> result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

        Map<String, SampleGenotype> expected = ImmutableMap.of("Adam", SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT));

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSingleSampleHetNonRef() {
        List<Allele> alleles = buildAlleles("A", "T", "G");

        Genotype genotype = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(2));
        VariantContext variantContext = buildVariantContext(1, 12345, alleles, genotype);

        Map<String, SampleGenotype> allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        Map<String, SampleGenotype> expected1 = ImmutableMap.of("Adam", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT));
        assertThat(allele1Result, equalTo(expected1));

        Map<String, SampleGenotype> allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        Map<String, SampleGenotype> expected2 = ImmutableMap.of("Adam", SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT));
        assertThat(allele2Result, equalTo(expected2));
    }

    @Test
    public void testSingleSampleWithNoCall() {
        List<Allele> alleles = buildAlleles("A", "T");

        Genotype proband = buildUnPhasedSampleGenotype("Cain",  Allele.create((byte) '.'), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband);

        //Test A T allele
        Map<String, SampleGenotype> allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        Map<String, SampleGenotype> expected = ImmutableMap.of(
                "Cain", SampleGenotype.of(AlleleCall.NO_CALL, AlleleCall.ALT)
        );
        assertThat(allele1Result, equalTo(expected));
    }

    @Test
    public void testMultiSample() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(1), alleles.get(1));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(1));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);

        //Test A T allele
        Map<String, SampleGenotype> allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        Map<String, SampleGenotype> expected1 = ImmutableMap.of(
                "Cain", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                "Abel", SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT),
                "Eve", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                "Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)
        );
        assertThat(allele1Result, equalTo(expected1));

        //Test A C allele
        Map<String, SampleGenotype> allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        Map<String, SampleGenotype> expected2 = ImmutableMap.of(
                "Cain", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                "Abel", SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT),
                "Eve", SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                "Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT)
        );
        assertThat(allele2Result, equalTo(expected2));
    }

    @Test
    public void testCompHetAutosomalRecessiveFamilyTwoAlts() {
        List<Allele> alleles = buildAlleles("A", "T", "C");

        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(2));
        Genotype brother = buildUnPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(2));
        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(0), alleles.get(2));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, brother, mother, father);

        //Test A T allele
        Map<String, SampleGenotype> allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        Map<String, SampleGenotype> expected1 = ImmutableMap.of(
                "Cain", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                "Abel", SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                "Eve", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                "Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT)
        );
        assertThat(allele1Result, equalTo(expected1));

        //Test A C allele
        Map<String, SampleGenotype> allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        Map<String, SampleGenotype> expected2 = ImmutableMap.of(
                "Cain", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                "Abel", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                "Eve", SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                "Adam", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)
        );
        assertThat(allele2Result, equalTo(expected2));
    }
}