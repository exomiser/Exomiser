/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.frequency.Frequency;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.frequency.RsId;
import jannovar.common.Constants;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class FrequencyDao {

    private final Logger logger = LoggerFactory.getLogger(FrequencyDao.class);

    @Autowired
    private DataSource dataSource;

    public FrequencyDao() {
    }

    public FrequencyData getFrequencyData(Variant variant) {

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedFrequencyQuery = createPreparedStatement(connection, variant);
                ResultSet rs = preparedFrequencyQuery.executeQuery()) {

            return processResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing frequency query: ", e);
        }
        return null;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        // Added order by clause as sometimes have multiple rows for the same position, ref and alt and first row may have no freq data
        // Can remove if future versions of database remove these duplicated rows

        //TODO: optimise this query to remove the order by 
        String frequencyQuery = "SELECT rsid, dbSNPmaf, espEAmaf, espAAmaf, espAllmaf "
                + "FROM frequency "
                + "WHERE chromosome = ? "
                + "AND position = ? "
                + "AND ref = ? "
                + "AND alt = ? "
                + "ORDER BY dbsnpmaf desc, espeamaf desc, espaamaf desc, espallmaf desc ";
        PreparedStatement ps = connection.prepareStatement(frequencyQuery);

        ps.setInt(1, variant.get_chromosome());
        ps.setInt(2, variant.get_position());
        ps.setString(3, variant.get_ref());
        ps.setString(4, variant.get_alt());

        return ps;
    }

    private FrequencyData processResults(ResultSet rs) throws SQLException {
        

        RsId rsId = null;
        Frequency dbSnp = null;
        Frequency espAll = null;
        Frequency espAA = null;
        Frequency espEA = null;

        if (rs.next()) { /* The way the db was constructed, there is just one line for each such query. */
            /* Corresponds to SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf */

            int dbSNPid = rs.getInt(1);
            if (!rs.wasNull() && dbSNPid != Constants.UNINITIALIZED_INT &&  dbSNPid != Constants.NO_RSID && dbSNPid != Constants.NOPARSE) {
                rsId = new RsId(dbSNPid);
            }
            float dbSNPmaf = rs.getFloat(2);
            if (!rs.wasNull() && dbSNPmaf != Constants.UNINITIALIZED_FLOAT) {
                dbSnp = new Frequency(dbSNPmaf);
            }
            float espEAmaf = rs.getFloat(3);
            if (!rs.wasNull() && espEAmaf != Constants.UNINITIALIZED_FLOAT) {
                espEA = new Frequency(espEAmaf);
            }
            float espAAmaf = rs.getFloat(4);
            if (!rs.wasNull() && espAAmaf != Constants.UNINITIALIZED_FLOAT) {
                espAA = new Frequency(espAAmaf);
            }
            float espAllmaf = rs.getFloat(5);
            if (!rs.wasNull() && espAllmaf != Constants.UNINITIALIZED_FLOAT) {
                espAll = new Frequency(espAllmaf);
            }
        }
        FrequencyData frequencyData = new FrequencyData(rsId, dbSnp, espAll, espAA, espEA);
        
        logger.debug("Made new {}", frequencyData);

        return frequencyData;
    }

}
