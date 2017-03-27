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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import com.google.common.collect.Maps;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Default implementation of the FrequencyDao. Can be configured to use caching.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class DefaultFrequencyDao implements FrequencyDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultFrequencyDao.class);

    private final DataSource dataSource;

    private final Map<FrequencySource, String> frequencySourceColumnMappings;

    @Autowired
    public DefaultFrequencyDao(DataSource dataSource) {
        this.dataSource = dataSource;

        Map<FrequencySource, String> frequencyMap = new EnumMap<>(FrequencySource.class);
        frequencyMap.put(FrequencySource.THOUSAND_GENOMES, "dbSNPmaf");
        frequencyMap.put(FrequencySource.ESP_AFRICAN_AMERICAN, "espAAmaf");
        frequencyMap.put(FrequencySource.ESP_EUROPEAN_AMERICAN, "espEAmaf");
        frequencyMap.put(FrequencySource.ESP_ALL, "espAllmaf");
        frequencyMap.put(FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN, "exacAFRmaf");
        frequencyMap.put(FrequencySource.EXAC_AMERICAN, "exacAMRmaf");
        frequencyMap.put(FrequencySource.EXAC_EAST_ASIAN, "exacEASmaf");
        frequencyMap.put(FrequencySource.EXAC_FINNISH, "exacFINmaf");
        frequencyMap.put(FrequencySource.EXAC_NON_FINNISH_EUROPEAN, "exacNFEmaf");
        frequencyMap.put(FrequencySource.EXAC_SOUTH_ASIAN, "exacSASmaf");
        frequencyMap.put(FrequencySource.EXAC_OTHER, "exacOTHmaf");
        frequencySourceColumnMappings = Maps.immutableEnumMap(frequencyMap);
        logger.debug("FrequencySource to columnLabel mappings: {}", frequencySourceColumnMappings);
    }

    
    
    @Cacheable(value = "frequency", key = "#variant.hgvsGenome")
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
        return FrequencyData.EMPTY_DATA;
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

        // FIXME(holtgrewe): The position comes directly from the GenomeChange in variant. This is fine. Currently, I'm
        // converting from the 0-based positions in new Jannovar's GenomeChange to 1-based for Exomisers (which is what
        // the old Janovar used). Also, the reference is "" in the case of deletions and alt is "" in the case of
        // insertions. The old representation for either was "-". Changing this will probably
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setString(3, variant.getRef());
        ps.setString(4, variant.getAlt());

        return ps;
    }

    private FrequencyData processResults(ResultSet rs) throws SQLException {

        RsId rsId = null;
        Set<Frequency> frequencies = new HashSet<>();

        if (rs.next()) {
            rsId = makeRsId(rs);
            frequencies = makeFrequencies(rs, frequencies);
        }

        return makeFrequencyData(rsId, frequencies);
    }

    private FrequencyData makeFrequencyData(RsId rsId, Set<Frequency> frequencies) {
        if (rsId == null && frequencies.isEmpty()) {
            return FrequencyData.EMPTY_DATA;
        }
        return new FrequencyData(rsId, frequencies);
    }

    private RsId makeRsId(ResultSet rs) throws SQLException {
        int dbSNPid = rs.getInt("rsid");
        if (!rs.wasNull() && dbSNPid != 0) {
            return RsId.valueOf(dbSNPid);
        }
        return null;
    }
    
    private Set<Frequency> makeFrequencies(ResultSet rs, Set<Frequency> frequencies) throws SQLException {
        for (Entry<FrequencySource, String> sourceColumnMapping : frequencySourceColumnMappings.entrySet()) {
            FrequencySource source = sourceColumnMapping.getKey();
            String columnLabel = sourceColumnMapping.getValue();
            float freq = rs.getFloat(columnLabel);
            if (!rs.wasNull() && freq != 0) {
                frequencies.add(Frequency.valueOf(freq, source));
            }
        }
        return frequencies;
    }

}
