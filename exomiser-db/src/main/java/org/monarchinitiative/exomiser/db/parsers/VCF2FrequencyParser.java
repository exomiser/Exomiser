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

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.db.reference.Frequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We are parsing two different VCF files for population frequency data on
 * variants, the file from ESP and the file from dbSNP. The formats of the two
 * files are similar enough that we extract that into this superclass. <P> This
 * classs encapsulates the functionality of parsing a basic VCF line,
 * transforming the coordinates of the variant from VCF-style to Annovar style
 * (if necessary), and also provides two convenience functions for parsing the
 * INFO field of the VCF line to extract minor allele frequency data and to
 * transform the rs ids to the correspopnding integer.
 *
 * @author Peter N. Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.03 (09.12.2013)
 */
public class VCF2FrequencyParser {

    private static final Logger logger = LoggerFactory.getLogger(VCF2FrequencyParser.class);

    /** The reference dictionary to use for chromosome to name conversion */
    private final ReferenceDictionary refDict;

    /**
     * Initialize object with the given <code>refDict</code>.
     * 
     * @param refDict
     *            reference dictionary to use for chromosome name conversion
     */
    public VCF2FrequencyParser(ReferenceDictionary refDict) {
        this.refDict = refDict;
    }

    /**
     * This method parses a standard VCF line of a population frequency VCF file from ESP or dbSNP This is an example of
     * the format: #CHROM POS ID REF ALT QUAL FILTER INFO 1 10019 rs376643643 TA T . .
     * RS=376643643;RSPOS=10020;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000200;WGT=1;VC=DIV;R5;OTHERKG 1
     * 10054 rs373328635 CAA C,CA . .
     * RS=373328635;RSPOS=10055;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000210;WGT=1;VC=DIV;R5;OTHERKG;NOC 1
     * 10109 rs376007522 A T . .
     * RS=376007522;RSPOS=10109;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000100;WGT=1;VC=SNV;R5;OTHERKG
     * 
     * @param line
     * @return a
     * <code>Frequency</code> object created from the input line.
     */
    public List<Frequency> parseVCFline(String line, byte chromosome) {

        List<Frequency> frequencyList = new ArrayList<>();

        String[] fields = line.split("\t");

        byte chrom = 0;
        try {
            chrom = (byte) refDict.getContigNameToID().get(fields[0]).intValue();
        } catch (NumberFormatException e) {
            String message = String.format("Unable to parse chromosome: %s. Error occured parsing line: %s", fields[0], line);
            logger.error(message, e.getMessage());
            throw new ResourceParserException(message, e);
        }
        if (chrom != chromosome){
            return frequencyList;
        }
        int pos = Integer.parseInt(fields[1]);
        /*
         * Transform rsID to integer to save space. Note that if there are
         * problems with parse we use the constant NO_RSID = -1:
         */
        int rsId = rsIdToInt(fields[2]);
        /*
         * Uppercasing shouldn't be necessary acccording to the VCF standard,
         * but occasionally one sees VCF files with lower case for part of the
         * sequences, e.g., to show indels.
         */
        String ref = fields[3].toUpperCase();

        /*
         * dbSNP has introduced the concept of multiple minor alleles on the
         * same VCF line with their frequencies reported in same order in the
         * INFO field in the CAF section Because of this had to introduce a loop
         * and move the dbSNP freq parsing to here. Not ideal as ESP processing
         * also goes through this method but does not use the CAF field so
         * should be skipped
         */
        String[] alts = fields[4].toUpperCase().split(",");

        String info = fields[7];

        float ea = 0f;
        float aa = 0f;
        float all = 0f;
        List<String> minorFreqs = new ArrayList<>();
        Map<String, String> exACFreqs = new HashMap<>();
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            // freq data from dbSNP file
            // format has changed in latest field to ;CAF=[0.9812,.,0.01882]; where major allele is 1st followed by minor alleles in order of alt line
            if (infoField.startsWith("CAF=")) {
                String[] parts = infoField.split(",");
                for (String part : parts) {
                    part = part.replace("]", "");
                    minorFreqs.add(part);
                }
            }
            // freq data from ESP file
            if (infoField.startsWith("MAF=")) {
                infoField = infoField.substring(4);
                /**
                 * This must now be a field with information for minor allele
                 * frequency for EA,AA,All
                 */
                String[] minorAlleleFreqs = infoField.split(",");
                if (minorAlleleFreqs.length == 3) {
                    ea = Float.parseFloat(minorAlleleFreqs[0]);
                    aa = Float.parseFloat(minorAlleleFreqs[1]);
                    all = Float.parseFloat(minorAlleleFreqs[2]);
                }
            }
            // freq data from ExAC file
            if (infoField.startsWith("AC") || infoField.startsWith("AN")) {
                String[] exACData = infoField.split("=");
                exACFreqs.put(exACData[0], exACData[1]);
            }
        }

        int minorAlleleCounter = 1;
        for (String alt : alts) {
            //2017-06-09 We're not going to use Annovar style any more. Seems Annovar recommends VCF spec now too:
            //http://annovar.openbioinformatics.org/en/latest/articles/VCF/
            //This was causing indels to be missed as the minimising ends up producing output which isn't compatible.
            //given we're ingesting VCF, outputting VCF and all the frequency data is coming from VCF, we'll use VCF standard.
            //Annotations will still come from Jannovar and these are right-shifted as opposed to left-shifted.
            AllelePosition minimisedAllele = AllelePosition.trim(pos, ref, alt);
            Frequency freq = new Frequency(chrom, minimisedAllele.getPos(), minimisedAllele.getRef(), minimisedAllele.getAlt(), rsId);
            if (ea != 0f){   
                freq.setESPFrequencyEA(ea);
                freq.setESPFrequencyAA(aa);
                freq.setESPFrequencyAll(all);
            }
            if (!minorFreqs.isEmpty()) {
                if (!minorFreqs.get(minorAlleleCounter).equals(".")) {
                    float maf = 100f * Float.parseFloat(minorFreqs.get(minorAlleleCounter));
                    //code here to exclude variants with no freq data
                    if (maf != 0){
                        freq.setDbSnpGmaf(maf);
                    }
                }
            }

            if (exACFreqs.containsKey("AN_AFR") && !exACFreqs.get("AN_AFR").equals("0")) {
                float afr = 100f * Integer.parseInt(exACFreqs.get("AC_AFR").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_AFR"));
                freq.setExACFrequencyAfr(afr);
            }
            if (exACFreqs.containsKey("AN_AMR") && !exACFreqs.get("AN_AMR").equals("0")) {
                float amr = 100f * Integer.parseInt(exACFreqs.get("AC_AMR").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_AMR"));
                freq.setExACFrequencyAmr(amr);
            }
            if (exACFreqs.containsKey("AN_EAS") && !exACFreqs.get("AN_EAS").equals("0")) {
                float eas = 100f * Integer.parseInt(exACFreqs.get("AC_EAS").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_EAS"));
                freq.setExACFrequencyEas(eas);
            }
            if (exACFreqs.containsKey("AN_FIN") && !exACFreqs.get("AN_FIN").equals("0")) {
                float fin = 100f * Integer.parseInt(exACFreqs.get("AC_FIN").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_FIN"));
                freq.setExACFrequencyFin(fin);
            }
            if (exACFreqs.containsKey("AN_NFE") && !exACFreqs.get("AN_NFE").equals("0")) {
                float nfe = 100f * Integer.parseInt(exACFreqs.get("AC_NFE").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_NFE"));
                freq.setExACFrequencyNfe(nfe);
            }
            if (exACFreqs.containsKey("AN_OTH") && !exACFreqs.get("AN_OTH").equals("0")) {
                float oth = 100f * Integer.parseInt(exACFreqs.get("AC_OTH").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_OTH"));
                freq.setExACFrequencyOth(oth);
            }
            if (exACFreqs.containsKey("AN_SAS") && !exACFreqs.get("AN_SAS").equals("0")) {
                float sas = 100f * Integer.parseInt(exACFreqs.get("AC_SAS").split(",")[minorAlleleCounter - 1]) / Integer.parseInt(exACFreqs.get("AN_SAS"));
                freq.setExACFrequencySas(sas);
            }
            
          
            frequencyList.add(freq);
            minorAlleleCounter++;
        }
        return frequencyList;
    }

    /**
     * @param rsId A dbSNP rsID such as rs101432848. In rare cases may be
     * multiple e.g., rs200118651;rs202059104 (then just take last id)
     * @return int value of id with the 'rs' removed
     */
    private int rsIdToInt(String rsId) {
        String[] rsIdFields = rsId.split(";");
        if (rsIdFields.length > 1) {
            return rsIdToInt(rsIdFields[rsIdFields.length - 1]);
        }
        /*
         * If we get here there is just one rsID
         */
        if (rsId.startsWith("rs")) {
            return Integer.parseInt(rsId.substring(2));
        }
        //return Constants.NO_RSID;
        return 0;
    }

}
