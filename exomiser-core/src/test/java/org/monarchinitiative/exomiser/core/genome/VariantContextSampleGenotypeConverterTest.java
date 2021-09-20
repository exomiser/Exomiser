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

import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantContextSampleGenotypeConverterTest {

    @Test
    void testNoGenotypeReturnsEmptyMap() {
        TestVcfReader vcfReader = TestVcfReader.forSamples();
        VariantContext variantContext = vcfReader.readVariantContext("1 12345 . A T . PASS .");

        SampleGenotypes result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        assertThat(result.isEmpty(), is(true));
    }

    @ParameterizedTest
    @CsvSource({
            "Adam, 0/1",
            "Adam, 0|1",
            "Adam, 0/0",
            "Adam, 1/1",
            "Adam, ./1",
            "Adam, ./.",
            "Adam Eve Cain, ./. 0/1 1/1",
            "Adam Eve Cain Abel, 0/1 0/1 1/1 0/0",
    })
    public void testSampleGenotypes(String sampleNames, String sampleGenotypes) {
        String[] names = sampleNames.split(" ");
        String[] genotypes = sampleGenotypes.split(" ");

        TestVcfReader vcfReader = TestVcfReader.forSamples(names);
        VariantContext variantContext = vcfReader.readVariantContext("1 12345 . A T . PASS . GT " + sampleGenotypes);
        SampleGenotypes actual = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

        List<SampleData> sampleData = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            sampleData.add(SampleData.of(names[i], SampleGenotype.parseGenotype(genotypes[i])));
        }
        SampleGenotypes expected = SampleGenotypes.of(sampleData);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testSingleSampleHetNonRef() {
        TestVcfReader vcfReader = TestVcfReader.forSamples("Adam");
        VariantContext variantContext = vcfReader.readVariantContext("1 12345 . A T,C . PASS . GT 1/2");

        SampleGenotypes allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        SampleGenotypes expected1 = SampleGenotypes.of("Adam", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT));
        assertThat(allele1Result, equalTo(expected1));

        SampleGenotypes allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        SampleGenotypes expected2 = SampleGenotypes.of("Adam", SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT));
        assertThat(allele2Result, equalTo(expected2));
    }


    @Test
    public void testMultiSample() {
        TestVcfReader vcfReader = TestVcfReader.forSamples("Cain", "Abel", "Eve", "Adam");
        List<String> sampleIdentifiers = vcfReader.readSampleIdentifiers();

        VariantContext variantContext = vcfReader.readVariantContext("1 12345 . A T,C . PASS . GT 1/2 1/1 0/1 1/2");

        //Test A T allele
        SampleGenotypes allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        SampleGenotypes expected1 = SampleGenotypes.of(
                sampleIdentifiers.get(0), SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(1), SampleGenotype.of(AlleleCall.ALT, AlleleCall.ALT),
                sampleIdentifiers.get(2), SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                sampleIdentifiers.get(3), SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT)
        );
        assertThat(allele1Result, equalTo(expected1));

        //Test A C allele
        SampleGenotypes allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        SampleGenotypes expected2 = SampleGenotypes.of(
                sampleIdentifiers.get(0), SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(1), SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(2), SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(3), SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT)
        );
        assertThat(allele2Result, equalTo(expected2));
    }

    @Test
    public void testCompHetAutosomalRecessiveFamilyTwoAlts() {
        TestVcfReader vcfReader = TestVcfReader.forSamples("Cain", "Abel", "Adam", "Eve");
        List<String> sampleIdentifiers = vcfReader.readSampleIdentifiers();

        VariantContext variantContext = vcfReader.readVariantContext("1 12345 . A T,C . PASS . GT 1/2 0/2 0/1 0/2");

        //Test A T allele
        SampleGenotypes allele1Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        SampleGenotypes expected1 = SampleGenotypes.of(
                sampleIdentifiers.get(0), SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(1), SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(2), SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                sampleIdentifiers.get(3), SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT)
        );
        assertThat(allele1Result, equalTo(expected1));

        //Test A C allele
        SampleGenotypes allele2Result = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 1);
        SampleGenotypes expected2 = SampleGenotypes.of(
                sampleIdentifiers.get(0), SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(1), SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                sampleIdentifiers.get(2), SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                sampleIdentifiers.get(3), SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT)
        );
        assertThat(allele2Result, equalTo(expected2));
    }

    @Nested
    public class CnvSampleGenotypes {

        @Test
        void cnvSampleGenotypes() {
            // The issue here is that sometimes the GT is missing from a CNV type.
            TestVcfReader vcfReader = TestVcfReader.forSamples("sample");
            VariantContext variantContext = vcfReader
                    .readVariantContext("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tGT:RC:BC:CN\t0/1:51:41:1\n");

            SampleGenotypes sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1/1",
                "1, 0/1",
                "2, 0/1",
        })
        void cnvSampleGenotypesFromCopyNumber(int CN, String expected) {
            // The issue here is that sometimes the GT is missing from a CNV type.
            TestVcfReader vcfReader = TestVcfReader.forSamples("sample");
            VariantContext variantContext = vcfReader
                    .readVariantContext("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tCN\t" + CN + "\t\n");

            SampleGenotypes sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

            assertThat(sampleGenotypes.getSampleGenotype("sample"), equalTo(SampleGenotype.parseGenotype(expected)));
        }

        @ParameterizedTest
        @CsvSource({
                "0, 0, 1/1",
                "1, 1, 1/1",
                "2, 1, 0/1",
                "2, 2, 1/1",
                "4, 2, 0/1",
                "4, 3, 0/1",
                "4, 4, 1/1",
        })
        void cnvSampleGenotypesFromCopyNumberWithMajorChromosomeCount(int CN, int MCC, String expected) {
            // The issue here is that sometimes the GT is missing from a CNV type.
            TestVcfReader vcfReader = TestVcfReader.forSamples("sample");
            VariantContext variantContext = vcfReader
                    .readVariantContext("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tCN:MCC\t" + CN + ":" + MCC + "\t\n");

            SampleGenotypes sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

            assertThat(sampleGenotypes.getSampleGenotype("sample"), equalTo(SampleGenotype.parseGenotype(expected)));
            assertThat(sampleGenotypes.getSampleCopyNumber("sample"), equalTo(CopyNumber.of(CN)));
        }

        @Test
        void cnvSampleGenotypesFromCopyNumberNonNumericCN() {
            // The issue here is that sometimes the GT is missing from a CNV type.
            TestVcfReader vcfReader = TestVcfReader.forSamples("sample");
            VariantContext variantContext = vcfReader
                    .readVariantContext("1\t13195138\tCanvas:LOSS:1:13195138:13239068\tN\t<CNV>\t24.00\tPASS\tSVTYPE=CNV;END=13239068\tCN\tWibble!\t\n");

            SampleGenotypes sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, 0);

            assertThat(sampleGenotypes.getSampleGenotype("sample"), equalTo(SampleGenotype.empty()));
            assertThat(sampleGenotypes.getSampleCopyNumber("sample"), equalTo(CopyNumber.empty()));
        }
    }

}