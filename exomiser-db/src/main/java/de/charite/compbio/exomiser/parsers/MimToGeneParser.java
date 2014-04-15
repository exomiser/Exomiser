package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to parse two files from OMIM in order to extract
 * information that links OMIM ids to Entrez Gene ids. The format of the
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
 * HPO project in order to parse out whether OMIM or Orphanet entries follow a
 * recessive, dominant or X chromosomal inheritance.
 *
 * @author Peter N Robinson
 * @version 0.07 (9 February, 2014)
 */
public class MimToGeneParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(MimToGeneParser.class);

    Map<Integer, Set<Integer>> mim2geneMap;

    /**
     * Key: A MIM id for a Gene; Value: the corresponding entrez Gene id. This
     * information comes from mim2gene.txt
     * @param mim2geneMap
     */
    public MimToGeneParser(Map<Integer, Set<Integer>> mim2geneMap) {
        this.mim2geneMap = mim2geneMap;
    }

    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outPath)));) {      
            // Parse OMIM gene ID information */
            parseMim2Gene(inPath, mim2geneMap);
            logger.info("Extracted {} genes from {}", mim2geneMap.size(), inPath);
                    
        } catch (IOException e) {
            logger.error("Error parsing mim2gene file: {}", inPath, e);
           return ResourceOperationStatus.FAILURE;
        } 
        
    return ResourceOperationStatus.SUCCESS;
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


    /**
     * Parse OMIMs mim2gene.txt file. A typical line is
     * <pre>
     * 100710  gene    1140    CHRNB1
     * </pre> The first number is the MIM number, the second field tells us
     * whether the entry is a gene or a phenotype, the thrid entry is the Entrez
     * Gene ID, and the fourth field is the gene symbol. Note the we parse this
     * file exclusively for the phenotype to gene relations, meaning we only use
     * the lines that start with the keyword "gene".
     */
    private Map<Integer, Set<Integer>> parseMim2Gene(String mim2genePath, Map<Integer, Set<Integer>> mim2geneMap) {
        logger.info("Parsing mim2gene file: {}", mim2genePath);
        try {
            FileReader fileReader = new FileReader(mim2genePath);
            BufferedReader br = new BufferedReader(fileReader);
            String line;

            while ((line = br.readLine()) != null) {
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
        } catch (IOException e) {
            logger.error("Error parsing mim2gene file: {}", mim2genePath, e);
            System.exit(1);
        }
        return mim2geneMap;

    }

//    /**
//     * Add a mim/entrez gene pair, and watch out for duplicates.
//     */
//    private void addMIM2EntrezGenePair(int mim, int entrez) {
//        List<Integer> lst;
//        if (mim2geneMap.containsKey(mim)) {
//            lst = mim2geneMap.get(mim);
//            if (!lst.contains(entrez)) {
//                lst.add(entrez);
//            }
//        } else {
//            lst = new ArrayList<>();
//            lst.add(entrez);
//            mim2geneMap.put(mim, lst);
//        }
//    }

    
//    /**
//     * Adds the parse information directly to the Exomiser database. Just call the Constructor
//     * and then this method. No need to import a dumpfile.
//     */
//     public void addOMIMDataToDatabase(Connection connection)  {
//	 System.out.println("[INFO] I will add " + this.mimList.size() +  " MIM entries to Exomiser database");
//	String insert = "INSERT INTO omim(phenmim,genemim,diseasename,gene_id,type,inheritance) VALUES(?,?,?,?,?,?);";
//	PreparedStatement pst=null;
//	try {
//	    pst = connection.prepareStatement(insert);
//	} catch (SQLException e) {
//	    System.out.println("[ERROR] Exception encountered while preparing statement for OMIM data: " + e);
//	    System.exit(1);
//	}
//	int i=1;
//	for (MIM mim : this.mimList) {
//	    try{
//		pst.setInt(1, mim.phenmim);
//		pst.setInt(2, mim.genemim);
//		pst.setString(3, mim.phenname);
//		pst.setInt(4, mim.entrezGeneId);
//		pst.setString(5, mim.type);
//		pst.setString(6, mim.inheritanceMode.getInheritanceCode());
//		pst.executeUpdate();
//		if (i%500 == 0) {
//		    System.out.println("Entered MIM record " + i + " for disease " + mim.phenname);
//		}
//		i++;
//	    } catch (SQLException e) {
//		System.out.println("[ERROR] Exception encountered while inputting OMIM data: " + e);
//		// just skip it.
//	    }
//	}
}
