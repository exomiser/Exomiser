/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import java.util.Arrays;
import java.util.Collection;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.AnnotationLocation;
import de.charite.compbio.jannovar.annotation.InvalidGenomeChange;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderDispatcher;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableList;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DaoTestConfig.class)
@Sql(scripts = { "file:src/test/resources/sql/create_frequency.sql",
        "file:src/test/resources/sql/frequencyDaoTestData.sql" })
public class DefaultFrequencyDaoTest {

    @Autowired
    private DefaultFrequencyDao instance;

    Variant variantNotInDatabase;
    Variant variantInDatabaseWithRsId;

    private static final FrequencyData NO_DATA = new FrequencyData(null, null, null, null, null);

    @Before
    public void setUp() {
        this.variantNotInDatabase = new VariantFactory().construct(1, 123, "CT", "CG", Genotype.HOMOZYGOUS_ALT, 30, 1);
        this.variantInDatabaseWithRsId = new VariantFactory().construct(10, 123256213, "CA", "CC",
                Genotype.HOMOZYGOUS_ALT, 30, 1);
    }

    @Test
    public void testVariantNotInDatabaseReturnsAnEmptyFrequencyData() {
        FrequencyData result = instance.getFrequencyData(variantNotInDatabase);

        assertThat(result, equalTo(NO_DATA));
        assertThat(result.representedInDatabase(), is(false));
    }

    @Test
    public void testVariantInDatabaseReturnsFrequencyData() {
        FrequencyData result = instance.getFrequencyData(variantInDatabaseWithRsId);
        FrequencyData expected = new FrequencyData(new RsId(121918506), new Frequency(0.01f), new Frequency(0.02f),
                new Frequency(0.03f), new Frequency(0.04f));
        assertThat(result, equalTo(expected));
        assertThat(result.representedInDatabase(), is(true));
    }

    /**
     * Allows the easy creation of transcript models from knownGenes.txt.gz lines.
     *
     * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
     */
    private static class TranscriptModelFactory {

        /**
         * Helper function to parse a knownGenes.txt.gz line into a TranscriptModel.
         *
         * @param refDict
         *            reference dictionary
         * @param s
         *            The knownGeneList line to parse.
         */
        public static TranscriptModelBuilder parseKnownGenesLine(ReferenceDictionary refDict, String s) {
            String[] fields = s.split("\t");
            TranscriptModelBuilder result = new TranscriptModelBuilder();
            result.setAccession(fields[0]);

            int chr = refDict.contigID.get(fields[1].substring(3));

            result.setStrand(fields[2].charAt(0));
            GenomeInterval txRegion = new GenomeInterval(refDict, '+', chr, Integer.parseInt(fields[3]) + 1,
                    Integer.parseInt(fields[4]), PositionType.ONE_BASED);
            result.setTxRegion(txRegion);
            GenomeInterval cdsRegion = new GenomeInterval(refDict, '+', chr, Integer.parseInt(fields[5]) + 1,
                    Integer.parseInt(fields[6]), PositionType.ONE_BASED);
            result.setCdsRegion(cdsRegion);

            int exonCount = Integer.parseInt(fields[7]);
            String[] startFields = fields[8].split(",");
            String[] endFields = fields[9].split(",");
            for (int i = 0; i < exonCount; ++i) {
                GenomeInterval exonRegion = new GenomeInterval(refDict, '+', chr, Integer.parseInt(startFields[i]) + 1,
                        Integer.parseInt(endFields[i]), PositionType.ONE_BASED);
                result.addExonRegion(exonRegion);
            }

            return result;
        }
    }

    /**
     * Helper class for constructing {@link Variant} objects for tests.
     * 
     * The construction of {@link Variant} objects is quite complex but for tests, we would ideally have them for
     * testing our data sets. This class helps us with the construction.
     */
    private static class VariantFactory {

        private final ReferenceDictionary refDict;
        // private final JannovarData jannovarData;

        private static final String KG_LINE = "uc021pzz.1\tchr10\t-\t123237843\t123357972\t123239370\t123353331\t18\t123237843,123243211,123244908,123246867,123247504,123256045,123258008,123260339,123263303,123274630,123276832,123279492,123298105,123310803,123324015,123324951,123353222,123357475,\t123239535,123243317,123245046,123246938,123247627,123256236,123258119,123260461,123263455,123274833,123276977,123279683,123298229,123310973,123324093,123325218,123353481,123357972,\tP21802\tuc021pzz.1";
        private static final String TRANSCRIPT_SEQ = "ggcggcggctggaggagagcgcggtggagagccgagcgggcgggcggcgggtgcggagcgggcgagggagcgcgcgcggccgccacaaagctcgggcgccgcggggctgcatgcggcgtacctggcccggcgcggcgactgctctccgggctggcgggggccggccgcgagccccgggggccccgaggccgcagcttgcctgcgcgctctgagccttcgcaactcgcgagcaaagtttggtggaggcaacgccaagcctgagtcctttcttcctctcgttccccaaatccgagggcagcccgcgggcgtcatgcccgcgctcctccgcagcctggggtacgcgtgaagcccgggaggcttggcgccggcgaagacccaaggaccactcttctgcgtttggagttgctccccgcaaccccgggctcgtcgctttctccatcccgacccacgcggggcgcggggacaacacaggtcgcggaggagcgttgccattcaagtgactgcagcagcagcggcagcgcctcggttcctgagcccaccgcaggctgaaggcattgcgcgtagtccatgcccgtagaggaagtgtgcagatgggattaacgtccacatggagatatggaagaggaccggggattggtaccgtaaccatggtcagctggggtcgtttcatctgcctggtcgtggtcaccatggcaaccttgtccctggcccggccctccttcagtttagttgaggataccacattagagccagaagagccaccaaccaaataccaaatctctcaaccagaagtgtacgtggctgcgccaggggagtcgctagaggtgcgctgcctgttgaaagatgccgccgtgatcagttggactaaggatggggtgcacttggggcccaacaataggacagtgcttattggggagtacttgcagataaagggcgccacgcctagagactccggcctctatgcttgtactgccagtaggactgtagacagtgaaacttggtacttcatggtgaatgtcacagatgccatctcatccggagatgatgaggatgacaccgatggtgcggaagattttgtcagtgagaacagtaacaacaagagagcaccatactggaccaacacagaaaagatggaaaagcggctccatgctgtgcctgcggccaacactgtcaagtttcgctgcccagccggggggaacccaatgccaaccatgcggtggctgaaaaacgggaaggagtttaagcaggagcatcgcattggaggctacaaggtacgaaaccagcactggagcctcattatggaaagtgtggtcccatctgacaagggaaattatacctgtgtagtggagaatgaatacgggtccatcaatcacacgtaccacctggatgttgtggagcgatcgcctcaccggcccatcctccaagccggactgccggcaaatgcctccacagtggtcggaggagacgtagagtttgtctgcaaggtttacagtgatgcccagccccacatccagtggatcaagcacgtggaaaagaacggcagtaaatacgggcccgacgggctgccctacctcaaggttctcaaggccgccggtgttaacaccacggacaaagagattgaggttctctatattcggaatgtaacttttgaggacgctggggaatatacgtgcttggcgggtaattctattgggatatcctttcactctgcatggttgacagttctgccagcgcctggaagagaaaaggagattacagcttccccagactacctggagatagccatttactgcataggggtcttcttaatcgcctgtatggtggtaacagtcatcctgtgccgaatgaagaacacgaccaagaagccagacttcagcagccagccggctgtgcacaagctgaccaaacgtatccccctgcggagacaggtaacagtttcggctgagtccagctcctccatgaactccaacaccccgctggtgaggataacaacacgcctctcttcaacggcagacacccccatgctggcaggggtctccgagtatgaacttccagaggacccaaaatgggagtttccaagagataagctgacactgggcaagcccctgggagaaggttgctttgggcaagtggtcatggcggaagcagtgggaattgacaaagacaagcccaaggaggcggtcaccgtggccgtgaagatgttgaaagatgatgccacagagaaagacctttctgatctggtgtcagagatggagatgatgaagatgattgggaaacacaagaatatcataaatcttcttggagcctgcacacaggatgggcctctctatgtcatagttgagtatgcctctaaaggcaacctccgagaatacctccgagcccggaggccacccgggatggagtactcctatgacattaaccgtgttcctgaggagcagatgaccttcaaggacttggtgtcatgcacctaccagctggccagaggcatggagtacttggcttcccaaaaatgtattcatcgagatttagcagccagaaatgttttggtaacagaaaacaatgtgatgaaaatagcagactttggactcgccagagatatcaacaatatagactattacaaaaagaccaccaatgggcggcttccagtcaagtggatggctccagaagccctgtttgatagagtatacactcatcagagtgatgtctggtccttcggggtgttaatgtgggagatcttcactttagggggctcgccctacccagggattcccgtggaggaactttttaagctgctgaaggaaggacacagaatggataagccagccaactgcaccaacgaactgtacatgatgatgagggactgttggcatgcagtgccctcccagagaccaacgttcaagcagttggtagaagacttggatcgaattctcactctcacaaccaatgaggaatacttggacctcagccaacctctcgaacagtattcacctagttaccctgacacaagaagttcttgttcttcaggagatgattctgttttttctccagaccccatgccttacgaaccatgccttcctcagtatccacacataaacggcagtgttaaaacatgaatgactgtgtctgcctgtccccaaacaggacagcactgggaacctagctacactgagcagggagaccatgcctcccagagcttgttgtctccacttgtatatatggatcagaggagtaaataattggaaaagtaatcagcatatgtgtaaagatttatacagttgaaaacttgtaatcttccccaggaggagaagaaggtttctggagcagtggactgccacaagccaccatgtaacccctctcacctgccgtgcgtactggctgtggaccagtaggactcaaggtggacgtgcgttctgccttccttgttaattttgtaataattggagaagatttatgtcagcacacacttacagagcacaaatgcagtatataggtgctggatgtatgtaaatatattcaaattatgtataaatatatattatatatttacaaggagttattttttgtattgattttaaatggatgtcccaatgcacctagaaaattggtctctctttttttaatagctatttgctaaatgctgttcttacacataatttcttaattttcaccgagcagaggtggaaaaatacttttgctttcagggaaaatggtataacgttaatttattaataaattggtaatatacaaaacaattaatcatttatagttttttttgtaatttaagtggcatttctatgcaggcagcacagcagactagttaatctattgcttggacttaactagttatcagatcctttgaaaagagaatatttacaatatatgactaatttggggaaaatgaagttttgatttatttgtgtttaaatgctgctgtcagacgattgttcttagacctcctaaatgccccatattaaaagaactcattcataggaaggtgtttcattttggtgtgcaaccctgtcattacgtcaacgcaacgtctaactggacttcccaagataaatggtaccagcgtcctcttaaaagatgccttaatccattccttgaggacagaccttagttgaaatgatagcagaatgtgcttctctctggcagctggccttctgcttctgagttgcacattaatcagattagcctgtattctcttcagtgaattttgataatggcttccagactctttggcgttggagacgcctgttaggatcttcaagtcccatcatagaaaattgaaacacagagttgttctgctgatagttttggggatacgtccatctttttaagggattgctttcatctaattctggcaggacctcaccaaaagatccagcctcatacctacatcagacaaaatatcgccgttgttccttctgtactaaagtattgtgttttgctttggaaacacccactcactttgcaatagccgtgcaagatgaatgcagattacactgatcttatgtgttacaaaattggagaaagtatttaataaaacctgttaatttttatactgacaataaaaatgtttctacagatattaatgttaacaagacaaaataaatgtcacgcaacttatttttttaataaaaaaaaaaaaaaa";

        public VariantFactory() {
            this.refDict = HG19RefDictBuilder.build();
            // this.jannovarData = buildJannovarData();
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

        // private JannovarData buildJannovarData() {
        // return new JannovarData(refDict, ImmutableList.of(buildTM()));
        // }

        private TranscriptModel buildTM() {
            TranscriptModelBuilder builder = TranscriptModelFactory.parseKnownGenesLine(refDict, KG_LINE);
            builder.setSequence(TRANSCRIPT_SEQ.toUpperCase());
            builder.setGeneSymbol("FGFR2");
            builder.setGeneID("ENTREZ2263");
            return builder.build();
        }

    }

}
