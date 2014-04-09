package de.charite.compbio.exomiser.sim;

import de.charite.compbio.exomiser.config.AppConfig;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * /**
 * This class can be used to simulate exome data on the basis of the minor
 * allele frequencies in the ESP data. The class assumes that the posgreSQL
 * database table frequency has been created by other programs in the exomizer
 * suite.
 *
 * @author Peter N Robinson
 * @version 0.04 (25 November, 2013)
 */
@Component(value = "exomeSimulator")
public class ExomeSimulator {

    private static final Logger logger = LoggerFactory.getLogger(ExomeSimulator.class);

    @Autowired
    private DataSource dataSource;

    private static final String VCF_HEADER = String.format(
            "##fileformat=VCFv4.1%n"
            + "##FORMAT=<ID=AD,Number=.,Type=Integer,Description=\"Allelic depths for the ref and alt alleles\">%n"
            + "##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Approximate read depth\">%n"
            + "##FORMAT=<ID=GQ,Number=1,Type=Float,Description=\"Genotype Quality\">%n"
            + "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">%n"
            + "##INFO=<ID=QD,Number=1,Type=Float,Description=\"Variant Confidence/Quality by Depth\">%n"
            + "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	sample%n");

    public ExomeSimulator() {
        
        if (dataSource == null) {
            logger.error("Null DataSource - check jdbc.properties is in classpath");
//            System.exit(-1);
        } 
//        dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl("jdbc:postgresql://nsfp-db:5443/nsfpalizer");
//        dataSource.setUsername("nsfp");
//        dataSource.setPassword("vcfanalysis");
    }

    /**
     * This will be return the relevant column name to get data from ESP
     * project: ALL, EA (European American) or AA (African American).
     */
    private String convertPopulationGroupCode(String type) {
        switch (type) {
            case "ALL":
                return "espallmaf";
            case "AA":
                return "espaamaf";
            case "EA":
                return "espeamaf";
            default:
                return null;
        }

    }

    /**
     * Output variants for a single chromosome based on data in the Exomiser
     * database (from the table "frequency", which contains data from dbSNP and
     * from ESP).
     *
     * @param out File handle to write to
     * @param populationGroup
     * @param chrom Integer representing current Chromosome (1-22, X=23, Y=24,
     * M=25)
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    private void outputChromosome(BufferedWriter out, String populationGroup, int chrom) {

        logger.info("Writing out variants for chromosome {}", chrom);

        String popGroupColumn = convertPopulationGroupCode(populationGroup);

        String frequencyQuery = String.format("SELECT rsid, \"position\", ref, alt, %s "
                + "FROM frequency "
                + "WHERE chromosome = ? "
                + "ORDER BY \"position\", ref, alt", popGroupColumn);
//      
        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(frequencyQuery);) {
            Random ran = new Random();
//            preparedStatement.setString(1, popGroupColumn);
            preparedStatement.setInt(1, chrom);
            ResultSet rs = preparedStatement.executeQuery();
            int n = 0;
            while (rs.next()) {
                int dbSNPid = rs.getInt(1);
                int position = rs.getInt(2);
                String ref = rs.getString(3);
                String alt = rs.getString(4);
                double maf = rs.getDouble(5); /* minor allele frequency. */

                /* Note that the database stores the frequencies as
                 percentages. Therefore, we divide by 100 to get
                 a frequency. We call the minor allele frequeny "p".
                 */
                double p = maf / 100f;
                double q = 1f - p;
                /* We calculate the frequencies for
                 homozygous ALT and het under the assumption
                 of Hardy Weinberg equilibrium */

                double het = 2f * p * q;
                double homalt = p * p;
                /* Calculate cumulative frequency ranges
                 A: homozygous REF -- skip
                 B: het
                 C: homozygous alt.
                 */
                double A = 1f - het - homalt;
                double B = 1f - homalt;

                double r = ran.nextDouble();
                if (r < A) {
                    continue; /* Hom REF */

                }
                /**
                 * Create appropriate Strings for the Chromosome and the
                 * variant, and if available the dbSNP ID .
                 */
                String chr = null;
                if (chrom < 23) {
                    chr = String.format("%d", chrom);
                } else if (chrom == 23) {
                    chr = "X";
                } else if (chrom == 24) {
                    chr = "Y";
                } else if (chrom == 25) {
                    chr = "M";
                } else {
                    logger.error("Bad chromosome integer in database: " + chrom);
                    System.exit(1);
                }
                String id = null;
                if (dbSNPid < 0) {
                    id = ".";
                } else {
                    id = String.format("rs%d", dbSNPid);
                }
                String var = String.format("%s\t%d\t%s\t%s\t%s\t100\tPASS\tQD=11.71;\tGT:GQ", chr, position, id, ref, alt);

                if (r < B) {
                    /* Heterozygous */
                    String s = String.format("%s\t0/1:99%n", var);
                    out.write(s);
                } else {
                    /* Zone C: homozygous ALT */
                    String s = String.format("%s\t1/1:99%n", var);
                    out.write(s);
                }
                n++;
            }
            out.flush();
            logger.info("Wrote " + n + " variants for chromosome " + chrom);
        } catch (SQLException | IOException ex) {
            logger.error("Problem writing output chromosome - check Database connection or filepath", ex);
        }

    }

    public File outputExome(String populationGroup, String outputPath) {
        
        File file = new File(outputPath);
        try (FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream)) {
            logger.info("Simulating whole exome for populationGroup {}", populationGroup);
            logger.info("VCF file will be written to {}", outputPath);
            out.write(VCF_HEADER);

            for (int i = 1; i <= 25; ++i) {
                outputChromosome(out, populationGroup, i);
            }
        } catch (IOException e) {

        }
        return file;
    }

    public File outputChromosome(String populationGroup, int chrom, String outputPath) {
        
        File file = new File(outputPath);
        try (FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream)) {
            logger.info("Simulating chromosomal exome for Chr {} populationGroup {}.", chrom, populationGroup);
            logger.info("VCF file will be written to {}", outputPath);
            out.write(VCF_HEADER);

            outputChromosome(out, populationGroup, chrom);

        } catch (IOException e) {

        }
        return file;
    }

}
