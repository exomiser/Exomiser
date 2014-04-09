package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.io.FileOperationStatus;
import de.charite.compbio.exomiser.reference.Frequency;
import jannovar.common.Constants;
import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.reference.TranscriptModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to parse the dbSNP file {@code 00-All.vcf} which is
 * available in gzipped form at the dbSNP FTP site. We use a collection of
 * {@link jannovar.reference.TranscriptModel} objects in order to filter the
 * dbSNP variants to those that are located either within an exon or close to an
 * exon (see {@link #FLANKING} for the distance threshold).
 *
 * @author Peter Robinson
 * @version 0.08 (8 December, 2013)
 * @see <a
 * href="ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/">ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/</a>
 */
public class DbSnpFrequencyParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(DbSnpFrequencyParser.class);

    /**
     * Total number of unique exons
     */
    private int n_exons;
    /**
     * Total number of variants found to be located in exons
     */
    private int n_exonic_vars;
    /**
     * Total number of variants found to be located outside of exonic sequences
     */
    private int n_non_exonic_vars;
    /**
     * Total number of duplicate entries in dbSNP.
     */
    private int n_duplicates;
    /**
     * List of all variants that pass threshold for inclusion in database
     * because they are exonic
     */
    private final List<Frequency> frequencyList;
    /**
     * Threshold distance from an exon to be considered as flanking
     */
    private final static int FLANKING = 50;

    /**
     * Map of Chromosomes
     */
    private final HashMap<Byte, ChromosomalExonLocations> chromosomeMap;

    public DbSnpFrequencyParser(File ucscSerializedData, List<Frequency> frequencyList) {
        this.frequencyList = frequencyList;
        chromosomeMap = new HashMap<>();
        deserializeUCSCdata(ucscSerializedData.getAbsolutePath());
    }

    /**
     * Inputs the list of known genes from the serialized data file. The
     * serialized file was originally created by parsing the three UCSC known
     * gene files.
     */
    private void deserializeUCSCdata(String serializedFile) {
        logger.info("De-serializing known Exon locations from UCSC data file: {}", serializedFile);
        SerializationManager manager = new SerializationManager();
        ArrayList<TranscriptModel> transcriptList = null;
        try {
            transcriptList = manager.deserializeKnownGeneList(serializedFile);
        } catch (JannovarException e) {
            logger.error("Unable to deserialize the TranscriptModel serialized file.", e);
        }

        for (TranscriptModel kgl : transcriptList) {
            byte chromosome = kgl.getChromosome();
            if (!chromosomeMap.containsKey(chromosome)) {
                ChromosomalExonLocations exonLocations = new ChromosomalExonLocations(chromosome);
                exonLocations.addGene(kgl);
                //System.out.println("Adding chromosome for " + chromosome);
                chromosomeMap.put(chromosome, exonLocations);
            } else {
                ChromosomalExonLocations exonLocations = chromosomeMap.get(chromosome);
                exonLocations.addGene(kgl);
            }
        }
        logger.info("Parsed " + serializedFile + " and added " + n_exons + " exons");
    }

    /**
     * Parse the main dbSNP file
     *
     * @param inPath Complete path to the dbSNPfile {@code 00-All.vcf}
     * @param outPath
     * @return
     */
    @Override
    public FileOperationStatus parse(String inPath, String outPath) {

        long startTime = System.currentTimeMillis();

        logger.info("Parsing file: {}", inPath);

        if (chromosomeMap.isEmpty()) {
            logger.error("Unable to parse file: {} as the chromosomeMap is empty", inPath);
            return FileOperationStatus.FAILURE;
        }

        try {
            FileInputStream fis = new FileInputStream(inPath);
            InputStream is;

            /* First, attempt to use a gzip input stream, if this doesn't work, open it as usual */
            try {
                is = new GZIPInputStream(fis);
            } catch (IOException exp) {
                fis.close();
                is = fis = new FileInputStream(inPath);
            }

            FileChannel fc = fis.getChannel();
            long fileSize = fc.size();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int vcount = 0; /* Keeps track of number of lines (variants) parsed. */

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                vcount++;
                Frequency frequency = VCF2FrequencyParser.parseVCFline(line); /* Method of superclass, instantiates various class variables  */

                checkVariantForExomalLocationAndOutput(frequency);

                long now = System.currentTimeMillis();
                if (now - startTime > 2000) {
                    long filePosition = fc.position();
                    long permil = filePosition * 1000 / fileSize;
                    logger.info(String.format("%d.%d%% %d variants parsed", permil / 10, permil % 10, vcount));
                    startTime = now;
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing dbSNP file: ", e);
            return FileOperationStatus.FAILURE;
        }

        logger.info("Found " + n_exonic_vars + " exonic vars and " + n_non_exonic_vars + " non-exonics");
        logger.info("Got " + n_duplicates + " duplicates");
        return FileOperationStatus.SUCCESS;
    }

    /**
     * This function is to be called following the processing of the VCF line
     * with the function parseVCFline from the Superclass
     * {@link exomizer.io.VCF2FrequencyParser VCF2FrequencyParser}. It contains
     * the parsing logic that is specific to the dbSNP file. Mainly, we are only
     * interested in data from VCF lines for exons or flankiing sequences.
     * However, the dbSNP file contains data for the entire genome. Therefore,
     * this method checks whether the Variant is exonic or not. It also converts
     * the GMAF taken from the dbSNP file from a proportion (i.e., a number
     * between 0 and 1) to a percent to achieve uniformity with the ESP data.
     */
    private void checkVariantForExomalLocationAndOutput(Frequency frequency) {
        /* 1) When we get here, we have transformed VCF format variant position and ref/alt strings to 
         annovar style */

        /* 3) Figure out if the variant is exonic or flanking */
        int endpos = frequency.getPos() + frequency.getRef().length();
        int pos = frequency.getPos();
        ChromosomalExonLocations c2e = chromosomeMap.get(frequency.getChromosome());
        if (c2e == null) {
            logger.error("Could not identify chromosome {}", frequency.getChromosome());
        } else {
            if (c2e.variantIsLocatedInExonicSequence(pos, endpos)) {
                //System.out.println(chromosome + ":" + pos + ":" + id + ":" + ref + ":" + alt + ":" + info);
//                Frequency freq = new Frequency(this.chrom, this.pos, this.ref, this.alt, rs);
                float maf = getMinorAlleleFrequencyFromVCFInfoField(frequency.getInfo());

                if (maf >= 0f) {
                    frequency.setDbSnpGmaf(maf);
                }
                Frequency previous = null; /* use to help avoid duplicate entries. */

                if (previous != null && previous.isIdenticalSNP(frequency)) {
                    float x = previous.getMaximumFrequency();
                    float y = frequency.getMaximumFrequency();
                    if (y > x) {
                        previous.resetFrequencyValues(frequency);
                        this.n_duplicates++;
                    }
                } else {
                    /* i.e., we have never seen this frequency object before. */
                    frequencyList.add(frequency);
                    n_exonic_vars++;
                }
            } else {
                //System.out.println("Not located");
                n_non_exonic_vars++;
            }
        }

    }

    /**
     * Parse the INFO field of the dbSNP VCF file, e.g.,
     * RSPOS=16327;dbSNPBuildID=127;SSR=0;SAO=0;VP=050100000005000402000100;WGT=1;VC=SNV;SLO;ASP;HD;OTHERKG
     * <P>
     * The field called {@code GMAF} denotes the Global Minor Allele Frequency
     * [0, 0.5]; global population is 1000GenomesProject phase 1 genotype data
     * from 629 individuals, released in the 11-23-2010 dataset".
     * <P>
     * There are also fields G5A: "&gt;5% minor allele frequency in each and all
     * populations" and G5: &gt;5% minor allele frequency in 1+ populations".
     * (Exact frequency not indicated).
     * <P>
     * There are some fields indicating a low quality SNP: WTD: "Is Withdrawn by
     * submitter (...)", and similarly NOV: "Rs cluster has non-overlapping
     * allele sets" and GCF: "Has Genotype Conflict (...)".
     * <P>
     * Upon inspection of the file and given the known difficulties with dbSNP
     * data, we will only record the frequency of variants with an explicit GMAF
     * field. This will miss some variants with G5 or G5A fields, but we assume
     * that most of these will have more information from the ESP file.
     *
     * @param info the Info field of a VCF line.
     * @return float of the minor allele frequency
     */
    protected static float getMinorAlleleFrequencyFromVCFInfoField(String info) {
        String A[] = info.split(";");
        for (String a : A) {
            // format has changed in latest field to CAF=[0.9812,0.01882]
            //if (a.startsWith("GMAF=")) {
            if (a.startsWith("CAF=")) {
                //System.out.println(info);
                String parts[] = a.split(",");
                String parts2[] = parts[1].split("]");
                if (!parts2[0].equals(".")) {
                    float maf = Float.parseFloat(parts2[0]);
                    /* NOTE that the dnSNP maf are given as proportion, whereas the ESP MAF
                     are given as percent. In order not to loose numerical accurary, we will
                     convert all to percent for the database. */
                    maf = maf * 100f;
                    return maf;
                }
            }
        }
        return Constants.NOPARSE_FLOAT;
    }

    /**
     * This class is used to represent all of the exons on a chromosome.
     */
    private class ChromosomalExonLocations {

        /**
         * one of the 22 autosomes, 23 for X, 24 for Y, 25 for M
         */
        private final byte chromosome;
        /**
         * TreeMap with all of the genes
         * ({@link jannovar.reference.TranscriptModel TranscriptModel} objects)
         * of this chromosome. The key is an Integer value representing the
         * transcription start site (txstart) of the transcript. Note that we
         * need to use an Array of TranscriptModels because there can be
         * multiple TranscriptModels that share the same transcription start
         * site. (e.g., multiple isoforms of the same gene).
         */
        private final TreeMap<Integer, List<Exon>> exonTreeMap;

        /**
         * @param c the integer representation of the Chromosome, 1-22, 23=X,
         * 24=Y, 25=M
         */
        private ChromosomalExonLocations(byte c) {
            chromosome = c;
            exonTreeMap = new TreeMap<>();
        }

        /**
         * This method adds the exon models of a gene to the exon tree map which
         * in turn is used to filter the variants from the dbSNP file for only
         * those variants located in or near to an exon.
         *
         * @param kgl A transcript of a human known gene.
         */
        public void addGene(TranscriptModel kgl) {
            int ends[] = kgl.getExonEnds();
            int starts[] = kgl.getExonStarts();
            for (int i = 0; i < ends.length; i++) {
                int start = starts[i];
                int end = ends[i];
                Exon exon = new Exon(start, end);
                n_exons++;
                /* 1. There is already an Exon with this start */
                if (exonTreeMap.containsKey(start)) {
                    List<Exon> exonList = exonTreeMap.get(start);
                    /* the same exon could already be present in this list, e.g., multiple
                     transcripts share the same exon. */
                    if (!exonList.contains(exon)) {
                        exonList.add(exon);
                    }
                } else {
                    /* 2. This is the first exon with this start */
                    List<Exon> exonList = new ArrayList<>();
                    exonList.add(exon);
                    exonTreeMap.put(start, exonList);
                }
            }
        }

        /**
         * Search for an exon in which the current dbSNP entry is located. Note
         * that because of genomic oddities, we cannot just look at a single
         * floor entry, because there may be short exons contained within larger
         * exons. Therefore, look back a total of 10 exons to find one that
         * might contain the variants.
         *
         * @param x start position of variant
         * @param y end position of variant.
         * @return true if the variant in question is located in or near to an
         * exon
         */
        public boolean variantIsLocatedInExonicSequence(int x, int y) {
            int SPAN = 10;
            Map.Entry<Integer, List<Exon>> entr = exonTreeMap.floorEntry(x);
            if (entr == null) {
                return false;
            }
            List<Exon> floor = entr.getValue();

            for (Exon e : floor) {
                if (e.contains(x)) {
                    return true;
                }
                if (e.contains(y)) {
                    return true;
                }
            }
            int pos = x;
            for (int i = SPAN; i > 0; i--) {
                entr = exonTreeMap.lowerEntry(pos);
                if (entr == null) {
                    return false;
                }
                floor = entr.getValue();
                pos = entr.getKey();/* reset the left position */

                for (Exon e : floor) {
                    if (e.contains(x)) {
                        return true;
                    }
                    if (e.contains(y)) {
                        return true;
                    }
                }
            }
            /* If we get here, we could not find an exon in which the variant is located */
            return false;
        }
    }

    /**
     * This is essentially a structure with two elements representing the start
     * and end position of an exon along a chromosome. It is intended to be used
     * in sorting our rs variants from dbSNP intop those that affect an exon or
     * the flanking sequence.
     */
    private class Exon {

        /**
         * Start position of exon on chromosome
         */
        public int start;
        /**
         * End position of exon on chromosome
         */
        public int end;

        public Exon(int start, int end) {
            this.start = start;
            this.end = end;
        }

        /**
         * @param x position of the variant being tested for containment within
         * this exon.
         * @return true if the position x is contained within the exon or its
         * flanking sequence
         */
        public boolean contains(int x) {
            return (x >= (start - FLANKING) && x <= (end + FLANKING));
        }
    }
}
