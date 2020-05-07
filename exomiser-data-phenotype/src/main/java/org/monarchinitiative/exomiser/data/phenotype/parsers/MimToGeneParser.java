/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
 * <p>
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

    private final Map<Integer, Integer> mim2geneMap;

    /**
     * Key: A MIM id for a Gene; Value: the corresponding entrez Gene id. This
     * information comes from mim2gene.txt
     *
     * @param mim2geneMap
     */
    public MimToGeneParser(Map<Integer, Integer> mim2geneMap) {
        this.mim2geneMap = mim2geneMap;
    }

    /**
     * Parse OMIMs mim2gene.txt file. A typical line is
     * <pre>
     * # MIM Number	MIM Entry Type (see FAQ 1.3 at https://omim.org/help/faq)	Entrez Gene ID (NCBI)	Approved Gene Symbol (HGNC)	Ensembl Gene ID (Ensembl)
     * 100850  gene    50      ACO2    ENSG00000100412
     * </pre> The first number is the MIM number, the second field tells us
     * whether the entry is a gene or a phenotype, the third entry is the Entrez
     * Gene ID, and the fourth field is the gene symbol. Note the we
     * parseResource this file exclusively for the phenotype to gene relations,
     * meaning we only use the lines that have with the keyword "gene" in the second column.
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
                if (!line.startsWith("#")) {
                    parseLine(line);
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

    private void parseLine(String line) {
        String[] fields = line.split("\t");
        try {
            String type = fields[1].trim();
            /* The following gets both "gene" and "gene/phenotype" */
            if (type.startsWith("gene") && fields.length == 5) {
                // typical line: 100850  gene    50      ACO2    ENSG00000100412
                Integer mim = Integer.parseInt(fields[0]);
                Integer entrezGeneId = Integer.parseInt(fields[2]); // Entrez Gene ID */
                if (mim2geneMap.containsKey(mim)) {
                    logger.warn("{} already mapped to EntrezId {}", mim, mim2geneMap.get(mim));
//                    mim2geneMap.get(mim).add(entrezGeneId);
                } else {
//                    Set<Integer> geneSet = new TreeSet<>();
//                    geneSet.add(entrezGeneId);
                    mim2geneMap.put(mim, entrezGeneId);
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing line {}", line, e);
        }
    }

}
