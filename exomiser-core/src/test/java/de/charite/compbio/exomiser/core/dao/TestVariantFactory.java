package de.charite.compbio.exomiser.core.dao;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

import java.util.Arrays;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.jannovar.annotation.Annotation;
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
 * The construction of {@link Variant} objects is quite complex but for tests, we would ideally have them for testing
 * our data sets. This class helps us with the construction.
 */
public class TestVariantFactory {

    private final ReferenceDictionary refDict;

    private static final String KG_LINE_FGFR2 = "uc021pzz.1\tchr10\t-\t123237843\t123357972\t123239370\t123353331\t18\t123237843,123243211,123244908,123246867,123247504,123256045,123258008,123260339,123263303,123274630,123276832,123279492,123298105,123310803,123324015,123324951,123353222,123357475,\t123239535,123243317,123245046,123246938,123247627,123256236,123258119,123260461,123263455,123274833,123276977,123279683,123298229,123310973,123324093,123325218,123353481,123357972,\tP21802\tuc021pzz.1";
    private static final String TRANSCRIPT_SEQ_FGFR2 = "ggcggcggctggaggagagcgcggtggagagccgagcgggcgggcggcgggtgcggagcgggcgagggagcgcgcgcggccgccacaaagctcgggcgccgcggggctgcatgcggcgtacctggcccggcgcggcgactgctctccgggctggcgggggccggccgcgagccccgggggccccgaggccgcagcttgcctgcgcgctctgagccttcgcaactcgcgagcaaagtttggtggaggcaacgccaagcctgagtcctttcttcctctcgttccccaaatccgagggcagcccgcgggcgtcatgcccgcgctcctccgcagcctggggtacgcgtgaagcccgggaggcttggcgccggcgaagacccaaggaccactcttctgcgtttggagttgctccccgcaaccccgggctcgtcgctttctccatcccgacccacgcggggcgcggggacaacacaggtcgcggaggagcgttgccattcaagtgactgcagcagcagcggcagcgcctcggttcctgagcccaccgcaggctgaaggcattgcgcgtagtccatgcccgtagaggaagtgtgcagatgggattaacgtccacatggagatatggaagaggaccggggattggtaccgtaaccatggtcagctggggtcgtttcatctgcctggtcgtggtcaccatggcaaccttgtccctggcccggccctccttcagtttagttgaggataccacattagagccagaagagccaccaaccaaataccaaatctctcaaccagaagtgtacgtggctgcgccaggggagtcgctagaggtgcgctgcctgttgaaagatgccgccgtgatcagttggactaaggatggggtgcacttggggcccaacaataggacagtgcttattggggagtacttgcagataaagggcgccacgcctagagactccggcctctatgcttgtactgccagtaggactgtagacagtgaaacttggtacttcatggtgaatgtcacagatgccatctcatccggagatgatgaggatgacaccgatggtgcggaagattttgtcagtgagaacagtaacaacaagagagcaccatactggaccaacacagaaaagatggaaaagcggctccatgctgtgcctgcggccaacactgtcaagtttcgctgcccagccggggggaacccaatgccaaccatgcggtggctgaaaaacgggaaggagtttaagcaggagcatcgcattggaggctacaaggtacgaaaccagcactggagcctcattatggaaagtgtggtcccatctgacaagggaaattatacctgtgtagtggagaatgaatacgggtccatcaatcacacgtaccacctggatgttgtggagcgatcgcctcaccggcccatcctccaagccggactgccggcaaatgcctccacagtggtcggaggagacgtagagtttgtctgcaaggtttacagtgatgcccagccccacatccagtggatcaagcacgtggaaaagaacggcagtaaatacgggcccgacgggctgccctacctcaaggttctcaaggccgccggtgttaacaccacggacaaagagattgaggttctctatattcggaatgtaacttttgaggacgctggggaatatacgtgcttggcgggtaattctattgggatatcctttcactctgcatggttgacagttctgccagcgcctggaagagaaaaggagattacagcttccccagactacctggagatagccatttactgcataggggtcttcttaatcgcctgtatggtggtaacagtcatcctgtgccgaatgaagaacacgaccaagaagccagacttcagcagccagccggctgtgcacaagctgaccaaacgtatccccctgcggagacaggtaacagtttcggctgagtccagctcctccatgaactccaacaccccgctggtgaggataacaacacgcctctcttcaacggcagacacccccatgctggcaggggtctccgagtatgaacttccagaggacccaaaatgggagtttccaagagataagctgacactgggcaagcccctgggagaaggttgctttgggcaagtggtcatggcggaagcagtgggaattgacaaagacaagcccaaggaggcggtcaccgtggccgtgaagatgttgaaagatgatgccacagagaaagacctttctgatctggtgtcagagatggagatgatgaagatgattgggaaacacaagaatatcataaatcttcttggagcctgcacacaggatgggcctctctatgtcatagttgagtatgcctctaaaggcaacctccgagaatacctccgagcccggaggccacccgggatggagtactcctatgacattaaccgtgttcctgaggagcagatgaccttcaaggacttggtgtcatgcacctaccagctggccagaggcatggagtacttggcttcccaaaaatgtattcatcgagatttagcagccagaaatgttttggtaacagaaaacaatgtgatgaaaatagcagactttggactcgccagagatatcaacaatatagactattacaaaaagaccaccaatgggcggcttccagtcaagtggatggctccagaagccctgtttgatagagtatacactcatcagagtgatgtctggtccttcggggtgttaatgtgggagatcttcactttagggggctcgccctacccagggattcccgtggaggaactttttaagctgctgaaggaaggacacagaatggataagccagccaactgcaccaacgaactgtacatgatgatgagggactgttggcatgcagtgccctcccagagaccaacgttcaagcagttggtagaagacttggatcgaattctcactctcacaaccaatgaggaatacttggacctcagccaacctctcgaacagtattcacctagttaccctgacacaagaagttcttgttcttcaggagatgattctgttttttctccagaccccatgccttacgaaccatgccttcctcagtatccacacataaacggcagtgttaaaacatgaatgactgtgtctgcctgtccccaaacaggacagcactgggaacctagctacactgagcagggagaccatgcctcccagagcttgttgtctccacttgtatatatggatcagaggagtaaataattggaaaagtaatcagcatatgtgtaaagatttatacagttgaaaacttgtaatcttccccaggaggagaagaaggtttctggagcagtggactgccacaagccaccatgtaacccctctcacctgccgtgcgtactggctgtggaccagtaggactcaaggtggacgtgcgttctgccttccttgttaattttgtaataattggagaagatttatgtcagcacacacttacagagcacaaatgcagtatataggtgctggatgtatgtaaatatattcaaattatgtataaatatatattatatatttacaaggagttattttttgtattgattttaaatggatgtcccaatgcacctagaaaattggtctctctttttttaatagctatttgctaaatgctgttcttacacataatttcttaattttcaccgagcagaggtggaaaaatacttttgctttcagggaaaatggtataacgttaatttattaataaattggtaatatacaaaacaattaatcatttatagttttttttgtaatttaagtggcatttctatgcaggcagcacagcagactagttaatctattgcttggacttaactagttatcagatcctttgaaaagagaatatttacaatatatgactaatttggggaaaatgaagttttgatttatttgtgtttaaatgctgctgtcagacgattgttcttagacctcctaaatgccccatattaaaagaactcattcataggaaggtgtttcattttggtgtgcaaccctgtcattacgtcaacgcaacgtctaactggacttcccaagataaatggtaccagcgtcctcttaaaagatgccttaatccattccttgaggacagaccttagttgaaatgatagcagaatgtgcttctctctggcagctggccttctgcttctgagttgcacattaatcagattagcctgtattctcttcagtgaattttgataatggcttccagactctttggcgttggagacgcctgttaggatcttcaagtcccatcatagaaaattgaaacacagagttgttctgctgatagttttggggatacgtccatctttttaagggattgctttcatctaattctggcaggacctcaccaaaagatccagcctcatacctacatcagacaaaatatcgccgttgttccttctgtactaaagtattgtgttttgctttggaaacacccactcactttgcaatagccgtgcaagatgaatgcagattacactgatcttatgtgttacaaaattggagaaagtatttaataaaacctgttaatttttatactgacaataaaaatgtttctacagatattaatgttaacaagacaaaataaatgtcacgcaacttatttttttaataaaaaaaaaaaaaaa";
    private static final String KG_LINE_SHH2 = "uc003wmk.1\tchr7\t-\t155595557\t155604967\t155595593\t155604816\t3\t155595557,155598989,155604516,\t155596420,155599251,155604967,\tQ15465\tuc003wmk.1";
    private static final String TRANSCRIPT_SEQ_SHH2 = "gcgaggcagccagcgagggagagagcgagcgggcgagccggagcgaggaagggaaagcgcaagagagagcgcacacgcacacacccgccgcgcgcactcgcgcacggacccgcacggggacagctcggaagtcatcagttccatgggcgagatgctgctgctggcgagatgtctgctgctagtcctcgtctcctcgctgctggtatgctcgggactggcgtgcggaccgggcagggggttcgggaagaggaggcaccccaaaaagctgacccctttagcctacaagcagtttatccccaatgtggccgagaagaccctaggcgccagcggaaggtatgaagggaagatctccagaaactccgagcgatttaaggaactcacccccaattacaaccccgacatcatatttaaggatgaagaaaacaccggagcggacaggctgatgactcagaggtgtaaggacaagttgaacgctttggccatctcggtgatgaaccagtggccaggagtgaaactgcgggtgaccgagggctgggacgaagatggccaccactcagaggagtctctgcactacgagggccgcgcagtggacatcaccacgtctgaccgcgaccgcagcaagtacggcatgctggcccgcctggcggtggaggccggcttcgactgggtgtactacgagtccaaggcacatatccactgctcggtgaaagcagagaactcggtggcggccaaatcgggaggctgcttcccgggctcggccacggtgcacctggagcagggcggcaccaagctggtgaaggacctgagccccggggaccgcgtgctggcggcggacgaccagggccggctgctctacagcgacttcctcactttcctggaccgcgacgacggcgccaagaaggtcttctacgtgatcgagacgcgggagccgcgcgagcgcctgctgctcaccgccgcgcacctgctctttgtggcgccgcacaacgactcggccaccggggagcccgaggcgtcctcgggctcggggccgccttccgggggcgcactggggcctcgggcgctgttcgccagccgcgtgcgcccgggccagcgcgtgtacgtggtggccgagcgtgacggggaccgccggctcctgcccgccgctgtgcacagcgtgaccctaagcgaggaggccgcgggcgcctacgcgccgctcacggcccagggcaccattctcatcaaccgggtgctggcctcgtgctacgcggtcatcgaggagcacagctgggcgcaccgggccttcgcgcccttccgcctggcgcacgcgctcctggctgcactggcgcccgcgcgcacggaccgcggcggggacagcggcggcggggaccgcgggggcggcggcggcagagtagccctaaccgctccaggtgctgccgacgctccgggtgcgggggccaccgcgggcatccactggtactcgcagctgctctaccaaataggcacctggctcctggacagcgaggccctgcacccgctgggcatggcggtcaagtccagctgaagccggggggccgggggaggggcgcgggagggggcg";
    private static final String KG_LINE_GNRHR2 = "uc009wiv.3\t\tchr1\t\t\t\t-\t\t\t145509751\t\t\t145515899\t\t\t145509859\t\t\t145510794\t\t\t4\t\t\t145509751,145510727,145515188,145515669,\t\t\t\t145510278,145510938,145515409,145515899,\t\t\t\tQ96P88\t\tuc009wiv.3";
    private static final String TRANSCRIPT_SEQ_GNRHR2 = "atcttcgttccctgcagaaccttgacagttgaacaagtgacctcctccagaacagatggagagtctccagaagcagaggctttagtgaacgaaattcgcaataatcagctccagatcctgaaaaggagggcgaagaatcagtggccaaagctaaccgcttcatacccacacttcatcctcctcagtttctctccaggccaccatgtctgcaggcaacggcaccccttgggatgccacctggaatatcactgttcaatggctggctgtggacatcgcatgtcggacactgatgttcctgaaactaatggccacgtattctgcagctttcctgcctgtggtcattggattggaccgccaggcagcagtactcaacccgcttggatcccgttcaggtgtaaggaaacttctgggggcagcctggggacttagtttcctgcttgccttcccccagctgttcctgttccacacggtccactgagctggcccagtccctttcactcagtgtgtcaccaaaggcagcttcaaggctcaatggcaagagaccacctataacctcttcaccttctgctgcctctttctgctgccactgactgccatggccatctgctatagccgcattgtcctcagtgtgtccaggccccagacaaggaaggggagccatgcccctgctggtgaatttgccctcccccgctcctttgacaattgtccccgtgttcgtctccgggccctgagactggccctgcttatcttgctgaccttcatcctctgctggacaccttattacctactgggtatgtggtactggttctcccccaccatgctaactgaagtccctcccagcctgagccacatccttttcctcttgggcctcctcaatgctcctttggatcctctcctctatggggccttcacccttggctgccgaagagggcaccaagaacttagtatagactcttctaaagaagggtctgggagaatgctccaagaggagattcatgcctttagacagctggaagtacaaaaaactgtgacatcaagaagggcaggagaaacaaaaggcatttctataacatctatctgatcctaacagagtatgtaggaacagaatagtaagtctttagtgccataagatcttaacatctcacttctactcctgctctcctagttccccccaaaaaagaaatactga";
    private static final String KG_LINE_RBM8A = "uc001ent.2\t\tchr1\t\t\t\t+\t\t\t145507556\t\t\t145513535\t\t\t145507666\t\t\t145509211\t\t\t6\t\t\t145507556,145508015,145508206,145508474,145508915,145509165,\t\t\t\t145507733,145508075,145508284,145508611,145509052,145513535,\t\t\t\tQ9Y5S9uc001ent.2";
    private static final String TRANSCRIPT_SEQ_RBM8A = "agagttagcctttgattggtcagcttgactggcgacctttcccctctgcgacagtttcccgaggtacctagtgtctgagcggcacagacgagatctcgatcgaaggcgagatggcggacgtgctagatcttcacgaggctgggggcgaagatttcgccatggatgaggatggggacgagagcattcacaaactgaaagaaaaagcgaagaaacggaagggtcgcggctttggctccgaagaggggtcccgagcgcggatgcgtgaggattatgacagcgtggagcaggatggcgatgaacccggaccacaacgctctgttgaaggctggattctctttgtaactggagtccatgaggaagccaccgaagaagacatacacgacaaattcgcagaatatggggaaattaaaaacattcatctcaacctcgacaggcgaacaggatatctgaaggggtatactctagttgaatatgaaacatacaaggaagcccaggctgctatggagggactcaatggccaggatttgatgggacagcccatcagcgttgactggtgttttgttcggggtccaccaaaaggcaagaggagaggtggccgaagacgcagcagaagtccagaccggagacgtcgctgacaggtcctctgttgtccaggtgttctcttcaagattccatttgaccatgcagccttggacaaataggactggggtggaacttgctgtgtttatatttaatctcttaccgtatatgcgtagtatttgagttgcgaataaatgttccatttttgttttctacatttaatgttactttcctgtcctaaaattgaaagttctaaagcatagcaaggctgtatggatcattgtgaagatacttctagggactgaactctatgtatttcttttttttcttttttttgagatagagtcttgctgtgttacccagggtggattgcagctgatcatagctcactgcagcttcaaactcttgggctcaagccatccttctgcctcactgtccctagtagttgggattacaggcacatgccaccatgcccagctaaatttttaatatttttgtagagatggggtcttgctgtgttacctgggctagttatgtgagtttctatattagacatagtctcaagtttcaggtagggtttaaagtagagacactggtcagtatttcttttttggggggaactaggagagcaggagtagaagtgagatgttaagatcttatggcactaaagacttactattctgttcctacatactctgttaggatcagatagatgttatagaaatgccttttgtttctcctgcccttcttgatgtcacagttttttgtacttccagctgtctaaaggcatgaatctcctcttggagcattctcccagacccttctttagaagagtctatactaagttcttggtgccctcttcggcagccaagggtgaaggccccatagaggagaggatccaaaggagcattgaggaggcccaagaggaaaaggatgtggctcaggctgggagggacttcagttagcatggtgggggagaaccagtaccacatacccagtaggtaataaggtgtccagcagaggatgaaggtcagcaagataagcagggccagtctcagggcccggagacgaacacggggacaattgtcaaaggagcgggggagggcaaattcaccagcaggggctaggaatttagaaaatatactgtaattcagacactcagcttctgatctgagtatagggtgaattgatggaggggcatagctagtgagacagagctcacctcctacaaggaggagaatgttgcaaaccgttttccccttcccaacctgggactatatgatttcttacccccagggattatgatagaaatatgaagccaccaagtctagacttgatggtgttcaagaataaataatactgattgcctccctagtccttgtccagctaactcagctgtttataattgaagggattcaacaaaattatctctagcatcaggtgctagacatggttagaatctcaccatggtttagtgactggtagatagctattaggtaggtagataaataaatgatgctagaggcaacaggtctagggttaaggattaaggcctgggaattggagtctcaccatggctccccttccttgtctggggcctggacacactgaggacaatgcggctatagcagatggccatggcagtcagtggcagcagaaagaggcagcagaaggtgaagaggttataggtggtctcttgccattgagccttgaagctgcctttggtgacacactgagtgaaagggactgggccagctcagtggaccgtgtggaacaggaacagctggagtggagttgaggactattagaactggttcccctcaccacccaacctacccacctatgtcatactgtctcctcccaattcatccttaattccaagtgaagcagcacagtgctgagaaacagttcatccatggtgccatgttaaagaagttggaaatatatcttgaaaatcctatcttccttttaggcttgaatatgatgctgaacagtaagtttgttaaatcttggaacttaaaacaatcctgctttctcaagtactattctaacattgcgctttataagggatgatatttctaccacctcactcatatttttagctgaaatgattttcctggtatgtctgttattttgtggaaaaagaaatattgtgtaaaatgggtgctgccaaaattccaggccattttgcagggactctgaagtgacctttagtagtaatagtcttatgtgcagtaactataatggtaaagaatgttaaataataaaatttaacattttccaaatgctattgggctgcccctccccctttttgttaaattgctgggttttccaactgaatcagtaaaaactatttctgtttagagctacaaggttaaagtgcctgctttccagtaatggagattgagtcactattaatttgataaaaggtaagctcagtaggcatcagattcctagatacaaggcatttgggaaagtgattttagcagacatgagggacatttaggaaagatgaatagtttcagcctaagagaattttgtgaactgtttggagttacgatcaggctactctgagctagttgggaaatggtctttcctcttcccatctcttgcattcatatatttctaagttttttttttttttttgtttttgtgctctgcctaagaagtgcttgagaattgtgaggagtataaaaatagtcaaagctggctgggcgcggtggctcacgcctgtaatcccagcactttgggaggctgaggcgggcggatcacgaggttaggagatggagaccatcctggctaacacagtgaaaccctgtctgtactaaagatacaaaaaactagccgggcgtggtggtaggtgcctgtagtcccagctacttgggaactcgggaggctgaggcaggagaatgacctgaacccaggaggcggagcttgcagtgagcagagattgcgccattgcactccagcctgggcgacagagtaagactgtctccaaaaaaaaaaaaaataataatcaaagctcttggatttatagtttggtccacagccttgttttgatctttcctttatcctgttttattgccatttaccacgtactgtagaaacatccctttcaactgctgataacttggaaacaagcctacaaaaataagtaatttctaactactcctaatactacctataactacccctaagcccttaccactctaacgtgacattattaaattttttattttattaacactaatattttaactacaattacagcatatgggcaatacagaatttacctaaaaggatactaatttggaacaaaaaaaatcacctttcgcacatgtatcatgtcacaaccagtttgccattgaaacaaatagaggttgcaaatattgtcagattgtcaggctgtaagaaaggatgaaattcatttcccattgcatcatcttgtggcccatggatttcaagtgccttagccaaaatcatatagctagttagcagtagagccgagactcagaaaaaaacaaagtaaaacaggcagactgaaacaaaaagtcttctaattcccagtccacatgtaaaatttgcttcatataaacaaacctaattgtaaatggcactgtagcaacaggcttctttttaacacttggattggtaaaggtcttgtttgcaacatattagaagtattatttttctctttcccccccaccccacccccaacagagtctggctctgccgcccacgccggagtgcagtggtgcagtcttggctcactgcagcctccacttcccaggttcaagcaattctcgtgcctcagcctcctgagtagttgggattacaggtgctcaccactatacccggctaatttttgtatttttagtagagatggggtttcgtcatgttggccaggttggtcttaaactcctgacctcaagtgatccacccaccttggccttccaaaatgctgggattacaggcttgagccaccaggcctttctttgttcttaggagtatagtcagactaacttctagtagttatatttctaataattgaggatgtaagtaaggatcaaatcttaaatcagtataatgcattgtcattccagagataaatcctagacccttcttggcctccttctgacataattctaatcctacagtctcagagatgctgttgtatcctgccccccaaccccatgatagtgatagtggtttttgccttgaaggaattgctttgtatttagcttttccccctctagatttctagttccttttcagtattggattggatttgagatttgattaacctagtactcaggttcagatgctcgcctctttgcaattttaacactcattcgacaataaagtcagtaaaaaacacaaaaaaaaaaaaaaaa";

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
     * @param qual
     *            phred-scale quality
     * @return {@link Variant} with the setting
     */
    public Variant constructVariant(int chrom, int pos, String ref, String alt, Genotype gt, int rd, int altAlleleID,
            double qual) {
        // build annotation list (for the one transcript we have below only)
        final GenomePosition gPos = new GenomePosition(refDict, '+', chrom, pos, PositionType.ZERO_BASED);
        final GenomeChange change = new GenomeChange(gPos, ref, alt);
        final AnnotationBuilderDispatcher dispatcher;
        final TranscriptModel tmFGFR2 = buildTMForFGFR2();
        final TranscriptModel tmSHH = buildTMForSHH();
        if (tmFGFR2.txRegion.contains(gPos))
            dispatcher = new AnnotationBuilderDispatcher(tmFGFR2, change, new AnnotationBuilderOptions());
        else if (tmSHH.txRegion.contains(gPos))
            dispatcher = new AnnotationBuilderDispatcher(tmSHH, change, new AnnotationBuilderOptions());
        else
            dispatcher = new AnnotationBuilderDispatcher(null, change, new AnnotationBuilderOptions());
        final AnnotationList annotations;
        try {
            Annotation anno = dispatcher.build();
            if (anno != null)
                annotations = new AnnotationList(Arrays.asList(anno));
            else
                annotations = new AnnotationList(Arrays.<Annotation> asList());
        } catch (InvalidGenomeChange e) {
            throw new RuntimeException("Problem building annotation", e);
        }

        return new Variant(constructVariantContext(chrom, pos, ref, alt, gt, rd, qual), altAlleleID, annotations);
    }

    public Variant constructVariant(int chrom, int pos, String ref, String alt, Genotype gt, int rd, int altAlleleID) {
        return constructVariant(chrom, pos, ref, alt, gt, rd, 0, 20.0);
    }

    public VariantContext constructVariantContext(int chrom, int pos, String ref, String alt, Genotype gt, int rd) {
        return constructVariantContext(chrom, pos, ref, alt, gt, rd, 20.0);
    }

    public VariantContext constructVariantContext(int chrom, int pos, String ref, String alt, Genotype gt, int rd,
            double qual) {
        Allele refAllele = Allele.create(ref, true);
        Allele altAllele = Allele.create(alt);
        VariantContextBuilder vcBuilder = new VariantContextBuilder();

        // build Genotype
        GenotypeBuilder gtBuilder = new GenotypeBuilder("sample");
        setGenotype(gtBuilder, refAllele, altAllele, gt);
        gtBuilder.attribute("RD", rd);
        // System.err.println(gtBuilder.make().toString());

        // build VariantContext
        vcBuilder.loc("chr" + chrom, pos + 1, pos + ref.length());
        vcBuilder.alleles(Arrays.asList(refAllele, altAllele));
        vcBuilder.genotypes(gtBuilder.make());
        vcBuilder.attribute("RD", rd);
        vcBuilder.log10PError(-0.1 * qual);
        // System.err.println(vcBuilder.make().toString());

        return vcBuilder.make();
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

    /**
     * @return {@link TranscriptModel} for gene FGFR2.
     */
    public TranscriptModel buildTMForFGFR2() {
        TranscriptModelBuilder builder = TestTranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE_FGFR2);
        builder.setSequence(TRANSCRIPT_SEQ_FGFR2.toUpperCase());
        builder.setGeneSymbol("FGFR2");
        builder.setGeneID("ENTREZ2263");
        return builder.build();
    }

    /**
     * @return {@link TranscriptModel} for gene SHH.
     */
    public TranscriptModel buildTMForSHH() {
        TranscriptModelBuilder builder = TestTranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE_SHH2);
        builder.setSequence(TRANSCRIPT_SEQ_SHH2.toUpperCase());
        builder.setGeneSymbol("SHH");
        builder.setGeneID("ENTREZ6469");
        return builder.build();
    }

    /**
     * GNRHR2 overlaps with RBM8A.
     * 
     * @return {@link TranscriptModel} for gene GNRHR2.
     */
    public TranscriptModel buildTMForGNRHR2A() {
        TranscriptModelBuilder builder = TestTranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE_GNRHR2);
        builder.setSequence(TRANSCRIPT_SEQ_GNRHR2.toUpperCase());
        builder.setGeneSymbol("GNRHR2");
        builder.setGeneID("ENTREZ114814");
        return builder.build();
    }

    /**
     * RBM8A overlaps with GNRHR2.
     * 
     * @return {@link TranscriptModel} for gene RBM8A.
     */
    public TranscriptModel buildTMForRBM8A() {
        TranscriptModelBuilder builder = TestTranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE_RBM8A);
        builder.setSequence(TRANSCRIPT_SEQ_RBM8A.toUpperCase());
        builder.setGeneSymbol("RBM8A");
        builder.setGeneID("ENTREZ9939");
        return builder.build();
    }

}