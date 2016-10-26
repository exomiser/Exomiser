/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.reference.VariantPathogenicity;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parse information from the NSFP chromosome files. Create an SQL dump file
 * that will be used to import the information into the postgreSQL database.
 * <P>
 * Note that for some SNVs, there are multiple lines in the dbSNFP file. This 
 * parser takes them all.
 * <P>
 * The annotations of the dbNSFP fields are from the dbNSFP documentation. The 
 * parser uses a small sub-set of fields from the file. These are declared 
 * initially as what currently works, but the parser will also try to auto-detect
 * them.
 * <P>
 * The structure of the <B>variant.pg</B> file is then (example line):</BR>
 * 6|345879|A|G|K|E|72|72|0.52|0.002|0.734868|0.278|-5|-5.0|2 </BR>
 * <UL>
 * <LI>6: chromosome
 * <LI>345879: position
 * <LI>A: ref nucleotide
 * <LI>G: alt nucleotide
 * <LI>K: aaref (reference amino acid)
 * <LI>E: aaalt (alternate amino acid)
 * <LI>72: uniprot_aapos
 * <LI>72: aapos (position of mutation in amino acid sequence)
 * <LI>0.52: sift score
 * <LI>0.002: polyphen2_HVAR
 * <LI>0.734868: mut_taster score
 * <LI>0.278: phyloP score
 * <LI>-5: ThGenom_AC, 1000G allele count (Note -5 is a flag that we could not
 * find data)
 * <LI>-5.0: ThGenom_AF, 1000G allele frequency (Note -5.0 is a flag that we
 * could not find data)
 * <LI>2: gene_id_key. This is the primary key of the gene table
 * (auto_increment, see above).
 *
 * @author Peter N. Robinson
 * @version 0.06 (15 July 2013)
 */
public class NSFP2SQLDumpParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(NSFP2SQLDumpParser.class);

    //This is wat's used inplace of a null in the dbNSFP file
    protected static final String NO_VALUE = ".";

    // N.B Changed positions to the hg19 ones - the hg38 ones at 0 and 1 are the defaults ones now
    // The following are the fields of the dbNSFP files.
    /**
     * Chromosome number
     */
    private static int CHR = 7;
    /**
     * physical position on the chromosome as to hg19 (1-based coordinate)
     */
    private static int POS = 8;
    /**
     * reference nucleotide allele (as on the + strand)
     */
    private static int REF = 2;
    /**
     * alternative nucleotide allele (as on the + strand)
     */
    private static int ALT = 3;

    /**
     * SIFT score, If a score is smaller than 0.05 the corresponding NS is
     * predicted as "D(amaging)"; otherwise it is predicted as "T(olerated)".
     */
    private static int SIFT_SCORE = 23;
    /**
     * SIFT_score_converted: SIFTnew=1-SIFTori. The larger the more damaging.
     * Currently unused in Exomiser.
     */
//    public static final int SIFT_SCORE_CONVERTED = 24;
 
    /**
     * Polyphen2 score based on HumVar, i.e. hvar_prob.
     * <P>
     * The score ranges from 0 to 1, and the corresponding prediction is
     * "probably damaging" if it is in [0.909,1]; "possibly damaging" if it is
     * in [0.447,0.908]; "benign" if it is in [0,0.446]. Score cutoff for binary
     * classification is 0.5, i.e. the prediction is "neutral" if the score is
     * smaller than 0.5 and "deleterious" if the score is larger than 0.5.
     * Multiple entries separated by ";".
     */
    private static int POLYPHEN2_HVAR_SCORE = 29;//28

    /**
     * MutationTaster score
     */
    private static int MUTATION_TASTER_SCORE = 35;//33

    /**
     * MutationTaster prediction, "A" ("disease_causing_automatic"), "D"
     * ("disease_causing"), "N" ("polymorphism") or "P"
     * ("polymorphism_automatic"). Note that the score represents the calculated
     * probability that the prediction is correct. Thus, if the prediction is
     * "N" or "P", we set the mutation score to zero. If the score is "A" or
     * "D", we report the score as given in dbNSFP.
     */
    private static int MUTATION_TASTER_PRED = 37;//35

    /**
     * Total number of fields in the dbNSFP database
     */
    private static int N_NSFP_FIELDS = 86;//59
    
    private static int CADD_raw = 51;
    
    private static int CADD_raw_rankscore = 52;
    
    /**
     * This variable will contain values such as A3238732G that represent the
     * current SNV. It will be used to deal with doubled lines for the same
     * variant
     */
    private String currentVar = null;

    /**
     * A number that will be used as a primary key in the gene file (like an
     * auto increment in MySQL).
     */
    private int autoIncrement = 0;
    /**
     * The count of all lines parsed from all of the dbNSFP files (Header lines
     * are not counted).
     */
    private int totalLinesCount = 0;
    /**
     * The count of all variants added to the dump file. Note, multiple lines
     * for same variant are counted once.
     */
    private int totalVariantsCount = 0;
    /**
     * The count of all the genes added to the dump file.
     */
    private int totalGenesCount = 0;

    /** the reference dictionary to use for chromosome name to numeric id conversion */
    //private final ReferenceDictionary refDict;

    /**
     * Get count of all lines parsed from all of the dbNSFP files (Header lines
     * are not counted).
     */
    public int getTotalNsfpLines() {
        return totalLinesCount;
    }

    /**
     * Get count of all variants added to the dump file. Note, multiple lines
     * for same variant are counted once.
     */
    public int getVariantCount() {
        return totalVariantsCount;
    }

    /**
     * Get count of all the genes added to the dump file.
     */
    public int getGeneCount() {
        return totalGenesCount;
    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;
        
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(inFile.toString()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().contains("_variant.chr")) {
                    logger.info("Parsing variant chromosome file: {}. Parsed {} variants so far...", zipEntry.getName(), totalLinesCount);
                    
                    writer.flush();
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        totalLinesCount++;
                        if (line.startsWith("#")) {
                            //try to autodetect the column positions for the parser 
                            setParseFields(line);                          
                        } else {
                            VariantPathogenicity pathogenicity = parseLine(line);
                            writer.write(pathogenicity.toDumpLine());
                        }
                    }
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

        resource.setParseStatus(status);
        logger.info("{}", status);
    }

    /**
     * Parses the dbNSFP variant lines for the pathogenicity scores.
     *
     * @param line
     * @return
     */
    private VariantPathogenicity parseLine(String line) {

        String[] fields = line.split("\t");
        if (fields.length < N_NSFP_FIELDS) {
            String message = String.format("Malformed line '%s' - Only %d fields found (expecting %d)", line, fields.length, N_NSFP_FIELDS);
            logger.error(message);
            throw new ResourceParserException(message);
        }
        //variant position
        /* if work out what Jules was doing with ReferenceDictionary 
         * put back to int c = refDict.contigID.get(fields[CHR]);
         */
        int c;
        switch (fields[CHR]) {
            case "X":
                c = 23; 
                break;
            case "Y":
                c = 24; 
                break;
            case "M":
                c = 25; 
                break;
            default:
                c = Integer.parseInt(fields[CHR]);
                break;
        }
        int pos = Integer.parseInt(fields[POS]);
        String ref = fields[REF];
        String alt = fields[ALT];
        //pathogenicity scores
        Float sift = getMostPathogenicSIFTScore(fields[SIFT_SCORE]);
        Float polyphen2HVAR = getMostPathogenicPolyphenScore(fields[POLYPHEN2_HVAR_SCORE]);
        Float mutTaster = getMostPathogenicMutTasterScore(fields[MUTATION_TASTER_SCORE], fields[MUTATION_TASTER_PRED]);
        Float caddRaw = valueOfField(fields[CADD_raw]);
        Float caddRawRankscore = valueOfField(fields[CADD_raw_rankscore]);

        return new VariantPathogenicity(c, pos, ref, alt,
                sift, polyphen2HVAR, mutTaster, caddRawRankscore, caddRaw);
    }

    /**
     * Some entries in dbNSFP are either nonnegative floats or "." . If the
     * latter, then this method will return a null.
     * @param field
     * @return 
     */
    protected Float valueOfField(String field) {
        try {
            return Float.valueOf(field);
        } catch (NumberFormatException e) {
            logger.error("Could not parse float value from: '{}'", field);
            return null;
        }
    }
        

    /**
     * If there are SIFT scores for two different transcripts that correspond to
     * a given chromosomal variant, they are entered e.g. as 0.527;0.223. In
     * this case, we will extract the most pathogenic score, i.e., the score
     * that is closest to zero.
     *
     * @param field SIFT score, either a single float number or a semicolon
     * separated list of such scores
     * @return A float representation of the SIFT score. If a list of SIFT
     * scores is passed to the function, then a float representation of the most
     * pathogenic score is returned. If "." is passed to the function, then
     * return NOPARSE_FLOAT (a flag)
     */
    protected Float getMostPathogenicSIFTScore(String field) {
        if (field.equals(NO_VALUE)) {
            return null;
        }
        float min = Float.MAX_VALUE;
        String[] scores = field.split(";");
        for (String score : scores) {
            score = score.trim();
            if (score.equals(NO_VALUE)){
               // Note there are some entries such as ".;0.292" so catch them here 
               continue;
            }
            Float value = valueOfField(score);
            if (value != null) {
                min = Math.min(value, min);
            }
        }
        if (min < Float.MAX_VALUE) {
            return min;
        } else {
            return null;
        }
    }

    /**
     * If there are Polyphen scores for two different transcripts that
     * correspond to a given chromosomal variant, they are entered e.g. as
     * 0.527;0.223. In this case, we will extract the most pathogenic score,
     * i.e., the score that is closest to one.
     *
     * @param field Polyphen score, either a single float number or a semicolon
     * separated list of such scores
     * @return A float representation of the Polyphen score. If a list of
     * Polyphen scores is passed to the function, then a float representation of
     * the most pathogenic score is returned. If "." is passed to the function,
     * then return NOPARSE_FLOAT (a flag)
     */
    protected Float getMostPathogenicPolyphenScore(String field) {
        if (field.equals(NO_VALUE)) {
            return null;
        }
        Float max = Float.MIN_VALUE;
        String[] scores = field.split(";");
        for (String score : scores) {
            score = score.trim();
            if (score.equals(NO_VALUE)){
               // Note there are some entries such as ".;0.292" so catch them here 
               continue;
            }
            Float value = valueOfField(score);
            if (value != null) {
                max = Math.max(value, max);
            }
        }
        if (max > Float.MIN_VALUE) {
            return max;
        } else {
            return null;
        }
    }

    /**
     * If there are Mutation Taster scores for two different transcripts that
     * correspond to a given chromosomal variant, they are entered e.g. as
     * 0.527;0.223. In this case, we will extract the most pathogenic score,
     * i.e., the score that is closest to one. Note that this function is
     * identical to the function for poylphen scores since both have scores
     * [0..1] with scores closer to 1 being more pathogenic. We keep a second
     * function since the way the various scores are normalized in dbNSFP may
     * change in the future.
     *
     * @param field Mutation Taster score, either a single float number or a
     * semicolon separated list of such scores
     * @param prediction MutationTaster prediction. If this is for a
     * polymorphism, then the score is set to zero (not path).
     * @return A float representation of the Mutation Taster score. If a list of
     * Mutation Taster scores is passed to the function, then a float
     * representation of the most pathogenic score is returned. If "." is passed
     * to the function, then return NOPARSE_FLOAT (a flag)
     */
    protected Float getMostPathogenicMutTasterScore(String field, String prediction) {
        if (field.equals(NO_VALUE)) {
            return null;
        }
        String[] scores = field.split(";");
        String[] predictions = prediction.split(";");
        if (scores.length != predictions.length) {
            logger.error("Badly formated mutation taster score entry: Score was: {} and prediction was {}", field, prediction);
            logger.error("Length of score entry: {}, length of prediction entry: {}", scores.length, predictions.length);
            return null;
        }
        Float max = Float.MIN_VALUE;
        for (int i = 0; i < scores.length; ++i) {
            String score = scores[i].trim();
            if (score.equals(NO_VALUE)){
               // Note there are some entries such as ".;0.292" so catch them here 
               continue;
            }
            String p = predictions[i].trim();
            if (p.equals("N") || p.equals("P")) {
                max = 0f;
                continue;
            }
            if (!p.equals("A") && !p.equals("D")) {
                logger.error("Badly formated mutation taster score entry. The prediction field was '{}'", p);
                logger.error("Acceptable values for prediction field are one of A,D,N,P");
                return null;
            }
            Float value = valueOfField(score);
            if (value != null) {
                max = Math.max(value, max);
            }
            
        }
        if (max > Float.MIN_VALUE) {
            return max;
        } else {
            return null;
        }
    }

    /**
     * Sets the parser fields so that if the column positions change (this is a 
     * common occurrence apparently), then the parser will adapt itself accordingly. 
     * Note that if the column names change this will break the parser.
     * @param line 
     */
    private void setParseFields(String line) {
        //remove the initial '#' character from the header line
        line = line.substring(1);
        //then split 
        String[] fields = line.split("\t");
        
        N_NSFP_FIELDS = fields.length;
        
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            logger.debug("Field {} = {}", i, field);
            int prev = 0;
            switch (field){
                case "hg19_chr":
                    prev = CHR;
                    logger.info("Setting CHR field '{}' from position {} to {}", field, prev, i);
                    CHR = i;
                    break;
                case "hg19_pos":
                    prev = POS;
                    logger.info("Setting POS field '{}' from position {} to {}", field, prev, i);
                    POS = i;
                    break;    
                case "ref":
                    prev = REF;
                    logger.info("Setting REF field '{}' from position {} to {}", field, prev, i);
                    REF = i;
                    break;
                case "alt":
                    prev = ALT;
                    logger.info("Setting ALT field '{}' from position {} to {}", field, prev, i);
                    ALT = i;
                    break;
               case "SIFT_score":
                    prev = SIFT_SCORE;
                    logger.info("Setting SIFT_SCORE field '{}' from position {} to {}", field, prev, i);
                    SIFT_SCORE = i;
                    break;
                case "Polyphen2_HVAR_score":
                    prev = POLYPHEN2_HVAR_SCORE;
                    logger.info("Setting POLYPHEN2_HVAR_SCORE field '{}' from position {} to {}", field, prev, i);
                    POLYPHEN2_HVAR_SCORE = i;
                    break;
                case "MutationTaster_score":
                    prev = MUTATION_TASTER_SCORE;
                    logger.info("Setting MUTATION_TASTER_SCORE field '{}' from position {} to {}", field, prev, i);
                    MUTATION_TASTER_SCORE = i;
                    break;
                case "MutationTaster_pred":
                    prev = MUTATION_TASTER_PRED;
                    logger.info("Setting MUTATION_TASTER_PRED field '{}' from position {} to {}", field, prev, i);
                    MUTATION_TASTER_PRED = i;
                    break;
                case "CADD_raw":
                    prev = CADD_raw;
                    logger.info("Setting CADD_raw field '{}' from position {} to {}", field, prev, i);
                    CADD_raw = i;
                    break;
                case "CADD_raw_rankscore":
                    prev = CADD_raw_rankscore;
                    logger.info("Setting CADD_raw_rankscore field '{}' from position {} to {}", field, prev, i);
                    CADD_raw_rankscore = i;
                    break;
            }                    
        }
    }


}
