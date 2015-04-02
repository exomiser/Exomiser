package de.charite.compbio.exomiser.db.parsers;

import de.charite.compbio.exomiser.db.resources.ResourceOperationStatus;
import de.charite.compbio.exomiser.db.reference.Frequency;
import de.charite.compbio.exomiser.db.resources.Resource;
import de.charite.compbio.jannovar.impl.intervals.Interval;
import de.charite.compbio.jannovar.io.Chromosome;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.JannovarDataSerializer;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.io.SerializationException;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.TranscriptModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to parseResource the dbSNP file {@code 00-All.vcf}
 * which is available in gzipped form at the dbSNP FTP site. We use a collection
 * of {@link jannovar.reference.TranscriptModel} objects in order to filter the
 * dbSNP variants to those that are located either within an exon or close to an
 * exon (see {@link #FLANKING} for the distance threshold).
 *
 * @author Peter Robinson
 * @version 0.08 (8 December, 2013)
 * @see <a
 * href="ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/">ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/</a>
 */
public class DbSnpFrequencyParser implements ResourceParser {

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
     * The VCF parser.
     */
    private final VCF2FrequencyParser vcf2FrequencyParser;

    /* use to avoid duplicate entries. */
    Frequency previous = null;

    /**
     * Map of Chromosomes
     */
    private final Map<Integer, ChromosomalExonLocations> chromosomeMap;

    public DbSnpFrequencyParser(ReferenceDictionary refDict, JannovarData jannovarData, Path ucscResourcePath, List<Frequency> frequencyList) {
        vcf2FrequencyParser = new VCF2FrequencyParser(refDict);
        this.frequencyList = frequencyList;
        chromosomeMap = new HashMap<>();
        //first we need to prepare the serialized ucsc19 data file from Jannovar
        //this is required for parsing the dbSNP data where it is used as a filter to 
        // remove variants outside of exonic regions.
        makeChromosomeMap(jannovarData);
    }

    /**
     * Inputs the list of known genes from the serialized data file. The
     * serialized file was originally created by parsing the three UCSC known
     * gene files.
     */
    private void makeChromosomeMap(JannovarData jannovarData) {
        for (Entry<Integer, Chromosome> entry : jannovarData.chromosomes.entrySet()) {
            int chr = entry.getKey();
            for (Interval<TranscriptModel> itv : entry.getValue().tmIntervalTree.intervals) {
                if (!chromosomeMap.containsKey(chr)) {
                    ChromosomalExonLocations exonLocations = new ChromosomalExonLocations(chr);
                    exonLocations.addGene(itv.value);
                    //System.out.println("Adding chromosome for " + chromosome);
                    chromosomeMap.put(chr, exonLocations);
                } else {
                    ChromosomalExonLocations exonLocations = chromosomeMap.get(chr);
                    exonLocations.addGene(itv.value);
                }
            }
        }
        logger.info("{} Added {} exons from JannovarData", ResourceOperationStatus.SUCCESS, n_exons);
    }

    /**
     * Parse the main dbSNP file
     *
     * @param resource
     * @param inDir
     * @param outDir
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        long startTime = System.currentTimeMillis();

        ResourceOperationStatus status;

        if (chromosomeMap.isEmpty()) {
            logger.error("Unable to parse file: {} as the chromosomeMap is empty", inFile);
            status = ResourceOperationStatus.FAILURE;
            resource.setParseStatus(status);
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(inFile.toString());
            InputStream is;

            /* First, attempt to use a gzip input stream, if this doesn't work, open it as usual */
            try {
                is = new GZIPInputStream(fis);
            } catch (IOException exp) {
                fis.close();
                is = fis = new FileInputStream(inFile.toString());
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
                List<Frequency> frequencyPerLine = vcf2FrequencyParser.parseVCFline(line);
                for (Frequency frequency : frequencyPerLine) {
                    checkVariantForExomalLocationAndOutput(frequency);
                }
                long now = System.currentTimeMillis();
                if (now - startTime > 2000) {
                    long filePosition = fc.position();
                    long permil = filePosition * 1000 / fileSize;
                    logger.info(String.format("%d.%d%% %d variants parsed", permil / 10, permil % 10, vcount));
                    startTime = now;
                }
            }
            logger.info("Found " + n_exonic_vars + " exonic vars and " + n_non_exonic_vars + " non-exonics");
            logger.info("Got " + n_duplicates + " duplicates");

            status = ResourceOperationStatus.SUCCESS;

        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }

        resource.setParseStatus(status);
        logger.info("{}", status);
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
        ChromosomalExonLocations c2e = chromosomeMap.get(Integer.valueOf((int) frequency.getChromosome()));
        if (c2e == null) {
            logger.error("Could not identify chromosome {}", frequency.getChromosome());
        } else {
            if (c2e.variantIsLocatedInExonicSequence(pos, endpos)) {
                if (previous != null && previous.isIdenticalSNP(frequency)) {
                    float x = previous.getMaximumFrequency();
                    float y = frequency.getMaximumFrequency();
                    this.n_duplicates++;
                    if (y > x) {
                        previous.resetFrequencyValues(frequency);
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
        previous = frequency;
    }

    /**
     * This class is used to represent all of the exons on a chromosome.
     */
    private class ChromosomalExonLocations {

        /**
         * one of the 22 autosomes, 23 for X, 24 for Y, 25 for M
         */
        private final int chromosome;
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
        private ChromosomalExonLocations(int c) {
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
        public void addGene(TranscriptModel tm) {
            for (GenomeInterval itv : tm.exonRegions) {
                final int start = itv.beginPos + 1; // convert to 1-based
                final int stop = itv.endPos;
                Exon exon = new Exon(start, stop);
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
