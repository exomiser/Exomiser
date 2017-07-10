package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.*;
import htsjdk.variant.variantcontext.*;
import htsjdk.variant.variantcontext.Genotype;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Static utility class for setting up alleles and pedigrees.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class TestAlleleFactory {

    private TestAlleleFactory() {

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
        int altAlleleId = 0;
        for (int i = 0; i < altAlleles.size(); i++) {
            if (alt.equalsIgnoreCase(altAlleles.get(i).getBaseString())) {
                altAlleleId = i;
            }
        }

        return VariantEvaluation.builder(chr, pos, ref, alt)
                .altAlleleId(altAlleleId)
                .variantContext(variantContext)
                .filterResults(filterResult)
                .build();
    }

    public static VariantEvaluation filteredVariant(int chr, int pos, String ref, String alt, FilterResult filterResult, VariantContext variantContext, VariantEffect variantEffect) {
        List<Allele> altAlleles = variantContext.getAlternateAlleles();
        int altAlleleId = 0;
        for (int i = 0; i < altAlleles.size(); i++) {
            if (alt.equalsIgnoreCase(altAlleles.get(i).getBaseString())) {
                altAlleleId = i;
            }
        }

        return VariantEvaluation.builder(chr, pos, ref, alt)
                .altAlleleId(altAlleleId)
                .variantContext(variantContext)
                .variantEffect(variantEffect)
                .filterResults(filterResult)
                .build();
    }

    public static List<Allele> buildAlleles(String ref, String... alts) {
        Allele refAllele = Allele.create(ref, true);

        List<Allele> altAlleles = Arrays.asList(alts).stream().map(Allele::create).collect(toList());
        List<Allele> alleles = new ArrayList<>();
        alleles.add(refAllele);
        alleles.addAll(altAlleles);
        return alleles;
    }

    public static Genotype buildSampleGenotype(String sampleName, Allele ref, Allele alt) {
        GenotypeBuilder gtBuilder = new GenotypeBuilder(sampleName).noAttributes()
                .alleles(Arrays.asList(ref, alt))
                .phased(true);
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
