package de.charite.compbio.exomiser.db.build.parsers;

import de.charite.compbio.exomiser.db.build.reference.Frequency;
import jannovar.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We are parsing two different VCF files for population frequency data on
 * variants, the file from ESP and the file from dbSNP. The formats of the two
 * files are similar enough that we extract that into this superclass.
 * <P>
 * This classs encapsulates the functionality of parsing a basic VCF line,
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

    /**
     * This method parses a standard VCF line of a population frequency VCF file
     * from ESP or dbSNP
     * This is an example of the format:
     * #CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO
     * 1       10019   rs376643643     TA      T       .       .       RS=376643643;RSPOS=10020;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000200;WGT=1;VC=DIV;R5;OTHERKG
     * 1       10054   rs373328635     CAA     C,CA    .       .       RS=373328635;RSPOS=10055;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000210;WGT=1;VC=DIV;R5;OTHERKG;NOC
     * 1       10109   rs376007522     A       T       .       .       RS=376007522;RSPOS=10109;dbSNPBuildID=138;SSR=0;SAO=0;VP=0x050000020001000002000100;WGT=1;VC=SNV;R5;OTHERKG
     * 
     * @param line
     * @return a <code>Frequency</code> object created from the input line.
     */
    public static Frequency parseVCFline(String line) {
        
        String fields[] = line.split("\t");
        
        byte chrom = 0;
        try {
            chrom = chromosomeStringToByte(fields[0]);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse chromosome: {}. Error occured parsing line: {}", fields[0], line);
            logger.error("", e.getMessage());
            System.exit(1);
        }
        
        int pos = Integer.parseInt(fields[1]);
        /* Transform rsID to integer to save space. Note that if there are problems with parse we use
         the constant NO_RSID = -1: */
        int rsId = rsIdToInt(fields[2]);
        /* Uppercasing shouldn't be necessary acccording to the VCF standard, but occasionally
         one sees VCF files with lower case for part of the sequences, e.g., to show indels. */
        String ref = fields[3].toUpperCase();
        String alt = fields[4].toUpperCase();
        
        String info = fields[7];
        // VCF files and Annovar-style annotations use different nomenclature for
        // indel variants. We use Annovar.
        transformVCF2AnnovarCoordinates(ref, alt, pos);
        
        return new Frequency(chrom, pos, ref, alt, rsId, info);
    }


    /**
     * VCF files and Annovar-style annotations use different nomenclature for
     * indel variants. This function transforms the {@link #ref},
     * {@link #alt}, and {@link #pos} fields from VCF style to Annovar style. It
     * should be used once for each VCF line and should be called only from the
     * method {@link #parseVCFline}.
     */
    private static void transformVCF2AnnovarCoordinates(String ref, String alt, int pos) {
        if (ref.length() == 1 && alt.length() == 1) {
            /* i.e., single nucleotide variant */
            /* In this case, no changes are needed. */
            return;
        } else if (ref.length() > alt.length()) {
            /* deletion or block substitution */
            String head = ref.substring(0, alt.length());
            /*System.out.println(String.format("1) ref=%s (%d nt), alt=%s (%d nt), head=%s (%d nt)",
             ref,ref.length(),alt,alt.length(),head,head.length()));*/
            /* For instance, if we have ref=TCG, alt=T, there is a two nt deletion, and head is "T" */
            if (head.equals(alt)) {
                pos = pos + head.length(); /* this advances to position of mutation */
                ref = ref.substring(alt.length());
                alt = "-";
            }
        } else if (alt.length() >= ref.length()) {
            /*  insertion or block substitution */
            String head = alt.substring(0, ref.length()); /* get first L nt of ALT (where L is length of REF) */
            /* System.out.println(String.format("2) ref=%s (%d nt), alt=%s (%d nt), head=%s (%d nt)",
             ref,ref.length(),alt,alt.length(),head,head.length()));*/

            if (head.equals(ref)) {
                pos = pos + ref.length() - 1;
                alt = alt.substring(ref.length());
                ref = "-";
            }
        }
    }
    
    /**
     * @param rsId A dbSNP rsID such as rs101432848. In rare cases may be multiple
     * e.g., rs200118651;rs202059104 (then just take last id)
     * @return int value of id with the 'rs' removed
     */
    private static int rsIdToInt(String rsId) {
        String A[] = rsId.split(";");
        if (A.length > 1) {
            return rsIdToInt(A[A.length - 1]);
        }
        /* If we get here there is just one rsID */
        if (rsId.startsWith("rs")) {
            return Integer.parseInt(rsId.substring(2));
        }
        return Constants.NO_RSID;
    }

    /**
     * Gets a byte representation of chromosome. Note that the dbSNP file does
     * not use "chr"
     */
    private static byte chromosomeStringToByte(String chrom) {

        byte chr;
        switch (chrom) {
            case "X":
                chr = Constants.X_CHROMOSOME;
                break;
            case "Y":
                chr = Constants.Y_CHROMOSOME;
                break;
            case "MT":
                chr = Constants.M_CHROMOSOME;
                break;
            default:
                chr = Byte.parseByte(chrom);
                break;
        }
        return chr;
    }

}
