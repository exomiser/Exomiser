package org.monarchinitiative.exomiser.db.parsers;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.monarchinitiative.exomiser.db.reference.Frequency;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * This class is designed to parseResource the dbSNP file {@code 00-All.vcf}
 * which is available in gzipped form at the dbSNP FTP site. We use a collection
 * of
 * {@link jannovar.reference.TranscriptModel} objects in order to filter the
 * dbSNP variants to those that are located either within an exon or close to an
 * exon (see {@link #FLANKING} for the distance threshold).
 *
 * @author Peter Robinson
 * @version 0.08 (8 December, 2013)
 * @see <a
 * href="ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/">ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/</a>
 */
public class ExACFrequencyParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(ExACFrequencyParser.class);
    /**
     * List of all variants that pass threshold for inclusion in database
     * because they are exonic
     */
    private final List<Frequency> frequencyList;
    
    private final VCF2FrequencyParser vcf2FrequencyParser;

    /*
     * use to avoid duplicate entries.
     */
    Frequency previous = null;
    private List<Frequency> exACFrequencyList;
    private byte chromosome;
    private final ReferenceDictionary refDict;
   
    /**
     * This object is used to allow binary searches on the FrequencyList
     */
    private Comparator<Frequency> comparator = new Comparator<Frequency>() {

        @Override
        public int compare(Frequency f1, Frequency f2) {
            return f1.compareTo(f2);
        }
    };

    public ExACFrequencyParser(ReferenceDictionary refDict, List<Frequency> frequencyList, byte chromosome) {
        vcf2FrequencyParser = new VCF2FrequencyParser(refDict);
        this.frequencyList = frequencyList;
        this.chromosome = chromosome;
        this.refDict = refDict;
        exACFrequencyList = new ArrayList<>();
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

        try {
            FileInputStream fis = new FileInputStream(inFile.toString());
            InputStream is;

            /*
             * First, attempt to use a gzip input stream, if this doesn't work,
             * open it as usual
             */
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
            int vcount = 0; /*
             * Keeps track of number of lines (variants) parsed.
             */

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                vcount++;
                String[] fields = line.split("\t");
                byte chrom = 0;
                try {
                    chrom = (byte) refDict.getContigNameToID().get(fields[0]).intValue();
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse chromosome: {}. Error occured parsing line: {}", fields[0], line);
                    logger.error("", e.getMessage());
                    System.exit(1);
                }
                if (chrom < chromosome) {
                    continue;
                } else if (chrom > chromosome) {
                    break;
                }
                List<Frequency> frequencyPerLine = vcf2FrequencyParser.parseVCFline(line,chromosome);
                
                for (Frequency frequency : frequencyPerLine) {
                    int idx = Collections.binarySearch(frequencyList, frequency, comparator);
                    if (idx < 0) {
                    /* This means we have not found this variant in the dbSNP data */
                        exACFrequencyList.add(frequency);
                    } else {
                    /* replace f with the pre-exisiting Frequency object that contains dbSNP data for this variant */
                        Frequency existingFrequency = frequencyList.get(idx);
                        existingFrequency.setExACFrequencyAfr(frequency.getExACFrequencyAfr());
                        existingFrequency.setExACFrequencyAmr(frequency.getExACFrequencyAmr());
                        existingFrequency.setExACFrequencyEas(frequency.getExACFrequencyEas());
                        existingFrequency.setExACFrequencyFin(frequency.getExACFrequencyFin());
                        existingFrequency.setExACFrequencyNfe(frequency.getExACFrequencyNfe());
                        existingFrequency.setExACFrequencyOth(frequency.getExACFrequencyOth());
                        existingFrequency.setExACFrequencySas(frequency.getExACFrequencySas());
                    }
                }
                long now = System.currentTimeMillis();
                if (now - startTime > 2000) {
                    long filePosition = fc.position();
                    long permil = filePosition * 1000 / fileSize;
                    logger.info(String.format("%d.%d%% %d variants parsed", permil / 10, permil % 10, vcount));
                    startTime = now;
                }
            }
            //add all the new ESP frequencies into the original list supplied in the constructor
            mergeAndSortFrequencyObjects();
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
     * Note that when we find ESP information for a variant for which we did not
     * have dbSNP information (and therefore, the variant wasn't listed in the
     * version of {@link #frequencyList} that was passed to the constructor), we
     * add it to a new list, {@link #espFrequencyList}. This was because we are
     * using a binary search for {@link #frequencyList}, and do not want to have
     * to keep the list sorted after each addition. Therefore, after we are
     * finished, we need to merge the two lists and sort the merged list.
     */
    private void mergeAndSortFrequencyObjects() {
        logger.info("mergeAndSortFrequencyObjects for chromosome {}", chromosome);
        logger.info("Original size of frequencyList: {}", frequencyList.size());
        logger.info("Size of ExAC derived Frequency list: {}", exACFrequencyList.size());
        frequencyList.addAll(exACFrequencyList);
        logger.info("After merge size of frequencyList: {}", frequencyList.size());
        Collections.sort(frequencyList);
    }
    
    
}
