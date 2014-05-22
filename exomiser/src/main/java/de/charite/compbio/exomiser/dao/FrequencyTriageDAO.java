/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.dao;

import de.charite.compbio.exomiser.filter.FrequencyTriage;
import jannovar.common.Constants;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO class for retrieving {@code de.charite.compbio.exomiser.filter.FrequencyTriage}
 * objects for from the database.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyTriageDAO implements TriageDAO {

    private final Logger logger = LoggerFactory.getLogger(FrequencyTriageDAO.class);

    private final DataSource dataSource;

    public FrequencyTriageDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    
    @Override
    public FrequencyTriage getTriageData(Variant variant) {

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedFrequencyQuery = createPreparedStatement(connection, variant);
                ResultSet rs = preparedFrequencyQuery.executeQuery()) {
                
            return processTriageResults(rs);
            
        } catch (SQLException e) {
            logger.error("Error executing ESP query: ", e);
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

    private FrequencyTriage processTriageResults(ResultSet rs) throws SQLException {
        int dbSNPid = Constants.UNINITIALIZED_INT;
        float espEAmaf = Constants.UNINITIALIZED_FLOAT;
        float espAAmaf = Constants.UNINITIALIZED_FLOAT;
        float espAllmaf = Constants.UNINITIALIZED_FLOAT;
        float dbSNPmaf = Constants.UNINITIALIZED_FLOAT;

        if (rs.next()) { /* The way the db was constructed, there is just one line for each such query. */
                /* Corresponds to SELECT rsid,dbSNPmaf,espEAmaf,espAAmaf,espAllmaf */

                dbSNPid = rs.getInt(1);
                dbSNPmaf = rs.getFloat(2);
                espEAmaf = rs.getFloat(3);
                espAAmaf = rs.getFloat(4);
                espAllmaf = rs.getFloat(5);
                //System.out.println(String.format("dbSNPid=rs%d//dbSNPmaf=%.2f//espAllmaf=%.2f",dbSNPid,dbSNPmaf,espAllmaf));
            }
        FrequencyTriage ft = new FrequencyTriage(dbSNPid, dbSNPmaf, espEAmaf, espAAmaf, espAllmaf);
        return ft;
    }
}
