/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import static de.charite.compbio.exomiser.core.model.frequency.FrequencySource.*;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    private final Map<FrequencySource, String> frequencySourceColumnMappings;
    
    public DefaultFrequencyDao() {
        frequencySourceColumnMappings = new LinkedHashMap<>();
        frequencySourceColumnMappings.put(THOUSAND_GENOMES, "dbSNPmaf");
        frequencySourceColumnMappings.put(ESP_AFRICAN_AMERICAN, "espAAmaf");
        frequencySourceColumnMappings.put(ESP_EUROPEAN_AMERICAN, "espEAmaf");
        frequencySourceColumnMappings.put(ESP_ALL, "espAllmaf");
        frequencySourceColumnMappings.put(EXAC_AFRICAN_INC_AFRICAN_AMERICAN, "exacAFRmaf");
        frequencySourceColumnMappings.put(EXAC_AMERICAN, "exacAMRmaf");
        frequencySourceColumnMappings.put(EXAC_EAST_ASIAN, "exacEASmaf");
        frequencySourceColumnMappings.put(EXAC_FINISH, "exacFINmaf");
        frequencySourceColumnMappings.put(EXAC_NON_FINISH_EUROPEAN, "exacNFEmaf");
        frequencySourceColumnMappings.put(EXAC_SOUTH_ASIAN, "exacSASmaf");
        frequencySourceColumnMappings.put(EXAC_OTHER, "exacOTHmaf");
        
        logger.debug("FrequencySource to columnLabel mappings: {}", frequencySourceColumnMappings);
    }

    
    
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
        Set<Frequency> frequencies = new HashSet<>();

        if (rs.next()) {
            rsId = makeRsId(rs);
            frequencies = makeFrequencies(rs, frequencies);
        }
        FrequencyData frequencyData = new FrequencyData(rsId, frequencies);

        logger.debug("Made new {}", frequencyData);

        return frequencyData;
    }

    private RsId makeRsId(ResultSet rs) throws SQLException {
        int dbSNPid = rs.getInt("rsid");
        if (!rs.wasNull() && dbSNPid != 0) {
            return new RsId(dbSNPid);
        }
        return null;
    }
    
    private Set<Frequency> makeFrequencies(ResultSet rs, Set<Frequency> frequencies) throws SQLException {
        for (Entry<FrequencySource, String> sourceColumnMapping : frequencySourceColumnMappings.entrySet()) {
            FrequencySource source = sourceColumnMapping.getKey();
            String columnLabel = sourceColumnMapping.getValue();
            float freq = rs.getFloat(columnLabel);
            if (!rs.wasNull() && freq != 0f) {
                frequencies.add(new Frequency(freq, source));
            }
        }
        return frequencies;
    }

}
