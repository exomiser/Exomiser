/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class TadDao {

    private final Logger logger = LoggerFactory.getLogger(TadDao.class);

    @Autowired
    private DataSource dataSource;

    @Cacheable(value = "tad", key = "#variant.chromosomalVariant")
    public List<String> getGenesInTad(Variant variant) {
        List<String> genesInTad = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                String geneSymbol = rs.getString(1);
                genesInTad.add(geneSymbol);
            }
        } catch (SQLException e) {
            logger.error("Error executing topologically associated domain query: ", e);
        }
        return genesInTad;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String tadQuery = "select symbol from tad where chromosome = ? and start < ? and \"end\" > ?";
        PreparedStatement ps = connection.prepareStatement(tadQuery);
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setInt(3, variant.getPosition());
        return ps;
    }

    public List<TopologicalDomain> getAllTads() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("select CHROMOSOME as chr, START as start, \"end\" as end, ENTREZID as geneId, SYMBOL as geneSymbol from tad");
             ResultSet rs = preparedStatement.executeQuery()) {
            return createTads(rs);

        } catch (SQLException e) {
            logger.error("Error executing topologically associated domain query: ", e);
        }
        return Collections.emptyList();
    }

    private List<TopologicalDomain> createTads(ResultSet rs) throws SQLException {
        List<TopologicalDomain> tads = new ArrayList<>();
        int chr = 0;
        int start = 0;
        int end = 0;
        Map<String, Integer> geneIdentifiers = new LinkedHashMap<>();
        while (rs.next()) {
            int currentChr = rs.getInt("chr");
            int currentStart = rs.getInt("start");
            int currentEnd = rs.getInt("end");
            String geneSymbol = rs.getString("geneSymbol");
            Integer geneId = rs.getInt("geneId");

            //first row
            if (chr == 0 && start == 0 && end == 0) {
                chr = currentChr;
                start = currentStart;
                end = currentEnd;
                geneIdentifiers.put(geneSymbol, geneId);
                continue;
            }
            //TADs probably don't overlap, but this is biology so at least one probably will.
            if (currentChr != chr || currentStart != start || currentEnd != end) {
                TopologicalDomain previousTad = new TopologicalDomain(chr, start, end, geneIdentifiers);
                tads.add(previousTad);
                geneIdentifiers = new LinkedHashMap<>();
            }
            chr = currentChr;
            start = currentStart;
            end = currentEnd;
            geneIdentifiers.put(geneSymbol, geneId);
        }
        TopologicalDomain finalTad = new TopologicalDomain(chr, start, end, geneIdentifiers);
        tads.add(finalTad);

        return tads;
    }

}
