package de.charite.compbio.exomiser.filter;

import de.charite.compbio.exomiser.dao.FrequencyVariantScoreDao;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exception.ExomizerSQLException;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import jannovar.common.Constants;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Filter variants according to their frequency. The Frequency is retrieved from
 * our database and comes from dbSNP (see
 * {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser} and
 * {@link exomizer.io.ESP2FrequencyParser ESP2FrequencyParser}), and the
 * frequency data are expressed as percentages.
 *
 * @author Peter N Robinson
 * @version 0.09 (April 28, 2013)
 */
@Component
public class FrequencyFilter implements Filter {

    /**
     * Threshold for filtering. Retain only those variants whose frequency
     * (expressed as a percentage) is below this threshold. The default value is
     * 100%, i.e., no filtering out.
     */
    private float maxFreq = 100.0f;

    private Connection connection;

    private final Logger logger = LoggerFactory.getLogger(FrequencyFilter.class);

    @Autowired
    private final FrequencyVariantScoreDao triageDao;

//    /** A prepared SQL statement for querying from the frequency table. */
//    private PreparedStatement preparedFrequencyQuery = null;
//  
    private final FilterType filterType = FilterType.FREQUENCY_FILTER;

    /**
     * A flag to indicate we could not retrieve data from the database for some
     * variant. Using the value 200% means that the variant will not fail the
     * ESP filter.
     */
    private static final float NO_ESP_DATA = 200.0f;

    /**
     * Frequency of the variant from [0..1] in the Exome Server Project (ESP)
     * data, expressed as a percentage.
     */
    private float ESP_freq;
    private List<String> errorRecord = new ArrayList<>();
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = new ArrayList<>();
    /**
     * Number of variants analyzed by filter
     */
    private int n_before;
    /**
     * Number of variants passing filter
     */
    private int n_after;
    /**
     * Filter out variants if they are represented at all in dbSNP or ESP,
     * regardless of frequency.
     */
    private boolean strictFiltering = false;

    /**
     * Creates a filter with a maximum frequency threshold for variants.
     *
     * @param maxFreq sets the maximum frequency threshold (percent value) of
     * the minor allele required to pass the filer. For example a value of 1
     * will set the threshold of the minor allele frequency to under 1%.
     * @param filterOutAllDbsnp removes all variants found in the dbSNP or in
     * the ESP database regardless of their frequency.
     *
     */
    public FrequencyFilter(FrequencyVariantScoreDao triageDao, float maxFreq, boolean filterOutAllDbsnp) {
        this.triageDao = triageDao;

        setMaxFrequency(maxFreq);
        this.strictFiltering = filterOutAllDbsnp;

    }

    /**
     * @return Name of filter (is used for the HTML output).
     */
    @Override
    public String getFilterName() {
        return "Frequency filter";
    }

    /**
     * Flag to output results of filtering against frequency with Thousand
     * Genomes and ESP data.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    private void setMaxFrequency(float maxFreq) {
        this.maxFreq = maxFreq;
        try {
            //why not just do this here?
            FrequencyVariantScore.setMaxFreqThreshold(this.maxFreq);
        } catch (ExomizerInitializationException e) {
            logger.error(null, e);
        }
        this.messages.add(String.format("Allele frequency &lt; %.2f %%", this.maxFreq));
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of frequency filtering.
     */
    @Override
    public List<String> getMessages() {
        if (this.errorRecord.size() > 0) {
            for (String s : errorRecord) {
                this.messages.add("Error: " + s);
            }
        }
        return this.messages;
    }

    /**
     * Get number of variants before filter was applied
     */
    @Override
    public int getBefore() {
        return this.n_before;
    }

    /**
     * Get number of variants after filter was applied
     */
    @Override
    public int getAfter() {
        return this.n_after;
    }

    /**
     * Filter out list of variants on the basis of the estimated frequency of
     * the variant in the population. If the variant in question has a higher
     * frequency than the threshold in either the dbSNP data or the ESP data,
     * then we remove the variant from further consideration.
     *
     * @param variant_list a list of Variants to be tested for rarity.
     */
    @Override
    public void filterVariants(List<VariantEvaluation> variant_list) {
        Iterator<VariantEvaluation> it = variant_list.iterator();
        int n_dbSNP_frequency_data_found = 0;
        int n_dbSNP_rsID_found = 0;
        int n_ESP_frequency_data_found = 0;

        this.n_before = variant_list.size();
        while (it.hasNext()) {
            VariantEvaluation ve = it.next();
            Variant v = ve.getVariant();
//	    try {
//		FrequencyVariantScore ft = retrieve_frequency_data(v);
            FrequencyVariantScore ft = triageDao.getVariantScore(v);
            if (ft.hasFrequencyDataFrom_dbSNP()) {
                n_dbSNP_frequency_data_found++;
            }
            if (ft.has_dbSNPrsID()) {
                n_dbSNP_rsID_found++;
            }
            if (ft.hasFrequencyDataFromESP()) {
                n_ESP_frequency_data_found++;
            }

            if (this.strictFiltering) {
                if (ft.representedInDatabase()) {
                    it.remove();
                } else {
                    ve.addFilterTriage(ft, FilterType.FREQUENCY_FILTER);
                }
            } else {
                if (!ft.passesFilter()) {
                    // Variant is not rare, discard it.
                    it.remove();
                } else {
                    // We passed the filter (Variant is rare).
                    ve.addFilterTriage(ft, FilterType.FREQUENCY_FILTER);
                }
            }

//	    } catch (ExomizerException e) {
//		errorRecord.add(e.toString());
//	    }
        }
        this.n_after = variant_list.size();
        String s
                = String.format("Frequency Data available in dbSNP (for 1000 Genomes Phase I) for %d variants (%.1f%%)",
                        n_dbSNP_frequency_data_found, 100f * (double) n_dbSNP_frequency_data_found / n_before);
        this.messages.add(s);
        s = String.format("dbSNP \"rs\" id available for %d variants (%.1f%%)",
                n_dbSNP_rsID_found, 100 * (double) n_dbSNP_rsID_found / n_before);
        this.messages.add(s);
        s = String.format("Data available in Exome Server Project for %d variants (%.1f%%)",
                n_ESP_frequency_data_found, 100f * (double) n_ESP_frequency_data_found / n_before);
        this.messages.add(s);
    }

    /**
     * Note that current, ESP only holds frequency data for nucleotide
     * substitutions. Therefore, just skip this step for other kinds of
     * variants. Use the flag NO_ESP_DATA to indicate this.
     *
     * @param v A Variant whose frequency is to be retrieved from the SQL
     * database by this function.
     */
    private FrequencyVariantScore retrieve_frequency_data(Variant v) throws ExomizerSQLException {
        int dbSNPid = Constants.UNINITIALIZED_INT;
        float espEAmaf = Constants.UNINITIALIZED_FLOAT;
        float espAAmaf = Constants.UNINITIALIZED_FLOAT;
        float espAllmaf = Constants.UNINITIALIZED_FLOAT;
        float dbSNPmaf = Constants.UNINITIALIZED_FLOAT;

        int chrom = v.get_chromosome();
        int position = v.get_position();
        String ref = v.get_ref();
        String alt = v.get_alt();

        // Added order by clause as sometimes have multiple rows for the same position, ref and alt and first row may have no freq data
        // Can remove if future versions of database remove these duplicated rows
        String frequencyQuery = "SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf "
                + "FROM frequency "
                + "WHERE chromosome = ? "
                + "AND position = ? "
                + "AND ref = ? "
                + "AND alt = ? "
                + "ORDER BY dbsnpmaf desc, espeamaf desc, espaamaf desc, espallmaf desc ";

        try (PreparedStatement preparedFrequencyQuery = connection.prepareStatement(frequencyQuery);) {
            preparedFrequencyQuery.setInt(1, chrom);
            preparedFrequencyQuery.setInt(2, position);
            preparedFrequencyQuery.setString(3, ref);
            preparedFrequencyQuery.setString(4, alt);
            //System.out.println(preparedFrequencyQuery);
            ResultSet rs = preparedFrequencyQuery.executeQuery();
            if (rs.next()) { /* The way the db was constructed, there is just one line for each such query. */
                /* Corresponds to SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf */

                dbSNPid = rs.getInt(1);
                dbSNPmaf = rs.getFloat(2);
                espEAmaf = rs.getFloat(3);
                espAAmaf = rs.getFloat(4);
                espAllmaf = rs.getFloat(5);
                //System.out.println(String.format("dbSNPid=rs%d//dbSNPmaf=%.2f//espAllmaf=%.2f",dbSNPid,dbSNPmaf,espAllmaf));
            }
            rs.close();
        } catch (SQLException e) {
            throw new ExomizerSQLException("Error executing ESP query: " + e);
        }

        FrequencyVariantScore ft = new FrequencyVariantScore(dbSNPid, dbSNPmaf, espEAmaf, espAAmaf, espAllmaf);
        return ft;
    }

    /**
     * Prepare the SQL query statements required for this filter.
     * <p>
     * SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf </br>
     * FROM frequency </br>
     * WHERE chromosome = ?</br>
     * AND position = ? </br>
     * AND ref = ? </br>
     * AND alt = ?
     * <p>
     */
//    private void setUpSQLPreparedStatements() {
//	// Added order by clause as sometimes have multiple rows for the same position, ref and alt and first row may have no freq data
//	// Can remove if future versions of database remove these duplicated rows
//	String frequencyQuery = "SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf " +
//	    "FROM frequency " +
//	    "WHERE chromosome = ? "+
//	    "AND position = ? " +
//	    "AND ref = ? " +
//	    "AND alt = ? " +
//	    "ORDER BY dbsnpmaf desc, espeamaf desc, espaamaf desc, espallmaf desc ";
//        
//        try {
//	    this.preparedFrequencyQuery  = connection.prepareStatement(frequencyQuery);
//	  
//        } catch (SQLException e) {
//	    String error = "Problem setting up SQL query:" + frequencyQuery;
////	    logger.error();
//        }
//	
//    }
//    /**
//     * Use connection to postgreSQL database that was initialized either by the 
//     * Exomizer or by the tomcat program (ExomeWalker etc). Also call the function 
//     * {@link #setUpSQLPreparedStatements}.*/
    public void setDatabaseConnection(java.sql.Connection connection) throws ExomizerInitializationException {
//	this.connection = connection;
//	if (connection == null) {
//	    String e = "[FrequencyFilter.java] setDatabaseConnection: connection was null";
//	    throw new ExomizerInitializationException(e);
//	}
//       	setUpSQLPreparedStatements();
    }

    public boolean displayInHTML() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s: maximum frequency threshold=%s filter out dbSNP and ESP=%s", filterType, maxFreq, strictFiltering);
    }
}
