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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.*;
import htsjdk.variant.variantcontext.*;
import htsjdk.variant.variantcontext.Genotype;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.genome.VariantContextSampleGenotypeConverter;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Static utility class for setting up alleles and pedigrees.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class TestAlleleFactory {

    private TestAlleleFactory() {
        // static utility class
    }

    public static VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult) {
        return VariantEvaluation.builder(chr, pos, ref, alt)
                .filterResults(filterResult)
                .build();
    }

    public static VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult, VariantEffect variantEffect) {
        return VariantEvaluation.builder(chr, pos, ref, alt)
                .variantEffect(variantEffect)
                .filterResults(filterResult)
                .build();
    }

    public static VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult, VariantContext variantContext) {
        List<Allele> altAlleles = variantContext.getAlternateAlleles();
        int altAlleleId = findAltAlleleId(alt, altAlleles);

        Map<String, SampleGenotype> sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);

        return VariantEvaluation.builder(chr, pos, ref, alt)
                .altAlleleId(altAlleleId)
                .variantContext(variantContext)
                .sampleGenotypes(sampleGenotypes)
                .filterResults(filterResult)
                .build();
    }

    public static VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult, VariantContext variantContext, VariantEffect variantEffect) {
        List<Allele> altAlleles = variantContext.getAlternateAlleles();
        int altAlleleId = findAltAlleleId(alt, altAlleles);

        Map<String, SampleGenotype> sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);

        return VariantEvaluation.builder(chr, pos, ref, alt)
                .altAlleleId(altAlleleId)
                .variantContext(variantContext)
                .variantEffect(variantEffect)
                .sampleGenotypes(sampleGenotypes)
                .filterResults(filterResult)
                .build();
    }

    private static int findAltAlleleId(String alt, List<Allele> altAlleles) {
        for (int i = 0; i < altAlleles.size(); i++) {
            if (alt.equalsIgnoreCase(altAlleles.get(i).getBaseString())) {
                return i;
            }
        }
        return 0;
    }

    public static List<Allele> buildAlleles(String ref, String... alts) {
        Allele refAllele = Allele.create(ref, true);

        List<Allele> altAlleles = Arrays.stream(alts).map(Allele::create).collect(toList());
        List<Allele> alleles = new ArrayList<>();
        alleles.add(refAllele);
        alleles.addAll(altAlleles);
        return alleles;
    }

    public static Genotype buildPhasedSampleGenotype(String sampleName, Allele ref, Allele alt) {
        GenotypeBuilder gtBuilder = new GenotypeBuilder(sampleName).noAttributes()
                .alleles(Arrays.asList(ref, alt))
                .phased(true);
        return gtBuilder.make();
    }

    public static Genotype buildUnPhasedSampleGenotype(String sampleName, Allele ref, Allele alt) {
        GenotypeBuilder gtBuilder = new GenotypeBuilder(sampleName).noAttributes()
                .alleles(Arrays.asList(ref, alt))
                .phased(false);
        return gtBuilder.make();
    }

    public static VariantContext buildVariantContext(int chr, int pos, List<Allele> alleles, Genotype... genotypes) {
        Allele refAllele = alleles.get(0);

        VariantContextBuilder vcBuilder = new VariantContextBuilder();
        vcBuilder.loc(Integer.toString(chr), pos, (pos - 1) + refAllele.length());
        vcBuilder.alleles(alleles);
        vcBuilder.genotypes(genotypes);
        //yeah I know, it's a zero
        vcBuilder.log10PError(-0.1 * 0);

        return vcBuilder.make();
    }

    public static Pedigree buildPedigree(PedPerson... people) {
        ImmutableList.Builder<PedPerson> individualBuilder = new ImmutableList.Builder<PedPerson>();
        individualBuilder.addAll(Arrays.asList(people));

        PedFileContents pedFileContents = new PedFileContents(new ImmutableList.Builder<String>().build(), individualBuilder
                .build());

        return buildPedigreeFromPedFile(pedFileContents);

    }

    public static Pedigree buildPedigreeFromPedFile(PedFileContents pedFileContents) {
        final String name = pedFileContents.getIndividuals().get(0).getPedigree();
        try {
            return new Pedigree(name, new PedigreeExtractor(name, pedFileContents).run());
        } catch (PedParseException e) {
            throw new RuntimeException(e);
        }
    }

}
