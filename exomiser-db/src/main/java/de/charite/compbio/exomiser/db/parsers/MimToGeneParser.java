package de.charite.compbio.exomiser.db.parsers;

import de.charite.compbio.exomiser.db.resources.Resource;
import de.charite.compbio.exomiser.db.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to parseResource two files from OMIM in order to
 * extract information that links OMIM ids to Entrez Gene ids. The format of the
 * mim2gene file is as follows:
 * <PRE>
 * #Format: MIM_number GeneID type (tab is used as a separator, pound sign - start of a comment)
 * 100070	100329167	gene
 * 100100	1131	phenotype
 * 100300	57514	phenotype
 * 100300	100188340	gene
 * 100640	216	gene
 * </PRE> In this example, the OMIM entry 100300 is a phenotype entry for
 * Adams-Oliver syndrome 1 (AOS1). It is associated with the gene gene ARHGAP31,
 * which has the NCBI Entrez Gene id 57514. The next line shows 100188340, which
 * is the Entrez gene id for Adams Oliver syndrome (Note: It is strange and
 * unhelpful that Entrez Gene now seems to be giving some diseases gene ids...).
 * <P>
 * The morbidmap file has the following format
 * <PRE>
 * Abetalipoproteinemia, 200100 (3)  |MTP|157147|4q22-q24
 * Acampomelic campomelic dysplasia, 114290 (3)  |SOX9, CMD1, SRA1|608160|17q24.3-q25.1
 * </PRE> For example, the entry 114290 is the disease entry for campomelic
 * dysplasia, and the entry 608160 is the gene entry for the disease gene SOX9.
 * Note that we now additionally take the file phenotype_annotation.tab from the
 * HPO project in order to parseResource out whether OMIM or Orphanet entries
 * follow a recessive, dominant or X chromosomal inheritance.
 *
 * @author Peter N Robinson
 * @version 0.07 (9 February, 2014)
 */
public class MimToGeneParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MimToGeneParser.class);

    Map<Integer, Set<Integer>> mim2geneMap;

    /**
     * Key: A MIM id for a Gene; Value: the corresponding entrez Gene id. This
     * information comes from mim2gene.txt
     *
     * @param mim2geneMap
     */
    public MimToGeneParser(Map<Integer, Set<Integer>> mim2geneMap) {
        this.mim2geneMap = mim2geneMap;
    }

    /**
     * Parse OMIMs mim2gene.txt file. A typical line is
     * <pre>
     * 100710  gene    1140    CHRNB1
     * </pre> The first number is the MIM number, the second field tells us
     * whether the entry is a gene or a phenotype, the thrid entry is the Entrez
     * Gene ID, and the fourth field is the gene symbol. Note the we
     * parseResource this file exclusively for the phenotype to gene relations,
     * meaning we only use the lines that start with the keyword "gene".
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        
        ResourceOperationStatus status;

        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset())) {
            String line;

            while ((line = reader.readLine()) != null) {
                //ignore comment lines
                if (line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\t");
                if (fields.length < 3) { // malformed, should never happen
                    logger.error("Malformed mim2gene line: " + line);
                    logger.error("Found only {} fields", fields.length);
                    continue;
                }
                try {
                    String type = fields[1].trim();
                    /* The following gets both "gene" and "gene/phenotype" */
                    if (!type.startsWith("gene")) {
                        continue;  /* We do not need phenotype MIMs or other types such as removed */

                    }
                    /* There are a lot of lines such as
                     "102777	gene	-	-"
                     That do not have valid Entrez Gene ids. We just skip them */
                    if (fields[2].equals("-")) {
                        continue;
                    }
                    Integer mim = Integer.parseInt(fields[0]);
                    Integer entrezGeneId = Integer.parseInt(fields[2]); // Entrez Gene ID */
                    //String IDs[] = gene.split(",")
//                    addMIM2EntrezGenePair(mim, entrezGeneId);
                    Set<Integer> geneSet;
                    if (mim2geneMap.containsKey(mim)) {
                        geneSet = mim2geneMap.get(mim);
                        geneSet.add(entrezGeneId);
                    } else {
                        geneSet = new TreeSet<>();
                        geneSet.add(entrezGeneId);
                        mim2geneMap.put(mim, geneSet);
                    }
                } catch (NumberFormatException e) {
                    logger.error("{}", e);
                }
            }
            logger.info("Extracted {} genes from {}", mim2geneMap.size(), inFile);
            status = ResourceOperationStatus.SUCCESS;
    
        } catch (FileNotFoundException ex) {
            logger.error("Unable to find file: {}", inFile, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error("Error parsing file: {}", inFile, ex);
            status = ResourceOperationStatus.FAILURE;
        }

        resource.setParseStatus(status);
        logger.info("{}", status);

    }
    
//    /**
//     * This is essentially a structure with three elements representing a line
//     * in the mim2gene File
//     */
//    private class MIM2Gene {
//
//        /**
//         * MIM number
//         */
//        public int mim;
//        /**
//         * 'P': phenotype, 'G': gene
//         */
//        public char type;
//        /**
//         * Entrez Gene ID
//         */
//        public int geneID;
//
//        public MIM2Gene(int m, String t, int g) {
//            this.mim = m;
//            if (t.equals("gene")) {
//                this.type = 'G';
//            } else {
//                System.err.println("Could not identify type in MIM2Gene: " + t);
//                System.exit(1);
//            }
//            this.geneID = g;
//        }
//    }

}
