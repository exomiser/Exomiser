package de.charite.compbio.exomiser.core.dao;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContextBuilder;

import java.util.Arrays;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.InvalidGenomeChange;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderDispatcher;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;

/**
 * Helper class for constructing {@link Variant} objects for tests.
 * 
 * The construction of {@link Variant} objects is quite complex but for tests, we would ideally have them for
 * testing our data sets. This class helps us with the construction.
 */
public class TestVariantFactory {

    private final ReferenceDictionary refDict;

    private static final String KG_LINE = "uc021pzz.1\tchr10\t-\t123237843\t123357972\t123239370\t123353331\t18\t123237843,123243211,123244908,123246867,123247504,123256045,123258008,123260339,123263303,123274630,123276832,123279492,123298105,123310803,123324015,123324951,123353222,123357475,\t123239535,123243317,123245046,123246938,123247627,123256236,123258119,123260461,123263455,123274833,123276977,123279683,123298229,123310973,123324093,123325218,123353481,123357972,\tP21802\tuc021pzz.1";
    private static final String TRANSCRIPT_SEQ = "ggcggcggctggaggagagcgcggtggagagccgagcgggcgggcggcgggtgcggagcgggcgagggagcgcgcgcggccgccacaaagctcgggcgccgcggggctgcatgcggcgtacctggcccggcgcggcgactgctctccgggctggcgggggccggccgcgagccccgggggccccgaggccgcagcttgcctgcgcgctctgagccttcgcaactcgcgagcaaagtttggtggaggcaacgccaagcctgagtcctttcttcctctcgttccccaaatccgagggcagcccgcgggcgtcatgcccgcgctcctccgcagcctggggtacgcgtgaagcccgggaggcttggcgccggcgaagacccaaggaccactcttctgcgtttggagttgctccccgcaaccccgggctcgtcgctttctccatcccgacccacgcggggcgcggggacaacacaggtcgcggaggagcgttgccattcaagtgactgcagcagcagcggcagcgcctcggttcctgagcccaccgcaggctgaaggcattgcgcgtagtccatgcccgtagaggaagtgtgcagatgggattaacgtccacatggagatatggaagaggaccggggattggtaccgtaaccatggtcagctggggtcgtttcatctgcctggtcgtggtcaccatggcaaccttgtccctggcccggccctccttcagtttagttgaggataccacattagagccagaagagccaccaaccaaataccaaatctctcaaccagaagtgtacgtggctgcgccaggggagtcgctagaggtgcgctgcctgttgaaagatgccgccgtgatcagttggactaaggatggggtgcacttggggcccaacaataggacagtgcttattggggagtacttgcagataaagggcgccacgcctagagactccggcctctatgcttgtactgccagtaggactgtagacagtgaaacttggtacttcatggtgaatgtcacagatgccatctcatccggagatgatgaggatgacaccgatggtgcggaagattttgtcagtgagaacagtaacaacaagagagcaccatactggaccaacacagaaaagatggaaaagcggctccatgctgtgcctgcggccaacactgtcaagtttcgctgcccagccggggggaacccaatgccaaccatgcggtggctgaaaaacgggaaggagtttaagcaggagcatcgcattggaggctacaaggtacgaaaccagcactggagcctcattatggaaagtgtggtcccatctgacaagggaaattatacctgtgtagtggagaatgaatacgggtccatcaatcacacgtaccacctggatgttgtggagcgatcgcctcaccggcccatcctccaagccggactgccggcaaatgcctccacagtggtcggaggagacgtagagtttgtctgcaaggtttacagtgatgcccagccccacatccagtggatcaagcacgtggaaaagaacggcagtaaatacgggcccgacgggctgccctacctcaaggttctcaaggccgccggtgttaacaccacggacaaagagattgaggttctctatattcggaatgtaacttttgaggacgctggggaatatacgtgcttggcgggtaattctattgggatatcctttcactctgcatggttgacagttctgccagcgcctggaagagaaaaggagattacagcttccccagactacctggagatagccatttactgcataggggtcttcttaatcgcctgtatggtggtaacagtcatcctgtgccgaatgaagaacacgaccaagaagccagacttcagcagccagccggctgtgcacaagctgaccaaacgtatccccctgcggagacaggtaacagtttcggctgagtccagctcctccatgaactccaacaccccgctggtgaggataacaacacgcctctcttcaacggcagacacccccatgctggcaggggtctccgagtatgaacttccagaggacccaaaatgggagtttccaagagataagctgacactgggcaagcccctgggagaaggttgctttgggcaagtggtcatggcggaagcagtgggaattgacaaagacaagcccaaggaggcggtcaccgtggccgtgaagatgttgaaagatgatgccacagagaaagacctttctgatctggtgtcagagatggagatgatgaagatgattgggaaacacaagaatatcataaatcttcttggagcctgcacacaggatgggcctctctatgtcatagttgagtatgcctctaaaggcaacctccgagaatacctccgagcccggaggccacccgggatggagtactcctatgacattaaccgtgttcctgaggagcagatgaccttcaaggacttggtgtcatgcacctaccagctggccagaggcatggagtacttggcttcccaaaaatgtattcatcgagatttagcagccagaaatgttttggtaacagaaaacaatgtgatgaaaatagcagactttggactcgccagagatatcaacaatatagactattacaaaaagaccaccaatgggcggcttccagtcaagtggatggctccagaagccctgtttgatagagtatacactcatcagagtgatgtctggtccttcggggtgttaatgtgggagatcttcactttagggggctcgccctacccagggattcccgtggaggaactttttaagctgctgaaggaaggacacagaatggataagccagccaactgcaccaacgaactgtacatgatgatgagggactgttggcatgcagtgccctcccagagaccaacgttcaagcagttggtagaagacttggatcgaattctcactctcacaaccaatgaggaatacttggacctcagccaacctctcgaacagtattcacctagttaccctgacacaagaagttcttgttcttcaggagatgattctgttttttctccagaccccatgccttacgaaccatgccttcctcagtatccacacataaacggcagtgttaaaacatgaatgactgtgtctgcctgtccccaaacaggacagcactgggaacctagctacactgagcagggagaccatgcctcccagagcttgttgtctccacttgtatatatggatcagaggagtaaataattggaaaagtaatcagcatatgtgtaaagatttatacagttgaaaacttgtaatcttccccaggaggagaagaaggtttctggagcagtggactgccacaagccaccatgtaacccctctcacctgccgtgcgtactggctgtggaccagtaggactcaaggtggacgtgcgttctgccttccttgttaattttgtaataattggagaagatttatgtcagcacacacttacagagcacaaatgcagtatataggtgctggatgtatgtaaatatattcaaattatgtataaatatatattatatatttacaaggagttattttttgtattgattttaaatggatgtcccaatgcacctagaaaattggtctctctttttttaatagctatttgctaaatgctgttcttacacataatttcttaattttcaccgagcagaggtggaaaaatacttttgctttcagggaaaatggtataacgttaatttattaataaattggtaatatacaaaacaattaatcatttatagttttttttgtaatttaagtggcatttctatgcaggcagcacagcagactagttaatctattgcttggacttaactagttatcagatcctttgaaaagagaatatttacaatatatgactaatttggggaaaatgaagttttgatttatttgtgtttaaatgctgctgtcagacgattgttcttagacctcctaaatgccccatattaaaagaactcattcataggaaggtgtttcattttggtgtgcaaccctgtcattacgtcaacgcaacgtctaactggacttcccaagataaatggtaccagcgtcctcttaaaagatgccttaatccattccttgaggacagaccttagttgaaatgatagcagaatgtgcttctctctggcagctggccttctgcttctgagttgcacattaatcagattagcctgtattctcttcagtgaattttgataatggcttccagactctttggcgttggagacgcctgttaggatcttcaagtcccatcatagaaaattgaaacacagagttgttctgctgatagttttggggatacgtccatctttttaagggattgctttcatctaattctggcaggacctcaccaaaagatccagcctcatacctacatcagacaaaatatcgccgttgttccttctgtactaaagtattgtgttttgctttggaaacacccactcactttgcaatagccgtgcaagatgaatgcagattacactgatcttatgtgttacaaaattggagaaagtatttaataaaacctgttaatttttatactgacaataaaaatgtttctacagatattaatgttaacaagacaaaataaatgtcacgcaacttatttttttaataaaaaaaaaaaaaaa";

    public TestVariantFactory() {
        this.refDict = HG19RefDictBuilder.build();
    }

    /**
     * Construct a new {@link Variant} object with the given values.
     * 
     * @param chrom
     *            numeric chromosome id
     * @param pos
     *            zero-based position of the variant
     * @param ref
     *            reference string
     * @param alt
     *            alt string
     * @param gt
     *            the Genotype to use
     * @param read
     *            depth the read depth to use
     * @param altAlleleID
     *            alternative allele ID
     * @return {@link Variant} with the setting
     */
    public Variant construct(int chrom, int pos, String ref, String alt, Genotype gt, int rd, int altAlleleID) {
        Allele refAllele = Allele.create(ref, true);
        Allele altAllele = Allele.create(alt);
        VariantContextBuilder vcBuilder = new VariantContextBuilder();

        // build Genotype
        GenotypeBuilder gtBuilder = new GenotypeBuilder("sample");
        setGenotype(gtBuilder, refAllele, altAllele, gt);
        gtBuilder.attribute("RD", rd);
        // System.err.println(gtBuilder.make().toString());

        // build VariantContext
        vcBuilder.loc("chr1", pos + 1, pos + ref.length());
        vcBuilder.alleles(Arrays.asList(refAllele, altAllele));
        vcBuilder.genotypes(gtBuilder.make());
        vcBuilder.attribute("RD", rd);
        // System.err.println(vcBuilder.make().toString());

        // build annotation list (for the one transcript we have below only)
        // public GenomeChange(GenomePosition pos, String ref, String alt) {
        final GenomePosition gPos = new GenomePosition(refDict, '+', chrom, pos, PositionType.ZERO_BASED);
        final GenomeChange change = new GenomeChange(gPos, ref, alt);
        final AnnotationBuilderDispatcher dispatcher;
        if (buildTM().txRegion.contains(gPos))
            dispatcher = new AnnotationBuilderDispatcher(buildTM(), change, new AnnotationBuilderOptions());
        else
            dispatcher = new AnnotationBuilderDispatcher(null, change, new AnnotationBuilderOptions());
        final AnnotationList annotations;
        try {
            annotations = new AnnotationList(Arrays.asList(dispatcher.build()));
        } catch (InvalidGenomeChange e) {
            throw new RuntimeException("Problem building annotation", e);
        }

        return new Variant(vcBuilder.make(), altAlleleID, annotations);
    }

    private void setGenotype(GenotypeBuilder gtb, Allele refAllele, Allele altAllele, Genotype gt) {
        switch (gt) {
            case HOMOZYGOUS_ALT:
                gtb.alleles(Arrays.asList(altAllele, altAllele));
                break;
            case HOMOZYGOUS_REF:
                gtb.alleles(Arrays.asList(refAllele, refAllele));
                break;
            case HETEROZYGOUS:
                gtb.alleles(Arrays.asList(refAllele, altAllele));
                break;
            default:
                break;
        }
    }

    public TranscriptModel buildTM() {
        TranscriptModelBuilder builder = TestTranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE);
        builder.setSequence(TRANSCRIPT_SEQ.toUpperCase());
        builder.setGeneSymbol("FGFR2");
        builder.setGeneID("ENTREZ2263");
        return builder.build();
    }

}