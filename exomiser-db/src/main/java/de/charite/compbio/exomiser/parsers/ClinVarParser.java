package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse variant_summary.txt from NCBI's ClinVarar. The file has the following
 * structure.
 * <ol>
 * <li>AlleleID, e.g., 31305
 * <li>Type, e.g., single nucleotide variant
 * <li>Name, e.g., FLT4:c.3257T>C (p.Ile1086Thr)
 * <li>GeneID, e.g., 2324
 * <li>GeneSymbol, e.g., FLT4
 * <li>ClinicalSignificance, e.g., Pathogenic
 * <li>RS# (dbSNP), e.g., 121909655
 * <li>nsv (dbVar), e.g., -
 * <li>RCVaccession, e.g.,RCV000017654
 * <li>TestedInGTR, e.g., N
 * <li>PhenotypeIDs, e.g.,
 * GeneReviews:NBK1239,MedGen:C1704423,OMIM:153100,Orphanet:79452,SNOMED
 * CT:399889006
 * <li>Origin	germline
 * <li>Assembly GRCh37
 * <li>Chromosome 5
 * <li>Start 180041142
 * <li>Stop 180041142
 * <li>Cytogenetic 5q35.3
 * <li>ReviewStatus classified by single submitter
 * <li>HGVS(c.) NM_182925.4:c.3257T>C
 * <li>HGVS(p.) NP_891555.2:p.Ile1086Thr
 * <li>NumberSubmitters 1
 * <li>LastEvaluated 30 Jan 2013
 * <li>Guidelines -
 * <li>OtherIDs OMIM Allelic Variant:136352.0009
 * </ol>
 * <P>
 For now, we will parseResource the Chromosome, start, stop and RCVaccession (Note
 there may be multiple of these, separated by a semicolon. The application
 will show any mutation start starts at the same nucleotide in the gene, on
 the assumption that this may be clinically useful.
 *
 * @version 0.02 (25 January, 2014)
 * @author Peter Robinson
 */
public class ClinVarParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarParser.class);

    private final String expectedBuild = "GRCh37";

    private final ArrayList<ClinVar> clinvarLst;

    /**
     * The constructor initializes {@link #clinvarLst}.
     */
    public ClinVarParser() {
        this.clinvarLst = new ArrayList<>();
    }

    private class ClinVar {

        private final int chromosome;
        private final int position;
        private int accession;
        private String significance;

        ClinVar(int chrom, int position, String accession, String sign) {
            this.chromosome = chrom;
            this.position = position;
            if (!accession.startsWith("RCV")) {
                logger.error("Malformed ClinVar accession number (expecting it to start with RSV):{}", accession);
                System.exit(1);
            }
            int i = 3;
            while (accession.charAt(i) == '0') {
                i++;
            }
            Integer acc = Integer.parseInt(accession.substring(i));
            this.accession = acc;
            if (sign.equals("Pathogenic")) {
                this.significance = "P";
            } else if (sign.equals("Likely pathogenic")) {
                this.significance = "L";
            } else if (sign.equals("Uncertain significance")) {
                this.significance = "U";
            } else if (sign.equals("Benign")) {
                this.significance = "B";
            } else if (sign.equals("Likely benign")) {
                this.significance = "C";
            } else if (sign.equalsIgnoreCase("protective")) {
                this.significance = "T";
            } else if (sign.equalsIgnoreCase("drug response")) {
                this.significance = "D";
            } else if (sign.equalsIgnoreCase("confers sensitivity")) {
                this.significance = "S";
            } else if (sign.equalsIgnoreCase("Association")) {
                this.significance = "A";
            } else if (sign.equals("other")) {
                this.significance = "?";
            } else if (sign.equalsIgnoreCase("Risk factor")) {
                this.significance = "R";
            } else if (sign.equalsIgnoreCase("not provided")) {
                this.significance = "?";
            } else {
                String split[] = sign.split(";");
                if (split.length == 1) {
                    logger.warn("Do not recognize clinical significance string: '{}'", sign);
                    //System.exit(1);
                }
                for (String s : split) {
                    if (s.equals("Pathogenic")) {
                        this.significance = "P";
                    }
                }
                this.significance = "?";
            }

        }

        private String toDumpLine() {
           return String.format("%s|%s|%s|%s%n", chromosome, position, accession, significance);
        }

    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        int noPositionInfoVariants = 0;
        int wrongBuildVariants = 0;
        int goodVariants = 0;
        ResourceOperationStatus status;
        
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
        
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // First line with column definitions.
                }
                String split[] = line.split("\t");
                int expectedFieldLength = 24;
                if (split.length < expectedFieldLength) {
                    logger.warn("Malformed line: {}", line);
                    logger.warn("Line has only {} fields. Expected at least %s fields", split.length, expectedFieldLength);
                    continue;
                }
                String build = split[12];
                String RCV = split[8];
                String chr = split[13];
                String from = split[14];
                String to = split[15];
                String sign = split[5];
                //System.out.println(sign);

                if (chr.equals("-") || from.equals("-")) {
                    noPositionInfoVariants++;
                    continue;
                } else {
                    goodVariants++;
                }
                /* If we get here, the genome build should be GRCh37(=hg19), otherwise we will need to
                 change the entire database or modify the parseResource code. */

                if (!build.startsWith(expectedBuild)) {
                    logger.warn("Wrong chromosome build: {}. Expected build {}. Check file and revise!", build, expectedBuild);
                    wrongBuildVariants++;
                }
                //System.out.println(RCV + "-" + chr + "-" + to + "-" + from);
                int chrom;
                switch (chr) {
                    case "X":
                        chrom = 23;
                        break;
                    case "Y":
                        chrom = 24;  // i.e., 24
                        break;
                    case "M":
                    case "MT":
                        chrom = 25;  // i.e., 25
                        break;
                    default:
                        chrom = Integer.parseInt(chr);
                        break;
                }

                int pos = Integer.parseInt(from);
                String acc[] = RCV.split(";"); /* can have a comma separated list of accession numbers */

                for (String a : acc) {
                    ClinVar clinVar = new ClinVar(chrom, pos, a, sign);
                    writer.write(clinVar.toDumpLine());
                    this.clinvarLst.add(clinVar);
                }
            }
            status = ResourceOperationStatus.SUCCESS;

        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }
        
        logger.info("No position information for {} variants", noPositionInfoVariants);
        logger.info("Found information for {} variants", goodVariants);
        logger.info("{} variants were skipped because they are not from build {}", wrongBuildVariants, expectedBuild);
        
        resource.setParseStatus(status);
        logger.info("{}", status);
    }
}
