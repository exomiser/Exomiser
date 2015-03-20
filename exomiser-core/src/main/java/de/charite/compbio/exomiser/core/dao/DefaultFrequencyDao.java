/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * Default implementation of the FrequencyDao. Can be configured to use caching.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class DefaultFrequencyDao implements FrequencyDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultFrequencyDao.class);
    @Autowired
    private DataSource dataSource;

    @Cacheable(value = "frequency", key = "#variant.chromosomalVariant")
    @Override
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
        String frequencyQuery = "SELECT rsid, dbSNPmaf, espEAmaf, espAAmaf, espAllmaf, exacAFRmaf,  exacAMRmaf, exacEASmaf, exacFINmaf, exacNFEmaf, exacOTHmaf, exacSASmaf "
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
        Frequency exacAFR = null;
        Frequency exacAMR = null;
        Frequency exacEAS = null;
        Frequency exacFIN = null;
        Frequency exacNFE = null;
        Frequency exacOTH = null;
        Frequency exacSAS = null;

        if (rs.next()) { /*
             * The way the db was constructed, there is just one line for each
             * such query.
             */
            /*
             * Corresponds to SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf
             */

            int dbSNPid = rs.getInt(1);
            if (!rs.wasNull() && dbSNPid != 0) {
                rsId = new RsId(dbSNPid);
            }
            float dbSNPmaf = rs.getFloat(2);
            if (!rs.wasNull() && dbSNPmaf != 0f) {
                dbSnp = new Frequency(dbSNPmaf);
            }
            float espEAmaf = rs.getFloat(3);
            if (!rs.wasNull() && espEAmaf != 0f) {
                espEA = new Frequency(espEAmaf);
            }
            float espAAmaf = rs.getFloat(4);
            if (!rs.wasNull() && espAAmaf != 0f) {
                espAA = new Frequency(espAAmaf);
            }
            float espAllmaf = rs.getFloat(5);
            if (!rs.wasNull() && espAllmaf != 0f) {
                espAll = new Frequency(espAllmaf);
            }
            float exacAFRmaf = rs.getFloat(6);
            if (!rs.wasNull() && exacAFRmaf != 0f) {
                exacAFR = new Frequency(exacAFRmaf);
            }
            float exacAMRmaf = rs.getFloat(7);
            if (!rs.wasNull() && exacAMRmaf != 0f) {
                exacAMR = new Frequency(exacAMRmaf);
            }
            float exacEASmaf = rs.getFloat(8);
            if (!rs.wasNull() && exacEASmaf != 0f) {
                exacEAS = new Frequency(exacEASmaf);
            }
            float exacFINmaf = rs.getFloat(9);
            if (!rs.wasNull() && exacFINmaf != 0f) {
                exacFIN = new Frequency(exacFINmaf);
            }
            float exacNFEmaf = rs.getFloat(10);
            if (!rs.wasNull() && exacNFEmaf != 0f) {
                exacNFE = new Frequency(exacNFEmaf);
            }
            float exacOTHmaf = rs.getFloat(11);
            if (!rs.wasNull() && exacOTHmaf != 0f) {
                exacOTH = new Frequency(exacOTHmaf);
            }
            float exacSASmaf = rs.getFloat(12);
            if (!rs.wasNull() && exacSASmaf != 0f) {
                exacSAS = new Frequency(exacSASmaf);
            }
        }
        FrequencyData frequencyData = new FrequencyData(rsId, dbSnp, espAll, espAA, espEA, exacAFR, exacAMR, exacEAS, exacFIN, exacNFE, exacOTH, exacSAS);

        logger.debug("Made new {}", frequencyData);

        return frequencyData;
    }
}
