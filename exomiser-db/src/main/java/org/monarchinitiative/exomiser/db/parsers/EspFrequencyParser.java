/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.db.parsers;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.monarchinitiative.exomiser.db.reference.Frequency;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is for parsing the ESP file
 * {@code ESP6500SI.snps_indels.vcf.tar.gz} (the VCF file with the Exome Server
 * Project data).
 * <P>
 * Note the the ESP project stores the minor allele frequency (MAF) data as
 * percentages. For this reason, we convert the dbSNP data from proportions to
 * percentages (see
 * {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser}) so that both
 * frequency data sources are compatible.
 *
 * @author Peter N. Robinson
 * @version 0.04 (28.07.2013)
 */
public class EspFrequencyParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(EspFrequencyParser.class);

    /**
     * The frequencey parse to use
     */
    private final VCF2FrequencyParser vcf2FrequencyParser;

    /**
     * List of objects that encapsulate information about the frequency of a
     * single SNP.
     */
    private List<Frequency> frequencyList;
    /**
     * List of "new" {@link exomizer.reference.Frequency Frequency} objects that
     * we found while parsing the ESP data but did find in dbSNP. Because we do
     * not want to sort the array list every time we find a new object, we will
     * add them to this list until we are finished and then join the two lists.
     */
    private List<Frequency> espFrequencyList;
    private byte chromosome;
    /**
     * This object is used to allow binary searches on the FrequencyList
     */
    private Comparator<Frequency> comparator = new Comparator<Frequency>() {
        @Override
        public int compare(Frequency f1, Frequency f2) {
            return f1.compareTo(f2);
        }
    };

    /**
     * The constructor initialized the file output stream.
     *
     * @param refDict the reference dictionary to use for chromosome name to id
     * conversion
     * @param frequencyList A previous list of Frequency objects (will have
     * dbSNP information). This is sorted here as it is a requirement for the
     * binary search.
     */
    public EspFrequencyParser(ReferenceDictionary refDict, List<Frequency> frequencyList, byte chromosome) {
        logger.info("Sorting variant frequency list ({} variants)", frequencyList.size());
        vcf2FrequencyParser = new VCF2FrequencyParser(refDict);
        Collections.sort(frequencyList);
        this.frequencyList = frequencyList;
        this.chromosome = chromosome;
        // list for adding new Frequencies from the ESP files
        espFrequencyList = new ArrayList<>();
    }

    /**
     * Parses all 24 of the ESP VCF files (one per chromosome).
     *
     * @param resource
     * @param inDir Path to the inputDir containing the ESP VCF files.
     * @param outDir
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        //n.b. we ignore the ouputDir for the sake of consistency with the other
        //Frequency parser - the writing out of the file is handled elsewhere. 
        ResourceOperationStatus status;
        if (frequencyList == null || frequencyList.isEmpty()) {
            logger.error("Require a frequency list to refer to - this one is null");
            status = ResourceOperationStatus.FAILURE;
            resource.setParseStatus(status);
            return;
        }
        if (frequencyList.isEmpty()) {
            logger.error("Require a frequency list with frequencies to refer to - this one is empty.");
            status = ResourceOperationStatus.FAILURE;
            resource.setParseStatus(status);
            return;
        }
        try (DirectoryStream<Path> espFilePaths = Files.newDirectoryStream(inFile)) {

            for (Path espFile : espFilePaths) {
                ResourceOperationStatus fileStatus = parseEspFile(espFile);
                if (fileStatus != ResourceOperationStatus.SUCCESS) {
                    status = fileStatus;
                }
            }
            //add all the new ESP frequencies into the original list supplied in the constructor
            mergeAndSortFrequencyObjects();
            //remember to set the status to success
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
     * Get annotations for each of the variants in the ESP file for one
     * chromosome. Strategy is not to just split and examine good candidate
     * lines. It is probably quicker to compare strings rather than to transform
     * every string (position on chromosome) into an Integer, but this can be
     * tested later.
     *
     * @param espFile Absolute path to the ESP VCF file for a chromosome.
     */
    private ResourceOperationStatus parseEspFile(Path espFile) {
        logger.info("Parsing ESP File: {}", espFile);
        try {
            BufferedReader br = Files.newBufferedReader(espFile, Charset.defaultCharset());
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                List<Frequency> frequencyPerLine = vcf2FrequencyParser.parseVCFline(line,chromosome);
                for (Frequency frequency : frequencyPerLine) {
                    //parseEspDataFromVCFInfoField(frequency);
                    int idx = Collections.binarySearch(frequencyList, frequency, comparator);
                    if (idx < 0) {
                        /* This means we have not found this variant in the dbSNP data */
                        espFrequencyList.add(frequency);
                    } else {
                        /* replace f with the pre-exisiting Frequency object that contains dbSNP data for this variant */
                        Frequency existingFrequency = frequencyList.get(idx);
                        existingFrequency.setESPFrequencyEA(frequency.getESPFrequencyEA());
                        existingFrequency.setESPFrequencyAA(frequency.getESPFrequencyAA());
                        existingFrequency.setESPFrequencyAll(frequency.getESPFrequencyAll());
                    }
                }

            }
        } catch (IOException e) {
            logger.error("{} - Error parsing ESP file: {}", ResourceOperationStatus.FAILURE, espFile, e.getMessage());
            return ResourceOperationStatus.FAILURE;
        }
        logger.info("{}", ResourceOperationStatus.SUCCESS);
        return ResourceOperationStatus.SUCCESS;
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
        logger.info("Size of ESP derived Frequency list: {}", espFrequencyList.size());
        frequencyList.addAll(espFrequencyList);
        logger.info("After merge size of frequencyList: {}", frequencyList.size());
        Collections.sort(frequencyList);
    }

}
